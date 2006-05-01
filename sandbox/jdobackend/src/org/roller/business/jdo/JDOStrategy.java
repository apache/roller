/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
/*
 * Created on Dec 13, 2005
 */
package org.roller.business.jdo;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.model.PersistenceSession;
import org.roller.pojos.PersistentObject;
import org.roller.pojos.UserData;

///////////////////////////////////////////////////////////////////////////////
/**
 * @author Dave Johnson
 */
public class JDOStrategy implements PersistenceStrategy {
    private static final ThreadLocal mSessionTLS = new ThreadLocal();
    private PersistenceManagerFactory mPMF = null;
    private static Log mLogger = LogFactory
            .getFactory().getInstance(JDOStrategy.class);

    //-------------------------------------------------------------------------
    /**
     * Construct using JDO PersistenceManagerFactory.
     */
    public JDOStrategy(PersistenceManagerFactory pmf) throws RollerException {
        this.mPMF = pmf;
    }

    //-------------------------------------------------------------------------
    /**
     * Start new Roller persistence session on current thread.
     */
    public void begin(UserData user) throws RollerException {
        getPersistenceSession(user, true); // force create of new session
    }

    //-------------------------------------------------------------------------
    /**
     * Start new Roller persistence session on current thread.
     */
    public void setUser(UserData user) throws RollerException {
        PersistenceSession pses = getPersistenceSession(user, false);
        pses.setUser(user);
    }

    //-------------------------------------------------------------------------
    /**
     * Start new Roller persistence session on current thread.
     */
    public UserData getUser() throws RollerException {
        PersistenceSession pses = getPersistenceSession(null, false);
        return pses.getUser();
    }

    //-------------------------------------------------------------------------
    /**
     * Get existing persistence session on current thread.
     */
    public Object getSession() throws RollerException {
        return (getPersistenceSession(UserData.ANONYMOUS_USER, false)
                .getSessionObject());
    }

    //-------------------------------------------------------------------------
    /**
     * Get existing or open new persistence session for current thread
     * 
     * @param createNew
     *            True if existing session on thread is an warn condition.
     */
    public PersistenceSession getPersistenceSession(UserData user,
            boolean createNew) throws RollerException {
        PersistenceSession ses = (PersistenceSession)mSessionTLS.get();
        if (createNew && ses != null)
        {
            mLogger.warn("TLS not empty at beginnng of request");
            release();
            ses = null;
        }
        if (ses == null && user != null)
        {
            try
            {
                PersistenceManager pm = mPMF.getPersistenceManager();
                ses = new JDOPersistenceSession(user, pm);
            }
            catch (Throwable e)
            {
                mLogger.error(
                    "JDOStrategy.exceptionGetPersistenceManager");
                throw new RuntimeException();
            }
            mSessionTLS.set(ses);
        }
        else if (ses == null)
        {
            throw new RollerException(
                "MUST specify user for new persistence session");
        }
        return ses;
    }

    //-------------------------------------------------------------------------
    /**
     * This is called on error to start a new Hibernate session. Gavin: "make
     * sure you never catch + handle an exception and then keep using the
     * session (ObjectNotFoundException included!)
     */
    private void newSession() throws RollerException {
        PersistenceSession pses = getPersistenceSession(null, false);
        UserData user = pses.getUser();
        release();
        getPersistenceSession(user, true);
    }

    //-------------------------------------------------------------------------
    /**
     * Release database session, rolls back any uncommitted changes.
     */
    public void release() throws RollerException {
    }

    //-------------------------------------------------------------------------
    /**
     * Remove object from persistence storage.
     * 
     * @param clazz
     *            Class of object to remove.
     * @param id
     *            Id of object to remove.
     * @throws RollerException
     *             Error deleting object.
     */
    public void remove(String id, Class clazz) throws RollerException {
    }

    //-------------------------------------------------------------------------
    /**
     * Remove object from persistence storage.
     */
    public void remove(PersistentObject po) throws RollerException {
    }

    //-------------------------------------------------------------------------
    /**
     * Retrieve object, begins and ends its own transaction.
     * 
     * @param clazz
     *            Class of object to retrieve.
     * @param id
     *            Id of object to retrieve.
     * @return Object Object retrieved.
     * @throws RollerException
     *             Error retrieving object.
     */
    public PersistentObject load(String id, Class clazz) throws RollerException {
        return null;
    }

    //-------------------------------------------------------------------------
    /**
     * Store object using an existing transaction.
     */
    public PersistentObject store(PersistentObject obj) throws RollerException {
        return null;
    }

    //-------------------------------------------------------------------------
    /**
     * Execute query
     */
    public List query(String query, Object[] args, Object[] types)
            throws RollerException {
        return query(query, args, types);
    }

    //-------------------------------------------------------------------------
    /**
     * Execute Hibernate HSQL query
     */
    public List query(String query) throws RollerException {
        return null;
    }

    //-------------------------------------------------------------------------
    /**
     * Commits current transaction, if there is one, does not release session.
     */
    public void commit() throws RollerException {
    }

    //-------------------------------------------------------------------------
    /**
     * Rollback uncommitted changes, does not release session.
     */
    public void rollback() throws RollerException {
    }
}

