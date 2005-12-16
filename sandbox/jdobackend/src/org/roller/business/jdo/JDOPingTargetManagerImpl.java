package org.roller.business.jdo;

import java.util.List;

import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.PingTargetManagerImpl;
import org.roller.pojos.WebsiteData;

public class JDOPingTargetManagerImpl extends PingTargetManagerImpl {
    public JDOPingTargetManagerImpl(PersistenceStrategy persistenceStrategy) {
        super(persistenceStrategy);
    }

    public List getCommonPingTargets() throws RollerException {
        return null;
    }

    public List getCustomPingTargets(WebsiteData website)
            throws RollerException {
        return null;
    }

    public void removeCustomPingTargets(WebsiteData website)
            throws RollerException {
    }

    public void removeAllCustomPingTargets() throws RollerException {
    }
}