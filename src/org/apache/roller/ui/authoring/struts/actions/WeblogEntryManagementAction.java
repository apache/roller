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

package org.apache.roller.ui.authoring.struts.actions;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.authoring.struts.formbeans.WeblogEntryManagementForm;
import org.apache.roller.util.DateUtil;


/////////////////////////////////////////////////////////////////////////////
/**
 * Query weblog entries and display the results in tabular form.
 *
 * @struts.action path="/roller-ui/authoring/weblogEntryManagement" name="weblogEntryManagementForm"
 *     scope="request" parameter="method"
 *
 * @struts.action-forward name="weblogEntryManagement.page" path=".WeblogEntryManagement"
 *
 * @author Dave Johnson
 */
public final class WeblogEntryManagementAction extends DispatchAction {
    //-----------------------------------------------------------------------
    /**
     * Respond to request to display weblog entry management page.
     * Loads the appropriate model objects and forwards the request to
     * the edit weblog page.
     */
    public ActionForward query(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException, RollerException {
        WeblogEntryManagementForm form = (WeblogEntryManagementForm)actionForm;
        RollerRequest   rreq = RollerRequest.getRollerRequest(request);
        WeblogManager   wmgr = RollerFactory.getRoller().getWeblogManager();
        RollerSession   rses = RollerSession.getRollerSession(request);
        
        // ensure that weblog is specfied and user has permission to work there
        if (rreq.getWebsite() != null && rses.isUserAuthorized(rreq.getWebsite())) {
            String status= form.getStatus().equals("ALL") ? null : form.getStatus();
            request.setAttribute("model", new PageModel(
                    request,
                    response,
                    mapping,
                    rreq.getWebsite(),
                    form));
        } else {
            return mapping.findForward("access-denied");
        }
        return mapping.findForward("weblogEntryManagement.page");
    }
    
    public class PageModel extends BasePageModel {
        private List               entries = null;
        private RollerRequest      rollerRequest = null;
        private HttpServletRequest request = null;
        private WebsiteData        website = null;
        private String             category = null;
        private Date               startDate = null;
        private Date               endDate = null;
        private String             status = WeblogEntryData.PUBLISHED;
        private Integer            maxEntries = null;
        private boolean            more = false;
        private WeblogEntryManagementForm    queryForm = null;
        
        public PageModel(
                HttpServletRequest request,
                HttpServletResponse response,
                ActionMapping mapping,
                WebsiteData website,
                WeblogEntryManagementForm queryForm) throws RollerException {
            super("weblogEntryQuery.title", request, response, mapping);
            rollerRequest = RollerRequest.getRollerRequest(request);
            this.request = request;
            this.queryForm = queryForm;
            this.website = website;
            
            if (null != queryForm.getCategoryId() && !queryForm.getCategoryId().equals("")) {
                WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
                WeblogCategoryData cd = wmgr.getWeblogCategory(queryForm.getCategoryId());
                category = cd.getPath();
            }
            
            final DateFormat df =
                    DateFormat.getDateInstance(DateFormat.SHORT, request.getLocale());
            String start = queryForm.getStartDateString();
            if (null != start && start.trim().length() > 0) {
                try {
                    startDate = DateUtil.getStartOfDay(df.parse(start));
                } catch (ParseException e) {
                    throw new RollerException("ERROR parsing start date.");
                }
            }
            
            String end = queryForm.getEndDateString();
            if (null != end && end.trim().length() > 0) {
                try {
                    endDate = DateUtil.getEndOfDay(df.parse(end));
                } catch (ParseException e) {
                    throw new RollerException("ERROR parsing end date.");
                }
            }
            
            this.status = "ALL".equals(queryForm.getStatus()) ? null: queryForm.getStatus();
            this.maxEntries = maxEntries;
            
            String[] tagsarr = new String[0];
            
            if (queryForm.getTags() != null)
                tagsarr = StringUtils.split(queryForm.getTags().toLowerCase(), ' ');
            
            int offset = queryForm.getOffset();
            if ("POST".equals(request.getMethod())) { 
                offset = 0;
            }
            
            entries = RollerFactory.getRoller().getWeblogManager().getWeblogEntries(
                    website,
                    null,
                    startDate,
                    endDate,
                    category,
                    Arrays.asList(tagsarr),
                    status,
                    queryForm.getSortby(),
                    null,
                    null,
                    offset,
                    queryForm.getCount() + 1);
            if (entries.size() > queryForm.getCount()) {
                more = true;
                entries.remove(entries.size()-1);
            }
        }
        
        public String getBaseURL() {
            return getRequest().getContextPath();
        }
        
        /**
         * Get recent weblog entries using request parameters to determine
         * username, date, and category name parameters.
         * @return List of WeblogEntryData objects.
         * @throws RollerException
         */
        public List getRecentWeblogEntries() throws RollerException {
            return entries;
        }
        
        public int getWeblogEntryCount() {
            return entries.size();
        }
        
        public List getCategories() throws Exception {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            List categories = RollerFactory.getRoller().getWeblogManager()
            .getWeblogCategories(rreq.getWebsite());
            return categories;
        }
        
        public Date getEarliestDate() {
            Date date = null;
            if (entries.size() > 0) {
                WeblogEntryData earliest = (WeblogEntryData)entries.get(entries.size()-1);
                date = earliest.getPubTime();
            }
            return date;
        }
        
        public Date getLatestDate() {
            Date date = null;
            if (entries.size() > 0) {
                WeblogEntryData latest = (WeblogEntryData)entries.get(0);
                date = latest.getPubTime();
            }
            return date;
        }
        
        public String getNextLink() {
            if (more) {
                int offset = queryForm.getOffset() + queryForm.getCount();
                offset = (offset < 0) ? 0 : offset;
                return getQueryLink() + "&offset=" + offset;
            } else {
                return null;
            }
        }
        
        public String getPrevLink() {
            if (queryForm.getOffset() > 0) {
                int offset = queryForm.getOffset() - queryForm.getCount();
                offset = (offset < 0) ? 0 : offset;
                return getQueryLink() + "&offset=" + offset;
            } else {
                return null;
            }
        }
        
        private String getQueryLink() {
            StringBuffer sb = new StringBuffer();
            sb.append(request.getContextPath());
            sb.append("/roller-ui/authoring/weblogEntryManagement.do"); // TODO: get path from Struts
            sb.append("?method=query");
            sb.append("&weblog=");
            sb.append(getWebsite().getHandle());
            
            sb.append("&count=");
            sb.append(queryForm.getCount());
            
            if (StringUtils.isNotEmpty(queryForm.getStartDateString())) {
                sb.append("&startDateString=");
                sb.append(queryForm.getStartDateString());
            }
            
            if (StringUtils.isNotEmpty(queryForm.getEndDateString())) {
                sb.append("&endDateString=");
                sb.append(queryForm.getEndDateString());
            }
            
            if (StringUtils.isNotEmpty(queryForm.getCategoryId())) {
                sb.append("&categoryId=");
                sb.append(queryForm.getCategoryId());
            }
            
            if (StringUtils.isNotEmpty(queryForm.getTags())) {
                sb.append("&tags=");
                sb.append(queryForm.getTags());
            }
            
            if (StringUtils.isNotEmpty(queryForm.getSortby())) {
                sb.append("&sortBy=");
                sb.append(queryForm.getSortby());
            }
            
            if (StringUtils.isNotEmpty(queryForm.getStatus())) {
                sb.append("&status=");
                sb.append(queryForm.getStatus());
            }
            
            return sb.toString();
        }
    }
}
