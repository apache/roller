/*
 * WeblogEntrySet.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

package org.roller.presentation.atomadminapi.sdk;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.roller.presentation.atomadminapi.sdk.EntrySet.Types;

/**
 * This class describes a set of member entries. 
 *
 * @author jtb
 */
public class MemberEntrySet extends EntrySet {
    static interface Tags {
        public static final String MEMBERS = "members";
    }       
        
    public MemberEntrySet(String urlPrefix) {
        setHref(urlPrefix + "/" + Types.MEMBERS);        
    }    

    public MemberEntrySet(Document d, String urlPrefix) throws Exception {
        populate(d, urlPrefix);
    }
    
    public MemberEntrySet(InputStream stream, String urlPrefix) throws Exception {               
        SAXBuilder sb = new SAXBuilder();
        Document d = sb.build(stream);

        populate(d, urlPrefix);        
    }    
    
    private void populate(Document d, String urlPrefix) throws Exception {
        Element root = d.getRootElement();
        String rootName = root.getName();
        if (!rootName.equals(Tags.MEMBERS)) {
            throw new Exception("ERROR: Expected root name: " + Tags.MEMBERS + ", root name was: " + rootName);
        }
        List members = root.getChildren(MemberEntry.Tags.MEMBER, NAMESPACE);
        if (members != null) {
            List entries = new ArrayList();
            for (Iterator i = members.iterator(); i.hasNext(); ) {
                Element member = (Element)i.next();
                MemberEntry entry = new MemberEntry(member, urlPrefix);
                entries.add(entry);
            }
            setEntries((Entry[])entries.toArray(new Entry[0]));
        }
        setHref(urlPrefix + "/" + Types.MEMBERS);
    }
        
    public String getType() {
        return Types.MEMBERS;
    }    
}
