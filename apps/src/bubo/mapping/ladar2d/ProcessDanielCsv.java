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

package bubo.mapping.ladar2d;

import boofcv.io.image.UtilImageIO;
import bubo.desc.sensors.lrf2d.Lrf2dParam;
import bubo.gui.UtilDisplayBubo;
import bubo.io.serialization.SerializationDefinitionManager;
import bubo.io.text.ReadCsvObjectSmart;
import bubo.mapping.ladar2d.update.LadarMapDanielScanUpdate;
import bubo.maps.d2.grid.GridMapSpacialInfo;
import bubo.maps.d2.grid.OccupancyGrid2D_F32;
import bubo.maps.d2.grid.impl.ArrayGrid2D_F32;
import bubo.maps.d2.grid.impl.OccupancyGridIO;
import georegression.metric.UtilAngle;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se2_F64;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Peter Abeles
 */
// TODO create an XML config file that specifies:
//  - units of ladar measurements
//  - type of sensor being used
//  - data that is being read in
//  - what the comment character is

// todo rename

    // todo create a growable occupancy grid
public class ProcessDanielCsv implements ActionListener {

    private static final long pauseTimeMilli = 0;

    LadarMappingComponent gui;

	LadarMapDanielScanUpdate mapBuilder;
    OccupancyGrid2D_F32 map;
    GridMapSpacialInfo mapSpacial;

    // todo add algorithm which can process the data
    ReadCsvObjectSmart<DanielRangeData> reader;
	DanielRangeData data;
    Lrf2dParam param;
    // todo make GUI on or off configurable

    int frameNum;

    volatile boolean paused = false;
    volatile boolean takeStep = false;


    public ProcessDanielCsv(String fileName, Lrf2dParam param) throws FileNotFoundException {
        this.param = param;
        data = new DanielRangeData(param.getNumberOfScans());

		SerializationDefinitionManager def = new SerializationDefinitionManager();
		def.loadDefinition(DanielRangeData.class,"timeStamp" , "range");

        reader = new ReadCsvObjectSmart<DanielRangeData>(new FileInputStream(fileName),def,DanielRangeData.class.getSimpleName());
        reader.setComment('#');
        reader.setIgnoreUnparsedData(true);  // TODO read hokuyo config and make sure this is needed

        gui = new LadarMappingComponent();
        gui.configureLadar(param);

        gui.getPlayButton().addActionListener(this);
        gui.getStepButton().addActionListener(this);
        gui.getFocusRobotButton().addActionListener(this);
        gui.getSaveMapImageButton().addActionListener(this);

        gui.getMapDisplay().setColorUnknown(null);

        UtilDisplayBubo.show(gui,"Ladar Mapping",false,0,0,1200,800);
    }

    public void process() throws IOException {

        mapBuilder = new LadarMapDanielScanUpdate();

        map = null;
        mapSpacial = null;
        if( reader.nextObject(data) != null ) {
            double cellSize = 0.1;
            map = new ArrayGrid2D_F32(750,500);

            mapSpacial = new GridMapSpacialInfo(cellSize,new Point2D_F64(-25,-25));

            map.setAll(0.5f);

            mapBuilder.init(param,map,mapSpacial);

            gui.setMap(mapSpacial,map);
        }

        frameNum = 0;
        while( reader.nextObject(data) != null ) {

            mapBuilder.process(data);

            gui.updateRobot(mapBuilder.getPosition());
            gui.updateLadar(data.getRange());
            gui.updateMap();
            gui.repaint();
            
            if( frameNum == 0 ) {
                // focus on the robot initially
                setDisplayToRobot();
            }

            UtilDisplayBubo.pause(pauseTimeMilli);
            while( paused ) {
                UtilDisplayBubo.pause(5);
                if( takeStep ) {
                    takeStep = false;
                    break;
                }
            }

//			BufferedImage renderedMap = OccupancyGridIO.render_F32(map,true);
//			UtilImageIO.saveImage(renderedMap,String.format("log/frame%06d.png",frameNum));

            System.out.println("Processed Data "+frameNum);
            frameNum++;
           // todo create histogram viewer
            // todo write odometry + ladar to a map and display that


        }
    }

	public static Lrf2dParam createHokuyo() {
		Lrf2dParam param = new Lrf2dParam();


		double sweep = UtilAngle.degreeToRadian(270);

		param.setNumberOfScans( 1080 );
//		param.setSweepAngle( -Math.PI );
//		param.setStartAngle( Math.PI/2.0 );
		param.setSweepAngle( sweep );
		param.setStartAngle( -sweep/2.0);
		param.setMaxRange( 25 );

		return param;
	}

    public static void main( String args[] ) throws IOException {

        String fileName = "/home/pja/Downloads/NF1_Offices_LIDARScan_5.txt";

        Lrf2dParam param = createHokuyo();

        ProcessDanielCsv p = new ProcessDanielCsv(fileName,param);

        p.process();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if( e.getSource() == gui.getPlayButton() ) {
            paused = !paused;
        } else if( e.getSource() == gui.getStepButton() ) {
            if( !paused ) {
                paused = true;
            } else {
                takeStep = true;
            }
        } else if( e.getSource() == gui.getFocusRobotButton() ) {
            setDisplayToRobot();
        } else if( e.getSource() == gui.getSaveMapImageButton() ) {
            String fileName = String.format("frame%05d.png",frameNum);

            BufferedImage renderedMap = OccupancyGridIO.render_F32(map,true);
            UtilImageIO.saveImage(renderedMap,fileName);
            System.out.println("Saved map to image");
        }
    }

    private void setDisplayToRobot() {
        // world coordinates
        Se2_F64 position = mapBuilder.getPosition();

        // map coordinates
        double x = position.getX() - mapSpacial.getBl().x;
        double y = position.getY() - mapSpacial.getBl().y;

        gui.setViewCenter(x,y);
    }
}
