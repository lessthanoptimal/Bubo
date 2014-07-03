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

package bubo.construct;

import georegression.metric.Intersection3D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.shapes.Cube3D_F64;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestConstructOctreeCellSize {

	/**
	 * Makes sure the point and data are correctly associated to each other
	 */
	@Test
	public void addPoint_data() {
		Point3D_F64 p = new Point3D_F64(1, 2, 3);
		Integer d = 1;

		ConstructOctreeCellSize alg = new ConstructOctreeCellSize(0.01);
		alg.addPoint(p, d);

		Octree.Info a = alg.getTree().points.get(0);
		assertTrue(a.point == p);
		assertTrue(a.data == d);
	}

	/**
	 * Add multiple points to the same node and see if everything expected happens
	 */
	@Test
	public void addPoint_multipleSameNode() {
		ConstructOctreeCellSize alg = new ConstructOctreeCellSize(0.01);
		alg.initialize(new Cube3D_F64(-50, -50, -50, 100, 100, 100));

		// all of them should have the same node
		Octree node = alg.addPoint(new Point3D_F64(1, 2, 3), new Integer(1));
		assertTrue(node == alg.addPoint(new Point3D_F64(1.0001, 2, 3), new Integer(2)));
		assertTrue(node == alg.addPoint(new Point3D_F64(1, 2.0001, 3), new Integer(3)));

		assertTrue(node.isLeaf());
		assertTrue(node.space.getLengthX() <= 0.01);
		assertTrue(node.space.getLengthY() <= 0.01);
		assertTrue(node.space.getLengthZ() <= 0.01);
	}

	/**
	 * Add points everywhere and see if it has the expected behavior
	 */
	@Test
	public void addPoint_createNodes() {

		Random rand = new Random(234);

		double r = 0.01;

		ConstructOctreeCellSize alg = new ConstructOctreeCellSize(r);
		alg.initialize(new Cube3D_F64(-50, -50, -50, 100, 100, 100));

		for (int i = 0; i < 20; i++) {

			double x = rand.nextDouble() * 150 - 50;
			double y = rand.nextDouble() * 150 - 50;
			double z = rand.nextDouble() * 150 - 50;

			Point3D_F64 p = new Point3D_F64(x, y, z);

			Octree node = alg.addPoint(p, null);
			assertTrue(node.isLeaf());
			assertTrue(Intersection3D_F64.contained(node.space, p));
			assertTrue(node.points.size() >= 1);
			assertTrue(node.space.getLengthX() <= r);
			assertTrue(node.space.getLengthY() <= r);
			assertTrue(node.space.getLengthZ() <= r);
		}
	}

	@Test
	public void addLeaf() {
		Random rand = new Random(234);

		double r = 0.01;

		ConstructOctreeCellSize alg = new ConstructOctreeCellSize(r);
		alg.initialize(new Cube3D_F64(-50, -50, -50, 100, 100, 100));

		for (int i = 0; i < 20; i++) {

			double x = rand.nextDouble() * 150 - 50;
			double y = rand.nextDouble() * 150 - 50;
			double z = rand.nextDouble() * 150 - 50;

			Point3D_F64 p = new Point3D_F64(x, y, z);

			Octree node = alg.addLeaf(p);
			assertTrue(node.isLeaf());
			assertTrue(Intersection3D_F64.contained(node.space, p));
			assertTrue(node.points.size() == 0);
			assertTrue(node.space.getLengthX() <= r);
			assertTrue(node.space.getLengthY() <= r);
			assertTrue(node.space.getLengthZ() <= r);
		}
	}

	@Test
	public void findLeaf() {
		double r = 1;

		ConstructOctreeCellSize alg = new ConstructOctreeCellSize(r);
		alg.initialize(new Cube3D_F64(0, 0, 0, 100, 100, 100));

		Octree node = alg.addLeaf(new Point3D_F64(0.1, 0.1, 0.1));

		Octree foundA = alg.findLeaf(new Point3D_F64(0.1, 0.1, 0.1));
		assertTrue(node == foundA);

		Octree foundB = alg.findLeaf(new Point3D_F64(10, 10, 10));
		assertTrue(null == foundB);

		// just a quick sanity check to make sure there isn't a bug elsewhere
		Octree foundC = alg.getTree().findDeepest(new Point3D_F64(10, 10, 10));
		assertTrue(foundC != null);
		assertTrue(foundC != alg.getTree());
	}
}
