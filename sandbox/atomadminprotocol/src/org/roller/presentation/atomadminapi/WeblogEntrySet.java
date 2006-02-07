/*
 * WeblogEntrySet.java
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
 * This class describes a set of weblog entries. 
 * @author jtb
 */
class WeblogEntrySet extends EntrySet {
    static interface Tags {
        public static final String WEBLOGS = "weblogs";
    }      
    
    public WeblogEntrySet(UserData[] users, String urlPrefix) {
        if (users == null) {
            throw new NullPointerException("null users not allowed");
        }
        
        List entries = new ArrayList();        
        for (int i = 0; i < users.length; i++) {
            UserData ud = users[i];
            List permissions = ud.getPermissions();
            for (Iterator j = permissions.iterator(); j.hasNext(); ) {
                PermissionsData pd = (PermissionsData)j.next();
                WebsiteData wd = pd.getWebsite();
                WeblogEntry entry = new WeblogEntry(wd, urlPrefix);
                entries.add(entry);
            }
        }
        setEntries((Entry[])entries.toArray(new Entry[0]));
        setHref(urlPrefix + "/" + Types.WEBLOGS);
    }
    
    public WeblogEntrySet(Document d, String urlPrefix) throws Exception {
        Element root = d.getRootElement();
        String rootName = root.getName();
        if (!rootName.equals(Tags.WEBLOGS)) {
            throw new Exception("ERROR: Expected root name: " + Tags.WEBLOGS + ", root name was: " + rootName);
        }
        List weblogs = root.getChildren(WeblogEntry.Tags.WEBLOG, AtomAdminService.NAMESPACE);
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
    
    public WeblogEntrySet(WebsiteData[] wds, String urlPrefix) {
        if (wds == null) {
            throw new NullPointerException("null website datas not allowed");
        }
        
        List entries = new ArrayList();
        for (int i = 0; i < wds.length; i++) {
            WeblogEntry entry = new WeblogEntry(wds[i], urlPrefix);
            entries.add(entry);
        }
        setEntries((Entry[])entries.toArray(new Entry[0]));
        setHref(urlPrefix + "/" + Types.WEBLOGS);
    }
    
    public String getType() {
        return Types.WEBLOGS;
    }    
}
