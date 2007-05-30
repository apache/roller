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

package org.apache.roller.weblogger.ui.rendering.util;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.themes.ThemeNotFoundException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.util.URLUtilities;


/**
 * Represents a request for a weblog preview.
 */
public class WeblogPreviewRequest extends WeblogPageRequest {
    
    private static Log log = LogFactory.getLog(WeblogPreviewRequest.class);
    
    private static final String PREVIEW_SERVLET = "/roller-ui/authoring/preview";
    
    // lightweight attributes
    private String themeName = null;
    private String previewEntry = null;
    
    // heavyweight attributes
    private Theme theme = null;
    private WeblogEntry weblogEntry = null;
    
    public WeblogPreviewRequest(HttpServletRequest request) 
            throws InvalidRequestException {
        
        // let parent go first
        super(request);
        
        // we may have a specific theme to preview
        if(request.getParameter("theme") != null) {
            this.themeName = request.getParameter("theme");
        }
        
        // we may also have a specific entry to preview
        if(request.getParameter("previewEntry") != null) {
            this.previewEntry = URLUtilities.decode(request.getParameter("previewEntry"));
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

    public void setThemeName(String theme) {
        this.themeName = theme;
    }
    
    // override so that previews never show login status
    public String getAuthenticUser() {
        return null;
    }
    
    // override so that previews never show login status
    public boolean isLoggedIn() {
        return false;
    }

    public Theme getTheme() {
        
        if(theme == null && themeName != null) {
            try {
                ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
                theme = themeMgr.getTheme(themeName);
            } catch(ThemeNotFoundException tnfe) {
                // bogus theme specified ... don't worry about it
            } catch(RollerException re) {
                log.error("Error looking up theme "+themeName, re);
            }
        }
        
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public String getPreviewEntry() {
        return previewEntry;
    }

    public void setPreviewEntry(String previewEntry) {
        this.previewEntry = previewEntry;
    }
    
    // if we have a preview entry we would prefer to return that
    public WeblogEntry getWeblogEntry() {
        
        if(weblogEntry == null && 
                (previewEntry != null || super.getWeblogAnchor() != null)) {
            
            String anchor = previewEntry;
            if(previewEntry == null) {
                anchor = super.getWeblogAnchor();
            }
            
            try {
                WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
                weblogEntry = wmgr.getWeblogEntryByAnchor(getWeblog(), anchor);
            } catch (RollerException ex) {
                log.error("Error getting weblog entry "+anchor, ex);
            }
        }
        
        return weblogEntry;
    }
    
    public void setWeblogEntry(WeblogEntry weblogEntry) {
        this.weblogEntry = weblogEntry;
    }
    
}
