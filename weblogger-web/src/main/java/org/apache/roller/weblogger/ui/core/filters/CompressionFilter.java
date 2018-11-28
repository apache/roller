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

package org.apache.roller.weblogger.ui.core.filters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;


/** 
 * Filter that compresses output with gzip (assuming that browser supports gzip).
 * <P>
 * Taken from More Servlets and JavaServer Pages from Prentice Hall and 
 * Sun Microsystems Press, http://www.moreservlets.com/.
 * &copy; 2002 Marty Hall; may be freely used or adapted.
 */
public class CompressionFilter implements Filter {
    
    private static Log log = LogFactory.getLog(CompressionFilter.class);
    
    private boolean enabled = true;
    
    
    /** 
     * If browser does not support gzip, invoke resource normally. If browser 
     * does support gzip, set the Content-Encoding response header and invoke 
     * resource with a wrapped response that collects all the output. Extract 
     * the output and write it into a gzipped byte array. Finally, write that 
     * array to the client's output stream.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        if (!this.enabled || !isGzipSupported(req)) {
            // Invoke resource normally.
            chain.doFilter(req, res);
        } else {
            // Tell browser we are sending it gzipped data.
            res.setHeader("Content-Encoding", "gzip");
            
            // Invoke resource, accumulating output in the wrapper.
            ByteArrayResponseWrapper responseWrapper =
                    new ByteArrayResponseWrapper(response);
            
            chain.doFilter(req, responseWrapper);
            
            ByteArrayOutputStream outputStream = responseWrapper.getByteArrayOutputStream();
            
            // Get character array representing output.
            log.debug("Pre-zip size:" + outputStream.size());
            
            // Make a writer that compresses data and puts
            // it into a byte array.
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            GZIPOutputStream zipOut = new GZIPOutputStream(byteStream);
            
            // Compress original output and put it into byte array.
            zipOut.write(responseWrapper.getByteArrayOutputStream().toByteArray());
            
            // Gzip streams must be explicitly closed.
            zipOut.close();
            
            log.debug("Gzip size:" + byteStream.size());
            
            // Update the Content-Length header.
            res.setContentLength(byteStream.size());
            
            ByteArrayOutputStreamWrapper newOut =
                    (ByteArrayOutputStreamWrapper) responseWrapper.getOutputStream();
            newOut.clear();
            newOut.setFinallized();
            
            /* now force close of OutputStream */
            newOut.write(byteStream.toByteArray());
            newOut.close();
        }
        
    }
    

    public void init(FilterConfig config) throws ServletException {
        
        // is compression enabled?
        if(WebloggerConfig.getBooleanProperty("compression.gzipResponse.enabled")) {
            this.enabled = true;
            log.info("Compressed Output ENABLED");
        } else {
            this.enabled = false;
            log.info("Compressed Output DISABLED");
        }
    }
    
    
    public void destroy() {}
    
    
    private boolean isGzipSupported(HttpServletRequest req) {
        String browserEncodings = req.getHeader("Accept-Encoding");
        return ((browserEncodings != null)
                    && (browserEncodings.indexOf("gzip") != -1));
    }
    
    
    /**
     * Implementation of HttpServletResponseWrapper that supports caching.
     */
    private class ByteArrayResponseWrapper extends HttpServletResponseWrapper {
        
        private PrintWriter tpWriter;
        private ByteArrayOutputStreamWrapper tpStream;
        
        
        public ByteArrayResponseWrapper(ServletResponse inResp) throws IOException {
            super((HttpServletResponse) inResp);
            tpStream = new ByteArrayOutputStreamWrapper(inResp.getOutputStream());
            tpWriter = new PrintWriter(new OutputStreamWriter(tpStream,"UTF-8"));
        }
        
        
        public ServletOutputStream getOutputStream() throws IOException {
            return tpStream;
        }
        
        public PrintWriter getWriter() throws IOException {
            return tpWriter;
        }
        
        /**
         * Get a String representation of the entire buffer.
         */
        public String toString() {
            return tpStream.getByteArrayStream().toString();
        }
        
        public ByteArrayOutputStream getByteArrayOutputStream() throws IOException {
            return tpStream.getByteArrayStream();
        }
        
    }
    
    
    /**
     * Implementation of ServletOutputStream that allows the filter to hold the
     * Response content for insertion into the cache.
     */
    private class ByteArrayOutputStreamWrapper extends ServletOutputStream {
        
        protected OutputStream intStream;
        protected ByteArrayOutputStream baStream;
        protected boolean finallized = false;
        protected boolean flushOnFinalizeOnly = true;
        
        
        public ByteArrayOutputStreamWrapper(OutputStream outStream) {
            intStream = outStream;
            baStream = new ByteArrayOutputStream();
        }
        
        public ByteArrayOutputStreamWrapper() {
            intStream = System.out;
            baStream = new ByteArrayOutputStream();
        }
        
        
        public ByteArrayOutputStream getByteArrayStream() {
            return baStream;
        }
        
        public void setFinallized() {
            finallized = true;
        }
        
        public boolean isFinallized() {
            return finallized;
        }
        
        
        public void write(int i) throws java.io.IOException {
            baStream.write(i);
        }
        
        public void close() throws java.io.IOException {
            if (finallized) {
                processStream();
                intStream.close();
            }
        }
        
        public void flush() throws java.io.IOException {
            if (baStream.size() != 0) {
                if (!flushOnFinalizeOnly || finallized) {
                    processStream();
                    baStream = new ByteArrayOutputStream();
                }
            }
        }
        
        protected void processStream() throws java.io.IOException {
            intStream.write(baStream.toByteArray());
            intStream.flush();
        }
        
        public void clear() {
            baStream = new ByteArrayOutputStream();
        }
        
    }

}
