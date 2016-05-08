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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business;

import java.io.IOException;
import java.util.List;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.xmlrpc.XmlRpcException;

/**
 * Manages ping targets.
 */
public interface PingTargetManager {
    
    /**
     * Store a ping target.
     *
     * @param pingTarget ping target data object.
     */
    void savePingTarget(PingTarget pingTarget);
    
    /**
     * Remove a ping target.
     */
    void removePingTarget(PingTarget pingTarget);

    /**
     * Retrieve a specific ping target by id.
     *
     * @param id id of the ping target to be retrieved.
     * @return the ping target whose id is specified.
     */
    PingTarget getPingTarget(String id);
    
    /**
     * Get a list of the common (shared) ping targets.
     *
     * @return the list of common ping targets as a <code>List</code> of {@link PingTarget objects
     */
    List<PingTarget> getCommonPingTargets();

    /**
     * Check if the ping target name already exists in the weblogger instance.
     *
     * @param pingTargetName ping target name to check
     * @return true if there is already a ping target with this name, false otherwise
     */
    boolean targetNameExists(String pingTargetName);
    
    /**
     * Check if the url of a ping target is well-formed.  For this test, it must parse as a <code>java.net.URL</code>,
     * with protocol <code>http</code> and a non-empty <code>host</code> portion.
     *
     * @param pingTargetUrl url to check.
     * @return true if the <code>pingTargetUrl</code> property of the ping target is a well-formed url.
     */
    boolean isUrlWellFormed(String pingTargetUrl);
    
    /**
     * Check if the host portion of the url of a ping target is known, meaning it is either a well-formed IP address
     * or a hostname that resolves from the server.  The ping target url must parse as a <code>java.net.URL</code> in
     * order for the hostname to be extracted for this test.  This will return false if that parsing fails.
     *
     * @param pingTargetUrl url to check.
     * @return true if the <code>pingTargetUrl</code> (is well-formed and) the <code>host</code> portion of the url of the
     *         ping target is a valid IP address or a hostname that can be resolved on the server.
     */
    boolean isHostnameKnown(String pingTargetUrl);

    /**
     * Store an auto ping configuration.
     *
     * @param autoPing the auto ping configuration
     */
    void saveAutoPing(AutoPing autoPing);

    /**
     * Remove the auto ping configuration with given id.
     *
     * @param autoPing the auto ping configuration to remove
     */
    void removeAutoPing(AutoPing autoPing);

    /**
     * Remove the auto ping configuration for the given ping target and weblog, if one exists.  Returns silently if it
     * doesn't exist.
     *
     * @param pingTarget the ping target
     * @param weblog the weblog
     */
    void removeAutoPing(PingTarget pingTarget, Weblog weblog);

    /**
     * Remove all auto ping configurations for all websites.
     *
     */
    void removeAllAutoPings();

    /**
     * Retrieve an auto ping configuration by id.
     *
     * @param id the id of the auto ping configuration to retrieve.
     * @return the auto ping configuration with specified id or null if not found
     */
    AutoPing getAutoPing(String id);

    /**
     * Get all of the auto ping configurations for the given website.
     *
     * @return a list of auto ping configurations for the given website as <code>AutoPing</code> objects.
     */
    List<AutoPing> getAutoPingsByWeblog(Weblog website);

    /**
     * Get all of the auto ping configurations for a given target (across all websites).
     *
     * @return a list of auto ping configurations for the given target as <code>AuAutoPingcode> objects.
     */
    List<AutoPing> getAutoPingsByTarget(PingTarget pingTarget);

    /**
     * Queue the auto ping configurations that should be pinged upon change to an entry in the given weblog.  This calls
     * {@link OutgoingPingQueue} to queue ping requests for each ping configuration that should be applied on change to
     * the given weblog.  If ping processing is suspended, this returns without doing anything.
     *
     * @param changedWeblog the weblog that has been changed
     */
    void queueApplicableAutoPings(Weblog changedWeblog);

    /**
     * Send all pings currently in the {@link OutgoingPingQueue} to their various ping targets.
     * If ping processing is suspended, this returns without doing anything.
     */
    void sendPings();

    /**
     * Send a weblog update ping.
     *
     * This implements the <code>WeblogUpdates.ping<code> XML-RPC call
     * described at <a href="http://www.xmlrpc.com/weblogsCom">www.xmlrpc.com</a>
     *
     * @param pingTarget         the target site to ping
     * @param website            the website that changed (from which the ping originates)
     * @return a PingResult encapsulating the result message sent by the ping target.
     * @throws IOException if an IOException occurs during the ping
     * @throws XmlRpcException if the XML RPC client throws one
     */
    PingResult sendPing(PingTarget pingTarget, Weblog website) throws IOException, XmlRpcException;

    /**
     * Initialize ping targets.
     */
    void initialize();

}
