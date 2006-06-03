/*
 * Created on Mar 10, 2004
 */
package org.roller;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import org.roller.business.FileManagerTest;

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
