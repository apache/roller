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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Dispatcher servlet for Roller's Atom Publishing Protocol (RFC 5023)
 * implementation. Replaces the ROME Propono {@code AtomServlet}: it
 * authenticates the request, routes by HTTP method and URI shape to
 * {@link RollerAtomHandler}, and serializes/parses Atom XML via {@link AtomWriter}
 * and {@link AtomReader}. No ROME or Propono types are involved.
 */
public class RollerAtomServlet extends HttpServlet {

    private static final Log log =
            LogFactory.getFactory().getInstance(RollerAtomServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        process(request, response, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        process(request, response, "POST");
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        process(request, response, "PUT");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        process(request, response, "DELETE");
    }

    private void process(HttpServletRequest request, HttpServletResponse response, String method)
            throws IOException {

        RollerAtomHandler handler = new RollerAtomHandler(request, response);
        String userName = handler.getAuthenticatedUsername();
        if (userName == null) {
            // The OAuth path may have already written a challenge/error response.
            if (!response.isCommitted()) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Roller\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication error");
            }
            return;
        }

        byte[] body = null;
        if ("POST".equals(method) || "PUT".equals(method)) {
            body = readBody(request);
        }
        AtomRequest areq = new AtomRequest(request, body);

        try {
            switch (method) {
                case "GET":
                    doGet(handler, areq, response);
                    break;
                case "POST":
                    doPost(handler, areq, response);
                    break;
                case "PUT":
                    doPut(handler, areq, response);
                    break;
                case "DELETE":
                    handler.deleteEntry(areq);
                    response.setStatus(HttpServletResponse.SC_OK);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
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

    private void doPost(RollerAtomHandler handler, AtomRequest areq, HttpServletResponse response)
            throws AtomException {

        if (!handler.isCollectionURI(areq)) {
            throw new AtomNotFoundException("Cannot POST to specified URI");
        }

        String contentType = areq.getContentType();
        AtomEntry created;
        if (contentType != null && contentType.startsWith("application/atom+xml")) {
            AtomEntry entry = new AtomReader().parseEntry(areq.getInputStream());
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

    private void doPut(RollerAtomHandler handler, AtomRequest areq, HttpServletResponse response)
            throws AtomException {

        if (handler.isEntryURI(areq)) {
            AtomEntry entry = new AtomReader().parseEntry(areq.getInputStream());
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
