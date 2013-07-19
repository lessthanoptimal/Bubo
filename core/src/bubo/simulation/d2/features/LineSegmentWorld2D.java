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

import georegression.struct.line.LineSegment2D_F64;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class LineSegmentWorld2D {

    public List<LineSegment2D_F64> lines = new ArrayList<LineSegment2D_F64>();
}
