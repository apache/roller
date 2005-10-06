package org.roller.presentation.ajax;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;

/**
 * Return list of users matching a startsWith strings. <br />
 * Accepts request params (none required):<br />
 *     startsWith: string to be matched against username and email address<br />
 *     enabled: true include only enabled users (default: no restriction<br />
 *     offset: offset into results (for paging)<br />
 *     length: number of users to return (max is 50)<br /><br />
 * List format:<br />
 *     username0, emailaddress0 <br/>
 *     username1, emailaddress1 <br/>
 *     username2, emailaddress2 <br/>
 *     usernameN, emailaddressN <br/>
 * 
 * @web.servlet name="UserDataServlet" 
 * @web.servlet-mapping url-pattern="/editor/userdata/*"
 * @author David M Johnson
 */
public class UserDataServlet extends HttpServlet {
    private final int MAX_LENGTH = 50;   
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {    
        
        String startsWith = request.getParameter("startsWith");
        Boolean enabledOnly = null;
        int offset = 0;
        int length = MAX_LENGTH;
        if ("true".equals(request.getParameter("enabled"))) enabledOnly = Boolean.TRUE;
        if ("false".equals(request.getParameter("enabled"))) enabledOnly = Boolean.FALSE;
        try { offset = Integer.parseInt(request.getParameter("offset"));
        } catch (Throwable ignored) {}             
        try { length = Integer.parseInt(request.getParameter("length"));
        } catch (Throwable ignored) {}
        
        Roller roller = RollerFactory.getRoller();
        try {
            UserManager umgr = roller.getUserManager();
            List users = 
             umgr.getUsersStartingWith(startsWith, offset, length, enabledOnly);
            Iterator userIter = users.iterator();
            while (userIter.hasNext()) {
                UserData user = (UserData)userIter.next();
                response.getWriter().print(user.getUserName());   
                response.getWriter().print(",");   
                response.getWriter().println(user.getEmailAddress());
            }
            response.flushBuffer();
        } catch (RollerException e) {
            throw new ServletException(e.getMessage());
        }
    }
}
