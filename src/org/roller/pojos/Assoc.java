/*
 * Created on Jan 13, 2004
 */

package org.roller.pojos;

import org.roller.RollerException;
import java.io.Serializable;


/**
 * Interface for hierarchical assocations.
 */
public interface Assoc extends Serializable {
    
    public static final String PARENT = "PARENT";
    public static final String GRANDPARENT = "GRANDPARENT";
    
    /** Object that owns this association. */
    public HierarchicalPersistentObject getObject();
    
    public void setObject(HierarchicalPersistentObject hpo);
    
    
    /** Associated object. */
    public HierarchicalPersistentObject getAncestor();
    
    public void setAncestor(HierarchicalPersistentObject hpo);
    
    
    /** Type of relationship, PARENT or GRANDPARENT. */
    public String getRelation();
    
}
