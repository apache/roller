/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.ui.rendering.velocity;

import org.apache.roller.ui.rendering.Renderer;
import org.apache.roller.ui.rendering.RendererFactory;


/**
 * Velocity RendererFactory for Roller.
 */
public class VelocityRendererFactory implements RendererFactory {
    
    
    public Renderer getRenderer(String rendererType, String resourceId) {
        
        // nothing we can do with null values
        if(rendererType == null || resourceId == null) {
            return null;
        }
        
        if("velocity".equals(rendererType)) {
            
            // standard velocity template
            return new VelocityRenderer(resourceId);
        } else if("velocityWeblogPage".equals(rendererType)) {
            
            // special case for velocity weblog page templates
            // needed because of the way we do the decorator stuff
            return new VelocityWeblogPageRenderer(resourceId);
        }
        
        // we don't want to handle this content
        return null;
    }
    
}
