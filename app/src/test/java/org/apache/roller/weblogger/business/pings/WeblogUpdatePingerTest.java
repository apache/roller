/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business.pings;

import org.apache.roller.weblogger.business.pings.WeblogUpdatePinger.PingResult;
import org.apache.xmlrpc.XmlRpcException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link WeblogUpdatePinger}, covering how it builds the
 * {@code weblogUpdates.ping} request and reads the response.
 *
 * <p>The response reader extracts only the {@code flerror} and {@code message}
 * fields the protocol defines, reads every scalar as text, ignores response
 * content it does not use, and refuses document type declarations.
 */
public class WeblogUpdatePingerTest {

    /** Set true if {@link Probe} is ever reconstructed from bytes. */
    private static volatile boolean probeReconstructed;

    @BeforeEach
    public void reset() {
        probeReconstructed = false;
    }

    /** A serializable object whose reconstruction from bytes is observable. */
    static final class Probe implements Serializable {
        private static final long serialVersionUID = 1L;

        private void readObject(ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
            in.defaultReadObject();
            probeReconstructed = true;
        }
    }

    private static byte[] serialize(Serializable object) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(object);
        }
        return bytes.toByteArray();
    }

    @Test
    public void readsFlerrorAndMessageFromNormalResponse() throws Exception {
        String response = "<?xml version=\"1.0\"?>"
                + "<methodResponse><params><param><value><struct>"
                + "<member><name>flerror</name><value><boolean>0</boolean></value></member>"
                + "<member><name>message</name><value><string>Thanks for the ping.</string></value></member>"
                + "</struct></value></param></params></methodResponse>";

        PingResult result = WeblogUpdatePinger.parseResponse(response.getBytes(StandardCharsets.UTF_8));
        assertFalse(result.isError());
        assertEquals("Thanks for the ping.", result.getMessage());
    }

    @Test
    public void readsErrorFlagFromResponse() throws Exception {
        String response = "<?xml version=\"1.0\"?>"
                + "<methodResponse><params><param><value><struct>"
                + "<member><name>flerror</name><value><boolean>1</boolean></value></member>"
                + "<member><name>message</name><value><string>Unknown blog.</string></value></member>"
                + "</struct></value></param></params></methodResponse>";

        PingResult result = WeblogUpdatePinger.parseResponse(response.getBytes(StandardCharsets.UTF_8));
        assertTrue(result.isError());
        assertEquals("Unknown blog.", result.getMessage());
    }

    @Test
    public void faultResponseBecomesException() throws Exception {
        String response = "<?xml version=\"1.0\"?>"
                + "<methodResponse><fault><value><struct>"
                + "<member><name>faultCode</name><value><int>4</int></value></member>"
                + "<member><name>faultString</name><value><string>boom</string></value></member>"
                + "</struct></value></fault></methodResponse>";

        XmlRpcException thrown = assertThrows(XmlRpcException.class,
                () -> WeblogUpdatePinger.parseResponse(response.getBytes(StandardCharsets.UTF_8)));
        assertEquals(4, thrown.code);
        assertEquals("boom", thrown.getMessage());
    }

    @Test
    public void controlProbeIsReconstructedByAnObjectStream() throws Exception {
        // Control: reading the probe through an ObjectInputStream reconstructs it.
        byte[] serialized = serialize(new Probe());
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serialized))) {
            in.readObject();
        }
        assertTrue(probeReconstructed);
    }

    @Test
    public void base64ResponseMemberIsReadAsTextOnly() throws Exception {
        // A base64 member is read as text; the reader does not turn it into an object.
        String embedded = Base64.getEncoder().encodeToString(serialize(new Probe()));
        String response = "<?xml version=\"1.0\"?>"
                + "<methodResponse><fault><value><struct>"
                + "<member><name>faultCode</name><value><int>4</int></value></member>"
                + "<member><name>faultString</name><value><string>boom</string></value></member>"
                + "<member><name>payload</name><value><base64>" + embedded + "</base64></value></member>"
                + "</struct></value></fault></methodResponse>";

        assertThrows(XmlRpcException.class,
                () -> WeblogUpdatePinger.parseResponse(response.getBytes(StandardCharsets.UTF_8)));
        assertFalse(probeReconstructed, "a base64 response member must be read as text, not reconstructed");
    }

    @Test
    public void doctypeInResponseIsRefused() {
        // A response that declares a DTD is refused, and nothing it names is resolved.
        String response = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE methodResponse [<!ENTITY x \"y\">]>"
                + "<methodResponse><params><param><value><struct>"
                + "<member><name>flerror</name><value><boolean>0</boolean></value></member>"
                + "<member><name>message</name><value><string>&x;</string></value></member>"
                + "</struct></value></param></params></methodResponse>";

        PingResult result = parse(response);
        assertFalse(result.isError());
    }

    @Test
    public void buildsWellFormedEscapedRequest() {
        String request = WeblogUpdatePinger.buildRequest(Arrays.asList("A & B <blog>", "http://example.test/"));
        assertTrue(request.contains("<methodName>weblogUpdates.ping</methodName>"));
        assertTrue(request.contains("A &amp; B &lt;blog&gt;"), "parameters must be XML-escaped");
        assertTrue(request.contains("<string>http://example.test/</string>"));
        assertFalse(request.contains("<blog>"), "raw markup must not leak into the request");
    }

    private static PingResult parse(String response) {
        try {
            return WeblogUpdatePinger.parseResponse(response.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new AssertionError("parsing should not throw for this response", ex);
        }
    }
}
