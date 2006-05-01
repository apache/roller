/*
 * Copyright 2005 David M Johnson (For RSS and Atom In Action)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.presentation.webservices.atomprotocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.Filter;

/**
 * This class models an Atom workspace.
 * @author Dave Johnson
 */
/* Based on: draft-ietf-atompub-protocol-08.txt
 * 
 * appService =
 *    element app:service {
 *       appCommonAttributes,
 *       ( appWorkspace+ 
 *         & extensionElement* )
 *    }
 *
 * Here is an example Atom workspace:
 * 
 * <?xml version="1.0" encoding='utf-8'?>
 * <service xmlns="http://purl.org/atom/app#">
 *   <workspace title="Main Site" >
 *     <collection
 *       title="My Blog Entries"
 *       href="http://example.org/reilly/main" >
 *       <member-type>entry</member-type>
 *     </collection>
 *     <collection
 *       title="Pictures"
 *       href="http://example.org/reilly/pic" >
 *       <member-type>media</member-type>
 *     </collection>
 *   </workspace>
 * </service>
 */
public class AtomService {
    public static final Namespace ns =
            Namespace.getNamespace("http://purl.org/atom/app#");
    
    private List workspaces = new ArrayList();
    
    public AtomService() {
    }
    
    public void addWorkspace(AtomService.Workspace workspace) {
        workspaces.add(workspace);
    }
    
    public List getWorkspaces() {
        return workspaces;
    }
    
    public void setWorkspaces(List workspaces) {
        this.workspaces = workspaces;
    }
    
    /**
     * This class models an Atom workspace.
     *
     * @author Dave Johnson
     */
    /*
     * appWorkspace = element app:workspace { attribute title { text }, (
     * appCollection* & anyElement* ) }
     */
    public static class Workspace {
        private String title       = null;
        private List   collections = new ArrayList();
        
        public Workspace() {
        }
        
        public List getCollections() {
            return collections;
        }
        
        public void setCollections(List collections) {
            this.collections = collections;
        }
        
        public void addCollection(AtomService.Collection col) {
            collections.add(col);
        }
        
        /** Workspace must have a human readable title */
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
    }
    
    /**
     * This class models an Atom workspace collection.    
     * @author Dave Johnson
     */
    /* appCollection =
     *       element app:collection {
     *          appCommonAttributes,
     *          attribute title { text },
     *          attribute href { text },
     *          ( appMemberType
     *            & appListTemplate
     *            & extensionElement* )
     *       }
     */
    public static class Collection {
        private String title = null;
        private String memberType = "entry"; // or "media"
        private String listTemplate = null;
        private String href = null;
        
        public Collection() {
        }
        
        /**
         * Member type May be "entry" or "media".
         */
        public String getMemberType() {
            return memberType;
        }
        
        public void setMemberType(String memberType) {
            this.memberType = memberType;
        }
        
        /** The URI of the collection */
        public String getHref() {
            return href;
        }
        
        public void setHref(String href) {
            this.href = href;
        }
        
        /** Must have human readable title */
        public String getTitle() {
            return title;
        }
        
        public void setTitle(String title) {
            this.title = title;
        }
    }
    
    /** Deserialize an Atom service XML document into an object */
    public static AtomService documentToService(Document document) {
        AtomService service = new AtomService();
        Element root = document.getRootElement();
        List spaces = root.getChildren("workspace", ns);
        Iterator iter = spaces.iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            service.addWorkspace(AtomService.elementToWorkspace(e));
        }
        return service;
    }
    
    /** Serialize an AtomService object into an XML document */
    public static Document serviceToDocument(AtomService service) {
        Document doc = new Document();
        Element root = new Element("service", ns);
        doc.setRootElement(root);
        Iterator iter = service.getWorkspaces().iterator();
        while (iter.hasNext()) {
            AtomService.Workspace space = (AtomService.Workspace) iter.next();
            root.addContent(AtomService.workspaceToElement(space));
        }
        return doc;
    }
    
    /** Deserialize a Atom workspace XML element into an object */
    public static AtomService.Workspace elementToWorkspace(Element element) {
        AtomService.Workspace space = new AtomService.Workspace();
        space.setTitle(element.getAttribute("title").getValue());
        List collections = element.getChildren("collection", ns);
        Iterator iter = collections.iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            space.addCollection(AtomService.elementToCollection(e));
        }
        return space;
    }
    
    /** Serialize an AtomService.Workspace object into an XML element */
    public static Element workspaceToElement(Workspace space) {
        Namespace ns = Namespace.getNamespace("http://purl.org/atom/app#");
        Element element = new Element("workspace", ns);
        element.setAttribute("title", space.getTitle());
        Iterator iter = space.getCollections().iterator();
        while (iter.hasNext()) {
            AtomService.Collection col = (AtomService.Collection) iter.next();
            element.addContent(collectionToElement(col));
        }
        return element;
    }
    
    /** Deserialize an Atom service collection XML element into an object */
    public static AtomService.Collection elementToCollection(Element element) {
        AtomService.Collection collection = new AtomService.Collection();
        collection.setTitle(element.getAttribute("title").getValue());
        collection.setHref(element.getAttribute("href").getValue());
        Element memberType = element.getChild("member-type",  ns);
        if (memberType != null) {
            collection.setMemberType(memberType.getText());
        }
        return collection;
    }
    
    /** Serialize an AtomService.Collection object into an XML element */
    public static Element collectionToElement(AtomService.Collection collection) {
        Namespace ns = Namespace.getNamespace("http://purl.org/atom/app#");
        Element element = new Element("collection", ns);
        element.setAttribute("title", collection.getTitle());
        element.setAttribute("href", collection.getHref());
        
        Element memberType = new Element("member-type", ns);
        memberType.setText(collection.getMemberType());
        element.addContent(memberType);
        
        return element;
    }
}