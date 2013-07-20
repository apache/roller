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
/*
 * AtomAdminService.java
 *
 * Created on January 17, 2006, 12:44 PM
 */
package org.apache.roller.weblogger.webservices.adminprotocol.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.EntrySet.Types;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * This class describes an RAP service (introspection document).
 * A Service is a set of workspaces, which is a set of
 * collections.
 *
 * @author jtb
 */

public class Service extends EntrySet {
    
    public Service(String href) {
        setHref(href);
    }
    
    public Service(Document d) throws UnexpectedRootElementException {
        populate(d);
    }
    
    public Service(InputStream stream) throws JDOMException, IOException, UnexpectedRootElementException {
        SAXBuilder sb = new SAXBuilder();
        Document d = sb.build(stream);
        populate(d);        
    }    

    public String getType() {
        return Types.SERVICE;
    }
    
    public Document toDocument() {
        Document doc = new Document();
        Element root = new Element(Types.SERVICE, NAMESPACE);
        doc.setRootElement(root);
        
        for (int i = 0; i < getEntries().length; i++) {
            Entry entry = getEntries()[i];
            root.addContent(entry.toDocument().detachRootElement());
        }
        
        return doc;
    }

    public void populate(Document doc) {
        Element root = doc.getRootElement();
        List workspaces = new ArrayList();
        List spaces = root.getChildren("workspace", NAMESPACE);
        Iterator iter = spaces.iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            Workspace ws = new Workspace();
            ws.populate(e);
            workspaces.add(ws);
        }
        setEntries((Entry[])workspaces.toArray(new Workspace[0]));
    }
            
    /** This class describes a service workspace. */    
    public static class Workspace extends EntrySet {   
        
        private interface Attributes {
            String TITLE = "title";
        }
        
        private String title = null;
        
        public Workspace() {
        }
        
        public String getType() {
            return Types.WORKSPACE;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        
        public Document toDocument() {
            Document doc = new Document();
            Element element = new Element(EntrySet.Types.WORKSPACE, NAMESPACE);
            doc.setRootElement(element);
            
            element.setAttribute(Attributes.TITLE, getTitle());
            for (int i = 0; i < getEntries().length; i++) {
                Entry entry = getEntries()[i];
                element.addContent(entry.toDocument().detachRootElement());
            }
            
            return doc;
        }
        
        public void populate(Element elem) {
            setTitle(elem.getAttributeValue(Attributes.TITLE)); //, NAMESPACE));
            List collections = new ArrayList();
            List spaces = elem.getChildren("collection", NAMESPACE);
            Iterator iter = spaces.iterator();
            while (iter.hasNext()) {
                Element e = (Element) iter.next();
                Collection col = new Collection();
                col.populate(e);
                collections.add(col);
            }
            Workspace.this.setEntries((Entry[])collections.toArray(new Collection[0]));
        }


        /** This class describes a workspace collection. */
        public static class Collection extends Entry {
            
            private interface Tags {
                String MEMBER_TYPE = "member-type";
            }
            
            private interface Attributes {
                String TITLE = "title";
            }
            
            private String title;
            private String memberType;
            
            public Collection() {
                // nothing
            }
            
            public String getType() {
                return Types.COLLECTION;
            }
            
            public String getTitle() {
                return title;
            }
            
            public void setTitle(String title) {
                this.title = title;
            }
            
            
            public Document toDocument() {
                Document doc = new Document();
                Element element = new Element(Types.COLLECTION, NAMESPACE);
                doc.setRootElement(element);
                
                element.setAttribute(Attributes.TITLE, getTitle());
                element.setAttribute(Entry.Attributes.HREF, getHref());
                
                Element memberType = new Element(Tags.MEMBER_TYPE, NAMESPACE);
                memberType.setText(getMemberType());
                element.addContent(memberType);
                
                return doc;
            }
            
            
            public void populate(Element elem) {
                setTitle(elem.getAttributeValue(Attributes.TITLE)); //, NAMESPACE));
                setHref(elem.getAttributeValue(Entry.Attributes.HREF)); //, NAMESPACE));
                Element typeElem = elem.getChild(Tags.MEMBER_TYPE, NAMESPACE);
                setMemberType(typeElem.getText());
            }
            
            public String getMemberType() {
                return memberType;
            }
            
            public void setMemberType(String memberType) {
                this.memberType = memberType;
            }
            
        }
        
    }
    
}
