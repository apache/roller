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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.rendering.processors;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.roller.weblogger.ui.rendering.comment.CommentAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * Used for generating the html used for comment authentication.  This is done outside of the
 * normal rendering process so that we can cache full pages and still set the comment authentication
 * section dynamically.
 */
@RestController
public class CommentAuthenticatorProcessor {

    @Autowired
    private CommentAuthenticator commentAuthenticator = null;

    public void setCommentAuthenticator(CommentAuthenticator commentAuthenticator) {
        this.commentAuthenticator = commentAuthenticator;
    }

    @RequestMapping(value="/CommentAuthenticatorServlet", method = RequestMethod.GET)
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html; charset=utf-8");

        // Convince proxies and IE not to cache this.
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");

        PrintWriter out = response.getWriter();
        out.println(commentAuthenticator.getHtml(request));
    }

}
