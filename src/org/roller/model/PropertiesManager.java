/*
 * PropertiesManager.java
 *
 * Created on April 21, 2005, 10:34 AM
 */

package org.roller.model;

import java.util.Map;
import org.roller.RollerException;
import org.roller.pojos.RollerPropertyData;


/**
 * Manages global properties for Roller.
 */
public interface PropertiesManager {
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
    
    /** 
     * Save a single property 
     */
    public void saveProperty(RollerPropertyData property) throws RollerException;
    
    
    /** 
     * Save a list of properties 
     */
    public void saveProperties(Map properties) throws RollerException;
    
    
    /** 
     * Retrieve a single property by name 
     */
    public RollerPropertyData getProperty(String name) throws RollerException;
    
    
    /** 
     * Retrieve a list of all properties 
     */
    public Map getProperties() throws RollerException;
    
}
