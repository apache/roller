/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.business;

import org.roller.model.AutoPingManager;
import org.roller.model.Roller;
import org.roller.model.PingQueueManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.AutoPingData;
import org.roller.pojos.PingTargetData;
import org.roller.pojos.WebsiteData;
import org.roller.pojos.WeblogEntryData;
import org.roller.RollerException;
import org.roller.config.PingConfig;
import org.roller.config.PingConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Iterator;
import java.util.Collection;

public abstract class AutoPingManagerImpl implements AutoPingManager
{
    protected PersistenceStrategy persistenceStrategy;

    private static Log mLogger =
        LogFactory.getFactory().getInstance(AutoPingManagerImpl.class);

    public AutoPingManagerImpl(PersistenceStrategy persistenceStrategy)
    {
        this.persistenceStrategy = persistenceStrategy;
    }

    public void release()
    {
    }

    public AutoPingData createAutoPing(PingTargetData pingTarget, WebsiteData website) throws RollerException
    {
        return new AutoPingData(null, pingTarget, website);
    }

    public AutoPingData retrieveAutoPing(String id) throws RollerException
    {
        return (AutoPingData) persistenceStrategy.load(id, AutoPingData.class);
    }

    public void storeAutoPing(AutoPingData autoPing) throws RollerException
    {
        persistenceStrategy.store(autoPing);
    }

    public void removeAutoPing(String id) throws RollerException
    {
        //TODO: first remove all related category restrictions (category restrictions are not yet implemented)
        persistenceStrategy.remove(id, AutoPingData.class);
    }

    public void removeAutoPing(AutoPingData autoPing)  throws RollerException
    {
        //TODO: first remove all related category restrictions (category restrictions are not yet implemented)
        persistenceStrategy.remove(autoPing);
    }

    public void removeAutoPings(Collection autopings) throws RollerException
    {
        for(Iterator i = autopings.iterator(); i.hasNext(); ) {
            removeAutoPing((AutoPingData) i.next());
        }
    }

    public void queueApplicableAutoPings(WeblogEntryData changedWeblogEntry) throws RollerException
    {
        if (PingConfig.getSuspendPingProcessing())
        {
            if (mLogger.isDebugEnabled()) mLogger.debug("Ping processing is suspended.  No auto pings will be queued.");
            return;
        }

        PingQueueManager pingQueueMgr = RollerFactory.getRoller().getPingQueueManager();
        List applicableAutopings = getApplicableAutoPings(changedWeblogEntry);
        for (Iterator i = applicableAutopings.iterator(); i.hasNext(); ) {
            AutoPingData autoPing = (AutoPingData) i.next();
            pingQueueMgr.addQueueEntry(autoPing);
        }
    }

}
