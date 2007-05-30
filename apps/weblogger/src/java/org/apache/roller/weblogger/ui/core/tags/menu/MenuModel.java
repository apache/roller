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

package org.apache.roller.weblogger.ui.core.tags.menu;
import javax.servlet.http.HttpServletRequest;

import org.apache.roller.RollerException;


/** Menu model contains Menus */
public interface MenuModel {
    /** Collection of Menu objects contained in this menu model */
    public java.util.Vector getMenus();
    
    /** Return menu selected by current request or first menu.
     *  If request does not indicate a menu then first menu is returned.
     * @throws RollerException
     */
    public Menu getSelectedMenu( HttpServletRequest req ) throws RollerException;
}

