
package org.apache.roller.weblogger.util.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;


/**
 * A utility class for storing cached content written to a java.io.Writer.
 */
public class CachedContent implements Serializable {
    
    private static Log log = LogFactory.getLog(CachedContent.class);
    
    // the byte array we use to maintain the cached content
    private byte[] content = new byte[0];
    
    // content-type of data in byte array
    private String contentType = null;
    
    // Use a byte array output stream to cached the output bytes
    private transient ByteArrayOutputStream outstream = null;
    
    // The PrintWriter that users will be writing to
    private transient PrintWriter cachedWriter = null;
    
    
    public CachedContent(int size) {
        
        // construct output stream
        if(size > 0) {
            this.outstream = new ByteArrayOutputStream(size);
        } else {
            this.outstream = new ByteArrayOutputStream(RollerConstants.EIGHT_KB_IN_BYTES);
        }
        
        // construct writer from output stream
        try {
            this.cachedWriter =
                    new PrintWriter(new OutputStreamWriter(this.outstream, "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            // shouldn't be possible, java always supports utf-8
            throw new RuntimeException("Encoding problem", e);
        }
    }
    
    public CachedContent(int size, String contentType) {
        this(size);
        this.contentType = contentType;
    }
    
    
    /**
     * Get the content cached in this object as a byte array.  If you convert
     * this back to a string yourself, be sure to re-encode in "UTF-8".
     *
     * NOTE: the content is only a representation of the data written to the
     *       enclosed Writer up until the last call to flush().
     */
    public byte[] getContent() {
        return this.content;
    }
    
    
    /**
     * Get the content cached in this object as a String.
     *
     * NOTE: the content is only a representation of the data written to the
     *       enclosed Writer up until the last call to flush().
     */
    public String getContentAsString() {
        try {
            return new String(this.content,"UTF-8");
        } catch (UnsupportedEncodingException uex) {
            // shouldn't ever happen - violates Java Spec.
            throw new RuntimeException(uex);
        }
    }
    
    
    public PrintWriter getCachedWriter() {
        return cachedWriter;
    }
    
    
    public String getContentType() {
        return contentType;
    }
    
    
    /**
     * Called to flush any output in the cached Writer to
     * the cached content for more permanent storage.
     *
     * @throws IllegalStateException if calling flush() after a close()
     */
    public void flush() {
        
        if(this.outstream == null) {
            throw new IllegalStateException("Cannot flush() after a close()!");
        }
        
        this.cachedWriter.flush();
        this.content = this.outstream.toByteArray();
        
        log.debug("FLUSHED "+this.content.length);
    }
    
    
    /**
     * Close this CachedContent from further writing.
     */
    public void close() throws IOException {
        
        if(this.cachedWriter != null) {
            this.cachedWriter.flush();
            this.cachedWriter.close();
            this.cachedWriter = null;
        }
        
        if(this.outstream != null) {
            this.content = this.outstream.toByteArray();
            this.outstream.close();
            this.outstream = null;
        }
        
        log.debug("CLOSED");
    }
    
}
