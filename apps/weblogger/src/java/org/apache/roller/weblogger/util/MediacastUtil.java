/*
 * MediacastUtil.java
 *
 * Created on May 11, 2007, 3:09:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.weblogger.util;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.mail.internet.ContentType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogManager;


/**
 * Utility for deailing with mediacast files.
 */
public final class MediacastUtil {
    
    private static final Log log = LogFactory.getLog(MediacastUtil.class);
    
    public static final int BAD_URL = 1;
    public static final int CHECK_FAILED = 2;
    public static final int BAD_RESPONSE = 3;
    public static final int INCOMPLETE = 4;
    
    
    // non-instantiable
    private MediacastUtil() {}
    
    
    /**
     * Validate a Mediacast resource.
     */
    public static final MediacastResource lookupResource(String url) 
            throws MediacastException {
        
        if(url == null || url.trim().length() ==0) {
            return null;
        }
        
        MediacastResource resource = null;
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");
            int response = con.getResponseCode();
            String message = con.getResponseMessage();
            
            if(response != 200) {
                log.debug("Mediacast error "+response+":"+message+" from url "+url);
                throw new MediacastException(BAD_RESPONSE, "weblogEdit.mediaCastResponseError");
            } else {
                String contentType = con.getContentType();
                long length = con.getContentLength();
                
                if(contentType == null || length == -1) {
                    log.debug("Response valid, but contentType or length is invalid");
                    throw new MediacastException(INCOMPLETE, "weblogEdit.mediaCastLacksContentTypeOrLength");
                }
                
                resource = new MediacastResource(url, contentType, length);
                log.debug("Valid mediacast resource = "+resource.toString());
                
            }
        } catch (MalformedURLException mfue) {
            log.debug("Malformed MediaCast url: " + url);
            throw new MediacastException(BAD_URL, "weblogEdit.mediaCastUrlMalformed", mfue);
        } catch (Exception e) {
            log.error("ERROR while checking MediaCast URL: " + url + ": " + e.getMessage());
            throw new MediacastException(CHECK_FAILED, "weblogEdit.mediaCastFailedFetchingInfo", e);
        }
        
//        if (!valid) {
//            log.debug("Removing MediaCast attributes");
//            WeblogManager weblogManager = WebloggerFactory.getRoller().getWeblogManager();
//            try {
//                weblogManager.removeWeblogEntryAttribute("att_mediacast_url", entry);
//                weblogManager.removeWeblogEntryAttribute("att_mediacast_type", entry);
//                weblogManager.removeWeblogEntryAttribute("att_mediacast_length", entry);
//            } catch (Exception e) {
//                log.error("ERROR removing invalid MediaCast attributes");
//            }
//        }
        
        return resource;
    }
    
}
