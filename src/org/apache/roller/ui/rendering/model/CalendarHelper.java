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
import org.apache.roller.pojos.WebsiteData;

import org.apache.roller.ui.authoring.tags.BigWeblogCalendarModel;
import org.apache.roller.ui.authoring.tags.WeblogCalendarModel;
import org.apache.roller.ui.core.LanguageUtil;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.tags.calendar.CalendarModel;
import org.apache.roller.ui.core.tags.calendar.CalendarTag;

/**
 * Displays weblog calendar by calling hybrid JSP tag.
 */
public class CalendarHelper  {
    private PageContext pageContext;
    
    protected static Log logger = 
        LogFactory.getFactory().getInstance(CalendarHelper.class);
    
    /**
     * Creates a new instance of CalendarHelper
     */
    public CalendarHelper(PageContext pageContext) {
        this.pageContext = pageContext;
    }  
    
    public String emitWeblogCalendar(String cat, boolean big) {
        
        HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
        HttpServletResponse response = (HttpServletResponse)pageContext.getResponse();

        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WebsiteData weblog = rreq.getWebsite();
        String pageLink = rreq.getPageLink();
        
        if ("nil".equals(cat)) cat = null;        
        String ret = null;
        try {
            String selfUrl = null;
            if (pageLink != null) {
                selfUrl = request.getContextPath() + "/page/"
                        + weblog.getHandle() + "/"+pageLink;
            } else {
                selfUrl = request.getContextPath()+"/page/" + weblog.getHandle();
            }

            CalendarModel model = null;
            if (big) {
                model = new BigWeblogCalendarModel(rreq, response, selfUrl, cat);
            } else {
                model = new WeblogCalendarModel(rreq, response, selfUrl, cat);
            }
            
            // save model in JSP page context so CalendarTag can find it
            pageContext.setAttribute("calendarModel",model);
            
            CalendarTag calTag = new CalendarTag();
            calTag.setPageContext(pageContext);
            calTag.setName("calendar");
            calTag.setModel("calendarModel");
            calTag.setLocale(LanguageUtil.getViewLocale(request));
            if (big) {
                calTag.setClassSuffix("Big");
            }
            ret = calTag.emit();
        } catch (Exception e) {
            logger.error("ERROR: initializing calendar tag",e);
        }
        return ret;
    }
}
