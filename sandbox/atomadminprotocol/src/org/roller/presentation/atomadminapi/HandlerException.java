package org.roller.presentation.atomadminapi;

/**
 * Abstract base class for all handler exceptions.
 *
 * Subclasses of this class allow handler implementations to indicate to
 * callers a particular HTTP error type, while still providing
 * a textual description of the problem.
 * 
 * Callers may use the 
 * <code>getStatus()</code> method to discover the HTTP status
 * code that should be returned to the client.
 */
public abstract class HandlerException extends Exception { 
    public HandlerException(String msg) {
        super(msg);
    }    

    public HandlerException(String msg, Throwable t) {
        super(msg);
    }    
    
    public abstract int getStatus();
}
