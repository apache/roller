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

package org.apache.roller.ui.rendering.velocity;

import java.io.InputStream;
import javax.servlet.ServletContext;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.ui.core.RollerContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;


/**
 * Loads Velocity resources from the webapp.
 *
 * All resource urls begin from the root of the webapp.  If a resource path
 * is relative (does not begin with a /) then it is prefixed with the path
 * /WEB-INF/velocity/, which is where Roller keeps its velocity files.
 */
public class WebappResourceLoader extends ResourceLoader {
    
    private static Log log = LogFactory.getLog(WebappResourceLoader.class);
    
    private ServletContext mContext = null;
    
    
    /**
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init(org.apache.commons.collections.ExtendedProperties)
     */
    public void init(ExtendedProperties config) {
        
        log.debug("WebappResourceLoader : initialization starting.");
        
        if (mContext == null) {
            mContext = RollerContext.getServletContext();
            log.debug("Servlet Context = "+mContext.getRealPath("/WEB-INF/velocity/"));
        }
        
        log.debug(config);
        
        log.debug("WebappResourceLoader : initialization complete.");
    }
    
    
    /**
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getResourceStream(java.lang.String)
     */
    public InputStream getResourceStream(String name) 
            throws ResourceNotFoundException {
        
        log.debug("Looking up resource named ... "+name);
        
        if (name == null || name.length() == 0) {
            throw new ResourceNotFoundException("No template name provided");
        }
        
        InputStream result = null;
        
        try {
            if(!name.startsWith("/"))
                name = "/WEB-INF/velocity/" + name;
            
            result = this.mContext.getResourceAsStream(name);
            
        } catch(Exception e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
        
        if(result == null) {
            throw new ResourceNotFoundException("Couldn't find "+name);
        }
        
        return result;
    }
    
    
    /**
     * Files loaded by this resource loader are considered static, so they are
     * never reloaded by velocity.
     *
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
     */
    public boolean isSourceModified(Resource arg0) {
        return false;
    }
    
    
    /**
     * Defaults to return 0.
     *
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
     */
    public long getLastModified(Resource arg0) {
        return 0;
    }
    
}
