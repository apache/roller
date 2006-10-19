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

package org.apache.roller.business.pings;

import java.util.List;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.WebsiteData;


/**
 * Manages ping targets.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 */
public interface PingTargetManager {
    
    
    /**
     * Store a ping target.
     *
     * @param pingTarget ping target data object.
     * @throws RollerException
     */
    public void savePingTarget(PingTargetData pingTarget) throws RollerException;
    
    
    /**
     * Remove a ping target.
     *
     * @param id id of the ping target to be removed
     * @throws RollerException
     */
    public void removePingTarget(PingTargetData pingTarget) throws RollerException;
    
    
    /**
     * Remove all custom targets (regardless of website).
     */
    public void removeAllCustomPingTargets() throws RollerException;
    
    
    /**
     * Retrieve a specific ping target by id.
     *
     * @param id id of the ping target to be retrieved.
     * @return the ping target whose id is specified.
     * @throws RollerException
     */
    public PingTargetData getPingTarget(String id) throws RollerException;
    
    
    /**
     * Get a list of the common (shared) ping targets.
     *
     * @return the list of common ping targets as a <code>List</code> of {@link PingTargetData} objects
     * @throws RollerException
     */
    public List getCommonPingTargets() throws RollerException;
    
    
    /**
     * Get a list of the custom ping targets for the given website.
     *
     * @param website the website whose custom targets should be returned.
     * @return the list of custom ping targets for the given website as a <code>List</code> of {@link PingTargetData}
     *         objects
     * @throws RollerException
     */
    public List getCustomPingTargets(WebsiteData website) throws RollerException;
    
    
    /**
     * Check if the ping target has a name that is unique in the appropriate set.  If the ping target has no website id
     * (is common), then this checks if the name is unique amongst common targets, and if custom then unique amongst
     * custom targets. If the target has a non-null id, then it is allowed to have the same name as tha existing stored
     * target with the same id.
     *
     * @param pingTarget
     * @return true if the name is unique in the appropriate set (custom or common) ping targets.
     * @throws RollerException
     */
    public boolean isNameUnique(PingTargetData pingTarget) throws RollerException;
    
    
    /**
     * Check if the url of the ping target is well-formed.  For this test, it must parse as a <code>java.net.URL</code>,
     * with protocol <code>http</code> and a non-empty <code>host</code> portion.
     *
     * @param pingTarget
     * @return true if the <code>pingUrl</code> property of the ping target is a well-formed url.
     * @throws RollerException
     */
    public boolean isUrlWellFormed(PingTargetData pingTarget) throws RollerException;
    
    
    /**
     * Check if the host portion of the url of the ping target is known, meaning it is either a well-formed IP address
     * or a hostname that resolves from the server.  The ping target url must parse as a <code>java.net.URL</code> in
     * order for the hostname to be extracted for this test.  This will return false if that parsing fails.
     *
     * @param pingTarget
     * @return true if the <code>pingUrl</code> (is well-formed and) the <code>host</code> portion of the url of the
     *         ping target is a valid IP address or a hostname that can be resolved on the server.
     * @throws RollerException
     */
    public boolean isHostnameKnown(PingTargetData pingTarget) throws RollerException;
    
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
}
