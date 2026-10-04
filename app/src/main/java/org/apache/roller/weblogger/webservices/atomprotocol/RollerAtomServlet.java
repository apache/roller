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

package org.apache.roller.weblogger.webservices.atomprotocol;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.rometools.propono.atom.server.AtomHandler;
import com.rometools.propono.atom.server.AtomServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.util.SafeSAXBuilder;
import org.jdom2.JDOMException;

/**
 * Roller's AtomPub endpoint. It answers only while
 * <code>webservices.enableAtomPub</code> is on, and it reads each Atom entry
 * body with Roller's shared XML parser settings before the Propono servlet
 * handles the request.
 */
public class RollerAtomServlet extends AtomServlet {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(RollerAtomServlet.class);

    /** Largest Atom entry body accepted, in bytes. Media uploads are not affected. */
    static final int MAX_ENTRY_BYTES = 10 * 1024 * 1024;

    private static final String ATOM_CONTENT_TYPE = "application/atom+xml";

    /**
     * Request attribute that carries the handler authenticated by this servlet
     * to {@link RollerAtomHandlerFactory}, so Propono does not authenticate the
     * request a second time.
     */
    static final String HANDLER_ATTRIBUTE = RollerAtomServlet.class.getName() + ".handler";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (!WebloggerRuntimeConfig.getBooleanProperty("webservices.enableAtomPub")) {
            LOG.debug("AtomPub service is disabled; rejecting request");
            sendText(res, HttpServletResponse.SC_NOT_FOUND, "AtomPub service is disabled");
            return;
        }

        if (!carriesEntry(req)) {
            forward(req, res);
            return;
        }

        // Authenticate before reading the body, as Propono does.
        AtomHandler handler = createHandler(req, res);
        if (handler.getAuthenticatedUsername() == null) {
            res.setHeader("WWW-Authenticate", "BASIC realm=\"AtomPub\"");
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        req.setAttribute(HANDLER_ATTRIBUTE, handler);

        byte[] body = readBody(req.getInputStream());
        if (body == null) {
            sendText(res, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "Entry is too large");
            return;
        }
        try {
            // Propono reads the entry as UTF-8 text, so check the same text.
            SafeSAXBuilder saxBuilder = new SafeSAXBuilder();
            saxBuilder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            saxBuilder.setFeature("http://xml.org/sax/features/external-general-entities", false);
            saxBuilder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            saxBuilder.build(new InputStreamReader(
                    new ByteArrayInputStream(body), StandardCharsets.UTF_8));
        } catch (JDOMException e) {
            LOG.debug("Rejecting Atom entry that could not be parsed", e);
            sendText(res, HttpServletResponse.SC_BAD_REQUEST, "Invalid Atom entry");
            return;
        }
        forward(new BufferedBodyRequest(req, body), res);
    }

    /** Creates the handler that authenticates the request. */
    protected AtomHandler createHandler(HttpServletRequest req, HttpServletResponse res) {
        return new RollerAtomHandler(req, res);
    }

    /** Hands the request to the Propono servlet. */
    protected void forward(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        super.service(req, res);
    }

    /**
     * True when Propono would parse the request body as an Atom entry: a POST
     * of Atom content, or a PUT to an entry URI.
     */
    static boolean carriesEntry(HttpServletRequest req) {
        String method = req.getMethod();
        if ("POST".equalsIgnoreCase(method)) {
            String contentType = req.getContentType();
            return contentType != null && contentType.startsWith(ATOM_CONTENT_TYPE);
        }
        if ("PUT".equalsIgnoreCase(method)) {
            return RollerAtomHandler.isEntryPath(req.getPathInfo());
        }
        return false;
    }

    /** Reads the whole body, or returns null when it exceeds the limit. */
    private static byte[] readBody(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            total += read;
            if (total > MAX_ENTRY_BYTES) {
                return null;
            }
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private static void sendText(HttpServletResponse res, int status, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType("text/plain;charset=UTF-8");
        res.getWriter().write(message);
    }

    /** A request whose body has already been read into memory. */
    static final class BufferedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        BufferedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            final ByteArrayInputStream in = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return in.read();
                }

                @Override
                public int read(byte[] b, int off, int len) {
                    return in.read(b, off, len);
                }

                @Override
                public boolean isFinished() {
                    return in.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(body), StandardCharsets.UTF_8));
        }

        @Override
        public int getContentLength() {
            return body.length;
        }

        @Override
        public long getContentLengthLong() {
            return body.length;
        }
    }
}
