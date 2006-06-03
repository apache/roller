/*
 * Created on Feb 4, 2004 */package org.roller.model;import org.roller.RollerException;import org.roller.pojos.RollerConfigData;import java.io.Serializable;/**
 * Manages Roller configuration
 * @deprecated Replaced by {@link RollerProperties}.
 */public interface ConfigManager extends Serializable {    /**
     * Release all resources associated with Roller session.
     */    public void release();    /**
     * Store
     */
    public void storeRollerConfig( RollerConfigData data ) throws RollerException;        /**
     * Get single RollerConfig object in system.
     * @deprecated 
     */
    public RollerConfigData getRollerConfig() throws RollerException;        /**
     * Read RollerConfig from XML file.
     */
    public RollerConfigData readFromFile(String filePath) throws RollerException;}
