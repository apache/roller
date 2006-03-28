package org.roller.presentation.atomadminapi;

import javax.servlet.http.HttpServletResponse;

/**
 * Indicates to client that the requested resource was not found
 * on the server.
 */
public class NotFoundException extends HandlerException { 
    public NotFoundException(String msg) {
        super(msg);
    }    
    
    public int getStatus() {
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
