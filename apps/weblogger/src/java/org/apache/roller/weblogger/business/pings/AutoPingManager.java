/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business.pings;

import java.util.Collection;
import java.util.List;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Manages autoping storage/retrieval, queries and queue.
 */
public interface AutoPingManager {
    
    
    /**
     * Store an auto ping configuration.
     *
     * @param autoPing the auto ping configuration
     * @throws RollerException
     */
    public void saveAutoPing(AutoPing autoPing) throws RollerException;
    
    
    /**
     * Remove the auto ping configuration with given id.
     *
     * @param autoPing the auto ping configuration to remove
     * @throws RollerException
     */
    public void removeAutoPing(AutoPing autoPing) throws RollerException;
    
    
    /**
     * Remove the auto ping configuration for the given ping target and website, if one exists.  Returns silently if it
     * doesn't exist.
     *
     * @param pingTarget the ping target
     * @param website    the website
     * @throws RollerException
     */
    public void removeAutoPing(PingTarget pingTarget, Weblog website) throws RollerException;
    
    
    /**
     * Remove a collection of auto ping configurations.
     * 
     * @param autopings a <code>Collection</code> of <code>AAutoPing/code> objects
     * @throws RollerException
     */
    public void removeAutoPings(Collection autopings) throws RollerException;
    
    
    /**
     * Remove all auto ping configurations for all websites.
     *
     * @throws RollerException
     */
    public void removeAllAutoPings() throws RollerException;
    
    
    /**
     * Retrieve an auto ping configuration by id.
     *
     * @param id the id of the auto ping configuration to retrieve.
     * @return the auto ping configuration with specified id or null if not found
     * @throws RollerException
     */
    public AutoPing getAutoPing(String id) throws RollerException;
    
    
    /**
     * Get all of the auto ping configurations for the given website.
     * 
     * @param website
     * @return a list of auto ping configurations for the given website as <code>AuAutoPingcode> objects.
     */
    public List getAutoPingsByWebsite(Weblog website) throws RollerException;
    
    
    /**
     * Get all of the auto ping configurations for a given target (across all websites).
     * 
     * @param pingTarget
     * @return a list of auto ping configurations for the given target as <code>AuAutoPingcode> objects.
     */
    public List getAutoPingsByTarget(PingTarget pingTarget) throws RollerException;
    
    
    /**
     * Get the auto ping configurations that should be pinged upon creation/change of the given weblog entry.
     *
     * @param changedWeblogEntry the entry that has been created or changed
     * @return a list of the ping configurations that should be applied due to this change
     */
    public List getApplicableAutoPings(WeblogEntry changedWeblogEntry) throws RollerException;
    
    
    /**
     * Queue the auto ping configurations that should be pinged upon change to the given weblog entry.  This calls the
     * {@link PingQueueManager} to queue ping requests for each ping configuration that should be applied on change to
     * the given weblog entry.  If ping processing is suspended, this returns without doing anything.
     *
     * @param changedWeblogEntry the entry that has been created or changed
     */
    public void queueApplicableAutoPings(WeblogEntry changedWeblogEntry) throws RollerException;
    
    
    /**
     * Get the category restrictions on the given auto ping configuration.
     *
     * @param autoPing
     * @return the category restrictions as a collection of <code>WeblogCategoryData</code> objects.  This collection
     *         will be empty if there are no restrictions (meaning that the auto ping configuration applies to changes
     *         in any category of the website).
     */
    public List getCategoryRestrictions(AutoPing autoPing) throws RollerException;
    
    
    /**
     * Set the category restrictions on the given ping configuration to the specified ones.  If the new collection is
     * empty, all category restrictions are removed.
     *
     * @param autoPing      auto ping configuration to change
     * @param newCategories a collection of <code>WeblogCategoryData</code> objects for the new category restrictions
     */
    public void setCategoryRestrictions(AutoPing autoPing,
            Collection newCategories);
    
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
}
