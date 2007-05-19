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

package org.apache.roller.business.hibernate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerImpl;

/**
 * A Hibernate specific implementation of the Roller business layer.
 */
@Singleton
public class HibernateRollerImpl extends RollerImpl { 
    
    static final long serialVersionUID = 5256135928578074652L;
    
    private static Log log = LogFactory.getLog(HibernateRollerImpl.class);
    
    // a persistence utility class
    private HibernatePersistenceStrategy strategy = null;
        
    
    @Inject
    public HibernateRollerImpl(HibernatePersistenceStrategy strategy) throws RollerException {
        this.strategy = strategy;
    }
    
    
    public void flush() throws RollerException {
        this.strategy.flush();
    }
    
    
    public void release() {        
        // release our own stuff first
        getBookmarkManager().release();
        getRefererManager().release();
        getUserManager().release();
        getWeblogManager().release();
        getPingTargetManager().release();
        getPingQueueManager().release();
        getAutopingManager().release();
        getThreadManager().release();
        
        // tell Hibernate to close down
        this.strategy.release();
    }
    
    
    public void shutdown() {        
        // do our own shutdown first
        this.release();
    }   
}
