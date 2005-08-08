package org.roller.presentation.velocity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.roller.pojos.WeblogTemplate;

/**
 * A web page insert is a big of HTML that is inserted into a page into 
 * any page that is rendered by the Roller Page Servlet and CommentServlet.
 *
 * @author David M Johnson
 */
public interface VelocityPageInsert 
{
    public void display(
        WeblogTemplate page,
        VelocityContext context,
        HttpServletRequest request, 
        HttpServletResponse response);
}
