/*
 * Copyright (c) 2013-2014, Peter Abeles. All Rights Reserved.
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

package bubo.io.rawlog.data;

import bubo.io.LittleEndianIO;
import bubo.io.rawlog.RawlogDecoder;
import bubo.io.rawlog.RawlogSerializableCustom;

import java.io.IOException;
import java.io.OutputStream;


/**
 * @author Peter Abeles
 */
public class CActionRobotMovement2D extends CAction implements RawlogSerializableCustom {
	public TEstimationMethod estimationMethod;

	public CPosePDF poseChange;

	// If "true" means that "encoderLeftTicks" and "encoderRightTicks" contain valid values.
	public boolean hasEncodersInfo;

	/**
	 * This is the raw odometry reading, and only is used when "estimationMethod" is "TEstimationMethod::emOdometry"
	 */
	public CPose2D rawOdometryIncrementReading;


	/**
	 * For odometry only: the ticks count for each wheel FROM the last reading (positive means FORWARD, for both wheels);
	 * \sa hasEncodersInfo
	 */
	public int encoderLeftTicks, encoderRightTicks;

	/**
	 * If "true" means that "velocityLin" and "velocityAng" contain valid values.
	 */
	public boolean hasVelocities;

	public boolean hasEncoderInfo;

	/**
	 * The velocity of the robot, linear in meters/sec and angular in rad/sec.
	 */
	public float velocityLin, velocityAng;

	public TMotionModelOptions motionModelConfiguration = new TMotionModelOptions();

	@Override
	public int getVersion() {
		return 6;
	}

