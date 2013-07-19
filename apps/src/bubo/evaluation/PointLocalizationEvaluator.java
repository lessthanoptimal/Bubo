/*
 * Copyright (c) 2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Project BUBO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bubo.evaluation;

import boofcv.struct.GrowQueue_F64;
import bubo.models.kinematics.PredictorRobotVelocity2D;
import bubo.models.sensor.ProjectorRangeBearing2D;
import bubo.simulation.d2.SimulatedRobotTrajectory;
import bubo.simulation.d2.features.PointLandmark;
import bubo.simulation.d2.motion.FixedTrajectoryVel2D;
import bubo.simulation.d2.sensors.RangeBearing;
import bubo.simulation.d2.sensors.SimRangeBearingMeasurement;
import georegression.metric.UtilAngle;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Evaluates localization algorithms for point landmarks in 2D.
 *
 * @author Peter Abeles
 */
public class PointLocalizationEvaluator {

	// simulates the robot's motion
	private SimulatedRobotTrajectory trajectory;
	// simulated sensor model
	private SimRangeBearingMeasurement sensorModel;
	// provides an initial pose estimate
	private InitialPose initialPose;
	// the algorithm being evaluated
	private LocalizationAlgorithm alg;

	// Random number generate for creating initial simulated world state
	private Random rand;

	// state of the world
	private List<PointLandmark> landmarks;
	private Se2_F64 robotPose = new Se2_F64();
	// simulation time
	private long tick = 0;

	// saves error locations
	private GrowQueue_F64 errorsT = new GrowQueue_F64(100);
	private GrowQueue_F64 errorsYaw = new GrowQueue_F64(100);

	public PointLocalizationEvaluator(SimulatedRobotTrajectory trajectory,
									  SimRangeBearingMeasurement sensorModel,
									  InitialPose initialPose )
	{
		this.trajectory = trajectory;
		this.sensorModel = sensorModel;
		this.initialPose = initialPose;

	}

	public void setLocalization( LocalizationAlgorithm alg ) {
		this.alg = alg;
	}

	public void generateWorld( long randSeed , int numberLandmarks , double worldWidth ) {
		this.rand = new Random(randSeed);

		// randomly assign landmark locations
		landmarks.clear();
		for( int i = 0; i < numberLandmarks; i++ ) {
			PointLandmark l = new PointLandmark();
			l.id = i;
			l.x = rand.nextDouble()*worldWidth - worldWidth/2;
			l.y = rand.nextDouble()*worldWidth - worldWidth/2;
			landmarks.add(l);
		}

		// set the initial robot pose away from the outer landmark border
		double w = worldWidth*0.8;
		robotPose.T.x = rand.nextDouble()*w-w/2;
		robotPose.T.y = rand.nextDouble()*w-w/2;
		robotPose.setYaw( 2*(rand.nextDouble()-0.5)*Math.PI);

		// initialize algorithm
		initialPose.generatePose(robotPose);
		alg.setInitial(initialPose.getPoseNoisy(),initialPose.getSigmaLocation(),initialPose.getSigmaYaw());
		List<Point2D_F64> pts = new ArrayList<Point2D_F64>();
		for( PointLandmark p : landmarks )
			pts.add(p);
		alg.configure(pts,sensorModel.getMeasurementCovariance());

		// reset simulation time
		this.tick = 0;
	}

	public void step() {
		// observe landmarks
		sensorModel.setSensorPose(robotPose);

		List<RangeBearing> measNoisy = new ArrayList<RangeBearing>();
		List<RangeBearing> measTruth = new ArrayList<RangeBearing>();
		for( PointLandmark l : landmarks ) {
			if( sensorModel.process(l) ) {
				measNoisy.add( new RangeBearing(l.id,sensorModel.getNoisyRange(),sensorModel.getNoisyBearing()));
				measTruth.add( new RangeBearing(l.id,sensorModel.getTrueRange(),sensorModel.getTrueBearing()));
			}
		}

		// update pose estimate and evaluate accuracy
		alg.update(measNoisy,measTruth);
		evaluate();

		// move the robot and passe on noisy information to estimation algorithm
		trajectory.update();
		robotPose.set(trajectory.getPose());
		double u[] = trajectory.getControlTruth();
		alg.predict(u[0],u[1]);
		tick++;
	}

	protected void evaluate() {
		Se2_F64 estimated = alg.getPoseEstimate();

		double locationError = estimated.getTranslation().distance(robotPose.getTranslation());
		double angleError = UtilAngle.dist(estimated.getYaw(),robotPose.getYaw());

		errorsT.push(locationError);
		errorsYaw.push(angleError);

		System.out.printf("Errors loc = %5.2f  ang = %5.2f\n",locationError,angleError);
	}

	public static void main( String args[] ) {

		double T=0.1;

		PredictorRobotVelocity2D predictor = new PredictorRobotVelocity2D(0.1,0.1,0.1,0.1);
		ProjectorRangeBearing2D projector = new ProjectorRangeBearing2D();

		SimulatedRobotTrajectory trajectory = new FixedTrajectoryVel2D(1,0.1,0,0,T,234234);
		SimRangeBearingMeasurement sensorModel = new SimRangeBearingMeasurement(0.2,0.01,50,83832);
		InitialPose initialPose = new InitialGaussianPose();

		PointLocalizationEvaluator evaluator = new PointLocalizationEvaluator(trajectory,sensorModel,initialPose);

		evaluator.setLocalization(new WrapLocalizationEkfKnownAssoc(predictor,projector,T));
	}
}
