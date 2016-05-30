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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.pojos.SafeUser;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.RollerMessages.RollerMessage;
import org.apache.roller.weblogger.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;


/**
 * A utility class for helping with sending email. 
 */
public class MailManager {

    private static Logger log = LoggerFactory.getLogger(MailManager.class);

    private static final String EMAIL_ADDR_REGEXP = "^.*@.*[.].{2,}$";

    private UserManager userManager;

    private WeblogManager weblogManager;

    private URLStrategy urlStrategy;

    private PropertiesManager propertiesManager;

    /**
     * Create file content manager.
     */
    public MailManager(UserManager umgr, WeblogManager wmgr, PropertiesManager pmgr, URLStrategy strategy) {
        userManager = umgr;
        weblogManager = wmgr;
        propertiesManager = pmgr;
        urlStrategy = strategy;
    }

    /**
     * Ideally mail senders should call this first to avoid errors that occur 
     * when mail is not properly configured. We'll complain about that at 
     * startup, no need to complain on every attempt to send.
     */
    public boolean isMailConfigured() {
        return WebloggerFactory.getMailProvider() != null;
    }
    
    /**
     * Send an email notice that a new pending entry has been submitted.
     */
    public void sendPendingEntryNotice(WeblogEntry entry) {
        
        Session mailSession = WebloggerFactory.getMailProvider() != null
                ? WebloggerFactory.getMailProvider().getSession() : null;

        if (mailSession == null) {
            log.info("Cannot send emails, no mail session configured.");
            return;
        }
        
        try {
            String screenName = entry.getCreator().getScreenName();
            String from = entry.getCreator().getEmailAddress();
            String cc[] = new String[] {from};
            String bcc[] = new String[0];
            String to[];
            String subject;
            String content;
            
            // list of enabled website authors and admins
            List<String> reviewers = new ArrayList<>();
            List<User> websiteUsers = weblogManager.getWeblogUsers(entry.getWeblog(), true);
            
            // build list of reviewers (website users with author permission)
            for (User websiteUser : websiteUsers) {
                if (userManager.checkWeblogRole(websiteUser, entry.getWeblog(), WeblogRole.POST)
                        && websiteUser.getEmailAddress() != null) {
                    reviewers.add(websiteUser.getEmailAddress());
                }
            }

            to = reviewers.toArray(new String[reviewers.size()]);
            
            // Figure URL to entry edit page
            String editURL = urlStrategy.getEntryEditURL(entry.getWeblog().getHandle(), entry.getId(), true);
            
            ResourceBundle resources = ResourceBundle.getBundle(
                    "ApplicationResources", entry.getWeblog().getLocaleInstance());
            StringBuilder sb = new StringBuilder();
            sb.append(
                    MessageFormat.format(
                    resources.getString("weblogEntry.pendingEntrySubject"),
                    new Object[] {
                entry.getWeblog().getName(),
                entry.getWeblog().getHandle()
            }));
            subject = sb.toString();
            sb = new StringBuilder();
            sb.append(
                    MessageFormat.format(
                    resources.getString("weblogEntry.pendingEntryContent"),
                    new Object[] { screenName, screenName, editURL })
                    );
            content = sb.toString();
            sendTextMessage(from, to, cc, bcc, subject, content);
        } catch (MessagingException e) {
            log.error("ERROR: Problem sending pending entry notification email.");
        }
    }
    
    
    /**
     * Send a weblog invitation email.
     */
    public void sendWeblogInvitation(Weblog website, User user) throws MessagingException {
        
        Session mailSession = WebloggerFactory.getMailProvider() != null
                ? WebloggerFactory.getMailProvider().getSession() : null;

        if (mailSession == null) {
            log.info("Cannot send emails, no mail session configured.");
            return;
        }
        
        String from = website.getCreator().getEmailAddress();
        String cc[] = new String[] {from};
        String bcc[] = new String[0];
        String to[] = new String[] {user.getEmailAddress()};
        String subject;
        String content;

        // Figure URL to entry edit page
        String rootURL = WebloggerStaticConfig.getAbsoluteContextURL();
        String url = rootURL + "/tb-ui/menu.rol";

        ResourceBundle resources = ResourceBundle.getBundle(
                "ApplicationResources",
                website.getLocaleInstance());
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format(
                resources.getString("inviteMember.notificationSubject"),
                new Object[] {
            website.getName(),
            website.getHandle()})
            );
        subject = sb.toString();
        sb = new StringBuilder();
        sb.append(MessageFormat.format(
                resources.getString("inviteMember.notificationContent"),
                new Object[] {
            website.getName(),
            website.getHandle(),
            user.getUserName(),
            url
        }));
        content = sb.toString();
        sendTextMessage(from, to, cc, bcc, subject, content);
    }
    
    
    /**
     * Send a weblog invitation email.
     */
    public void sendUserActivationEmail(User user) throws MessagingException {
        
        Session mailSession = WebloggerFactory.getMailProvider() != null
                ? WebloggerFactory.getMailProvider().getSession() : null;

        if (mailSession == null) {
            log.info("Cannot send emails, no mail session configured.");
            return;
        }
        
        ResourceBundle resources = ResourceBundle.getBundle(
                "ApplicationResources", Locale.forLanguageTag(user.getLocale()));

        String from = propertiesManager.getStringProperty(
                "user.account.activation.mail.from");

        String cc[] = new String[0];
        String bcc[] = new String[0];
        String to[] = new String[] { user.getEmailAddress() };
        String subject = resources.getString(
                "user.account.activation.mail.subject");
        String content;

        String rootURL = WebloggerStaticConfig.getAbsoluteContextURL();

        StringBuilder sb = new StringBuilder();

        // activationURL=
        String activationURL = rootURL
                + "/tb-ui/emailResponse!activate.rol?activationCode="
                + user.getActivationCode();
        sb.append(MessageFormat.format(
                resources.getString("user.account.activation.mail.content"),
                new Object[] { user.getScreenName(), user.getUserName(),
                activationURL }));
        content = sb.toString();

        sendHTMLMessage(from, to, cc, bcc, subject, content);
    }
    
    
    /**
     * Send email notification of new or newly approved comment.
     *
     * @param commentObject      The new comment
     * @param messages           Messages to be included in e-mail (or null). 
     *                           Errors will be assumed to be "validation errors" 
     *                           and messages will be assumed to be "from the system"
     */
    public void sendEmailNotification(WeblogEntryComment commentObject,
                                             RollerMessages messages,
                                             I18nMessages resources,
                                             boolean notifySubscribers) {

        WeblogEntry entry = commentObject.getWeblogEntry();
        Weblog weblog = entry.getWeblog();
        SafeUser user = entry.getCreator();
        
        // Only send email if email notification is enabled, or a pending message that needs moderation.
        if (!commentObject.getPending()) {
            boolean notify = propertiesManager.getBooleanProperty("users.comments.emailnotify");
            if (!notify) {
                // notifications disabled, just bail
                return;
            } else {
                log.debug("Comment notification enabled ... preparing email");
            }
        } else {
            log.debug("Pending comment...sending moderation email to blog owner");
        }

        // build list of email addresses to send notification to
        Set<String> subscribers = new TreeSet<>();
        
        // If we are to notify subscribers, then...
        if (commentObject.getApproved() && notifySubscribers) {
            log.debug("Sending notification email to all subscribers");

            // Get all the subscribers to this comment thread
            List<WeblogEntryComment> comments = entry.getComments();
            for (WeblogEntryComment comment : comments) {
                if (!StringUtils.isEmpty(comment.getEmail())) {
                    // if user has commented twice, count the most recent notify setting
                    // also, don't send a routing email to the person who made the comment.
                    if (comment.getNotify() && !comment.getEmail().equals(commentObject.getEmail())) {
                        // only add those with valid email
                        if (comment.getEmail().matches(EMAIL_ADDR_REGEXP)) {
                            log.info("Add to subscribers list: {}", comment.getEmail());
                            subscribers.add(comment.getEmail());
                        }
                    } else {
                        // remove user who doesn't want to be notified
                        log.info("Remove from subscribers list: {}", comment.getEmail());
                        subscribers.remove(comment.getEmail());
                    }
                }
            }
        } else {
            log.debug("Sending notification email only to weblog owner");
        }

        //------------------------------------------
        // Form the messages to be sent -
        // The owner email gets the subscriber message with additional header and footer info added
        
        // Determine with mime type to use for e-mail
        StringBuilder msg = new StringBuilder();
        StringBuilder ownermsg = new StringBuilder();
        boolean isPlainText = !propertiesManager.getBooleanProperty("users.comments.htmlenabled");
        
        // first the common stub message for the owner and commenters (if applicable)
        if (!isPlainText) {
            msg.append("<html><body style=\"background: white; ");
            msg.append(" color: black; font-size: 12px\">");
        }
        
        if (!StringUtils.isEmpty(commentObject.getName())) {
            msg.append(commentObject.getName() + " "
                    + resources.getString("email.comment.wrote")+": ");
        } else {
            msg.append(resources.getString("email.comment.anonymous")+": ");
        }
        
        msg.append((isPlainText) ? "\n\n" : "<br /><br />");

        // Don't escape the content if email will be sent as plain text
        msg.append((isPlainText) ? commentObject.getContent()
        : Utilities.transformToHTMLSubset(StringEscapeUtils.escapeHtml4(commentObject.getContent())));
        
        msg.append((isPlainText) ? "\n\n----\n"
                : "<br /><br /><hr /><span style=\"font-size: 11px\">");
        msg.append(resources.getString("email.comment.respond") + ": ");
        msg.append((isPlainText) ? "\n" : "<br />");
        
        // Build link back to comment
        String commentURL = urlStrategy.getWeblogCommentsURL(weblog, entry.getAnchor(), true);
        
        if (isPlainText) {
            msg.append(commentURL);
        } else {
            msg.append("<a href=\""+commentURL+"\">"+commentURL+"</a></span>");
        }
        
        // next the additional information that is sent to the blog owner
        // Owner gets an email if it's pending and/or he's turned on notifications
        if (commentObject.getPending() || weblog.getEmailComments()) {
            // First, list any messages from the system that were passed in:
            if (messages.getMessageCount() > 0) {
                ownermsg.append((isPlainText) ? "" : "<p>");
                ownermsg.append(resources.getString("commentServlet.email.thereAreSystemMessages"));
                ownermsg.append((isPlainText) ? "\n\n" : "</p>");
                ownermsg.append((isPlainText) ? "" : "<ul>");
            }
            for (Iterator it = messages.getMessages(); it.hasNext();) {
                RollerMessage rollerMessage = (RollerMessage)it.next();
                ownermsg.append((isPlainText) ? "" : "<li>");
                ownermsg.append(MessageFormat.format(resources.getString(
                    rollerMessage.getKey()), (Object[])rollerMessage.getArgs()) );
                ownermsg.append((isPlainText) ? "\n\n" : "</li>");
            }
            if (messages.getMessageCount() > 0) {
                ownermsg.append((isPlainText) ? "\n\n" : "</ul>");
            }

            // Next, list any validation error messages that were passed in:
            if (messages.getErrorCount() > 0) {
                ownermsg.append((isPlainText) ? "" : "<p>");
                ownermsg.append(resources.getString("commentServlet.email.thereAreErrorMessages"));
                ownermsg.append((isPlainText) ? "\n\n" : "</p>");
                ownermsg.append((isPlainText) ? "" : "<ul>");
            }
            for (Iterator it = messages.getErrors(); it.hasNext();) {
                RollerMessage rollerMessage = (RollerMessage)it.next();
                ownermsg.append((isPlainText) ? "" : "<li>");
                ownermsg.append(MessageFormat.format(resources.getString(
                    rollerMessage.getKey()), (Object[])rollerMessage.getArgs()) );
                ownermsg.append((isPlainText) ? "\n\n" : "</li>");
            }
            if (messages.getErrorCount() > 0) {
                ownermsg.append((isPlainText) ? "\n\n" : "</ul>");
            }

            ownermsg.append(msg);

            ownermsg.append((isPlainText) ? "\n\n----\n" :
                "<br /><br /><hr /><span style=\"font-size: 11px\">");

            // commenter email address: allow blog owner to reply via email instead of blog comment
            if (!StringUtils.isBlank(commentObject.getEmail())) {
                ownermsg.append(resources.getString("email.comment.commenter.email") + ": " + commentObject.getEmail());
                ownermsg.append((isPlainText) ? "\n\n" : "<br/><br/>");
            }
            // add link to weblog edit page so user can login to manage comments
            ownermsg.append(resources.getString("email.comment.management.link") + ": ");
            ownermsg.append((isPlainText) ? "\n" : "<br/>");

            Map<String, String> parameters = new HashMap<>();
            parameters.put("bean.entryId", entry.getId());
            String deleteURL = urlStrategy.getActionURL(
                    "comments", "/tb-ui/authoring", weblog.getHandle(), parameters, true);

            if (isPlainText) {
                ownermsg.append(deleteURL);
            } else {
                ownermsg.append(
                        "<a href=\"" + deleteURL + "\">" + deleteURL + "</a></span>");
                msg.append("</Body></html>");
                ownermsg.append("</Body></html>");
            }
        }

        // determine email subject
        String subject;
        if (commentObject.getPending()) {
            subject = resources.getString("email.comment.moderate.title") + ": ";
        } else {
            if ((subscribers.size() > 1) ||
                    (StringUtils.equals(commentObject.getEmail(), user.getEmailAddress()))) {
                subject= "RE: "+resources.getString("email.comment.title")+": ";
            } else {
                subject = resources.getString("email.comment.title") + ": ";
            }
        }
        subject += entry.getTitle();
        
        // send message to email recipients
        try {
            String from = user.getEmailAddress();

            boolean isHtml = !isPlainText;
            
            if (commentObject.getPending() || weblog.getEmailComments()) {
                if(isHtml) {
                    sendHTMLMessage(
                            from,
                            new String[]{user.getEmailAddress()},
                            null,
                            null,
                            subject,
                            ownermsg.toString());
                } else {
                    sendTextMessage(
                            from,
                            new String[]{user.getEmailAddress()},
                            null,
                            null,
                            subject,
                            ownermsg.toString());
                }
            }

            // now send to subscribers
            if (notifySubscribers && subscribers.size() > 0) {
                // Form array of commenter addrs
                String[] commenterAddrs = subscribers.toArray(new String[subscribers.size()]);

                if (isHtml) {
                    sendHTMLMessage(
                            from, 
                            null,
                            null,
                            commenterAddrs,
                            subject, 
                            msg.toString());
                } else {
                    sendTextMessage(
                            from, 
                            null,
                            null,
                            commenterAddrs,
                            subject, 
                            msg.toString());
                }
            }
        } catch (Exception e) {
            log.warn("Exception sending comment notification mail", e);
            log.debug("", e);
        }
        
        log.debug("Done sending email message");
    }
    

    public void sendEmailApprovalNotifications(List<WeblogEntryComment> comments, I18nMessages resources) {
        
        RollerMessages messages = new RollerMessages();
        for (WeblogEntryComment comment : comments) {

            // Send email notifications because a new comment has been approved
            sendEmailNotification(comment, messages, resources, true);

            // Send approval notification to author of approved comment
            sendEmailApprovalNotification(comment, resources);
        }
    }
    
    
    /**
     * Send message to author of approved comment
     */
    public void sendEmailApprovalNotification(WeblogEntryComment cd, I18nMessages resources) {
        
        WeblogEntry entry = cd.getWeblogEntry();
        Weblog weblog = entry.getWeblog();
        SafeUser user = entry.getCreator();

        String from = user.getEmailAddress();

        // form the message to be sent
        String subject = resources.getString("email.comment.commentApproved");
        
        StringBuilder msg = new StringBuilder();
        msg.append(resources.getString("email.comment.commentApproved"));
        msg.append("\n\n");
        msg.append(urlStrategy.getWeblogCommentsURL(weblog, entry.getAnchor(), true));
        
        // send message to author of approved comment
        try {
            sendTextMessage(from, new String[] {cd.getEmail()}, null, null, subject, msg.toString());
        } catch (Exception e) {
            log.warn("Exception sending comment mail: {}", e.getMessage());
            log.debug("", e);
        }
        log.debug("Done sending email message");
    }
    
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
    public void sendMessage(String from, String[] to, String[] cc, String[] bcc, String subject,
            String content, String mimeType) throws MessagingException {
        
        MailProvider mailProvider = WebloggerFactory.getMailProvider();
        if (mailProvider == null) {
            return;
        }
        
        Session session = mailProvider.getSession();
        MimeMessage message = new MimeMessage(session);
        
        // n.b. any default from address is expected to be determined by caller.
        if (! StringUtils.isEmpty(from)) {
            InternetAddress sentFrom = new InternetAddress(from);
            message.setFrom(sentFrom);
            log.debug("e-mail from: {}", sentFrom);
        }
        
        if (to!=null) {
            InternetAddress[] sendTo = new InternetAddress[to.length];
            
            for (int i = 0; i < to.length; i++) {
                sendTo[i] = new InternetAddress(to[i]);
                log.debug("sending e-mail to: {}", to[i]);
            }
            message.setRecipients(Message.RecipientType.TO, sendTo);
        }
        
        if (cc != null) {
            InternetAddress[] copyTo = new InternetAddress[cc.length];
            
            for (int i = 0; i < cc.length; i++) {
                copyTo[i] = new InternetAddress(cc[i]);
                log.debug("copying e-mail to: {}", cc[i]);
            }
            message.setRecipients(Message.RecipientType.CC, copyTo);
        }
        
        if (bcc != null) {
            InternetAddress[] copyTo = new InternetAddress[bcc.length];
            
            for (int i = 0; i < bcc.length; i++) {
                copyTo[i] = new InternetAddress(bcc[i]);
                log.debug("blind copying e-mail to: {}", bcc[i]);
            }
            message.setRecipients(Message.RecipientType.BCC, copyTo);
        }
        message.setSubject((subject == null) ? "(no subject)" : subject, "UTF-8");
        message.setContent(content, mimeType);
        message.setSentDate(new java.util.Date());
        
        // First collect all the addresses together.
        Address[] remainingAddresses = message.getAllRecipients();
        int nAddresses;
        boolean bFailedToSome = false;
        
        SendFailedException sendex = new SendFailedException("Unable to send message to some recipients");
        
        Transport transport = mailProvider.getTransport();
        
        // Try to send while there remain some potentially good addresses
        try { 
            do {
                // Avoid a loop if we are stuck
                nAddresses = remainingAddresses.length;

                try {
                    // Send to the list of remaining addresses, ignoring the addresses attached to the message
                    transport.sendMessage(message, remainingAddresses);
                } catch(SendFailedException ex) {
                    bFailedToSome=true;
                    sendex.setNextException(ex);

                    // Extract the remaining potentially good addresses
                    remainingAddresses=ex.getValidUnsentAddresses();
                }
            } while (remainingAddresses!=null && remainingAddresses.length>0 
                    && remainingAddresses.length!=nAddresses);
            
        } finally {
            transport.close();
        }
        
        if (bFailedToSome) {
            throw sendex;
        }
    }
    
    
    /**
     * This method is used to send a Text Message.
     *
     * @param from e-mail address of sender
     * @param to e-mail addresses of recipients
     * @param cc e-mail address of cc recipients
     * @param bcc e-mail address of bcc recipients
     * @param subject subject of e-mail
     * @param content the body of the e-mail
     * @throws MessagingException the exception to indicate failure
     */
    public void sendTextMessage(String from, String[] to, String[] cc, String[] bcc,
                                       String subject, String content) throws MessagingException {
        sendMessage(from, to, cc, bcc, subject, content, "text/plain; charset=utf-8");
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
    public void sendHTMLMessage(String from, String[] to, String[] cc, String[] bcc, String subject,
                                       String content) throws MessagingException {
        sendMessage(from, to, cc, bcc, subject, content, "text/html; charset=utf-8");
    }

}
