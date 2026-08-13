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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

/**
 * Lightweight wrapper around an {@link HttpServletRequest} for AtomPub handlers.
 * The dispatcher servlet reads any request body once into a byte array and
 * passes it here so handlers can read it (and so {@code getPathInfo()} never
 * returns null for the service-document URI).
 */
public class AtomRequest {

    private static final byte[] EMPTY = new byte[0];

    private final HttpServletRequest request;
    private final byte[] body;

    public AtomRequest(HttpServletRequest request, byte[] body) {
        this.request = request;
        this.body = (body != null) ? body : EMPTY;
    }

    /** Path info relative to the AtomPub servlet, never null ("" for the service doc). */
    public String getPathInfo() {
        String pathInfo = request.getPathInfo();
        return (pathInfo != null) ? pathInfo : "";
    }

    public String getHeader(String name) {
        return request.getHeader(name);
    }

    public String getContentType() {
        return request.getContentType();
    }

    /** A fresh stream over the buffered request body. */
    public InputStream getInputStream() {
        return new ByteArrayInputStream(body);
    }

    public HttpServletRequest getRequest() {
        return request;
    }
}
