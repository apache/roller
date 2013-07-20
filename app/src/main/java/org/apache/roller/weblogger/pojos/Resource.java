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

package org.apache.roller.weblogger.pojos;

import java.io.InputStream;


/**
 * Represents a static resource of some kind.
 */
public interface Resource {
    
    
    /**
     * The short name of this resource.
     * i.e. "some.jpg"
     *
     * @return The short name for the resource.
     */
    String getName();
    
    
    /**
     * The path to this resource, relative to its container.
     * i.e. "images/some.jpg"
     *
     * @return The path to the resource, relative to its container.
     */
    String getPath();
    
    
    /**
     * The last-modified time for this resource.
     *
     * @return The last time the resource changed, as a long value.
     */
    long getLastModified();
    
    
    /**
     * The length of this resource, in bytes.
     *
     * @return The length of the resource in bytes.
     */
    long getLength();
    
    
    /**
     * An InputStream that the resource can be read from.
     *
     * @return an InputStream for the resource.
     */
    InputStream getInputStream();
    
}
