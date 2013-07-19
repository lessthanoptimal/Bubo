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

package bubo.desc.sensors.laser2d;

import georegression.struct.point.Point2D_F64;

/**
 * Precomputes the sine and cosine for each element in a ladar. It can then
 * compute the location of ladar point in the sensor's reference frame given
 * its index and range.
 *
 * @author Peter Abeles
 */
// todo move out of the desc package?
public class PlanarLadarPrecomputedTrig {

    public double c[];
    public double s[];

    // end point of ladar
    public double x;
    public double y;

    public PlanarLadarPrecomputedTrig( final PlanarLadarParam config ) {
        double theta = config.getStartAngle();
        double delta = config.getAngleIncrement();

        final int N = config.getNumberOfScans();

        c = new double[ N ];
        s = new double[ N ];

        for( int i = 0; i < N; i++ , theta += delta) {
            c[i] = Math.cos(theta);
            s[i] = Math.sin(theta);
        }
    }

    public void computeEndPoint( int index , double range ) {
        x = c[index]*range;
        y = s[index]*range;
    }

    public void computeEndPoint( int index , double range , Point2D_F64 pt ) {
        pt.x = c[index]*range;
        pt.y = s[index]*range;
    }
}
