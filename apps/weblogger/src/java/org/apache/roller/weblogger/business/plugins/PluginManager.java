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

package org.apache.roller.weblogger.business.plugins;

import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.business.plugins.comment.WeblogEntryCommentPlugin;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;


/**
 * Plugin management for business layer and more generally applied plugins.
 */
public interface PluginManager {
    
    /**
     * Returns true if plugins are present
     */
    public boolean hasPagePlugins();
    
    
    /**
     * Returns a list of all registered weblog entry plugins initialized for
     * use with the specified weblog.
     *
     * @param website        Website being processed
     */
    public Map getWeblogEntryPlugins(Weblog website);
    
    
    /**
     * Apply a set of weblog entry plugins to the specified string and
     * return the results.  This method must *NOT* alter the contents of
     * the original entry object.
     *
     * @param entry       Original weblog entry
     * @param plugins     Map of plugins to apply
     * @param str         String to which to apply plugins
     * @return        the transformed text
     */
    public String applyWeblogEntryPlugins(Map pagePlugins,WeblogEntry entry, String str);
    
    
    /**
     * Get the list of WeblogEntryCommentPlugin classes configured.
     *
     * This lists the set of plugins which are available to the system, not the
     * set of plugins which are enabled.
     *
     * Should return an empty list if no plugins are configured.
     */
    public List<WeblogEntryCommentPlugin> getCommentPlugins();
    
    
    /**
     * Apply comment plugins.
     *
     * @param comment The comment to apply plugins for.
     * @param text The text to apply the plugins to.
     * @return String The transformed comment text.
     */
    public String applyCommentPlugins(WeblogEntryComment comment, String text);
    
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
}
