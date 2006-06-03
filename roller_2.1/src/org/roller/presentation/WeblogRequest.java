/*
 * WeblogRequest.java
 *
 * Created on December 14, 2005, 6:14 PM
 */

package org.roller.presentation;

import javax.servlet.http.HttpServletRequest;


/**
 * Represents a request to single weblog.
 *
 * This is a fairly generic parsed request which is only trying to figure out
 * the weblog handle that this request is destined for.
 *
 * @author Allen Gilliland
 */
public class WeblogRequest extends ParsedRequest {
    
    private String weblogHandle = null;
    
    
    public WeblogRequest(HttpServletRequest request) throws InvalidRequestException {
        
        // let our parent take care of their business first
        super(request);
        
        String pathInfo = request.getPathInfo();
        
        // we expect a path info of /<handle/*
        if(pathInfo != null && pathInfo.trim().length() > 1) {
            // strip off the leading slash
            pathInfo = pathInfo.substring(1);
            String[] pathElements = pathInfo.split("/");
            
            if(pathElements[0] != null && pathElements[0].trim().length() > 1) {
                this.weblogHandle = pathElements[0];
            } else {
                // no handle in path info
                throw new InvalidRequestException("not a weblog request, "+request.getRequestURL());
            }
            
        } else {
            // invalid request ... path info is empty
            throw new InvalidRequestException("not a weblog request, "+request.getRequestURL());
        }
    }

    
    public String getWeblogHandle() {
        return weblogHandle;
    }
    
}
