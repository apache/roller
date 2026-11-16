/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.pojos;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


/**
 * Represents the content of a file from file system.
 *
 */
public class FileContent {
    
    // the physical java.io.File backing this resource
    private File resourceFile = null;
    
    // the file Id of the resource
    private String fileId = null;
    
    // the weblog the resource is attached to
    private Weblog weblog = null;
    
    
    public FileContent(Weblog weblog, String fileId, File file) {
        this.weblog = weblog;
        this.fileId = fileId;
        this.resourceFile = file;
    }
    
    public Weblog getWeblog() {
        return weblog;
    }
    
    public String getName() {
        return resourceFile.getName();
    }
    
    public String getFileId() {
        return fileId;
    }
    
    public long getLastModified() {
        return resourceFile.lastModified();
    }
    
    public long getLength() {
        return resourceFile.length();
    }
    
    /**
     * Returns the input stream for the underlying file.
     */
    public InputStream getInputStream() {
        try {
            return new FileInputStream(resourceFile);
        } catch (java.io.FileNotFoundException ex) {
            // should never happen, rethrow as runtime exception
            throw new RuntimeException("Error constructing input stream", ex);
        }
    }
}
