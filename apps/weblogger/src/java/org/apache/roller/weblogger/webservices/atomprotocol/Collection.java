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
package org.apache.roller.weblogger.webservices.atomprotocol;

import com.sun.syndication.feed.atom.Category;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;


/**
 * This class models an Atom workspace collection.    
 *//* 
	appCollection =
	   element app:collection {
   appCommonAttributes,
   attribute href { atomURI  },
   ( appAccept?	
     & appCategories*
     & extensionElement* )
	   }
 */
public class Collection {
    private String title = null;
    private String titleType = null; // may be TEXT, HTML, XHTML
    private String accept = "entry";
    private String listTemplate = null;
    private String href = null;
    private List categories = new ArrayList(); // of Categories objects    
    
    /**
     * Collection MUST have title and href.
     * @param title    Title for collection
     * @param typeType Content type of title (null for plain text)
     * @param href     Collection URI.
     */
    public Collection(String title, String titleType, String href) {
        this.title = title;
        this.titleType = titleType;
        this.href = href;
    }
    
    /**
     * Comma separated list of media-ranges accepted by collection.
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
    
    public List getCategories() {
        return categories;
    }
    
    /**
     * Returns true if contentType is accepted by collection.
     */
    public boolean accepts(String ct) {
        if (accept != null && accept.trim().equals("*/*")) return true;
        String entryType = "application/atom+xml";
        boolean entry = entryType.equals(ct);
        if (entry && null == accept) {
            return true;
        } else if (entry && "entry".equals(accept)) {
            return true;
        } else if (entry && entryType.equals(accept)) {
            return true;
        } else {
            String[] rules = accept.split(",");
            for (int i=0; i<rules.length; i++) {
                String rule = rules[i].trim();
                if (rule.equals(ct)) return true;
                int slashstar = rule.indexOf("/*");
                if (slashstar > 0) {
                    rule = rule.substring(0, slashstar + 1);
                    if (ct.startsWith(rule)) return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Serialize an AtomService.Collection into an XML element
     */
    public static Element collectionToElement(Collection collection) {
        Element element = new Element("collection", AtomService.ATOM_PROTOCOL);
        element.setAttribute("href", collection.getHref());
                       
        Element title = new Element("title", AtomService.ATOM_FORMAT);
        title.setText(collection.getTitle());
        element.addContent(title);
        if (collection.getTitleType() != null && !collection.getTitleType().equals("TEXT")) {
            element.setAttribute("type", collection.getTitleType()); //, AtomService.ATOM_FORMAT);
        }
                    
        // Loop to create <app:categories> elements            
        for (Iterator it = collection.getCategories().iterator(); it.hasNext();) {
            Categories cats = (Categories)it.next();
            Element catsElem = new Element("categories", AtomService.ATOM_PROTOCOL);
            catsElem.setAttribute("fixed", cats.isFixed() ? "yes" : "no"); //, AtomService.ATOM_PROTOCOL);
            if (cats.getScheme() != null) {
                catsElem.setAttribute("scheme", cats.getScheme()); //, AtomService.ATOM_PROTOCOL);
            }
            // Loop to create <atom:category> elements
            for (Iterator catIter = cats.getCategories().iterator(); catIter.hasNext();) {
                Category cat = (Category) catIter.next();
                Element catElem = new Element("category", AtomService.ATOM_FORMAT);
                catElem.setAttribute("term", cat.getTerm()); //, AtomService.ATOM_FORMAT);
                if (cat.getScheme() != null) { // optional
                    catElem.setAttribute("scheme", cat.getScheme()); //, AtomService.ATOM_FORMAT);
                }
                if (cat.getLabel() != null) { // optional
                    catElem.setAttribute("label", cat.getLabel()); //, AtomService.ATOM_FORMAT);
                }
                catsElem.addContent(catElem);
            }
            element.addContent(catsElem);
        }
        
        Element memberType = new Element("accept", AtomService.ATOM_PROTOCOL);
        memberType.setText(collection.getAccept());
        element.addContent(memberType);
        
        return element;
    }
    
    /** Deserialize an Atom service collection XML element into an object */
    public static Collection elementToCollection(Element element) {
        String newHref = element.getAttribute("href").getValue();
        Element titleElem = element.getChild("title", AtomService.ATOM_FORMAT);
        String newTitle = titleElem.getText();
        String newType = null;
        if (titleElem.getAttribute("type", AtomService.ATOM_FORMAT) != null) {
            newType = titleElem.getAttribute("type", AtomService.ATOM_FORMAT).getValue();
        }
        Collection collection = new Collection(newTitle, newType, newHref);
                
        Element memberType = element.getChild("accept",  AtomService.ATOM_PROTOCOL);
        if (memberType != null) {
            collection.setAccept(memberType.getText());
        }
        
        // Loop to parse <app:categories> element to Categories objects
        List catsElems = element.getChildren("categories", AtomService.ATOM_PROTOCOL);
        for (Iterator catsIter = catsElems.iterator(); catsIter.hasNext();) {
            Element catsElem = (Element) catsIter.next();  
            Categories cats = new Categories();
            if ("yes".equals(catsElem.getAttribute("fixed", AtomService.ATOM_PROTOCOL))) {
                cats.setFixed(true);
            }
            // Loop to parse <atom:category> elemenents to Category objects
            List catElems = catsElem.getChildren("category", AtomService.ATOM_FORMAT);
            for (Iterator catIter = catElems.iterator(); catIter.hasNext();) {                
                Element catElem = (Element) catIter.next();
                Category cat = new Category();
                cat.setTerm(catElem.getAttributeValue("term"));                
                cat.setLabel(catElem.getAttributeValue("label")); 
                cat.setScheme(catElem.getAttributeValue("scheme"));
                cats.addCategory(cat);
            }
            collection.addCategories(cats);
        }
        return collection;
    }

}
