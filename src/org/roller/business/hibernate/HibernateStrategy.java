/*
 * Created on Mar 7, 2003
 */
package org.roller.business.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.Type;
import org.roller.RollerException;
import org.roller.RollerPermissionsException;
import org.roller.business.PersistenceStrategy;
import org.roller.model.PersistenceSession;
import org.roller.pojos.PersistentObject;
import org.roller.pojos.UserData;


///////////////////////////////////////////////////////////////////////////////
/**
 * Reusable Hibernate implementations of CRUD operations.
 * @author David M Johnson
 */
public class HibernateStrategy implements PersistenceStrategy
{
    static final long serialVersionUID = 2561090040518169098L;    
    private static SessionFactory mSessionFactory = null;
    private static final ThreadLocal mSessionTLS = new ThreadLocal();
    //private static final ThreadLocal mTransactionTLS = new ThreadLocal();

    private static Log mLogger =
        LogFactory.getFactory().getInstance(HibernateStrategy.class);

    //-------------------------------------------------------------------------
    /**
     * Construct using Hibernate Session Factory.
     */
    public HibernateStrategy(SessionFactory factory) throws RollerException
    {
        mSessionFactory = factory;
    }

    //-------------------------------------------------------------------------
    /** 
     * Start new Roller persistence session on current thread. 
     */
    public void begin(UserData user) throws RollerException
    {
        getPersistenceSession(user, true); // force create of new session
    }

    //-------------------------------------------------------------------------
    /** 
     * Start new Roller persistence session on current thread. 
     */
    public void setUser(UserData user) throws RollerException
    {
        PersistenceSession pses = getPersistenceSession(user, false);
        pses.setUser(user);
    }

    //-------------------------------------------------------------------------
    /** 
     * Start new Roller persistence session on current thread. 
     */
    public UserData getUser() throws RollerException
    {
        PersistenceSession pses = getPersistenceSession(null, false);
        return pses.getUser();
    }

    //-------------------------------------------------------------------------
    /** 
     * Get existing persistence session on current thread. 
     */
    public Session getSession() throws RollerException
    {
        return (Session)(getPersistenceSession(
           UserData.ANONYMOUS_USER, false).getSessionObject());
    }
    
