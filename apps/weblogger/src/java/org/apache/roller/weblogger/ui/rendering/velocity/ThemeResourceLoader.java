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
 * ThemeResourceLoader.java
 *
 * Created on June 28, 2005, 12:25 PM
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
import org.apache.roller.weblogger.business.themes.ThemeNotFoundException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeTemplate;


/**
 * The ThemeResourceLoader is a Velocity template loader which loads
 * templates from shared themes.
 *
 * @author Allen Gilliland
 */
public class ThemeResourceLoader extends ResourceLoader {
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(ThemeResourceLoader.class);
        
    
    public void init(ExtendedProperties configuration) {
        mLogger.debug(configuration);
    }
    
    
    public InputStream getResourceStream( String name )
        throws ResourceNotFoundException {
        
        mLogger.debug("Looking up resource named ... "+name);
        
        if (name == null || name.length() < 1) {
            throw new ResourceNotFoundException("Need to specify a template name!");
        }
        
        try {
            // parse the name ... theme templates name are <theme>:<template>
            String[] split = name.split(":", 2);
            if(split.length < 2)
                throw new ResourceNotFoundException("Invalid ThemeRL key "+name);
            
            // lookup the template from the proper theme
            ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
            Theme theme = themeMgr.getTheme(split[0]);
            ThemeTemplate template = theme.getTemplateByName(split[1]);
            
            if(template == null)
                throw new ResourceNotFoundException("Template ["+split[1]+
                        "] doesn't seem to be part of theme ["+split[0]+"]");
            
            mLogger.debug("Resource found!");
            
            // return the input stream
            return new ByteArrayInputStream(template.getContents().getBytes("UTF-8"));
            
        } catch (UnsupportedEncodingException uex) {
            // We expect UTF-8 in all JRE installation.
            // This rethrows as a Runtime exception after logging.
            mLogger.error(uex);
            throw new RuntimeException(uex);
           
        } catch (ThemeNotFoundException tnfe) {
            String msg = "ThemeResourceLoader Error: " + tnfe.getMessage();
            mLogger.error(msg, tnfe);
            throw new ResourceNotFoundException(msg);
            
        } catch (WebloggerException re) {
            String msg = "RollerResourceLoader Error: " + re.getMessage();
            mLogger.error( msg, re );
            throw new ResourceNotFoundException(msg);
        }
    }
    
    
    public boolean isSourceModified(Resource resource) {
        return (resource.getLastModified() != this.getLastModified(resource));
    }
    
    
    public long getLastModified(Resource resource) {
        long last_mod = 0;
        String name = resource.getName();
        
        mLogger.debug("Checking last modified time for resource named ... "+name);
        
        if (name == null || name.length() < 1)
            return last_mod;
        
        try {
            // parse the name ... theme templates name are <theme>:<template>
            String[] split = name.split(":", 2);
            if(split.length < 2)
                return last_mod;
            
            // lookup the template from the proper theme
            ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
            Theme theme = themeMgr.getTheme(split[0]);
            ThemeTemplate template = theme.getTemplateByName(split[1]);
            
            if(template == null)
                return last_mod;
            
            last_mod = template.getLastModified().getTime();
            
        } catch (ThemeNotFoundException tnfe) {
            // ignore
        } catch (WebloggerException re) {
            // we don't like to see this happen, but oh well
        }
        
        return last_mod;
    }
    
}
