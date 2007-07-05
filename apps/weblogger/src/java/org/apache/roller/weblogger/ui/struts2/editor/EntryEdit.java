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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.MailUtil;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.RollerMessages.RollerMessage;
import org.apache.roller.weblogger.util.Trackback;
import org.apache.roller.weblogger.util.TrackbackNotAllowedException;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Edit an existing entry.
 */
public final class EntryEdit extends EntryBase {
    
    private static Log log = LogFactory.getLog(EntryEdit.class);
    
    // bean for managing form data
    private EntryBean bean = new EntryBean();
    
    // the entry we are editing
    private WeblogEntry entry = null;
    
    // url to send trackback to
    private String trackbackUrl = null;
    
    
    public EntryEdit() {
        this.actionName = "entryEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "weblogEdit.title.editEntry";
    }
    
    
    @Override
    public short requiredWeblogPermissions() {
        return WeblogPermission.LIMITED;
    }
    
    
    public void myPrepare() {
        if(getBean().getId() != null) {
            try {
                WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
                setEntry(wmgr.getWeblogEntry(getBean().getId()));
            } catch (WebloggerException ex) {
                log.error("Error looking up entry by id - "+getBean().getId(), ex);
            }
        }
    }
    
    
    /**
     * Show form for editing an existing entry.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
        
        // make sure we have an entry to edit and it belongs to the action weblog
        if(getEntry() == null) {
            return ERROR;
        } else if(!getEntry().getWebsite().equals(getActionWeblog())) {
            return DENIED;
        }
        
        // load bean with pojo data
        getBean().copyFrom(getEntry(), getLocale());
        
        return INPUT;
    }
    
    
    /**
     * Save weblog entry.
     * 
     * @return String The result of the action.
     */
    public String save() {
        
        // make sure we have an entry to edit and it belongs to the action weblog
        if(getEntry() == null) {
            return ERROR;
        } else if(!getEntry().getWebsite().equals(getActionWeblog())) {
            return DENIED;
        }
        
        if(!hasActionErrors()) try {
            WeblogManager weblogMgr = WebloggerFactory.getWeblogger().getWeblogManager();
            
            WeblogEntry entry = getEntry();
            
            // set updatetime & pubtime
            entry.setUpdateTime(new Timestamp(new Date().getTime()));
            entry.setPubTime(getBean().getPubTime(getLocale(), getActionWeblog().getTimeZoneInstance()));
            
            // copy data to pojo
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
            
//            // Fetch MediaCast content type and length
//            log.debug("Checking MediaCast attributes");
//            if (!checkMediaCast(entry)) {
//                log.debug("Invalid MediaCast attributes");
//            } else {
//                log.debug("Validated MediaCast attributes");
//            }
            
            if(log.isDebugEnabled()) {
                log.debug("entry bean is ...\n"+getBean().toString());
                log.debug("final status = "+entry.getStatus());
                log.debug("updtime = "+entry.getUpdateTime());
                log.debug("pubtime = "+entry.getPubTime());
            }
            
            log.debug("Saving entry");
            weblogMgr.saveWeblogEntry(entry);
            WebloggerFactory.getWeblogger().flush();
            
            // notify search of the new entry
            reindexEntry(entry);
            
            // notify caches
            CacheManager.invalidate(entry);
            
            // Queue applicable pings for this update.
            if(entry.isPublished()) {
                WebloggerFactory.getWeblogger().getAutopingManager().queueApplicableAutoPings(entry);
            }
            
            if(entry.isPending()) {
                // implies that entry just changed to pending
                MailUtil.sendPendingEntryNotice(entry);
                addMessage("weblogEdit.submittedForReview");
            } else {
                addMessage("weblogEdit.changesSaved");
            }
            
            return INPUT;
            
        } catch (Exception e) {
            log.error("Error saving new entry", e);
            // TODO: i18n
            addError("Error saving new entry");
        }


        
        return INPUT;
    }
    
    
    /**
     * Send trackback to a specific url.
     */
    @SkipValidation
    public String trackback() {
        
        // make sure we have an entry to edit and it belongs to the action weblog
        if(getEntry() == null) {
            return ERROR;
        } else if(!getEntry().getWebsite().equals(getActionWeblog())) {
            return DENIED;
        }
        
        if(!StringUtils.isEmpty(getTrackbackUrl())) {
            RollerMessages results = null;
            try {
                Trackback trackback = new Trackback(getEntry(), getTrackbackUrl());
                results = trackback.send();
            } catch(TrackbackNotAllowedException ex) {
                addError("error.trackbackNotAllowed");
            } catch(Throwable t) {
                log.error("Error sending trackback", t);
                // TODO: error handling
                addError("error.general", t.getMessage());
            }
            
            if(results != null) {
                for (Iterator mit = results.getMessages(); mit.hasNext();) {
                    RollerMessage msg = (RollerMessage) mit.next();
                    if(msg.getArgs() == null) {
                        addMessage(msg.getKey());
                    } else {
                        addMessage(msg.getKey(), Arrays.asList(msg.getArgs()));
                    }
                }
                
                for (Iterator eit = results.getErrors(); eit.hasNext();) {
                    RollerMessage err = (RollerMessage) eit.next();
                    if(err.getArgs() == null) {
                        addError(err.getKey());
                    } else {
                        addError(err.getKey(), Arrays.asList(err.getArgs()));
                    }
                }
            }

            // reset trackback url
            setTrackbackUrl(null);
            
        }
        
        return INPUT;
    }
    
    
    /**
     * Get the list of all categories for the action weblog, not including root.
     */
    public List<WeblogCategory> getCategories() {
        try {
            WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
            return wmgr.getWeblogCategories(getActionWeblog(), false);
        } catch (WebloggerException ex) {
            log.error("Error getting category list for weblog - "+getWeblog(), ex);
            return Collections.EMPTY_LIST;
        }
    }
    
    public String getPreviewURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getPreviewURLStrategy(null).getWeblogEntryURL(getActionWeblog(), null, getEntry().getAnchor(), true);
    }
    
    
    public EntryBean getBean() {
        return bean;
    }

    public void setBean(EntryBean bean) {
        this.bean = bean;
    }

    public WeblogEntry getEntry() {
        return entry;
    }

    public void setEntry(WeblogEntry entry) {
        this.entry = entry;
    }

    public String getTrackbackUrl() {
        return trackbackUrl;
    }

    public void setTrackbackUrl(String trackbackUrl) {
        this.trackbackUrl = trackbackUrl;
    }
    
}
