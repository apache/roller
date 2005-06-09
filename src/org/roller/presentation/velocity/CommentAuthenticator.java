package org.roller.presentation.velocity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.context.Context;
import org.roller.pojos.CommentData;

/**
 * Interface for comment authentication plugin.
 * @author David M Johnson
 */
public interface CommentAuthenticator
{
    /**
     * Plugin should write out HTML for the form fields and other UI elements
     * needed inside the Roller comment form. Called when HTML form is being 
     * displayed by Velocity template (see comments.vm).
     *
     * @param context Plugin can access objects in context of calling page.
     * @param request Plugin can access request parameters
     * @param response Plugin should write to response
     */
    public String getHtml(
                        Context context,
                        HttpServletRequest request, 
                        HttpServletResponse response);
    /**
     * Plugin should return true only if comment passes authentication test.
     * Called when a comment is posted, not called when a comment is previewed.
     *
     * @param comment Comment data that was posted 
     * @param request Plugin can access request parameters
     * @return True if comment passes authentication test
     */
    public boolean authenticate(
                        CommentData comment,
                        HttpServletRequest request);
}
