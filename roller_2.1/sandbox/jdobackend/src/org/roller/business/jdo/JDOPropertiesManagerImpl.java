package org.roller.business.jdo;

import java.util.Map;

import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.PropertiesManagerImpl;
import org.roller.pojos.RollerPropertyData;

/**
 * @author Dave Johnson
 */
public class JDOPropertiesManagerImpl extends PropertiesManagerImpl {

    public JDOPropertiesManagerImpl(PersistenceStrategy strategy) {
        super(strategy);
    }

    public RollerPropertyData getProperty(String name) throws RollerException {
        return null;
    }

    public Map getProperties() throws RollerException {
        return null;
    }

}