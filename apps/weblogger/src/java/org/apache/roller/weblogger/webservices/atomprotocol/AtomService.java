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


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * This class models an Atom Publishing Protocol AtomService Document.
 * <p />
 * Based on: draft-ietf-atompub-protocol-10.txt
 * <p />
 * Designed to be Roller independent.
 *//* 

	namespace app = "http://purl.org/atom/app#"
	start = appService
	             
	appService =
	   element app:service {
	      appCommonAttributes,
	      ( appWorkspace+
	        & extensionElement* )
	   }
	   
	For example:
	 
	<?xml version="1.0" encoding='utf-8'?>
	<service xmlns="http://purl.org/atom/app#"
	         xmlns:atom="http://www.w3.org/2005/Atom">
	  <workspace>
	    <atom:title>Main Site</atom:title>
	    <collection
	        href="http://example.org/reilly/main" >
	      <atom:title>My Blog Entries</atom:title>
	      <categories
	         href="http://example.com/cats/forMain.cats" />
	    </collection>
	    <collection
	        href="http://example.org/reilly/pic" >
	      <atom:title>Pictures</atom:title>
	      <accept>image/*</accept>
	    </collection>
	  </workspace>
	  <workspace>
	    <atom:title>Side Bar Blog</atom:title>
	    <collection
	        href="http://example.org/reilly/list" >
	      <atom:title>Remaindered Links</atom:title>
	      <accept>entry</accept>
	      <categories fixed="yes">
	        <atom:category
	          scheme="http://example.org/extra-cats/"
	          term="joke" />
	        <atom:category
	          scheme="http://example.org/extra-cats/"
	          term="serious" />
	      </categories>
	    </collection>
	  </workspace>
	</service>
 */
public class AtomService {

    private List workspaces = new ArrayList();
    
    /** Namespace for Atom Syndication Format */
    public static Namespace ATOM_FORMAT = 
        Namespace.getNamespace("atom","http://www.w3.org/2005/Atom");
    
    /** Namespace for Atom Publishing Protocol */
    public static Namespace ATOM_PROTOCOL = 
        Namespace.getNamespace("app","http://purl.org/atom/app#");  
        
    public AtomService() {
    }
    
    public void addWorkspace(Workspace workspace) {
        workspaces.add(workspace);
    }
    
    public List getWorkspaces() {
        return workspaces;
    }
    
    public void setWorkspaces(List workspaces) {
        this.workspaces = workspaces;
    }
    
    public Workspace findWorkspace(String title) {
        for (Iterator it = workspaces.iterator(); it.hasNext();) {
            Workspace ws = (Workspace) it.next();
            if (title.equals(ws.getTitle())) {
                return ws;
            }
        }
        return null;
    }

    /** Deserialize an Atom service XML document into an object */
    public static AtomService documentToService(Document document) {
        AtomService service = new AtomService();
        Element root = document.getRootElement();
        List spaces = root.getChildren("workspace", ATOM_PROTOCOL);
        Iterator iter = spaces.iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            service.addWorkspace(Workspace.elementToWorkspace(e));
        }
        return service;
    }
    
    /**
     * Serialize an AtomService object into an XML document
     */
    public static Document serviceToDocument(AtomService service) {
        Document doc = new Document();
        Element root = new Element("service", ATOM_PROTOCOL);
        doc.setRootElement(root);
        Iterator iter = service.getWorkspaces().iterator();
        while (iter.hasNext()) {
            Workspace space = (Workspace) iter.next();
            root.addContent(Workspace.workspaceToElement(space));
        }
        return doc;
    }

}

