/*
 * InvalidRequestException.java
 *
 * Created on December 12, 2005, 9:49 AM
 */

package org.roller.presentation;

/**
 * An InvalidRequestException is thrown by the ParsedRequest class or any of
 * its subclasses when the request being parsed is invalid in any way.
 *
 * @author Allen Gilliland
 */
public class InvalidRequestException extends Exception {
    
    public InvalidRequestException(String msg) {
        super(msg);
    }
    
    public InvalidRequestException(String msg, Exception e) {
        super(msg, e);
    }
    
}
