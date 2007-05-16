/*
 * MediacastResource.java
 *
 * Created on May 14, 2007, 9:12 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.util;

/**
 * An external 'mediacast' resource, typically a podcast, video, etc.
 *
 * This class is mainly used by weblog entries to track external resources used
 * in postings via enclosures.
 */
public class MediacastResource {
    
    private String url = null;
    private String contentType = null;
    private long length = 0;
    
    
    public MediacastResource(String u, String c, long l) {
        this.setUrl(u);
        this.setContentType(c);
        this.setLength(l);
    }

    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
    
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        buf.append("url = ").append(getUrl()).append("\n");
        buf.append("contentType = ").append(getContentType()).append("\n");
        buf.append("length = ").append(getLength()).append("\n");
        
        return buf.toString();
    }
    
}
