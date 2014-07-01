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

package bubo.desc.ext;

import bubo.desc.RobotComponent;
import georegression.struct.se.Se2_F64;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestRefFramePath {

	private static RobotComponent createComp(RobotComponent ref) {
		return new RobotComponent(null, new Extrinsic2D(ref, true), null, null);
	}

	private static RobotComponent createComp(RobotComponent ref, double x, double y, double yaw) {
		Extrinsic2D ext = new Extrinsic2D(ref, true);
		ext.getTransformToParent().set(x, y, yaw);
		return new RobotComponent(null, ext, null, null);
	}

	private static void checkNode(RefFramePath.Node n, RobotComponent comp, boolean forward) {
		assertTrue(n.comp == comp);
		assertTrue(n.forward == forward);
	}

	/**
	 * The two inputs are the same
	 */
	@Test
	public void findPath_sameNode() {
		RobotComponent b = createComp(null);
		RobotComponent a = createComp(b);

		RefFramePath path = RefFramePath.findPath(a, a);

		assertEquals(0, path.getPath().size());
	}


	/**
	 * the 'from' node is the base node and 'to' is N off
	 */
	@Test
	public void findPath_fromIsBase() {
		RobotComponent base = createComp(null);
		RobotComponent b = createComp(base);
		RobotComponent a = createComp(b);

		RefFramePath path = RefFramePath.findPath(base, a);

		List<RefFramePath.Node> l = path.getPath();

		assertEquals(2, l.size());
		checkNode(l.get(0), b, false);
		checkNode(l.get(1), a, false);
	}

	/**
	 * the 'to' node is the base node and 'from' is N off
	 */
	@Test
	public void findPath_toIsBase() {
		RobotComponent base = createComp(null);
		RobotComponent b = createComp(base);
		RobotComponent a = createComp(b);

		RefFramePath path = RefFramePath.findPath(a, base);

		List<RefFramePath.Node> l = path.getPath();

		assertEquals(2, l.size());
		checkNode(l.get(0), a, true);
		checkNode(l.get(1), b, true);
	}

	/**
	 * The two paths diverse some place in the middle
	 */
	@Test
	public void findPath_meetMiddle() {
		RobotComponent base = createComp(null);
		RobotComponent c = createComp(base);
		RobotComponent b = createComp(c);
		RobotComponent a = createComp(b);
		RobotComponent d = createComp(c);

		RefFramePath path = RefFramePath.findPath(a, d);

		List<RefFramePath.Node> l = path.getPath();

		assertEquals(3, l.size());
		checkNode(l.get(0), a, true);
		checkNode(l.get(1), b, true);
		checkNode(l.get(2), d, false);
	}

	/**
	 * Create a bad graph with a cycle
	 */
	@Test(expected = RuntimeException.class)
	public void findPath_cycle() {
		RobotComponent base = createComp(null);
		RobotComponent b = createComp(base);
		RobotComponent a = createComp(b);
		b.setExtrinsic(new Extrinsic2D(a, true));

		RefFramePath.findPath(a, base);
	}

	/**
	 * Bad graph where the two have nothing in common
	 */
	@Test(expected = RuntimeException.class)
	public void findPath_nocommon() {
		RobotComponent base = createComp(null);
		RobotComponent b = createComp(null);
		RobotComponent a = createComp(base);

		RefFramePath.findPath(a, b);
	}

	@Test
	public void computeTransform_emptyPath() {
		RobotComponent b = createComp(null);
		RobotComponent a = createComp(b);

		RefFramePath path = RefFramePath.findPath(a, a);

		Se2_F64 result = new Se2_F64();
		path.computeTransform(result);
	}

	@Test
	public void computeTransform() {
		RobotComponent base = createComp(null);
		RobotComponent b = createComp(base, 1, 2, 0);
		RobotComponent a = createComp(b, 4, 5, 0);

		RefFramePath path = RefFramePath.findPath(a, base);

		Se2_F64 found = new Se2_F64();
		path.computeTransform(found);

		assertEquals(5, found.getX(), 1e-8);
		assertEquals(7, found.getY(), 1e-8);
	}
}
