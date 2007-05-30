
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
import java.util.Iterator;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.jpa.JPAPersistenceStrategy;

import org.apache.roller.RollerException;

import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.Weblog;

/*
 * JPAPingTargetManagerImpl.java
 *
 * Created on May 29, 2006, 2:24 PM
 *
 */
public class JPAPingTargetManagerImpl implements PingTargetManager {
    
    /** The logger instance for this class. */
    private static Log log = LogFactory.getLog(
        JPAPingTargetManagerImpl.class);

    private JPAPersistenceStrategy strategy;
    
    public JPAPingTargetManagerImpl(
            JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    public void removePingTarget(PingTarget pingTarget) 
            throws RollerException {
        // remove contents and then target
        this.removePingTargetContents(pingTarget);
        this.strategy.remove(pingTarget);
    }

    /**
     * Convenience method which removes any queued pings or auto pings that
     * reference the given ping target.
     */
    private void removePingTargetContents(PingTarget ping) 
            throws RollerException {
        // Remove the website's ping queue entries
        Query q = strategy.getNamedUpdate("PingQueueEntry.removeByPingTarget");
        q.setParameter(1, ping);
        q.executeUpdate();
        
        // Remove the website's auto ping configurations
        q = strategy.getNamedUpdate("AutoPing.removeByPingTarget");
        q.setParameter(1, ping);
        q.executeUpdate();
    }

    public void removeAllCustomPingTargets()
            throws RollerException {
        Query q = strategy.getNamedUpdate(
            "PingTarget.removeByWebsiteNotNull");
        q.executeUpdate();
    }

    public void savePingTarget(PingTarget pingTarget)
            throws RollerException {
        strategy.store(pingTarget);
    }

    public PingTarget getPingTarget(String id)
            throws RollerException {
        return (PingTarget)strategy.load(PingTarget.class, id);
    }

    public boolean isNameUnique(PingTarget pingTarget) 
            throws RollerException {
        String name = pingTarget.getName();
        if (name == null || name.trim().length() == 0) return false;
        
        String id = pingTarget.getId();
        
        // Determine the set of "brother" targets (custom or common) 
        // among which this name should be unique.
        List brotherTargets = null;
        Weblog website = pingTarget.getWebsite();
        if (website == null) {
            brotherTargets = getCommonPingTargets();
        } else {
            brotherTargets = getCustomPingTargets(website);
        }
        
        // Within that set of targets, fail if there is a target 
        // with the same name and that target doesn't
        // have the same id.
        for (Iterator i = brotherTargets.iterator(); i.hasNext();) {
            PingTarget brother = (PingTarget) i.next();
            // Fail if it has the same name but not the same id.
            if (brother.getName().equals(name) && 
                (id == null || !brother.getId().equals(id))) {
                return false;
            }
        }
        // No conflict found
        return true;
    }

    
    public boolean isUrlWellFormed(PingTarget pingTarget) 
            throws RollerException {
        String url = pingTarget.getPingUrl();
        if (url == null || url.trim().length() == 0) return false;
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
            throws RollerException {
        String url = pingTarget.getPingUrl();
        if (url == null || url.trim().length() == 0) return false;
        try {
            URL parsedUrl = new URL(url);
            String host = parsedUrl.getHost();
            if (host == null || host.trim().length() == 0) return false;
            InetAddress addr = InetAddress.getByName(host);
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public List getCommonPingTargets()
            throws RollerException {
        Query q = strategy.getNamedQuery(
                "PingTarget.getByWebsiteNullOrderByName");
        return q.getResultList();
    }

    public List getCustomPingTargets(Weblog website)
            throws RollerException {
        Query q = strategy.getNamedQuery(
                "PingTarget.getByWebsiteOrderByName");
        q.setParameter(1, website);
        return q.getResultList();
    }

    public void release() {}
    
}
