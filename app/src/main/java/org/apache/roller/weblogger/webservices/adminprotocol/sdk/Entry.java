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
package org.apache.roller.weblogger.webservices.adminprotocol.sdk;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import org.jdom.Document;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * This class is the abstract notion of an entry.
 * Weblog resources are represented by sets of entries.
 */
public abstract class Entry {
    protected static final Namespace NAMESPACE = Namespace.getNamespace("http://purl.org/apache/roller/rap#");
    
    /** Entry types. */
    public interface Types {
        /** 
         * User entry.
         * A user entry is contained within a user entry set.
         */
        String USER = "user";
        /**
         * Weblog entry.
         * A weblog entry is contained within a weblog entry set.
         */
        String WEBLOG = "weblog";
        /**
         * Member entry.
         * A member entry is contained within a member entry set.
         */
        String MEMBER = "member";
        /**
         * Collection entry.
         * A collection entry is contained within a workspace, which is
         * contained within a service.
         */
        String COLLECTION = "collection";
    }
    
    /** XML attributes common to all entry types. */
    protected interface Attributes {
        String HREF = "href";
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
    
    public boolean equals(Object o) {
        if ( o == null || o.getClass() != this.getClass()) { 
            return false;        
        }
                
        Entry other = (Entry)o;
        
        if (!areEqual(getHref(), other.getHref())) {
            return false;
        }
        if (!areEqual(getType(), other.getType())) {
            return false;
        }
        
        return true;
    }
    
    protected static boolean areEqual(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }
    
    protected static boolean areEqual(Object[] oa1, Object[] oa2) {
        return oa1 == null ? oa2 == null : Arrays.equals(oa1, oa2);
    }    
}
