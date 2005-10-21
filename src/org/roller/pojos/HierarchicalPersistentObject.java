/*
 * Created on Jan 13, 2004
 */
package org.roller.pojos;

import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.model.RollerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract base class for hierarchical persistent objects. Provides generic
 * implementations of save and remove that know how to handle parents, children, 
 * and descendents.
 * 
 * @author David M Johnson
 */
public abstract class HierarchicalPersistentObject extends WebsiteObject
{        
    protected HierarchicalPersistentObject mNewParent = null;   
    
    /** Create an association between object and ancestor. */
    protected abstract Assoc createAssoc(
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
    
    protected abstract Assoc getParentAssoc() throws RollerException;

    protected abstract List getChildAssocs() throws RollerException;
    
    public abstract List getAllDescendentAssocs() throws RollerException;
    
    public abstract List getAncestorAssocs() throws RollerException;
    
    /** Returns true if this object is in use and should not be deleted */
    public abstract boolean isInUse() throws RollerException;   

    /** Save this  object and ancestoral associations. */
    public void save() throws RollerException
    {
        boolean fresh = (getId() == null || "".equals(getId()));
        PersistenceStrategy pstrategy =
            RollerFactory.getRoller().getPersistenceStrategy();
        pstrategy.store(this);
        if (fresh)
        {
            // Every fresh cat needs a parent assoc     
            Assoc parentAssoc = createAssoc(
                this, mNewParent, Assoc.PARENT);
            parentAssoc.save();
        }
        else if (null != mNewParent)
        {
            // New parent must be added to parentAssoc
            Assoc parentAssoc = getParentAssoc();
            parentAssoc.setAncestor(mNewParent);
            parentAssoc.save();
        }
        
        // Clear out existing grandparent associations
        Iterator ancestors = getAncestorAssocs().iterator();
        while (ancestors.hasNext())
        {
            Assoc assoc = (Assoc)ancestors.next();
            if (assoc.getRelation().equals(Assoc.GRANDPARENT))
            {
                assoc.remove();
            }
        }
        
        // Walk parent assocations, creating new grandparent associations
        int count = 0;
        Assoc currentAssoc = getParentAssoc();               
        while (null != currentAssoc.getAncestor())
        {
            if (count > 0) 
            {
                Assoc assoc = createAssoc(this, 
                    currentAssoc.getAncestor(), 
                    Assoc.GRANDPARENT);
                assoc.save();
            }                
            currentAssoc = currentAssoc.getAncestor().getParentAssoc();
            count++;
        }

        Iterator children = getChildAssocs().iterator();
        while (children.hasNext())
        {
            Assoc assoc = (Assoc) children.next();
            
            // resetting parent will cause reset of ancestors links
            assoc.getObject().setParent(this);
            
            // recursively...
            assoc.getObject().save();    
        }
    
        // Clear new parent now that new parent has been saved
        mNewParent = null;
    }


    /** Remove self, all decendent children and associations. */
    public void remove() throws RollerException
    {
        PersistenceStrategy pstrategy =
            RollerFactory.getRoller().getPersistenceStrategy();

        // loop to remove all of my descendents and associations
        List toRemove = new LinkedList();
        List assocs = this.getAllDescendentAssocs();
        for (int i=assocs.size()-1; i>=0; i--)
        {
            Assoc assoc = (Assoc)assocs.get(i);
            HierarchicalPersistentObject hpo = assoc.getObject();
            
            // remove my descendent's parent and grandparent associations
            Iterator ancestors = hpo.getAncestorAssocs().iterator();
            while (ancestors.hasNext())
            {
                Assoc dassoc = (Assoc)ancestors.next();
                dassoc.remove();
            }
            
            // remove decendent association and descendents
            //assoc.remove();
            toRemove.add(hpo);
        }
        Iterator removeIterator = toRemove.iterator();
        while (removeIterator.hasNext())
        {
            PersistentObject po = (PersistentObject) removeIterator.next();
            removeDescendent(pstrategy, po);
        }

        // loop to remove my own parent and grandparent associations
        Iterator ancestors = getAncestorAssocs().iterator();
        while (ancestors.hasNext())
        {
            Assoc assoc = (Assoc)ancestors.next();
            assoc.remove();
        }
        
        // remove myself
        removeDescendent(pstrategy, this);        
    }

    /**
     * Override this if you want to handle descendant removal yourself.
     */
    protected void removeDescendent(
       PersistenceStrategy pstrategy, PersistentObject po)  throws RollerException
    {
        pstrategy.remove(po);
    }

    /** Should be needed only be manager objects */
    public HierarchicalPersistentObject getNewParent()
    {
        return mNewParent;
    }
       
}
