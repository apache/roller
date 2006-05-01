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
/* Created on Nov. 26, 2005 */package org.apache.roller.model;import java.util.Map;import java.io.Serializable;import org.apache.velocity.context.Context;import org.apache.roller.pojos.WeblogEntryData;import org.apache.roller.pojos.WebsiteData;/** * Manages Roller page plugins */public interface PagePluginManager extends Serializable {        /**      * Returns true if plugins are present      */    public boolean hasPagePlugins();        /**     * Create and init plugins for processing entries in a specified website.      * @param website        Website being processed      * @param servletContext ServetContext or null if running outside webapp     * @param contextURL     Absolute URL of webapp         * @param ctx            Velocity context      */    public Map createAndInitPagePlugins(            WebsiteData website,            Object servletContext,            String contextPath,            Context ctx);        /**     * Accepts weblog entry, creates copy, applies plugins to copy and     * returns the results.     * @param entry       Original weblog entry     * @param plugins     Map of plugins to apply     * @param str         String to which to apply plugins     * @param singleEntry Rendering for single entry page?     * @return        Copy of weblog entry with plugins applied     */    public String applyPagePlugins(       WeblogEntryData entry, Map pagePlugins, String str, boolean singleEntry);        /**      * Release all resources associated with Roller session.      */    public void release();      }