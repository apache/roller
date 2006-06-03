/*
 * DisplayGroup.java
 *
 * Created on June 4, 2005, 1:10 PM
 */

package org.roller.config.runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single DisplayGroup.
 * Each DisplayGroup may contain 0 or more PropertyDefs.
 *
 * @author Allen Gilliland
 */
public class DisplayGroup {
    
    private List propertyDefs = null;
    private String name = null;
    private String key = null;
    
    
    public DisplayGroup() {
        this.propertyDefs = new ArrayList();
    }
    
    public DisplayGroup(List propdefs) {
        this.propertyDefs = propdefs;
    }
    
    
    public boolean addPropertyDef(PropertyDef prop) {
        return this.propertyDefs.add(prop);
    }
    
    public boolean removePropertyDef(PropertyDef prop) {
        return this.propertyDefs.remove(prop);
    }
    

    public String toString() {
        return name+","+key;
    }
    
    public List getPropertyDefs() {
        return propertyDefs;
    }

    public void setPropertyDefs(List propertyDefs) {
        this.propertyDefs = propertyDefs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    
}
