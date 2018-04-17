/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tightblog.pojos.WebloggerProperties;

import javax.annotation.PreDestroy;
import javax.naming.NamingException;
import javax.persistence.Cache;
import javax.persistence.CacheRetrieveMode;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Responsible for the lowest-level interaction with the JPA API.
 */
@Component("persistenceStrategy")
public class JPAPersistenceStrategy {

    private static Logger log = LoggerFactory.getLogger(JPAPersistenceStrategy.class);

    /**
     * The thread local EntityManager.
     */
    private final ThreadLocal<EntityManager> threadLocalEntityManager = new ThreadLocal<>();

    /**
     * The EntityManagerFactory for this Roller instance.
     */
    private EntityManagerFactory emf;

    /**
     * Construct by finding JPA EntityManagerFactory.
     */
    protected JPAPersistenceStrategy() throws NamingException {

        // Add all JPA, OpenJPA, HibernateJPA, etc. properties found
        Properties emfProps = new Properties();
        Enumeration keys = WebloggerStaticConfig.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.startsWith("javax.persistence.") ||
                    key.startsWith("eclipselink.") ||
                    key.startsWith("hibernate.")) {
                String value = WebloggerStaticConfig.getProperty(key);
                log.info("{}: {}", key, value);
                emfProps.setProperty(key, value);
            }
        }

        boolean usingJNDI = "jndi".equals(WebloggerStaticConfig.getProperty("database.configurationType"));
        if (usingJNDI) {
            emfProps.setProperty("javax.persistence.nonJtaDataSource",
                    WebloggerStaticConfig.getProperty("database.jndi.name"));
        } else {
            emfProps.setProperty("javax.persistence.jdbc.driver",
                    WebloggerStaticConfig.getProperty("database.jdbc.driverClass"));
            emfProps.setProperty("javax.persistence.jdbc.url",
                    WebloggerStaticConfig.getProperty("database.jdbc.connectionURL"));
            emfProps.setProperty("javax.persistence.jdbc.user",
                    WebloggerStaticConfig.getProperty("database.jdbc.username"));
            emfProps.setProperty("javax.persistence.jdbc.password",
                    WebloggerStaticConfig.getProperty("database.jdbc.password"));
        }

        this.emf = Persistence.createEntityManagerFactory("TightBlogPU", emfProps);
    }

    /**
     * Refresh changes to the current object.
     */
    public void refresh(Object clazz) {
        if (clazz == null) {
            return;
        }
        try {
            EntityManager em = getEntityManager(true);
            em.refresh(clazz);
        } catch (Exception ignored) {
        }
    }

    /**
     * Flush changes to the datastore, commit transaction, release em.
     *
     * @throws RollbackException if the commit fails
     */
    public void flush() throws RollbackException {
        EntityManager em = getEntityManager(true);
        em.getTransaction().commit();
    }

    /**
     * Retrieve object, no transaction needed.
     *
     * @param clazz the class of object to retrieve
     * @param id    the id of the object to retrieve
     * @return the object retrieved
     */
    public <R> R load(Class<R> clazz, String id) {
        return load(clazz, id, false);
    }

    /**
     * Retrieve object without accessing cache, no transaction needed.
     *
     * @param clazz the class of object to retrieve
     * @param id    the id of the object to retrieve
     * @return the object retrieved
     */
    public <R> R load(Class<R> clazz, String id, boolean bypassCache) {
        EntityManager em = getEntityManager(false);
        if (bypassCache) {
            Map<String, Object> props = new HashMap<>();
            props.put("javax.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
            return em.find(clazz, id, props);
        }
        return em.find(clazz, id);
    }

    /**
     * Evict object from JPA's second-level cache
     * https://en.wikibooks.org/wiki/Java_Persistence/Caching#2nd_Level_Cache
     *
     * @param clazz the class of object to evict from cache
     * @param id    the id of the object to evict from cache
     */
    public void evict(Class clazz, String id) {
        Cache cache = emf.getCache();
        cache.evict(clazz, id);
    }

    /**
     * Retrieve managed version of object
     *
     * @return the object retrieved
     */
    public <R> R merge(R entity) {
        EntityManager em = getEntityManager(false);
        return em.merge(entity);
    }

    /**
     * Store object using an existing transaction.
     *
     * @param obj the object to persist
     * @return the object persisted
     */
    public Object store(Object obj) {
        EntityManager em = getEntityManager(true);
        if (!em.contains(obj)) {
            // If entity is not managed we can assume it is new
            em.persist(obj);
        }
        return obj;
    }

    /**
     * Remove object from persistence storage.
     *
     * @param clazz the class of object to remove
     * @param id    the id of the object to remove
     */
    public void remove(Class<Object> clazz, String id) {
        EntityManager em = getEntityManager(true);
        Object po = em.find(clazz, id);
        em.remove(po);
    }

    /**
     * Remove object from persistence storage.
     *
     * @param po the persistent object to remove
     */
    public void remove(Object po) {
        EntityManager em = getEntityManager(true);
        em.remove(po);
    }

    /**
     * Detach object, so changes to it are no longer stored in DB
     *
     * @param po the persistence object to detach
     */
    public void detach(Object po) {
        EntityManager em = getEntityManager(true);
        em.detach(po);
    }

    /**
     * Remove object from persistence storage.
     *
     * @param objsToRemove the persistent objects to remove
     */
    public void removeAll(List<?> objsToRemove) {
        EntityManager em = getEntityManager(true);
        objsToRemove.forEach(em::remove);
    }

    /**
     * Return true if a transaction is active on the current EntityManager.
     *
     * @param em the persistence manager
     * @return true if the persistence manager is not null and has an active
     * transaction
     */
    private boolean isTransactionActive(EntityManager em) {
        return em != null && em.getTransaction().isActive();
    }

    /**
     * Get the EntityManager associated with the current thread of control.
     *
     * @param isTransactionRequired true if a transaction is begun if not
     *                              already active
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
        EntityManager em = threadLocalEntityManager.get();
        if (em == null) {
            em = emf.createEntityManager();
            threadLocalEntityManager.set(em);
        }
        return em;
    }

    /**
     * Get named TypedQuery that won't commit changes to DB first (FlushModeType.COMMIT)
     * FlushModeType.AUTO commits changes to DB prior to running statement
     *
     * @param queryName   the name of the query
     * @param resultClass return type of query
     */
    public <T> TypedQuery<T> getNamedQuery(String queryName, Class<T> resultClass) {
        EntityManager em = getEntityManager(false);
        TypedQuery<T> q = em.createNamedQuery(queryName, resultClass);
        // For performance, never flush/commit prior to running queries.
        // TightBlog code assumes this behavior
        q.setFlushMode(FlushModeType.COMMIT);
        return q;
    }

    /**
     * Create TypedQuery from queryString that won't commit changes to DB first (FlushModeType.COMMIT)
     *
     * @param queryString the query
     * @param resultClass return type of query
     */
    public <T> TypedQuery<T> getDynamicQuery(String queryString, Class<T> resultClass) {
        EntityManager em = getEntityManager(false);
        TypedQuery<T> q = em.createQuery(queryString, resultClass);
        // For performance, never flush/commit prior to running queries.
        // TightBlog code assumes this behavior
        q.setFlushMode(FlushModeType.COMMIT);
        return q;
    }

    /**
     * Get named update query with default flush mode (usually FlushModeType.AUTO)
     * FlushModeType.AUTO commits changes to DB prior to running statement
     *
     * @param queryName the name of the query
     */
    public Query getNamedUpdate(String queryName) {
        EntityManager em = getEntityManager(true);
        return em.createNamedQuery(queryName);
    }

    /**
     * Release database session, rolls back any uncommitted changes.
     */
    public void release() {
        EntityManager em = null;
        try {
            em = getEntityManager(false);
            if (isTransactionActive(em)) {
                em.getTransaction().rollback();
            }
        } catch (Exception e) {
            log.error("error during releasing database session", e);
        } finally {
            if (em != null) {
                try {
                    em.close();
                } catch (Exception e) {
                    log.debug("error during closing EntityManager", e);
                }
            }
            threadLocalEntityManager.remove();
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("DB shutdown");
        release();
        if (emf != null) {
            emf.close();
        }
    }

    public WebloggerProperties getWebloggerProperties() {
        // Eclipselink logging shows WebloggerProperties is cached (with the cached value
        // updated when needed) so SQL queries don't occur with every call.
        return load(WebloggerProperties.class, "1");
    }
}
