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

package bubo.gui.maps;

import boofcv.io.image.UtilImageIO;
import bubo.gui.ScrollableSpacialPane;
import bubo.gui.UtilDisplayBubo;
import bubo.maps.d2.grid.GridMapSpacialInfo;
import bubo.maps.d2.grid.OccupancyGrid2D_F32;
import bubo.maps.d2.grid.impl.OccupancyGridIO;

import java.awt.image.BufferedImage;

/**
 * A display component for OccupancyGrid2D_F32 that allows the user to scroll around, zoom in and out, and displays
 * information about the map.
 *
 * @author Peter Abeles
 */
// todo information in bar: cell size, mouse grid coordinate
    // todo comment
public class GridMapInteractionDisplay extends ScrollableSpacialPane {

    GridMapBasicDisplay mapDisplay;

    public GridMapInteractionDisplay() {
        mapDisplay = new GridMapBasicDisplay();
        setDisplay(mapDisplay);
    }

    public GridMapInteractionDisplay(GridMapBasicDisplay mapDisplay ) {
        this.mapDisplay = mapDisplay;
        setDisplay(mapDisplay);
    }

    public void setMap( GridMapSpacialInfo spacial , OccupancyGrid2D_F32 map ) {
        mapDisplay.setMap(spacial,map);
    }

    public GridMapBasicDisplay getMapDisplay() {
        return mapDisplay;
    }

    public static void main( String args[] ) {
        String fileName = "/home/pja/greatfalls.jpg";
        BufferedImage image = UtilImageIO.loadImage(fileName);
        OccupancyGrid2D_F32 map = OccupancyGridIO.load_F32(image);
        GridMapSpacialInfo spacial = new GridMapSpacialInfo(0.1,0,0);

        GridMapInteractionDisplay comp = new GridMapInteractionDisplay();
        comp.setMap(spacial,map);

        UtilDisplayBubo.show(comp,"Occupancy Grid Interaction",false,0,0,200,200);
    }
}
