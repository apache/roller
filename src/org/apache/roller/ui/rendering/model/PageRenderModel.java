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

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
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
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.util.DateUtil;

/**
 * Model provide information needed to render a weblog page.
 */
public class PageRenderModel implements RenderModel {
    private HttpServletRequest     request = null;
    private WebsiteData            weblog = null;
    private int                    offset = 0;
    private String                 categoryPath = null;
    private String                 entryAnchor = null;
    private String                 dateString = null;
    private WeblogEntryDataWrapper nextEntry = null;
    private WeblogEntryDataWrapper prevEntry = null;
    private WeblogEntryDataWrapper firstEntry = null;
    private WeblogEntryDataWrapper lastEntry = null;
    
    protected static Log log =
            LogFactory.getFactory().getInstance(PageRenderModel.class);
    
    /** Creates a new instance of AtlasWeblogPageModel */
    public PageRenderModel() {
    }
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "model";
    }
    
    /** Init page model based on request */
    public void init(Map map) throws RollerException {
        HttpServletRequest request = (HttpServletRequest)map.get("request");
        WeblogPageRequest parsed = new WeblogPageRequest(request);
        categoryPath = parsed.getWeblogCategory();
        entryAnchor = parsed.getWeblogAnchor();
        dateString = parsed.getWeblogDate();
        Roller roller = RollerFactory.getRoller();
        UserManager umgr = roller.getUserManager();
        weblog = umgr.getWebsiteByHandle(parsed.getWeblogHandle(), Boolean.TRUE);
    }
    
    /**
     * Get weblog being displayed.
     */
    public WebsiteDataWrapper getWeblog() {
        return WebsiteDataWrapper.wrap(weblog);
    }
    
    /**
     * Get weblog entry to be displayed; null if not on single-entry page or if entry not published.
     */
    public WeblogEntryDataWrapper getWeblogEntry() {
       WeblogEntryDataWrapper ret = null;
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            WeblogEntryData entry = 
                 wmgr.getWeblogEntryByAnchor(weblog, entryAnchor);
            if (entry != null && entry.getStatus().equals(WeblogEntryData.PUBLISHED)) {
                ret = WeblogEntryDataWrapper.wrap(entry);
            }

        } catch (Exception e) {
            log.error("ERROR: fetching entry");
        }
        return ret;
    }
    
    /**
     * Get weblog category or null if request does not specify one.
     */
    public WeblogCategoryDataWrapper getWeblogCategory() {
        WeblogCategoryDataWrapper ret = null;
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            WeblogCategoryData cat = wmgr.getWeblogCategoryByPath(
                    weblog, categoryPath);
            if (cat != null) {
                ret = WeblogCategoryDataWrapper.wrap(cat);
            }
        } catch (Exception e) {
            log.error("ERROR: fetching category");
        }
        return ret;
    }
    
    /**
     * Get weblog entries as specified by the date and category specified or 
     * implied by the URL.  
     * @param cat Category restriction or null for all categories
     * @return List of WeblogEntryDataWrapper objects.
     */
    public List getWeblogEntries(String cat, int offset, int length) {        
        if (cat != null && "nil".equals(cat)) cat = null;
        List ret = new ArrayList();
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            Date date = parseDate(dateString);
            
            boolean isDaySpecified = false;
            boolean isMonthSpecified = false;
            if (dateString != null && dateString.length() == 8) {
                isDaySpecified = true;
            }  
            else if (dateString != null && dateString.length() == 6) {
                isMonthSpecified = true;
            }
            
            String chosenCatPath = cat != null ? cat : categoryPath;
            if (chosenCatPath == null) {
                // no category specifed so use default
                chosenCatPath = weblog.getDefaultCategory().getPath();
                chosenCatPath = chosenCatPath.equals("/") ? null : chosenCatPath;
            }
            
            // if weblog is specified, use its timezone
            Calendar cal = null;
            if (weblog != null) {
                TimeZone tz = weblog.getTimeZoneInstance();
                cal = Calendar.getInstance(tz);
            } else {
                cal = Calendar.getInstance();
            }
            
            Date startDate = null;
            Date endDate = date;
            if (endDate == null) endDate = new Date(); 
            if (isDaySpecified) {
                // URL specified a specific day so get all entries for it
                startDate = DateUtil.getStartOfDay(endDate, cal);
                endDate = DateUtil.getEndOfDay(endDate, cal);
            } else if (isMonthSpecified) {
                // URL specified a specific month so get all entries for it
                startDate = DateUtil.getStartOfMonth(endDate, cal);
                endDate = DateUtil.getEndOfMonth(endDate, cal);
            }
            List entries = wmgr.getWeblogEntries(
                    weblog,        // weblog
                    null,          // user
                    startDate,     // startDate
                    endDate,       // endDate
                    chosenCatPath, // catName
                    WeblogEntryData.PUBLISHED, // status
                    null,          // sortby (null for pubTime)
                    offset,        // offset into results
                    length); // max results to return
            
            // wrap pojos
            ret = new ArrayList(entries.size());
            Iterator it = entries.iterator();
            int i=0;
            while(it.hasNext()) {
                ret.add(i, WeblogEntryDataWrapper.wrap((WeblogEntryData) it.next()));
                i++;
            }
            setFirstAndLastEntries( ret );
                        
        } catch (Exception e) {
            log.error("ERROR: getting entry list", e);
        }
        return ret;
    }
    
    /**
     * Get most recent weblog entries for day or month specified by request.
     * @return Map of Lists of weblog entry objects, keyed by 8-char date strings.
     */
    public Map getWeblogEntriesMonthMap(String cat, int offset, int length) {
        if (cat != null && "nil".equals(cat)) cat = null;
        Map ret = new HashMap();
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            Date date = parseDate(dateString);
            
            boolean isDaySpecified = false;
            boolean isMonthSpecified = false;
            if (dateString != null && dateString.length() == 8) {
                isDaySpecified = true;
            }  
            else if (dateString != null && dateString.length() == 6) {
                isMonthSpecified = true;
            }
            
            String chosenCatPath = cat != null ? cat : categoryPath;
            if (chosenCatPath == null) {
                // no category specifed so use default
                chosenCatPath = weblog.getDefaultCategory().getPath();
                chosenCatPath = chosenCatPath.equals("/") ? null : chosenCatPath;
            }
            
            // if weblog is specified, use its timezone
            Calendar cal = null;
            if (weblog != null) {
                TimeZone tz = weblog.getTimeZoneInstance();
                cal = Calendar.getInstance(tz);
            } else {
                cal = Calendar.getInstance();
            }
            
            Date startDate = null;
            Date endDate = date;
            if (endDate == null) endDate = new Date();
            if (isDaySpecified) {
                // URL specified a specific day so get all entries for it
                startDate = DateUtil.getStartOfDay(endDate, cal);
                endDate = DateUtil.getEndOfDay(endDate, cal);
            } else if (isMonthSpecified) {
                // URL specified a specific month so get all entries for it
                startDate = DateUtil.getStartOfMonth(endDate, cal);
                endDate = DateUtil.getEndOfMonth(endDate, cal);
            }
            Map mmap = RollerFactory.getRoller().getWeblogManager().getWeblogEntryObjectMap(
                    weblog,
                    startDate,
                    endDate,
                    chosenCatPath,
                    WeblogEntryData.PUBLISHED, 
                    offset,  
                    length);
            
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
            log.error("ERROR: getting entry month map", e);
        }
        return ret;
    }
    
    /**
     * Get weblog entry to be displayed or null if not on single-entry page.
     */
    public WeblogEntryDataWrapper getWeblogEntryNext() {
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            WeblogCategoryData category = wmgr.getWeblogCategoryByPath(
                    weblog, categoryPath);
            WeblogEntryDataWrapper currentEntry = getWeblogEntry();
            if (firstEntry != null) currentEntry = firstEntry;
            if (nextEntry == null && currentEntry != null) {
                String catName = null;
                if (category != null) {
                    catName = category.getName();
                }
                WeblogEntryData next =
                        wmgr.getNextEntry(currentEntry.getPojo(), catName);

                if (nextEntry != null)
                    nextEntry = WeblogEntryDataWrapper.wrap(next);

                // make sure that mNextEntry is not published to future
                if (nextEntry != null && nextEntry.getPubTime().after(new Date())) {
                    nextEntry = null;
                }
            }
        } catch (RollerException e) {
            log.error("ERROR: getting next entry", e);
        }
        return nextEntry;
    }
    
    /**
     * Get weblog entry to be displayed or null if not on single-entry page.
     */
    public WeblogEntryDataWrapper getWeblogEntryPrev() {
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            WeblogCategoryData category = wmgr.getWeblogCategoryByPath(
                    weblog, categoryPath);
            WeblogEntryDataWrapper currentEntry = getWeblogEntry();
            if (lastEntry != null) currentEntry = lastEntry;
            if (prevEntry == null && currentEntry != null ) {
                String catName = null;
                if (category != null) {
                    catName = category.getName();
                }
                WeblogEntryData prev =
                        wmgr.getPreviousEntry(currentEntry.getPojo(), catName);
                if(prev != null) {
                    prevEntry = WeblogEntryDataWrapper.wrap(prev);
                }
            }
        } catch (RollerException e) {
            log.error("ERROR: getting next entry", e);
        }
        return prevEntry;
    }
    
    /**
     * Get most recent approved and non-spam comments in weblog.
     * @return List of CommentDataWrapper objects.
     */
    public List getComments(int offset, int length) {        
        List recentComments = new ArrayList();
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            List recent = wmgr.getComments(
                    weblog,
                    null,          // weblog entry
                    null,          // search String
                    null,          // startDate
                    null,          // endDate
                    null,          // pending
                    Boolean.TRUE,  // approved only
                    Boolean.FALSE, // no spam
                    true,          // we want reverse chrono order
                    offset,        // offset
                    length);       // length
            
            // wrap pojos
            recentComments = new ArrayList(recent.size());
            Iterator it = recent.iterator();
            while(it.hasNext()) {
                recentComments.add(CommentDataWrapper.wrap((CommentData) it.next()));
            }
        } catch (RollerException e) {
            log.error("ERROR: getting comments", e);
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
            log.warn("ERROR: creating comment form", e);
        }
        return commentWrapper;
    }
    
    public boolean isUserAuthorizedToAdmin() {
        try {
            RollerSession rses = RollerSession.getRollerSession(request);
            if (rses.getAuthenticatedUser() != null) {
                return rses.isUserAuthorizedToAdmin(weblog);
            }
        } catch (RollerException e) {
            log.warn("ERROR: checking user authorization", e);
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
    
    /** Pull the last WeblogEntryData out of the List. */
    private void setFirstAndLastEntries(List entries) {
        if (entries.size() > 0) {
            firstEntry = (WeblogEntryDataWrapper)entries.get(0);
            if (entries.size() > 1) {
                lastEntry = (WeblogEntryDataWrapper)entries.get(entries.size() - 1);
            } else {
                lastEntry = (WeblogEntryDataWrapper)entries.get(0);                
            }
        }
    }
    
    private Date parseDate(String dateString) {
        Date ret = null;
        SimpleDateFormat char8DateFormat = DateUtil.get8charDateFormat();
        SimpleDateFormat char6DateFormat = DateUtil.get6charDateFormat();
        if (   dateString!=null
                && dateString.length()==8
                && StringUtils.isNumeric(dateString) ) {
            ParsePosition pos = new ParsePosition(0);
            ret = char8DateFormat.parse( dateString, pos );
            
            // make sure the requested date is not in the future
            Date today = getToday();
            if (ret.after(today)) ret = today;
        }
        if (   dateString!=null
                && dateString.length()==6
                && StringUtils.isNumeric(dateString) ) {
            ParsePosition pos = new ParsePosition(0);
            ret = char6DateFormat.parse( dateString, pos );
            
            // make sure the requested date is not in the future
            Date today = getToday();
            if (ret.after(today)) ret = today;
        }
        return ret;
    }
    
    private Date getToday() {
        Calendar todayCal = Calendar.getInstance();
        todayCal = Calendar.getInstance(
                weblog.getTimeZoneInstance(),weblog.getLocaleInstance());
        todayCal.setTime(new Date());
        return todayCal.getTime();
    }
}


