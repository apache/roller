
package org.apache.roller.ui.rendering;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Interface representing an object that maps requests.
 */
public interface RequestMapper {
    
    /**
     * Handle an incoming request.
     *
     * RequestMappers are not required to handle all requests and are instead
     * encouraged to inspect the request and only take action when it
     * wants to.  If action is taken then the RequestMapper should return a 
     * boolean "true" value indicating that no further action is required.
     */
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException;
    
}
