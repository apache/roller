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

import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.PingTarget;


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
     * @throws WebloggerException
     */
    void savePingTarget(PingTarget pingTarget) throws WebloggerException;
    
    
    /**
     * Remove a ping target.
     *
     * @throws WebloggerException
     */
    void removePingTarget(PingTarget pingTarget) throws WebloggerException;
    
    
    /**
     * Retrieve a specific ping target by id.
     *
     * @param id id of the ping target to be retrieved.
     * @return the ping target whose id is specified.
     * @throws WebloggerException
     */
    PingTarget getPingTarget(String id) throws WebloggerException;
    
    
    /**
     * Get a list of the common (shared) ping targets.
     * 
     * @return the list of common ping targets as a <code>List</code> of {@link PingTarget objects
     * @throws WebloggerException
     */
    List<PingTarget> getCommonPingTargets() throws WebloggerException;

    /**
     * Check if the ping target name already exists in Roller.
     *
     * @param pingTargetName ping target name to check
     * @return true if there is already a ping target with this name, false otherwise
     * @throws WebloggerException
     */
    boolean targetNameExists(String pingTargetName) throws WebloggerException;
    
    /**
     * Check if the url of a ping target is well-formed.  For this test, it must parse as a <code>java.net.URL</code>,
     * with protocol <code>http</code> and a non-empty <code>host</code> portion.
     *
     * @param pingTargetUrl url to check.
     * @return true if the <code>pingTargetUrl</code> property of the ping target is a well-formed url.
     * @throws WebloggerException
     */
    boolean isUrlWellFormed(String pingTargetUrl) throws WebloggerException;
    
    
    /**
     * Check if the host portion of the url of a ping target is known, meaning it is either a well-formed IP address
     * or a hostname that resolves from the server.  The ping target url must parse as a <code>java.net.URL</code> in
     * order for the hostname to be extracted for this test.  This will return false if that parsing fails.
     *
     * @param pingTargetUrl url to check.
     * @return true if the <code>pingTargetUrl</code> (is well-formed and) the <code>host</code> portion of the url of the
     *         ping target is a valid IP address or a hostname that can be resolved on the server.
     * @throws WebloggerException
     */
    boolean isHostnameKnown(String pingTargetUrl) throws WebloggerException;
    
    
    /**
     * Release all resources associated with Roller session.
     */
    void release();
    
}
