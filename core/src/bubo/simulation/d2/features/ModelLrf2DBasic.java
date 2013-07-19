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

package bubo.simulation.d2.features;

import bubo.desc.sensors.lrf2d.Lrf2dParam;
import georegression.metric.Intersection2D_F64;
import georegression.struct.line.LineParametric2D_F64;
import georegression.struct.line.LineSegment2D_F64;
import georegression.struct.se.Se2_F64;

import java.util.List;
import java.util.Random;

/**
 * Basic sensor model for 2D laser rangefinders (LRF) in a {@link LineSegmentWorld2D}.
 *
 * @author Peter Abeles
 */
public class ModelLrf2DBasic {

    Random rand = new Random(123123);
    LineSegmentWorld2D world;

    // standard deviation of noise which is added to range measurements
    double noiseSigma = 0;

    public ModelLrf2DBasic() {

    }

    public double getNoiseSigma() {
        return noiseSigma;
    }

    public void setNoiseSigma(double noiseSigma) {
        this.noiseSigma = noiseSigma;
    }

    public void setWorld( LineSegmentWorld2D world ) {
        this.world = world;
    }

    public void updateSensor( SimStateLrf2D sensor ) {
        List<LineSegment2D_F64> lines = world.lines;

        Se2_F64 l2p = sensor.getLocalToParent();

        Lrf2dParam param = sensor.getSensorParam();
        double ranges[] = sensor.getRanges();

        LineParametric2D_F64 ray = new LineParametric2D_F64();
        ray.setPoint(l2p.getX(),l2p.getY());

        double startAngle = param.getStartAngle() + l2p.getYaw();

        final double angleInc = param.getAngleIncrement();

        for( int i = 0; i < param.getNumberOfScans(); i++ ) {
            double angle = startAngle + i*angleInc;
            ray.setAngle(angle);

            // find the line which
            double closest = Double.MAX_VALUE;

            for( LineSegment2D_F64 l : lines ) {
                double r = Intersection2D_F64.intersection(ray,l);

                if( noiseSigma > 0 ) {
                    r += rand.nextGaussian()*noiseSigma;
                    if( r < 0 )
                        r = 0;
                }

                if( r > 0 && r < closest ) {
                    closest = r;
                }
            }

            ranges[i] = closest < param.getMaxRange() ? closest : param.getMaxRange();
        }
    }
}
