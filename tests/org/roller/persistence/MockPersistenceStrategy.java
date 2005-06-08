package org.roller.persistence;
import org.roller.RollerException;
import org.roller.business.*;
import org.roller.pojos.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Created on Mar 4, 2004
 */

/**
 * @author lance.lavandowska
 */
public class MockPersistenceStrategy implements PersistenceStrategy
{
    private Map classMap = new HashMap();

    /**
     * 
     */
    public MockPersistenceStrategy()
    {
        classMap.put(org.roller.business.HitCountData.class, new HashMap());     
        classMap.put(org.roller.pojos.BookmarkData.class, new HashMap());
        classMap.put(org.roller.pojos.CommentData.class, new HashMap());
        classMap.put(org.roller.pojos.FolderAssoc.class, new HashMap());
        classMap.put(org.roller.pojos.FolderData.class, new HashMap());
        classMap.put(org.roller.pojos.PageData.class, new HashMap());
        classMap.put(org.roller.pojos.RefererData.class, new HashMap());
        classMap.put(org.roller.pojos.RoleData.class, new HashMap());
        classMap.put(org.roller.pojos.RollerConfig.class, new HashMap());
        classMap.put(org.roller.pojos.UserData.class, new HashMap());
        classMap.put(org.roller.pojos.WeblogCategoryData.class, new HashMap());
        classMap.put(org.roller.pojos.WeblogCategoryAssoc.class, new HashMap());
        classMap.put(org.roller.pojos.WeblogEntryData.class, new HashMap());
        classMap.put(org.roller.pojos.WebsiteData.class, new HashMap());  
    }

    /**
     * @return
     */
    private String randomStr()
    {
        // TODO Not-quite random:-)
        return (new java.util.Date().toString());
    }
    
    public Map getObjectStore(Class clazz) 
    {
        HashMap objMap = (HashMap)classMap.get(clazz);
        if (objMap == null) 
        {
            objMap = new HashMap();
            classMap.put(clazz, objMap);
        }
        return objMap;
    }

    /* 
     * @see org.roller.persistence.PersistenceStrategy#store(org.roller.persistence.PersistentObject)
     */
    public PersistentObject store(PersistentObject data) throws RollerException
    {
        if (data.getId() == null)
        {
            data.setId( randomStr() );
        }
        Map objMap = getObjectStore(data.getClass());
        objMap.put(data.getId(), data);
        return data;
    }

    /* 
     * @see org.roller.persistence.PersistenceStrategy#load(java.lang.String, java.lang.Class)
     */
    public PersistentObject load(String id, Class cls) throws RollerException
    {
        Map objMap = getObjectStore(cls);
        return (PersistentObject)objMap.get(id);
    }

    /* 
     * @see org.roller.persistence.PersistenceStrategy#remove(org.roller.persistence.PersistentObject)
     */
    public void remove(PersistentObject po) throws RollerException
    {
        remove(po.getId(), po.getClass());
    }

    /* 
     * @see org.roller.persistence.PersistenceStrategy#remove(java.lang.String, java.lang.Class)
     */
    public void remove(String id, Class cls) throws RollerException
    {
        Map objMap = getObjectStore(cls);
        objMap.remove(id);
    }

    /* 
     * @see org.roller.persistence.PersistenceStrategy#begin()
     */
    public void begin() throws RollerException
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.persistence.PersistenceStrategy#commit()
     */
    public void commit() throws RollerException
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.persistence.PersistenceStrategy#rollback()
     */
    public void rollback() throws RollerException
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.persistence.PersistenceStrategy#release()
     */
    public void release() throws RollerException
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.persistence.PersistenceStrategy#query(java.lang.String, java.lang.Object[], java.lang.Object[])
     */
    public List query(String query, Object[] args, Object[] types)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
