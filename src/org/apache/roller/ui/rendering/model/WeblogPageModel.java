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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.CommentDataWrapper;
import org.apache.roller.pojos.wrapper.WeblogCategoryDataWrapper;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;
import org.apache.roller.ui.authoring.struts.formbeans.CommentFormEx;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.util.DateUtil;

/*
 * Roller 3.0 development notes
 * 
 * - I'm still using RollerRequest here, but I'd like to factor it out
 *   so I'm working to isolate it's usage as much as possible.
 *
 * - I'm still using 'nil' for Velocity null, but perhaps that is unnecessary
 *   or nil-advised ;-) Maybe we can use an undefined variable like $nullValue 
 *   insteaed to indicate null when using the new page models.
 *
 * - Note to self - make sure POJOs provide these methods:
 *      website.getPages()
 *      website.getComments()
 *      website.getPageByName(String name)
 *      webste.getDayHits()
 */

/**
 * New Atlas minimalistic page model provides access to a weblog, possibly
 * a weblog entry and pageable collections of entries and comments.
 */
public class WeblogPageModel implements PageModel {    
    private HttpServletRequest request = null;
    private WeblogEntryDataWrapper nextEntry = null;
    private WeblogEntryDataWrapper prevEntry = null;
    private WeblogEntryDataWrapper firstEntry = null;
    private WeblogEntryDataWrapper lastEntry = null;
    
    protected static Log logger = 
            LogFactory.getFactory().getInstance(WeblogPageModel.class);
    
