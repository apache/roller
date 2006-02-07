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
    
    private AtomAdminService getIntrospection(HttpServletRequest req) throws Exception {
        String href = getUrlPrefix();
        AtomAdminService service = new AtomAdminService(href);
        
        AtomAdminService.Workspace workspace = new AtomAdminService.Workspace();
        workspace.setTitle("Workspace: Collections for administration");
        workspace.setHref(service.getHref());
        service.setEntries(new Entry[] { workspace });
        
        List workspaceCollections = new ArrayList();
        
        AtomAdminService.WorkspaceCollection weblogCol = new AtomAdminService.WorkspaceCollection();
        weblogCol.setTitle("Collection: Weblog administration entries");
        weblogCol.setMemberType(org.roller.presentation.atomadminapi.Entry.Types.WEBLOG);
        weblogCol.setHref(service.getHref() + "/" + org.roller.presentation.atomadminapi.EntrySet.Types.WEBLOGS);
        workspaceCollections.add(weblogCol);
        
        AtomAdminService.WorkspaceCollection userCol = new AtomAdminService.WorkspaceCollection();
        userCol.setTitle("Collection: User administration entries");
        userCol.setMemberType("user");
        userCol.setHref(service.getHref() + "/" + org.roller.presentation.atomadminapi.EntrySet.Types.USERS);
        workspaceCollections.add(userCol);
        
        AtomAdminService.WorkspaceCollection memberCol = new AtomAdminService.WorkspaceCollection();
        memberCol.setTitle("Collection: Member administration entries");
        memberCol.setMemberType("member");
        memberCol.setHref(service.getHref() + "/" + org.roller.presentation.atomadminapi.EntrySet.Types.MEMBERS);
        workspaceCollections.add(memberCol);
        
        workspace.setEntries((Entry[])workspaceCollections.toArray(new Entry[0]));
        
        return service;
    }
}

