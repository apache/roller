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

package org.apache.roller.business.themes;

import java.util.List;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.Theme;
import org.apache.roller.pojos.WebsiteData;


/**
 * Manager interface for accessing Theme related objects.
 */
public interface ThemeManager {
    
    /**
     * Get the Theme object with the given id.
     *
     * @return Theme The Theme object with the given id.
     * @throws ThemeNotFoundException If the named theme cannot be found.
     * @throws RollerException If there is some kind of fatal backend error.
     **/
    public Theme getTheme(String id)
        throws ThemeNotFoundException, RollerException;
    
    
    /**
     * Get a list of all theme names that are currently enabled.
     * This list is ordered alphabetically by default.
     *
     * @return List A list of Theme objects which are enabled.
     */
    public List getEnabledThemesList();
    
    
    /**
     * Import all the contents for a Theme into a weblog.
     *
     * @param weblog The weblog to import the theme into.
     * @param theme The theme that should be imported.
     *
     * @throws RollerException If there is some kind of error in saving.
     */
    public void importTheme(WebsiteData website, Theme theme) 
        throws RollerException;
    
}
