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
import java.util.List;
import org.jdom.Element;


/**
 * This class models an Atom workspace.
 * @author Dave Johnson
 *//*
	appWorkspace =
	   element app:workspace {
   appCommonAttributes,
   ( appCollection*
     & extensionElement* )
	   }
	atomTitle = element atom:title { atomTextConstruct }
 */
public class Workspace {
    private String title = null;
    private String titleType = null; // may be TEXT, HTML, XHTML
    private List collections = new ArrayList(); 
    
    /**
     * Collection MUST have title.
     * @param title    Title for collection
     * @param typeType Content type of title (null for plain text)
     */
    public Workspace(String title, String titleType) {
        this.title = title;
        this.titleType = titleType;
    }
    
    /** Iterate over collections in workspace */
    public List getCollections() {
        return collections;
    }
    
    /** Add new collection to workspace */
    public void addCollection(Collection col) {
        collections.add(col);
    }
    
    /**
     * DefaultWorkspace must have a human readable title
     */
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
    
    public Collection findCollection(String title, String contentType) {
        for (Iterator it = collections.iterator(); it.hasNext();) {
            Collection col = (Collection) it.next();
            if (title != null && col.accepts(contentType)) {
                return col;
            } else if (col.accepts(contentType)) {
                return col;
            }
        }
        return null;
    }
        
    /** Deserialize a Atom workspace XML element into an object */
    public static Workspace elementToWorkspace(Element element) {  
        Element titleElem = element.getChild("title", AtomService.ATOM_FORMAT);        
        String newTitle = titleElem.getText();
        String newType = null;        
        if (titleElem.getAttribute("type", AtomService.ATOM_FORMAT) != null) {
            newType = titleElem.getAttribute("type", AtomService.ATOM_FORMAT).getValue();
        }
        Workspace space = new Workspace(newTitle, newType);
        List collections = element.getChildren("collection", AtomService.ATOM_PROTOCOL);
        Iterator iter = collections.iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            space.addCollection(Collection.elementToCollection(e));
        }
        return space;
    }
    
    /**
     * Serialize an AtomService.DefaultWorkspace object into an XML element
     */
    public static Element workspaceToElement(Workspace space) {
        Element element = new Element("workspace", AtomService.ATOM_PROTOCOL);
        
        Element title = new Element("title", AtomService.ATOM_FORMAT);
        title.setText(space.getTitle());
        element.addContent(title);
        if (space.getTitleType() != null && !space.getTitleType().equals("TEXT")) {
            element.setAttribute("type", space.getTitleType(), AtomService.ATOM_FORMAT);
        }
        
        Iterator iter = space.getCollections().iterator();
        while (iter.hasNext()) {
            Collection col = (Collection) iter.next();
            element.addContent(Collection.collectionToElement(col));
        }
        return element;
    }

}
