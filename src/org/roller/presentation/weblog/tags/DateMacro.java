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
	Date mDate = null;
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


