/*
 * UserEntrySet.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

package org.roller.presentation.atomadminapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.atomadminapi.EntrySet.Types;

/**
 * This class describes a set of user entries. 
 * @author jtb
 */
class UserEntrySet extends EntrySet {
    /** XML tags that describe a set of user entries. */
    static interface Tags {
        public static final String USERS = "users";
    }       
        
    /** Construct based on an array of Roller UserData objects. */
    public UserEntrySet(UserData[] users, String urlPrefix) {
        if (users == null) {
            throw new NullPointerException("ERROR: Null users not allowed");
        }
        
        List entries = new ArrayList();        
        for (int i = 0; i < users.length; i++) {
            UserData ud = users[i];
            Entry entry = new UserEntry(ud, urlPrefix);
            entries.add(entry);            
        }
        setEntries((Entry[])entries.toArray(new Entry[0]));
        setHref(urlPrefix + "/" + Types.USERS);
    }
    
    /** Construct based on a JDOM Document object. */
    public UserEntrySet(Document d, String urlPrefix) throws Exception {
        Element root = d.getRootElement();
        String rootName = root.getName();
        if (!rootName.equals(Tags.USERS)) {
            throw new Exception("ERROR: Expected root name: " + Tags.USERS + ", root name was: " + rootName);
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
