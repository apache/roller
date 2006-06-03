/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package org.roller.presentation.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * CacheServletResponse is a serialized representation of a response
 *
 * @author  <a href="mailto:sergek@lokitech.com">Serge Knystautas</a>
 * @version $Revision: 1.2 $
 */
public class CacheHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private final Log log = LogFactory.getLog(this.getClass());

    /**
     * We cache the printWriter so we can maintain a single instance
     * of it no matter how many times it is requested.
     */
    private PrintWriter cachedWriter;
    private ResponseContent result = null;
    private SplitServletOutputStream cacheOut = null;
    private int status = SC_OK;

    /**
     * Constructor
     *
     * @param response The servlet response
     */
    public CacheHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
        result = new ResponseContent();
    }

    /**
     * Get a response content
     *
     * @return The content
     */
    public ResponseContent getContent() {
        //Create the byte array
        result.commit();

        //Return the result from this response
        return result;
    }

    /**
     * Set the content type
     *
     * @param value The content type
     */
    public void setContentType(String value) {
        super.setContentType(value);
        result.setContentType(value);
    }

    /**
     * Set the date of a header
     *
     * @param name The header name
     * @param value The date
     */
    public void setDateHeader(String name, long value) {
        if (log.isDebugEnabled()) {
            log.debug("dateheader: " + name + ": " + value);
        }

        if ("last-modified".equalsIgnoreCase(name)) {
            result.setLastModified(value);
        }

        super.setDateHeader(name, value);
    }

    /**
     * Set a header field
     *
     * @param name The header name
     * @param value The header value
     */
    public void setHeader(String name, String value) {
        if (log.isDebugEnabled()) {
            log.debug("header: " + name + ": " + value);
        }

        super.setHeader(name, value);
    }

    /**
     * Set the int value of the header
     *
     * @param name The header name
     * @param value The int value
     */
    public void setIntHeader(String name, int value) {
        if (log.isDebugEnabled()) {
            log.debug("intheader: " + name + ": " + value);
        }

        super.setIntHeader(name, value);
    }

    /**
     * We override this so we can catch the response status. Only
     * responses with a status of 200 (<code>SC_OK</code>) will
     * be cached.
     */
    public void setStatus(int status) {
        super.setStatus(status);
        this.status = status;
    }

    /**
     * We override this so we can catch the response status. Only
     * responses with a status of 200 (<code>SC_OK</code>) will
     * be cached.
     */
    public void sendError(int status, String string) throws IOException {
        super.sendError(status, string);
        this.status = status;
    }

    /**
     * We override this so we can catch the response status. Only
     * responses with a status of 200 (<code>SC_OK</code>) will
     * be cached.
     */
    public void sendError(int status) throws IOException {
        super.sendError(status);
        this.status = status;
    }

    /**
     * We override this so we can catch the response status. Only
     * responses with a status of 200 (<code>SC_OK</code>) will
     * be cached.
     */
    public void setStatus(int status, String string) {
        super.setStatus(status, string);
        this.status = status;
    }

    public void sendRedirect(String location) throws IOException {
        this.status = SC_MOVED_TEMPORARILY;
        super.sendRedirect(location);
    }

    /**
     * Retrieves the captured HttpResponse status.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set the locale
     *
     * @param value The locale
     */
    public void setLocale(Locale value) {
        super.setLocale(value);
        result.setLocale(value);
    }

    /**
     * Get an output stream
     *
     * @throws IOException
     */
    public ServletOutputStream getOutputStream() throws IOException {
        // Pass this faked servlet output stream that captures what is sent
        if (cacheOut == null) {
            cacheOut = new SplitServletOutputStream(
                result.getOutputStream(), super.getOutputStream());
        }

        return cacheOut;
    }

    /**
     * Get a print writer
     *
     * @throws IOException
     */
    public PrintWriter getWriter() throws IOException {
       if (cachedWriter == null) {
           cachedWriter = new SplitPrintWriter(
              new PrintWriter(new OutputStreamWriter(result.getOutputStream(),"UTF-8")), 
              super.getWriter());
       }
       return cachedWriter;
    }

    public void flushBuffer() throws IOException {
        super.flushBuffer();

        if (cacheOut != null) {
            cacheOut.flush();
        }

        if (cachedWriter != null) {
            cachedWriter.flush();
        }
    }
    
    public String getCharacterEncoding()
    {
        return "UTF-8";
    }
}
