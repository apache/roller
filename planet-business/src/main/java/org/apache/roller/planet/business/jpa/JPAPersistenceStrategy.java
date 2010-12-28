
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

package org.apache.roller.planet.business.jpa;

import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.DatabaseProvider;
import org.apache.roller.planet.business.startup.PlanetStartup;
import org.apache.roller.planet.config.PlanetConfig;


/**
 * Responsible for the lowest-level interaction with the JPA API.
 */
@com.google.inject.Singleton
public class JPAPersistenceStrategy {
    
    private static Log logger = 
        LogFactory.getFactory().getInstance(JPAPersistenceStrategy.class);
    
    
    /**
     * The thread local EntityManager.
     */
    private final ThreadLocal threadLocalEntityManager = new ThreadLocal();
    
    /**
     * The EntityManagerFactory for this Roller instance.
     */
    protected EntityManagerFactory emf;
    
            
    /**
     * Construct by finding JPA EntityManagerFactory.
     * @throws org.apache.roller.planet.PlanetException on any error
     */
    protected JPAPersistenceStrategy() throws PlanetException {

        String jpaConfigurationType = PlanetConfig.getProperty("jpa.configurationType");
        if ("jndi".equals(jpaConfigurationType)) {
            // Lookup EMF via JNDI: added for Geronimo
            String emfJndiName = "java:comp/env/" + PlanetConfig.getProperty("jpa.emf.jndi.name");
            try {
                emf = (EntityManagerFactory) new InitialContext().lookup(emfJndiName);
            } catch (NamingException e) {
                throw new PlanetException("Could not look up EntityManagerFactory in jndi at " + emfJndiName, e);
            }
        } else {
            DatabaseProvider dbProvider = PlanetStartup.getDatabaseProvider();

            // Add all JPA, OpenJPA, HibernateJPA, etc. properties found
            Properties emfProps = new Properties();
            Enumeration keys = PlanetConfig.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                if (       key.startsWith("javax.persistence.") 
                        || key.startsWith("openjpa.") 
                        || key.startsWith("hibernate.")) {
                    String value = PlanetConfig.getProperty(key);
                    logger.info(key + ": " + value);
                    emfProps.setProperty(key, value);
                }
            }

            if (dbProvider.getType() == DatabaseProvider.ConfigurationType.JNDI_NAME) {
                emfProps.setProperty("javax.persistence.nonJtaDataSource", dbProvider.getJndiName());

            } else {
                emfProps.setProperty("javax.persistence.jdbc.driver", dbProvider.getJdbcDriverClass());
                emfProps.setProperty("javax.persistence.jdbc.url", dbProvider.getJdbcConnectionURL());
                emfProps.setProperty("javax.persistence.jdbc.user", dbProvider.getJdbcUsername());
                emfProps.setProperty("javax.persistence.jdbc.password", dbProvider.getJdbcPassword());
            }

            try {
                this.emf = Persistence.createEntityManagerFactory("PlanetPU", emfProps);
            } catch (PersistenceException pe) {
                logger.error("ERROR: creating entity manager", pe);
                throw new PlanetException(pe);
            }
        }
    }
          
    
    /**
     * Flush changes to the datastore, commit transaction, release em.
     * @throws org.apache.roller.planet.PlanetException on any error
     */
    public void flush() throws PlanetException {
        try {
            EntityManager em = getEntityManager(true);
            em.getTransaction().commit();
        } catch (PersistenceException pe) {
            throw new PlanetException(pe);
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
     * @throws org.apache.roller.planet.PlanetException on any error
     */
    public Object store(Object obj) throws PlanetException {
        EntityManager em = getEntityManager(true);
        if (!em.contains(obj)) {
            // If entity is not managed we can assume it is new
            em.persist(obj);
        }
        return obj;
    }
    
    /**
     * Remove object from persistence storage.
     * @param clazz the class of object to remove
     * @param id the id of the object to remove
     * @throws PlanetException on any error deleting object
     */
    public void remove(Class clazz, String id) throws PlanetException {
        EntityManager em = getEntityManager(true);
        Object po = em.find(clazz, id);
        em.remove(po);
    }
    
    /**
     * Remove object from persistence storage.
     * @param po the persistent object to remove
     * @throws org.apache.roller.planet.PlanetException on any error
     */
    public void remove(Object po) throws PlanetException {
        EntityManager em = getEntityManager(true);
        em.remove(po);
    }
    
    /**
     * Remove object from persistence storage.
     * @param pos the persistent objects to remove
     * @throws org.apache.roller.planet.PlanetException on any error
     */
    public void removeAll(Collection pos) throws PlanetException {
        EntityManager em = getEntityManager(true);
        for (Iterator iterator = pos.iterator(); iterator.hasNext();) {
            Object obj = iterator.next();
            em.remove(obj);
        }
    }    
    
    /**
     * Retrieve object, no transaction needed.
     * @param clazz the class of object to retrieve
     * @param id the id of the object to retrieve
     * @return the object retrieved
     * @throws PlanetException on any error retrieving object
     */
    public Object load(Class clazz, String id)
    throws PlanetException {
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
     * Get named query with FlushModeType.COMMIT
     * @param queryName the name of the query
     * @throws org.apache.roller.planet.PlanetException on any error
     */
    public Query getNamedQuery(String queryName)
    throws PlanetException {
        EntityManager em = getEntityManager(false);
        Query q = em.createNamedQuery(queryName);
        // Never flush for queries. Roller code assumes this behavior
        q.setFlushMode(FlushModeType.COMMIT);
        return q;
    }
    
    /**
     * Create query from queryString with FlushModeType.COMMIT
     * @param queryString the quuery
     * @throws org.apache.roller.planet.PlanetException on any error
     */
    public Query getDynamicQuery(String queryString)
    throws PlanetException {
        EntityManager em = getEntityManager(false);
        Query q = em.createQuery(queryString);
        // Never flush for queries. Roller code assumes this behavior
        q.setFlushMode(FlushModeType.COMMIT);
        return q;
    }
    
    /**
     * Get named update query with default flush mode
     * @param queryName the name of the query
     * @throws org.apache.roller.planet.PlanetException on any error
     */
    public Query getNamedUpdate(String queryName)
    throws PlanetException {
        EntityManager em = getEntityManager(true);
        Query q = em.createNamedQuery(queryName);
        return q;
    }
    
    /**
     * Loads properties from given resourceName using given class loader
     * @param resourceName The name of the resource containing properties
     * @param cl Classloeder to be used to locate the resouce
     * @return A properties object
     * @throws PlanetException
     */
    protected static Properties loadPropertiesFromResourceName(
            String resourceName, ClassLoader cl) throws PlanetException {
        Properties props = new Properties();
        InputStream in = null;
        in = cl.getResourceAsStream(resourceName);
        if (in == null) {
            //TODO: Check how i18n is done in roller
            throw new PlanetException(
                    "Could not locate properties to load " + resourceName);
        }
        try {
            props.load(in);
        } catch (IOException ioe) {
            throw new PlanetException(
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
    protected static ClassLoader getContextClassLoader() {
        return (ClassLoader) AccessController.doPrivileged(
                new PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }  
}
