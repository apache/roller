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
import org.roller.model.PingQueueManager;
import org.roller.pojos.PingQueueEntryData;

public abstract class PingQueueManagerImpl implements PingQueueManager
{
    protected PersistenceStrategy persistenceStrategy;

    private static Log logger = LogFactory.getLog(PingQueueManagerImpl.class);

    public PingQueueManagerImpl(PersistenceStrategy persistenceStrategy)
    {
        this.persistenceStrategy = persistenceStrategy;
    }

    public void release()
    {
    }

    public PingQueueEntryData retrieveQueueEntry(String id) throws RollerException
    {
        return (PingQueueEntryData) persistenceStrategy.load(id, PingQueueEntryData.class);
    }

    public void storeQueueEntry(PingQueueEntryData pingQueueEntry) throws RollerException
    {
        if (logger.isDebugEnabled()) logger.debug("Storing ping queue entry: " + pingQueueEntry);
        persistenceStrategy.store(pingQueueEntry);
    }

    public void removeQueueEntry(String id) throws RollerException
    {
        if (logger.isDebugEnabled()) logger.debug("Removing ping queue entry with id: " + id);
        persistenceStrategy.remove(id, PingQueueEntryData.class);
    }

    public void removeQueueEntry(PingQueueEntryData pingQueueEntry) throws RollerException
    {
        if (logger.isDebugEnabled()) logger.debug("Removing ping queue entry: " + pingQueueEntry);
        persistenceStrategy.remove(pingQueueEntry);
    }

}
