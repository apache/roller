package org.roller;

/**
 * Thrown when persistence session user lacks one or more required permissions.
 */ 
public class RollerPermissionsException extends RollerException
{
	/**
	 * Construct RollerException, wrapping existing throwable.
	 * @param s Error message.
	 * @param t Throwable to be wrapped
	 */
	public RollerPermissionsException(String s,Throwable t)
	{
		super(s,t);
	}
	/**
	 * Construct RollerException, wrapping existing throwable.
	 * @param t Throwable to be wrapped
	 */
	public RollerPermissionsException(Throwable t)
	{
        super(t);
	}
	/**
	 * Construct RollerException, with error message.
	 * @param s Error message
	 */
	public RollerPermissionsException(String s)
	{
		super(s);
	}
}

