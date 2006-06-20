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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.ui.core.tags.menu.EditorNavigationBarTag;

/**
 * Displays editor menu/navigation-bar by calling hybrid JSP tag.
 */
public class EditorMenuHelper  {
    private PageContext pageContext;
    
    protected static Log logger =
            LogFactory.getFactory().getInstance(EditorMenuHelper.class);
    
    /**
     * Creates a new instance of EditorMenuHelper
     */       
    public EditorMenuHelper(PageContext pageContext) {
        this.pageContext = pageContext;
    }
    
    /**
     * Call hybrid EditorNavBarTag to render editor navbar.
     * @param vertical True for vertical navbar.
     * @return String HTML for navbar.
     */
    public String showEditorNavBar(boolean vertical) {
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
