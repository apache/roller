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

package org.apache.roller.business.runnable;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.util.DateUtil;


/**
 * An abstract class representing a scheduled task in Roller.
 *
 * This class extends the java.util.TimerTask class and adds in some Roller
 * specifics.
 */
public abstract class RollerTask implements Runnable {
    
    private static Log log = LogFactory.getLog(RollerTask.class);
    
    
    /**
     * Initialization.  Run once before the task is started.
     */
    public void init() throws RollerException {
        // no-op by default
    }
    
    
    /**
     * Get the unique name for this task.
     *
     * @return The unique name for this task.
     */
    public abstract String getName();
    
    
    /**
     * Get the unique id representing a specific instance of a task.  This is
     * important for tasks being run in a clustered environment so that a 
     * lease can be associated with a single cluster member.
     *
     * @return The unique client identifier for this task instance.
     */
    public abstract String getClientId();
    
    
    /**
     * When should this task be started?  The task is given the current time
     * so that it may determine a start time relative to the current time, 
     * such as the end of the day or hour.
     *
     * It is acceptable to return the currentTime object passed in or any other
     * time after it.  If the return value is before currentTime then it will
     * be ignored and the task will be started at currentTime.
     *
     * @param currentTime The current time.
     * @return The Date when this task should be started.
     */
    public abstract Date getStartTime(Date currentTime);
    
    
    /**
     * How often should the task run, in seconds.
     *
     * example: 3600 means this task runs once every hour.
     *
     * @return The interval the task should be run at, in minutes.
     */
    public abstract int getInterval();
    
    
    /**
     * Get the time, in seconds, this task wants to be leased for.
     *
     * example: 300 means the task is allowed 5 minutes to run.
     *
     * @return The time this task should lease its lock for, in minutes.
     */
    public abstract int getLeaseTime();
    
    
    /**
     * Get the properties from RollerConfig which pertain to this task.
     *
     * This extracts all properties from the RollerConfig of the type
     * task.<taskname>.<prop>=value and returns them in a properties object
     * where each item is keyed by <prop>.
     */
    protected Properties getTaskProperties() {
        
        String prefix = "tasks."+this.getName()+".";
        
        Properties taskProps = new Properties();
        
        String key = null;
        Enumeration keys = RollerConfig.keys();
        while(keys.hasMoreElements()) {
            key = (String) keys.nextElement();
            
            if(key.startsWith(prefix)) {
                taskProps.setProperty(key.substring(prefix.length()), 
                        RollerConfig.getProperty(key));
            }
        }
        
        // special addition for clientId property that applies to all tasks
        taskProps.setProperty("clientId", RollerConfig.getProperty("tasks.clientId"));
        
        return taskProps;
    }
    
    
    /**
     * A convenience method for calculating an adjusted time given an initial
     * Date to work from and a "changeFactor" which describes how the time 
     * should be adjusted.
     *
     * Allowed change factors are ...
     *   'immediate' - no change
     *   'startOfHour' - top of the hour, beginning with next hour
     *   'startOfDay' - midnight, beginning on the next day
     */
    protected Date getAdjustedTime(Date startTime, String changeFactor) {
        
        if(startTime == null || changeFactor == null) {
            return startTime;
        }
        
        Date adjustedTime = startTime;
        
        if("startOfDay".equals(changeFactor)) {
            adjustedTime = DateUtil.getEndOfDay(startTime);
        } else if("startOfHour".equals(changeFactor)) {
            adjustedTime = DateUtil.getEndOfHour(startTime);
        }
        
        return adjustedTime;
    }
    
}
