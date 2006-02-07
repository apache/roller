/*
 * EntrySet.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

package org.roller.presentation.atomadminapi;

import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import org.jdom.Document;
import org.jdom.Element;

/**
 * This class is the abstract notion of a set of entries.
 * Weblog resources are represented by sets of entries. 
 *
 * @author jtb
 */
abstract class EntrySet extends Entry {
    /** Entry set types. */
    static interface Types {
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
          * for the AAPP service.
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
}
