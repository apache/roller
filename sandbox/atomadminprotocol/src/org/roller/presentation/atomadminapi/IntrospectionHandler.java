/*
 * IntrospectionHandler.java
 *
 * Created on January 17, 2006, 12:44 PM
 */
package org.roller.presentation.atomadminapi;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.roller.presentation.atomadminapi.sdk.Service;
import org.roller.presentation.atomadminapi.sdk.Entry;
import org.roller.presentation.atomadminapi.sdk.EntrySet;

/**
 * This class handles requests for the AAPP introspection document.
 * It only processes HTTP GET requests.
 *
 * @author jtb
 */
class IntrospectionHandler extends Handler {   
    public IntrospectionHandler(HttpServletRequest request) {
        super(request);
    }
    
    public EntrySet processGet() throws Exception {
        if (getUri().isIntrospection()) {
            return getIntrospection(getRequest());
        } else {
            throw new Exception("ERROR: Unknown GET URI type");
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
    
    private Service getIntrospection(HttpServletRequest req) throws Exception {
        String href = getUrlPrefix();
        Service service = new Service(href);
        
        Service.Workspace workspace = new Service.Workspace();
        workspace.setTitle("Workspace: Collections for administration");
        workspace.setHref(service.getHref());
        service.setEntries(new Entry[] { workspace });
        
        List workspaceCollections = new ArrayList();
        
        Service.Workspace.Collection weblogCol = new Service.Workspace.Collection();
        weblogCol.setTitle("Collection: Weblog administration entries");
        weblogCol.setMemberType(org.roller.presentation.atomadminapi.sdk.Entry.Types.WEBLOG);
        weblogCol.setHref(service.getHref() + "/" + org.roller.presentation.atomadminapi.sdk.EntrySet.Types.WEBLOGS);
        workspaceCollections.add(weblogCol);
        
        Service.Workspace.Collection userCol = new Service.Workspace.Collection();
        userCol.setTitle("Collection: User administration entries");
        userCol.setMemberType("user");
        userCol.setHref(service.getHref() + "/" + org.roller.presentation.atomadminapi.sdk.EntrySet.Types.USERS);
        workspaceCollections.add(userCol);
        
        Service.Workspace.Collection memberCol = new Service.Workspace.Collection();
        memberCol.setTitle("Collection: Member administration entries");
        memberCol.setMemberType("member");
        memberCol.setHref(service.getHref() + "/" + org.roller.presentation.atomadminapi.sdk.EntrySet.Types.MEMBERS);
        workspaceCollections.add(memberCol);
        
        workspace.setEntries((Entry[])workspaceCollections.toArray(new Entry[0]));
        
        return service;
    }
}

