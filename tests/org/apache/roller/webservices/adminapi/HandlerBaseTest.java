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
package org.apache.roller.webservices.adminapi;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.util.Base64;
import org.apache.roller.webservices.adminapi.sdk.MemberEntry;
import org.apache.roller.webservices.adminapi.sdk.MemberEntrySet;
import org.apache.roller.webservices.adminapi.sdk.UnexpectedRootElementException;
import org.apache.roller.webservices.adminapi.sdk.UserEntry;
import org.apache.roller.webservices.adminapi.sdk.UserEntrySet;
import org.apache.roller.webservices.adminapi.sdk.WeblogEntry;
import org.apache.roller.webservices.adminapi.sdk.WeblogEntrySet;
import org.jdom.JDOMException;

public abstract class HandlerBaseTest extends AappTest {
    public static class HttpResponse {
        private int status;
        private InputStream responseBody;
        
        public HttpResponse(int status) {
            this(status, null);
        }
        
        public HttpResponse(int status, InputStream responseBody) {
            this.status = status;
            this.responseBody = responseBody;
        }
        
        public int getStatus() {
            return status;
        }
        
        public InputStream getResponseBody() {
            return responseBody;
        }
    }
    
    protected void setUp() throws Exception {
        //System.err.println("HandlerTest.setUp()");
        try {
            deleteSampleMember(false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            deleteSampleWeblog(false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            deleteSampleUser(false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    protected void tearDown() throws Exception {
        //System.err.println("HandlerTest.tearDown()");
        try {
            deleteSampleMember(false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            deleteSampleWeblog(false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            deleteSampleUser(false);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    protected UserEntrySet createSampleUser() throws IOException, JDOMException, UnexpectedRootElementException {
        UserEntrySet ues = getSampleUserEntrySet();
        
        String url = ues.getHref();
        String user = getUser();
        String password = getPassword();
        
        String body = ues.toString();
        
        HttpResponse res = post(url, user, password, body);
        assertEquals(201, res.getStatus());
        
        UserEntrySet uesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
            uesResponse = new UserEntrySet(responseBody, getEndpointUrl());
        }
        
        return uesResponse;
    }
    
    protected UserEntrySet updateSampleUser() throws IOException, JDOMException, UnexpectedRootElementException {
        UserEntrySet ues = updateSampleUserEntrySet(getSampleUserEntrySet());
        
        String url = ues.getHref();
        String user = getUser();
        String password = getPassword();
        
        String body = ues.toString();
        
        HttpResponse res = put(url, user, password, body);
        assertEquals(200, res.getStatus());
        
        UserEntrySet uesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
            uesResponse = new UserEntrySet(responseBody, getEndpointUrl());
        }
        
        return uesResponse;
    }
    
    protected WeblogEntrySet updateSampleWeblog() throws IOException, JDOMException, UnexpectedRootElementException {
        WeblogEntrySet wes = updateSampleWeblogEntrySet(getSampleWeblogEntrySet());
        
        String url = wes.getHref();
        String user = getUser();
        String password = getPassword();
        
        String body = wes.toString();
        
        HttpResponse res = put(url, user, password, body);
        assertEquals(200, res.getStatus());
        
        WeblogEntrySet wesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
            wesResponse = new WeblogEntrySet(responseBody, getEndpointUrl());
        }
        
        return wesResponse;
    }
    
    protected MemberEntrySet updateSampleMember() throws IOException, JDOMException, UnexpectedRootElementException {
        MemberEntrySet mes = updateSampleMemberEntrySet(getSampleMemberEntrySet());
        
        String url = mes.getHref();
        String user = getUser();
        String password = getPassword();
        
        String body = mes.toString();
        
        HttpResponse res = put(url, user, password, body);
        assertEquals(200, res.getStatus());
        
        MemberEntrySet mesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
            mesResponse = new MemberEntrySet(responseBody, getEndpointUrl());
        }
        
        return mesResponse;
    }
    
    protected WeblogEntrySet createSampleWeblog() throws IOException, JDOMException, UnexpectedRootElementException {
        WeblogEntrySet wes = getSampleWeblogEntrySet();
        
        String url = wes.getHref();
        String user = getUser();
        String password = getPassword();
        
        String body = wes.toString();
        
        HttpResponse res = post(url, user, password, body);
        assertEquals(201, res.getStatus());
        
        WeblogEntrySet wesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
            wesResponse = new WeblogEntrySet(responseBody, getEndpointUrl());
        }
        
        return wesResponse;
    }
    
    protected MemberEntrySet createSampleMember() throws IOException, JDOMException, UnexpectedRootElementException {
        MemberEntrySet mes = getSampleMemberEntrySet();
        
        String url = mes.getHref();
        String user = getUser();
        String password = getPassword();
        
        String body = mes.toString();
        
        HttpResponse res = post(url, user, password, body);
        assertEquals(201, res.getStatus());
        
        MemberEntrySet mesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
            mesResponse = new MemberEntrySet(responseBody, getEndpointUrl());
        }
        
        return mesResponse;
    }
    
    protected UserEntrySet deleteSampleUser(boolean test) throws IOException, JDOMException, UnexpectedRootElementException {
        UserEntry ue = getSampleUserEntry();
        
        HttpResponse res = delete(ue.getHref(), getUser(), getPassword());
        if (test) {
            assertEquals(200, res.getStatus());
        } else if (res.getStatus() != 200) {
            return null;
        }
        
        UserEntrySet uesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
            uesResponse = new UserEntrySet(responseBody, getEndpointUrl());
        }
        
        return uesResponse;
    }
    
    protected UserEntrySet fetchSampleUser() throws IOException, JDOMException, UnexpectedRootElementException {
        UserEntry ue = getSampleUserEntry();
        
        HttpResponse res = get(ue.getHref(), getUser(), getPassword());
        assertEquals(200, res.getStatus());
        
        UserEntrySet uesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
            uesResponse = new UserEntrySet(responseBody, getEndpointUrl());
        }
        
        return uesResponse;
    }
    
    protected WeblogEntrySet fetchSampleWeblog() throws IOException, JDOMException, UnexpectedRootElementException {
        WeblogEntry we = getSampleWeblogEntry();
        
        HttpResponse res = get(we.getHref(), getUser(), getPassword());
        assertEquals(200, res.getStatus());
        
        WeblogEntrySet wesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
            wesResponse = new WeblogEntrySet(responseBody, getEndpointUrl());
        }
        
        return wesResponse;
    }
    
