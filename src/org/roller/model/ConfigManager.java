/*
 * Created on Feb 4, 2004
 */
package org.roller.model;

import org.roller.RollerException;
import org.roller.pojos.RollerConfig;

/**
 * @author lance.lavandowska
 */
public interface ConfigManager {

    /** Release any resources used */
    public void release();

    public void storeRollerConfig( RollerConfig data ) throws RollerException;
    
    public RollerConfig getRollerConfig() throws RollerException;
    
    public RollerConfig readFromFile(String filePath) throws RollerException;
}
