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

package bubo.clouds.motion;

import bubo.clouds.fit.Lrf2dScanToScan;
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import georegression.struct.se.Se2_F64;
import org.ddogleg.struct.CircularQueue;

/**
 * Selects key-frames intelligently from a rolling window to improve state estimation accuracy.  When a key-frame
 * is changed it back estimates from the current frame (which has an accurate state estimate) to older scans
 * which might not have an accurate estimate.  The number of scans in the history is dynamically selected. The
 * specific scan matching algorithm is provided externally.  The focus of this class is key-frame maintenance.
 *
 * Accuracy of scan-to-scan algorithms pose estimation depends on the magnitude of the true motion.  Smaller
 * the true motion, more the estimation noise will dominate.  However, if two scans are too different
 * then accuracy will suffer.  Thus pose should be estimated from two scans which are as different as possible
 * without them being too different.
 *
 * When a new key-frame is selected is determined using a heuristic based on the number of matched scan points.
 * If too many are matched then it is assumed that the scans are too similar and if too few are matched then
 * they are too different.
 *
 * @author Peter Abeles
 */
// TODO detect no motion identical scans
// TODO if history limit exceeded then change keyframe
// TODO must be able to handle bad scans.  recognize that robot bounced up and down
public class Lrf2dMotionRollingKeyFrame {

	Lrf2dScanToScan estimator;
	Lrf2dParam param;

	CircularQueue<ScanInfo> history;

	boolean first;

	int maxHistory;
	double keyFraction = 0.75;
	int keyValidScans;

	Se2_F64 odomWorldToKey = new Se2_F64();
	Se2_F64 odomCurrToKey = new Se2_F64();

	public Lrf2dMotionRollingKeyFrame(Lrf2dScanToScan estimator, int maxHistory) {
		this.estimator = estimator;
		this.maxHistory = maxHistory;
	}

	public void init( final Lrf2dParam param ) {
		this.param = param;
		this.history = new CircularQueue<ScanInfo>(ScanInfo.class,maxHistory) {
			@Override
			protected ScanInfo createInstance() {
				return new ScanInfo(param.getNumberOfScans());
			}
		};
		estimator.setSensorParam(param);
		reset();
	}

	public void setKeyFraction(double keyFraction) {
		this.keyFraction = keyFraction;
	}

	public void reset() {
		first = true;
		history.reset();
	}

	public Se2_F64 getSensorToWorld() {
		return history.tail().sensorToWorld;
	}

	public boolean process( Se2_F64 odometrySensorToWorld , double[] scan ) {

		// check to see if the sensor is blind
		int totalValid = countValidScans(scan);
		if( totalValid < 2 ) {
			if( history.size() > 0 ) {
				ScanInfo prev = history.tail();

				// TODO compute odometry correction factor?
			}
			// TODO just integrate odometry
			first = true;
			return false;
		}

		if( first ) {
			first = false;
			ScanInfo key = history.grow();
			key.init(scan, odometrySensorToWorld);
			key.sensorToWorld.set(odometrySensorToWorld);
			estimator.setDestination(key.scan);
			keyValidScans = totalValid;
		} else {
			ScanInfo key = history.head();

			// todo should really do last trusted frame + odometry
			//      since that's in theory more accurate than pure odometry
			// then again odometry isn't tainted by a bad match
			key.odometrySensorToWorld.invert(odomWorldToKey);
			odometrySensorToWorld.concat(odomWorldToKey, odomCurrToKey);

			estimator.setSource(scan);
			if( !estimator.process(odomCurrToKey) )
				throw new RuntimeException("Crap it failed.  I should something smart here");

			ScanInfo curr = history.grow();
			curr.init(scan, odometrySensorToWorld);
			curr.trusted = key.trusted;

			Se2_F64 currToKey = estimator.getSourceToDestination();
			currToKey.concat(key.sensorToWorld,curr.sensorToWorld);

			if( estimator.totalScansMatched() < keyValidScans*keyFraction ) {
				curr.trusted = true;

				while (history.size() > 1) {
					history.removeHead();
				}

				key = history.head();
				estimator.setDestination(key.scan);
				keyValidScans = totalValid;
			}
		}

		return true;
	}

	private int countValidScans( double scan[]) {
		int total = 0;
		for (int i = 0; i < param.getNumberOfScans(); i++) {
			if( param.isValidRange(scan[i]))
				total++;
		}
		return total;
	}

	/**
	 * Information on a scan.
	 */
	public static class ScanInfo
	{
		double scan[];
		Se2_F64 odometrySensorToWorld = new Se2_F64();
		Se2_F64 sensorToWorld = new Se2_F64();
		boolean trusted;

		public ScanInfo( int numScans ) {
			scan = new double[numScans];
		}

		public void init( double scan[] , Se2_F64 odometrySensorToWorld ) {
			set(scan);
			this.odometrySensorToWorld.set(odometrySensorToWorld);
			sensorToWorld.reset();
			trusted = false;
		}

		public void set( double scan[] ) {
			System.arraycopy(scan,0,this.scan,0,scan.length);
		}
	}
}
