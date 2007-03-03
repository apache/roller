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

package org.apache.roller.planet.ui.rendering.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.config.PlanetConfig;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.StaticTemplate;
import org.apache.roller.planet.pojos.Template;
import org.apache.roller.planet.ui.rendering.Renderer;
import org.apache.roller.planet.ui.rendering.RendererManager;
import org.apache.roller.planet.ui.rendering.model.ModelLoader;
import org.apache.roller.planet.ui.rendering.util.PlanetGroupPageRequest;


/**
 * Responsible for rendering application homepage.
 */
public class HomepageServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(HomepageServlet.class);


    /**
     * Init method for this servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {

        super.init(servletConfig);

        log.info("Initializing HomepageServlet");
    }


    /**
     * Handle GET requests for weblog feeds.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        log.debug("Entering");

        // set content type
        response.setContentType("text/html; charset=utf-8");
        
        
        // initialize model
        HashMap model = new HashMap();
        try {
            // populate the rendering model
            Map initData = new HashMap();
            
            // Load models for pages
            String models = PlanetConfig.getProperty("rendering.homepageModels");
            ModelLoader.loadModels(models, model, initData, true);

        } catch (RollerException ex) {
            log.error("ERROR loading model", ex);

            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }


        // lookup Renderer we are going to use
        Renderer renderer = null;
        try {
            log.debug("Looking up renderer");
            
            // what template are we going to render?
            Template template = new StaticTemplate("home.vm", null, "velocity");
            
            // get the Renderer
            renderer = RendererManager.getRenderer(template);
            
        } catch(Exception e) {
            // nobody wants to render my content :(

            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        
        // render content
        try {
            log.debug("Doing rendering");
            renderer.render(model, response.getWriter());
        } catch(Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering", e);

            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }


        // post rendering process

        // flush rendered content to response
        log.debug("Flushing response output");
        //response.setContentLength(rendererOutput.getContent().length);
        //response.getOutputStream().write(rendererOutput.getContent());

        log.debug("Exiting");
    }

}
