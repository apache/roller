package org.roller.business.jdo;

import java.util.Collection;
import java.util.List;

import org.roller.RollerException;
import org.roller.business.AutoPingManagerImpl;
import org.roller.business.PersistenceStrategy;
import org.roller.pojos.AutoPingData;
import org.roller.pojos.PingTargetData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;

/**
 * @author Dave Johnson
 */
public class JDOAutoPingManagerImpl extends AutoPingManagerImpl {

    public JDOAutoPingManagerImpl(PersistenceStrategy persistenceStrategy) {
        super(persistenceStrategy);
        // TODO Auto-generated constructor stub
    }

    public void removeAutoPing(PingTargetData pingTarget, WebsiteData website)
            throws RollerException {
        // TODO Auto-generated method stub

    }

    public void removeAllAutoPings() throws RollerException {
        // TODO Auto-generated method stub

    }

    public List getAutoPingsByWebsite(WebsiteData website)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getAutoPingsByTarget(PingTargetData pingTarget)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getCategoryRestrictions(AutoPingData autoPing)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setCategoryRestrictions(AutoPingData autoPing,
            Collection newCategories) {
        // TODO Auto-generated method stub

    }

    public List getApplicableAutoPings(WeblogEntryData changedWeblogEntry)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

}