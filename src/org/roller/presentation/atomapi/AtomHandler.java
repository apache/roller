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

import java.io.InputStream;
import java.util.Date;

import com.sun.syndication.feed.atom.Entry;

/**
 * Interface to be supported by an Atom server, expected lifetime: one request.
 * AtomServlet calls this generic interface instead of Roller specific APIs. 
 * Does not impose any specific set of collections, just three collection types: 
 * entries, resources and categories. Implementations determine what collections 
 * of each type exist and what URIs are used to get and edit them.
 * <p />
 * Designed to be Roller independent.
 * 
 * @author David M Johnson
 */
public interface AtomHandler
{   
    /** Get username of authenticated user */
    public String getAuthenticatedUsername();    

    /**
     * Return introspection document
     */
    public AtomService getIntrospection(String[] pathInfo) throws Exception;
    
    /**
     * Return collection
     * @param pathInfo Used to determine which collection
     */   
    public AtomCollection getCollection(String[] pathInfo) throws Exception;
    
    /**
     * Return collection restricted by date range
     * @param pathInfo Used to determine which collection
     * @param start    Start date or null if none
     * @param end      End date or null of none
     * @param offset   Offset into query results (or -1 if none)
     */
    public AtomCollection getCollection(
            String[] pathInfo, Date start, Date end, int offset) 
        throws Exception; 
    
    /**
     * Create a new entry specified by pathInfo and posted entry.
     * @param pathInfo Path info portion of URL
     */
    public Entry postEntry(String[] pathInfo, Entry entry) throws Exception;

    /**
     * Get entry specified by pathInfo.
     * @param pathInfo Path info portion of URL
     */
    public Entry getEntry(String[] pathInfo) throws Exception;
    
    /**
     * Update entry specified by pathInfo and posted entry.
     * @param pathInfo Path info portion of URL
     */
    public Entry putEntry(String[] pathInfo, Entry entry) throws Exception;

    /**
     * Delete entry specified by pathInfo.
     * @param pathInfo Path info portion of URL
     */
    public void deleteEntry(String[] pathInfo) throws Exception;
    
    /**
     * Create a new resource specified by pathInfo, contentType, and binary data
     * @param pathInfo Path info portion of URL
     * @param contentType MIME type of uploaded content
     * @param data Binary data representing uploaded content
     */
    public String postResource(String[] pathInfo, String name, String contentType, 
            InputStream is) throws Exception;

    /**
     * Update a resource.
     * @param pathInfo Path info portion of URL
     */
    public void putResource(String[] pathInfo, String contentType, 
            InputStream is) throws Exception;
    
    /**
     * Delete resource specified by pathInfo.
     * @param pathInfo Path info portion of URL
     */
    public void deleteResource(String[] pathInfo) throws Exception;
    
    /**
     * Get resource file path (so Servlet can determine MIME type).
     * @param pathInfo Path info portion of URL
     */
    public String getResourceFilePath(String[] pathInfo) throws Exception;
    
    public boolean isIntrospectionURI(String [] pathInfo);  
 
    public boolean isCollectionURI(String [] pathInfo);   
    public boolean isEntryCollectionURI(String [] pathInfo);   
    public boolean isResourceCollectionURI(String [] pathInfo);   
    public boolean isCategoryCollectionURI(String [] pathInfo);  
    
    public boolean isEntryURI(String[] pathInfo);
    public boolean isResourceURI(String[] pathInfo);
    public boolean isCategoryURI(String[] pathInfo);
}

