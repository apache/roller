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
package org.apache.roller.webservices.adminprotocol.sdk;

import java.util.Arrays;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;

/**
 * This class is the abstract notion of a set of entries.
 * Weblog resources are represented by sets of entries. 
 *
 * @author jtb
 */
public abstract class EntrySet extends Entry {
    /** Entry set types. */
    public static interface Types {
        /*
         * Set of user entries. 
         * A user entry describes a user of the weblog server.
         */
        public static final String USERS = "users";
        /** 
         * Set of weblog entries. 
         * Note that this is not a set of entries in a weblog, but rather,
         * a set of entries that describe the weblog itself.
         */
        public static final String WEBLOGS = "weblogs";
        /** 
         * Set of member entries.
         * A member entry describes a user's membership to and 
         * permission with a particular weblog.
         */
        public static final String MEMBERS = "members";
         /**
          * Set of workspace entries.
          * This type, along with WORKSPACE and COLLECTION, define
          * the element that describe the introspection document
          * for the RAP service.
          * <p>
          * A service is a set of workspaces, and a workspace is a set of 
          * collections.
          */
        public static final String SERVICE = "service";        
        /** Set of collection entries. */
        public static final String WORKSPACE = "workspace";           
    }
    
    private List entries = null;
        
    /** Get the type of this object. */
    public abstract String getType();
    
    /** Get the entries in this object. */
    public Entry[] getEntries() {
        return (Entry[])entries.toArray(new Entry[0]);
    }
    
    /** Set the entries of this object. */
    public void setEntries(Entry[] entryArray) {
        entries = Arrays.asList(entryArray);
    }
    
    /** Is this entry set empty? */
    public boolean isEmpty() {
        return entries == null || entries.size() == 0;
    }
    
    /** This object as a JDOM Document */
    public Document toDocument() {
        Element e = new Element(getType(), NAMESPACE);
        Document doc = new Document(e);
        
        // href
        e.setAttribute(Attributes.HREF, getHref());
        
        // entries
        for (int i = 0; i < getEntries().length; i++) {
            e.addContent(getEntries()[i].toDocument().detachRootElement());
        }
        
        return doc;
    }
   
    public boolean equals(Object o) {
        if ( o == null || o.getClass() != this.getClass()) { 
            return false;        
        }
                
        EntrySet other = (EntrySet)o;
        
        if (!areEqual(getHref(), other.getHref())) {
            return false;
        }
        if (!areEqual(getType(), other.getType())) {
            return false;
        }        
        if (!areEqual(getEntries(), other.getEntries())) {
            return false;
        }
        
        return true;
    }    
}
