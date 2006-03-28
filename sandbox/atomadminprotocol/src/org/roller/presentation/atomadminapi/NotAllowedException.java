package org.roller.presentation.atomadminapi;

import javax.servlet.http.HttpServletResponse;

/**
 * Indicates to client that they are not allows to perform the requested
 * operation on the requested resource.
 */
public class NotAllowedException extends HandlerException { 
    public NotAllowedException(String msg) {
        super(msg);
    }    
    
    public int getStatus() {
        return HttpServletResponse.SC_METHOD_NOT_ALLOWED;
    }
}
