/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.apache.roller.business.jdo;

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
import org.apache.roller.pojos.PersistentObject;
import org.apache.roller.pojos.UserData;

/**
 * JDOPersistenceStrategy is responsible for the lowest-level interaction
 * with the JDO API.
 */
public class JDOPersistenceStrategy implements DatamapperPersistenceStrategy {

    /**
     * The thread local PersistenceManager.
     */
    private static final ThreadLocal pmTLS = new ThreadLocal();

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
        PersistenceManager pm = getPersistenceManager(false, false);
        if (!isTransactionActive(pm))
            return;
        pm.currentTransaction().commit();
    }

    /**
     * Release database session, rolls back any uncommitted changes.
     */
    public void release() {
    }

    /**
     * Store object using an existing transaction.
     * @param obj the object to persist
     * @return the object persisted
     * @throws org.apache.roller.RollerException on any error
     */
    public PersistentObject store(PersistentObject obj) 
            throws RollerException {
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
    }

    /**
     * Remove object from persistence storage.
     * @param po the persistent object to remove
     * @throws org.apache.roller.RollerException on any error
     */
    public void remove(PersistentObject po) 
            throws RollerException {
    }

    /**
     * Remove object from persistence storage.
     * @param pos the persistent objects to remove
     * @throws org.apache.roller.RollerException on any error
     */
    public void removeAll(Collection pos) 
            throws RollerException {
    }

    /**
     * Remove instances based on query with query parameters
     * @param clazz the class of instances to delete
     * @param queryName the name of the query
     * @param arg the argument to the query
     * @return the number of instances removed
     * @throws org.apache.roller.RollerException on any error
     */
    public int removeAll(Class clazz, String queryName, Object arg) 
            throws RollerException {
        return 0;
    }

    /**
     * Remove instances based on query with query parameters
     * @param clazz the class of instances to delete
     * @param queryName the name of the query
     * @param arg the argument to the query
     * @param types the types of the arguments to the query
     * @return the number of instances removed
     * @throws org.apache.roller.RollerException on any error
     */
    public int removeAll(Class clazz, String queryName, 
            Object arg, Object[] types)
            throws RollerException {
        return 0;
    }

    /**
     * Remove instances based on query with no query parameters
     * @param clazz the class of instances to delete
     * @param queryName the name of the query
     * @return the number of instances removed
     * @throws org.apache.roller.RollerException on any error
     */
    public int removeAll(Class clazz, String queryName)
            throws RollerException {
        return 0;
    }

    /**
     * Remove objects from persistence storage.
     * @param clazz the persistent from which to remove all objects 
     * @throws org.apache.roller.RollerException on any error
     */
    public void removeAll(Class clazz) 
            throws RollerException {
    }

    /**
     * Retrieve object, no transaction needed.
     * @param clazz the class of object to retrieve
     * @param id the id of the object to retrieve
     * @return the object retrieved
     * @throws RollerException on any error retrieving object
     */
    public PersistentObject load(Class clazz, String id) 
            throws RollerException {
        return null;
    }

    /**
     * Execute query with one query parameter.
     * @param clazz the class of instances to find
     * @param queryName the name of the query
     * @param arg the argument to the query
     * @param types the types of the arguments to the query
     * @return the results of the query
     * @throws org.apache.roller.RollerException on any error
     */
    public Object query(Class clazz, String queryName, Object arg)
            throws RollerException {
        return null;
    }

    /**
     * Execute query with one query parameter.
     * @param clazz the class of instances to find
     * @param queryName the name of the query
     * @param arg the argument to the query
     * @param types the types of the arguments to the query
     * @return the results of the query
     * @throws org.apache.roller.RollerException on any error
     */
    public Object query(Class clazz, String queryName, 
            Object arg, Object[] types)
            throws RollerException {
        return null;
    }

    /**
     * Execute query with query parameters.
     * @param clazz the class of instances to find
     * @param queryName the name of the query
     * @param args the arguments to the query
     * @return the results of the query
     * @throws org.apache.roller.RollerException on any error
     */
    public Object query(Class clazz, String queryName, Object[] args) 
            throws RollerException {
        return null;
    }

    /**
     * Execute query with query parameters.
     * @param clazz the class of instances to find
     * @param queryName the name of the query
     * @param args the arguments to the query
     * @param types the types of the arguments to the query
     * @return the results of the query
     * @throws org.apache.roller.RollerException on any error
     */
    public Object query(Class clazz, String queryName, 
            Object[] args, Object[] types)
            throws RollerException {
        return null;
    }

    /**
     * Execute query with no query parameters.
     * @param queryName the name of the query
     * @return the results of the query
     * @throws org.apache.roller.RollerException on any error
     */
    public Object query(Class clazz, String queryName) 
            throws RollerException {
        return query(clazz, queryName, null, null);
    }

    /**
     * Remove instances based on query with query parameters.
     * @param clazz the class of instances to delete
     * @param queryName the name of the query
     * @param args the arguments to the query
     * @param types the types of the arguments to the query
     * @return the number of instances removed
     * @throws org.apache.roller.RollerException on any error
     */
    public int removeAll(Class clazz, String queryName, 
            Object[] args, Object[] types)
            throws RollerException {
        return 0;
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
        return pm.currentTransaction().isActive()?true:false;
    }

    /**
     * Get the PersistenceManager associated with the 
     * current thread of control.
     * @param isPMRequired true if a PersistenceManager is created 
     * if not already associated with the thread of control
     * @param isTransactionRequired true if a transaction is begun 
     * if not already active
     * @return the PersistenceManager
     */
    private PersistenceManager getPersistenceManager
            (boolean isPMRequired, boolean isTransactionRequired) {
        return null;
    }

    /**
     * Create query.
     * @param clazz the class of instances to find
     * @param queryName the name of the query
     * @throws org.apache.roller.RollerException on any error
     */
    public DatamapperQuery newQuery(Class clazz, String queryName)
            throws RollerException {
        PersistenceManager pm = getPersistenceManager(false, false);
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
        PersistenceManager pm = getPersistenceManager(false, false);
        return new JDORemoveQueryImpl(pm, clazz, queryName);
    }

}

