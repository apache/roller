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
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.util.Base64;
import org.apache.roller.webservices.adminapi.sdk.MemberEntry;
import org.apache.roller.webservices.adminapi.sdk.MemberEntrySet;
import org.apache.roller.webservices.adminapi.sdk.MissingElementException;
import org.apache.roller.webservices.adminapi.sdk.UnexpectedRootElementException;
import org.apache.roller.webservices.adminapi.sdk.UserEntry;
import org.apache.roller.webservices.adminapi.sdk.UserEntrySet;
import org.apache.roller.webservices.adminapi.sdk.WeblogEntry;
import org.apache.roller.webservices.adminapi.sdk.WeblogEntrySet;
import org.jdom.JDOMException;

public class AappTest extends TestCase {
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
    
    private static final Date sampleDate = new Date();
    private static final String DEFAULT_ENDPOINT_URL = "http://localhost:8080/roller/aapp";
    private static final String DEFAULT_USER = "jtb";
    private static final String DEFAULT_PASSWORD = "iplanet";
    
    protected static String getEndpointUrl() {
        String endpoint = System.getProperty("aapp.endpoint");
        if (endpoint == null) {
            endpoint = DEFAULT_ENDPOINT_URL;
        }
        
        System.err.println("endpoint=" + endpoint);
        return endpoint;
    }

    protected static String getUser() {
        String user = System.getProperties().getProperty("aapp.user");
        if (user == null) {
            user = DEFAULT_USER;
        }
        
        System.err.println("user=" + user);
        return user;
    }

    protected static String getPassword() {
        String password = System.getProperties().getProperty("aapp.password");
        if (password == null) {
            password = DEFAULT_PASSWORD;
        }
        
        System.err.println("password=" + password);        
        return password;
    }

    protected static UserEntry getSampleUserEntry() {
        UserEntry ue = new UserEntry("foo", getEndpointUrl());
        ue.setEmailAddress("foo@bar.org");
        ue.setFullName("Foo Bar");
        ue.setLocale(Locale.getDefault());
        ue.setTimezone(TimeZone.getDefault());
        ue.setPassword("foo");
        
        return ue;
    }

    protected static MemberEntry getSampleMemberEntry() {
        MemberEntry me = new MemberEntry("fooblog", "foo", getEndpointUrl());
        me.setPermission(MemberEntry.Permissions.AUTHOR);
        return me;
    }
    
    protected static UserEntry updateSampleUserEntry(UserEntry ue) {
        UserEntry ueUpdate = new UserEntry(ue.getName(), getEndpointUrl());
        ueUpdate.setEmailAddress("billy@bob.org");
        ueUpdate.setFullName("Billy Bob");
        ueUpdate.setLocale(new Locale("ms", "MY"));
        ueUpdate.setTimezone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        ueUpdate.setPassword("billy");
        
        return ueUpdate;
    }

    protected static WeblogEntry updateSampleWeblogEntry(WeblogEntry we) {
        WeblogEntry weUpdate = new WeblogEntry(we.getHandle(), getEndpointUrl());
        weUpdate.setEmailAddress("billy@bob.org");
        weUpdate.setName("Billy Bob Weblog Name");
        weUpdate.setLocale(new Locale("ms", "MY"));
        weUpdate.setTimezone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        weUpdate.setDescription("Billy Bob Weblog Description");
        weUpdate.setCreatingUser(we.getCreatingUser());
        
        return weUpdate;
    }

    protected static MemberEntry updateSampleMemberEntry(MemberEntry me) {
        MemberEntry meUpdate = new MemberEntry(me.getHandle(), me.getName(), getEndpointUrl());
        meUpdate.setPermission(MemberEntry.Permissions.LIMITED);
        
        return meUpdate;
    }
    
    protected static UserEntrySet updateSampleUserEntrySet(UserEntrySet ues) {
        UserEntry ue = (UserEntry)ues.getEntries()[0];
        UserEntry ueUpdated = updateSampleUserEntry(ue);        
        UserEntrySet uesUpdated = new UserEntrySet(getEndpointUrl());
        uesUpdated.setEntries(new UserEntry[] { ueUpdated });
        
        return uesUpdated;
    }
    
    protected static WeblogEntrySet updateSampleWeblogEntrySet(WeblogEntrySet wes) {
        WeblogEntry we = (WeblogEntry)wes.getEntries()[0];
        WeblogEntry weUpdated = updateSampleWeblogEntry(we);        
        WeblogEntrySet wesUpdated = new WeblogEntrySet(getEndpointUrl());
        wesUpdated.setEntries(new WeblogEntry[] { weUpdated });
        
        return wesUpdated;
    }

