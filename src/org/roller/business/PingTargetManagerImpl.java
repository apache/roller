/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.AutoPingManager;
import org.roller.model.PingTargetManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.PingTargetData;
import org.roller.pojos.WebsiteData;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract implementation of PingTargetManager.
 */
public abstract class PingTargetManagerImpl implements PingTargetManager
{
    protected PersistenceStrategy persistenceStrategy;

    private static Log mLogger =
        LogFactory.getFactory().getInstance(PingTargetManagerImpl.class);

    public PingTargetManagerImpl(PersistenceStrategy persistenceStrategy)
    {
        this.persistenceStrategy = persistenceStrategy;
    }

    public void release()
    {
    }

    public PingTargetData createCommonPingTarget(String name, String pingUrl) throws RollerException
    {
        return new PingTargetData(null, name, pingUrl, null);
    }

    public PingTargetData createCustomPingTarget(String name, String pingUrl, WebsiteData website) throws RollerException
    {
        if (website == null) throw new RollerException(new IllegalArgumentException("website == null"));
        return new PingTargetData(null, name, pingUrl, website);
    }

    public void storePingTarget(PingTargetData pingTarget) throws RollerException
    {
        persistenceStrategy.store(pingTarget);
    }

    public PingTargetData retrievePingTarget(String id) throws RollerException
    {
        return (PingTargetData) persistenceStrategy.load(id, PingTargetData.class);
    }

    public void removePingTarget(String id) throws RollerException
    {
        // The retrieval is necessary in order to do the necessary cleanup of references in pingTarget.remove().
        PingTargetData pingTarget = retrievePingTarget(id);
        pingTarget.remove();
    }

    public boolean isNameUnique(PingTargetData pingTarget) throws RollerException
    {
        String name = pingTarget.getName();
        if (name == null || name.trim().length() == 0) return false;

        String id = pingTarget.getId();

        // Determine the set of "brother" targets (custom or common) among which this name should be unique.
        List brotherTargets = null;
        WebsiteData website = pingTarget.getWebsite();
        if (website == null)
        {
            brotherTargets = getCommonPingTargets();
        }
        else
        {
            brotherTargets = getCustomPingTargets(website);
        }

        // Within that set of targets, fail if there is a target with the same name and that target doesn't
        // have the same id.
        for (Iterator i = brotherTargets.iterator(); i.hasNext();)
        {
            PingTargetData brother = (PingTargetData) i.next();
            // Fail if it has the same name but not the same id.
            if (brother.getName().equals(name) && (id == null || !brother.getId().equals(id)))
            {
                return false;
            }
        }
        // No conflict found
        return true;
    }

    public boolean isUrlWellFormed(PingTargetData pingTarget) throws RollerException
    {
        String url = pingTarget.getPingUrl();
        if (url == null || url.trim().length() == 0) return false;
        try
        {
            URL parsedUrl = new URL(url);
            // OK.  If we get here, it parses ok.  Now just check that the protocol is http and there is a host portion.
            boolean isHttp = parsedUrl.getProtocol().equals("http");
            boolean hasHost = (parsedUrl.getHost() != null) && (parsedUrl.getHost().trim().length() > 0);
            return isHttp && hasHost;
        }
        catch (MalformedURLException e)
        {
            return false;
        }
    }

    public boolean isHostnameKnown(PingTargetData pingTarget) throws RollerException
    {
        String url = pingTarget.getPingUrl();
        if (url == null || url.trim().length() == 0) return false;
        try
        {
            URL parsedUrl = new URL(url);
            String host = parsedUrl.getHost();
            if (host == null || host.trim().length() == 0) return false;
            InetAddress addr = InetAddress.getByName(host);
            return true;
        }
        catch (MalformedURLException e)
        {
            return false;
        }
        catch (UnknownHostException e)
        {
            return false;
        }
    }
}
