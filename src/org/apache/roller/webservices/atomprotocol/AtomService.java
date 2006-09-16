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
package org.apache.roller.webservices.atomprotocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * This class models an Atom Ublishing Protocol Service Document.
 * <p />
 * Based on: draft-ietf-atompub-protocol-10.txt
 * <p />
 * Designed to be Roller independent.
 *//* 
 * appService =
 *    element app:service {
 *       appCommonAttributes,
 *       ( appWorkspace+ 
 *         & extensionElement* )
 *    }
 *
 * <?xml version="1.0" encoding='utf-8'?>
 * <service xmlns="http://purl.org/atom/app#">
 *   <workspace title="Main Site" >
 *     <collection
 *       title="My Blog Entries"
 *       href="http://example.org/reilly/main" >
 *       <accept>entry</accept>
 *     </collection>
 *     <collection
 *       title="Pictures"
 *       href="http://example.org/reilly/pic" >
 *       <accept>*</accept>
 *     </collection>
 *   </workspace>
 * </service>
 */
public class AtomService {
    public static final Namespace ns =
            Namespace.getNamespace("app","http://purl.org/atom/app#");
    public static final Namespace atomns =
            Namespace.getNamespace("atom","http://www.w3.org/2005/atom");

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
     * @author Dave Johnson
     *//*
     * appWorkspace = element app:workspace { attribute title { text }, (
     * appCollection* & anyElement* ) }
     */
    public static class Workspace {
        private String title       = null;
        private String titleType   = null; // may be TEXT, HTML, XHTML
        private Set    collections = new LinkedHashSet();
        
        public Workspace() {
        }
        
        /** Iterate over collections in workspace */
        public Iterator getCollections() {
            return collections.iterator();
        }
        
        /** Add new collection to workspace */
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

        public String getTitleType() {
            return titleType;
        }

        public void setTitleType(String titleType) {
            this.titleType = titleType;
        }
    }
    
    /**
     * This class models an Atom workspace collection.    
     * @author Dave Johnson
     *//* 
     * appCollection =
     *       element app:collection {
     *          appCommonAttributes,
     *          attribute title { text },
     *          attribute href { text },
     *          ( appAccept?
     *            & extensionElement* )
     *       }
     */
    public static class Collection {
        private String title = null;
        private String titleType =null; // may be TEXT, HTML, XHTML
        private String accept = "entry";
        private String listTemplate = null;
        private String href = null;
        private Set categories = new LinkedHashSet(); // of Categories objects
        
        public Collection() {
        }
        
        /**
         * Comma separated list of media-ranges accepted by collection
         * or "entry" to indicate Atom entries only. Leaving null will 
         * default to entries only.
         */
        public String getAccept() {
            return accept;
        }
        
        public void setAccept(String accept) {
            this.accept = accept;
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

        public String getTitleType() {
            return titleType;
        }

        public void setTitleType(String titleType) {
            this.titleType = titleType;
        }

        /** Workspace can have multiple Categories objects */
        public void addCategories(Categories cats) {
            categories.add(cats);
        }
        
        /** Iterate over Categories objects in Collection */
        public Iterator getCategories() {
            return categories.iterator();
        }
    }
    
    /** Categories object contains Category objects */
    public static class Categories {
        private Set categories = new LinkedHashSet(); // of Category objects
        private String scheme = null;
        private boolean fixed = false;
        
        /** Add category list of those specified*/
        public void addCategory(Category cat) {
            categories.add(cat);
        }
        
        /** Iterate over Category objects */
        public Iterator getCategories() {
            return categories.iterator();
        }

        /** True if clients MUST use one of the categories specified */
        public boolean isFixed() {
            return fixed;
        }

        /** True if clients MUST use one of the categories specified */
        public void setFixed(boolean fixed) {
            this.fixed = fixed;
        }

        /** Category URI scheme to use for Categories without a scheme */
        public String getScheme() {
            return scheme;
        }

        /** Category URI scheme to use for Categories without a scheme */
        public void setScheme(String scheme) {
            this.scheme = scheme;
        }        
    }
    
      
    /** Represents an <atom:category> object */
    public static class Category {
        private String term;
        private String scheme;
        private String label;                

