/*
 * Created on Mar 7, 2003
 */
package org.roller.business.hibernate;

import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.pojos.PersistentObject;

import java.sql.Connection;
import java.util.List;


///////////////////////////////////////////////////////////////////////////////
/**
 * Reusable Hibernate implementations of CRUD operations.
 * @author David M Johnson
 */
public class HibernateStrategy implements PersistenceStrategy
{
    private static SessionFactory mSessionFactory = null;

    private static final ThreadLocal mSessionTLS = new ThreadLocal();
    //private static final ThreadLocal mTransactionTLS = new ThreadLocal();

    private static Log mLogger =
        LogFactory.getFactory().getInstance(HibernateStrategy.class);


    //-------------------------------------------------------------------------

    public HibernateStrategy(SessionFactory factory) throws RollerException
    {
        mSessionFactory = factory;
    }

    //-------------------------------------------------------------------------
    /** Start new Roller persistence session on current thread. */
    public void begin() throws RollerException
    {
        getSession(true); // force create of new session
    }

    //-------------------------------------------------------------------------

    /** Get existing or open new persistence session on current thread. */
    public Session getSession() throws RollerException
    {
        return getSession(false);
    }
    
    /** 
     * Get existing or open new persistence session for current thread. 
     * @param createNew True if existing session on thread is an error condition.
     */
    public Session getSession(boolean begin) throws RollerException
    {
        Session ses = (Session)mSessionTLS.get();
        if (begin && ses != null)
        {
        	mLogger.debug("TLS not empty at beginnng of request");
        }
        else if (ses == null)
        {
            try
            {
                ses = mSessionFactory.openSession();
            }
            catch (Exception e)
            {
                mLogger.error(Messages.getString(
                    "HibernateStrategy.exceptionOpeningSession"));
                throw new RuntimeException();
            }
            mSessionTLS.set(ses);
        }
        return ses;
    }

    //-------------------------------------------------------------------------

    /** Get existing or begin new transaction for current thread */
//    public Transaction getTransaction()
//    {
//        Session ses = getSession();
//        Transaction tx = (Transaction)mTransactionTLS.get();
//        if ( tx == null )
//        {
//            try
//            {
//                tx = ses.beginTransaction();
//                if (mLogger.isDebugEnabled())
//                {
//                    mLogger.debug(Messages.getString(
//                        "HibernateStrategy.beginTransaction")+ses);
//                }
//            }
//            catch (Exception e)
//            {
//                mLogger.error(Messages.getString(
//                    "HibernateStrategy.exceptionBeginningTransaction"));
//                throw new RuntimeException();
//            }
//            mTransactionTLS.set(tx);
//        }
//        return tx;
//    }

    //-------------------------------------------------------------------------