    //-------------------------------------------------------------------------
    /** 
     * Get existing or open new persistence session for current thread 
     * @param createNew True if existing session on thread is an warn condition.
     */
    public PersistenceSession getPersistenceSession(UserData user, boolean createNew) 
        throws RollerException
    {
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
                Session hses = mSessionFactory.openSession();
                ses = new HibernatePersistenceSession(user, hses);
            }
            catch (Throwable e)
            {
                mLogger.error(Messages.getString(
                    "HibernateStrategy.exceptionOpeningSession"));
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
     * This is called on error to start a new Hibernate session.
     * Gavin: "make sure you never catch + handle an exception and
     * then keep using the session (ObjectNotFoundException included!)
     */
    private void newSession()  throws RollerException
    {
        PersistenceSession pses = getPersistenceSession(null, false);       
        UserData user = pses.getUser();
        release();
        getPersistenceSession(user, true);
    }
    
    //-------------------------------------------------------------------------
    /** 
     * Release database session, rolls back any uncommitted changes. 
     */
    public void release() throws RollerException
    {
        PersistenceSession pses = (PersistenceSession)mSessionTLS.get();
        if ( null == pses ) return; // there is no session to release
        mSessionTLS.set(null); // sets thread's session to null
        Session ses = (Session)pses.getSessionObject();
        if (ses != null)
        {
            try
            {
                if (ses.isOpen())
                {
                    ses.close();   
                }               
            }
            catch (Throwable he)
            {
            	    mLogger.error("ERROR cleaning up Hibernate session", he);
            }
        }
        ses = null;
    }

    //-------------------------------------------------------------------------
    /**
     * Remove object from persistence storage.
     * @param clazz Class of object to remove.
     * @param id Id of object to remove.
     * @throws RollerException Error deleting object.
     */
    public void remove( String id, Class clazz ) throws RollerException
    {
        if ( id == null )
        {
            throw new RollerException(Messages.getString(
                "HibernateStrategy.nullNotValidId"));
        }
        if ( clazz == null )
        {
            throw new RollerException(Messages.getString(
                "HibernateStrategy.nullNotValidClass"));
        }

        // Create persistent instance and delete it
        PersistentObject obj;
        try
        {
            obj = (PersistentObject)getSession().load(clazz,id);
            if (obj.canSave()) 
            {
                getSession().delete(obj);
                getSession().flush();
            }
            else
            {
                throw new RollerPermissionsException("DENIED: cannot remove");
            }
        }
        catch (HibernateException e)
        {
            String msg = Messages.formatString(
                "HibernateStrategy.exceptionRemoving",id,clazz.getName());
            mLogger.error(msg, e);

            newSession();
            throw new RollerException(e);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Remove object from persistence storage.
     */
    public void remove(PersistentObject po) throws RollerException
    {
        if (!po.canSave())
        {
            throw new RollerPermissionsException(
                "DENIED: cannot remove: "+po.toString());
        }
        try
        {
            if (po.getId() != null) // no need to delete transient object
            {
                getSession().delete(po);
                getSession().flush();
            }
        }
        catch (HibernateException e)
        {
            String msg = Messages.formatString(
                "HibernateStrategy.exceptionRemoving",po.getId());
            mLogger.error(msg, e);
            newSession();
            throw new RollerException(e);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Retrieve object, begins and ends its own transaction.
     * @param clazz Class of object to retrieve.
     * @param id Id of object to retrieve.
     * @return Object Object retrieved.
     * @throws RollerException Error retrieving object.
     */
    public PersistentObject load(String id, Class clazz)
        throws RollerException
    {
        if ( id == null )
        {
            throw new RollerException(Messages.getString(
                "HibernateStrategy.nullNotValidId"));
        }

        if ( clazz == null )
        {
            throw new RollerException(Messages.getString(
                "HibernateStrategy.nullNotValidClass"));
        }

        Object obj = null;
        Session ses = getSession();
        try
        {
            obj = (PersistentObject)ses.get( clazz, id );
        }
        catch (Throwable e)
        {
            if (mLogger.isDebugEnabled())
            {
                if (e instanceof ObjectNotFoundException)
                {
                    mLogger.debug("No " + clazz.getName() + " found for ID:" + id);
                }
                else
                {
                    String msg = Messages.formatString(
                     "HibernateStrategy.exceptionRetrieving", id, clazz.getName());
                    mLogger.debug(msg, e);
                }
            }
            newSession();
        }
        return (PersistentObject)obj;
    }

    //-------------------------------------------------------------------------
    /**
     * Store object using an existing transaction.
     */
    public PersistentObject store(PersistentObject obj)
        throws RollerException
    {
        if ( obj == null )
        {
            throw new RollerException(Messages.getString(
                "HibernateStrategy.nullPassedIn"));
        }
        if (!obj.canSave())
        {
            throw new RollerPermissionsException(
                "DENIED: cannot save: "+obj.toString());
        }
        Session ses = getSession();
        try
        {
            // TODO: better to use ses.saveOrUpdate() here, if possible
            if ( obj.getId() == null || obj.getId().trim().equals("") )
            {
                // Object has never been written to database, so save it.
                // This makes obj into a persistent instance.
                ses.save(obj);
            }

            if ( !ses.contains(obj) )
            {
                // Object has been written to database, but instance passed in
                // is not a persistent instance, so must be loaded into session.
                PersistentObject vo =
                    (PersistentObject)ses.load(obj.getClass(),obj.getId());
                vo.setData(obj);
                obj = vo;
            }
        }
        catch (HibernateException e)
        {
            String msg = Messages.formatString(
                "HibernateStrategy.exceptionStoring",obj.getId());
            mLogger.error(msg, e);
            newSession();
            throw new RollerException(msg,e);
        }
        return obj;
    }

    //-------------------------------------------------------------------------
    /** 
     * Execute Hibernate HSQL query 
     */
    public List query( String query, Object[] args, Object[] types)
        throws RollerException
    {
        return query(query, args, (Type[])types);
    }

    //-------------------------------------------------------------------------
    /** 
     * Execute Hibernate HSQL query 
     */
    public List query( String query, Object[] args, Type[] types )
        throws RollerException
    {
        if ( query == null )
          {
              throw new RollerException(Messages.getString(
                "HibernateStrategy.nullNotValidQuery"));
          }

          if ( args == null )
          {
              throw new RollerException(Messages.getString(
                "HibernateStrategy.nullNotValidArgArray"));
          }

          if ( types == null )
          {
              throw new RollerException(Messages.getString(
                "HibernateStrategy.nullNotValidArrayType"));
          }

          try
          {
              if (query.indexOf("$") > -1)
              {
                  query = query.replaceAll("\\$\\d+", "\\?");
              }
              Query q = getSession().createQuery(query);
              q.setParameters(args,types);
              return q.list();
          }
          catch (Throwable e)
          {
              String msg = Messages.getString("HibernateStrategy.duringQuery");
              mLogger.error(msg, e);
              newSession();
              throw new RollerException(msg,e);
          }
    }

    //-------------------------------------------------------------------------
    /** 
     * Execute Hibernate HSQL query 
     */
    public List query( String query )
        throws RollerException
    {
        try
        {
            if (query.indexOf("$") > -1)
            {
                query = query.replaceAll("\\$\\d+", "\\?");
            }
            Query q = getSession().createQuery(query);
            return q.list();
        }
        catch (Throwable e)
        {
            String msg = Messages.getString("HibernateStrategy.duringQuery");
            mLogger.error(msg, e);
            newSession();
            throw new RollerException(msg,e);
        }
    }

    //-------------------------------------------------------------------------
    /** 
     * Commits current transaction, if there is one, does not release session. 
     */
    public void commit() throws RollerException
    {
        try
        {
            if (mSessionTLS.get()!=null)
            {
                getSession().flush();

                // can't call commit when autocommit is true
                if (!getSession().connection().getAutoCommit())
                {
                    getSession().connection().commit();
                }
            }
        }
        catch (Throwable he) // HibernateExeption or SQLException
        {
            newSession();
            throw new RollerException(he);
        }
    }

    //-------------------------------------------------------------------------
    /** 
     * Rollback uncommitted changes, does not release session. 
     */
    public void rollback() throws RollerException
    {
        // Can't call rollback when autoCommit=true
        try
        {
            if (!getSession().connection().getAutoCommit())
            {
                getSession().connection().rollback();
            }
        }
        catch (Throwable he)
        {
            newSession();            
            if (mLogger.isDebugEnabled())
            {
                mLogger.debug("ERROR during rollback",he);
            }
        }
    }
}

