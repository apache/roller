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

package org.apache.roller.weblogger.pojos;

import org.apache.roller.weblogger.WebloggerException;

import java.util.Date;
import java.util.List;


/**
 * A Theme represents the set of components which are used to generate the
 * web design for a weblog along with some metadata like a name, etc.
 */
public interface Theme {
    
    /**
     * A unique identifier for this Theme.
     */
    String getId();
    
    
    /**
     * A common or display name for this Theme.
     */
    String getName();

    /**
     * A description of the Theme.
     */
    String getDescription();
    
    
    /**
     * The last modification date of the Theme.
     */
    Date getLastModified();
    
    
    /**
     * Is the Theme enable for use?
     */
    boolean isEnabled();
    
    
    /**
     * Get the list of all templates associated with this Theme.
     */
    List<? extends ThemeTemplate> getTemplates() throws WebloggerException;
    
    
    /**
     * Lookup the stylesheet template for the Theme.
     */
    ThemeTemplate getStylesheet() throws WebloggerException;
    
    
    /**
     * Lookup the default template for the Theme.
     */
    ThemeTemplate getDefaultTemplate() throws WebloggerException;
    
    
    /**
     * Lookup a template by action.
     * Returns null if the template cannot be found.
     */
    ThemeTemplate getTemplateByAction(String action) throws WebloggerException;
    
    
    /**
     * Lookup a template by name.
     * Returns null if the template cannot be found.
     */
    ThemeTemplate getTemplateByName(String name) throws WebloggerException;
    
    
    /**
     * Lookup a template by link.
     * Returns null if the template cannot be found.
     */
    ThemeTemplate getTemplateByLink(String link) throws WebloggerException;
    
    
    /**
     * Lookup a resource by path.
     * Returns null if the resource cannot be found.
     */
    ThemeResource getResource(String path);
    
}
