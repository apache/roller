/*
 * Created on Jan 13, 2004
 */
package org.roller.pojos;

import org.roller.RollerException;
import java.util.List;

/**
 * Abstract base class for hierarchical persistent objects. Provides generic
 * implementations of save and remove that know how to handle parents, children, 
 * and descendents.
 * 
 * @author David M Johnson
 */
public abstract class HierarchicalPersistentObject extends PersistentObject
{        
    HierarchicalPersistentObject mNewParent = null;   
    
    /** Create an association between object and ancestor. */
    public abstract Assoc createAssoc(
        HierarchicalPersistentObject object, 
        HierarchicalPersistentObject ancestor,
        String relation ) throws RollerException;
        
    /** Name of association class which must implement Assoc. */
    public abstract Class getAssocClass();
    
    /** Name of object propery in association class */
    public abstract String getObjectPropertyName();
    
    /** Name of ancestor propery in association class */
    public abstract String getAncestorPropertyName();
    
    /** Set new parent - invalidates getPath() until object is saved(). */
    public abstract void setParent(HierarchicalPersistentObject parent);
    
    public abstract Assoc getParentAssoc() throws RollerException;

    public abstract List getChildAssocs() throws RollerException;
    
    public abstract List getAllDescendentAssocs() throws RollerException;
    
    public abstract List getAncestorAssocs() throws RollerException;
    
    /** Returns true if this object is in use and should not be deleted */
    public abstract boolean isInUse() throws RollerException;   

    /** Should be needed only be manager objects */
    public HierarchicalPersistentObject getNewParent()
    {
        return mNewParent;
    }
       
}
