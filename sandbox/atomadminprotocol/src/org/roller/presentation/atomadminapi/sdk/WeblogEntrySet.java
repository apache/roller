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
 * This class describes a set of weblog entries. 
 * @author jtb
 */
public class WeblogEntrySet extends EntrySet {
    static interface Tags {
        public static final String WEBLOGS = "weblogs";
    }      
    
    public WeblogEntrySet(String urlPrefix) {
        setHref(urlPrefix + "/" + Types.WEBLOGS);
    }
    
    public WeblogEntrySet(Document d, String urlPrefix) throws Exception {
        populate(d, urlPrefix);
    }
    
    public WeblogEntrySet(InputStream stream, String urlPrefix) throws Exception {               
        SAXBuilder sb = new SAXBuilder();
        Document d = sb.build(stream);

        populate(d, urlPrefix);        
    }    
    
    private void populate(Document d, String urlPrefix) throws Exception {
        Element root = d.getRootElement();
        String rootName = root.getName();
        if (!rootName.equals(Tags.WEBLOGS)) {
            throw new Exception("ERROR: Expected root name: " + Tags.WEBLOGS + ", root name was: " + rootName);
        }
        List weblogs = root.getChildren(WeblogEntry.Tags.WEBLOG, Service.NAMESPACE);
        if (weblogs != null) {
            List entries = new ArrayList();
            for (Iterator i = weblogs.iterator(); i.hasNext(); ) {
                Element weblog = (Element)i.next();
                WeblogEntry entry = new WeblogEntry(weblog, urlPrefix);
                entries.add(entry);
            }
            setEntries((Entry[])entries.toArray(new Entry[0]));
        }
        setHref(urlPrefix + "/" + Types.WEBLOGS);
    }    
       
    public String getType() {
        return Types.WEBLOGS;
    }    
}
