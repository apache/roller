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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.rendering.requests;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Represents a request for a weblog preview, either that of testing a new
 * theme or previewing an unpublished blog entry.
 */
public class WeblogPreviewRequest extends WeblogPageRequest {
    
    private static Log log = LogFactory.getLog(WeblogPreviewRequest.class);
    
    private static final String PREVIEW_SERVLET = "/tb-ui/authoring/preview";
    
    // lightweight attributes
    // theme name provided only for theme (not blog entry) previews.
    private String themeName = null;
    private String type = "standard";
    
    // heavyweight attributes
    private SharedTheme sharedTheme = null;
    private WeblogEntry weblogEntry = null;
    
    public WeblogPreviewRequest(HttpServletRequest request) {
        
        // let parent go first
        super(request);
        
        // we may have a specific theme to preview
        if(request.getParameter("theme") != null) {
            this.themeName = request.getParameter("theme");
        }

        //we may need to know the type of page we are going to preview
         if(request.getParameter("type") != null) {
             this.setType(request.getParameter("type"));
         }
        
        // we may also have a specific entry to preview
        if(request.getParameter("previewEntry") != null) {
            this.weblogAnchor = Utilities.decode(request.getParameter("previewEntry"));
        }

        if(log.isDebugEnabled()) {
            log.debug("theme = "+this.themeName);
        }
    }
    
    
    boolean isValidDestination(String servlet) {
        return (servlet != null && PREVIEW_SERVLET.equals(servlet));
    }
    
    public String getThemeName() {
        return themeName;
    }

    // override so that previews never show login status
    public String getAuthenticUser() {
        return null;
    }
    
    // override so that previews never show login status
    public boolean isLoggedIn() {
        return false;
    }

    public SharedTheme getSharedTheme() {
        
        if (sharedTheme == null && themeName != null) {
            try {
                ThemeManager themeMgr = WebloggerFactory.getWeblogger().getThemeManager();
                sharedTheme = themeMgr.getSharedTheme(themeName);
            } catch(IllegalArgumentException tnfe) {
                // bogus theme given in URL, return null for callee to handle
            } catch(WebloggerException re) {
                log.error("Error looking up theme " + themeName, re);
            }
        }
        
        return sharedTheme;
    }

    @Override
    public WeblogEntry getWeblogEntry() {
        if (weblogEntry == null && super.getWeblogAnchor() != null) {
            String anchor = super.getWeblogAnchor();

            try {
                WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
                weblogEntry = wmgr.getWeblogEntryByAnchor(getWeblog(), anchor);
            } catch (WebloggerException ex) {
                log.error("Error getting weblog entry " + anchor, ex);
            }
        }
        return weblogEntry;
    }
    
    public void setWeblogEntry(WeblogEntry weblogEntry) {
        this.weblogEntry = weblogEntry;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
