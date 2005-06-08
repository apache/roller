/*
 * Created on Jun 10, 2003
 */
package org.roller.presentation.xmlrpc;

import junit.framework.TestCase;

/**
 * @author llavandowska
 */
public class TestRollerXmlRpcClient extends TestCase
{
	/**
	 * Constructor for LinkbackExtractorTest.
	 * @param arg0
	 */
	public TestRollerXmlRpcClient(String arg0)
	{
		super(arg0);
	}

	public static void main(String[] args)
	{
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	/**
	 * Perform same ping twice to guarantee a getting the "Take a Break" message.
	 *
	 */
	public void testSendWeblogsPing()
	{
		String testUrl = "http://www.brainopolis.com/roller/page/lance";
		String testName = "Vanity Foul";
		String TakeABreak = "Thanks for the ping, however we can only accept one ping every five minutes. It's cool that you're updating so often, however, if I may be so bold as to offer some advice -- take a break, you'll enjoy life more.";
		
		String response = RollerXmlRpcClient.sendWeblogsPing( testUrl, testName );
		
		response = RollerXmlRpcClient.sendWeblogsPing( testUrl, testName );
			
		System.out.print(response);
		
		// not an IOException
		assertNotSame("IOException", RollerXmlRpcClient.IO_MESSAGE, response);
		
		// not an XmlRpcException
		assertNotSame("XmlRpcException", RollerXmlRpcClient.XMLRPC_MESSAGE, response);
		
		// is "take a break" message
		assertEquals(TakeABreak, response);
	}

}
