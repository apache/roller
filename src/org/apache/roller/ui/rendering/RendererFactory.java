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

package org.apache.roller.ui.rendering;

import org.apache.roller.pojos.Template;


/**
 * A factory for Renderer objects.
 *
 * Implementations of this interface are used to handle the actual lookup of
 * what Renderer object should be used to render a given resource.
 */
public interface RendererFactory {
    
    
    /**
     * Get a Renderer that will handle the given Template.
     * If a RendererFactory does not have a Renderer which can handle the
     * content then it may return null.
     */
    public Renderer getRenderer(Template template);
    
}
