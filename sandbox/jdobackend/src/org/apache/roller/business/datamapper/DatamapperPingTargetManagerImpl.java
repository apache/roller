/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.business.datamapper;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.PingTargetManager;
import org.apache.roller.pojos.AutoPingData;
import org.apache.roller.pojos.PingQueueEntryData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.WebsiteData;

/*
 * DatamapperPingTargetManagerImpl.java
 *
 * Created on May 29, 2006, 2:24 PM
 *
 */
public class DatamapperPingTargetManagerImpl implements PingTargetManager {
    
    private DatamapperPersistenceStrategy strategy;
    
    /** The logger instance for this class. */
    private static Log logger = LogFactory
            .getFactory().getInstance(DatamapperPingTargetManagerImpl.class);

    /** Creates a new instance of DatamapperPropertiesManagerImpl */
    public DatamapperPingTargetManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    public void savePingTarget(PingTargetData pingTarget)
            throws RollerException {
        strategy.store(pingTarget);
    }

    public void removePingTarget(PingTargetData pingTarget)
            throws RollerException {
        // remove queued ping entries that refer to this ping target
        strategy.newRemoveQuery(PingQueueEntryData.class, "getByPingTarget")
            .removeAll(pingTarget);
        // remove autopings that refer to this ping target
        strategy.newRemoveQuery(AutoPingData.class, "getByPingTarget")
            .removeAll(pingTarget);
    }

    public void removeAllCustomPingTargets()
            throws RollerException {
        strategy.newRemoveQuery(PingTargetData.class, "getByWebsiteNotNull")
            .removeAll();
    }

    public PingTargetData getPingTarget(String id)
            throws RollerException {
        return (PingTargetData)strategy.load(PingTargetData.class, id);
    }

    public List getCommonPingTargets()
            throws RollerException {
        return (List)strategy.newQuery(PingTargetData.class,
                "getByWebsiteNull.orderByName")
            .execute();
    }

    public List getCustomPingTargets(WebsiteData website)
            throws RollerException {
        return (List)strategy.newQuery(PingTargetData.class,
                "getByWebsite.orderByName")
            .execute(website);
    }

    public boolean isNameUnique(PingTargetData pingTarget)
            throws RollerException {
        String name = pingTarget.getName();
        if (name == null || name.trim().length() == 0) return false;
        int count = ((Integer)
            strategy.newQuery(PingTargetData.class,
                    "countByWebsite&&NameEqual&&IdNotEqual")
                .execute(new Object[]{
                    pingTarget.getWebsite(), 
                    name,
                    pingTarget.getId()}))
            .intValue();
        return (count != 0);
    }

    public boolean isUrlWellFormed(PingTargetData pingTarget)
            throws RollerException {
        String url = pingTarget.getPingUrl();
        if (url == null || url.trim().length() == 0) return false;
        try {
            URL parsed = new URL(url);
            if (!parsed.getProtocol().equals("http"))
                return false;
            return (parsed.getHost() != null) && 
                    (parsed.getHost().trim().length() > 0);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public boolean isHostnameKnown(PingTargetData pingTarget)
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

    public void release() {
    }
    
}
