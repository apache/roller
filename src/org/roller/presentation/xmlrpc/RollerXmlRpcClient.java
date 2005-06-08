/*
 * Created on Jun 9, 2003
 */
package org.roller.presentation.xmlrpc;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Weblogs Ping URLS:
 *  weblogs.com:http://rpc.weblogs.com:80/RPC2
 *  java.blogs: http://javablogs.com/xmlrpc
 *  blo.gs: http://ping.blo.gs/
 *  BlogRollering: http://rpc.blogrolling.com/pinger/
 *  Technorati: http://rpc.technorati.com/rpc/ping
 *  BlogChatter: http://www.mod-pubsub.org/kn_apps/blogchatter/ping.php
 * 
 * @author llavandowska
 */
public abstract class RollerXmlRpcClient
{
	public static String IO_MESSAGE = "Unable to connect.";
	public static String XMLRPC_MESSAGE = "Bad response.";
    
	/**
	 * Sends ping message to Weblogs.com RPC2 service.
	 * 
	 * @param blogUrl
	 * @param blogName
	 * @return
	 */
	public static String sendWeblogsPing(String blogUrl, String blogName)
	{
		String postTo = "http://rpc.weblogs.com:80/RPC2";
		try
		{            
			XmlRpcClient client = new XmlRpcClient(postTo);
			Vector params = new Vector();
			params.addElement(blogName);
			params.addElement(blogUrl);
			Hashtable result = (Hashtable)client.execute("weblogUpdates.ping", params);
			//Boolean error = (Boolean)result.get("flerror");

			return (String)result.get("message");
		}
		catch (IOException ioe)
		{
			//ioe.printStackTrace();
			return IO_MESSAGE;
		}
		catch (XmlRpcException xre)
		{
			//xre.printStackTrace();
			return XMLRPC_MESSAGE;
		}
	}
}
