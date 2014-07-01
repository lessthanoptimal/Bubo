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

package bubo.simulation.d2.sensors;

/**
 * Range bearing sensor measurement
 *
 * @author Peter Abeles
 */
public class RangeBearing {
	// landmark ID
	public long id;

	// range to landmark
	public double r;
	// landmark's relative bearing
	public double bearing;

	public RangeBearing(long id, double r, double bearing) {
		this.id = id;
		this.r = r;
		this.bearing = bearing;
	}

	public RangeBearing() {
	}
}
