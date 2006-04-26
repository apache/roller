package org.roller.presentation.atomadminapi;

import javax.servlet.http.HttpServletResponse;

/**
 * Indicates to client that an internal error occured when processing
 * the request.
 */
public class UnauthorizedException extends HandlerException { 
    public UnauthorizedException(String msg) {
        super(msg);
    }    
    
    public int getStatus() {
        return HttpServletResponse.SC_UNAUTHORIZED;
    }
}