    protected static MemberEntrySet updateSampleMemberEntrySet(MemberEntrySet mes) {
        MemberEntry me = (MemberEntry)mes.getEntries()[0];
        MemberEntry meUpdated = updateSampleMemberEntry(me);        
        MemberEntrySet mesUpdated = new MemberEntrySet(getEndpointUrl());
        mesUpdated.setEntries(new MemberEntry[] { meUpdated });
        
        return mesUpdated;
    }

    protected static WeblogEntry getSampleWeblogEntry() {
        WeblogEntry we = new WeblogEntry("fooblog", getEndpointUrl());
        we.setEmailAddress("foo@bar.org");
        we.setCreatingUser("foo");
        we.setDescription("Foo Weblog Description");
        we.setLocale(Locale.getDefault());
        we.setTimezone(TimeZone.getDefault());
        we.setName("Foo Weblog Name");
        
        return we;
    }    
    
    protected static UserEntrySet getSampleUserEntrySet() {
        UserEntry ue = getSampleUserEntry();
        UserEntrySet ues = new UserEntrySet(getEndpointUrl());
        ues.setEntries(new UserEntry[] { ue });
        
        return ues;
    }
    
    protected static WeblogEntrySet getSampleWeblogEntrySet() {
        WeblogEntry we = getSampleWeblogEntry();
        WeblogEntrySet wes = new WeblogEntrySet(getEndpointUrl());
        wes.setEntries(new WeblogEntry[] { we });
        
        return wes;
    }
    
    protected static MemberEntrySet getSampleMemberEntrySet() {
        MemberEntry me = getSampleMemberEntry();
        MemberEntrySet mes = new MemberEntrySet(getEndpointUrl());
        mes.setEntries(new MemberEntry[] { me });
        
        return mes;
    }

    protected UserEntrySet createSampleUser() throws IOException, JDOMException, MissingElementException, UnexpectedRootElementException {
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

    protected UserEntrySet updateSampleUser() throws IOException, JDOMException, MissingElementException, UnexpectedRootElementException {
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

    protected WeblogEntrySet updateSampleWeblog() throws IOException, JDOMException, MissingElementException, UnexpectedRootElementException {
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

    protected MemberEntrySet updateSampleMember() throws IOException, JDOMException, MissingElementException, UnexpectedRootElementException {
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
    
    protected WeblogEntrySet createSampleWeblog() throws IOException, JDOMException, MissingElementException, UnexpectedRootElementException {
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

    protected MemberEntrySet createSampleMember() throws IOException, JDOMException, MissingElementException, UnexpectedRootElementException {
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
    
    protected UserEntrySet deleteSampleUser() throws IOException, JDOMException, MissingElementException, UnexpectedRootElementException {
        UserEntry ue = getSampleUserEntry();
        
        HttpResponse res = delete(ue.getHref(), getUser(), getPassword());
        assertEquals(200, res.getStatus());
        
        UserEntrySet uesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
           uesResponse = new UserEntrySet(responseBody, getEndpointUrl());
        }
        
        return uesResponse;
    }

    protected UserEntrySet fetchSampleUser() throws IOException, JDOMException, MissingElementException, UnexpectedRootElementException {
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

    protected WeblogEntrySet fetchSampleWeblog() throws IOException, JDOMException, MissingElementException, UnexpectedRootElementException {
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

    protected MemberEntrySet fetchSampleMember() throws IOException, JDOMException, MissingElementException, UnexpectedRootElementException {
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
    
    protected WeblogEntrySet deleteSampleWeblog() throws IOException, JDOMException, MissingElementException, UnexpectedRootElementException {
        WeblogEntry we = getSampleWeblogEntry();
        
        HttpResponse res = delete(we.getHref(), getUser(), getPassword());
        assertEquals(200, res.getStatus());
        
        WeblogEntrySet wesResponse = null;
        InputStream responseBody = res.getResponseBody();
        if (responseBody != null) {
           wesResponse = new WeblogEntrySet(responseBody, getEndpointUrl());
        }
        
        return wesResponse;
    }

    protected MemberEntrySet deleteSampleMember() throws IOException, JDOMException, MissingElementException, UnexpectedRootElementException {
        MemberEntry me = getSampleMemberEntry();
        
        HttpResponse res = delete(me.getHref(), getUser(), getPassword());
        assertEquals(200, res.getStatus());
        
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
        
        String contentType = "application/xml; charset=utf8";
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
        
        String contentType = "application/xml; charset=utf8";
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
        
        String contentType = "application/xml; charset=utf8";
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
                
        String contentType = "application/xml; charset=utf8";
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
