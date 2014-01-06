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

package org.apache.roller.weblogger.business.jpa;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerConfig;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.roller.weblogger.business.DatabaseProvider;


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
    private EntityManagerFactory emf = null;
    
            
    /**
     * Construct by finding JPA EntityManagerFactory.
     * @param dbProvider database configuration information for manual configuration.
     * @throws org.apache.roller.weblogger.WebloggerException on any error
     */
    @com.google.inject.Inject
    protected JPAPersistenceStrategy(DatabaseProvider dbProvider) throws WebloggerException {
        String jpaConfigurationType = WebloggerConfig.getProperty("jpa.configurationType");
        if ("jndi".equals(jpaConfigurationType)) {
            // Lookup EMF via JNDI: added for Geronimo
            String emfJndiName = "java:comp/env/" + WebloggerConfig.getProperty("jpa.emf.jndi.name");
            try {
                emf = (EntityManagerFactory) new InitialContext().lookup(emfJndiName);
            } catch (NamingException e) {
                throw new WebloggerException("Could not look up EntityManagerFactory in jndi at " + emfJndiName, e);
            }
        } else {

            // Add all JPA, OpenJPA, HibernateJPA, etc. properties found
            Properties emfProps = new Properties();
            Enumeration keys = WebloggerConfig.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                if (       key.startsWith("javax.persistence.") 
                        || key.startsWith("openjpa.")
                        || key.startsWith("eclipselink.")
                        || key.startsWith("hibernate.")) {
                    String value = WebloggerConfig.getProperty(key);
                    logger.info(key + ": " + value);
                    emfProps.setProperty(key, value);
                }
            }

            if (dbProvider.getType() == DatabaseProvider.ConfigurationType.JNDI_NAME) {
                emfProps.setProperty("javax.persistence.nonJtaDataSource", dbProvider.getFullJndiName());
            } else {
                emfProps.setProperty("javax.persistence.jdbc.driver", dbProvider.getJdbcDriverClass());
                emfProps.setProperty("javax.persistence.jdbc.url", dbProvider.getJdbcConnectionURL());
                emfProps.setProperty("javax.persistence.jdbc.user", dbProvider.getJdbcUsername());
                emfProps.setProperty("javax.persistence.jdbc.password", dbProvider.getJdbcPassword());
            }

            try {
                this.emf = Persistence.createEntityManagerFactory("RollerPU", emfProps);

            } catch (Exception pe) {
                logger.error("ERROR: creating entity manager", pe);
                throw new WebloggerException(pe);
            }
        }
    }
    /**
     * Refresh changes to the current object.
     * 
     * @throws org.apache.roller.weblogger.WebloggerException on any error
     */
    public void refresh(Object clazz) throws WebloggerException {
        try {
            EntityManager em = getEntityManager(true);
            em.refresh(clazz);
        } catch (PersistenceException pe) {
            throw new WebloggerException(pe);
        }
    }
    /**
     * Flush changes to the datastore, commit transaction, release em.
     * @throws org.apache.roller.weblogger.WebloggerException on any error
     */
    public void flush() throws WebloggerException {
        try {
            EntityManager em = getEntityManager(true);
            em.getTransaction().commit();
        } catch (PersistenceException pe) {
            throw new WebloggerException(pe);
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
     * @throws org.apache.roller.weblogger.WebloggerException on any error
     */
    public Object store(Object obj) throws WebloggerException {
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
     * @throws WebloggerException on any error deleting object
     */
    public void remove(Class clazz, String id) throws WebloggerException {
        EntityManager em = getEntityManager(true);
        Object po = em.find(clazz, id);
        em.remove(po);
    }
    
    /**
     * Remove object from persistence storage.
     * @param po the persistent object to remove
     * @throws org.apache.roller.weblogger.WebloggerException on any error
     */
    public void remove(Object po) throws WebloggerException {
        EntityManager em = getEntityManager(true);
        em.remove(po);
    }
    
    /**
     * Remove object from persistence storage.
     * @param pos the persistent objects to remove
     * @throws org.apache.roller.weblogger.WebloggerException on any error
     */
    public void removeAll(Collection pos) throws WebloggerException {
        EntityManager em = getEntityManager(true);
        for (Object obj : pos) {
            em.remove(obj);
        }
    }
    
    /**
     * Retrieve object, no transaction needed.
     * @param clazz the class of object to retrieve
     * @param id the id of the object to retrieve
     * @return the object retrieved
     * @throws WebloggerException on any error retrieving object
     */
    public Object load(Class clazz, String id)
    throws WebloggerException {
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
    public EntityManager getEntityManager(boolean isTransactionRequired) {
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
     * @throws org.apache.roller.weblogger.WebloggerException on any error
     */
    public Query getNamedQuery(String queryName)
    throws WebloggerException {
        EntityManager em = getEntityManager(false);
        Query q = em.createNamedQuery(queryName);
        // Never flush for queries. Roller code assumes this behavior
        q.setFlushMode(FlushModeType.COMMIT);
        return q;
    }
    
    /**
     * Create query from queryString with FlushModeType.COMMIT
     * @param queryString the quuery
     * @throws org.apache.roller.weblogger.WebloggerException on any error
     */
    public Query getDynamicQuery(String queryString)
    throws WebloggerException {
        EntityManager em = getEntityManager(false);
        Query q = em.createQuery(queryString);
        // Never flush for queries. Roller code assumes this behavior
        q.setFlushMode(FlushModeType.COMMIT);
        return q;
    }
    
    /**
     * Get named update query with default flush mode
     * @param queryName the name of the query
     * @throws org.apache.roller.weblogger.WebloggerException on any error
     */
    public Query getNamedUpdate(String queryName)
    throws WebloggerException {
        EntityManager em = getEntityManager(true);
        Query q = em.createNamedQuery(queryName);
        return q;
    }
    
    /**
     * Loads properties from given resourceName using given class loader
     * @param resourceName The name of the resource containing properties
     * @param cl Classloeder to be used to locate the resouce
     * @return A properties object
     * @throws WebloggerException
     */
    private static Properties loadPropertiesFromResourceName(
            String resourceName, ClassLoader cl) throws WebloggerException {
        Properties props = new Properties();
        InputStream in;
        in = cl.getResourceAsStream(resourceName);
        if (in == null) {
            //TODO: Check how i18n is done in roller
            throw new WebloggerException(
                    "Could not locate properties to load " + resourceName);
        }
        try {
            props.load(in);
        } catch (IOException ioe) {
            throw new WebloggerException(
                    "Could not load properties from " + resourceName);
        } finally {
            try {
                in.close();
            } catch (IOException ioe) {
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
