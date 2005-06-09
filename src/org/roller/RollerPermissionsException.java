package org.roller;

/** 
 * Throw when persistence session user lacks one or more required permissions.
 */ 
public class RollerPermissionsException extends RollerException
{
	public RollerPermissionsException(String s,Throwable t)
	{
		super(s,t);
	}
	public RollerPermissionsException(Throwable t)
	{
        super(t);
	}
	public RollerPermissionsException(String s)
	{
		super(s);
	}
}

