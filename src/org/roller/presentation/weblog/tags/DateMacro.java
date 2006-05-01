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
package org.roller.presentation.weblog.tags;

import java.text.SimpleDateFormat;
import java.util.Date;

/** For formatting a date using patterns from 
 * {@link java.text.SimpleDateFormat SimpleDateFormat}.
 * @deprecated Use methods on 
 * {@link org.roller.presentation.weblog.WeblogEntryDataEx WeblogEntryDataEx} 
 * instead.
 */
public class DateMacro
{
	final Date mDate;
    
    /** Construct macro for specified date */
	public DateMacro( Date d ) 
	{ 
		mDate = d; 
	}
    
    /** Format date using pattern */
	public String view( String pattern )
	{
		SimpleDateFormat format = new SimpleDateFormat( pattern );
		return format.format( mDate );
	}
    
    /** Format date using standard format. */
	public String toString()
	{
		return view("EEEE MMMM dd, yyyy");
	}
}