	@Override
	public void customDecoding(int version, RawlogDecoder decoder) {
		if (version != 3)
			throw new RuntimeException("Not supported");

		try {
			// read the type of estimation
			int estimationMethodInt = LittleEndianIO.readInt(decoder.getInput());
			estimationMethod = TEstimationMethod.values()[estimationMethodInt];

			switch (estimationMethod) {
				case Odometry:
					// The odometry data:
					rawOdometryIncrementReading = (CPose2D) decoder.decodeObject();

					int msIndex = LittleEndianIO.readInt(decoder.getInput());

					motionModelConfiguration.modelSelection = TDrawSampleMotionModel.values()[msIndex];

					LittleEndianIO.readFloat(decoder.getInput());
					LittleEndianIO.readFloat(decoder.getInput());
					LittleEndianIO.readFloat(decoder.getInput());

					motionModelConfiguration.gausianModel = new GaussianModel();
					motionModelConfiguration.gausianModel.minStdXY = LittleEndianIO.readFloat(decoder.getInput());
					motionModelConfiguration.gausianModel.minStdPHI = LittleEndianIO.readFloat(decoder.getInput());

					// Leave the default values for a1,a2,a3,a4:
					motionModelConfiguration.thrunModel = new TOptions_ThrunModel();
					motionModelConfiguration.thrunModel.nParticlesCount = LittleEndianIO.readInt(decoder.getInput());
					motionModelConfiguration.thrunModel.alfa1_rot_rot = LittleEndianIO.readFloat(decoder.getInput());
					motionModelConfiguration.thrunModel.alfa2_rot_trans = LittleEndianIO.readFloat(decoder.getInput());
					motionModelConfiguration.thrunModel.alfa3_trans_trans = LittleEndianIO.readFloat(decoder.getInput());
					motionModelConfiguration.thrunModel.alfa4_trans_rot = LittleEndianIO.readFloat(decoder.getInput());

					// Re-build the PDF:
//                    computeFromOdometry( rawOdometryIncrementReading,motionModelConfiguration );
					break;

				case Scan2DMatching:
					poseChange = (CPosePDF) decoder.decodeObject();
					break;

				default:
					throw new RuntimeException("Unknown: " + estimationMethod);
			}

			hasVelocities = LittleEndianIO.readBoolean(decoder.getInput());
			velocityLin = LittleEndianIO.readFloat(decoder.getInput());
			velocityAng = LittleEndianIO.readFloat(decoder.getInput());
			hasEncoderInfo = LittleEndianIO.readBoolean(decoder.getInput());

			encoderLeftTicks = LittleEndianIO.readInt(decoder.getInput());
			encoderRightTicks = LittleEndianIO.readInt(decoder.getInput());

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void customEncoding(OutputStream output) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public TEstimationMethod getEstimationMethod() {
		return estimationMethod;
	}

	public void setEstimationMethod(TEstimationMethod estimationMethod) {
		this.estimationMethod = estimationMethod;
	}

	public CPosePDF getPoseChange() {
		return poseChange;
	}

	public void setPoseChange(CPosePDF poseChange) {
		this.poseChange = poseChange;
	}

	public boolean isHasEncodersInfo() {
		return hasEncodersInfo;
	}

	public void setHasEncodersInfo(boolean hasEncodersInfo) {
		this.hasEncodersInfo = hasEncodersInfo;
	}

	public CPose2D getRawOdometryIncrementReading() {
		return rawOdometryIncrementReading;
	}

	public void setRawOdometryIncrementReading(CPose2D rawOdometryIncrementReading) {
		this.rawOdometryIncrementReading = rawOdometryIncrementReading;
	}

	public int getEncoderLeftTicks() {
		return encoderLeftTicks;
	}

	public void setEncoderLeftTicks(int encoderLeftTicks) {
		this.encoderLeftTicks = encoderLeftTicks;
	}

	public int getEncoderRightTicks() {
		return encoderRightTicks;
	}

	public void setEncoderRightTicks(int encoderRightTicks) {
		this.encoderRightTicks = encoderRightTicks;
	}

	public boolean isHasVelocities() {
		return hasVelocities;
	}

	public void setHasVelocities(boolean hasVelocities) {
		this.hasVelocities = hasVelocities;
	}

	public boolean isHasEncoderInfo() {
		return hasEncoderInfo;
	}

	public void setHasEncoderInfo(boolean hasEncoderInfo) {
		this.hasEncoderInfo = hasEncoderInfo;
	}

	public float getVelocityLin() {
		return velocityLin;
	}

	public void setVelocityLin(float velocityLin) {
		this.velocityLin = velocityLin;
	}

	public float getVelocityAng() {
		return velocityAng;
	}

	public void setVelocityAng(float velocityAng) {
		this.velocityAng = velocityAng;
	}

	public TMotionModelOptions getMotionModelConfiguration() {
		return motionModelConfiguration;
	}

	public void setMotionModelConfiguration(TMotionModelOptions motionModelConfiguration) {
		this.motionModelConfiguration = motionModelConfiguration;
	}

	enum TEstimationMethod {
		Odometry,
		Scan2DMatching
	}

	enum TDrawSampleMotionModel {
		Gaussian,
		Thrun
	}

	static class GaussianModel {
		public float a1, a2, a3, a4, minStdXY, minStdPHI;
	}

	static class TOptions_ThrunModel {
		/**
		 * The default number of particles to generate in a internal representation (anyway you can draw as many samples as you want through CActionRobotMovement2D::drawSingleSample)
		 */
		public int nParticlesCount;

		public float alfa1_rot_rot;
		public float alfa2_rot_trans;
		public float alfa3_trans_trans;
		public float alfa4_trans_rot;

		/**
		 * An additional noise added to the thrun model (std. dev. in meters and radians).
		 */
		public float additional_std_XY, additional_std_phi;
	}

	/**
	 * The parameter to be passed to "computeFromOdometry".
	 */
	static class TMotionModelOptions {
		/**
		 * The model to be used.
		 */
		public TDrawSampleMotionModel modelSelection;
		/**
		 * Options for the gaussian model, which generates a CPosePDFGaussian object in poseChange
		 */
		public GaussianModel gausianModel;
		/**
		 * Options for the Thrun's model, which generates a CPosePDFParticles object in poseChange
		 */
		public TOptions_ThrunModel thrunModel;

		/**
		 * Default values loader.
		 */
		public TMotionModelOptions() {
		}

	}
}
