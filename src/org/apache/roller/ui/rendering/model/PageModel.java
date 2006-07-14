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
    
    private static Log log = LogFactory.getLog(PageModel.class);
    
    private HttpServletRequest request = null;
    private WeblogPageRequest pageRequest = null;
    private WebsiteData weblog = null;
    
    
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
        this.pageRequest = (WeblogPageRequest) initData.get("pageRequest");
        if(this.pageRequest == null) {
            throw new RollerException("expected pageRequest from init data");
        }
        
        // extract weblog object
        weblog = pageRequest.getWeblog();
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
        return (pageRequest.getWeblogAnchor() != null);
    }
    
    
    /**
     * Get weblog entry being displayed or null if none specified by request.
     */
    public WeblogEntryDataWrapper getWeblogEntry() {  
        return WeblogEntryDataWrapper.wrap(pageRequest.getWeblogEntry());
    }
    
    
    /**
     * Get weblog entry being displayed or null if none specified by request.
     */
    public TemplateWrapper getWeblogPage() {
        if(pageRequest.getWeblogPageName() != null) {
            return TemplateWrapper.wrap(pageRequest.getWeblogPage());
        } else {
            try {
                return TemplateWrapper.wrap(weblog.getDefaultPage());
            } catch (RollerException ex) {
                log.error("Error getting default page", ex);
            }
        }
        return null;
    }
    
    
    /**
     * Get weblog category specified by request, or null if the category path
     * found in the request does not exist in the current weblog.
     */
    public WeblogCategoryDataWrapper getWeblogCategory() {
        return WeblogCategoryDataWrapper.wrap(pageRequest.getWeblogCategory());
    }
    
    
    /**
     * A map of entries representing this page. The collection is grouped by 
     * days of entries.  Each value is a list of entry objects keyed by the 
     * date they were published.
     * @param catArgument Category restriction (null or "nil" for no restriction)
     */
    public WeblogEntriesPager getWeblogEntriesPager(String catArgument) {        
        // category specified by argument wins over request parameter
        WeblogCategoryData chosenCat = null;
        if (catArgument != null) {
            try {
                Roller roller = RollerFactory.getRoller();
                WeblogManager wmgr = roller.getWeblogManager();
                chosenCat = wmgr.getWeblogCategoryByPath(weblog, catArgument);
            } catch (Exception ignore) {
                log.debug("Ignoring unknown category restriction");
            }
        }            
        return new WeblogEntriesPagerImpl(  
            weblog, 
            pageRequest.getWeblogPage(), 
            pageRequest.getWeblogEntry(),
            pageRequest.getWeblogDate(), 
            chosenCat != null ? chosenCat : pageRequest.getWeblogCategory(),  
            pageRequest.getLocale(), 
            pageRequest.getPageNum());
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


