package org.roller.presentation.atomadminapi;

import javax.servlet.http.HttpServletResponse;

/**
 * Indicates to client that an internal error occured when processing
 * the request.
 */
public class InternalException extends HandlerException { 
    public InternalException(String msg, Throwable t) {
        super(msg, t);
    }    
    
    public int getStatus() {
        return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }
}
