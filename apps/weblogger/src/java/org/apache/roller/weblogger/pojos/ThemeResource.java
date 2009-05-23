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


/**
 * A Resource that is attached to a Theme.
 */
public interface ThemeResource extends Resource {
    
    /**
     * Does this resource represent a directory?  True if yes, False otherwise.
     *
     * @return True if the resource is a directory, False otherwise.
     */
    public boolean isDirectory();
    
    
    /**
     * Does this resource represent a file?  True if yes, False otherwise.
     *
     * @return True if the resource is a file, False otherwise.
     */
    public boolean isFile();
    
    
    /**
     * List child resources if this resource represents a directory.
     *
     * The children returned by this method should only be actual files.  No
     * directories should be returned by this method.
     *
     * @return null if resource is not a directory, otherwise a WeblogResource[].
     */
    public ThemeResource[] getChildren();
    
}
