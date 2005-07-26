package org.roller.presentation.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.struts.util.LabelValueBean;
import org.roller.util.LocaleComparator;
import org.roller.util.TimeZoneComparator;

public class StrutsUtil
{
    public static ArrayList locales;
    public static ArrayList timezones;
    
    //-----------------------------------------------------------------------
    /**
     * LabelValueBeans are Comparable but violate the
     * equals() part of the TreeSet requirements.
     * And the html:options tag won't recognize
     * toString as a property.  So we have to put the
     * Locales into a TreeSet to sort them, then convert
     * them to LabelValueBeans to display them.
     * Glad we only have to do this once.
     * 
     * @return List of LabelValueBeans, one for each locale available from the JVM
     */
    public static List getLocaleBeans() 
    {
        if (locales == null)
        {
            locales = new ArrayList();
            TreeSet locTree = new TreeSet(new LocaleComparator());
            Locale[] localeArray = Locale.getAvailableLocales();
            for (int i=0; i<localeArray.length; i++)
            {
                locTree.add(localeArray[i]);
            }
            java.util.Iterator it = locTree.iterator();
            while (it.hasNext())
            {
                Locale loc = (Locale)it.next();
                locales.add(new LabelValueBean(
                   loc.getDisplayName(),
                   loc.toString()));
            }

        }
        return locales;
    }

    //-----------------------------------------------------------------------
    /**
     * html:options tag recognizes "ID" as a property
     * so we don't have to go through all the rigamarole (sp?)
     * that we did for Locales.
     */
    public static List getTimeZoneBeans() 
    {
        if (timezones == null)
        {
            Date today = new Date();
            timezones = new ArrayList();
            TreeSet zoneTree = new TreeSet(new TimeZoneComparator());
            String[] zoneArray = TimeZone.getAvailableIDs();
            for (int i=0; i<zoneArray.length; i++)
            {
                zoneTree.add((TimeZone)TimeZone.getTimeZone(zoneArray[i]));
            }
            java.util.Iterator it = zoneTree.iterator();
            while (it.hasNext())
            {
                StringBuffer sb = new StringBuffer();
                TimeZone zone = (TimeZone)it.next();
                sb.append(zone.getDisplayName(zone.inDaylightTime(today), TimeZone.SHORT));
                sb.append(" - ");
                sb.append(zone.getID());
                timezones.add(new LabelValueBean(
                   sb.toString(),
                   zone.getID()));
            }
        }
        return timezones;
    }
}
