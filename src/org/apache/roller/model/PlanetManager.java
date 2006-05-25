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
package org.apache.roller.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.roller.RollerException;
import org.apache.roller.pojos.PlanetConfigData;
import org.apache.roller.pojos.PlanetEntryData;
import org.apache.roller.pojos.PlanetGroupData;
import org.apache.roller.pojos.PlanetSubscriptionData;

/**
 * Manages groups and subscriptions, can return aggregation for any group.
 * @author David M Johnson
 */
public interface PlanetManager extends Serializable
{
    //------------------------------------------------------------------ create
    
    /**
     * Save configration 
     */
    public void saveConfiguration(PlanetConfigData config) 
        throws RollerException;
    
    /**
     * Save new or update existing entry
     */
    public void saveEntry(PlanetEntryData entry) throws RollerException; 

    /**
     * Save new or update existing a group
     */
    public void saveGroup(PlanetGroupData sub) throws RollerException;
    
    /**
     * Save or update a subscription
     */
    public void saveSubscription(PlanetSubscriptionData sub) 
        throws RollerException;
    
    //---------------------------------------------------------------- retrieve    

    /** 
     * Get the one planet config object, config has default group 
     */
    public PlanetConfigData getConfiguration() throws RollerException;
    
    /** 
     * Get handles for all defined groups 
     */
    public List getGroupHandles() throws RollerException;
    
    /** 
     * Get list of group objects 
     */
    public List getGroups() throws RollerException;
    
    /** 
     * Get group by handle, group has subscriptions
     */
    public PlanetGroupData getGroup(String handle) throws RollerException;
    
    /** 
     * Get group by ID rather than handle.
     */
    public PlanetGroupData getGroupById(String id) throws RollerException;
    
    /**
     * Get subscription by feedUrl.
     */
    public PlanetSubscriptionData getSubscription(String feedUrl) 
        throws RollerException;

    /**
     * Get subscription by ID rather than feedUrl.
     */
    public PlanetSubscriptionData getSubscriptionById(String id) 
        throws RollerException;
    
    /** 
     * Get all subscriptions.
     */
    public Iterator getAllSubscriptions() throws RollerException;
    
    /**
     * Get total number of subscriptions.
     */
    public int getSubscriptionCount() throws RollerException;
    
    /** 
     * Get top X subscriptions.
     */
    public List getTopSubscriptions(int offset, int len) throws RollerException;

    /** 
     * Get top X subscriptions, restricted by group.
     */
    public List getTopSubscriptions( 
        PlanetGroupData group, int offset, int len) throws RollerException;

    /** 
     * Get entries in a single feed as list of PlanetEntryData objects.
     */
    public List getFeedEntries( 
        String feedUrl, int offset, int len) throws RollerException;

    //------------------------------------------------------------ aggregations
    
    /** 
     * Get agggration for group from cache, enries in reverse chonological order.
     * Respects category constraints of group.
     * @param group Restrict to entries from one subscription group.
     * @param offset    Offset into results (for paging)
     * @param len       Maximum number of results to return (for paging) 
     */
    public List getAggregation(
        PlanetGroupData group, int offset, int len) throws RollerException;
    
    /** 
     * Get agggration from cache, enries in reverse chonological order.
     * @param offset    Offset into results (for paging)
     * @param len       Maximum number of results to return (for paging)
     */
    public List getAggregation(int offset, int len) throws RollerException;
    
    //------------------------------------------------------------------ update
    
    /** Refresh entry data by fetching and parsing feeds. */
    public void refreshEntries() throws RollerException;
    
    //------------------------------------------------------------------ delete
    
    /** Delete group and any subscriptions that are orphaned. */
    public void deleteGroup(PlanetGroupData group) throws RollerException;
    
    /** Delete subscription, remove it from groups, cache, etc. */
    public void deleteSubscription(PlanetSubscriptionData group) 
        throws RollerException;
    
    /** Delete entry. */
    public void deleteEntry(PlanetEntryData entry) throws RollerException;

    /** Clear any aggregations and update times that have been cached */
    public void clearCachedAggregations();
    
    /** Get last update time for entries most recent 'all' aggregation */
    public Date getLastUpdated();
    
    /** Get last updated time for entries in a specify group */
    public Date getLastUpdated(PlanetGroupData group);
}

