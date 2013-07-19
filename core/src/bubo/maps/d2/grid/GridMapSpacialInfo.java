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

package bubo.maps.d2.grid;

import georegression.struct.point.Point2D_F64;

/**
 * Description of the grid map's spacial information and its location in the global frame.  All occupancy
 * grids are assumed to be aligned along the global coordinate system's axis.  This makes look up much easier
 * since a rotation does not need to be performed.  All cells have a constant size.
 *
 * @author Peter Abeles
 */
public class GridMapSpacialInfo {

    // size of a grid cell in global units
    double cellSize;

    // bottom left corner of the map in the global frame
    Point2D_F64 bl;

    public GridMapSpacialInfo(double cellSize, Point2D_F64 bl) {
        this.cellSize = cellSize;
        this.bl = bl.copy();
    }

    public GridMapSpacialInfo(double cellSize, double x , double y ) {
        this.cellSize = cellSize;
        this.bl = new Point2D_F64(x,y);
    }

    public GridMapSpacialInfo() {
    }

    public void globalToLocal( Point2D_F64 global , Point2D_F64 local ) {

    }

    public void localToGlobal( Point2D_F64 global , Point2D_F64 local ) {

    }

    public double getCellSize() {
        return cellSize;
    }

    public Point2D_F64 getBl() {
        return bl;
    }
}
