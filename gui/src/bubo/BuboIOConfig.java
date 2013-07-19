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

package bubo;

import bubo.gui.LogoComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains parameters that can turns functionality on and off globally.
 *
 * @author Peter Abeles
 */
public class BuboIOConfig {

    public final static boolean SHOW_BUBO_LOGO = true;

    public final static boolean SHOW_DEBUG_GUI = true;

    public final static List<LogoComponent> DEFAULT_LOGOS = new ArrayList<LogoComponent>();

    static {
        if( SHOW_BUBO_LOGO )
            DEFAULT_LOGOS.add(new LogoComponent());
    }

}
