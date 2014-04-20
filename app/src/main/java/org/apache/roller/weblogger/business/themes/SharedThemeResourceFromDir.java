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

package org.apache.roller.weblogger.business.themes;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import org.apache.roller.weblogger.pojos.ThemeResource;


/**
 * A FileManagerImpl specific implementation of a ThemeResource.
 *
 * ThemeResources from the FileManagerImpl are backed by a java.io.File
 * object which represents the resource on a filesystem.
 *
 * This class is internal to the FileManagerImpl class because there should
 * not be any external classes which need to construct their own instances
 * of this class.
 */
public class SharedThemeResourceFromDir 
        implements ThemeResource, Serializable {
    
    // the physical java.io.File backing this resource
    private File resourceFile = null;
    
    // the relative path of the resource within the theme
    private String relativePath = null;
    
    
    public SharedThemeResourceFromDir(String path, File file) {
        relativePath = path;
        resourceFile = file;
    }
    
    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ThemeResource other) {
        return getPath().compareTo(other.getPath());
    }
    
    
    public ThemeResource[] getChildren() {
        return null;
    }
    
    
    public String getName() {
        return resourceFile.getName();
    }
    
    public String getPath() {
        return relativePath;
    }
    
    public long getLastModified() {
        return resourceFile.lastModified();
    }
    
    public long getLength() {
        return resourceFile.length();
    }
    
    public boolean isDirectory() {
        return resourceFile.isDirectory();
    }
    
    public boolean isFile() {
        return resourceFile.isFile();
    }
    
    public InputStream getInputStream() {
        try {
            return new FileInputStream(resourceFile);
        } catch (java.io.FileNotFoundException ex) {
            // should never happen, rethrow as runtime exception
            throw new RuntimeException("Error constructing input stream", ex);
        }
    }
    
}
