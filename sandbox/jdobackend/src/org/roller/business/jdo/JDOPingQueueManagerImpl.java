package org.roller.business.jdo;

import java.util.List;

import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.PingQueueManagerImpl;
import org.roller.pojos.AutoPingData;
import org.roller.pojos.PingTargetData;
import org.roller.pojos.WebsiteData;

/**
 * @author Dave Johnson
 */
public class JDOPingQueueManagerImpl extends PingQueueManagerImpl {
    public JDOPingQueueManagerImpl(PersistenceStrategy persistenceStrategy) {
        super(persistenceStrategy);
    }

    public void addQueueEntry(AutoPingData autoPing) throws RollerException {
    }

    public void dropQueue() throws RollerException {
    }

    public List getAllQueueEntries() throws RollerException {
        return null;
    }

    public void removeQueueEntriesByPingTarget(PingTargetData pingTarget)
            throws RollerException {
    }

    public void removeQueueEntriesByWebsite(WebsiteData website)
            throws RollerException {
    }

}