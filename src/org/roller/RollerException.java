
package org.roller;

import java.io.PrintStream;
import java.io.PrintWriter;

/** 
 * Generic RollerException
 */ 
public class RollerException extends Exception
{
    private Throwable mRootCause = null;
    
	public RollerException(String s,Throwable t)
	{
		super(s);
        mRootCause = t;
	}
	public RollerException(Throwable t)
	{
        mRootCause = t;
	}
	public RollerException(String s)
	{
		super(s);
	}
	public RollerException()
	{
		super();
	}
    public Throwable getRootCause()
    {
        return mRootCause;
    }
   
    /** 
     * @see java.lang.Throwable#printStackTrace()
     */
    public void printStackTrace()
    {
        super.printStackTrace();
        if (mRootCause != null)
        {
            System.out.println("--- ROOT CAUSE ---");
            mRootCause.printStackTrace();
        }
    }

    /** 
     * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
     */
    public void printStackTrace(PrintStream s)
    {
        super.printStackTrace(s);
        if (mRootCause != null)
        {
            s.println("--- ROOT CAUSE ---");
            mRootCause.printStackTrace(s);
        }
    }

    /** 
     * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
     */
    public void printStackTrace(PrintWriter s)
    {
       super.printStackTrace(s);
       if (null != mRootCause) 
       {
           s.println("--- ROOT CAUSE ---");
           mRootCause.printStackTrace(s);
       }
    }

    public String getRootCauseMessage() 
    {
        String rcmessage = null;
        if (getRootCause()!=null) 
        {
            if (getRootCause().getCause()!=null)
            {
                rcmessage = getRootCause().getCause().getMessage();
            }
            rcmessage = (rcmessage == null) ? getRootCause().getMessage() : rcmessage;
            rcmessage = (rcmessage == null) ? super.getMessage() : rcmessage;
            rcmessage = (rcmessage == null) ? "NONE" : rcmessage;
        }
        return rcmessage;
    }
}

