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

package org.apache.roller.weblogger.ui.struts2.admin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.ui.struts2.pagers.CommentsPager;
import org.apache.roller.weblogger.ui.struts2.util.KeyValueObject;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.URLUtilities;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Action for managing global set of comments.
 */
public class GlobalCommentManagement extends UIAction {
    
    private static Log log = LogFactory.getLog(GlobalCommentManagement.class);
    
    // number of comments to show per page
    private static final int COUNT = 2;
    
    // bean for managing submitted data
    private GlobalCommentManagementBean bean = new GlobalCommentManagementBean();
    
    // pager for the comments we are viewing
    private CommentsPager pager = null;
    
    // first comment in the list
    private WeblogEntryComment firstComment = null;
    
    // last comment in the list
    private WeblogEntryComment lastComment = null;
    
    // indicates number of comments that would be deleted by bulk removal
    // a non-zero value here indicates bulk removal is a valid option
    private int bulkDeleteCount = 0;
    
    
    public GlobalCommentManagement() {
        this.actionName = "globalCommentManagement";
        this.desiredMenu = "admin";
        this.pageTitle = "commentManagement.title";
    }
    
    
    // admin role required
    public String requiredUserRole() {
        return "admin";
    }
    
    // no weblog required
    public boolean isWeblogRequired() {
        return false;
    }
    
    
    public void loadComments() {
        
        List comments = Collections.EMPTY_LIST;
        boolean hasMore = false;
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            comments = wmgr.getComments(
                    null,
                    null,
                    getBean().getSearchString(),
                    getBean().getStartDate(),
                    getBean().getEndDate(),
                    getBean().getStatus(),
                    true, // reverse  chrono order
                    getBean().getPage() * COUNT,
                    COUNT + 1);
            
            if(comments != null && comments.size() > 0) {
                if(comments.size() > COUNT) {
                    comments.remove(comments.size()-1);
                    hasMore = true;
                }
                
                setFirstComment((WeblogEntryComment)comments.get(0));
                setLastComment((WeblogEntryComment)comments.get(comments.size()-1));
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up comments", ex);
            // TODO: i18n
            addError("Error looking up comments");
        }
        
        // build comments pager
        String baseUrl = buildBaseUrl();
        setPager(new CommentsPager(baseUrl, getBean().getPage(), comments, hasMore));
    }
    
    
    // use the action data to build a url representing this action, including query data
    private String buildBaseUrl() {
        
        Map<String, String> params = new HashMap();
        
        SimpleDateFormat dojoFormat = new SimpleDateFormat("yyyy-MM-dd", getLocale());
        SimpleDateFormat stdFormat = new SimpleDateFormat("MM/dd/yy", getLocale());
        
        if(!StringUtils.isEmpty(getBean().getSearchString())) {
            params.put("bean.searchString", getBean().getSearchString());
        }
        if(getBean().getStartDate() != null) {
            params.put("bean.startDate", stdFormat.format(getBean().getStartDate()));
            params.put("dojo.bean.startDate", dojoFormat.format(getBean().getStartDate()));
        }
        if(getBean().getEndDate() != null) {
            params.put("bean.endDate", stdFormat.format(getBean().getEndDate()));
            params.put("dojo.bean.endDate", dojoFormat.format(getBean().getEndDate()));
        }
        if(!StringUtils.isEmpty(getBean().getApprovedString())) {
            params.put("bean.approvedString", getBean().getApprovedString());
        }
        if(!StringUtils.isEmpty(getBean().getSpamString())) {
            params.put("bean.spamString", getBean().getSpamString());
        }
        
        return URLUtilities.getActionURL("globalCommentManagement", "/roller-ui/admin", 
                null, params, false);
    }
    
    
    // show comment management page
    public String execute() {
        
        // load list of comments from query
        loadComments();
        
        // load bean data using comments list
        getBean().loadCheckboxes(getPager().getItems());
        
        return LIST;
    }
    
    
    /**
     * Query for a specific subset of comments based on various criteria.
     */
    public String query() {
        
        // load list of comments from query
        loadComments();
        
        // load bean data using comments list
        getBean().loadCheckboxes(getPager().getItems());
        
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            List allMatchingComments = wmgr.getComments(
                    null,
                    null,
                    getBean().getSearchString(),
                    getBean().getStartDate(),
                    getBean().getEndDate(),
                    getBean().getStatus(),
                    true, // reverse  chrono order
                    0,
                    -1);
            
            if(allMatchingComments.size() > COUNT) {
                setBulkDeleteCount(allMatchingComments.size());
            }
            
        } catch (WebloggerException ex) {
            log.error("Error looking up comments", ex);
            // TODO: i18n
            addError("Error looking up comments");
        }
        
