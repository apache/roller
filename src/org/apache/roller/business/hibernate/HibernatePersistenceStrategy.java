/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.business.hibernate;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.Assoc;
import org.apache.roller.pojos.HierarchicalPersistentObject;
import org.apache.roller.pojos.PersistentObject;


/**
 * Base class for Hibernate persistence implementation.
 *
 * This class serves as a helper/util class for all of the Hibernate
 * manager implementations by providing a set of basic persistence methods
 * that can be easily reused.
 *
 */
public class HibernatePersistenceStrategy {
    
    static final long serialVersionUID = 2561090040518169098L;
    
    protected static SessionFactory sessionFactory = null;
    
    private static Log log = LogFactory.getLog(HibernatePersistenceStrategy.class);
    
    
    public HibernatePersistenceStrategy() {
    }
    
    /**
     * Construct using Hibernate Session Factory.
     */
    public HibernatePersistenceStrategy(boolean configure) throws Exception {
        if (configure) {
            log.debug("Initializing Hibernate SessionFactory");
            Configuration config = new Configuration();
            config.configure("/hibernate.cfg.xml");
            this.sessionFactory = config.buildSessionFactory();
        }
    }
    
    
    /**
     * Get persistence session on current thread.
     *
     * This will open a new Session if one is not already open, otherwise
     * it will return the already open Session.
     */
    public Session getSession() {
        
        log.debug("Opening Hibernate Session");
        
        // get Hibernate Session and make sure we are in a transaction
        // this will join existing Session/Transaction if they exist
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        
        return session;
    }
    
    
    public void flush() throws RollerException {
        
        Session session = getSession();
        try {
            session.getTransaction().commit();
        } catch(Throwable t) {
            // uh oh ... failed persisting, gotta release
            release();
            
            // wrap and rethrow so caller knows something bad happened
            throw new RollerException(t);
        }
    }
    
    
    /**
     * Release database session, rollback any uncommitted changes.
     *
     * IMPORTANT: we don't want to open a transaction and force the use of a
     * jdbc connection just to close the session and do a rollback, so this
     * method must be sensitive about how the release is triggered.
     *
     * In particular we don't want to use our custom getSession() method which
     * automatically begins a transaction.  Instead we get a Session and check
     * if there is already an active transaction that needs to be rolled back.
     * If not then we can close the Session without ever getting a jdbc
     * connection, which is important for scalability.
     */
    public void release() {
        
        try {
            Session session = sessionFactory.getCurrentSession();
            
            if(session != null && session.isOpen()) {
                
                log.debug("Closing Hibernate Session");
                
                try {
                    Transaction tx = session.getTransaction();
                    
                    if(tx != null && tx.isActive()) {
                        log.debug("Forcing rollback on active transaction");
                        tx.rollback();
                    }
                } catch(Throwable t) {
                    log.error("ERROR doing Hibernate rollback", t);
                } finally {
                    if(session.isOpen()) {
                        session.close();
                    }
                }
            }
        } catch(Throwable t) {
            log.error("ERROR closing Hibernate Session", t);
        }
    }
    
    
    /**
     * Retrieve object.  We return null if the object is not found.
     */
    public PersistentObject load(String id, Class clazz) throws RollerException {
        
        if(id == null || clazz == null) {
            throw new RollerException("Cannot load objects when value is null");
        }
        
        return (PersistentObject) getSession().get(clazz, id);
    }
    
    
    /**
     * Store object.
     */
    public void store(PersistentObject obj) throws HibernateException {
        
        if(obj == null) {
            throw new HibernateException("Cannot save null object");
        }
        
        Session session = getSession();
        
        // TODO BACKEND: this is wacky, we should double check logic here
        
        // TODO BACKEND: better to use session.saveOrUpdate() here, if possible
        if ( obj.getId() == null || obj.getId().trim().equals("") ) {
            // Object has never been written to database, so save it.
            // This makes obj into a persistent instance.
            session.save(obj);
        }
        
        /*
         * technically we shouldn't have any reason to support the saving
         * of detached objects, so at some point we should re-evaluate this.
         *
         * objects should be re-attached before being saved again. it would
         * be more appropriate to reject these kinds of saves because they are
         * not really safe.
         *
         * NOTE: this may be coming from the way we use formbeans on the UI.
         *   we very commonly repopulate all data in a pojo (including id) from
         *   form data rather than properly loading the object from a Session
         *   then modifying its properties.
         */
        if ( !session.contains(obj) ) {
            
            log.debug("storing detached object: "+obj.toString());
            
            // Object has been written to database, but instance passed in
            // is not a persistent instance, so must be loaded into session.
            PersistentObject vo =
                    (PersistentObject)session.load(obj.getClass(),obj.getId());
            vo.setData(obj);
            obj = vo;
        }
        
    }
    
    
    /**
     * Remove object.
     *
     * TODO BACKEND: force the use of remove(Object) moving forward.
     */
    public void remove(String id, Class clazz) throws HibernateException {
        
        if(id == null || clazz == null) {
            throw new HibernateException("Cannot remove object when values are null");
        }
        
        Session session = getSession();
        
        PersistentObject obj = (PersistentObject) session.load(clazz,id);
        session.delete(obj);
    }
    
    
    /**
     * Remove object.
     */
    public void remove(PersistentObject obj) throws HibernateException {
        
        if(obj == null) {
            throw new HibernateException("Cannot remove null object");
        }
        
        // TODO BACKEND: can hibernate take care of this check for us?
        //               what happens if object does not use id?
        // can't remove transient objects
        if (obj.getId() != null) {
            
            getSession().delete(obj);
        }
    }
    
    
    /**
     * Store hierarchical object.
     *
     * NOTE: if the object has proper cascade setting then is all this necessary?
     */
    public void store(HierarchicalPersistentObject obj)
            throws HibernateException, RollerException {
        
        if(obj == null) {
            throw new HibernateException("Cannot save null object");
        }
        
        log.debug("Storing hierarchical object "+obj);
        
        Session session = getSession();
        
        HierarchicalPersistentObject mNewParent = obj.getNewParent();
        boolean fresh = (obj.getId() == null || "".equals(obj.getId()));
        
        if (fresh) {
            // Object has never been written to database, so save it.
            // This makes obj into a persistent instance.
            session.save(obj);
        }
        
        if(!session.contains(obj)) {
            
            // Object has been written to database, but instance passed in
            // is not a persistent instance, so must be loaded into session.
            HierarchicalPersistentObject vo =
                    (HierarchicalPersistentObject)session.load(obj.getClass(),obj.getId());
            vo.setData(obj);
            obj = vo;
        }
        
        if (fresh) {
            // Every fresh cat needs a parent assoc
            Assoc parentAssoc = obj.createAssoc(
                    obj, mNewParent, Assoc.PARENT);
            this.store(parentAssoc);
        } else if (null != mNewParent) {
            // New parent must be added to parentAssoc
            Assoc parentAssoc = obj.getParentAssoc();
            if(parentAssoc == null)
                log.error("parent assoc is null");
            parentAssoc.setAncestor(mNewParent);
            this.store(parentAssoc);
        }
        
        // Clear out existing grandparent associations
        Iterator ancestors = obj.getAncestorAssocs().iterator();
        while (ancestors.hasNext()) {
            Assoc assoc = (Assoc)ancestors.next();
            if (assoc.getRelation().equals(Assoc.GRANDPARENT)) {
                this.remove(assoc);
            }
        }
        
        // Walk parent assocations, creating new grandparent associations
        int count = 0;
        Assoc currentAssoc = obj.getParentAssoc();
        while (null != currentAssoc.getAncestor()) {
            if (count > 0) {
                Assoc assoc = obj.createAssoc(obj,
                        currentAssoc.getAncestor(),
                        Assoc.GRANDPARENT);
                this.store(assoc);
            }
            currentAssoc = currentAssoc.getAncestor().getParentAssoc();
            count++;
        }
        
        Iterator children = obj.getChildAssocs().iterator();
        while (children.hasNext()) {
            Assoc assoc = (Assoc) children.next();
            
            // resetting parent will cause reset of ancestors links
            assoc.getObject().setParent(obj);
            
            // recursively...
            this.store(assoc.getObject());
        }
        
        // Clear new parent now that new parent has been saved
        mNewParent = null;
    }
    
    
    /**
     * Store assoc.
     */
    public void store(Assoc assoc) throws HibernateException {
        
        if(assoc == null) {
            throw new HibernateException("Cannot save null object");
        }
        
        getSession().saveOrUpdate(assoc);
    }
    
    
    /**
     * Remove hierarchical object.
     *
     * NOTE: if the object has proper cascade setting then is all this necessary?
     */
    public void remove(HierarchicalPersistentObject obj) throws RollerException {
        
        if(obj == null) {
            throw new RollerException("Cannot remove null object");
        }
        
        log.debug("Removing hierarchical object "+obj.getId());
        
        // loop to remove all descendents and associations
        List toRemove = new LinkedList();
        List assocs = obj.getAllDescendentAssocs();
        for (int i=assocs.size()-1; i>=0; i--) {
            Assoc assoc = (Assoc)assocs.get(i);
            HierarchicalPersistentObject hpo = assoc.getObject();
            
            // remove my descendent's parent and grandparent associations
            Iterator ancestors = hpo.getAncestorAssocs().iterator();
            while (ancestors.hasNext()) {
                Assoc dassoc = (Assoc)ancestors.next();
                this.remove(dassoc);
            }
            
            // remove decendent association and descendents
            //assoc.remove();
            toRemove.add(hpo);
        }
        Iterator removeIterator = toRemove.iterator();
        while (removeIterator.hasNext()) {
            PersistentObject po = (PersistentObject) removeIterator.next();
            getSession().delete(po);
        }
        
        // loop to remove my own parent and grandparent associations
        Iterator ancestors = obj.getAncestorAssocs().iterator();
        while (ancestors.hasNext()) {
            Assoc assoc = (Assoc)ancestors.next();
            this.remove(assoc);
        }
        
        getSession().delete(obj);
    }
    
    
    /**
     * Remove assoc.
     */
    public void remove(Assoc assoc) throws HibernateException {
        
        if(assoc == null) {
            throw new HibernateException("Cannot save null object");
        }
        
        getSession().delete(assoc);
    }
    
}
