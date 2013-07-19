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

package bubo.maps.d2.grid.impl;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Abeles
 */
public class TestArrayGrid2D_I8 {

    @Test
    public void setAll() {
        ArrayGrid2D_I8 map = new ArrayGrid2D_I8(5,7);

        for( int i = 0; i < map.getHeight(); i++ ) {
            for( int j = 0; j < map.getWidth(); j++ ) {
                assertTrue( 12 != map.get(j,i));
            }
        }

        map.setAll(12);

        for( int i = 0; i < map.getHeight(); i++ ) {
            for( int j = 0; j < map.getWidth(); j++ ) {
                assertTrue( 12 == map.get(j,i));
            }
        }
    }

    @Test
    public void set_get() {
        ArrayGrid2D_I8 map = new ArrayGrid2D_I8(5,7);

        assertTrue( 12 != map.get(2,3));
        map.set(2,3,12);
        assertTrue( 12 == map.get(2,3));
    }

    @Test
    public void isKnown() {
        ArrayGrid2D_I8 map = new ArrayGrid2D_I8(5,7);

        map.set(2,3,ArrayGrid2D_I8.UNKNOWN);
        assertFalse(map.isKnown(2,3));
        map.set(2,3,10);
        assertTrue(map.isKnown(2,3));
    }

    @Test
    public void isValid() {
        ArrayGrid2D_I8 map = new ArrayGrid2D_I8(5,7);

        assertTrue( map.isValid(0));
        assertTrue( map.isValid(256));
        assertTrue( map.isValid(70));
        assertFalse( map.isValid(-1));
        assertFalse( map.isValid(1000));
    }
}
