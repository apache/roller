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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.hibernate.HibernatePersistenceStrategy;
import org.hibernate.cfg.Configuration;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;

class PlanetHibernatePersistenceStrategy extends HibernatePersistenceStrategy {
    private static Log log = 
            LogFactory.getLog(PlanetHibernatePersistenceStrategy.class);
    
    public PlanetHibernatePersistenceStrategy(
            String configResource,
            String dialect) throws Exception {
        
        // read configResource into DOM form
        SAXBuilder builder = new SAXBuilder();
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
                && dialect != null
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

    public PlanetHibernatePersistenceStrategy(
            String configResource,
            String dialect,
            String driverClass,
            String connectionURL,
            String username,
            String password) throws Exception {
        
        // read configResource into DOM form
        SAXBuilder builder = new SAXBuilder();
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
                && dialect != null
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
}



