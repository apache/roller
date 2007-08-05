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
package org.apache.roller.weblogger.webservices.atomprotocol;

import java.io.InputStream;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;

/**
 * Interface to be supported by an Atom server, expected lifetime: one request.
 * AtomServlet calls this generic interface instead of Roller specific APIs. 
 * <p />
 * Based on: draft-ietf-atompub-protocol-14.txt
 * <p />
 * Designed to be Roller independent.
 */
public interface AtomHandler
{   
    /** Get username of authenticated user */
    public String getAuthenticatedUsername();    

    /**
     * Return introspection document
     */
    public AtomService getIntrospection() throws AtomException;
    
    /**
     * Return collection
     * @param pathInfo Used to determine which collection and range
     */   
    public Feed getCollection(String[] pathInfo) throws AtomException;
    
    /**
     * Create a new entry specified by pathInfo and posted entry.
     * @param pathInfo Path info portion of URL
     */
    public Entry postEntry(String[] pathInfo, Entry entry) throws AtomException;

    /**
     * Get entry specified by pathInfo.
     * @param pathInfo Path info portion of URL
     */
    public Entry getEntry(String[] pathInfo) throws AtomException;
    
    /**
     * Update entry specified by pathInfo and posted entry.
     * @param pathInfo Path info portion of URL
     */
    public Entry putEntry(String[] pathInfo, Entry entry) throws AtomException;
    

    /**
     * Delete entry specified by pathInfo.
     * @param pathInfo Path info portion of URL
     */
    public void deleteEntry(String[] pathInfo) throws AtomException;
    
    /**
     * Get media resource specified by pathInfo.
     * @param pathInfo Path info portion of URL
     */
    public AtomMediaResource getMediaResource(String[] pathInfo) throws AtomException;
    
    /**
     * Create a new media-link entry.
     * @param pathInfo Path info portion of URL
     * @param contentType MIME type of uploaded content
     * @param data Binary data representing uploaded content
     */
    public Entry postMedia(
        String[] pathInfo, String title, String slug, String contentType, InputStream is) throws AtomException;

    /**
     * Update the media file part of a media-link entry.
     * @param pathInfo Path info portion of URL
     */
    public Entry putMedia(
        String[] pathInfo, String contentType, InputStream is) throws AtomException;
        
    /**
     * Return true if specified pathinfo represents URI of introspection doc.
     */
    public boolean isIntrospectionURI(String [] pathInfo);  
 
    /**
     * Return true if specified pathinfo represents URI of a collection.
     */
    public boolean isCollectionURI(String [] pathInfo);   
     
    /**
     * Return true if specified pathinfo represents URI of an Atom entry.
     */
    public boolean isEntryURI(String[] pathInfo);
        
    /**
     * Return true if specified pathinfo represents media-edit URI.
     */
    public boolean isMediaEditURI(String[] pathInfo);
}

