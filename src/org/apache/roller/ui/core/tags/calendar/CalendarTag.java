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

package org.apache.roller.ui.core.tags.calendar;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.RequestUtils;
import org.apache.roller.ui.core.tags.HybridTag;
import org.apache.roller.util.DateUtil;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import org.apache.roller.ui.core.RollerContext;


/**
 * Calendar tag.
 * @jsp.tag name="Calendar"
 */
public class CalendarTag extends HybridTag {
    private static Log mLogger =
            LogFactory.getFactory().getInstance(CalendarTag.class);
    
    // JSP Attributes
    
    /** @jsp.attribute required="true" */
    public String getName() { return mName; }
    public void setName( String name ) { mName = name; }
    private String mName = null;
    
    /* @jsp.attribute description="Date in yyyyMMdd format"
    public String getDate() { return mDate; }
    public void setDate( String s ) { mDate = s; }
    private String mDate = null;
     */
    
    /** @jsp.attribute */
    public String getModel() { return mModelName; }
    public void setModel( String s ) { mModelName= s; }
    private String mModelName = null;
    
    /** @jsp.attribute */
    public String getClassSuffix() { return mClassSuffix; }
    public void setClassSuffix( String s ) { mClassSuffix= s; }
    private String mClassSuffix = "";
    
    // not a tag attribute
    public void setLocale(Locale locale) {
        if (locale != null)
            mLocale = locale;
    }
    private Locale mLocale = Locale.getDefault();
    
    // not a tag attribute
    /*
    private TimeZone mTimeZone = TimeZone.getDefault();
    public void setTimeZone(TimeZone zone) {
        if (zone != null)
            mTimeZone = zone;
    }
    private TimeZone getTimeZone()
    {
        // I've seen TimeZone.getDefault() return null. -Lance
        if (mTimeZone == null)
            mTimeZone = TimeZone.getTimeZone("America/New_York");
        return mTimeZone;
    }
     */
    
    private String[] mDayNames = null;
    
    public CalendarTag() {
        /*
         * Empty constructor.
         *
         * Used to build the day names, but the correct locale
         * was not set at this stage. Day-name-building has moved to the
         * doStartTag() method.
         */
    }
    
    //------------------------------------------------------------------------
    /**
     * Write to a PrintWriter so that tag may be used from Velocity
     */
    public int doStartTag( PrintWriter pw ) throws JspException {
        try {
            // build week day names
            this.buildDayNames();
            
            Date day=null;       // day to be displayed
            Calendar dayCal;     // set to day to be displayed
            Calendar cal;        // for iterating through days of month
            Calendar todayCal;   // for iterating through days of month
            CalendarModel model; // the calendar model
            
            // ---------------------------------
            // --- initialize date variables ---
            // ---------------------------------
            
            // check for parameter map and target url
            StringTokenizer toker = new StringTokenizer(mModelName,".");
            String tok1 = toker.nextToken();
            if (toker.hasMoreTokens()) {
                String tok2 = toker.nextToken();
                Object bean = pageContext.findAttribute(tok1);
                model = (CalendarModel)PropertyUtils.getProperty(bean, tok2);
            } else {
                model = (CalendarModel)pageContext.findAttribute( mModelName );
            }
            
            // no model specified, nothing to generate
            if (model == null) {
                return SKIP_BODY;
            }
            
            day = model.getDay();
            
            // ceate object to represent today
            todayCal = model.getCalendar();
            todayCal.setTime( new Date() );
            
            // formatter Month-Year title of calendar
            SimpleDateFormat formatTitle = new SimpleDateFormat("MMMM yyyy", mLocale);
            
            HttpServletRequest request =
                    (HttpServletRequest)pageContext.getRequest();
            
            // get Resource Bundle
            MessageResources resources = getResources(request);
            
            // go back to first day in month
            cal = model.getCalendar();
            day = DateUtil.getNoonOfDay(day, cal);
            cal.set( Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DAY_OF_MONTH) );
            
            // go back to sunday before that: the first sunday in the calendar
            while ( cal.get( Calendar.DAY_OF_WEEK ) != Calendar.SUNDAY ) {
                cal.add( Calendar.DATE, -1 );
            }
            
            // create table of 5 weeks, 7 days per row
            dayCal = model.getCalendar();
            dayCal.setTime( day );
            
            // -------------------------
            // --- draw the calendar ---
            // -------------------------
            pw.print("<table cellspacing=\"0\" border=\"0\" ");
            pw.print(" summary=\""
                    +resources.getMessage(mLocale, "calendar.summary")
                    +"\" class=\"hCalendarTable"
                    +mClassSuffix+"\">");
            pw.print("<tr>");
            pw.print("<td colspan=\"7\" align=\"center\" "+
                    "class=\"hCalendarMonthYearRow"+mClassSuffix+"\">");
            pw.print("<a href=\"" + model.computePrevMonthUrl()
            + "\" title=\"" + resources.getMessage(mLocale, "calendar.prev")
            + "\" class=\"hCalendarNavBar\">&laquo;</a> ");
            pw.print( formatTitle.format(day) );
            if (todayCal.getTime().compareTo(model.getNextMonth()) >= 0) {
                pw.print(" <a href=\"" + model.computeNextMonthUrl()
                + "\" title=\"" + resources.getMessage(mLocale, "calendar.next")
                + "\" class=\"hCalendarNavBar\">&raquo;</a>");
            }
            pw.print("</td></tr>");
            
            // emit the HTML calendar
            for ( int w=-1; w<6; w++ ) {
                pw.print("<tr>");
                for ( int d=0; d<7; d++ ) {
                    if ( w == -1 ) {
                        pw.print(
                                "<th class=\"hCalendarDayNameRow"
                                +mClassSuffix+"\" align=\"center\">");
                        pw.print( mDayNames[d] );
                        pw.print("</th>");
                        continue;
                    }
                    
                    // determine URL for this calendar day
                    Date tddate = cal.getTime();
                    String url = model.computeUrl(tddate, false, false);
                    String content = model.getContent( tddate );
                    
                    if // day is in calendar month
                            ((cal.get(Calendar.MONTH) == dayCal.get(Calendar.MONTH))
                            && (cal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR))) {
                        if // day is today then use today style
                                ((          cal.get(Calendar.DAY_OF_MONTH)
                                == todayCal.get(Calendar.DAY_OF_MONTH))
                                && (        cal.get(Calendar.MONTH)
                                == todayCal.get(Calendar.MONTH))
                                && (        cal.get(Calendar.YEAR)
                                == todayCal.get(Calendar.YEAR))) {
                            printToday(pw, cal, url, content);
                        } else {
                            printDayInThisMonth(pw, cal, url, content);
                        }
                    } else // apply day-not-in-month style ;-)
                    {
                        printDayNotInMonth(pw, cal);
                    }
                    
                    // increment calendar by one day
                    cal.add( Calendar.DATE, 1 );
                }
                pw.print("</tr>");
            }
            
