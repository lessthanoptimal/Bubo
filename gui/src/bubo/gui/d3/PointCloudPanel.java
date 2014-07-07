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

package bubo.gui.d3;

import georegression.struct.point.Point3D_F64;
import georegression.struct.se.Se3_F64;

import javax.swing.*;
import java.util.List;

/**
 * Class for displaying 3D point clouds in a swing panel.
 *
 * @author Peter Abeles
 */
public abstract class PointCloudPanel extends JPanel {

	public abstract void setFov( double angle );

	public abstract void setCamera( Se3_F64 camera );

	public abstract void setShowAxis( boolean show );

	public abstract boolean getShowAxis();

	public abstract void addPoints( List<Point3D_F64> points , int color , float size );

	public abstract void addPoints( List<Point3D_F64> points , int[] colors , float size );

	public abstract void addBox( double sizeX , double sizeY , double sizeZ ,
								 Se3_F64 boxToWorld ,
								 int color );

	public abstract void shutdownVisualize();

}
