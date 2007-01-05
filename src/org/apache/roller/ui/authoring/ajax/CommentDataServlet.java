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
package org.apache.roller.ui.authoring.ajax;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.WordUtils;

import org.apache.roller.RollerException;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.util.Utilities;

/**
 * Return comment id and content in JavaScript Object Notation (JSON) format. 
 * For example comment with id "3454545346" and content "hi there" will be 
 * represented as: {id : "3454545346", content : "hi there"}
 *
 * @web.servlet name="CommentDataServlet" 
 * @web.servlet-mapping url-pattern="/roller-ui/authoring/commentdata/*"
 */
public class CommentDataServlet extends HttpServlet {
 
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {    
                
        Roller roller = RollerFactory.getRoller();
        try {
            WeblogManager wmgr = roller.getWeblogManager();
            CommentData c = wmgr.getComment(request.getParameter("id"));
            String content = Utilities.escapeHTML(c.getContent());
            content = WordUtils.wrap(content, 72);
            content = StringEscapeUtils.escapeJavaScript(content);
            String json = "{ id: \"" + c.getId() + "\"," + "content: \"" + content + "\" }"; 
            response.setContentType("text/html; charset=utf-8");
            response.getWriter().print(json);   
            response.flushBuffer();
            response.getWriter().flush();
            response.getWriter().close();
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }
}
