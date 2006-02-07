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
package org.roller.presentation.atomadminapi;

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
            String userName = handler.getUsername();
            if (userName == null) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            EntrySet c = handler.processGet();
            Document doc = c.toDocument();
            
            res.setContentType("application/xml; charset=utf8");
            Writer writer = res.getWriter();
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(doc, writer);
            writer.close();
            
            res.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(res.getWriter());
            logger.error(e);
        }
    }
    
    /**
     * Handles an Atom POST by calling handler to identify URI, reading/parsing
     * data, calling handler and writing results to response.
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Handler handler = Handler.getHandler(req);
            String userName = handler.getUsername();
            if (userName == null) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            EntrySet c = handler.processPost(new InputStreamReader(req.getInputStream()));
            Document doc = c.toDocument();
            
            
            res.setContentType("application/xml; charset=utf8");
            Writer writer = res.getWriter();
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(doc, writer);
            writer.close();
            
            res.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(res.getWriter());
            logger.error(e);
        }
    }
    
    /**
     * Handles an Atom PUT by calling handler to identify URI, reading/parsing
     * data, calling handler and writing results to response.
     */
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Handler handler = Handler.getHandler(req);
            String userName = handler.getUsername();
            if (userName == null) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            EntrySet c = handler.processPut(new InputStreamReader(req.getInputStream()));
            Document doc = c.toDocument();
            
            res.setContentType("application/xml; charset=utf8");
            Writer writer = res.getWriter();
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(doc, writer);
            writer.close();
            
            res.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(res.getWriter());
            logger.error(e);
        }
    }
    
    /**
     * Handle Atom DELETE by calling appropriate handler.
     */
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            Handler handler = Handler.getHandler(req);
            String userName = handler.getUsername();
            if (userName == null) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            EntrySet es = handler.processDelete();
            Document doc = es.toDocument();
            
            res.setContentType("application/xml; charset=utf8");
            Writer writer = res.getWriter();
            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(doc, writer);
            writer.close();
            
            res.setStatus(HttpServletResponse.SC_OK);            
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(res.getWriter());
            logger.error(e);
        }
    }
}
