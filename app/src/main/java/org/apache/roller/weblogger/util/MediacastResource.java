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

package org.apache.roller.weblogger.util;


/**
 * An external 'mediacast' resource, typically a podcast, video, etc.
 *
 * This class is mainly used by weblog entries to track external resources used
 * in postings via enclosures.
 */
public class MediacastResource {
    
    private String url = null;
    private String contentType = null;
    private long length = 0;
    
    
    public MediacastResource(String u, String c, long l) {
        this.setUrl(u);
        this.setContentType(c);
        this.setLength(l);
    }

    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
    
    
    public String toString() {
        StringBuilder buf = new StringBuilder();
        
        buf.append("url = ").append(getUrl()).append("\n");
        buf.append("contentType = ").append(getContentType()).append("\n");
        buf.append("length = ").append(getLength()).append("\n");
        
        return buf.toString();
    }
    
}
