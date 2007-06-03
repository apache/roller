/*
 * MediacastException.java
 * 
 * Created on May 11, 2007, 3:53:28 PM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.weblogger.util;

import org.apache.roller.weblogger.WebloggerException;


/**
 * An exception thrown when dealing with Mediacast files.
 */
public class MediacastException extends WebloggerException {
    
    private int errorCode = 0;
    private String errorKey = null;
    
    
    public MediacastException(int code, String msgKey) {
        this.errorCode = code;
        this.errorKey = msgKey;
    }
    
    
    public MediacastException(int code, String msgKey, Throwable t) {
        super(t);
        this.errorCode = code;
        this.errorKey = msgKey;
    }
    
    
    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorKey() {
        return errorKey;
    }
    
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }
    
}
