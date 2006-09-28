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
 * Created on Mar 10, 2004
 */
package org.apache.roller.ui.core.tasks;

import java.text.ParseException;
import java.util.Date;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.ScheduledTask;
import org.apache.roller.pojos.RollerPropertyData;
import org.apache.roller.util.DateUtil;

/**
 * Maintains tag summary information for faster response to tag UI in Roller weblogs.
 * 
 * @author Elias Torres (<a href="mailto:eliast@us.ibm.com">eliast@us.ibm.com</a>)
 *
 */
public class TagSummariesTask extends TimerTask implements ScheduledTask {
    
    private static Log log = LogFactory.getLog(TagSummariesTask.class);
    
    private static final String LAST_RUN = "tag.summary.lastRun";
    
    /**
     * Task init.
     */
    public void init(Roller roller, String realPath) throws RollerException {
        log.debug("initing");
    }
    
    
    /**
     * Excecute the task.
     */
    public void run() {
        
        log.info("task started");
        
        try {
            Roller roller = RollerFactory.getRoller();
            
            // find the last time we ran
            RollerPropertyData property = roller.getPropertiesManager().getProperty(LAST_RUN);
            
            Date lastRun = null;
            
            if (property == null)
            {
              // the first time we need to create a new property object
              property = new RollerPropertyData();
              property.setName(LAST_RUN);
            }
            else
            {
              // else, let's get last time we ran.
              try {
                lastRun = DateUtil.parse(property.getValue(), DateUtil.defaultTimestampFormat());
              } catch (ParseException e) {
                e.printStackTrace();
              }      
            }
            
            Date summarized = roller.getTagManager().summarize(lastRun);
            
            if(summarized != null) {
              property.setValue(DateUtil.defaultTimestamp(summarized));
              roller.getPropertiesManager().saveProperty(property);
              roller.flush();
            }
                        
            roller.release();
            log.info("task completed");   
            
        } catch (RollerException e) {
            log.error("Error while summarizing tag table.", e);
        } catch (Exception ee) {
            log.error("unexpected exception", ee);
        }
        
        log.info("task completed");
    }
    
    
    /**
     * Main method so that this task may be run from outside the webapp.
     */
    public static void main(String[] args) throws Exception {
        try {            
            TagSummariesTask task = new TagSummariesTask();
            task.init(null, null);
            task.run();
            System.exit(0);
        } catch (RollerException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
}
