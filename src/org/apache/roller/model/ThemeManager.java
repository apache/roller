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

import java.util.List;
import org.apache.roller.RollerException;
import org.apache.roller.ThemeNotFoundException;
import org.apache.roller.pojos.Theme;
import org.apache.roller.pojos.ThemeTemplate;
import org.apache.roller.pojos.WebsiteData;


/**
 * Manager interface for accessing Theme related objects.
 */
public interface ThemeManager {
    
    /**
     * Get the Theme object with the given name.
     *
     * @throws ThemeNotFoundException If the named theme cannot be found.
     * @throws RollerException If there is some kind of fatal backend error.
     **/
    public Theme getTheme(String name)
        throws ThemeNotFoundException, RollerException;
    
    
    /**
     * Get the Theme object with the given theme id.
     *
     * @throws ThemeNotFoundException If the named theme cannot be found.
     * @throws RollerException If there is some kind of fatal backend error.
     */
    public Theme getThemeById(String theme_id)
        throws ThemeNotFoundException, RollerException;
    
    
    /**
     * Get a list of all available themes.
     * This list is ordered alphabetically by default.
     *
     * NOTE: this only returns a list of theme names, not actual Theme objects.
     **/
    public List getThemesList();
    
    
    /**
     * Get a list of all theme names that are currently enabled.
     * This list is ordered alphabetically by default.
     *
     * NOTE: this only returns a list of theme names, not actual Theme objects.
     */
    public List getEnabledThemesList();
    
    
    /**
     * Get the template from a given theme.
     *
     * @throws ThemeNotFoundException If the named theme cannot be found.
     * @throws RollerException If there is some kind of fatal backend error.
     */
    public ThemeTemplate getTemplate(String theme_name, String template_name)
        throws ThemeNotFoundException, RollerException;
    
    
    /**
     * Get the template from a given theme using the template id.
     *
     * Theme templates use a special id value when they come off the filesystem.
     * When a theme is read off the filesystem it's templates are given an id
     * like ... <theme name>:<template name>
     *
     * @throws ThemeNotFoundException If the named theme cannot be found.
     * @throws RollerException If there is some kind of fatal backend error.
     */
    public ThemeTemplate getTemplateById(String template_id)
        throws ThemeNotFoundException, RollerException;

    
    /**
     * Get the template from a given theme using the template link value.
     *
     * Note that for themes we enforce the rule that 
     *      Theme.name == Theme.link
     *
     * So doing a lookup by link is the same as doing a lookup by name.
     *
     * @throws ThemeNotFoundException If the named theme cannot be found.
     * @throws RollerException If there is some kind of fatal backend error.
     */
    public ThemeTemplate getTemplateByLink(String theme_name, String template_link)
        throws ThemeNotFoundException, RollerException;
   
    public void saveThemePages(WebsiteData website, Theme theme) throws RollerException;
}
