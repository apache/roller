
package org.roller.presentation.velocity;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Extend RollerServlet to load proper resource loader for page execution.
 * 
 * @web.servlet name="PreviewServlet" load-on-startup="1"
 * @web.servlet-init-param name="properties" value="/WEB-INF/velocity.properties" 
 * @web.servlet-mapping url-pattern="/preview/*"
 */ 
public class PreviewServlet extends BasePageServlet
{
    public Template handleRequest( HttpServletRequest request,
                                    HttpServletResponse response, 
                                    Context ctx ) throws Exception
    {
        return super.handleRequest(request, response, ctx);
    }
}

