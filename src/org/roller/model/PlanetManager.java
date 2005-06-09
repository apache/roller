package org.roller.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.roller.RollerException;
import org.roller.pojos.PlanetConfigData;
import org.roller.pojos.PlanetEntryData;
import org.roller.pojos.PlanetGroupData;
import org.roller.pojos.PlanetSubscriptionData;

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
    public List getTopSubscriptions(int max) throws RollerException;

    /** 
     * Get top X subscriptions, restricted by group.
     */
    public List getTopSubscriptions( 
        PlanetGroupData group, int max) throws RollerException;

    //------------------------------------------------------------ aggregations
    
    /** 
     * Get agggration for group from cache, enries in  
     * reverse chonological order.
     * Respects category constraints of group.
     * @param group 
     * @param maxEntries Maximum number of entries to return. 
     */
    public List getAggregation(
        PlanetGroupData group, int maxEntries) throws RollerException;
    
    /** 
     * Get agggration from cache, enries in reverse chonological order.
     * @param maxEntries Maximum number of entries to return. 
     */
    public List getAggregation(int maxEntries) throws RollerException;
    
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

