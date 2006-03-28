/*
 * MemberEntry.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

package org.roller.presentation.atomadminapi.sdk;

import java.io.InputStream;
import java.io.IOException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.jdom.JDOMException;
import org.roller.presentation.atomadminapi.sdk.Entry.Attributes;
import org.roller.presentation.atomadminapi.sdk.Entry.Types;

/**
 * This class describes a member entry. 
 * A member entry is a triple consisting of a user name, a weblog handle,
 * and a permission.
 */
public class MemberEntry extends Entry {
    /** Member permissions */
    public interface Permissions {
        public static final String ADMIN = "ADMIN";
        public static final String AUTHOR = "AUTHOR";
        public static final String LIMITED = "LIMITED";
    }
    
    static interface Tags {
        public static final String MEMBER = "member";
        public static final String NAME = "name";
        public static final String HANDLE = "handle";
        public static final String PERMISSION = "permission";
    }     
    
    private String name;
    private String handle;
    private String permission;
    
    public MemberEntry(Element e, String urlPrefix) throws MissingElementException {
        populate(e, urlPrefix);
    }
    
    public MemberEntry(String handle, String userName, String urlPrefix) {
        String href = urlPrefix + "/" + EntrySet.Types.MEMBERS + "/" + handle + "/" + userName;       
	setHref(href);
        setHandle(handle);
        setName(userName);
    }

    public MemberEntry(InputStream stream, String urlPrefix) throws JDOMException, IOException, MissingElementException {               
        SAXBuilder sb = new SAXBuilder();
        Document d = sb.build(stream);
        Element e = d.detachRootElement();
        
        populate(e, urlPrefix);        
    }

    private void populate(Element e, String urlPrefix) throws MissingElementException {
        // name
        Element nameElement = e.getChild(Tags.NAME, NAMESPACE);
        if (nameElement == null) {
            throw new MissingElementException("ERROR: Missing element", e.getName(), Tags.NAME);
        }
        setName(nameElement.getText());
                
        // handle
        Element handleElement = e.getChild(Tags.HANDLE, NAMESPACE);
        if (handleElement == null) {
            throw new MissingElementException("ERROR: Missing element", e.getName(), Tags.HANDLE);
        }
        setHandle(handleElement.getText());

        // href
        setHref(urlPrefix + "/" + EntrySet.Types.MEMBERS + "/" + getHandle() + "/" + getName()); 
        
        // password
        Element permissionElement = e.getChild(Tags.PERMISSION, NAMESPACE);
        if (permissionElement == null) {
            throw new MissingElementException("ERROR: Missing element", e.getName(), Tags.PERMISSION);
        }
        setPermission(permissionElement.getText());
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
        Element name = new Element(Tags.NAME, Service.NAMESPACE);
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
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
}
