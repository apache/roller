package org.roller.model;

import org.roller.pojos.UserData;

/**
 * Represents a persistence session; this is the object that a PersistenceStrategy
 * stores in thread local storage.
 * @author David M Johnson
 */
public interface PersistenceSession 
{
    /** Get underlying persistence session object (e.g. a Hibernate Session) */
    public Object getSessionObject();
    
    /** Set underlying persistence session object */
    public void setSessionObject(Object newSession);
    
    /** Get user associated with this session */
    public UserData getUser();
    
    /** Set user assoicated with this session */
    public void setUser(UserData user);
}
