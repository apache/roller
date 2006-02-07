/*
 * Entry.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

package org.roller.presentation.atomadminapi;

import org.jdom.Document;
import org.jdom.Namespace;

/**
 * This class is the abstract notion of an entry.
 * Weblog resources are represented by sets of entries. 
 *
 * @author jtb
 */
abstract class Entry {
    protected static final Namespace NAMESPACE = Namespace.getNamespace("http://purl.org/atom/aapp#");   
    
    /** Entry types. */
    static interface Types {
        public static final String USER = "user";
        public static final String WEBLOG = "weblog";
        public static final String MEMBER = "member";
        public static final String COLLECTION = "collection";
    }    
       
    /** XML attributes common to all entry types. */
    public static interface Attributes {
        public static final String HREF = "href";
    }     
        
    private String href = null;    
    
    /** Get the HREF that identifies this entry. */
    public String getHref() {
        return href;
    }

    /** Set the HREF that identifies this entry. */
    public void setHref(String href) {
        this.href = href;
    }    
    
    /** This entry, as a JDOM Document object. */
    public abstract Document toDocument();    
}
