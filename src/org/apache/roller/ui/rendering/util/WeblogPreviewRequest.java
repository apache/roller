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

package org.apache.roller.ui.rendering.util;

import javax.servlet.http.HttpServletRequest;


/**
 * Represents a request for a weblog preview.
 */
public class WeblogPreviewRequest extends WeblogPageRequest {
    
    private static final String PREVIEW_SERVLET = "/roller-ui/authoring/preview";
    
    private String theme = null;
    
    
    public WeblogPreviewRequest(HttpServletRequest request) 
            throws InvalidRequestException {
        
        // let parent go first
        super(request);
        
        // all we need to worry about is the query params
        // the only param we expect is "theme"
        if(request.getParameter("theme") != null) {
            this.theme = request.getParameter("theme");
        }
    }
    
    
    boolean isValidDestination(String servlet) {
        return (servlet != null && PREVIEW_SERVLET.equals(servlet));
    }
    
    
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
    
    // override so that previews never show login status
    public String getAuthenticUser() {
        return null;
    }
    
    // override so that previews never show login status
    public boolean isLoggedIn() {
        return false;
    }
    
}
