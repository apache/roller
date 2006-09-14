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

package org.apache.roller.ui.rendering.model;

import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.ui.core.tags.menu.EditorNavigationBarTag;


/**
 * Model which provides methods for displaying editor menu/navigation-bar.
 * 
 * Implemented by calling hybrid JSP tag.
 */
public class MenuModel implements Model {
    
    private static Log logger = LogFactory.getLog(MenuModel.class);
    
    private PageContext pageContext = null;
    
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "menuModel";
    }
    
    
    /** Init page model based on request */
    public void init(Map initData) throws RollerException {
        
        // extract page context
        this.pageContext = (PageContext) initData.get("pageContext");
    }
    
    
    /**
     * Display author menu.
     * @param vertical True for vertical navbar.
     * @return String HTML for navbar.
     */
    public String showAuthorMenu(boolean vertical) {
        EditorNavigationBarTag editorTag = new EditorNavigationBarTag();
        editorTag.setPageContext(pageContext);
        if ( vertical ) {
            editorTag.setView("templates/navbar/navbar-vertical.vm");
        } else {
            editorTag.setView("templates/navbar/navbar-horizontal.vm");
        }
        editorTag.setModel("editor-menu.xml");
        return editorTag.emit();
    }
    
}
