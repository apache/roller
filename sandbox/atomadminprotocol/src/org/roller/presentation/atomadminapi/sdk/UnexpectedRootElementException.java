package org.roller.presentation.atomadminapi.sdk;

public class UnexpectedRootElementException extends Exception {
    private String expected;
    private String actual;
    
    public UnexpectedRootElementException(String msg, String expected, String actual) {
        this.expected = expected;
        this.actual = actual;
    }
    
    public String getMessage() {
        return super.getMessage() + ": expected root element:  " + expected + ", was: " + actual;
    }    
}
