/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package org.apache.roller.business.datamapper;

import java.util.Collection;
import org.apache.roller.RollerException;

import org.apache.roller.pojos.PersistentObject;

/**
 *
 */
public interface DatamapperPersistenceStrategy {

    /**
     * Flush changes to the datastore, commit transaction, release pm.
     * @throws org.apache.roller.RollerException on any error
     */
    void flush() 
            throws RollerException;
    
    /**
     * Release database session, rolls back any uncommitted changes.
     */
    void release();

    /**
     * Store object in the database. Start a transaction if no
     * transaction in progress.
     * @param obj the object to persist
     * @return the object persisted
     * @throws org.apache.roller.RollerException on any error
     */
    PersistentObject store(PersistentObject obj) 
            throws RollerException;

    /**
     * Remove object from persistence storage. Start a transaction if no
     * transaction in progress.
     * @param clazz the class of object to remove
     * @param id the id of the object to remove
     * @throws RollerException on any error deleting object
     */
    public void remove(Class clazz, String id) 
            throws RollerException;

    /**
     * Remove object from persistence storage. Start a transaction if no
     * transaction in progress.
     * @param po the persistent object to remove
     * @throws org.apache.roller.RollerException on any error
     */
    public void remove(PersistentObject po) 
            throws RollerException;

    /**
     * Remove objects from persistence storage. Start a transaction if no
     * transaction in progress.
     * @param pos the persistent objects to remove
     * @throws org.apache.roller.RollerException on any error
     */
    public void removeAll(Collection pos) 
            throws RollerException;

    /**
     * Remove objects from persistence storage. Start a transaction if no
     * transaction in progress.
     * @param clazz the persistent from which to remove all objects 
     * @throws org.apache.roller.RollerException on any error
     */
    public void removeAll(Class clazz) 
            throws RollerException;

    /**
     * Retrieve object, no transaction needed.
     * @param clazz the class of object to retrieve
     * @param id the id of the object to retrieve
     * @return the object retrieved
     * @throws RollerException on any error retrieving object
     */
    public PersistentObject load(Class clazz, String id) 
            throws RollerException;

    /**
     * Create query.
     * @param clazz the class of instances to find
     * @param queryName the name of the query
     * @throws org.apache.roller.RollerException on any error
     */
    public DatamapperQuery newQuery(Class clazz, String queryName)
            throws RollerException;

}
