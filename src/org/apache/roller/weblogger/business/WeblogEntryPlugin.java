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

package org.apache.roller.weblogger.business;

import java.util.Map;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Interface for Roller weblog entry plugins.
 *
 * Weblog entry plugins are used to make transformations to the entry text.
 * These plugins affect both the entry summary and entry body.
 */
public interface WeblogEntryPlugin {
    
    /**
     * Returns the display name of this Plugin.
     */
    public String getName();
    
    
    /**
     * Briefly describes the function of the Plugin.  May contain HTML.
     */
    public String getDescription();
    
    
    /**
     * Give plugin a chance to initialize and add objects the rendering model.
     *
     * @param weblog     Weblog being processed
     * @param model      Rendering model where objects can be placed
     */
    public void init(Weblog weblog) throws RollerException;
    
    
    /**
     * Apply plugin to the specified text.
     *
     * @param entry       Entry being rendered.
     * @param str         String to which plugin should be applied.
     * @return            Results of applying plugin to entry.
     */
    public String render(WeblogEntry entry, String str);
    
}
