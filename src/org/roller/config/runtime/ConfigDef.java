/*
 * ConfigDef.java
 *
 * Created on June 4, 2005, 1:10 PM
 */

package org.roller.config.runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a logic grouping of runtime configuration properties.
 * Each ConfigDef may contain 0 or more DisplayGroups.
 *
 * @author Allen Gilliland
 */
public class ConfigDef {
    
    private List displayGroups = null;
    private String name = null;
    
    
    public ConfigDef() {
        this.displayGroups = new ArrayList();
    }

    public ConfigDef(List displaygroups) {
        this.displayGroups = displaygroups;
    }

    
    public boolean addDisplayGroup(DisplayGroup group) {
        return this.displayGroups.add(group);
    }
    
    public boolean removeDisplayGroup(DisplayGroup group) {
        return this.displayGroups.remove(group);
    }
    
    
    public String toString() {
        return name;
    }
    
    public List getDisplayGroups() {
        return displayGroups;
    }

    public void setDisplayGroups(List displayGroups) {
        this.displayGroups = displayGroups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
