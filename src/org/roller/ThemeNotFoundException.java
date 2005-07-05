/*
 * ThemeNotFoundException.java
 *
 * Created on June 28, 2005, 12:48 PM
 */

package org.roller;


/**
 * Thrown when the ThemeManager has a problem finding a named theme.
 *
 * @author Allen Gilliland
 */
public class ThemeNotFoundException extends RollerException {
    
    public ThemeNotFoundException(String s,Throwable t) {
        super(s, t);
    }
    
    public ThemeNotFoundException(Throwable t) {
        super(t);
    }
    
    public ThemeNotFoundException(String s) {
        super(s);
    }
    
    public ThemeNotFoundException() {
        super();
    }
    
}
