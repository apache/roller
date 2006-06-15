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

package org.apache.roller.model;

import org.apache.velocity.context.Context;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;


/**
 * Interface for Roller weblog entry page plugins, which can transform
 * entry summary or text fields.
 */
public interface PagePlugin {
    
    public String name = "PagePlugin";
    
    
    /**
     * Plugins can this as an opportunity to add any required objects
     * to the RollerRequest and the VelocityContext.
     * @param website     Website being processed
     * @param config      javax.servlet.ServletContext (or null if running outside webapp)
     * @param baseURL     Base URL of Roller site
     * @param ctx         Plugins may place objects into the Velocity Context.
     */
    public void init(
            WebsiteData website,
            Object servletContext,
            String baseURL,
            Context ctx) throws RollerException;
    
    /**
     * Apply plugin to summary or text string associated with entry.
     * @param entry       Entry being rendered.
     * @param str         String to which plugin should be applied.
     * @param singleEntry Indicates rendering on single entry page.
     * @return            Results of applying plugin to entry.
     */
    public String render(WeblogEntryData entry, String str);
    
    /**
     * Apply plugin to summary or text specified in string.
     * @param str String to which plugin should be applied.
     * @param singleEntry Indicates rendering on single entry page.
     * @return Results of applying plugin to string.
     */
    public String render(String str);
    
    /**
     * Must implement toString(), returning the human-friendly
     * name of this Plugin.  This is what users will see.
     * @return The human-friendly name of this Plugin.
     */
    public String toString();
    
    /**
     * Returns the human-friendly name of this Plugin.
     * This is what users will see.
     * @return The human-friendly name of this Plugin.
     */
    public String getName();
    
    /**
     * Briefly describes the function of the Plugin. May
     * contain HTML.
     * @return A brief description of the Plugin.
     */
    public String getDescription();
    
    /** Returns true if this plugin should be skipped on single entry pages. */
    public boolean getSkipOnSingleEntry();
    
}
