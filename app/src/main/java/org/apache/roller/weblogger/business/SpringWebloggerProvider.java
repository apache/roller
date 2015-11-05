/*
   Copyright 2015 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.apache.roller.weblogger.business;

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * A Spring specific implementation of a WebloggerProvider.
 */
public class SpringWebloggerProvider {

    // Spring Application Context
    protected ApplicationContext context;

    // maintain our own singleton instance of Weblogger
    protected Weblogger webloggerInstance = null;

    /**
     * Instantiate a new SpringWebloggerProvider using context file
     * configured in WebloggerConfig via 'spring.context.file' property.
     */
    public SpringWebloggerProvider() {
        String contextFilename = WebloggerConfig.getProperty("spring.context.file");

        if(contextFilename == null) {
            throw new IllegalStateException("unable to lookup default spring module via property 'spring.context.file'");
        }

        try {
            context = new ClassPathXmlApplicationContext(contextFilename);
        } catch (BeansException e) {
            throw new RuntimeException("Error instantiating Spring context; exception message: " + e.getMessage(), e);
        } catch (ThreadDeath t) {
            throw t;
        } catch (Throwable e) {
            // Fatal misconfiguration, cannot recover
            throw new RuntimeException("Error instantiating backend module " + contextFilename + "; exception message: " + e.getMessage(), e);
        }
    }

    public void bootstrap() {
        try {
            if (context == null) {
                throw new RuntimeException("Spring context not initialized, check property file configuration.");
            }
            webloggerInstance = context.getBean("webloggerBean", Weblogger.class);
        } catch (BeansException e) {
            throw new RuntimeException("Error finding webloggerBean; exception message: " + e.getMessage(), e);
        }
    }

    public Weblogger getWeblogger() {
        return webloggerInstance;
    }

    public ApplicationContext getContext() {
        return context;
    }

}
