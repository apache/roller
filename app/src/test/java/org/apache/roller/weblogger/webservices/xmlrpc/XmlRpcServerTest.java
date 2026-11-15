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

package org.apache.roller.weblogger.webservices.xmlrpc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class XmlRpcServerTest {

    /** Stands in for BloggerAPIHandler. */
    public static class ParentHandler {
        public String echo(String text) {
            return text;
        }

        public String publish(String postid, int publish) {
            return "int:" + publish;
        }

        public String publish(String postid, boolean publish) {
            return "boolean:" + publish;
        }

        public String describe(Hashtable<String, ?> struct) {
            Object[] categories = (Object[]) struct.get("categories");
            return struct.get("title") + "/" + categories.length;
        }

        public Object refuse() throws XmlRpcException {
            throw new XmlRpcException(BaseAPIHandler.UPLOAD_DENIED_EXCEPTION, "Upload denied");
        }

        public Object breaks() {
            throw new IllegalStateException("internal detail");
        }

        public Object nothing() {
            return null;
        }

        public void noResult() {
            // void methods are not callable
        }
    }

    /** Stands in for MetaWeblogAPIHandler, which extends BloggerAPIHandler. */
    public static class ChildHandler extends ParentHandler {
        public String own() {
            return "own";
        }
    }

    private final XmlRpcServer server = new XmlRpcServer(Map.of(
            "parent", ParentHandler.class, "child", ChildHandler.class));

    private String call(String method, String... values) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?><methodCall><methodName>")
                .append(method).append("</methodName><params>");
        for (String value : values) {
            xml.append("<param><value>").append(value).append("</value></param>");
        }
        xml.append("</params></methodCall>");
        return new String(server.execute(xml.toString().getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }

    private static void assertResult(String response, String expectedValue) {
        assertTrue(response.contains("<params><param><value>" + expectedValue + "</value></param></params>"),
                response);
    }

    private static void assertFault(String response, int code, String message) {
        assertTrue(response.contains("<fault>"), response);
        assertTrue(response.contains("<name>faultCode</name><value><int>" + code + "</int></value>"), response);
        assertTrue(response.contains("<name>faultString</name><value><string>" + message + "</string></value>"),
                response);
    }

    @Test
    public void callsTheNamedMethod() {
        assertResult(call("parent.echo", "<string>hello</string>"), "<string>hello</string>");
    }

    @Test
    public void picksTheOverloadThatFitsTheValues() {
        assertResult(call("parent.publish", "<string>p</string>", "<int>1</int>"), "<string>int:1</string>");
        assertResult(call("parent.publish", "<string>p</string>", "<boolean>1</boolean>"),
                "<string>boolean:true</string>");
    }

    @Test
    public void exposesInheritedMethodsUnderTheSubclassPrefix() {
        assertResult(call("child.echo", "<string>hi</string>"), "<string>hi</string>");
        assertResult(call("child.own"), "<string>own</string>");
        assertFault(call("parent.own"), 0, "No such handler: parent.own");
    }

    @Test
    public void passesStructsAsHashtablesAndArraysAsObjectArrays() {
        String struct = "<struct>"
                + "<member><name>title</name><value><string>Post</string></value></member>"
                + "<member><name>categories</name><value><array><data>"
                + "<value><string>a</string></value><value><string>b</string></value>"
                + "</data></array></value></member>"
                + "</struct>";
        assertResult(call("parent.describe", struct), "<string>Post/2</string>");
    }

    @Test
    public void voidMethodsAreNotCallable() {
        assertFault(call("parent.noResult"), 0, "No such handler: parent.noResult");
    }

    @Test
    public void reportsAnUnknownMethod() {
        assertFault(call("parent.missing"), 0, "No such handler: parent.missing");
    }

    @Test
    public void reportsArgumentsThatFitNoOverload() {
        assertFault(call("parent.echo", "<int>5</int>"), 0, "No method matching arguments: parent.echo");
        assertFault(call("parent.echo"), 0, "No method matching arguments: parent.echo");
    }

    @Test
    public void sendsAHandlersFaultAsIs() {
        assertFault(call("parent.refuse"), BaseAPIHandler.UPLOAD_DENIED_EXCEPTION, "Upload denied");
    }

    @Test
    public void keepsUnexpectedFailuresOnTheServer() {
        String response = call("parent.breaks");
        assertFault(response, BaseAPIHandler.UNKNOWN_EXCEPTION, BaseAPIHandler.UNKNOWN_EXCEPTION_MSG);
        assertFalse(response.contains("internal detail"), response);
    }

    private String send(String document) {
        return new String(server.execute(document.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    @Test
    public void refusesADocumentTypeDeclarationWithoutReadingWhatItNames() throws IOException {
        Path secret = Files.createTempFile("xmlrpc-entity", ".txt");
        try {
            Files.writeString(secret, "parent.echo");
            // If the entity were resolved, the method name would be parent.echo and the call would succeed
            String response = send("<?xml version=\"1.0\"?><!DOCTYPE methodCall [<!ENTITY e SYSTEM \""
                    + secret.toUri() + "\">]><methodCall><methodName>&e;</methodName><params><param>"
                    + "<value><string>hello</string></value></param></params></methodCall>");
            assertFault(response, 0, "Failed to parse XML-RPC request");
            assertFalse(response.contains("hello"), response);

            assertFault(send("<?xml version=\"1.0\"?><!DOCTYPE methodCall><methodCall>"
                    + "<methodName>parent.echo</methodName></methodCall>"), 0, "Failed to parse XML-RPC request");
        } finally {
            Files.delete(secret);
        }
    }

    @Test
    public void answersAFaultWhenJdomRefusesADocumentTheParserAccepts() {
        // XML 1.1 allows a reference to a control character; JDOM throws an unchecked exception for it
        String response = send("<?xml version=\"1.1\"?><methodCall><methodName>parent.echo</methodName>"
                + "<params><param><value>ctl&#1;</value></param></params></methodCall>");
        assertFault(response, 0, "Failed to parse XML-RPC request");
        assertFalse(response.contains("JDOM"), response);

        // and XML 1.1 names may start with a supplementary-plane character, which JDOM refuses too
        assertFault(send("<?xml version=\"1.1\"?><methodCall><methodName>parent.echo</methodName>"
                + "<params><param><value><\uD800\uDC00>1</\uD800\uDC00></value></param></params></methodCall>"),
                0, "Failed to parse XML-RPC request");

        assertFault(send("<?xml version=\"1.1\"?><methodCall a=\"&#2;\"><methodName>parent.echo</methodName>"
                + "</methodCall>"), 0, "Failed to parse XML-RPC request");
    }

    @Test
    public void treatsANullResultAsAnError() {
        assertFault(call("parent.nothing"), BaseAPIHandler.UNKNOWN_EXCEPTION, BaseAPIHandler.UNKNOWN_EXCEPTION_MSG);
    }

    @Test
    public void exposesTheBloggerAndMetaWeblogMethods() {
        XmlRpcServer roller = new XmlRpcServer(RollerXmlRpcServlet.HANDLERS);
        // Unknown names are refused before any handler code runs, so a fault
        // other than "No such handler" shows that the name is registered.
        String[] names = {
            "blogger.deletePost", "blogger.setTemplate", "blogger.getTemplate", "blogger.getUserInfo",
            "blogger.getUsersBlogs", "blogger.editPost", "blogger.newPost", "blogger.getRecentPosts",
            "metaWeblog.getCategories", "metaWeblog.editPost", "metaWeblog.newPost", "metaWeblog.getPost",
            "metaWeblog.newMediaObject", "metaWeblog.getRecentPosts", "metaWeblog.getUsersBlogs",
        };
        for (String name : names) {
            String response = new String(roller.execute((
                    "<methodCall><methodName>" + name + "</methodName><params>"
                    + "<param><value><array><data/></array></value></param>"
                    + "</params></methodCall>").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            assertTrue(response.contains("No method matching arguments: " + name), name + ": " + response);
        }
    }
}
