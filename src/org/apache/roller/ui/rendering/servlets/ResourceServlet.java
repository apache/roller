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

package org.apache.roller.ui.rendering.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.ui.rendering.util.ModDateHeaderUtil;


/**
 * Resources servlet.  Acts as a gateway to files uploaded by users.
 *
 * Since we keep uploaded resources in a location outside of the webapp
 * context we need a way to serve them up.  This servlet assumes that
 * resources are stored on a filesystem in the "uploads.dir" directory.
 *
 * @web.servlet name="ResourcesServlet" load-on-startup="5"
 * @web.servlet-mapping url-pattern="/roller-ui/rendering/resources/*"
 */
public class ResourceServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(ResourceServlet.class);

    private String upload_dir = null;
    private ServletContext context = null;


    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        log.info("Initializing ResourceServlet");

        this.context = config.getServletContext();

        try {
            this.upload_dir = RollerFactory.getRoller().getFileManager().getUploadDir();
            log.debug("upload dir is ["+this.upload_dir+"]");
        } catch(Exception e) {
            log.error(e);
        }

    }


    /**
     * Handles requests for user uploaded resources.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String context = request.getContextPath();
        String servlet = request.getServletPath();
        String reqURI = request.getRequestURI();

        // URL decoding

        // Fix for ROL-1065: even though a + should mean space in a URL, folks 
        // who upload files with plus signs expect them to work without 
        // escaping. This is essentially what other systems do (e.g. JIRA) to 
        // enable this.
        reqURI = reqURI.replaceAll("\\+", "%2B");

        // now we really decode the URL
        reqURI = URLDecoder.decode(reqURI, "UTF-8");

        // calculate the path of the requested resource
        // we expect ... /<context>/<servlet>/path/to/resource
        String reqResource = reqURI.substring(servlet.length() + context.length());

        // now we can formulate the *real* path to the resource on the filesystem
        String resource_path = this.upload_dir + reqResource;
        File resource = new File(resource_path);

        log.debug("Resource requested ["+reqURI+"]");
        log.debug("Real path is ["+resource.getAbsolutePath()+"]");

        // do a quick check to make sure the resource exits, otherwise 404
        if(!resource.exists() || !resource.canRead() || resource.isDirectory()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // make sure someone isn't trying to sneek outside the uploads dir
        File uploadDir = new File(this.upload_dir);
        if(!resource.getCanonicalPath().startsWith(uploadDir.getCanonicalPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Respond with 304 Not Modified if it is not modified.
        if (ModDateHeaderUtil.respondIfNotModified(request,response, resource.lastModified())) {
            return;
        }

        // set last-modified date
        ModDateHeaderUtil.setLastModifiedHeader(response,resource.lastModified());

        // set the content type based on whatever is in our web.xml mime defs
        response.setContentType(this.context.getMimeType(resource.getAbsolutePath()));

        // ok, lets serve up the file
        byte[] buf = new byte[8192];
        int length = 0;
        OutputStream out = response.getOutputStream();
        InputStream resource_file = new FileInputStream(resource);
        while((length = resource_file.read(buf)) > 0)
            out.write(buf, 0, length);

        // cleanup
        out.close();
        resource_file.close();
    }

}
