/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package org.roller.presentation.pagecache.rollercache;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Locale;

import javax.servlet.ServletResponse;

/**
 * Holds the servlet response in a byte array so that it can be held
 * in the cache (and, since this class is serializable, optionally
 * persisted to disk).
 *
 * @version $Revision: 1.1 $
 * @author  <a href="mailto:sergek@lokitech.com">Serge Knystautas</a>
 */
public class ResponseContent implements Serializable {
    private transient ByteArrayOutputStream bout = new ByteArrayOutputStream(1000);
    private Locale locale = null;
    private String contentType = null;
    private byte[] content = null;

    /**
     * Set the content type. We capture this so that when we serve this
     * data from cache, we can set the correct content type on the response.
     */
    public void setContentType(String value) {
        contentType = value;
    }

    /**
     * Set the Locale. We capture this so that when we serve this data from
     * cache, we can set the correct locale on the response.
     */
    public void setLocale(Locale value) {
        locale = value;
    }

    /**
     * Get an output stream. This is used by the {@link SplitServletOutputStream}
     * to capture the original (uncached) response into a byte array.
     */
    public OutputStream getOutputStream() {
        return bout;
    }

    /**
     * Gets the size of this cached content.
     *
     * @return The size of the content, in bytes. If no content
     * exists, this method returns <code>-1</code>.
     */
    public int getSize() {
        return (content != null) ? content.length : (-1);
    }

    /**
     * Called once the response has been written in its entirety. This
     * method commits the response output stream by converting the output
     * stream into a byte array.
     */
    public void commit() {
        content = bout.toByteArray();
    }

    /**
     * Writes this cached data out to the supplied <code>ServletResponse</code>.
     *
     * @param response The servlet response to output the cached content to.
     * @throws IOException
     */
    public void writeTo(ServletResponse response) throws IOException {
        //Send the content type and data to this response
        if (contentType != null) {
            response.setContentType(contentType);
        }

        response.setContentLength(content.length);

        if (locale != null) {
            response.setLocale(locale);
        }

        OutputStream out = new BufferedOutputStream(response.getOutputStream());
        out.write(content);
        out.flush();
    }
}
