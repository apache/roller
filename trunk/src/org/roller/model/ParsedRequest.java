
package org.roller.model;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;

/**
 * Servlet API free interface that represents an incoming web request.
 * @author David M Johnson
 */
public interface ParsedRequest
{  
    /** Get date specified by request as YYYYMMDD, or null */
    public String getDateString();
    
    /** Get refering URL, or null. */
    public String getRefererURL();
    
    /** Get request URL. */
    public String getRequestURL();
    
    /** Get website specified by request, or null. */
    public WebsiteData getWebsite();
    
    /** Get weblog entry specified by request, or null. */
    public WeblogEntryData getWeblogEntry();
    
    /** Was date specified by request URL? */
    public boolean isDateSpecified();

    /** True if Linkback extraction is enabled */
    public boolean isEnableLinkback();
}