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
package org.roller.presentation.atomapi;

import java.io.FileInputStream;
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
import org.roller.util.Utilities;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.WireFeedOutput;
import java.io.StringWriter;

/**
 * Atom Servlet implements Atom by calling a Roller independent handler.
 * @web.servlet name="AtomServlet"
 * @web.servlet-mapping url-pattern="/app05/*"
 * @author David M Johnson
 */
public class AtomServlet extends HttpServlet
{
    public static final String FEED_TYPE = "atom_1.0"; 
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(AtomServlet.class);

    //-----------------------------------------------------------------------------
    /**
     * Create an Atom request handler.
     * TODO: make AtomRequestHandler implementation configurable.
     */
    private AtomHandler createAtomRequestHandler(HttpServletRequest request)
    {
        return new RollerAtomHandler(request);   
    }
    
    //-----------------------------------------------------------------------------
    /**
     * Handles an Atom GET by calling handler and writing results to response.
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        AtomHandler handler = createAtomRequestHandler(req);
        String userName = handler.getAuthenticatedUsername();
        if (userName != null) 
        {
            String[] pathInfo = getPathInfo(req);
            try
            {
                if (handler.isIntrospectionURI(pathInfo)) 
                {
                    // return an Atom Service document
                    AtomService service = handler.getIntrospection(pathInfo);                   
                    Document doc = AtomService.serviceToDocument(service);
                    Writer writer = res.getWriter();
                    XMLOutputter outputter = new XMLOutputter();
                    outputter.setFormat(Format.getPrettyFormat());
                    outputter.output(doc, writer);
                    writer.close();
                    res.setStatus(HttpServletResponse.SC_OK);
                }
                else if (handler.isCollectionURI(pathInfo))
                {
                    // return a collection
                    String ranges = req.getHeader("Range");
                    if (ranges == null) req.getParameter("Range");
                    AtomCollection col = null;
                    if (ranges != null) 
                    {
                        /* // return a range of collection members
                        AtomCollection.Range range = 
                            AtomCollection.parseRange(req.getHeader("Range"));
                        int offset = 0;
                        String offsetString = req.getParameter("offset");
                        if (offsetString != null) 
                        {
                            offset = Integer.parseInt(offsetString);
                        }
                        col= handler.getCollection(
                            pathInfo, range.start, range.end, offset); */
                    }
                    else 
                    {
                        col= handler.getCollection(pathInfo);
                    }
                    // serialize collection to XML and write it out
                    Document doc = AtomCollection.collectionToDocument(col);
                    Writer writer = res.getWriter();
                    XMLOutputter outputter = new XMLOutputter();
                    outputter.setFormat(Format.getPrettyFormat());
                    outputter.output(doc, writer);
                    writer.close();
                    res.setStatus(HttpServletResponse.SC_OK);
                }
                else if (handler.isEntryURI(pathInfo)) 
                {
                    // return an entry
                    Entry entry = handler.getEntry(pathInfo);                    
                    Writer writer = res.getWriter(); 
                    serializeEntry(entry, writer);                    
                    writer.close();
                }
                else if (handler.isResourceURI(pathInfo))
                {
                    // return a resource
                    String absPath = handler.getResourceFilePath(pathInfo);
                    String type = getServletContext().getMimeType(absPath);
                    res.setContentType(type);
                    Utilities.copyInputToOutput(
                        new FileInputStream(absPath), res.getOutputStream());
                }
                else
                {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }
            catch (Exception e)
            {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                e.printStackTrace(res.getWriter());
                mLogger.error(e);
            }
        }
        else 
        {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    
    //-----------------------------------------------------------------------------  
    /**
     * Handles an Atom POST by calling handler to identify URI, reading/parsing
     * data, calling handler and writing results to response.
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        AtomHandler handler = createAtomRequestHandler(req);
        String userName = handler.getAuthenticatedUsername();
        if (userName != null) 
        {
            String[] pathInfo = getPathInfo(req);
            try
            {
                if (handler.isEntryCollectionURI(pathInfo)) 
                {
                    /* // parse incoming entry                    
                    Entry unsavedEntry = parseEntry(
                        new InputStreamReader(req.getInputStream()));
                    
                    // call handler to post it
                    Entry savedEntry = handler.postEntry(pathInfo, unsavedEntry);
                    Iterator links = savedEntry.getLinks().iterator();
                    
                    // return alternate link as Location header
                    while (links.hasNext()) {
                        Link link = (Link) links.next();
                        if (link.getRel().equals("alternate") || link.getRel() == null) {
                            res.addHeader("Location", link.getHref());
                            break;
                        }
                    }                  
                    // write entry back out to response
                    res.setStatus(HttpServletResponse.SC_CREATED);
                    Writer writer = res.getWriter(); 
                    serializeEntry(savedEntry, writer);                    
                    writer.close(); */
                }
                else if (handler.isResourceCollectionURI(pathInfo)) 
                {
                    // get incoming file name from HTTP header
                    String name = req.getHeader("Name");
                    
                    // hand input stream of to hander to post file
                    String location = handler.postResource(
                       pathInfo, name, req.getContentType(), req.getInputStream());
                    res.setStatus(HttpServletResponse.SC_CREATED);
                    res.setHeader("Location", location);
                }
                else
                {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }
            catch (Exception e)
            {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                e.printStackTrace(res.getWriter());
                mLogger.error(e);
            }
        }
        else 
        {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    //-----------------------------------------------------------------------------    
    /**
     * Handles an Atom PUT by calling handler to identify URI, reading/parsing
     * data, calling handler and writing results to response.
     */
    protected void doPut(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        AtomHandler handler = createAtomRequestHandler(req);
        String userName = handler.getAuthenticatedUsername();
        if (userName != null) 
        {
            String[] pathInfo = getPathInfo(req);
            try
            {
                if (handler.isEntryURI(pathInfo)) 
                {
                    // parse incoming entry
                    Entry unsavedEntry = parseEntry(
                        new InputStreamReader(req.getInputStream()));
                    
                    // call handler to put entry
                    Entry updatedEntry = handler.putEntry(pathInfo, unsavedEntry);
                    
                    // write entry back out to response
                    Writer writer = res.getWriter(); 
                    serializeEntry(updatedEntry, writer);                    
                    res.setStatus(HttpServletResponse.SC_OK);
                    writer.close();
                }
                else if (handler.isResourceCollectionURI(pathInfo)) 
                {
                    // handle input stream to handler
                    handler.putResource(
                        pathInfo, req.getContentType(), req.getInputStream());
                    res.setStatus(HttpServletResponse.SC_OK);
                }
                else
                {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }
            catch (Exception e)
            {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                e.printStackTrace(res.getWriter());
                mLogger.error(e);
            }
        }
        else 
        {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    //-----------------------------------------------------------------------------
    /**
     * Handle Atom DELETE by calling appropriate handler.
     */
    protected void doDelete(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException
    {
        AtomHandler handler = createAtomRequestHandler(req);
        String userName = handler.getAuthenticatedUsername();
        if (userName != null) 
        {
            String[] pathInfo = getPathInfo(req);
            try
            {
                if (handler.isEntryURI(pathInfo)) 
                {
                    handler.deleteEntry(pathInfo); 
                    res.setStatus(HttpServletResponse.SC_OK);
                }
                else if (handler.isResourceURI(pathInfo)) 
                {
                    handler.deleteResource(pathInfo); 
                    res.setStatus(HttpServletResponse.SC_OK);
                }
                else
                {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            }
            catch (Exception e)
            {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                e.printStackTrace(res.getWriter());
                mLogger.error(e);
            }
        }
        else 
        {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    
    //-----------------------------------------------------------------------------
    /**
     * Convenience method to return the PathInfo from the request.  
     */
    protected String[] getPathInfo(HttpServletRequest request)
    {
        String mPathInfo = request.getPathInfo();
        mPathInfo = (mPathInfo!=null) ? mPathInfo : "";
        return StringUtils.split(mPathInfo,"/");   
    }

    /** 
     * Utility method to make up for a Rome shortcoming:
     * Rome can only serialize entire feeds, not individual elements
     */
    public static void serializeEntry(Entry entry, Writer writer) 
        throws IllegalArgumentException, FeedException, IOException
    {
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
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        
        StringWriter sw = new StringWriter();  // DEBUG
        outputter.output(entryElement, sw);    // DEBUG
        System.out.println(sw.toString());     // DEBUG    
        writer.write(sw.toString());           // DEBUG
        
        //outputter.output(entryElement, writer);
    }
    
    /** 
     * Utility method to make up for a Rome shortcoming:
     * Rome can only parse Atom data with XML document root 'feed'
     */
    public static Entry parseEntry(Reader rd) 
        throws JDOMException, IOException, IllegalArgumentException, FeedException 
    {
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
        
        WireFeedInput input = new WireFeedInput();
        Feed parsedFeed = (Feed)input.build(feedDoc);
        return (Entry)parsedFeed.getEntries().get(0);
    }
}
