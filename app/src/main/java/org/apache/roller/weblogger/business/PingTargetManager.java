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
     * Get a list of the defined ping targets.
     *
     * @return the list of ping targets as a <code>List</code> of {@link PingTarget objects
     */
    List<PingTarget> getPingTargets();

    /**
     * Get a list of enabled ping targets.
     *
     * @return the list of enabled ping targets as a <code>List</code> of {@link PingTarget objects
     */
    List<PingTarget> getEnabledPingTargets();

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
     * Add the weblog to the set whose URLs will be sent to the ping targets.  Normally called after a blog entry
     * update so the ping targets can be made aware.  This method can be called multiple times for the same weblog
     * (due to repeated edits) without concern of ping duplication as sets naturally discard duplicates.
     *
     * If ping processing is suspended, this returns without doing anything.
     *
     * @param changedWeblog the weblog that has been changed.
     */
    void addToPingSet(Weblog changedWeblog);

    /**
     * Send out pings for all weblogs added via the above addToPingSet(weblog) call to the enabled PingTargets. Set
     * is cleared as part of the sending.
     *
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
