
package org.roller;

/** 
 * Datastore exception.
 */ 
public class RollerDatastoreException extends RollerException
{
	public RollerDatastoreException(String s,Throwable t)
	{
		super(s,t);
	}
	public RollerDatastoreException(Throwable t)
	{
        super(t);
	}
	public RollerDatastoreException(String s)
	{
		super(s);
	}
}

