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

import bubo.gui.jme.JmeBridgeToAwt;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.util.BufferUtils;
import georegression.geometry.RotationMatrixGenerator;
import georegression.geometry.UtilPolygons2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point3D_F64;
import georegression.struct.point.Vector3D_F64;
import georegression.struct.se.Se3_F64;
import georegression.struct.so.Quaternion_F64;
import georegression.transform.se.SePointOps_F64;
import org.ddogleg.struct.GrowQueue_I32;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Peter Abeles
 */
// TODO get point size working
@SuppressWarnings("PointlessArithmeticExpression")
public class JmePointCloudPanel extends PointCloudPanel {

	final JmeBridgeToAwt bridge;

	int totalBoxes = 0;

	public JmePointCloudPanel(JmeBridgeToAwt bridge) {
		this.bridge = bridge;
		add(bridge.getCanvas());
	}

	@Override
	public void setFov(double angle) {

	}

	@Override
	public void setCamera(Se3_F64 camera) {

	}

	@Override
	public void setShowAxis(boolean show) {

	}

	@Override
	public boolean getShowAxis() {
		return false;
	}

	@Override
	public void addPoints(List<Point3D_F64> points, final int color , final float size ) {
		final float[] buf = convertPointsToArray(points);

		bridge.enqueue(new Callable<Void>(){
			public Void call(){
				float alpha = ((color >> 24) & 0xFF ) / 255.0f;
				float red = ((color >> 16) & 0xFF ) / 255.0f;
				float green = ((color >> 8) & 0xFF ) / 255.0f;
				float blue = (color & 0xFF ) / 255.0f;

				Material mat = new Material(bridge.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				mat.setColor("Color", new ColorRGBA(red,green,blue,alpha) );
				mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

				Mesh m = new Mesh();
				m.setBuffer(VertexBuffer.Type.Position, 3, buf);
				m.setMode(Mesh.Mode.Points);
				m.setPointSize(size);
				m.setStatic();
				m.updateBound();

				Geometry g = new Geometry("Point Cloud", m);
				g.setShadowMode(RenderQueue.ShadowMode.Off);
				g.setQueueBucket(RenderQueue.Bucket.Opaque);
				g.setMaterial(mat);
				bridge.getRootNode().attachChild(g);
				return null;
			}});

	}

	@Override
	public void addPoints(List<Point3D_F64> points, int[] colors , final float size ) {
		final float[] buffPoints = convertPointsToArray(points);
		final float[] buffColor = new float[4*points.size()];

		for (int i = 0; i < points.size(); i++) {
			int color = colors[i];

			float alpha = ((color >> 24) & 0xFF ) / 255.0f;
			float red = ((color >> 16) & 0xFF ) / 255.0f;
			float green = ((color >> 8) & 0xFF ) / 255.0f;
			float blue = (color & 0xFF ) / 255.0f;

			buffColor[i*4+0] = red;
			buffColor[i*4+1] = green;
			buffColor[i*4+2] = blue;
			buffColor[i*4+3] = alpha;
		}

		bridge.enqueue(new Callable<Void>() {
			public Void call() {
				Material mat = new Material(bridge.getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
				mat.getAdditionalRenderState().setPointSprite(true);
				mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
				mat.setBoolean("PointSprite", true);


				Mesh m = new Mesh();
				m.setBuffer(VertexBuffer.Type.Position, 3, buffPoints);
				m.setBuffer(VertexBuffer.Type.Color, 4, buffColor);
				m.setMode(Mesh.Mode.Points);
				m.setPointSize(size);
				m.setStatic();
				m.updateBound();

				Geometry g = new Geometry("Point Cloud", m);
				g.setShadowMode(RenderQueue.ShadowMode.Off);
				g.setQueueBucket(RenderQueue.Bucket.Opaque);
				g.setMaterial(mat);
				bridge.getRootNode().attachChild(g);
				return null;
			}
		});
	}

	@Override
	public void addBox(final double sizeX, final double sizeY, final double sizeZ,
					   Se3_F64 boxToWorld, int color) {

		final Transform t = convertToJme(boxToWorld);

		float alpha = ((color >> 24) & 0xFF ) / 255.0f;
		float red = ((color >> 16) & 0xFF ) / 255.0f;
		float green = ((color >> 8) & 0xFF ) / 255.0f;
		float blue = (color & 0xFF ) / 255.0f;
		final boolean translucent = alpha < 1.0f;

		final ColorRGBA jmeColor = new ColorRGBA(red,green,blue,alpha);

		bridge.enqueue(new Callable<Void>() {
			public Void call() {
				Box b = new Box((float) sizeX, (float) sizeY, (float) sizeZ); // create cube shape
				Geometry geom = new Geometry("Box"+(totalBoxes++), b);  // create cube geometry from the shape
				Material mat = new Material(bridge.getAssetManager(),
						"Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
//				mat.getAdditionalRenderState().setWireframe(true);
				mat.setColor("Color", jmeColor);
				geom.setMaterial(mat);
				geom.setLocalTransform(t);

				if( translucent ) {
					mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
					geom.setQueueBucket(RenderQueue.Bucket.Translucent);
				}

				bridge.getRootNode().attachChild(geom);
				return null;
			}
		});
	}

	@Override
	public void addMesh2D( final Se3_F64 meshToWorld, List<Point2D_F64> vertexes , int argb ) {

		if( vertexes.size() < 3 )
			throw new IllegalArgumentException("There must be at least 3 points to be a mesh");

		final List<Point2D_F64> ordered = ensureCCW(vertexes);

		float alpha = ((argb >> 24) & 0xFF ) / 255.0f;
		float red = ((argb >> 16) & 0xFF ) / 255.0f;
		float green = ((argb >> 8) & 0xFF ) / 255.0f;
		float blue = (argb & 0xFF ) / 255.0f;
		final boolean translucent = alpha < 1.0f;

		final ColorRGBA jmeColor = new ColorRGBA(red,green,blue,alpha);

		bridge.enqueue(new Callable<Void>() {
			public Void call() {

				Vector3f [] vertices = new Vector3f[ordered.size()];

				Point3D_F64 p3 = new Point3D_F64();
				for (int i = 0; i < ordered.size(); i++) {
					Point2D_F64 p2 = ordered.get(i);
					p3.set(p2.x,p2.y,0);
					SePointOps_F64.transform(meshToWorld,p3,p3);

					vertices[i] = new Vector3f((float)p3.x,(float)p3.y,(float)p3.z);
				}

				// specify mesh triangles
				GrowQueue_I32 open = new GrowQueue_I32();
				GrowQueue_I32 open2 = new GrowQueue_I32();

				for (int i = 0; i < ordered.size(); i++) {
					open.add(i);
				}

				// Create triangles along the outside border and work your way in
				// TODO not sure this algorithm is really right.  seems to render correctly but concave shapes are off
				// maybe see what the order of the points is and skip or accept
				// see if vertex is CW if so, do nothing.  If CCW don't add triangle and add vertexes instead?
				// TODO could use O(N) algorithm.  this is a slower
				GrowQueue_I32 list = new GrowQueue_I32();
				while( open.size() > 0 ) {
					open2.reset();
					if( open.size() >= 3 ) {
						for (int i = 0; i < open.size(); i += 2) {

							int index0 = i;
							int index1 = i+1;
							int index2 = i+2;

							if( index1 == open.size() ) {
								break;
							} else if( index2 == open.size() ) {
								open2.add(open.get(index1));
							} else {
								list.add(open.get(index0));
								list.add(open.get(index1));
								list.add(open.get(index2));

								if( i == 0 )
									open2.add(open.get(index0));
								open2.add(open.get(index2));
							}
						}
					} else {
						break;
					}

					GrowQueue_I32 tmp = open2;
					open2 = open;
					open = tmp;
				}

				// add a second set of triangles so that it can be visible on the other side
				int origSize = list.size();
				for (int i = 0; i < origSize; i += 3 ) {
					list.add( list.get(i+2));
					list.add( list.get(i+1));
					list.add( list.get(i));
				}

				int tmp[] = new int[ list.size() ];
				System.arraycopy(list.data,0,tmp,0,tmp.length);


				Mesh mesh = new Mesh();
				mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
				mesh.setBuffer(VertexBuffer.Type.Index,    3, BufferUtils.createIntBuffer(tmp));
				mesh.updateBound();

				Material mat = new Material(bridge.getAssetManager(),"Common/MatDefs/Misc/Unshaded.j3md");
				mat.setColor("Color", jmeColor);

				Geometry g = new Geometry("Mesh2D", mesh);
				g.setMaterial(mat);

				if( translucent ) {
					mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
					g.setQueueBucket(RenderQueue.Bucket.Translucent);
				}

				bridge.getRootNode().attachChild(g);

				return null;
			}
		});
	}

	@Override
	public void addVectors(List<Point3D_F64> location, List<Vector3D_F64> direction, final int argb) {

		if( location.size() != direction.size() )
			throw new IllegalArgumentException("size of location and direction must match");

		final float[] buf = new float[location.size()*6];
		final short[]connections = new short[location.size()*2];
		for (int i = 0; i < location.size(); i++) {
			Point3D_F64 p = location.get(i);
			Vector3D_F64 v = direction.get(i);
			int index = i*6;

			buf[index+0] = (float)p.x;
			buf[index+1] = (float)p.y;
			buf[index+2] = (float)p.z;
			buf[index+3] = (float)(p.x+v.x);
			buf[index+4] = (float)(p.y+v.y);
			buf[index+5] = (float)(p.z+v.z);

			connections[i*2+0] = (short)(i*2);
			connections[i*2+1] = (short)(i*2+1);
		}

		bridge.enqueue(new Callable<Void>(){
			public Void call(){
				float alpha = ((argb >> 24) & 0xFF ) / 255.0f;
				float red = ((argb >> 16) & 0xFF ) / 255.0f;
				float green = ((argb >> 8) & 0xFF ) / 255.0f;
				float blue = (argb & 0xFF ) / 255.0f;

				Material mat = new Material(bridge.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
				mat.setColor("Color", new ColorRGBA(red,green,blue,alpha) );
				mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

				Mesh m = new Mesh();
				m.setMode(Mesh.Mode.Lines);
				m.setBuffer(VertexBuffer.Type.Position, 3, buf);
				m.setBuffer(VertexBuffer.Type.Index, 2, connections);
				m.updateBound();

				Geometry g = new Geometry("Line",m);
				g.setShadowMode(RenderQueue.ShadowMode.Off);
				g.setQueueBucket(RenderQueue.Bucket.Opaque);
				g.setMaterial(mat);
				bridge.getRootNode().attachChild(g);
				return null;
			}});
	}

	public static List<Point2D_F64> ensureCCW( List<Point2D_F64> mesh ) {

		if(UtilPolygons2D_F64.isCCW(mesh) ) {
			return mesh;
		} else {
			List<Point2D_F64> ret = new ArrayList<Point2D_F64>();
			for (int i = 0; i < mesh.size(); i++) {
				ret.add( mesh.get(mesh.size()-1-i) );
			}
			return ret;
		}
	}

	public static Transform convertToJme( Se3_F64 a ) {
		Quaternion_F64 quat = RotationMatrixGenerator.matrixToQuaternion(a.getR(),null);
		Quaternion jmeQaut = new Quaternion((float)quat.x,(float)quat.y,(float)quat.z,(float)quat.w);

		Vector3D_F64 T = a.getT();
		Vector3f translation = new Vector3f((float)T.x,(float)T.y,(float)T.z);

		return new Transform(translation,jmeQaut);
	}

	@Override
	public void shutdownVisualize() {
		// wait for it to finish shutting down
		bridge.stop(true);
	}

	private float[] convertPointsToArray(List<Point3D_F64> points) {
		int N = points.size();

		final float buf[] = new float[N*3];
		for( int i = 0; i < N; i++ ) {
			Point3D_F64 p = points.get(i);
			buf[i*3+0] = (float)p.x;
			buf[i*3+1] = (float)p.y;
			buf[i*3+2] = (float)p.z;
		}
		return buf;
	}
}