            pw.print("<tr class=\"hCalendarNextPrev"
                    +mClassSuffix+"\">");
            pw.print("<td colspan=\"7\" align=\"center\">");
            
            pw.print("<a href=\""+model.computeTodayMonthUrl()
            +"\" class=\"hCalendarNavBar\">"
                    +resources.getMessage(mLocale, "calendar.today")
                    +"</a>");
            
            pw.print("</td>");
            pw.print("</tr>");
            
            pw.print("</table>");
        } catch (Exception e) {
            pw.print("<span class=\"error\">");
            pw.print("<p><b>An ERROR has occured CalendarTag</b></p>");
            pw.print("</span>");
            mLogger.error("Calendar tag exception",e);
        }
        return Tag.SKIP_BODY;
    }
    
    private void printDayNotInMonth(PrintWriter pw, Calendar cal) {
        pw.print("<td class=\"hCalendarDayNotInMonth"+mClassSuffix+"\">");
        //pw.print(cal.get(Calendar.DAY_OF_MONTH));
        pw.print("&nbsp;");
        pw.print("</td>");
    }
    
    private void printDayInThisMonth(PrintWriter pw, Calendar cal, String url, String content) {
        if ( content!=null ) {
            pw.print("<td class=\"hCalendarDayCurrent"
                    +mClassSuffix+"\">");
            pw.print( content );
            pw.print("</td>");
        } else if (url!=null) {
            pw.print("<td class=\"hCalendarDayLinked"
                    +mClassSuffix+"\">");
            pw.print("<div class=\"hCalendarDayTitle"
                    +mClassSuffix+"\">");
            pw.print("<a href=\""+url+"\">");
            pw.print(cal.get(Calendar.DAY_OF_MONTH));
            pw.print("</a></div>");
            pw.print("</td>");
        } else {
            pw.print("<td class=\"hCalendarDay"
                    +mClassSuffix+"\">");
            pw.print("<div class=\"hCalendarDayTitle"
                    +mClassSuffix+"\">");
            pw.print(cal.get(Calendar.DAY_OF_MONTH));
            pw.print("</div>");
            pw.print("</td>");
        }
    }
    
    private void printToday(PrintWriter pw, Calendar cal, String url, String content) {
        if ( content!=null ) {
            pw.print("<td class=\"hCalendarDayCurrent"
                    +mClassSuffix+"\">");
            pw.print( content );
            pw.print("</td>");
        } else if (url!=null) {
            pw.print("<td class=\"hCalendarDayCurrent"
                    +mClassSuffix+"\">");
            pw.print("<a href=\""+url+"\" "
                    +"class=\"hCalendarDayTitle"+mClassSuffix+"\">");
            pw.print(cal.get(Calendar.DAY_OF_MONTH));
            pw.print("</a>");
            pw.print("</td>");
        } else {
            pw.print("<td class=\"hCalendarDayCurrent"
                    +mClassSuffix+"\">");
            pw.print("<div class=\"hCalendarDayTitle"
                    +mClassSuffix+"\">");
            pw.print(cal.get(Calendar.DAY_OF_MONTH));
            pw.print("</div></td>");
        }
    }
    private MessageResources getResources(HttpServletRequest request) {
        ServletContext app = RollerContext.getServletContext();
        ModuleConfig moduleConfig = RequestUtils.getModuleConfig(request, app);
        return (MessageResources)app.getAttribute(Globals.MESSAGES_KEY +
                moduleConfig.getPrefix());
    }
    
    /**
     * Helper method to build the names of the weekdays. This
     * used to take place in the <code>CalendarTag</code> constructor,
     * but there, <code>mLocale</code> doesn't have the correct value yet.
     */
    private void buildDayNames() {
        // build array of names of days of week
        mDayNames = new String[7];
        Calendar dayNameCal = Calendar.getInstance(mLocale);
        SimpleDateFormat dayFormatter = new SimpleDateFormat("EEE", mLocale);
        dayNameCal.set(Calendar.DAY_OF_WEEK, dayNameCal.getFirstDayOfWeek());
        for (int dnum = 0; dnum < 7; dnum++) {
            mDayNames[dnum] = dayFormatter.format(dayNameCal.getTime());
            dayNameCal.add(Calendar.DATE, 1);
        }
    }
    
}

