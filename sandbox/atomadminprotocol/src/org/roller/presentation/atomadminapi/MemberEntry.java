/*
 * MemberEntry.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

package org.roller.presentation.atomadminapi;

import java.util.Date;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.presentation.atomadminapi.Entry.Attributes;
import org.roller.presentation.atomadminapi.Entry.Types;

/**
 *
 * @author jtb
 */
class MemberEntry extends Entry {
    static interface Tags {
        public static final String MEMBER = "member";
        public static final String NAME = "name";
        public static final String HANDLE = "handle";
        public static final String PERMISSION = "permission";
    }     
    
    private String name;
    private String handle;
    private String permission;
    
    public MemberEntry(Element e, String urlPrefix) throws Exception {
        // name
        Element nameElement = e.getChild(Tags.NAME, NAMESPACE);
        if (nameElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.NAME);
        }
        setName(nameElement.getText());
        
        String href = urlPrefix + "/" + EntrySet.Types.MEMBERS + "/" + getHandle() + "/" + getName();
        setHref(href);
        
        // full name
        Element handleElement = e.getChild(Tags.HANDLE, NAMESPACE);
        if (handleElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.HANDLE);
        }
        setHandle(handleElement.getText());
        
        // password
        Element permissionElement = e.getChild(Tags.PERMISSION, NAMESPACE);
        if (permissionElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.PERMISSION);
        }
        setPermission(permissionElement.getText());                
    }
    
    public MemberEntry(PermissionsData pd, String urlPrefix) {
        String href = urlPrefix + "/" + EntrySet.Types.MEMBERS + "/" + pd.getWebsite().getHandle() + "/" + pd.getUser().getUserName();
        
        setHref(href);
        setName(pd.getUser().getUserName());
        setHandle(pd.getWebsite().getHandle());
        setPermission(maskToString(pd.getPermissionMask()));        
    }
    
    private static String maskToString(short mask) {
        if (mask == PermissionsData.ADMIN) {
            return "ADMIN";
        }
        if (mask == PermissionsData.AUTHOR) {
            return "AUTHOR";
        }
        if (mask == PermissionsData.LIMITED) {
            return "LIMITED";
        }
        return null;
    }

    
    private static short stringToMask(String s) {
        if (s.equalsIgnoreCase("ADMIN")) {
            return PermissionsData.ADMIN;
        }
        if (s.equalsIgnoreCase("AUTHOR")) {
            return PermissionsData.AUTHOR;
        }
        if (s.equalsIgnoreCase("LIMITED")) {
            return PermissionsData.LIMITED;
        }
        return 0;
    }
    
    public String getType() {
        return Types.MEMBER;
    }
    
    public Document toDocument() {
        Element member = new Element(Tags.MEMBER, NAMESPACE);
        Document doc = new Document(member);
        
        // href
        member.setAttribute(Attributes.HREF, getHref());
               
        // name
        Element name = new Element(Tags.NAME, AtomAdminService.NAMESPACE);
        Text nameText = new Text(getName());
        name.addContent(nameText);
        member.addContent(name);
       
        // handle
        Element handle = new Element(Tags.HANDLE, NAMESPACE);
        Text handleText = new Text(getHandle());
        handle.addContent(handleText);
        member.addContent(handle);
        
        // permission
        Element permission = new Element(Tags.PERMISSION, NAMESPACE);
        Text permissionText = new Text(getPermission());
        permission.addContent(permissionText);
        member.addContent(permission);
                
        return doc;
    }
            

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getPermission() {
        return permission;
    }
    
    public short getPermissionMask() {
        return stringToMask(getPermission());
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
