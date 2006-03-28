package org.roller.presentation.atomadminapi.sdk;

public class MissingElementException extends Exception {
    private String parent;
    private String child;
    
    public MissingElementException(String msg, String parent, String child) {
        this.parent = parent;
        this.child = child;
    }
    
    public String getMessage() {
        return super.getMessage() + ": expected element " + child + " as a child of element " + parent;
    }    
}
