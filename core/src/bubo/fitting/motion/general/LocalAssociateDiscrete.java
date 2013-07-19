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

package bubo.fitting.motion.general;

import bubo.desc.sensors.lrf2d.Lrf2dParam;
import georegression.struct.point.Point2D_F64;

import java.util.ArrayList;
import java.util.List;


/**
 * Association is done to the LRF sample points with no interpolation.  Child classes
 * are given the index of the scan being considered.
 *
 * @author Peter Abeles
 */
public abstract class LocalAssociateDiscrete implements AssociateLrfMeas {
    // description of the sensor
    private Lrf2dParam param;

    //  LRF scan information.
    protected ScanInfo scanMatch;
    protected ScanInfo scanRef;

    // list of associated points
    private List<Point2D_F64> matchPts = new ArrayList<Point2D_F64>();
    private List<Point2D_F64> refPts = new ArrayList<Point2D_F64>();

    // how many indexes away from the target index will it search
    private int searchNeighborhood;
    // the maximum distance away two points can be to be associated
    private double maxSeparation;

    protected LocalAssociateDiscrete(int searchNeighborhood,
                                     double maxSeparation ) {
        this.searchNeighborhood = searchNeighborhood;
        this.maxSeparation = maxSeparation;
    }

    public void setParam(Lrf2dParam param) {
        this.param = param;
    }

    @Override
    public void associate(ScanInfo scanMatch, ScanInfo scanRef) {

        this.scanMatch = scanMatch;
        this.scanRef = scanRef;

        matchPts.clear();
        refPts.clear();

        final int N = param.getNumberOfScans();
        for( int i = 0; i < N; i++ ) {
            if( !scanMatch.vis[i] )
                continue;

            int min = i - searchNeighborhood;
            int max = i + searchNeighborhood;
            if( min < 0 ) min = 0;
            if( max > N ) max = N;

            int bestIndex = -1;
            double bestDistance = Double.MAX_VALUE;

            setTarget(i);

            for( int j = min; j < max; j++ ) {
                if( !scanRef.vis[j] )
                    continue;

                double dist = distToTarget(j);

                if( dist < bestDistance ) {
                    bestDistance = dist;
                    bestIndex = j;
                }
            }

            if( bestIndex != -1 && bestDistance < maxSeparation) {
                matchPts.add(scanMatch.pts[i]);
                refPts.add(scanRef.pts[bestIndex]);
            }
        }
    }

    @Override
    public List<Point2D_F64> getListMatch() {
        return matchPts;
    }

    @Override
    public List<Point2D_F64> getListReference() {
        return refPts;
    }

    /**
     * Specifies which measurement in the match scan that the distance is being measured against.
     * 
     * @param indexMatch
     */
    public abstract void setTarget( int indexMatch );

    /**
     * Distance from reference to the specified index
     */
    public abstract double distToTarget( int indexRef );
}
