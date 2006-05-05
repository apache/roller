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
package org.apache.roller.webservices.adminapi.sdk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;

/**
 * This class describes an AAPP service (introspection document).
 * A Service is a set of workspaces, which is a set of
 * collections.
 *
 * @author jtb
 */

public class Service extends EntrySet {
    /** This class describes a service workspace. */    
    public static class Workspace extends EntrySet {        
        /** This class describes a workspace collection. */
        public static class Collection extends Entry {
            private static interface Tags {
                public static final String MEMBER_TYPE = "member-type";
            }
            
            private static interface Attributes {
                public static final String TITLE = "title";
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
            
            public String getMemberType() {
                return memberType;
            }
            
            public void setMemberType(String memberType) {
                this.memberType = memberType;
            }
            
        }
        
        private static interface Attributes {
            public static final String TITLE = "title";
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
    }
    
    public Service(String href) {
        setHref(href);
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
}
