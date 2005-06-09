
package org.roller.presentation;

import javax.servlet.jsp.JspException;

/** 
 * Indicates user is not authorized to access a resource.
 */ 
public class AccessDeniedException extends JspException 
{
	public AccessDeniedException(String s,Throwable t)
	{
		super(s,t);
	}
	public AccessDeniedException(Throwable t)
	{
		super(t);
	}
	public AccessDeniedException(String s)
	{
		super(s);
	}
	public AccessDeniedException()
	{
		super();
	}
}

