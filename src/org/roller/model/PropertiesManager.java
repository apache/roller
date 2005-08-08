/*
 * PropertiesManager.java
 *
 * Created on April 21, 2005, 10:34 AM
 */

package org.roller.model;

import java.io.Serializable;
import java.util.Map;
import org.roller.RollerException;
import org.roller.pojos.RollerPropertyData;

/**
 *
 * @author Allen Gilliland
 */
public interface PropertiesManager extends Serializable 
{
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();

    /** Save a single property */
    public void store(RollerPropertyData property) throws RollerException;
    
    /** Save a list of properties */
    public void store(Map properties) throws RollerException;
    
    /** Retrieve a single property by name */
    public RollerPropertyData getProperty(String name) throws RollerException;
    
    /** Retrieve a list of all properties */
    public Map getProperties() throws RollerException;
    
}
