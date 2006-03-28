/*
 * UserEntrySet.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

package org.roller.presentation.atomadminapi.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.roller.presentation.atomadminapi.sdk.EntrySet.Types;

/**
 * This class describes a set of user entries. 
 * @author jtb
 */
public class UserEntrySet extends EntrySet {
    /** XML tags that describe a set of user entries. */
    private static interface Tags {
        public static final String USERS = "users";
    }       
        
    /** Construct based on an array of Roller UserData objects. */
    public UserEntrySet(String urlPrefix) {
        setHref(urlPrefix + "/" + Types.USERS);
    }
    
    /** Construct based on a JDOM Document object. */
    public UserEntrySet(Document d, String urlPrefix) throws MissingElementException, UnexpectedRootElementException {
        populate(d, urlPrefix);
    }
    
    public UserEntrySet(InputStream stream, String urlPrefix) throws JDOMException, IOException, MissingElementException, UnexpectedRootElementException {               
        SAXBuilder sb = new SAXBuilder();
        Document d = sb.build(stream);

        populate(d, urlPrefix);        
    }        
    
    private void populate(Document d, String urlPrefix) throws MissingElementException, UnexpectedRootElementException {
        Element root = d.getRootElement();
        String rootName = root.getName();
        if (!rootName.equals(Tags.USERS)) {
            throw new UnexpectedRootElementException("ERROR: Unexpected root element", Tags.USERS, rootName);
        }
        List users = root.getChildren(UserEntry.Tags.USER, NAMESPACE);
        if (users != null) {
            List entries = new ArrayList();
            for (Iterator i = users.iterator(); i.hasNext(); ) {
                Element user = (Element)i.next();
                UserEntry entry = new UserEntry(user, urlPrefix);
                entries.add(entry);
            }
            setEntries((Entry[])entries.toArray(new Entry[0]));
        }
        setHref(urlPrefix + "/" + Types.USERS);
    }    
        
    public String getType() {
        return Types.USERS;
    }    
}
