
package org.roller.presentation.velocity;
import java.util.ArrayList;
import java.util.List;
import org.roller.model.PlanetManager;
import org.roller.model.Roller;
import org.roller.pojos.PlanetGroupData;
import org.roller.pojos.PlanetSubscriptionData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.PageModel;

/**
 * Allow Roller page templates to get the main Planet aggregation (the 'all'
 * and 'external' group), custom aggregations, specified by handle, and 
 * subscription entries (specified by feedUrl). 
 * @author Dave Johnson
 */
public class PlanetPageModel extends PageModel 
{
    PlanetManager planetManager = null;
    public void init(RollerRequest rreq)
    {
        super.init(rreq);
        try 
        {
            planetManager = rreq.getRoller().getPlanetManager();
        }
        catch (Exception e)
        {
            mLogger.error("ERROR initializing page model",e);
        }
    }
    /** 
     * Get main aggregation (of 'all' and 'external' groups) 
     * @returns List of PlanetEntryData objects
     */
    public List getPlanetAggregation(int max) throws Exception
    {
        return planetManager.getAggregation(max);
    }
    /** 
     * Get aggregation by group handle 
     * @returns List of PlanetEntryData objects
     */
    public List getPlanetAggregation(String groupHandle, int max) throws Exception
    {
        List list = new ArrayList();
        PlanetGroupData group = planetManager.getGroup(groupHandle);
        if (group != null) 
        {
            list = planetManager.getAggregation(group, max);
        }
        return list;
    }
    /** 
     * Get entries in a subscription specified by feedUrl.
     * @returns List of PlanetEntryData objects
     */
    public List getPlanetSubscriptionEntries(String feedUrl, int max) throws Exception 
    {
        List list = new ArrayList();
        PlanetSubscriptionData sub = planetManager.getSubscription(feedUrl);
        if (sub != null)
        {
            list = sub.getEntries();
        }
        return list;
    }
}
