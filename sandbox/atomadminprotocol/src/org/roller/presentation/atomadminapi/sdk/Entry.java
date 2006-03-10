/*
 * Entry.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

package org.roller.presentation.atomadminapi.sdk;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.jdom.Document;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * This class is the abstract notion of an entry.
 * Weblog resources are represented by sets of entries.
 *
 * @author jtb
 */
public abstract class Entry {
    protected static final Namespace NAMESPACE = Namespace.getNamespace("http://purl.org/atom/aapp#");
    
    /** Entry types. */
    public static interface Types {
        /** 
         * User entry.
         * A user entry is contained within a user entry set.
         */
        public static final String USER = "user";
        /**
         * Weblog entry.
         * A weblog entry is contained within a weblog entry set.
         */
        public static final String WEBLOG = "weblog";
        /**
         * Member entry.
         * A member entry is contained within a member entry set.
         */
        public static final String MEMBER = "member";
        /**
         * Collection entry.
         * A collection entry is contained within a workspace, which is
         * contained within a service.
         */
        public static final String COLLECTION = "collection";
    }
    
    /** XML attributes common to all entry types. */
    protected static interface Attributes {
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
    
    /** 
     * This entry, as a String (XML).
     */
    public String toString() {
        Writer writer = new StringWriter();
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        try {
            outputter.output(toDocument(), writer);
            writer.close();
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe.getMessage());
        }
        
        return writer.toString();
    }
    
    public abstract String getType();
}
