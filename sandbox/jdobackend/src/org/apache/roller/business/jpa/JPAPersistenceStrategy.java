
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

package org.apache.roller.business.jpa;

import java.util.Collection;
import java.util.Properties;
import java.util.Iterator;
import java.io.InputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.datamapper.DatamapperPersistenceStrategy;
import org.apache.roller.business.datamapper.DatamapperQuery;
import org.apache.roller.business.datamapper.DatamapperRemoveQuery;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

/**
 * JPAPersistenceStrategy is responsible for the lowest-level interaction with
 * the JPA API.
 */
// TODO handle PersistenceExceptions!
public class JPAPersistenceStrategy implements DatamapperPersistenceStrategy {

    /**
     * The thread local EntityManager.
     */
    private static final ThreadLocal threadLocalEntityManager = new ThreadLocal();

    /**
     * The EntityManagerFactory for this Roller instance.
     */
    private EntityManagerFactory emf = null;

    /**
     * The logger instance for this class.
     */
    private static Log logger = LogFactory.getFactory().getInstance(
            JPAPersistenceStrategy.class);

    /**
     * Construct by finding JPA EntityManagerFactory.
     * @throws org.apache.roller.RollerException on any error
     */
    public JPAPersistenceStrategy() throws RollerException {
        Properties emfProps = loadPropertiesFromResourceName(
                "JPAEMF.properties", getContextClassLoader());
        try {
            this.emf =
                Persistence.createEntityManagerFactory("RollerPU", emfProps);
        } catch (PersistenceException pe) {
            throw new RollerException(pe);
        }
    }

    /**
     * Flush changes to the datastore, commit transaction, release em.
     * @throws org.apache.roller.RollerException on any error
     */
    public void flush() throws RollerException {
        try {
            EntityManager em = getEntityManager(false);
            if (isTransactionActive(em)) {
                em.getTransaction().commit();
            }
        } catch (PersistenceException pe) {
            throw new RollerException(pe);
        }
    }

    /**
     * Release database session, rolls back any uncommitted changes.
     */
    public void release() {
        EntityManager em = getEntityManager(false);
        if (isTransactionActive(em)) {
            em.getTransaction().rollback();
        }
        em.close();
        setThreadLocalEntityManager(null);
    }

    /**
     * Store object using an existing transaction.
     * @param obj the object to persist
     * @return the object persisted
     * @throws org.apache.roller.RollerException on any error
     */
    public Object store(Object obj) throws RollerException {
        EntityManager em = getEntityManager(true);
        em.persist(obj);
        return obj;
    }

    /**
     * Remove object from persistence storage.
     * @param clazz the class of object to remove
     * @param id the id of the object to remove
     * @throws RollerException on any error deleting object
     */
    public void remove(Class clazz, String id) throws RollerException {
        EntityManager em = getEntityManager(true);
        Object po = em.find(clazz, id);
        em.remove(po);
    }

    /**
     * Remove object from persistence storage.
     * @param po the persistent object to remove
     * @throws org.apache.roller.RollerException on any error
     */
    public void remove(Object po) throws RollerException {
        EntityManager em = getEntityManager(true);
        em.remove(po);
    }

    /**
     * Remove object from persistence storage.
     * @param pos the persistent objects to remove
     * @throws org.apache.roller.RollerException on any error
     */
    public void removeAll(Collection pos) throws RollerException {
        EntityManager em = getEntityManager(true);
        for (Iterator iterator = pos.iterator(); iterator.hasNext();) {
            Object obj = iterator.next();
            em.remove(obj);
        }
    }

    /**
     * Remove objects from persistence storage.
     * @param clazz the persistent from which to remove all objects
     * @throws org.apache.roller.RollerException on any error
     */
    public void removeAll(Class clazz) throws RollerException {
        //TODO: Think how this would be implemented
        //One possible solution is to use bulk delete using nammed queries
        //The name would be generated using clazz.
        //We will need to make sure that the named query is defined for each clazz
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
        EntityManager em = getEntityManager(false);
        return em.find(clazz, id);
    }

    /**
     * Return true if a transaction is active on the current EntityManager.
     * @param em the persistence manager
     * @return true if the persistence manager is not null and has an active
     *         transaction
     */
    private boolean isTransactionActive(EntityManager em) {
        if (em == null) {
            return false;
        }
        return em.getTransaction().isActive();
    }

    /**
     * Get the EntityManager associated with the current thread of control.
     * @param isTransactionRequired true if a transaction is begun if not
     * already active
     * @return the EntityManager
     */
    private EntityManager getEntityManager(boolean isTransactionRequired) {
        EntityManager em = getThreadLocalEntityManager();
        if (isTransactionRequired && !em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
        return em;
    }

    /**
     * Get the current ThreadLocal EntityManager
     */
    private EntityManager getThreadLocalEntityManager() {
        EntityManager em = (EntityManager) threadLocalEntityManager.get();
        if (em == null) {
            em = emf.createEntityManager();
            threadLocalEntityManager.set(em);
        }
        return em;
    }

    /**
     * Set the current ThreadLocal EntityManager
     */
    private void setThreadLocalEntityManager(Object em) {
        threadLocalEntityManager.set(em);
    }

    /**
     * Create query.
     * @param clazz the class of instances to find
     * @param queryName the name of the query
     * @throws org.apache.roller.RollerException on any error
     */
    public DatamapperQuery newQuery(Class clazz, String queryName)
            throws RollerException {
        EntityManager em = getEntityManager(false);
        return new JPANamedQueryImpl(em, queryName);
    }

    /**
     * Create query used for bulk remove operations.
     * @param clazz the class of instances to remove
     * @param queryName the name of the query
     * @throws org.apache.roller.RollerException on any error
     */
    public DatamapperRemoveQuery newRemoveQuery(Class clazz, String queryName)
            throws RollerException {
        EntityManager em = getEntityManager(false);
        return new JPARemoveQueryImpl(em, clazz, queryName);
    }

    /**
     * Create query used for bulk update operations.
     * @param queryName the name of the query
     * @throws org.apache.roller.RollerException on any error
     */
    public JPAUpdateQuery newUpdateQuery(String queryName)
            throws RollerException {
        EntityManager em = getEntityManager(false);
        return new JPAUpdateQuery(em, queryName);
    }

    /**
     * Create query from queryString
     * @param queryString the quuery
     * @throws org.apache.roller.RollerException on any error
     */
    public DatamapperQuery newDynamicQuery(String queryString)
            throws RollerException {
        EntityManager em = getEntityManager(false);
        return new JPADynamicQueryImpl(em, queryString);
    }


    /**
     * Loads properties from given resourceName using given class loader
     * @param resourceName The name of the resource containing properties
     * @param cl Classloeder to be used to locate the resouce
     * @return A properties object
     * @throws RollerException
     */
    private static Properties loadPropertiesFromResourceName(
            String resourceName, ClassLoader cl) throws RollerException {
        Properties props = new Properties();
        InputStream in = null;
        in = cl.getResourceAsStream(resourceName);
        if (in == null) {
            //TODO: Check how i18n is done in roller
            throw new RollerException(
                    "Could not locate properties to load " + resourceName);
        }
        try {
            props.load(in);
        } catch (IOException ioe) {
            throw new RollerException(
                    "Could not load properties from " + resourceName);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }

        return props;
    }

    /**
     * Get the context class loader associated with the current thread. This is
     * done in a doPrivileged block because it is a secure method.
     * @return the current thread's context class loader.
     */
    private static ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
    }
}

