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
import org.apache.roller.RollerException;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;
import org.apache.roller.ui.core.tags.calendar.BigWeblogCalendarModel;
import org.apache.roller.ui.core.LanguageUtil;
import org.apache.roller.ui.core.tags.calendar.CalendarTag;
import org.apache.roller.ui.core.tags.calendar.WeblogCalendarModel;
import org.apache.roller.ui.rendering.util.WeblogPageRequest;


/**
 * Model which provides functionality for displaying weblog calendar.
 * 
 * Implemented by calling hybrid JSP tag.
 */
public class CalendarModel implements Model {
    
    private static Log log = LogFactory.getLog(CalendarModel.class);
    
    private PageContext pageContext = null;
    private WeblogPageRequest pageRequest = null;
    
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "calendarModel";
    }
    
    
    /** Init page model based on request */
    public void init(Map initData) throws RollerException {
        
        // extract page context
        this.pageContext = (PageContext) initData.get("pageContext");
        
        // we expect the init data to contain a pageRequest object
        this.pageRequest = (WeblogPageRequest) initData.get("pageRequest");
        if(this.pageRequest == null) {
            throw new RollerException("expected pageRequest from init data");
        }
    }
    
    
    public String showWeblogCalendar(WebsiteDataWrapper websiteWrapper, String catArgument) {        
        return showWeblogCalendar(websiteWrapper, catArgument, false);
    }
    
    
    public String showWeblogCalendarBig(WebsiteDataWrapper websiteWrapper, String catArgument) { 
        return showWeblogCalendar(websiteWrapper, catArgument, true);
    }
    
    
    private String showWeblogCalendar(WebsiteDataWrapper websiteWrapper, String catArgument, boolean big) {
        
        if ("nil".equals(catArgument)) catArgument = null;        
        String ret = null;
        try {
            org.apache.roller.ui.core.tags.calendar.CalendarModel model = null;
            if (big) {
                model = new BigWeblogCalendarModel(pageRequest, catArgument);
            } else {
                model = new WeblogCalendarModel(pageRequest, catArgument);
            }
            
            // save model in JSP page context so CalendarTag can find it
            pageContext.setAttribute("calendarModel", model);
            
            CalendarTag calTag = new CalendarTag();
            calTag.setPageContext(pageContext);
            calTag.setName("calendar");
            calTag.setModel("calendarModel");
            calTag.setLocale(pageRequest.getLocaleInstance());
            if (big) {
                calTag.setClassSuffix("Big");
            }
            ret = calTag.emit();
        } catch (Exception e) {
            log.error("ERROR: initializing calendar tag",e);
        }
        return ret;
    }
    
}