    /** Creates a new instance of AtlasWeblogPageModel */
    public WeblogPageModel() {
    }
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "page";
    }

    /** Init page model based on request */
    public void init(Map map) {
        this.request = (HttpServletRequest)map.get("request");
    }
    
    /**
     * Get website being displayed.
     */
    public WebsiteDataWrapper getWeblog() { 
        
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WebsiteData weblog = rreq.getWebsite();
        
        WebsiteDataWrapper ret = null;
        if (weblog != null) {
            ret = WebsiteDataWrapper.wrap(weblog);
        }
        return ret;
    }
    
    /** 
     * Get weblog entry to be displayed; null if not on single-entry page or if entry not published.
     */
    public WeblogEntryDataWrapper getWeblogEntry() {
        
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WeblogEntryData entry = rreq.getWeblogEntry(); 
        
        WeblogEntryDataWrapper ret = null;
        if (entry != null && entry.getStatus().equals(WeblogEntryData.PUBLISHED)) {
            ret = WeblogEntryDataWrapper.wrap(entry);
        }
        return ret;
    }
        
    /** 
     * Get weblog category or null if request does not specify one.
     */
    public WeblogCategoryDataWrapper getWeblogCategory() {
        
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WeblogCategoryData cat = rreq.getWeblogCategory(); 
        
        WeblogCategoryDataWrapper ret = null;
        if (cat != null) {
            ret = WeblogCategoryDataWrapper.wrap(cat);
        }
        return ret;
    }
    
    /**
     * Get most recent weblog entries for date and category specified by request.
     * @return List of WeblogEntryDataWrapper objects. 
     */
    public List getWeblogEntriesList(String cat, int offset, int length) {
        
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        Date date                   = rreq.getDate();
        WebsiteData weblog          = rreq.getWebsite();
        WeblogCategoryData category = rreq.getWeblogCategory(); 
        boolean isDaySpecified      = rreq.isDaySpecified();
        boolean isMonthSpecified    = rreq.isMonthSpecified();
        
        if ("nil".equals(cat)) cat = null;
        List ret = new ArrayList();
        try {
            if (date == null) date = new Date();
            
            // If request specifies a category, then use that
            String catParam = null;
            if (category != null) {
                catParam = category.getPath();
            } else if (cat != null) {
                // use category argument instead
                catParam = cat;
            } else if (weblog != null) // MAIN
            {
                catParam = weblog.getDefaultCategory().getPath();
                if (catParam.equals("/")) {
                    catParam = null;
                }
            }
            WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();            
            List mEntries = mgr.getWeblogEntries(
                    weblog,      // weblog
                    null,        // user
                    null,        // startDate
                    date,        // endDate
                    catParam,    // catName
                    WeblogEntryData.PUBLISHED, // status
                    null,        // sortby (null for pubTime)
                    offset,      // offset into results
                    length);     // max results to return
            
            // wrap pojos
            ret = new ArrayList(mEntries.size());
            Iterator it = mEntries.iterator();
            int i=0;
            while(it.hasNext()) {
                ret.add(i, WeblogEntryDataWrapper.wrap((WeblogEntryData) it.next()));
                i++;
            }
        } catch (Exception e) {
            logger.error("ERROR: getting entry list", e);
        }
        return ret;
    }
    
    /**
     * Get most recent weblog entries for day or month specified by request.
     * @return Map of Lists of weblog entry objects, keyed by 8-char date strings.
     */
    public Map getWeblogEntriesMonthMap(String cat, int offset, int length) {
        
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        Date date                   = rreq.getDate();
        WebsiteData weblog          = rreq.getWebsite();
        WeblogCategoryData category = rreq.getWeblogCategory(); 
        boolean isDaySpecified      = rreq.isDaySpecified();
        boolean isMonthSpecified    = rreq.isMonthSpecified();
        
        if ("nil".equals(cat)) cat = null;
        Map ret = new HashMap();
        try {            
            String catParam = null;
            if (category != null) {
                // request specified category so use it
                catParam = category.getPath();
            } else if (cat != null) {
                // use category argument instead
                catParam = cat;
            } else if (weblog != null) {
                // otherwise use default category of weblog
                catParam = weblog.getDefaultCategory().getPath();
                if (catParam.equals("/")) {
                    catParam = null;
                }
            }
            
            // if weblog is specified, use its timezone
            Calendar cal = null;
            if (weblog != null) { 
                TimeZone tz = weblog.getTimeZoneInstance();
                cal = Calendar.getInstance(tz);
            } else {
                cal = Calendar.getInstance();
            }
            
            int limit = length;
            Date startDate = null;
            Date endDate = date;
            if (endDate == null) endDate = new Date();
            if (isDaySpecified) { 
                // URL specified a specific day so get all entries for that day
                endDate = DateUtil.getEndOfDay(endDate, cal);
                startDate = DateUtil.getStartOfDay(endDate, cal); 
                limit = Integer.MAX_VALUE;                  
            } else if (isMonthSpecified) {
                endDate = DateUtil.getEndOfMonth(endDate, cal);
            }
            Map mmap = RollerFactory.getRoller().getWeblogManager().getWeblogEntryObjectMap(
                    weblog,
                    startDate,          
                    endDate,                  
                    catParam,                   
                    WeblogEntryData.PUBLISHED, 0, -1); 
            
            // need to wrap pojos
            java.util.Date key = null;
            Iterator days = mmap.keySet().iterator();
            while(days.hasNext()) {
                key = (java.util.Date)days.next();
                
                // now we need to go through each entry in a day and wrap
                List wrappedEntries = new ArrayList();
                List entries = (List) mmap.get(key);
                for(int i=0; i < entries.size(); i++) {
                    wrappedEntries.add(i,
                         WeblogEntryDataWrapper.wrap((WeblogEntryData)entries.get(i)));
                }
                mmap.put(key, wrappedEntries);
            }
            
            ret = mmap;
            
            setFirstAndLastEntries( ret );
            
        } catch (Exception e) {
            logger.error("ERROR: getting entry month map", e);
        }
        return ret;
    }
    
    /** 
     * Get weblog entry to be displayed or null if not on single-entry page.
     */
    public WeblogEntryDataWrapper getWeblogEntryNext() {
        
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WeblogCategoryData category = rreq.getWeblogCategory(); 
        
        WeblogEntryDataWrapper currentEntry = getWeblogEntry();
        if (firstEntry != null) currentEntry = firstEntry;
        if (nextEntry == null && currentEntry != null) {
            String catName = null;
            if (category != null) {
                catName = category.getName();
            }
            try {
                WeblogManager wmgr = 
                    RollerFactory.getRoller().getWeblogManager();
                WeblogEntryData next =
                    wmgr.getNextEntry(currentEntry.getPojo(), catName);
                
                if (nextEntry != null)
                    nextEntry = WeblogEntryDataWrapper.wrap(next);
                
                // make sure that mNextEntry is not published to future
                if (nextEntry != null && nextEntry.getPubTime().after(new Date())) {
                    nextEntry = null;
                }
            } catch (RollerException e) {
                logger.error("ERROR: getting next entry", e);
            }
        }
        return nextEntry;
    }
    
    /** 
     * Get weblog entry to be displayed or null if not on single-entry page.
     */
    public WeblogEntryDataWrapper getWeblogEntryPrev() {
        
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WeblogCategoryData category = rreq.getWeblogCategory(); 
        
        WeblogEntryDataWrapper currentEntry = getWeblogEntry();
        if (lastEntry != null) currentEntry = lastEntry;
        if (prevEntry == null && currentEntry != null ) {
            String catName = null;
            if (category != null) {
                catName = category.getName();
            }
            try {
                WeblogManager wmgr = 
                     RollerFactory.getRoller().getWeblogManager();
                WeblogEntryData prev =
                     wmgr.getPreviousEntry(currentEntry.getPojo(), catName);                
                if(prev != null) {
                   prevEntry = WeblogEntryDataWrapper.wrap(prev);
                }
            } catch (RollerException e) {
                logger.error("ERROR: getting previous entry", e);
            }
        }
        return prevEntry;
    }
    
    /**
     * Get most recent approved and non-spam comments in weblog.
     * @return List of CommentDataWrapper objects.
     */
    public List getComments(int offset, int length) {
        
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WebsiteData weblog          = rreq.getWebsite();
        
        List recentComments = new ArrayList();
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            List recent = wmgr.getComments(
                    weblog,
                    null,  // weblog entry
                    null,  // search String
                    null,  // startDate
                    null,  // endDate
                    null,  // pending
                    Boolean.TRUE,  // approved only
                    Boolean.FALSE, // no spam
                    true,          // we want reverse chrono order
                    offset,        // offset
                    length);       // no limit
            
            // wrap pojos
            recentComments = new ArrayList(recent.size());
            Iterator it = recent.iterator();
            while(it.hasNext()) {
                recentComments.add(CommentDataWrapper.wrap((CommentData) it.next()));
            }
        } catch (RollerException e) {
            logger.error("ERROR: getting comments", e);
        }
        return recentComments;
    }
    
    /**
     * Get comment form to be displayed or null if not on single-entry page.
     */
    public CommentFormEx getCommentForm() {
        CommentFormEx commentForm =
            (CommentFormEx) request.getAttribute("commentForm");
        if (commentForm == null) {
            commentForm = new CommentFormEx();            
            // Set fields to spaces to please Velocity
            commentForm.setName("");
            commentForm.setEmail("");
            commentForm.setUrl("");
            commentForm.setContent("");
        }
        return commentForm;
    }      
    
    /**
     * Get preview comment or null if none exists.
     */
    public CommentDataWrapper getCommentPreview() {
        CommentDataWrapper commentWrapper = null;
        try {
            if (request.getAttribute("previewComments") != null) {
                ArrayList list = new ArrayList();
                CommentData comment = new CommentData();
                CommentFormEx commentForm = getCommentForm();
                commentForm.copyTo(comment, request.getLocale());
                commentWrapper = CommentDataWrapper.wrap(comment);
            }        
        } catch (RollerException e) {
            logger.warn("ERROR: creating comment form", e);
        }
        return commentWrapper;
    }
    
    public boolean isUserAuthorizedToAdmin() {
        try {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rses = RollerSession.getRollerSession(request);
            if (rses.getAuthenticatedUser() != null
                    && rreq.getWebsite() != null) {
                return rses.isUserAuthorizedToAdmin(rreq.getWebsite());
            }
        } catch (RollerException e) {
           logger.warn("ERROR: checking user authorization", e);
        }
        return false;
    }

    public boolean isUserAuthenticated() {
        return (request.getUserPrincipal() != null);
    }
    
     /** Pull the last WeblogEntryData out of the Map. */
    private void setFirstAndLastEntries(Map days) {
        int numDays = days.keySet().size();
        if (numDays > 0) // there is at least one day
        {
            // get first entry in map
            Object[] keys = days.keySet().toArray(new Object[numDays]);
            List vals = (List)days.get( keys[0] );
            int valSize = vals.size();
            if (valSize > 0) {
                firstEntry = (WeblogEntryDataWrapper)vals.get(0);
            }
            
            // get last entry in map
            vals = (List)days.get( keys[--numDays] );
            valSize = vals.size();
            if (valSize > 0) {
                lastEntry = (WeblogEntryDataWrapper)vals.get(--valSize);
            }
        }
    }
}


