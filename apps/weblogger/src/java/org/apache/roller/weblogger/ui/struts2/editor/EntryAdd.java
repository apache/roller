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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.util.MediacastException;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.MailUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.util.MediacastResource;
import org.apache.roller.weblogger.util.MediacastUtil;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Add a new weblog entry.
 */
public final class EntryAdd extends EntryBase {
    
    private static Log log = LogFactory.getLog(EntryAdd.class);
    
    // bean for managing form data
    private EntryBean bean = new EntryBean();
    
    
    public EntryAdd() {
        this.actionName = "entryAdd";
        this.desiredMenu = "editor";
        this.pageTitle = "weblogEdit.title.newEntry";
    }
    
    
    @Override
    public short requiredWeblogPermissions() {
        return WeblogPermission.LIMITED;
    }
    
    
    /**
     * Show form for adding a new weblog entry.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
        
        // if user is an author then post status defaults to PUBLISHED, otherwise PENDING
        if(getActionWeblog().hasUserPermissions(getAuthenticatedUser(),WeblogPermission.AUTHOR)) {
            getBean().setStatus(WeblogEntry.PUBLISHED);
        } else {
            getBean().setStatus(WeblogEntry.PENDING);
        }
        
        // set entry locale based on weblog locale
        getBean().setLocale(getActionWeblog().getLocale());
        
        // set comment defaults
        getBean().setAllowComments(getActionWeblog().getDefaultAllowComments());
        getBean().setCommentDays(new Integer(getActionWeblog().getDefaultCommentDays()));
        
        // apply weblog default plugins
        if(getActionWeblog().getDefaultPlugins() != null) {
            getBean().setPlugins(StringUtils.split(getActionWeblog().getDefaultPlugins(), ","));
        }
        
        return INPUT;
    }
    
    
    /**
     * Save a new weblog entry.
     * 
     * @return String The result of the action.
     */
    public String save() {
        
        if(!hasActionErrors()) try {
            WeblogManager weblogMgr = RollerFactory.getRoller().getWeblogManager();
            
            WeblogEntry entry = new WeblogEntry();
            entry.setCreator(getAuthenticatedUser());
            entry.setWebsite(getActionWeblog());
            
            // set updatetime & pubtime if it was specified
            entry.setUpdateTime(new Timestamp(new Date().getTime()));
            entry.setPubTime(getBean().getPubTime(getLocale(), getActionWeblog().getTimeZoneInstance()));
            
            // copy data to new entry pojo
            getBean().copyTo(entry);
            
            // handle pubtime auto set
            if(entry.isPublished()) {
                if(entry.getPubTime() == null) {
                    // no time specified, use current time
                    entry.setPubTime(entry.getUpdateTime());
                }
                
                // if user does not have author perms then force PENDING status
                if(!getActionWeblog().hasUserPermissions(getAuthenticatedUser(),WeblogPermission.AUTHOR)) {
                    entry.setStatus(WeblogEntry.PENDING);
                }
            }
            
            // if user is an admin then apply pinned to main value as well
            if(getAuthenticatedUser().hasRole("admin")) {
                entry.setPinnedToMain(getBean().getPinnedToMain());
            }
            
            if(!StringUtils.isEmpty(getBean().getEnclosureURL())) try {
                // Fetch MediaCast resource
                log.debug("Checking MediaCast attributes");
                MediacastResource mediacast = MediacastUtil.lookupResource(getBean().getEnclosureURL());
                
                // set mediacast attributes
                entry.putEntryAttribute("att_mediacast_url", mediacast.getUrl());
                entry.putEntryAttribute("att_mediacast_type", mediacast.getContentType());
                entry.putEntryAttribute("att_mediacast_length", ""+mediacast.getLength());
                
            } catch (MediacastException ex) {
                addMessage(getText(ex.getErrorKey()));
            }
            
            if(log.isDebugEnabled()) {
                log.debug("entry bean is ...\n"+getBean().toString());
                log.debug("final status = "+entry.getStatus());
                log.debug("updtime = "+entry.getUpdateTime());
                log.debug("pubtime = "+entry.getPubTime());
            }
            
            log.debug("Saving entry");
            weblogMgr.saveWeblogEntry(entry);
            RollerFactory.getRoller().flush();
            
            // notify search of the new entry
            reindexEntry(entry);
            
            // notify caches
            CacheManager.invalidate(entry);
            
            // Queue applicable pings for this update.
            if(entry.isPublished()) {
                RollerFactory.getRoller().getAutopingManager().queueApplicableAutoPings(entry);
            }
            
            if(entry.isPending()) {
                // implies that entry just changed to pending
                MailUtil.sendPendingEntryNotice(entry);
                addMessage("weblogEdit.submittedForReview");
            } else {
                addMessage("weblogEdit.changesSaved");
            }
            
            // now that entry is saved we want to store the id in the bean
            // so that our success action can use it properly
            getBean().setId(entry.getId());
            
            return SUCCESS;
            
        } catch (Exception e) {
            log.error("Error saving new entry", e);
            // TODO: i18n
            addError("Error saving new entry");
        }


        
        return INPUT;
    }
    
    
    /**
     * Get the list of all categories for the action weblog, not including root.
     */
    public List<WeblogCategory> getCategories() {
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            return wmgr.getWeblogCategories(getActionWeblog(), false);
        } catch (WebloggerException ex) {
            log.error("Error getting category list for weblog - "+getWeblog(), ex);
            return Collections.EMPTY_LIST;
        }
    }
    
    
    public EntryBean getBean() {
        return bean;
    }

    public void setBean(EntryBean bean) {
        this.bean = bean;
    }
    
}
