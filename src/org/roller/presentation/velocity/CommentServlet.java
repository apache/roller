/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.roller.presentation.velocity;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.RollerRequest;


/**
 * This is the old Roller CommentServlet which used to extend the PageServlet
 * and do some page rendering.  This was replaced by the new version ...
 * org.roller.persentation.CommentServlet which does not do any rendering.
 *
 * This servlet is left in place to maintain old urls and redirect them to
 * their proper new location.
 *
 * @web.servlet name="CommentsServlet"
 * @web.servlet-mapping url-pattern="/comments/*"
 *
 * @author Allen Gilliland
 */
public class CommentServlet extends HttpServlet {
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(CommentServlet.class);
    
    
    /**
     * Handle GET requests.
     *
     * This method just responds to all GET requests with a 301 redirect 
     * because these old comment servlet urls are deprecated now.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        
        // a GET request means that this client is trying to use this old
        // comment servlet as a form of permalink for rendering an entry.
        // we want to discourage this, so we send a 301 response which means
        // this url has been permanently moved.
        String forward = request.getContextPath();
        
        // calculate the location of the requested permalink
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        
        // ROL-1102: prevent NPE for bad URLs
        if (rreq == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } 
        WeblogEntryData entry = rreq.getWeblogEntry();
        if (entry != null) {
            forward += entry.getPermaLink();
            
            // make sure to propogate popup requests
            if(request.getParameter("popup") != null) {
                if(forward.indexOf("?") == -1)
                    forward += "?popup=true";
                else
                    forward += "&popup=true";
            }
                
        }        
        mLogger.debug("forwarding to "+forward);
        
        // send an HTTP 301 response
        response.setStatus(response.SC_MOVED_PERMANENTLY);
        response.setHeader("Location", forward);
    }

    
    /**
     * Handle POST requests.
     *
     * POST requests to this old CommentServlet are simply dispatched to the
     * new CommentServlet inside our servlet container.  We log these requests
     * in the log file with a WARN so that site admins can track which users
     * have hard coded the use of the old comment servlet and hopefully those
     * users can be contacted about udating their templates.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        
        mLogger.warn(request.getHeader("referer")+
                " is posting to the OLD CommentServlet");
        
        // just dispatch to new CommentServlet
        RequestDispatcher dispatch = request.getRequestDispatcher("/comment");
        dispatch.forward(request, response);
    }

}

