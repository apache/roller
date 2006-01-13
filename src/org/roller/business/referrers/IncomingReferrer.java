/*
 * IncomingReferrer.java
 *
 * Created on December 20, 2005, 3:39 PM
 */

package org.roller.business.referrers;

/**
 * Represents an incoming (unprocessed) referrer.
 *
 * @author Allen Gilliland
 */
public class IncomingReferrer {
    
    private String referrerUrl = null;
    private String requestUrl = null;
    private String weblogHandle = null;
    private String weblogAnchor = null;
    private String weblogDateString = null;
    
    
    public IncomingReferrer() {}

    public String getReferrerUrl() {
        return referrerUrl;
    }

    public void setReferrerUrl(String referrerUrl) {
        this.referrerUrl = referrerUrl;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getWeblogHandle() {
        return weblogHandle;
    }

    public void setWeblogHandle(String weblogHandle) {
        this.weblogHandle = weblogHandle;
    }

    public String getWeblogAnchor() {
        return weblogAnchor;
    }

    public void setWeblogAnchor(String weblogAnchor) {
        this.weblogAnchor = weblogAnchor;
    }

    public String getWeblogDateString() {
        return weblogDateString;
    }

    public void setWeblogDateString(String weblogDateString) {
        this.weblogDateString = weblogDateString;
    }
    
}
