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

package org.apache.roller.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;


/**
 * A utility class for helping with sending emails.
 */
public class MailUtil {
    
    private static Log log = LogFactory.getLog(MailUtil.class);
    
    
    /**
     * Lookup the configured mail Session if possible.
     *
     * @returns Mail Session if possible, otherwise null.
     */
    public static Session getMailSession() throws RollerException {
        try {
            Context ctx = (Context) new InitialContext().lookup("java:comp/env");
            Session mailSession = (Session) ctx.lookup("mail/Session");
            return mailSession;
        } catch (NamingException ex) {
            throw new RollerException("ERROR: Notification email(s) not sent, "
                    + "Roller's mail session not properly configured", ex);
        }
    }
    
    
    /**
     * Send an email notice that a new pending entry has been submitted.
     */
    public static void sendPendingEntryNotice(WeblogEntryData entry) 
            throws RollerException {
        
        Session mailSession = getMailSession();
        if(mailSession == null) {
            throw new RollerException("Couldn't get mail Session");
        }
        
        try {
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            
            String userName = entry.getCreator().getUserName();
            String from = entry.getCreator().getEmailAddress();
            String cc[] = new String[] {from};
            String bcc[] = new String[0];
            String to[];
            String subject;
            String content;
            
            // list of enabled website authors and admins
            ArrayList reviewers = new ArrayList();
            List websiteUsers = umgr.getUsers(
                    entry.getWebsite(), Boolean.TRUE, null, null, 0, -1);
            
            // build list of reviewers (website users with author permission)
            Iterator websiteUserIter = websiteUsers.iterator();
            while (websiteUserIter.hasNext()) {
                UserData websiteUser = (UserData)websiteUserIter.next();
                if (entry.getWebsite().hasUserPermissions(
                        websiteUser, PermissionsData.AUTHOR)
                        && websiteUser.getEmailAddress() != null) {
                    reviewers.add(websiteUser.getEmailAddress());
                }
            }
            to = (String[])reviewers.toArray(new String[reviewers.size()]);
            
            // Figure URL to entry edit page
            String editURL = URLUtilities.getEntryEditURL(entry.getWebsite().getHandle(), entry.getId(), true);
            
            ResourceBundle resources = ResourceBundle.getBundle(
                    "ApplicationResources", entry.getWebsite().getLocaleInstance());
            StringBuffer sb = new StringBuffer();
            sb.append(
                    MessageFormat.format(
                    resources.getString("weblogEntry.pendingEntrySubject"),
                    new Object[] {
                entry.getWebsite().getName(),
                entry.getWebsite().getHandle()
            }));
            subject = sb.toString();
            sb = new StringBuffer();
            sb.append(
                    MessageFormat.format(
                    resources.getString("weblogEntry.pendingEntryContent"),
                    new Object[] { userName, userName, editURL })
                    );
            content = sb.toString();
            MailUtil.sendTextMessage(
                    mailSession, from, to, cc, bcc, subject, content);
        } catch (MessagingException e) {
            log.error("ERROR: Problem sending pending entry notification email.");
        }
    }
    
    
    /**
     * Send a weblog invitation email.
     */
    public static void sendWeblogInvitation(WebsiteData website, 
                                            UserData user)
            throws RollerException {
        
        Session mailSession = getMailSession();
        if(mailSession == null) {
            throw new RollerException("ERROR: Notification email(s) not sent, "
                    + "Roller's mail session not properly configured");
        }
        
        try {
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            
            String userName = user.getUserName();
            String from = website.getEmailAddress();
            String cc[] = new String[] {from};
            String bcc[] = new String[0];
            String to[] = new String[] {user.getEmailAddress()};
            String subject;
            String content;
            
            // Figure URL to entry edit page
            String rootURL = RollerRuntimeConfig.getAbsoluteContextURL();
            String url = rootURL + "/roller-ui/menu.rol";
            
            ResourceBundle resources = ResourceBundle.getBundle(
                    "ApplicationResources",
                    website.getLocaleInstance());
            StringBuffer sb = new StringBuffer();
            sb.append(MessageFormat.format(
                    resources.getString("inviteMember.notificationSubject"),
                    new Object[] {
                website.getName(),
                website.getHandle()})
                );
            subject = sb.toString();
            sb = new StringBuffer();
            sb.append(MessageFormat.format(
                    resources.getString("inviteMember.notificationContent"),
                    new Object[] {
                website.getName(),
                website.getHandle(),
                user.getUserName(),
                url
            }));
            content = sb.toString();
            MailUtil.sendTextMessage(
                    mailSession, from, to, cc, bcc, subject, content);
        } catch (MessagingException e) {
            throw new RollerException("ERROR: Notification email(s) not sent, "
                    + "due to Roller configuration or mail server problem.", e);
        }
    }
    
    
    /**
     * Send a weblog invitation email.
     */
    public static void sendUserActivationEmail(UserData user)
            throws RollerException {
        
        Session mailSession = getMailSession();
        if(mailSession == null) {
            throw new RollerException("ERROR: Notification email(s) not sent, "
                    + "Roller's mail session not properly configured");
        }
        
        try {
            ResourceBundle resources = ResourceBundle.getBundle(
                    "ApplicationResources", getLocaleInstance(user.getLocale()));
            
            String from = RollerRuntimeConfig.getProperty(
                    "user.account.activation.mail.from");
            
            String cc[] = new String[0];
            String bcc[] = new String[0];
            String to[] = new String[] { user.getEmailAddress() };
            String subject = resources.getString(
                    "user.account.activation.mail.subject");
            String content;
            
            String rootURL = RollerRuntimeConfig.getAbsoluteContextURL();
            
            StringBuffer sb = new StringBuffer();
            
            // activationURL=
            String activationURL = rootURL
                    + "/roller-ui/register!activate.rol?activationCode="
                    + user.getActivationCode();
            sb.append(MessageFormat.format(
                    resources.getString("user.account.activation.mail.content"),
                    new Object[] { user.getFullName(), user.getUserName(),
                    activationURL }));
            content = sb.toString();
            
            sendHTMLMessage(mailSession, from, to, cc, bcc, subject, content);
        } catch (MessagingException e) {
            throw new RollerException("ERROR: Problem sending activation email.", e);
        }
    }
    
    
    // TODO: there is a better place for this, but i am being lazy :/
    private static Locale getLocaleInstance(String locale) {
        if (locale != null) {
            String[] localeStr = StringUtils.split(locale, "_");
            if (localeStr.length == 1) {
                if (localeStr[0] == null)
                    localeStr[0] = "";
                return new Locale(localeStr[0]);
            } else if (localeStr.length == 2) {
                if (localeStr[0] == null)
                    localeStr[0] = "";
                if (localeStr[1] == null)
                    localeStr[1] = "";
                return new Locale(localeStr[0], localeStr[1]);
            } else if (localeStr.length == 3) {
                if (localeStr[0] == null)
                    localeStr[0] = "";
                if (localeStr[1] == null)
                    localeStr[1] = "";
                if (localeStr[2] == null)
                    localeStr[2] = "";
                return new Locale(localeStr[0], localeStr[1], localeStr[2]);
            }
        }
        return Locale.getDefault();
    }
    
    
    // agangolli: Incorporated suggested changes from Ken Blackler.
    
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
            String[] bcc,
            String subject,
            String content,
            String mimeType
            )
            throws MessagingException {
        Message message = new MimeMessage(session);
        
        // n.b. any default from address is expected to be determined by caller.
        if (! StringUtils.isEmpty(from)) {
            InternetAddress sentFrom = new InternetAddress(from);
            message.setFrom(sentFrom);
            if (log.isDebugEnabled()) log.debug("e-mail from: " + sentFrom);
        }
        
        if (to!=null) {
            InternetAddress[] sendTo = new InternetAddress[to.length];
            
            for (int i = 0; i < to.length; i++) {
                sendTo[i] = new InternetAddress(to[i]);
                if (log.isDebugEnabled()) log.debug("sending e-mail to: " + to[i]);
            }
            message.setRecipients(Message.RecipientType.TO, sendTo);
        }
        
        if (cc != null) {
            InternetAddress[] copyTo = new InternetAddress[cc.length];
            
            for (int i = 0; i < cc.length; i++) {
                copyTo[i] = new InternetAddress(cc[i]);
                if (log.isDebugEnabled()) log.debug("copying e-mail to: " + cc[i]);
            }
            message.setRecipients(Message.RecipientType.CC, copyTo);
        }
        
        if (bcc != null) {
            InternetAddress[] copyTo = new InternetAddress[bcc.length];
            
            for (int i = 0; i < bcc.length; i++) {
                copyTo[i] = new InternetAddress(bcc[i]);
                if (log.isDebugEnabled()) log.debug("blind copying e-mail to: " + bcc[i]);
            }
            message.setRecipients(Message.RecipientType.BCC, copyTo);
        }
        message.setSubject((subject == null) ? "(no subject)" : subject);
        message.setContent(content, mimeType);
        message.setSentDate(new java.util.Date());
        
        // First collect all the addresses together.
        Address[] remainingAddresses = message.getAllRecipients();
        int nAddresses = remainingAddresses.length;
        boolean bFailedToSome = false;
        
        SendFailedException sendex = new SendFailedException("Unable to send message to some recipients");
        
        // Try to send while there remain some potentially good addresses
        do
        {
            // Avoid a loop if we are stuck
            nAddresses = remainingAddresses.length;
            
            try {
                // Send to the list of remaining addresses, ignoring the addresses attached to the message
                Transport.send(message,remainingAddresses);
            } catch(SendFailedException ex) {
                bFailedToSome=true;
                sendex.setNextException(ex);
                
                // Extract the remaining potentially good addresses
                remainingAddresses=ex.getValidUnsentAddresses();
            }
        } while (remainingAddresses!=null && remainingAddresses.length>0 && remainingAddresses.length!=nAddresses);
        
        if (bFailedToSome) throw sendex;
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
            String[] bcc,
            String subject,
            String content
            )
            throws MessagingException {
        sendMessage(session, from, to, cc, bcc, subject, content, "text/plain; charset=utf-8");
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
            String[] bcc,
            String subject,
            String content
            )
            throws MessagingException {
        String[] recipient = null;
        if (to!=null) recipient = new String[] {to};
        
        sendMessage(session, from, recipient, cc, bcc, subject, content, "text/plain; charset=utf-8");
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
            String bcc,
            String subject,
            String content
            )
            throws MessagingException {
        String[] recipient = null;
        String[] copy = null;
        String[] bcopy = null;
        
        if (to!=null) recipient = new String[] {to};
        if (cc!=null) copy = new String[] {cc};
        if (bcc!=null) bcopy = new String[] {bcc};
        
        sendMessage(session, from, recipient, copy, bcopy, subject, content, "text/plain; charset=utf-8");
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
            String[] bcc,
            String subject,
            String content
            )
            throws MessagingException {
        sendMessage(session, from, to, cc, bcc, subject, content, "text/html; charset=utf-8");
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
            String bcc,
            String subject,
            String content
            )
            throws MessagingException {
        String[] recipient = null;
        String[] copy = null;
        String[] bcopy = null;
        
        if (to!=null) recipient = new String[] {to};
        if (cc!=null) copy = new String[] {cc};
        if (bcc!=null) bcopy = new String[] {bcc};
        
        sendMessage(session, from, recipient, copy, bcopy, subject, content, "text/html; charset=utf-8");
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
            String[] bcc,
            String subject,
            String content
            )
            throws MessagingException {
        String[] recipient = null;
        if (to!=null) recipient = new String[] {to};
        
        sendMessage(session, from, recipient, cc, bcc, subject, content, "text/html; charset=utf-8");
    }
    
}
