package org.roller.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class MailUtil extends Object {
   
	private static Log mLogger = 
		LogFactory.getFactory().getInstance(MailUtil.class);
		
    /**
     * This method is used to send a Message with a pre-defined
     * mime-type.
     * 
     * @param from e-mail address of sender
     * @param to e-mail address(es) of recipients
     * @param subject subject of e-mail
     * @param content the body of the e-mail
     * @param mimeType type of message, i.e. text/plain or text/html
     * @throws MessagingException the exception to indicate failure
     */
    public static void sendMessage
    (
    	Session session,
        String from,
        String[] to,
        String[] cc,
        String subject,
        String content,
        String mimeType
    ) 
    throws MessagingException
    {
        Message message = new MimeMessage(session);
        
        // TODO Add configuration for default "from" email address
        if (! StringUtils.isEmpty(from)) {
			InternetAddress sentFrom = new InternetAddress(from);
			message.setFrom(sentFrom);
			if (mLogger.isDebugEnabled()) 
			{
				mLogger.debug("e-mail from: " + sentFrom);
			}
        }

		InternetAddress[] sendTo = new InternetAddress[to.length];

		for (int i = 0; i < to.length; i++) 
		{
			sendTo[i] = new InternetAddress(to[i]);

			if (mLogger.isDebugEnabled()) 
			{
				mLogger.debug("sending e-mail to: " + to[i]);
			}
		}
		message.setRecipients(Message.RecipientType.TO, sendTo);

		if (cc != null) 
		{
			InternetAddress[] copyTo = new InternetAddress[cc.length];

			for (int i = 0; i < cc.length; i++) 
			{
				copyTo[i] = new InternetAddress(cc[i]);

				if (mLogger.isDebugEnabled()) 
				{
					mLogger.debug("copying e-mail to: " + cc[i]);
				}
			}

			message.setRecipients(Message.RecipientType.CC, copyTo);
		}	        

        message.setSubject((subject == null) ? "(no subject)" : subject);
        message.setContent(content, mimeType);

        Transport.send(message);
    }

    /**
     * This method is used to send a Text Message.
     * 
     * @param from e-mail address of sender
     * @param to e-mail addresses of recipients
     * @param subject subject of e-mail
     * @param content the body of the e-mail
     * @throws MessagingException the exception to indicate failure
     */
    public static void sendTextMessage
    (
    	Session session,
        String from,
        String[] to,
        String[] cc,
        String subject,
        String content
    ) 
    throws MessagingException
    {
        sendMessage(session, from, to, cc, subject, content, "text/plain; charset=utf-8");
    }
    
	/**
	 * This method overrides the sendTextMessage to specify
	 * one receiver and mulitple cc recipients.
	 * 
	 * @param from e-mail address of sender
	 * @param to e-mail addresses of recipients
	 * @param subject subject of e-mail
	 * @param content the body of the e-mail
	 * @throws MessagingException the exception to indicate failure
	 */
	public static void sendTextMessage
	(
		Session session,
		String from,
		String to,
		String[] cc,
		String subject,
		String content
	) 
	throws MessagingException
	{
		String[] recipient = {to};
		sendMessage(session, from, recipient, cc, subject, content, "text/plain; charset=utf-8");
	}
	
    /**
	 * This method overrides the sendTextMessage to specify
	 * only one receiver and cc recipients, rather than 
	 * an array of recipients.
     * 
     * @param from e-mail address of sender
     * @param to e-mail address of recipient
     * @param cc e-mail address of cc recipient
     * @param subject subject of e-mail
     * @param content the body of the e-mail
     * @throws MessagingException the exception to indicate failure
     */
    public static void sendTextMessage
    (
    	Session session,
        String from,
        String to,
        String cc,
        String subject,
        String content
    ) 
    throws MessagingException
    {
        String[] recipient = {to};
        String[] copy = {cc};
        sendMessage(session, from, recipient, copy, subject, content, "text/plain; charset=utf-8");
    }
    
    /**
     * This method is used to send a HTML Message
     * 
     * @param from e-mail address of sender
     * @param to e-mail address(es) of recipients
     * @param subject subject of e-mail
     * @param content the body of the e-mail
     * @throws MessagingException the exception to indicate failure
     */
    public static void sendHTMLMessage
    (
    	Session session,
        String from,
        String[] to,
        String[] cc,
        String subject,
        String content
    ) 
    throws MessagingException
    {
        sendMessage(session, from, to, cc, subject, content, "text/html; charset=utf-8");
    }
    
    /**
     * This method overrides the sendHTMLMessage to specify
     * only one sender, rather than an array of senders.
     * 
     * @param from e-mail address of sender
     * @param to e-mail address of recipients
     * @param subject subject of e-mail
     * @param content the body of the e-mail
     * @throws MessagingException the exception to indicate failure
     */
	public static void sendHTMLMessage
    (
    	Session session,
        String from,
        String to,
        String cc,
        String subject,
        String content
    ) 
    throws MessagingException
    {
        String[] recipient = {to};
        String[] copy = {cc};
        sendMessage(session, from, recipient, copy, subject, content, "text/html; charset=utf-8");
    }
    
	/**
	 * This method overrides the sendHTMLMessage to specify
	 * one receiver and mulitple cc recipients.
	 * 
	 * @param from e-mail address of sender
	 * @param to e-mail address of recipient
	 * @param cc e-mail addresses of recipients
	 * @param subject subject of e-mail
	 * @param content the body of the e-mail
	 * @throws MessagingException the exception to indicate failure
	 */
	public static void sendHTMLMessage
	(
		Session session,
		String from,
		String to,
		String[] cc,
		String subject,
		String content
	) 
	throws MessagingException
	{
		String[] recipient = {to};
		sendMessage(session, from, recipient, cc, subject, content, "text/html; charset=utf-8");
	}
}
