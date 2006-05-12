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
package org.apache.roller;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.roller.business.FileManagerTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author dmj
 */
public class DateTest extends TestCase
{

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(DateTest.class);
    }
    
    public void testDate() throws Exception
    {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        Date newDate = df.parse("5/8/1964");
        Calendar cal = Calendar.getInstance();
        cal.setTime(newDate);
        cal.set(Calendar.HOUR_OF_DAY, 11);
        cal.set(Calendar.MINUTE, 39);
        cal.set(Calendar.SECOND, 0);
        System.out.println(cal.getTime().toString());
    }

    public static Test suite()
    {
        return new TestSuite(DateTest.class);
    }
}
