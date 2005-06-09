/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.model;

import org.roller.pojos.PingTargetData;
import org.roller.pojos.WebsiteData;
import org.roller.RollerException;

import java.io.Serializable;
import java.util.List;

public interface PingTargetManager extends Serializable
{
    /**
     * Release all resources used.
     */
    public void release();

    /**
     * Create a common (shared) ping target.  This method does not persist the new instance.
     *
     * @param name
     * @param pingUrl
     * @return the new ping target.
     * @throws RollerException
     */
    public PingTargetData createCommonPingTarget(String name, String pingUrl) throws RollerException;

    /**
     * Create a custom ping target for the specified website.  This method does not persist the new instance.
     *
     * @param name    a short descriptive name of the ping target
     * @param pingUrl the URL to which to send pings
     * @param website the website for which the custom target is created.
     * @return the new ping target.
     * @throws RollerException
     */
    public PingTargetData createCustomPingTarget(String name, String pingUrl,
                                                 WebsiteData website) throws RollerException;

    /**
     * Store a ping target.
     *
     * @param pingTarget ping target data object.
     * @throws RollerException
     */
    public void storePingTarget(PingTargetData pingTarget) throws RollerException;

    /**
     * Retrieve a specific ping target by id.
     *
     * @param id id of the ping target to be retrieved.
     * @return the ping target whose id is specified.
     * @throws RollerException
     */
    public PingTargetData retrievePingTarget(String id) throws RollerException;

    /**
     * Remove a ping target by id.
     *
     * @param id id of the ping target to be removed
     * @throws RollerException
     */
    public void removePingTarget(String id) throws RollerException;

    /**
     * Get a list of the common (shared) ping targets.
     *
     * @return the list of common ping targets as a <code>List</code> of {@link PingTargetData} objects
     * @throws RollerException
     */
    public List getCommonPingTargets() throws RollerException;

    /**
     * Get a list of the custom ping targets for the given website.
     *
     * @param website the website whose custom targets should be returned.
     * @return the list of custom ping targets for the given website as a <code>List</code> of {@link PingTargetData}
     *         objects
     * @throws RollerException
     */
    public List getCustomPingTargets(WebsiteData website) throws RollerException;

    /**
     * Remove all of the custom ping targets for the given website.
     *
     * @param website the website whose custom ping targets should be removed
     * @throws RollerException
     */
    public void removeCustomPingTargets(WebsiteData website) throws RollerException;

    /**
     * Remove all custom targets (regardless of website).
     */
    public void removeAllCustomPingTargets() throws RollerException;


    /**
     * Check if the ping target has a name that is unique in the appropriate set.  If the ping target has no website id
     * (is common), then this checks if the name is unique amongst common targets, and if custom then unique amongst
     * custom targets. If the target has a non-null id, then it is allowed to have the same name as tha existing stored
     * target with the same id.
     *
     * @param pingTarget
     * @return true if the name is unique in the appropriate set (custom or common) ping targets.
     * @throws RollerException
     */
    public boolean isNameUnique(PingTargetData pingTarget) throws RollerException;

    /**
     * Check if the url of the ping target is well-formed.  For this test, it must parse as a <code>java.net.URL</code>,
     * with protocol <code>http</code> and a non-empty <code>host</code> portion.
     *
     * @param pingTarget
     * @return true if the <code>pingUrl</code> property of the ping target is a well-formed url.
     * @throws RollerException
     */
    public boolean isUrlWellFormed(PingTargetData pingTarget) throws RollerException;

    /**
     * Check if the host portion of the url of the ping target is known, meaning it is either a well-formed IP address
     * or a hostname that resolves from the server.  The ping target url must parse as a <code>java.net.URL</code> in
     * order for the hostname to be extracted for this test.  This will return false if that parsing fails.
     *
     * @param pingTarget
     * @return true if the <code>pingUrl</code> (is well-formed and) the <code>host</code> portion of the url of the
     *         ping target is a valid IP address or a hostname that can be resolved on the server.
     * @throws RollerException
     */
    public boolean isHostnameKnown(PingTargetData pingTarget) throws RollerException;

}
