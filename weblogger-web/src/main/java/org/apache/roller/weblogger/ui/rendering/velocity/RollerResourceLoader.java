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
package org.apache.roller.weblogger.ui.rendering.velocity;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogTemplate;


/**
 * This is a simple template file loader that loads templates
 * from the Roller instance instead of plain files.
 * 
 * RollerResourceLoader makes use of WebloggerFactory.
 * 
 * @author <a href="mailto:lance@brainopolis.com">Lance Lavandowska</a>
 * @version $Id: RollerResourceLoader.java,v 1.9 2005/01/15 03:32:49 snoopdave Exp $
 */
public class RollerResourceLoader extends ResourceLoader {
    
    private static Log mLogger = LogFactory.getLog(RollerResourceLoader.class);
    
    
    public void init(ExtendedProperties configuration) {
        if (mLogger.isDebugEnabled()) {
            mLogger.debug(configuration);
        }
    }
    
    
    public boolean isSourceModified(Resource resource) {
        return (resource.getLastModified() !=
                readLastModified(resource, "checking timestamp"));
    }
    
    
    public long getLastModified(Resource resource) {
        return readLastModified(resource, "getting timestamp");
    }
    
    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     * @param name name of template
     * @return InputStream containing template
     */
    public InputStream getResourceStream(String name)
            throws ResourceNotFoundException {
        
        if (name == null || name.length() == 0) {
            throw new ResourceNotFoundException("Need to specify a template name!");
        }
        
        try {
            WeblogTemplate page = 
                    WebloggerFactory.getWeblogger().getWeblogManager().getPage(name);
            
            if (page == null) {
                throw new ResourceNotFoundException(
                        "RollerResourceLoader: page \"" +
                        name + "\" not found");
            }
            return new ByteArrayInputStream( page.getContents().getBytes("UTF-8") );
        } catch (UnsupportedEncodingException uex) {
            // This should never actually happen.  We expect UTF-8 in all JRE installation.
            // This rethrows as a Runtime exception after logging.
            mLogger.error(uex);
            throw new RuntimeException(uex);
        } catch (WebloggerException re) {
            String msg = "RollerResourceLoader Error: " +
                    "database problem trying to load resource " + name;
            mLogger.error( msg, re );
            throw new ResourceNotFoundException(msg);
        }
    }
    
    
    /**
     * Fetches the last modification time of the resource
     *
     * @param resource Resource object we are finding timestamp of
     * @param i_operation string for logging, indicating caller's intention
     *
     * @return timestamp as long
     */
    private long readLastModified(Resource resource, String i_operation) {
        
        /*
         *  get the template name from the resource
         */
        String name = resource.getName();
        try {
            WeblogTemplate page = 
                    WebloggerFactory.getWeblogger().getWeblogManager().getPage(name);
            
            if (mLogger.isDebugEnabled()) {
                mLogger.debug(name + ": resource=" + resource.getLastModified() +
                        " vs. page=" + page.getLastModified().getTime());
            }
            return page.getLastModified().getTime();
        } catch (WebloggerException re) {
            mLogger.error( "Error " + i_operation, re );
        }
        return 0;
    }
    
}