    protected MemberEntrySet fetchSampleMember() throws IOException, JDOMException, UnexpectedRootElementException {
        MemberEntry me = getSampleMemberEntry();
        
        HttpResponse res = get(me.getHref(), getUser(), getPassword());
        assertEquals(200, res.getStatus());
        
        MemberEntrySet mesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
            mesResponse = new MemberEntrySet(responseBody, getEndpointUrl());
        }
        
        return mesResponse;
    }
    
    protected WeblogEntrySet deleteSampleWeblog(boolean test) throws IOException, JDOMException, UnexpectedRootElementException {
        WeblogEntry we = getSampleWeblogEntry();
        
        HttpResponse res = delete(we.getHref(), getUser(), getPassword());
        if (test) {
            assertEquals(200, res.getStatus());
        } else if (res.getStatus() != 200) {
            return null;
        }
        
        WeblogEntrySet wesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
            wesResponse = new WeblogEntrySet(responseBody, getEndpointUrl());
        }
        
        return wesResponse;
    }
    
    protected MemberEntrySet deleteSampleMember(boolean test) throws IOException, JDOMException, UnexpectedRootElementException {
        MemberEntry me = getSampleMemberEntry();
        
        HttpResponse res = delete(me.getHref(), getUser(), getPassword());
        if (test) {
            assertEquals(200, res.getStatus());
        } else if (res.getStatus() != 200) {
            return null;
        }
        
        MemberEntrySet mesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
            mesResponse = new MemberEntrySet(responseBody, getEndpointUrl());
        }
        
        return mesResponse;
    }
    
    protected static HttpResponse post(String url, String user, String password, String body) throws IOException {
        HttpClient httpClient = new HttpClient();
        EntityEnclosingMethod method = new PostMethod(url);
        addAuthHeader(method, user, password);
        
        method.setRequestBody(body);
        
        String contentType = "application/xml; charset=utf-8";
        method.setRequestHeader("Content-type", contentType);
        
        int status = httpClient.executeMethod(method);
        InputStream responseBody = method.getResponseBodyAsStream();
        
        HttpResponse res = new HttpResponse(status, responseBody);
        return res;
    }
    
    protected static HttpResponse put(String url, String user, String password, String body) throws IOException {
        HttpClient httpClient = new HttpClient();
        EntityEnclosingMethod method = new PutMethod(url);
        addAuthHeader(method, user, password);
        
        method.setRequestBody(body);
        
        String contentType = "application/xml; charset=utf-8";
        method.setRequestHeader("Content-type", contentType);
        
        int status = httpClient.executeMethod(method);
        InputStream responseBody = method.getResponseBodyAsStream();
        
        HttpResponse res = new HttpResponse(status, responseBody);
        return res;
    }
    
    protected static HttpResponse get(String url, String user, String password) throws IOException {
        HttpClient httpClient = new HttpClient();
        HttpMethod method = new GetMethod(url);
        addAuthHeader(method, user, password);
        
        String contentType = "application/xml; charset=utf-8";
        method.setRequestHeader("Content-type", contentType);
        
        int status = httpClient.executeMethod(method);
        InputStream responseBody = method.getResponseBodyAsStream();
        
        HttpResponse res = new HttpResponse(status, responseBody);
        return res;
    }
    
    protected static HttpResponse delete(String url, String user, String password) throws IOException {
        HttpClient httpClient = new HttpClient();
        HttpMethod method = new DeleteMethod(url);
        addAuthHeader(method, user, password);
        
        String contentType = "application/xml; charset=utf-8";
        method.setRequestHeader("Content-type", contentType);
        
        int status = httpClient.executeMethod(method);
        InputStream responseBody = method.getResponseBodyAsStream();
        
        HttpResponse res = new HttpResponse(status, responseBody);
        return res;
    }
    
    private static void addAuthHeader(HttpMethod method, String user, String password) {
        String credentials = user + ":" + password;
        method.setRequestHeader("Authorization", "Basic "  + new String(Base64.encode(credentials.getBytes())));
    }
    
}

