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

package org.apache.roller.planet.business.hibernate;

import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.apache.roller.RollerException;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;


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
    
    /** No-op so XML parser doesn't hit the network looking for Hibernate DTDs */
    private EntityResolver noOpEntityResolver = new EntityResolver() {
        public InputSource resolveEntity(String publicId, String systemId) {
            return new InputSource(new StringBufferInputStream(""));
        }
    };
    
    
    public HibernatePersistenceStrategy() {
    }   

    /** 
     * Construct self using Hibernate config resource and optional dialect.
     * @param configResouce Classpath-based path to Hibernate config file (e.g. "/hibernate.cgf.xml")
     * @parma dialect Classname of Hibernate dialect to be used (overriding any specified in the configResource)
     */
    public HibernatePersistenceStrategy(
            String configResource,
            String dialect) throws Exception {

        log.info("configResource: " + configResource);
        log.info("dialect:        " + dialect);
        
        // read configResource into DOM form
        SAXBuilder builder = new SAXBuilder();
        builder.setEntityResolver(noOpEntityResolver); 
        Document configDoc = builder.build(
            getClass().getResourceAsStream(configResource));
        Element root = configDoc.getRootElement();
        Element sessionFactoryElem = root.getChild("session-factory");
        
        // remove any existing connection.datasource and dialect properties
        List propertyElems = sessionFactoryElem.getChildren("property");
        List removeList = new ArrayList();
        for (Iterator it = propertyElems.iterator(); it.hasNext();) {
            Element elem = (Element) it.next();
            if (elem.getAttribute("name") != null 
                && elem.getAttribute("name").getValue().equals("dialect")) {
                removeList.add(elem);           
            }
        }
        for (Iterator it = removeList.iterator(); it.hasNext();) {
            Element elem = (Element) it.next();
            sessionFactoryElem.removeContent(elem); 
        }
        
        // add Roller dialect property      
        Element prop = new Element("property").setAttribute(
            new Attribute("name","dialect"));
        prop.addContent(dialect);
        sessionFactoryElem.addContent(prop);
        
        Configuration config = new Configuration();
        DOMOutputter outputter = new DOMOutputter();
        config.configure(outputter.output(configDoc));
        this.sessionFactory = config.buildSessionFactory(); 
    }
    
    /** 
     * Construct self using Hibernate config resource and optional dialect.
     * @param configResouce Classpath-based path to Hibernate config file (e.g. "/hibernate.cgf.xml")
     * @parma dialect Classname of Hibernate dialect to be used (or null to use one specified in configResource)
     */
    public HibernatePersistenceStrategy(
            String configResource,
            String dialect,
            String driverClass,
            String connectionURL,
            String username,
            String password) throws Exception {
        
        log.info("configResource: " + configResource);
        log.info("dialect:        " + dialect);
        log.info("driverClass:    " + driverClass);
        log.info("connectionURL:  " + connectionURL);
        log.info("username:       " + username);

        // read configResource into DOM form
        SAXBuilder builder = new SAXBuilder();
        builder.setEntityResolver(noOpEntityResolver); 
        Document configDoc = builder.build(
            getClass().getResourceAsStream(configResource));
        Element root = configDoc.getRootElement();
        Element sessionFactoryElem = root.getChild("session-factory");
        
        // remove any existing connection.datasource and dialect properties
        List propertyElems = sessionFactoryElem.getChildren("property");
        List removeList = new ArrayList();
        for (Iterator it = propertyElems.iterator(); it.hasNext();) {
            Element elem = (Element) it.next();
            if (elem.getAttribute("name") != null 
                && elem.getAttribute("name").getValue().equals("connection.datasource")) {
                removeList.add(elem);
            }
            if (elem.getAttribute("name") != null 
                && elem.getAttribute("name").getValue().equals("dialect")) {
                removeList.add(elem);
            }
        }
        for (Iterator it = removeList.iterator(); it.hasNext();) {
            Element elem = (Element) it.next();
            sessionFactoryElem.removeContent(elem); 
        }
                                       
        // add JDBC connection params instead
        Element prop = new Element("property").setAttribute(
            new Attribute("name","hibernate.connection.driver_class"));
        prop.addContent(driverClass);
        sessionFactoryElem.addContent(prop);

        prop = new Element("property").setAttribute(
            new Attribute("name","hibernate.connection.url"));
        prop.addContent(connectionURL);
        sessionFactoryElem.addContent(prop);
        
        prop = new Element("property").setAttribute(
            new Attribute("name","hibernate.connection.username"));
        prop.addContent(username);
        sessionFactoryElem.addContent(prop);
        
        prop = new Element("property").setAttribute(
            new Attribute("name","hibernate.connection.password"));
        prop.addContent(password);
        sessionFactoryElem.addContent(prop);
        
        prop = new Element("property").setAttribute(
            new Attribute("name","dialect"));
        prop.addContent(dialect);
        sessionFactoryElem.addContent(prop);
        
        Configuration config = new Configuration();
        DOMOutputter outputter = new DOMOutputter();
        config.configure(outputter.output(configDoc));
        this.sessionFactory = config.buildSessionFactory();
    }
    
    
    /**
     * Get persistence session on current thread.
     *
     * This will open a new Session if one is not already open, otherwise
     * it will return the already open Session.
     */
    public Session getSession() {
        
        log.debug("Obtaining Hibernate Session");
        
        // get Hibernate Session and make sure we are in a transaction
        // this will join existing Session/Transaction if they exist
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();
        
        return session;
    }
    
    
    public void flush() throws RollerException {
        
        Session session = getSession();
        try {
            // first lets flush the current state to the db
            log.debug("Flushing Hibernate Session");
            session.flush();
            
            // then commit the current transaction to finish it
            log.debug("Committing Hibernate Transaction");
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
    public Object load(String id, Class clazz) throws RollerException {
        
        if(id == null || clazz == null) {
            throw new RollerException("Cannot load objects when value is null");
        }
        
        return (Object) getSession().get(clazz, id);
    }
    
    
    /**
     * Store object.
     */
    public void store(Object obj) throws HibernateException {
        
        if(obj == null) {
            throw new HibernateException("Cannot save null object");
        }
        
        Session session = getSession();
        
        session.saveOrUpdate(obj);
    }
    
    
    /**
     * Remove object.
     */
    public void remove(Object obj) throws HibernateException {
        
        if(obj == null) {
            throw new HibernateException("Cannot remove null object");
        }
        
        getSession().delete(obj);
    }
    
}
