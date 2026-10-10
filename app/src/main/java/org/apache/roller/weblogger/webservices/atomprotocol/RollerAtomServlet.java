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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;

/**
 * Dispatcher servlet for Roller's Atom Publishing Protocol (RFC 5023)
 * implementation. Replaces the ROME Propono {@code AtomServlet}: it
 * authenticates the request, routes by HTTP method and URI shape to
 * {@link RollerAtomHandler}, and serializes/parses Atom XML via {@link AtomWriter}
 * and {@link AtomReader}. No ROME or Propono types are involved.
 *
 * <p>It answers only while <code>webservices.enableAtomPub</code> is on. Atom
 * entry bodies are capped at <code>webservices.atomPubMaxEntrySize</code> bytes
 * and are read only after the request is authenticated.
 */
public class RollerAtomServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Log log =
            LogFactory.getFactory().getInstance(RollerAtomServlet.class);

    /** Default maximum Atom entry body size, in bytes. Media uploads are not affected. */
    static final int DEFAULT_MAX_ENTRY_BYTES = 1024 * 1024;

    static final String MAX_ENTRY_SIZE_PROPERTY = "webservices.atomPubMaxEntrySize";

    private static final String ATOM_CONTENT_TYPE = "application/atom+xml";

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        if (!WebloggerRuntimeConfig.getBooleanProperty("webservices.enableAtomPub")) {
            log.debug("AtomPub service is disabled; rejecting request");
            sendText(response, HttpServletResponse.SC_NOT_FOUND, "AtomPub service is disabled");
            return;
        }

        String method = request.getMethod();
        if (!"GET".equals(method) && !"POST".equals(method)
                && !"PUT".equals(method) && !"DELETE".equals(method)) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        // Authenticate before reading the body.
        RollerAtomHandler handler = createHandler(request, response);
        String userName = handler.getAuthenticatedUsername();
        if (userName == null) {
            // The OAuth path may have already written a challenge/error response.
            if (!response.isCommitted()) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Roller\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
            return;
        }

        byte[] body = null;
        AtomEntry entry = null;
        if (carriesEntry(request)) {
            int maxEntryBytes = maxEntryBytes();
            // Read one byte past the limit, so an oversized body can be detected.
            body = request.getInputStream().readNBytes(maxEntryBytes + 1);
            if (body.length > maxEntryBytes) {
                sendText(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                        "Entry is too large");
                return;
            }
            try {
                entry = new AtomReader().parseEntry(new ByteArrayInputStream(body));
            } catch (AtomException e) {
                log.debug("Rejecting Atom entry that could not be parsed", e);
                sendText(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid Atom entry");
                return;
            }
        } else if ("POST".equals(method) || "PUT".equals(method)) {
            body = readBody(request);
        }
        AtomRequest areq = new AtomRequest(request, body);

        try {
            switch (method) {
                case "GET":
                    doGet(handler, areq, response);
                    break;
                case "POST":
                    doPost(handler, areq, entry, response);
                    break;
                case "PUT":
                    doPut(handler, areq, entry, response);
                    break;
                default:
                    handler.deleteEntry(areq);
                    response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (AtomException ae) {
            log.debug("Returning error to client: " + ae.getMessage(), ae);
            if (!response.isCommitted()) {
                response.sendError(ae.getStatus(), ae.getMessage());
            }
        } catch (Exception e) {
            log.error("Unexpected error handling AtomPub request", e);
            if (!response.isCommitted()) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }
    }

    /** Creates the handler that authenticates the request. */
    protected RollerAtomHandler createHandler(HttpServletRequest req, HttpServletResponse res) {
        return new RollerAtomHandler(req, res);
    }

    /**
     * True when the request body is an Atom entry: a POST of Atom content, or
     * a PUT to an entry URI.
     */
    static boolean carriesEntry(HttpServletRequest req) {
        String method = req.getMethod();
        if ("POST".equals(method)) {
            String contentType = req.getContentType();
            return contentType != null && contentType.startsWith(ATOM_CONTENT_TYPE);
        }
        if ("PUT".equals(method)) {
            return RollerAtomHandler.isEntryPath(req.getPathInfo());
        }
        return false;
    }

    /** Uses the default when an older installation has no setting or its value is invalid. */
    private static int maxEntryBytes() {
        String value = WebloggerRuntimeConfig.getProperty(MAX_ENTRY_SIZE_PROPERTY);
        if (value != null) {
            try {
                int limit = Integer.parseInt(value.trim());
                if (limit > 0 && limit < Integer.MAX_VALUE) {
                    return limit;
                }
            } catch (NumberFormatException e) {
                // Fall back to the default below.
            }
            log.warn("Invalid " + MAX_ENTRY_SIZE_PROPERTY + "; using the default entry limit");
        }
        return DEFAULT_MAX_ENTRY_BYTES;
    }

    private static void sendText(HttpServletResponse res, int status, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType("text/plain;charset=UTF-8");
        res.getWriter().write(message);
    }

    private void doGet(RollerAtomHandler handler, AtomRequest areq, HttpServletResponse response)
            throws AtomException, IOException {

        if (handler.isAtomServiceURI(areq)) {
            AtomServiceDoc service = handler.getAtomService(areq);
            response.setContentType(AtomConstants.SERVICE_MEDIA_TYPE);
            new AtomWriter().writeServiceDoc(response.getOutputStream(), service);

        } else if (handler.isCollectionURI(areq)) {
            AtomFeed feed = handler.getCollection(areq);
            response.setContentType(AtomConstants.FEED_MEDIA_TYPE);
            new AtomWriter().writeFeed(response.getOutputStream(), feed);

        } else if (handler.isEntryURI(areq)) {
            AtomEntry entry = handler.getEntry(areq);
            response.setContentType(AtomConstants.ENTRY_MEDIA_TYPE);
            new AtomWriter().writeEntry(response.getOutputStream(), entry);

        } else if (handler.isMediaEditURI(areq)) {
            AtomMediaResource resource = handler.getMediaResource(areq);
            if (resource.getContentType() != null) {
                response.setContentType(resource.getContentType());
            }
            response.setContentLengthLong(resource.getContentLength());
            if (resource.getLastModified() != null) {
                response.setDateHeader("Last-Modified", resource.getLastModified().getTime());
            }
            try (InputStream in = resource.getInputStream()) {
                in.transferTo(response.getOutputStream());
            }

        } else {
            throw new AtomNotFoundException("Cannot find specified resource");
        }
    }

    private void doPost(RollerAtomHandler handler, AtomRequest areq, AtomEntry entry,
            HttpServletResponse response) throws AtomException {

        if (!handler.isCollectionURI(areq)) {
            throw new AtomNotFoundException("Cannot POST to specified URI");
        }

        String contentType = areq.getContentType();
        AtomEntry created;
        if (entry != null) {
            created = handler.postEntry(areq, entry);
        } else {
            // Media POST: synthesize an entry carrying the request content type
            // and Slug; the binary data is read from the request body.
            AtomEntry mediaEntry = new AtomEntry();
            AtomContent content = new AtomContent();
            content.setType(contentType);
            mediaEntry.setContent(content);
            mediaEntry.setTitle(areq.getHeader("Slug"));
            created = handler.postMedia(areq, mediaEntry);
        }
        writeCreated(response, created);
    }

    private void doPut(RollerAtomHandler handler, AtomRequest areq, AtomEntry entry,
            HttpServletResponse response) throws AtomException {

        if (entry != null) {
            handler.putEntry(areq, entry);
            response.setStatus(HttpServletResponse.SC_OK);
        } else if (handler.isMediaEditURI(areq)) {
            handler.putMedia(areq);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            throw new AtomNotFoundException("Cannot PUT to specified URI");
        }
    }

    private void writeCreated(HttpServletResponse response, AtomEntry entry)
            throws AtomException {
        String editHref = entry.getLinkHref("edit");
        if (editHref != null) {
            response.setHeader("Location", editHref);
            response.setHeader("Content-Location", editHref);
        }
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType(AtomConstants.ENTRY_MEDIA_TYPE);
        try {
            OutputStream out = response.getOutputStream();
            new AtomWriter().writeEntry(out, entry);
        } catch (IOException ioe) {
            throw new AtomException("Error writing created entry", ioe);
        }
    }

    private byte[] readBody(HttpServletRequest request) throws IOException {
        try (InputStream in = request.getInputStream()) {
            return in.readAllBytes();
        }
    }
}
