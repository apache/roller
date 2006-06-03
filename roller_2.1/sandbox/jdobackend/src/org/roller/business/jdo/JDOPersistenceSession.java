package org.roller.business.jdo;

import org.roller.model.PersistenceSession;
import org.roller.pojos.UserData;

/**
 * @author David M Johnson
 */
public class JDOPersistenceSession implements PersistenceSession {
    private Object   session = null;
    private UserData user    = null;

    public JDOPersistenceSession(UserData user, Object session) {
        this.user = user;
        this.session = session;
    }

    public Object getSessionObject() {
        return session;
    }

    public void setSessionObject(Object newSession) {
        this.session = session;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }
}

