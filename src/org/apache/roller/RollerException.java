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

package org.apache.roller;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Roller services interfaces throw this exception.
 */ 
public class RollerException extends Exception
{
    private Throwable mRootCause = null;
    
	/**
	 * Construct RollerException, wrapping existing throwable.
	 * @param s Error message
	 * @param t Existing connection to wrap.
	 */
	public RollerException(String s,Throwable t)
	{
		super(s);
        mRootCause = t;
	}
	/**
	 * Construct RollerException, wrapping existing throwable.
	 * @param t Existing exception to be wrapped.
	 */
	public RollerException(Throwable t)
	{
        mRootCause = t;
	}
	/**
	 * Construct RollerException with message string.
	 * @param s Error message string.
	 */
	public RollerException(String s)
	{
		super(s);
	}
	/**
	 * Construct emtpy exception object.
	 */
	public RollerException()
	{
		super();
	}
    /**
     * Get root cause object, or null if none.
     * @return Root cause or null if none.
     */
    public Throwable getRootCause()
    {
        return mRootCause;
    }
   
    /**
     * Print stack trace for exception and for root cause exception if htere is one.
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
     * Print stack trace for exception and for root cause exception if htere is one.
     * @param s Stream to print to.
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
     * Print stack trace for exception and for root cause exception if htere is one.
     * @param s Writer to write to.
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

    /**
     * Get root cause message.
     * @return Root cause message.
     */
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

