/*
 * Created on Dec 13, 2005
 */
package org.roller.business.jdo;

import org.roller.RollerException;
import org.roller.business.ConfigManagerImpl;
import org.roller.business.PersistenceStrategy;
import org.roller.pojos.RollerConfigData;

/**
 * @author Dave Johnson
 */
public class JDOConfigManagerImpl extends ConfigManagerImpl {
    public JDOConfigManagerImpl(PersistenceStrategy strategy) {
        super(strategy);
    }

    public RollerConfigData getRollerConfig() throws RollerException {
        return null;
    }

}