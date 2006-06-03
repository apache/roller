/*
 * RuntimeConfigDefs.java
 *
 * Created on June 4, 2005, 1:06 PM
 */

package org.roller.config.runtime;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents the collection of all ConfigDefs.
 *
 * @author Allen Gilliland
 */
public class RuntimeConfigDefs {
    
    private List configDefs = null;
    
    
    public RuntimeConfigDefs() {
        this.configDefs = new ArrayList();
    }

    public RuntimeConfigDefs(List configs) {
        this.configDefs = configs;
    }
    
    
    public boolean addConfigDef(ConfigDef config) {
        return this.configDefs.add(config);
    }
    
    public boolean removeConfigDef(ConfigDef config) {
        return this.configDefs.remove(config);
    }
    
    
    public List getConfigDefs() {
        return configDefs;
    }

    public void setConfigDefs(List configDefs) {
        this.configDefs = configDefs;
    }
    
}