        return LIST;
    }
    
    
    /**
     * Bulk delete all comments matching query criteria.
     */
    public String delete() {
        
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            int deleted = wmgr.removeMatchingComments(
                    null,
                    null,
                    getBean().getSearchString(),
                    getBean().getStartDate(),
                    getBean().getEndDate(),
                    getBean().getStatus());
            
            // TODO: i18n
            addMessage("Successfully deleted "+deleted+" comments");
            
            // reset form and load fresh comments list
            setBean(new GlobalCommentManagementBean());
            
            return execute();
            
        } catch (WebloggerException ex) {
            log.error("Error doing bulk delete", ex);
            // TODO: i18n
            addError("Bulk delete failed due to unexpected error");
        }
        
        return LIST;
    }
    
    
    /**
     * Update a list of comments.
     */
    public String update() {
        
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            
            List flushList = new ArrayList();
            
            // delete all comments with delete box checked
            List<String> deletes = Arrays.asList(getBean().getDeleteComments());
            if(deletes != null && deletes.size() > 0) {
                log.debug("Processing deletes - "+deletes.size());
                
                WeblogEntryComment deleteComment = null;
                for(String deleteId : deletes) {
                    deleteComment = wmgr.getComment(deleteId);
                    wmgr.removeComment(deleteComment);
                    flushList.add(deleteComment);
                }
            }
            
            // loop through IDs of all comments displayed on page
            List spamIds = Arrays.asList(getBean().getSpamComments());
            log.debug(spamIds.size()+" comments marked as spam");
            
            String[] ids = Utilities.stringToStringArray(getBean().getIds(),",");
            for (int i=0; i < ids.length; i++) {
                log.debug("processing id - "+ ids[i]);
                
                // if we already deleted it then skip forward
                if(deletes.contains(ids[i])) {
                    log.debug("Already deleted, skipping - "+ids[i]);
                    continue;
                }
                
                WeblogEntryComment comment = wmgr.getComment(ids[i]);
                
                // mark/unmark spam
                if (spamIds.contains(ids[i]) && 
                        !WeblogEntryComment.SPAM.equals(comment.getStatus())) {
                    log.debug("Marking as spam - "+comment.getId());
                    comment.setStatus(WeblogEntryComment.SPAM);
                    wmgr.saveComment(comment);
                    
                    flushList.add(comment);
                } else if(WeblogEntryComment.SPAM.equals(comment.getStatus())) {
                    log.debug("Marking as approved - "+comment.getId());
                    comment.setStatus(WeblogEntryComment.APPROVED);
                    wmgr.saveComment(comment);
                    
                    flushList.add(comment);
                }
            }
            
            RollerFactory.getRoller().flush();
            
            // notify caches of changes
            for (Iterator comments=flushList.iterator(); comments.hasNext();) {
                CacheManager.invalidate((WeblogEntryComment)comments.next());
            }
            
            addMessage("commentManagement.updateSuccess");
            
            // reset form and load fresh comments list
            setBean(new GlobalCommentManagementBean());
            
            return execute();
            
        } catch (Exception ex) {
            log.error("ERROR updating comments", ex);
            addError("commentManagement.updateError", ex.toString());
        }
        
        return LIST;
    }
    
    
    public List getCommentStatusOptions() {
        
        List opts = new ArrayList();
        
        opts.add(new KeyValueObject("ALL", getText("commentManagement.all")));
        opts.add(new KeyValueObject("ONLY_PENDING", getText("commentManagement.onlyPending")));
        opts.add(new KeyValueObject("ONLY_APPROVED", getText("commentManagement.onlyApproved")));
        opts.add(new KeyValueObject("ONLY_DISAPPROVED", getText("commentManagement.onlyDisapproved")));
        
        return opts;
    }
    
    public List getSpamStatusOptions() {
        
        List opts = new ArrayList();
        
        opts.add(new KeyValueObject("ALL", getText("commentManagement.all")));
        opts.add(new KeyValueObject("NO_SPAM", getText("commentManagement.noSpam")));
        opts.add(new KeyValueObject("ONLY_SPAM", getText("commentManagement.onlySpam")));
        
        return opts;
    }
    
    
    public GlobalCommentManagementBean getBean() {
        return bean;
    }

    public void setBean(GlobalCommentManagementBean bean) {
        this.bean = bean;
    }

    public int getBulkDeleteCount() {
        return bulkDeleteCount;
    }

    public void setBulkDeleteCount(int bulkDeleteCount) {
        this.bulkDeleteCount = bulkDeleteCount;
    }

    public WeblogEntryComment getFirstComment() {
        return firstComment;
    }

    public void setFirstComment(WeblogEntryComment firstComment) {
        this.firstComment = firstComment;
    }

    public WeblogEntryComment getLastComment() {
        return lastComment;
    }

    public void setLastComment(WeblogEntryComment lastComment) {
        this.lastComment = lastComment;
    }

    public CommentsPager getPager() {
        return pager;
    }

    public void setPager(CommentsPager pager) {
        this.pager = pager;
    }
    
}
