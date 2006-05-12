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
package org.apache.roller.webservices.adminapi;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.apache.roller.webservices.adminapi.sdk.Entry;
import org.apache.roller.webservices.adminapi.sdk.EntrySet;
import org.apache.roller.webservices.adminapi.sdk.MissingElementException;
import org.apache.roller.webservices.adminapi.sdk.Service;
import org.apache.roller.webservices.adminapi.sdk.UnexpectedRootElementException;
import org.jdom.Document;

/**
 * This class handles requests for the AAPP introspection document.
 * It only processes HTTP GET requests.
 *
 * @author jtb
 */
class IntrospectionHandler extends Handler {
    public IntrospectionHandler(HttpServletRequest request) throws HandlerException {
        super(request);
    }
    
    protected EntrySet getEntrySet(Document d) throws MissingElementException, UnexpectedRootElementException {
        throw new UnsupportedOperationException();
    }
    
    public EntrySet processGet() throws HandlerException {
        if (getUri().isIntrospection()) {
            return getIntrospection(getRequest());
        } else {
            throw new BadRequestException("ERROR: Unknown GET URI type");
        }
    }
    
    public EntrySet processPost(Reader r) {
        throw new UnsupportedOperationException("ERROR: POST not supported in this handler");
    }
    
    public EntrySet processPut(Reader r) {
        throw new UnsupportedOperationException("ERROR: PUT not supported in this handler");
    }
    
    public EntrySet processDelete() {
        throw new UnsupportedOperationException("ERROR: DELETE not supported in this handler");
    }
    
    private Service getIntrospection(HttpServletRequest req) {
        String href = getUrlPrefix();
        Service service = new Service(href);
        
        Service.Workspace workspace = new Service.Workspace();
        workspace.setTitle("Workspace: Collections for administration");
        workspace.setHref(service.getHref());
        service.setEntries(new Entry[] { workspace });
        
        List workspaceCollections = new ArrayList();
        
        Service.Workspace.Collection weblogCol = new Service.Workspace.Collection();
        weblogCol.setTitle("Collection: Weblog administration entries");
        weblogCol.setMemberType(org.apache.roller.webservices.adminapi.sdk.Entry.Types.WEBLOG);
        weblogCol.setHref(service.getHref() + "/" + org.apache.roller.webservices.adminapi.sdk.EntrySet.Types.WEBLOGS);
        workspaceCollections.add(weblogCol);
        
        Service.Workspace.Collection userCol = new Service.Workspace.Collection();
        userCol.setTitle("Collection: User administration entries");
        userCol.setMemberType("user");
        userCol.setHref(service.getHref() + "/" + org.apache.roller.webservices.adminapi.sdk.EntrySet.Types.USERS);
        workspaceCollections.add(userCol);
        
        Service.Workspace.Collection memberCol = new Service.Workspace.Collection();
        memberCol.setTitle("Collection: Member administration entries");
        memberCol.setMemberType("member");
        memberCol.setHref(service.getHref() + "/" + org.apache.roller.webservices.adminapi.sdk.EntrySet.Types.MEMBERS);
        workspaceCollections.add(memberCol);
        
        workspace.setEntries((Entry[])workspaceCollections.toArray(new Entry[0]));
        
        return service;
    }
}

