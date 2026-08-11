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
 * limitations under the License.
 */
/*
 * Derived from com.rometools.propono.atom.server.AtomRequestImpl.
 * Forked to use jakarta.servlet instead of javax.servlet.
 */
package org.apache.roller.weblogger.webservices.atomprotocol;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import com.rometools.propono.atom.server.AtomRequest;

public class RollerAtomRequestImpl implements AtomRequest {

    private final HttpServletRequest wrapped;

    public RollerAtomRequestImpl(HttpServletRequest request) {
        this.wrapped = request;
    }

    @Override
    public String getPathInfo() {
        return wrapped.getPathInfo();
    }

    @Override
    public String getQueryString() {
        return wrapped.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return wrapped.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String role) {
        return wrapped.isUserInRole(role);
    }

    @Override
    public Principal getUserPrincipal() {
        return wrapped.getUserPrincipal();
    }

    @Override
    public String getRequestURI() {
        return wrapped.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return wrapped.getRequestURL();
    }

    @Override
    public int getContentLength() {
        return wrapped.getContentLength();
    }

    @Override
    public String getContentType() {
        return wrapped.getContentType();
    }

    @Override
    public String getParameter(String name) {
        return wrapped.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return wrapped.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return wrapped.getParameterValues(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getParameterMap() {
        return (Map) wrapped.getParameterMap();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return wrapped.getInputStream();
    }

    @Override
    public long getDateHeader(String name) {
        return wrapped.getDateHeader(name);
    }

    @Override
    public String getHeader(String name) {
        return wrapped.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return wrapped.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return wrapped.getHeaderNames();
    }

    @Override
    public int getIntHeader(String name) {
        return wrapped.getIntHeader(name);
    }
}
