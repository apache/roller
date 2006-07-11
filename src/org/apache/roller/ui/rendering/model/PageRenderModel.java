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
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.CommentDataWrapper;
import org.apache.roller.pojos.wrapper.WeblogCategoryDataWrapper;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;
import org.apache.roller.ui.authoring.struts.formbeans.CommentFormEx;
import org.apache.roller.ui.rendering.util.WeblogEntriesCollectionPager;
import org.apache.roller.ui.rendering.util.WeblogEntriesPager;
import org.apache.roller.ui.rendering.util.WeblogEntriesPermalinkPager;
import org.apache.roller.ui.rendering.util.WeblogPageRequest;


/**
 * Model provides information needed to render a weblog page.
 */
public class PageRenderModel implements Model {
    
    protected static Log log = LogFactory.getLog(PageRenderModel.class);
    
    private HttpServletRequest     request = null;
    private WebsiteData            weblog = null;
    private int                    offset = 0;
    private String                 categoryPath = null;
    private String                 entryAnchor = null;
    private String                 dateString = null;
    private int page = 0;
    private WeblogEntryDataWrapper nextEntry = null;
    private WeblogEntryDataWrapper prevEntry = null;
    private WeblogEntryDataWrapper firstEntry = null;
    private WeblogEntryDataWrapper lastEntry = null;
    
    private WeblogEntriesPager pager = null;
    
    
    /** 
     * Creates an un-initialized new instance, Roller calls init() to complete
     * construction. 
     */
    public PageRenderModel() {}
    
    
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
        
        categoryPath = parsed.getWeblogCategory();
        entryAnchor = parsed.getWeblogAnchor();
        dateString = parsed.getWeblogDate();
        page = parsed.getPageNum();
        
        // lookup weblog object
        Roller roller = RollerFactory.getRoller();
        UserManager umgr = roller.getUserManager();
        weblog = umgr.getWebsiteByHandle(parsed.getWeblogHandle(), Boolean.TRUE);
        
        // get the entry pager which represents this page
        if (entryAnchor != null) {
            this.pager = new WeblogEntriesPermalinkPager(weblog, entryAnchor);
        } else {
            this.pager = new WeblogEntriesCollectionPager(weblog, dateString, categoryPath, page);
        }
    }
    
    
    /**
     * Get weblog being displayed.
     */
    public WebsiteDataWrapper getWeblog() {
        return WebsiteDataWrapper.wrap(weblog);
    }
    
    
    /**
     * Is this page considered a permalink?
     */
    public boolean isPermalink() {
        return false;
    }
    
    
    /**
     * A map of entries representing this page.
     *
     * The collection is grouped by days of entries.  Each value is a list of
     * entry objects keyed by the date they were published.
     */
    public Map getEntries() {
        return this.pager.getEntries();
    }
    
    
    /**
     * Link value for next collection view
     */
    public String getNextLink() {
        return this.pager.getNextLink();
    }
    
    /**
     * Link name for next collection view
     */
    public String getNextLinkName() {
        return this.pager.getNextLinkName();
    }
    
    /**
     * Link value for prev collection view
     */
    public String getPrevLink() {
        return this.pager.getPrevLink();
    }
    
    /**
     * Link name for prev collection view
     */
    public String getPrevLinkName() {
        return this.pager.getPrevLinkName();
    }
    
    /**
     * Does this pager represent a multi-page collection?
     */
    public boolean isMultiPage() {
        return this.pager.isMultiPage();
    }
    
    /**
     * Link value for next page in current collection view
     */
    public String getNextPageLink() {
        return this.pager.getNextPageLink();
    }
    
    /**
     * Link name for next page in current collection view
     */
    public String getNextPageName() {
        return this.pager.getNextPageName();
    }
    
    /**
     * Link value for prev page in current collection view
     */
    public String getPrevPageLink() {
        return this.pager.getPrevPageLink();
    }
    
    /**
     * Link value for prev page in current collection view
     */
    public String getPrevPageName() {
        return this.pager.getPrevPageName();
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
                    cat,        // categoryPath or null
                    WeblogEntryData.PUBLISHED, 
                    "pubTime",  // sortby
                    0,          // offset
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


