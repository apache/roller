
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
package org.apache.roller.weblogger.business.jpa;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.pojos.PingTarget;

/*
 * JPAPingTargetManagerImpl.java
 *
 * Created on May 29, 2006, 2:24 PM
 *
 */
@com.google.inject.Singleton
public class JPAPingTargetManagerImpl implements PingTargetManager {
    
    private final JPAPersistenceStrategy strategy;

    @com.google.inject.Inject
    protected JPAPingTargetManagerImpl(JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    
    public void removePingTarget(PingTarget pingTarget) 
            throws WebloggerException {
        // remove contents and then target
        this.removePingTargetContents(pingTarget);
        this.strategy.remove(pingTarget);
    }

    /**
     * Convenience method which removes any queued pings or auto pings that
     * reference the given ping target.
     */
    private void removePingTargetContents(PingTarget ping) 
            throws WebloggerException {
        // Remove the website's ping queue entries
        Query q = strategy.getNamedUpdate("PingQueueEntry.removeByPingTarget");
        q.setParameter(1, ping);
        q.executeUpdate();
        
        // Remove the website's auto ping configurations
        q = strategy.getNamedUpdate("AutoPing.removeByPingTarget");
        q.setParameter(1, ping);
        q.executeUpdate();
    }

    public void savePingTarget(PingTarget pingTarget)
            throws WebloggerException {
        strategy.store(pingTarget);
    }

    public PingTarget getPingTarget(String id)
            throws WebloggerException {
        return (PingTarget)strategy.load(PingTarget.class, id);
    }

    public boolean isNameUnique(PingTarget pingTarget) 
            throws WebloggerException {
        String name = pingTarget.getName();
        if (name == null || name.trim().length() == 0) {
            return false;
        }
        
        String id = pingTarget.getId();
        
        // Determine the set of "brother" targets
        // among which this name should be unique.
        List<PingTarget> brotherTargets;
        brotherTargets = getCommonPingTargets();

        // Within that set of targets, fail if there is a target 
        // with the same name and that target doesn't
        // have the same id.
        for (PingTarget brother : brotherTargets) {
            if (brother.getName().equals(name) &&
                    (id == null || !brother.getId().equals(id))) {
                return false;
            }
        }
        // No conflict found
        return true;
    }

    
    public boolean isUrlWellFormed(PingTarget pingTarget) 
            throws WebloggerException {
        String url = pingTarget.getPingUrl();
        if (url == null || url.trim().length() == 0) {
            return false;
        }
        try {
            URL parsedUrl = new URL(url);
            // OK.  If we get here, it parses ok.  Now just check 
            // that the protocol is http and there is a host portion.
            boolean isHttp = parsedUrl.getProtocol().equals("http");
            boolean hasHost = (parsedUrl.getHost() != null) && 
                (parsedUrl.getHost().trim().length() > 0);
            return isHttp && hasHost;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    
    public boolean isHostnameKnown(PingTarget pingTarget) 
            throws WebloggerException {
        String url = pingTarget.getPingUrl();
        if (url == null || url.trim().length() == 0) {
            return false;
        }
        try {
            URL parsedUrl = new URL(url);
            String host = parsedUrl.getHost();
            if (host == null || host.trim().length() == 0) {
                return false;
            }
            InetAddress addr = InetAddress.getByName(host);
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public List<PingTarget> getCommonPingTargets()
            throws WebloggerException {
        TypedQuery<PingTarget> q = strategy.getNamedQuery(
                "PingTarget.getPingTargetsOrderByName", PingTarget.class);
        return q.getResultList();
    }

    public void release() {}
    
}
