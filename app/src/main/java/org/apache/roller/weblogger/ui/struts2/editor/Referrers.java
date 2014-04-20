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

import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.referrers.RefererManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;


/**
 * Manage weblog referrer data.
 */
public class Referrers extends UIAction {
    
    private static Log log = LogFactory.getLog(Referrers.class);
    
    // list of referrers to display
    private List<WeblogReferrer> referrers = Collections.emptyList();

    // referrers hits today
    private int dayHits = 0;

    // ids of referrers to remove
    private String[] removeIds = null;
    
    
    public Referrers() {
        this.actionName = "referrers";
        this.desiredMenu = "editor";
        this.pageTitle = "referers.todaysReferers";
    }
    
    
    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.ADMIN);
    }
    
    
    /**
     * Show list of weblog referrers.
     */
    public String execute() {
        
        RefererManager refmgr = WebloggerFactory.getWeblogger().getRefererManager();
        try {
            setDayHits(refmgr.getDayHits(getActionWeblog()));
            setReferrers(refmgr.getTodaysReferers(getActionWeblog()));
        } catch (Exception ex) {
            log.error("Error getting referrer data for weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error loading referrer data");
        }
        
        return LIST;
    }
    
    
    /**
     * Reset all referrer counts.
     */
    public String reset() {
        
        try {
            RefererManager refmgr = WebloggerFactory.getWeblogger().getRefererManager();
            refmgr.clearReferrers(getActionWeblog());
            WebloggerFactory.getWeblogger().flush();
            
            CacheManager.invalidate(getActionWeblog());
        } catch (Exception ex) {
            log.error("Error resetting referrers", ex);
            // TODO: i18n
            addError("Error resetting referrers");
        }
        
        return execute();
    }
    
    
    /**
     * Remove selected referrers.
     */
    public String remove() {
        
        String[] idsToRemove = getRemoveIds();
        if (idsToRemove != null) {
            RefererManager refmgr = WebloggerFactory.getWeblogger().getRefererManager();
            
            try {
                WeblogReferrer referer;
                for (String idToRemove : idsToRemove) {
                    referer = refmgr.getReferer(idToRemove);
                    
                    // make sure referrer belongs to action weblog
                    if (getActionWeblog().equals(referer.getWebsite())) {
                        refmgr.removeReferer(referer);
                    }
                }
                
                // flush
                WebloggerFactory.getWeblogger().flush();
                
                // notify caches
                CacheManager.invalidate(getActionWeblog());
                
                addMessage("referers.deletedReferers");

            } catch (Exception ex) {
                log.error("Error removing referrers", ex);
                // TODO: i18n
                addError("Error removing referrers");
            }
        } else {
            addError("referers.noReferersSpecified");
        }
        
        return execute();
    }

    
    public List<WeblogReferrer> getReferrers() {
        return referrers;
    }

    public void setReferrers(List<WeblogReferrer> referrers) {
        this.referrers = referrers;
    }

    public int getDayHits() {
        return dayHits;
    }

    public void setDayHits(int dayHits) {
        this.dayHits = dayHits;
    }

    public String[] getRemoveIds() {
        return removeIds;
    }

    public void setRemoveIds(String[] removeIds) {
        this.removeIds = removeIds;
    }
    
}
