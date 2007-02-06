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

package org.apache.roller.pojos;

import java.io.InputStream;


/**
 * Represents a static resource file uploaded to a weblog.
 *
 * The main reason for having this interface is so that we can provide a layer 
 * of abstraction between how Roller uses uploaded resource files and how those
 * files are physically being managed by the underlying code.
 */
public interface WeblogResource {
    
    /**
     * The weblog this resource is attached to.
     *
     * @returns The weblog object owning this resource.
     */
    public WebsiteData getWeblog();
    
    
    /**
     * The name of this resource.
     * i.e. "some.jpg"
     *
     * @returns The short name for the resource.
     */
    public String getName();
    
    
    /**
     * The path to this resource, relative to the weblog's uploads area.
     * i.e. "images/some.jpg"
     *
     * @returns The path to the resource, relative to the weblog's uploads area.
     */
    public String getPath();
    
    
    /**
     * The last-modified time for this resource.
     *
     * @returns The last time the resource changed, as a long value.
     */
    public long getLastModified();
    
    
    /**
     * The length of this resource, in bytes.
     *
     * @returns The length of the resource in bytes.
     */
    public long getLength();
    
    
    /**
     * Does this resource represent a directory?  True if yes, False otherwise.
     *
     * @returns True if the resource is a directory, False otherwise.
     */
    public boolean isDirectory();
    
    
    /**
     * Does this resource represent a file?  True if yes, False otherwise.
     *
     * @returns True if the resource is a file, False otherwise.
     */
    public boolean isFile();
    
    
    /**
     * List child resources if this resource represents a directory.
     *
     * The children returned by this method should only be actual files.  No
     * directories should be returned by this method.
     *
     * @returns null if resource is not a directory, otherwise a WeblogResource[].
     */
    public WeblogResource[] getChildren();
    
    
    /**
     * The web url where the resource can be accessed, optionally absolute.
     *
     * @param absolute should the url be absolute or not?
     *
     * @returns a String representing the web url to this resource.
     */
    public String getURL(boolean absolute);
    
    
    /**
     * An InputStream that the resource can be read from.
     *
     * @returns an InputStream for the resource.
     */
    public InputStream getInputStream();
    
}