        public String getTerm() {
            return term;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
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
        
        Element titleElem = element.getChild("title", atomns);
        space.setTitle(titleElem.getText());
        if (titleElem.getAttribute("type", atomns) != null) {
            space.setTitleType(titleElem.getAttribute("type", atomns).getValue());
        }
        
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
        Element element = new Element("workspace", ns);
        
        Element title = new Element("title", atomns);
        title.setText(space.getTitle());
        element.addContent(title);
        if (space.getTitleType() != null && !space.getTitleType().equals("TEXT")) {
            element.setAttribute("type", space.getTitleType(), atomns);
        }
        
        Iterator iter = space.getCollections();
        while (iter.hasNext()) {
            AtomService.Collection col = (AtomService.Collection) iter.next();
            element.addContent(collectionToElement(col));
        }
        return element;
    }
    
    /** Deserialize an Atom service collection XML element into an object */
    public static AtomService.Collection elementToCollection(Element element) {
        AtomService.Collection collection = new AtomService.Collection();
        collection.setHref(element.getAttribute("href").getValue());
        
        Element titleElem = element.getChild("title", atomns);
        if (titleElem != null) {
            collection.setTitle(titleElem.getText());
            if (titleElem.getAttribute("type", atomns) != null) {
                collection.setTitleType(titleElem.getAttribute("type", atomns).getValue());
            }
        }
                
        Element memberType = element.getChild("accept",  ns);
        if (memberType != null) {
            collection.setAccept(memberType.getText());
        }
        
        // Loop to parse <app:categories> element to Categories objects
        List catsElems = element.getChildren("categories", ns);
        for (Iterator catsIter = catsElems.iterator(); catsIter.hasNext();) {
            Element catsElem = (Element) catsIter.next();  
            Categories cats = new Categories();
            if ("yes".equals(catsElem.getAttribute("fixed", ns))) {
                cats.setFixed(true);
            }
            // Loop to parse <atom:category> elemenents to Category objects
            List catElems = catsElem.getChildren("category", atomns);
            for (Iterator catIter = catElems.iterator(); catIter.hasNext();) {                
                Element catElem = (Element) catIter.next();
                Category cat = new Category();
                cat.setTerm(catElem.getAttributeValue("term", atomns));                
                cat.setLabel(catElem.getAttributeValue("label", atomns)); 
                cat.setScheme(catElem.getAttributeValue("scheme", atomns));
                cats.addCategory(cat);
            }
            collection.addCategories(cats);
        }
        return collection;
    }
    
    /** Serialize an AtomService.Collection object into an XML element */
    public static Element collectionToElement(AtomService.Collection collection) {
        Element element = new Element("collection", ns);
        element.setAttribute("href", collection.getHref());
                       
        Element title = new Element("title", atomns);
        title.setText(collection.getTitle());
        element.addContent(title);
        if (collection.getTitleType() != null && !collection.getTitleType().equals("TEXT")) {
            element.setAttribute("type", collection.getTitleType(), atomns);
        }
                    
        // Loop to create <app:categories> elements            
        for (Iterator it = collection.getCategories(); it.hasNext();) {
            Categories cats = (Categories)it.next();
            Element catsElem = new Element("categories", ns);
            catsElem.setAttribute("fixed", cats.isFixed() ? "yes" : "no", ns);
            if (cats.getScheme() != null) {
                catsElem.setAttribute("scheme", cats.getScheme(), ns);
            }
            // Loop to create <atom:category> elements
            for (Iterator catIter = cats.getCategories(); catIter.hasNext();) {
                Category cat = (Category) catIter.next();
                Element catElem = new Element("category", atomns);
                catElem.setAttribute("term", cat.getTerm(), atomns);
                if (cat.getScheme() != null) { // optional
                    catElem.setAttribute("scheme", cat.getScheme(), atomns);
                }
                if (cat.getLabel() != null) { // optional
                    catElem.setAttribute("label", cat.getLabel(), atomns);
                }
                catsElem.addContent(catElem);
            }
            element.addContent(catsElem);
        }
        
        Element memberType = new Element("accept", ns);
        memberType.setText(collection.getAccept());
        element.addContent(memberType);
        
        return element;
    }
}

