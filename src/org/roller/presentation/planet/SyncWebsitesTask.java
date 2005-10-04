package org.roller.presentation.planet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.PlanetManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.ScheduledTask;
import org.roller.model.UserManager;
import org.roller.pojos.PlanetConfigData;
import org.roller.pojos.PlanetGroupData;
import org.roller.pojos.PlanetSubscriptionData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.util.Technorati;

/**
 * Ensure that every user is represented by a subscription in Planet Roller 
 * database. Also "ranks" each subscription by populating Technorati inbound 
 * blogs and links counts.
 * @author Dave Johnson
 */
public class SyncWebsitesTask extends TimerTask implements ScheduledTask
{
    private static Log logger = 
        LogFactory.getFactory().getInstance(SyncWebsitesTask.class);
    private Roller roller = null;
 
    /** Task may be run from the command line */
    public static void main(String[] args) throws Exception 
    {
        RollerFactory.setRoller(
            "org.roller.business.hibernate.HibernateRollerImpl");
        SyncWebsitesTask task = new SyncWebsitesTask();
        task.init(RollerFactory.getRoller(), "dummy");
        task.run();
    }
    public void init(Roller roller, String realPath) throws RollerException
    {
        this.roller = roller;
    }
    public void run()
    {
        syncWebsites();
        rankSubscriptions();
    }
    /** 
     * Ensure there's a subscription in the "all" group for every Roller user.
     */
    private void syncWebsites()
    {       
        try
        {
            roller.begin(UserData.SYSTEM_USER);
            List liveUserFeeds = new ArrayList();            
            String baseURL = RollerRuntimeConfig.getProperty("site.absoluteurl");
            if (baseURL == null || baseURL.trim().length()==0)
            {
                logger.error("ERROR: cannot sync websites with Planet Roller - "
                            +"absolute URL not specified in Roller Config");
            }
            else
            {
                PlanetManager planet = roller.getPlanetManager();
                UserManager userManager = roller.getUserManager();
                PlanetGroupData group = planet.getGroup("all");
                if (group == null)
                {
                    group = new PlanetGroupData();
                    group.setHandle("all");
                    group.setTitle("all");
                    planet.saveGroup(group);
                    roller.commit();
                }
                try 
                {
                    String baseFeedURL = baseURL + "/rss/";
                    String baseSiteURL = baseURL + "/page/";
                    Iterator websites = 
                        roller.getUserManager().getWebsites(null, null).iterator();
                    while (websites.hasNext())
                    {
                        WebsiteData website = (WebsiteData)websites.next();
                        
                        StringBuffer sitesb = new StringBuffer();
                        sitesb.append(baseSiteURL);
                        sitesb.append(website.getHandle());
                        String siteUrl = sitesb.toString();
                        
                        StringBuffer feedsb = new StringBuffer();
                        feedsb.append(baseFeedURL);
                        feedsb.append(website.getHandle());
                        String feedUrl = feedsb.toString();
                        
                        liveUserFeeds.add(feedUrl);
                        
                        PlanetSubscriptionData sub = 
                                planet.getSubscription(feedUrl);
                        if (sub == null)
                        {
                            logger.info("ADDING feed: "+feedUrl);
                            sub = new PlanetSubscriptionData();
                            sub.setTitle(website.getName());
                            sub.setFeedUrl(feedUrl);
                            sub.setSiteUrl(siteUrl);
                            planet.saveSubscription(sub);
                            group.addSubscription(sub);
                        }
                        else
                        {
                            sub.setTitle(website.getName());
                            planet.saveSubscription(sub);
                        }
                    }
                    planet.saveGroup(group);
                    roller.commit();
                    roller.release();
                    
                    roller.begin();
                    group = group = planet.getGroup("all");
                    Iterator subs = group.getSubscriptions().iterator();
                    while (subs.hasNext())
                    {
                        PlanetSubscriptionData sub = 
                                (PlanetSubscriptionData)subs.next();
                        if (!liveUserFeeds.contains(sub.getFeedUrl()))
                        {
                            logger.info("DELETING feed: "+sub.getFeedUrl());
                            planet.deleteSubscription(sub);
                        }
                    }
                    roller.commit();                   
                }
                finally
                {
                    roller.release();
                }
            }
        }
        catch (RollerException e)
        {
            logger.error("ERROR refreshing entries", e);
        }
    }
    
    /** 
     * Loop through all subscriptions get get Technorati rankings for each 
     */
    private void rankSubscriptions()
    {       
        int count = 0;
        int errorCount = 0;
        try
        {
            roller.begin(UserData.SYSTEM_USER);
            PlanetManager planet = roller.getPlanetManager();
            PlanetConfigData config = planet.getConfiguration();
            Technorati technorati = null;
            if (config.getProxyHost()!=null && config.getProxyPort() != -1)
            {
                technorati = new Technorati(
                        config.getProxyHost(), config.getProxyPort());
            }
            else 
            {
                technorati = new Technorati();
            }                
            UserManager userManager = roller.getUserManager();
            try 
            {
                // Technorati API allows only 500 queries per-day
                int limit = 500;
                int userCount = planet.getSubscriptionCount();
                int mod = (userCount / limit) + 1;
                
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                int day = cal.get(Calendar.DAY_OF_YEAR);
                
                int start = (day % mod) * limit;
                int end = start + limit;
                end = end > userCount ? userCount : end; 
                logger.info("Updating subscriptions ["+start+":"+end+"]");
                
                Iterator subs = planet.getAllSubscriptions();
                while (subs.hasNext())
                {
                    PlanetSubscriptionData sub = 
                            (PlanetSubscriptionData)subs.next();
                    if (count >= start && count < end)
                    {
                        try
                        {
                            Technorati.Result result = 
                                    technorati.getBloginfo(sub.getSiteUrl());
                            if (result != null && result.getWeblog() != null)
                            {
                              sub.setInboundblogs(
                                      result.getWeblog().getInboundblogs());
                              sub.setInboundlinks(
                                      result.getWeblog().getInboundlinks());
                              logger.debug("Adding rank for "
                                      +sub.getFeedUrl()+" ["+count+"|"
                                      +sub.getInboundblogs()+"|"
                                      +sub.getInboundlinks()+"]");
                            }
                            else 
                            {
                              logger.debug(
                                "No ranking available for "
                                      +sub.getFeedUrl()+" ["+count+"]");
                              sub.setInboundlinks(0);
                              sub.setInboundblogs(0);
                            }
                            planet.saveSubscription(sub);
                        }
                        catch (Exception e) 
                        {
                            logger.warn("WARN ranking subscription ["
                                        + count + "]: " + e.getMessage());
                            if (errorCount++ > 5)
                            {
                                logger.warn(
                                    "   Stopping ranking, too many errors");
                                break;
                            }
                        }
                    }
                    count++;
                }
                roller.commit();
            }
            finally
            {
                roller.release();
            }
        }
        catch (Exception e)
        {
            logger.error("ERROR ranking subscriptions", e);
        }
    }
}

