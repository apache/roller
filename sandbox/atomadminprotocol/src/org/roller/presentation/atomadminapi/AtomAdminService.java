/*
 * AtomAdminService.java
 *
 * Created on January 17, 2006, 12:44 PM
 */
package org.roller.presentation.atomadminapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;

/**
 * This class describes an AAPP introspection document.
 * An AtomAdminService is a set of workspaces, which is a set of
 * collections.
 *
 * @author jtb
 */

class AtomAdminService extends EntrySet {
    static class Workspace extends EntrySet {
        public static interface Attributes {
            public static final String TITLE = "title";
        }
        
        private String title = null;
        
        public Workspace() {
        }
        
        public String getType() {
            return Types.WORKSPACE;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        
        public Document toDocument() {
            Document doc = new Document();
            Element element = new Element(EntrySet.Types.WORKSPACE, NAMESPACE);
            doc.setRootElement(element);
            
            element.setAttribute(Attributes.TITLE, getTitle());
            for (int i = 0; i < getEntries().length; i++) {
                Entry entry = getEntries()[i];
                element.addContent(entry.toDocument().detachRootElement());
            }
            
            return doc;
        }
    }
    
    public static class WorkspaceCollection extends Entry {
        public static interface Tags {
            public static final String MEMBER_TYPE = "member-type";
        }
        
        public static interface Attributes {
            public static final String TITLE = "title";
        }
        
        private String title;
        private String memberType;
        
        public WorkspaceCollection() {
        }
        
        public String getType() {
            return Types.COLLECTION;
        }
        
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
        
        
        public Document toDocument() {
            Document doc = new Document();
            Element element = new Element(Types.COLLECTION, NAMESPACE);
            doc.setRootElement(element);
            
            element.setAttribute(Attributes.TITLE, getTitle());
            element.setAttribute(Entry.Attributes.HREF, getHref());
            
            Element memberType = new Element(Tags.MEMBER_TYPE, NAMESPACE);
            memberType.setText(getMemberType());
            element.addContent(memberType);
            
            return doc;
        }
        
        public String getMemberType() {
            return memberType;
        }
        
        public void setMemberType(String memberType) {
            this.memberType = memberType;
        }
        
    }
    
    public AtomAdminService(String href) {
        setHref(href);
    }
    
    public String getType() {
        return Types.SERVICE;
    }
    
    public Document toDocument() {
        Document doc = new Document();
        Element root = new Element(Types.SERVICE, NAMESPACE);
        doc.setRootElement(root);
        
        for (int i = 0; i < getEntries().length; i++) {
            Entry entry = getEntries()[i];
            root.addContent(entry.toDocument().detachRootElement());
        }
        
        return doc;
    }
}
