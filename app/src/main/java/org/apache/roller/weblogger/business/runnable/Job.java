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
 * Job.java
 *
 * Created on December 16, 2005, 6:14 PM
 */

package org.apache.roller.weblogger.business.runnable;

import java.util.Map;

/**
 * A job to be executed.
 *
 * @author Allen Gilliland
 */
public interface Job {
    
    /**
     * Execute the job.
     */
    void execute();
    
    
    /**
     * Pass in input to be used for the job.
     */
    void input(Map input);
    
    
    /**
     * Get any output from the job.
     */
    Map output();
    
}
