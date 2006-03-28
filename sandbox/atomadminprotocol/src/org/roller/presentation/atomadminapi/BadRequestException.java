package org.roller.presentation.atomadminapi;

import javax.servlet.http.HttpServletResponse;

/**
 * Indicates to client that a bad (syntactically incorrect)
 * request has been made.
 */
public class BadRequestException extends HandlerException { 
    public BadRequestException(String msg) {
        super(msg);
    }    
    
    public int getStatus() {
        return HttpServletResponse.SC_BAD_REQUEST;
    }
}
