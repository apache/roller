package org.roller.presentation.velocity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;
import org.roller.pojos.CommentData;

/**
 * Default authenticator does nothing, always returns true.
 * @author David M Johnson
 */
public class DefaultCommentAuthenticator implements CommentAuthenticator 
{   
    public String getHtml(
                        Context context,
                        HttpServletRequest request, 
                        HttpServletResponse response)
    {
        return "<!-- custom authenticator would go here -->";
    }
    
    public boolean authenticate(
                        CommentData comment,
                        HttpServletRequest request) 
    {
        return true;
    }
}
