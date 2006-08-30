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
package org.apache.roller.webservices.atomprotocol; 

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.WireFeedOutput;
import java.io.StringWriter;
import org.jdom.Namespace;
import org.apache.roller.config.RollerConfig;

/**
 * Atom Servlet implements Atom by calling a Roller independent handler.
 * @web.servlet name="AtomServlet"
 * @web.servlet-mapping url-pattern="/roller-services/app/*"
 * @author David M Johnson
 */
public class AtomServlet extends HttpServlet {
    public static final String FEED_TYPE = "atom_1.0";
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(AtomServlet.class);
    
    //-----------------------------------------------------------------------------
    /**
     * Create an Atom request handler.
     * TODO: make AtomRequestHandler implementation configurable.
     */
    private AtomHandler createAtomRequestHandler(HttpServletRequest request) 
    throws ServletException {
        boolean enabled = RollerConfig.getBooleanProperty(
            "webservices.atomprotocol.enabled");
        if (!enabled) {
            throw new ServletException("ERROR: Atom protocol not enabled");
        }
        return new RollerAtomHandler(request);
    }
    
    //-----------------------------------------------------------------------------
    /**
     * Handles an Atom GET by calling handler and writing results to response.
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException { 
        AtomHandler handler = createAtomRequestHandler(req);
        String userName = handler.getAuthenticatedUsername();
        if (userName != null) {
            String[] pathInfo = getPathInfo(req);
            try {
                if (handler.isIntrospectionURI(pathInfo)) {
                    // return an Atom Service document
                    AtomService service = handler.getIntrospection();
                    Document doc = AtomService.serviceToDocument(service);
                    res.setContentType("application/atomserv+xml; charset=utf-8");
                    Writer writer = res.getWriter();
                    XMLOutputter outputter = new XMLOutputter();
                    outputter.setFormat(Format.getPrettyFormat());
                    outputter.output(doc, writer);
                    writer.close();
                    res.setStatus(HttpServletResponse.SC_OK);
                } 
                else if (handler.isCollectionURI(pathInfo)) {
                    // return a collection
                    Feed col = handler.getCollection(pathInfo);
                    col.setFeedType(FEED_TYPE);
                    WireFeedOutput wireFeedOutput = new WireFeedOutput();
                    Document feedDoc = wireFeedOutput.outputJDom(col);
                    res.setContentType("application/atom+xml; charset=utf-8");
                    Writer writer = res.getWriter();
                    XMLOutputter outputter = new XMLOutputter();
                    outputter.setFormat(Format.getPrettyFormat());
                    outputter.output(feedDoc, writer);
                    writer.close();
                    res.setStatus(HttpServletResponse.SC_OK);
                } 
                else if (handler.isEntryURI(pathInfo)) {
                    // return an entry
                    Entry entry = handler.getEntry(pathInfo);
                    if (entry != null) {
                        Writer writer = res.getWriter();
                        res.setContentType("application/atom+xml; charset=utf-8");
                        serializeEntry(entry, writer);
                        writer.close();
                    } else {
                        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    }
                } else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (AtomException ae) {
                res.setStatus(ae.getStatus());
                mLogger.debug(ae);
            } catch (Exception ae) {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                mLogger.debug(ae);
            }
        } else {
            res.setHeader("WWW-Authenticate", "BASIC realm=\"Roller\"");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    
    //-----------------------------------------------------------------------------
    /**
     * Handles an Atom POST by calling handler to identify URI, reading/parsing
     * data, calling handler and writing results to response.
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        AtomHandler handler = createAtomRequestHandler(req);
        String userName = handler.getAuthenticatedUsername();
        if (userName != null) {
            String[] pathInfo = getPathInfo(req);
            try {
                if (handler.isCollectionURI(pathInfo)) {
                    
                    if (req.getContentType().startsWith("application/atom+xml")) {

                        // parse incoming entry
                        Entry unsavedEntry = parseEntry(
                            new InputStreamReader(req.getInputStream()));

                        // call handler to post it
                        Entry savedEntry = handler.postEntry(pathInfo, unsavedEntry);
                        
                        // return alternate link as Location header
                        Iterator links = savedEntry.getOtherLinks().iterator();
                        while (links.hasNext()) {
                            Link link = (Link) links.next();
                            if (link.getRel().equals("edit") || link.getRel() == null) {
                                res.addHeader("Location", link.getHref());
                                break;
                            }
                        }
                        // write entry back out to response
                        res.setStatus(HttpServletResponse.SC_CREATED);
                        res.setContentType("application/atom+xml; charset=utf-8");
                        Writer writer = res.getWriter();
                        serializeEntry(savedEntry, writer);
                        writer.close(); 
                    
                    } else if (req.getContentType() != null) {
                        // get incoming file name from HTTP header
                        String title = req.getHeader("Title");

                        // hand input stream of to hander to post file
                        Entry resource = handler.postMedia(
                            pathInfo, title, req.getContentType(), req.getInputStream());
                        
                        res.setStatus(HttpServletResponse.SC_CREATED);
                        com.sun.syndication.feed.atom.Content content = 
                            (com.sun.syndication.feed.atom.Content)resource.getContents().get(0);

                        // return alternate link as Location header
                        Iterator links = resource.getOtherLinks().iterator();
                        while (links.hasNext()) {
                            Link link = (Link) links.next();
                            if (link.getRel().equals("edit") || link.getRel() == null) {
                                res.addHeader("Location", link.getHref());
                                break;
                            }
                        }
                        Writer writer = res.getWriter();
                        serializeEntry(resource, writer);
                        writer.close(); 
                    } else {
                        res.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
                    }
                    
                } else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (AtomException ae) {
                res.setStatus(ae.getStatus());
                mLogger.debug(ae);
            } catch (Exception ae) {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                mLogger.debug(ae);
            }
        } else {
            res.setHeader("WWW-Authenticate", "BASIC realm=\"Roller\"");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    
    //-----------------------------------------------------------------------------
    /**
     * Handles an Atom PUT by calling handler to identify URI, reading/parsing
     * data, calling handler and writing results to response.
     */
    protected void doPut(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        AtomHandler handler = createAtomRequestHandler(req);
        String userName = handler.getAuthenticatedUsername();
        if (userName != null) {
            String[] pathInfo = getPathInfo(req);
            try {
                if (handler.isEntryURI(pathInfo)) {
                    
                    // parse incoming entry
                    Entry unsavedEntry = parseEntry(
                            new InputStreamReader(req.getInputStream()));
                    
                    // call handler to put entry
                    Entry updatedEntry = handler.putEntry(pathInfo, unsavedEntry);
                    
                    // write entry back out to response
                    res.setContentType("application/atom+xml; charset=utf-8");
                    Writer writer = res.getWriter();
                    serializeEntry(updatedEntry, writer);
                    res.setStatus(HttpServletResponse.SC_OK);
                    writer.close();
                    
                } else if (handler.isMediaEditURI(pathInfo)) {
                    
                    // hand input stream to handler
                    Entry updatedEntry = handler.putMedia(
                        pathInfo, req.getContentType(), req.getInputStream());
                                        
                    // write entry back out to response
                    res.setContentType("application/atom+xml; charset=utf-8");
                    Writer writer = res.getWriter();
                    serializeEntry(updatedEntry, writer);
                    writer.close();
                    res.setStatus(HttpServletResponse.SC_OK);
                    
                } else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (AtomException ae) {
                res.setStatus(ae.getStatus());
                mLogger.debug(ae);
            } catch (Exception ae) {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                mLogger.debug(ae);
            }
        } else {
            res.setHeader("WWW-Authenticate", "BASIC realm=\"Roller\"");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    
    //-----------------------------------------------------------------------------
    /**
     * Handle Atom DELETE by calling appropriate handler.
     */
    protected void doDelete(HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException {
        AtomHandler handler = createAtomRequestHandler(req);
        String userName = handler.getAuthenticatedUsername();
        if (userName != null) {
            String[] pathInfo = getPathInfo(req);
            try {
                if (handler.isEntryURI(pathInfo)) {
                    handler.deleteEntry(pathInfo);
                    res.setStatus(HttpServletResponse.SC_OK);
                } 
                else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (AtomException ae) {
                res.setStatus(ae.getStatus());
                mLogger.debug(ae);
            } catch (Exception ae) {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                mLogger.debug(ae);
            }
        } else {
            res.setHeader("WWW-Authenticate", "BASIC realm=\"Roller\"");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    
    //-----------------------------------------------------------------------------
    /**
     * Convenience method to return the PathInfo from the request.
     */
    protected String[] getPathInfo(HttpServletRequest request) {
        String mPathInfo = request.getPathInfo();
        mPathInfo = (mPathInfo!=null) ? mPathInfo : "";
        return StringUtils.split(mPathInfo,"/");
    }
    
    /**
     * Serialize entry to writer.
     */
    public static void serializeEntry(Entry entry, Writer writer)
    throws IllegalArgumentException, FeedException, IOException {
        // Build a feed containing only the entry
        List entries = new ArrayList();
        entries.add(entry);
        Feed feed1 = new Feed();
        feed1.setFeedType(AtomServlet.FEED_TYPE);
        feed1.setEntries(entries);
        
        // Get Rome to output feed as a JDOM document
        WireFeedOutput wireFeedOutput = new WireFeedOutput();
        Document feedDoc = wireFeedOutput.outputJDom(feed1);
        
        // Grab entry element from feed and get JDOM to serialize it
        Element entryElement= (Element)feedDoc.getRootElement().getChildren().get(0);
        
        // Add our own namespaced element, so we can determine if we can 
        // count on client to preserve foreign markup as it should.
        Element rollerElement = new Element("atom-draft", 
            "http://rollerweblogger.org/namespaces/app");
        rollerElement.setText("9");
        entryElement.addContent(rollerElement);
        
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        
        if (mLogger.isDebugEnabled()) {
            StringWriter sw = new StringWriter();
            outputter.output(entryElement, sw); 
            mLogger.debug(sw.toString());
            writer.write(sw.toString()); 
        } else {
            outputter.output(entryElement, writer);
        }        
    }
    
    /**
     * Parse entry from reader.
     */
    public static Entry parseEntry(Reader rd) 
        throws JDOMException, IOException, IllegalArgumentException, FeedException {
        // Parse entry into JDOM tree
        SAXBuilder builder = new SAXBuilder();
        Document entryDoc = builder.build(rd);
        Element fetchedEntryElement = entryDoc.getRootElement();
        fetchedEntryElement.detach();
        
        // Put entry into a JDOM document with 'feed' root so that Rome can handle it
        Feed feed = new Feed();
        feed.setFeedType(FEED_TYPE);
        WireFeedOutput wireFeedOutput = new WireFeedOutput();
        Document feedDoc = wireFeedOutput.outputJDom(feed);
        feedDoc.getRootElement().addContent(fetchedEntryElement);
        
        // Check for our special namespaced element. If it's there, then we 
        // know that client is not preserving foreign markup.
        Namespace ns = Namespace.getNamespace(
            "http://rollerweblogger.org/namespaces/app");
        Element rollerElement = fetchedEntryElement.getChild("atom-draft", ns);
        if (rollerElement == null) {
            mLogger.debug("Client is NOT preserving foreign markup");
        }
        
        WireFeedInput input = new WireFeedInput();
        Feed parsedFeed = (Feed)input.build(feedDoc);
        return (Entry)parsedFeed.getEntries().get(0);
    }
}
