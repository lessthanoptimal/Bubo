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

package bubo.io.rawlog.data;

import bubo.io.rawlog.RawlogSerializableStandard;
import georegression.geometry.ConvertRotation3D_F64;
import georegression.struct.EulerType;
import georegression.struct.point.Point3D_F64;
import org.ejml.data.DMatrixRMaj;

/**
 * <p>
 * Java implementation of mrpt/poses/CPose3D
 * </p>
 * <p/>
 * <p/>
 * A class used to store a 3D pose (a 3D translation + a rotation in 3D).
 * The 6D transformation in SE(3) stored in this class is kept in two
 * separate containers: a 3-array for the translation, and a 3x3 rotation matrix.
 * <p/>
 * The 6D pose is parameterized as a 6-vector: [x y z yaw pitch roll]. Note however,
 * that the yaw/pitch/roll angles are only computed (on-demand and transparently)
 * when the user requests them. Normally, rotations are handled via the 3x3 rotation matrix only.
 * <p/>
 * For a complete descriptionan of Points/Poses, see mrpt::poses::CPoseOrPoint, or refer
 * to the <a href="http://www.mrpt.org/2D_3D_Geometry">2D/3D Geometry tutorial</a> in the wiki.
 * <p/>
 * To change the individual components of the pose, use CPose3D::setFromValues. This class assures that the internal
 * 3x3 rotation matrix is always up-to-date with the "yaw pitch roll" members.
 * <p/>
 * Rotations in 3D can be also represented by quaternions. See mrpt::math::CQuaternion, and method CPose3D::getAsQuaternion.
 * <p/>
 * This class and CPose3DQuat are very similar, and they can be converted to the each other automatically via transformation constructors.
 * <p/>
 * There are Lie algebra methods: \a exp and \a ln (see the methods for documentation).
 *
 * @author Peter Abeles
 */
public class CPose3D extends CPose implements RawlogSerializableStandard {

	Point3D_F64 position = new Point3D_F64();
	DMatrixRMaj rotation = new DMatrixRMaj(3, 3);

	public String[] getVariableOrder(int version) {
		if (version >= 2) {
			throw new RuntimeException("Unsupported version");
		} else if (version == 1) {
			return new String[]{"HomoMatrix"};
		} else {
			return new String[]{"HomoMatrixF"};
		}
	}

	public String toReadableText() {
		String ret = "";

		double euler[] = ConvertRotation3D_F64.matrixToEuler(rotation, EulerType.XYZ, (double[])null);

		ret += "Position = ( " + position.getX() + " , " + position.getY() + " , " + position.getZ() + " )\n";
		ret += String.format("Rotation = ( rotX = %5.2f | rotY = %5.2f | rotZ = %5.2f )\n", euler[0], euler[1], euler[2]);

		return ret;
	}

	public CMatrixD getHomoMatrix() {
		return null;
	}

	public void setHomoMatrix(CMatrixD HM) {
		position.x = HM.get(0, 3);
		position.y = HM.get(1, 3);
		position.z = HM.get(2, 3);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				rotation.set(i, j, HM.get(i, j));
			}
		}
	}

	public CMatrix getHomoMatrixF() {
		return null;
	}

	public void setHomoMatrixF(CMatrix HM) {
		position.x = HM.get(0, 3);
		position.y = HM.get(1, 3);
		position.z = HM.get(2, 3);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				rotation.set(i, j, HM.get(i, j));
			}
		}
	}

	public Point3D_F64 getPosition() {
		return position;
	}

	public void setPosition(Point3D_F64 position) {
		this.position.set(position);
	}

	public DMatrixRMaj getRotation() {
		return rotation;
	}

	public void setRotation(DMatrixRMaj rotation) {
		this.rotation.set(rotation);
	}

	public int getVersion() {
		return 2;
	}
}
