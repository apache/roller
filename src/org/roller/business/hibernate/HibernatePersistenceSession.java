package org.roller.business.hibernate;

import net.sf.hibernate.Session;

import org.roller.model.PersistenceSession;
import org.roller.pojos.UserData;

/**
 * @author David M Johnson
 */
public class HibernatePersistenceSession implements PersistenceSession 
{
    private Session session = null;
    private UserData user = null;
    public HibernatePersistenceSession(UserData user, Session session)
    {
        this.user = user;
        this.session = session;
    }
    public Object getSessionObject() 
    {
        return session;
    }
    public void setSessionObject(Object newSession) 
    {
        this.session = (Session)session;
    }
    public UserData getUser() 
    {
        return user;
    }
    public void setUser(UserData user) 
    { 
        this.user = user;
    }
}


