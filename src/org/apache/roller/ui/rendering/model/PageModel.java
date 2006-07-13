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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
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
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.CommentDataWrapper;
import org.apache.roller.pojos.wrapper.TemplateWrapper;
import org.apache.roller.pojos.wrapper.WeblogCategoryDataWrapper;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;
import org.apache.roller.ui.authoring.struts.formbeans.CommentFormEx;
import org.apache.roller.ui.rendering.util.WeblogPageRequest;


/**
 * Model provides information needed to render a weblog page. 
 * Includes methods for paging through a collection of entries restricted by category. 
 * The getEntries() method must be called first, before any pager methods will work.
 */
public class PageModel implements Model {
    
    protected static Log           log = LogFactory.getLog(PageModel.class);    
    private HttpServletRequest     request = null;
    private WebsiteData            weblog = null;
    private WeblogEntryData        entry = null;
    private String                 cat = null; 
    private String                 entryAnchor = null;
    private String                 dateString = null;
    private String                 weblogPage = null;
    private String                 locale = null;
    private int                    page = 0;
    private WeblogEntryDataWrapper nextEntry = null;
    private WeblogEntryDataWrapper prevEntry = null;
    private WeblogEntryDataWrapper firstEntry = null;
    private WeblogEntryDataWrapper lastEntry = null;
    
    
    /** 
     * Creates an un-initialized new instance, Roller calls init() to complete
     * construction. 
     */
    public PageModel() {}
    
    
    /** 
     * Template context name to be used for model.
     */
    public String getModelName() {
        return "model";
    }
    
    
    /** 
     * Init page model based on request. 
     */
    public void init(Map initData) throws RollerException {
        
        HttpServletRequest request = (HttpServletRequest) initData.get("request");
        this.request = request;
        
        // we expect the init data to contain a pageRequest object
        WeblogPageRequest parsed = (WeblogPageRequest) initData.get("pageRequest");
        if(parsed == null) {
            throw new RollerException("expected pageRequest from init data");
        }
        
        cat = parsed.getWeblogCategoryName();
        entryAnchor = parsed.getWeblogAnchor();
        dateString = parsed.getWeblogDate();
        weblogPage = parsed.getWeblogPageName();
        locale = parsed.getLocale();
        page = parsed.getPageNum();
        
        // lookup weblog object
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
     * Get weblog entry being displayed or null if none specified by request.
     */
    public WeblogEntryDataWrapper getWeblogEntry() {      
        WeblogEntryDataWrapper ret = null;
        if (entryAnchor != null) {
            Roller roller = RollerFactory.getRoller();
            try {
                WeblogManager wmgr = roller.getWeblogManager();
                WeblogEntryData entry = wmgr.getWeblogEntryByAnchor(weblog, entryAnchor);
                ret = WeblogEntryDataWrapper.wrap(entry);
            } catch (RollerException e) {
                log.error("ERROR: getting weblog entry");
            }
        }        
        return ret;
    }
    
    
    /**
     * Get weblog entry being displayed or null if none specified by request.
     */
    public TemplateWrapper getWeblogPage() {       
        TemplateWrapper ret = null;
        try {
            if (weblogPage != null) {
                Roller roller = RollerFactory.getRoller();
                UserManager umgr = roller.getUserManager();
                WeblogTemplate template = umgr.getPageByName(weblog, weblogPage);
                ret = TemplateWrapper.wrap(template);

            } else {
                ret = TemplateWrapper.wrap(weblog.getDefaultPage());
            }  
        } catch (RollerException e) {
            log.error("ERROR: getting page template");
        }
        return ret;
    }
    
    
    /**
     * Is this page considered a permalink?
     */
    public boolean isPermalink() {
        return entryAnchor != null;
    }
    
    
    /**
     * A map of entries representing this page. The collection is grouped by 
     * days of entries.  Each value is a list of entry objects keyed by the 
     * date they were published.
     * @param catArgument Category restriction (null or "nil" for no restriction)
     */
    public WeblogEntriesPager getWeblogEntriesPager(String catArgument) {        
        // category specified by argument wins over request parameter
        String chosenCat = (catArgument != null) ? catArgument : cat;            
        return new WeblogEntriesPagerImpl(weblog, dateString, entryAnchor, chosenCat, locale, page);
    }
    
    
    /**
     * A map of entries representing this page. The collection is grouped by 
     * days of entries.  Each value is a list of entry objects keyed by the 
     * date they were published.
     */
    public WeblogEntriesPager getWeblogEntriesPager() {
        return getWeblogEntriesPager(null);
    }
    
    
    /**
     * Get weblog category specified by request, or null if the category path
     * found in the request does not exist in the current weblog.
     */
    public WeblogCategoryDataWrapper getWeblogCategory() {
        WeblogCategoryDataWrapper ret = null;
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            WeblogCategoryData category = wmgr.getWeblogCategoryByPath(weblog, cat);
            if (category != null) {
                ret = WeblogCategoryDataWrapper.wrap(category);
            }
        } catch (Exception e) {
            log.error("ERROR: fetching category");
        }
        return ret;
    }
    
    
    /**
     * Get up to 100 most recent published entries in weblog.
     * @param cat Category path or null for no category restriction
     * @param length Max entries to return (1-100)
     * @return List of WeblogEntryDataWrapper objects.
     */
    public List getRecentWeblogEntries(String cat, int length) {  
        if (cat != null && "nil".equals(cat)) cat = null;
        if (length > 100) length = 100;
        List recentEntries = new ArrayList();
        if (length < 1) return recentEntries;
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            List recent = wmgr.getWeblogEntries(
                    weblog, 
                    null,       // user
                    null,       // startDate
                    new Date(), // endDate
                    cat,        // cat or null
                    WeblogEntryData.PUBLISHED, 
                    "pubTime",  // sortby
                    null, 
                    0,
                    length); 
            
            // wrap pojos
            recentEntries = new ArrayList(recent.size());
            Iterator it = recent.iterator();
            while(it.hasNext()) {
                recentEntries.add(WeblogEntryDataWrapper.wrap((WeblogEntryData) it.next()));
            }
        } catch (RollerException e) {
            log.error("ERROR: getting comments", e);
        }
        return recentEntries;
    }
    
    
    /**
     * Get up to 100 most recent approved and non-spam comments in weblog.
     * @param length Max entries to return (1-100)
     * @return List of CommentDataWrapper objects.
     */
    public List getRecentComments(int length) {   
        if (length > 100) length = 100;
        List recentComments = new ArrayList();
        if (length < 1) return recentComments;
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
                    0,             // offset
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
     * Get comment form to be displayed, may contain preview data. 
     * @return Comment form object or null if not on a comment page.
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
}


