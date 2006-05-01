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

package org.apache.roller.presentation.tags.calendar;
import java.util.Calendar;
import java.util.Date;

/** 
 * Model interface for the CalendarTag. The CalendarTag will set a day, 
 * then use the computeUrl method to get the URLs it needs.
 */
public interface CalendarModel 
{    
    public Calendar getCalendar();
    
	public void setDay( String month ) throws Exception;
    
	public Date getDay();
    
    public Date getNextMonth();
    
    public String computePrevMonthUrl();    

    public String computeTodayMonthUrl();    

    public String computeNextMonthUrl();
    
    /** 
     * Create URL for use on edit-weblog page, preserves the request
     * parameters used by the tabbed-menu tag for navigation.
     * 
     * @param day   Day for URL
     * @param valid Always return a URL, never return null 
     * @return URL for day, or null if no weblog entry on that day
     */
    public String computeUrl( java.util.Date day, boolean valid );
    
    /** 
     * Get calendar cell content or null if none.
     * 
     * @param day Day for URL
     * @param valid Always return a URL, never return null 
     * @return Calendar cell content or null if none.
     */
    public String getContent( java.util.Date day );
}

