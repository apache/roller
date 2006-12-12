package org.apache.roller.business.jdo;
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

import java.util.Collection;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.datamapper.DatamapperPersistenceStrategy;
import org.apache.roller.business.datamapper.DatamapperQuery;
import org.apache.roller.business.datamapper.DatamapperRemoveQuery;

/**
 * JDOPersistenceStrategy is responsible for the lowest-level interaction
 * with the JDO API.
 */
public class JDOPersistenceStrategy implements DatamapperPersistenceStrategy {

    /**
     * The thread local PersistenceManager.
     */
    private static final ThreadLocal threadLocalPersistenceManager = 
            new ThreadLocal();

    /**
     * The PersistenceManagerFactory for this Roller instance.
     */
    private PersistenceManagerFactory pmf = null;

    /**
     * The logger instance for this class.
     */
    private static Log logger = LogFactory
            .getFactory().getInstance(JDOPersistenceStrategy.class);

    /**
     * Construct by finding JDO PersistenceManagerFactory.
     * @throws org.apache.roller.RollerException on any error
     */
    public JDOPersistenceStrategy() 
            throws RollerException {
        PersistenceManagerFactory pmf = 
                JDOHelper.getPersistenceManagerFactory("JDOPMF.properties");
        this.pmf = pmf;
    }

    /**
     * Flush changes to the datastore, commit transaction, release pm.
     * @throws org.apache.roller.RollerException on any error
     */
    public void flush() 
            throws RollerException {
        PersistenceManager pm = getPersistenceManager(false);
        if (isTransactionActive(pm))
            pm.currentTransaction().commit();
    }

    /**
     * Release database session, rolls back any uncommitted changes.
     */
    public void release() {
        PersistenceManager pm = getPersistenceManager(false);
        if (isTransactionActive(pm))
            pm.currentTransaction().rollback();
        pm.close();
        setThreadLocalPersistenceManager(null);
    }

    /**
     * Store object using an existing transaction.
     * @param obj the object to persist
     * @return the object persisted
     * @throws org.apache.roller.RollerException on any error
     */
    public Object store(Object obj) 
            throws RollerException {
        PersistenceManager pm = getPersistenceManager(true);
        pm.makePersistent(obj);
        return obj;
    }

    /**
     * Remove object from persistence storage.
     * @param clazz the class of object to remove
     * @param id the id of the object to remove
     * @throws RollerException on any error deleting object
     */
    public void remove(Class clazz, String id) 
            throws RollerException {
        PersistenceManager pm = getPersistenceManager(true);
        Object po = pm.getObjectById(clazz, id);
        pm.deletePersistent(po);
    }

    /**
     * Remove object from persistence storage.
     * @param po the persistent object to remove
     * @throws org.apache.roller.RollerException on any error
     */
    public void remove(Object po) 
            throws RollerException {
        PersistenceManager pm = getPersistenceManager(true);
        pm.deletePersistent(po);
    }

    /**
     * Remove object from persistence storage.
     * @param pos the persistent objects to remove
     * @throws org.apache.roller.RollerException on any error
     */
    public void removeAll(Collection pos) 
            throws RollerException {
        PersistenceManager pm = getPersistenceManager(true);
        pm.deletePersistent(pos);
    }

    /**
     * Remove objects from persistence storage.
     * @param clazz the persistent from which to remove all objects 
     * @throws org.apache.roller.RollerException on any error
     */
    public void removeAll(Class clazz) 
            throws RollerException {
        DatamapperRemoveQuery rq = newRemoveQuery(clazz, null);
        rq.removeAll();
    }

    /**
     * Retrieve object, no transaction needed.
     * @param clazz the class of object to retrieve
     * @param id the id of the object to retrieve
     * @return the object retrieved
     * @throws RollerException on any error retrieving object
     */
    public Object load(Class clazz, String id) 
            throws RollerException {
        PersistenceManager pm = getPersistenceManager(false);
        return pm.getObjectById(clazz, id);
    }

    /**
     * Return true if a transaction is active on the 
     * current PersistenceManager.
     * @param pm the persistence manager
     * @return true if the persistence manager is not null and has 
     * an active transaction
     */
    private boolean isTransactionActive(PersistenceManager pm) {
        if (pm == null)
            return false;
        return pm.currentTransaction().isActive();
    }

    /**
     * Get the PersistenceManager associated with the 
     * current thread of control.
     * @param isTransactionRequired true if a transaction is begun 
     * if not already active
     * @return the PersistenceManager
     */
    private PersistenceManager getPersistenceManager
            (boolean isTransactionRequired) {
        PersistenceManager pm = getThreadLocalPersistenceManager();
        if (isTransactionRequired && !pm.currentTransaction().isActive()) {
             pm.currentTransaction().begin();
        }
        return pm;
    }

    /** 
     * Get the current ThreadLocal PersistenceManager
     */
    private PersistenceManager getThreadLocalPersistenceManager() {
        PersistenceManager pm = (PersistenceManager) 
            threadLocalPersistenceManager.get();
        if (pm == null) {
            pm = pmf.getPersistenceManager();
            threadLocalPersistenceManager.set(pm);
        }
        return pm;
    }

    /** 
     * Set the current ThreadLocal PersistenceManager
     */
    private void setThreadLocalPersistenceManager(Object pm) {
            threadLocalPersistenceManager.set(pm);
    }

    /**
     * Create query.
     * @param clazz the class of instances to find
     * @param queryName the name of the query
     * @throws org.apache.roller.RollerException on any error
     */
    public DatamapperQuery newQuery(Class clazz, String queryName)
            throws RollerException {
        PersistenceManager pm = getPersistenceManager(false);
        return new JDOQueryImpl(pm, clazz, queryName);
    }

    /**
     * Create query used for bulk remove operations.
     * @param clazz the class of instances to remove
     * @param queryName the name of the query
     * @throws org.apache.roller.RollerException on any error
     */
    public DatamapperRemoveQuery newRemoveQuery(Class clazz, String queryName)
            throws RollerException {
        PersistenceManager pm = getPersistenceManager(false);
        return new JDORemoveQueryImpl(pm, clazz, queryName);
    }

}

