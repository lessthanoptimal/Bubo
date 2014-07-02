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

package bubo.gui.jme;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

import java.util.Random;

/**
 * An easy check to see if JME is working.  Opens a window and displays a random point cloud
 *
 * @author Peter Abeles
 */
public class VisualCheckOfJmeApp extends SimpleApplication {
	public static void main(String[] args){
		VisualCheckOfJmeApp app = new VisualCheckOfJmeApp();
		app.start();
	}

	@Override
	public void simpleInitApp() {

		Random rand = new Random(234);

		int N = 1000;

		float buf[] = new float[N*3];
		for( int i = 0; i < N; i++ ) {
			buf[i*3+0] = rand.nextFloat();
			buf[i*3+1] = rand.nextFloat();
			buf[i*3+2] = rand.nextFloat();
		}

		Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat.setColor("Color", ColorRGBA.Green);
		mat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

		Mesh m = new Mesh();
		m.setBuffer(VertexBuffer.Type.Position, 3, buf);
		m.setMode(Mesh.Mode.Points);
		m.setPointSize(1f);
		m.setStatic();
		m.updateBound();

		Geometry g = new Geometry("Point Cloud", m);
		g.setShadowMode(RenderQueue.ShadowMode.Off);
		g.setQueueBucket(RenderQueue.Bucket.Opaque);
		g.setMaterial(mat);
		rootNode.attachChild(g);
	}
}
