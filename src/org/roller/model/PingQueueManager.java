/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.model;

import org.roller.RollerException;
import org.roller.pojos.AutoPingData;
import org.roller.pojos.PingQueueEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.pojos.PingTargetData;

import java.io.Serializable;
import java.util.List;

/**
 * PingQueueManager.  This interface describes the manager for the weblog update ping request queue. The queue is
 * processed by the <code>PingQueueProcesssor</code> and <code>PingQueueTask</code> components in the application
 * layer.
 */
public interface PingQueueManager extends Serializable
{
    /**
     * Release resources.
     */
    public void release();

    /**
     * Add a new persistent entry to the queue.  If the queue already contains an entry for the ping target and website
     * specified by this auto ping configuration, a new one will not be added.
     *
     * @param autoPing auto ping configuration for the ping request to be queued.
     */
    public void addQueueEntry(AutoPingData autoPing) throws RollerException;

    /**
     * Retrieve an entry from the queue.
     *
     * @param id the unique id of the entry.
     * @return the queue entry with the specified id.
     * @throws RollerException
     */
    public PingQueueEntryData retrieveQueueEntry(String id) throws RollerException;

    /**
     * Store the given queue entry.
     *
     * @param pingQueueEntry update the given queue entry
     * @throws RollerException
     */
    public void storeQueueEntry(PingQueueEntryData pingQueueEntry) throws RollerException;

    /**
     * Remove a queue entry by id.
     *
     * @param id the unique id of the entry to be removed.
     * @throws RollerException
     */
    public void removeQueueEntry(String id) throws RollerException;

    /**
     * Remove a queue entry.
     *
     * @param pingQueueEntry the entry to be removed.
     * @throws RollerException
     */
    public void removeQueueEntry(PingQueueEntryData pingQueueEntry) throws RollerException;

    /**
     * Drop the queue.  Removes all elements from the queue.
     *
     * @throws RollerException
     */
    public void dropQueue() throws RollerException;

    /**
     * Get all of the queue entries.
     *
     * @return the queue as a <code>List</code> of {@link PingQueueEntryData} objects.
     * @throws RollerException
     */
    public List getAllQueueEntries() throws RollerException;

    /**
     * Remove all of the queue entries that reference the given ping target.  This is used when the ping target is being
     * deleted (application-level cascading).
     *
     * @param pingTarget the ping target for which queue entries are to be removed
     */
    public void removeQueueEntriesByPingTarget(PingTargetData pingTarget) throws RollerException;

    /**
     * Remove all of the queue entries that reference the given website. This is used when the website is being deleted
     * (application-level cascading).
     *
     * @param website the website for which queue entreis are to be removed
     */
    public void removeQueueEntriesByWebsite(WebsiteData website) throws RollerException;
}
