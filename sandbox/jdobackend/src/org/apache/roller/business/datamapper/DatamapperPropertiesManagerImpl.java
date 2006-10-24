
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
package org.apache.roller.business.datamapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.PropertiesManager;
import org.apache.roller.pojos.RollerPropertyData;

/*
 * DatamapperPropertiesManagerImpl.java
 *
 * Created on May 29, 2006, 2:06 PM
 *
 */
public class DatamapperPropertiesManagerImpl implements PropertiesManager {
    
    private DatamapperPersistenceStrategy strategy;
    
    /** The logger instance for this class. */
    private static Log logger = LogFactory
            .getFactory().getInstance(DatamapperPropertiesManagerImpl.class);

    /** Creates a new instance of DatamapperPropertiesManagerImpl */
    public DatamapperPropertiesManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    public void release() {
    }

    public void saveProperty(RollerPropertyData property) 
            throws RollerException {
        strategy.store(property);
    }

    public void saveProperties(Map properties) 
            throws RollerException {
        Iterator it = properties.values().iterator();
        while (it.hasNext()) {
            strategy.store((RollerPropertyData)it.next());
        }
    }

    public RollerPropertyData getProperty(String name) 
            throws RollerException {
        return (RollerPropertyData)strategy.load(RollerPropertyData.class,
                name);
    }

    public Map getProperties() 
            throws RollerException {
        HashMap result = new HashMap();
        Collection properties = (Collection)strategy.newQuery
                (RollerPropertyData.class, null)
            .execute();
        for (Iterator it = properties.iterator(); it.hasNext();) {
            RollerPropertyData property = (RollerPropertyData)it.next();
            result.put(property.getName(), property);
        }
        return result;
    }
    
}