    /** Release database session, rolls back any uncommitted changes. */
    public void release() throws RollerException
    {
        Session ses = (Session)mSessionTLS.get();
        if ( null == ses ) return; // there is no session to release
        mSessionTLS.set(null); // sets thread's session to null

//        Transaction tx = (Transaction)mTransactionTLS.get();
//        mTransactionTLS.set(null);
//
//        try
//        {
//            if (tx != null) tx.rollback();
//        }
//        catch (Throwable he)
//        {
//            if (mLogger.isDebugEnabled())
//            {
//                mLogger.debug(Messages.getString(
//                    "HibernateStrategy.exceptionRollback"),he);
//            }
//        }

        if (ses != null)
        {
            try
            {
                if (ses.isOpen())
                {
                    ses.close();   
                }
                
                // According to the Hibernate documentation, it is not necessary to
                // close the Hibernate session but you should at least disconnect it.
//                Connection conn = ses.disconnect();
//                if (conn != null && !conn.isClosed())
//                {
//                    conn = ses.disconnect();
//                    conn.close();                    
//                }
            }
            catch (Throwable he)
            {
            	    mLogger.error("ERROR cleaning up Hibernate session", he);
            }
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Removes object using an existing transaction.
     * @param clazz Class of object to remove.
     * @param id Id of object to remove.
     * @throws RollerException Error deleting object.
     */
    public void remove( String id, Class clazz )
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

        // Create persistent instance and delete it
        Object obj;
        try
        {
            obj = getSession().load(clazz,id);
            getSession().delete(obj);
        }
        catch (Exception e)
        {
            String msg = Messages.formatString(
                "HibernateStrategy.exceptionRemoving",id,clazz.getName());
            mLogger.error(msg, e);

            // Gavin: "make sure you never catch + handle an exception and
            // then keep using the session (ObjectNotFoundException included!)"
            release();
            throw new RollerException(e);
        }
    }

    /**
     * @see org.roller.business.persistence.PersistenceStrategy#removePersistentObject(org.roller.pojos.PersistentObject)
     */
    public void remove(PersistentObject po) throws RollerException
    {
        try
        {
            getSession().delete(po);
        }
        catch (Exception e)
        {
            String msg = Messages.formatString(
                "HibernateStrategy.exceptionRemoving",po.getId());
            mLogger.error(msg, e);

            // Gavin: "make sure you never catch + handle an exception and
            // then keep using the session (ObjectNotFoundException included!)"
            release();
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
            obj = (PersistentObject)ses.load( clazz, id );
        }
        catch (Exception e)
        {
            if (mLogger.isDebugEnabled())
            {
                String msg = Messages.formatString(
                 "HibernateStrategy.exceptionRetrieving", id, clazz.getName());
                mLogger.debug(msg, e);
            }

            // Gavin: "make sure you never catch + handle an exception and
            // then keep using the session (ObjectNotFoundException included!)"
            release();
        }
        return (PersistentObject)obj;
    }

    //-------------------------------------------------------------------------
    /**
     * Store object using an existing transaction.
     * @param obj
     * @throws RollerException
     */
    public PersistentObject store(PersistentObject obj)
        throws RollerException
    {
        if ( obj == null )
        {
            throw new RollerException(Messages.getString(
                "HibernateStrategy.nullPassedIn"));
        }

        Session ses = getSession();
        try
        {
            // Dave: I tried using ses.saveOrUpdate() here, but it did not work.

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
        catch (Exception e)
        {
            String msg = Messages.formatString(
                "HibernateStrategy.exceptionStoring",obj.getId());
            mLogger.error(msg, e);

            // Gavin: "make sure you never catch + handle an exception and
            // then keep using the session (ObjectNotFoundException included!)"
            release();
            throw new RollerException(msg,e);
        }
        return obj;
    }

    //-------------------------------------------------------------------------

    public List query( String query, Object[] args, Object[] types)
        throws RollerException
    {
        return query(query, args, (Type[])types);
    }

    //-------------------------------------------------------------------------

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
              return getSession().find(query,args,types);
          }
          catch (Exception e)
          {
              String msg = Messages.getString("HibernateStrategy.duringQuery");
              mLogger.error(msg, e);

              // Gavin: "make sure you never catch + handle an exception and
              // then keep using the session (ObjectNotFoundException included!)"
              release();
              throw new RollerException(msg,e);
          }
    }

    //-------------------------------------------------------------------------

    public List query( String query )
        throws RollerException
    {
        try
        {
            if (query.indexOf("$") > -1)
            {
                query = query.replaceAll("\\$\\d+", "\\?");
            }
            return getSession().find(query);
        }
        catch (Exception e)
        {
            String msg = Messages.getString("HibernateStrategy.duringQuery");
            mLogger.error(msg, e);

            // Gavin: "make sure you never catch + handle an exception and
            // then keep using the session (ObjectNotFoundException included!)"
            release();
            throw new RollerException(msg,e);
        }
    }

    //-------------------------------------------------------------------------

    /** Commits current transaction, if there is one. */
    public void commit() throws RollerException
    {
        try
        {
            if (mSessionTLS.get()!=null)
            {
                getSession().flush();

                // can't call commit when autocommit is true
                //getSession().connection().commit();
            }
            //if (mTransactionTLS.get()!=null) getTransaction().commit();
        }
        catch (Exception he) // HibernateExeption or SQLException
        {
            // Gavin: "make sure you never catch + handle an exception and
            // then keep using the session (ObjectNotFoundException included!)"
            release();
            throw new RollerException(he);
        }
    }

    //-------------------------------------------------------------------------

    /** Rollback uncommitted changes. */
    public void rollback() throws RollerException
    {
// Can't call rollback when autoCommit=true
//        try
//        {
//            getSession().connection().rollback();
//            //if (mTransactionTLS.get()!=null) getTransaction().rollback();
//        }
//        catch (Exception he)
//        {
//            if (mLogger.isDebugEnabled())
//            {
//                mLogger.debug("ERROR during rollback",he);
//            }
//        }
    }

}

