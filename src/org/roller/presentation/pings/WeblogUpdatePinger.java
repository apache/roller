/*
 * Copyright (c) 2004-2005
 * Lance Lavandowska, Anil R. Gangolli.
 * All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License.
 */

package org.roller.presentation.pings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.pojos.PingTargetData;
import org.roller.pojos.WebsiteData;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Utility for sending a weblog update ping.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</author>
 * @author llavandowska (for code refactored from the now-defunct <code>RollerXmlRpcClient</code>)
 */
public class WeblogUpdatePinger
{
    public static final Log logger = LogFactory.getLog(WeblogUpdatePinger.class);

    /**
     * Conveys a ping result.
     */
    public static class PingResult
    {
        boolean error;
        String message;

        public PingResult(Boolean error, String message)
        {
            this.error = error != null ? error.booleanValue() : false;
            this.message = message;
        }

        public boolean isError()
        {
            return error;
        }

        public void setError(boolean error)
        {
            this.error = error;
        }

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }

        public String toString()
        {
            return "PingResult{" +
                "error=" + error +
                ", message='" + message + "'" +
                "}";
        }
    }

    // Inhibit construction
    private WeblogUpdatePinger()
    {
    }

    /**
     * Send a weblog update ping.
     *
     * @param absoluteContextUrl the absolute context url of the Roller site.
     * @param pingTarget         the target site to ping
     * @param website            the website that changed (from which the ping originates)
     * @return the result message string sent by the server.
     * @throws IOException
     * @throws XmlRpcException
     * @throws RollerException
     */
    public static PingResult sendPing(String absoluteContextUrl, PingTargetData pingTarget, WebsiteData website)
        throws RollerException, IOException, XmlRpcException
    {
        // Figure out the url of the user's website.
        String websiteUrl =
            RollerFactory.getRoller().getWeblogManager().getUrl(website.getUser(), absoluteContextUrl);

        // Set up the ping parameters.
        Vector params = new Vector();
        params.addElement(website.getName());
        params.addElement(websiteUrl);
        if (logger.isDebugEnabled())
        {
            logger.debug("Executing ping to '" + pingTarget.getPingUrl() + "' for website '" +
                websiteUrl + "' (" + website.getName() + ")");
        }

        // Send the ping
        XmlRpcClient client = new XmlRpcClient(pingTarget.getPingUrl());
        Hashtable result = (Hashtable) client.execute("weblogUpdates.ping", params);
        PingResult pingResult = new PingResult((Boolean) result.get("flerror"), (String) result.get("message"));
        if (logger.isDebugEnabled()) logger.debug("Ping result is: " + pingResult);
        return pingResult;
    }

    /**
     * Decide if the given exception appears to warrant later retrial attempts.
     *
     * @param ex an exception thrown by the <coce>sendPing</code> operation
     * @return true if the error warrants retrial
     */
    public static boolean shouldRetry(Exception ex)
    {
        // Determine if error appears transient (warranting retrial)
        // We give most errors the "benefit of the doubt" by considering them transient
        // This picks out a few that we consider non-transient
        if (ex instanceof UnknownHostException)
        {
            // User probably mistyped the url in the custom target.
            return false;
        }
        else if (ex instanceof MalformedURLException)
        {
            // This should never happen due to validations but if we get here, retrial won't fix it.
            return false;
        }
        return true;
    }

}
