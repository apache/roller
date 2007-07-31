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

package org.apache.roller.weblogger.business.plugins.comment;

import org.apache.roller.weblogger.pojos.WeblogEntryComment;


/**
 * Interface for weblog entry comment plugins.
 *
 * Weblog entry comment plugins are used to make transformations to comment text.
 */
public interface WeblogEntryCommentPlugin {
    
    /**
     * A unique identifier for the plugin.
     */
    public String getId();
    
    
    /**
     * Returns the display name of this Plugin.
     */
    public String getName();
    
    
    /**
     * Briefly describes the function of the Plugin.  May contain HTML.
     */
    public String getDescription();
    
    
    /**
     * Apply plugin to the specified text.
     *
     * @param entry       Entry being rendered.
     * @param str         String to which plugin should be applied.
     * @return            Results of applying plugin to string.
     */
    public String render(final WeblogEntryComment comment, String str);
    
}
