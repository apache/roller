/*
 * Copyright 2005 David M Johnson (For RSS and Atom In Action)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.webservices.adminapi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;
import org.apache.roller.webservices.adminapi.sdk.EntrySet;

/**
 * Atom Admin Servlet implements the Atom Admin endpoint.
 * This servlet simply delegates work to a particular handler object.
 *
 * @web.servlet name="AtomAdminServlet"
 * @web.servlet-mapping url-pattern="/aapp/*"
 *
 * @author jtb
 */
public class AtomAdminServlet extends HttpServlet {
    private static Log logger = LogFactory.getFactory().getInstance(AtomAdminServlet.class);
    
    /**
     * Handles an Atom GET by calling handler and writing results to response.
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Handler handler = Handler.getHandler(req);
            String userName = handler.getUserName();
            
            EntrySet c = handler.processGet();
            
            res.setStatus(HttpServletResponse.SC_OK);            
            res.setContentType("application/xml; charset=utf-8");
            String s = c.toString();
            Writer writer = res.getWriter();
            writer.write(s);            
            writer.close();            
        } catch (HandlerException he) {
            res.sendError(he.getStatus(), he.getMessage());
            he.printStackTrace(res.getWriter());
            logger.error(he);
        }
    }
    
    /**
     * Handles an Atom POST by calling handler to identify URI, reading/parsing
     * data, calling handler and writing results to response.
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Handler handler = Handler.getHandler(req);
            String userName = handler.getUserName();
            
            EntrySet c = handler.processPost(new InputStreamReader(req.getInputStream()));
            
            res.setStatus(HttpServletResponse.SC_CREATED);            
            res.setContentType("application/xml; charset=utf-8");
            String s = c.toString();
            Writer writer = res.getWriter();
            writer.write(s);            
            writer.close();            
        } catch (HandlerException he) {
            res.sendError(he.getStatus(), he.getMessage());
            he.printStackTrace(res.getWriter());
            logger.error(he);
        }
    }
    
    /**
     * Handles an Atom PUT by calling handler to identify URI, reading/parsing
     * data, calling handler and writing results to response.
     */
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Handler handler = Handler.getHandler(req);
            String userName = handler.getUserName();
            
            EntrySet c = handler.processPut(new InputStreamReader(req.getInputStream()));
            
            res.setStatus(HttpServletResponse.SC_OK);            
            res.setContentType("application/xml; charset=utf-8");
            String s = c.toString();
            Writer writer = res.getWriter();
            writer.write(s);            
            writer.close();            
        } catch (HandlerException he) {
            res.sendError(he.getStatus(), he.getMessage());
            he.printStackTrace(res.getWriter());
            logger.error(he);
        }
    }
    
    /**
     * Handle Atom Admin DELETE by calling appropriate handler.
     */
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Handler handler = Handler.getHandler(req);
            String userName = handler.getUserName();
            
            EntrySet es = handler.processDelete();
            
            res.setStatus(HttpServletResponse.SC_OK);                        
            res.setContentType("application/xml; charset=utf-8");
            String s = es.toString();
            Writer writer = res.getWriter();
            writer.write(s);            
            writer.close();                        
        } catch (HandlerException he) {
            res.sendError(he.getStatus(), he.getMessage());
            he.printStackTrace(res.getWriter());
            logger.error(he);
        }
    }
}
