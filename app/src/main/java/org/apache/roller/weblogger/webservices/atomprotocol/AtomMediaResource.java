/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
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
import java.util.Date;

/**
 * Holder for the binary data of a media resource, returned when a client GETs
 * the edit-media URI so the dispatcher servlet can stream the bytes.
 */
public class AtomMediaResource {

    private final String name;
    private final long contentLength;
    private final String contentType;
    private final Date lastModified;
    private final InputStream inputStream;

    public AtomMediaResource(String name, long contentLength, String contentType,
            Date lastModified, InputStream inputStream) {
        this.name = name;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.lastModified = lastModified;
        this.inputStream = inputStream;
    }

    public String getName() {
        return name;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
