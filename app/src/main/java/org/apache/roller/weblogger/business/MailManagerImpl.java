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

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserSearchCriteria;
import org.apache.roller.weblogger.pojos.UserStatus;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.pojos.WebloggerProperties;
import org.apache.roller.weblogger.util.I18nMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class MailManagerImpl implements MailManager {

    private static Logger log = LoggerFactory.getLogger(MailManagerImpl.class);

    private UserManager userManager;

    private WeblogManager weblogManager;

    private WeblogEntryManager weblogEntryManager;

    private URLStrategy urlStrategy;

    private JavaMailSender mailSender;

    private SpringTemplateEngine mailTemplateEngine;

    public MailManagerImpl(UserManager umgr, WeblogManager wmgr, WeblogEntryManager wemgr,
                           URLStrategy strategy, JavaMailSender sender,
                           SpringTemplateEngine mailTemplateEngine) {
        userManager = umgr;
        weblogManager = wmgr;
        weblogEntryManager = wemgr;
        urlStrategy = strategy;
        mailSender = sender;
        this.mailTemplateEngine = mailTemplateEngine;
    }

    private boolean isMailEnabled() {
        return WebloggerStaticConfig.getBooleanProperty("mail.enabled");
    }

    @Override
    public void sendPendingEntryNotice(WeblogEntry entry) {

        if (!isMailEnabled()) {
            return;
        }

        try {
            String screenName = entry.getCreator().getScreenName();
            String from = entry.getCreator().getEmailAddress();
            String[] cc = new String[]{from};
            String[] bcc = new String[0];
            String[] to;
            String subject;
            String content;

            // list of enabled website authors and admins
            List<String> reviewers = new ArrayList<>();
            List<User> websiteUsers = weblogManager.getWeblogUsers(entry.getWeblog(), true);

            // build list of reviewers (website users with author permission)
            websiteUsers.stream().forEach(user -> {
                if (userManager.checkWeblogRole(user, entry.getWeblog(), WeblogRole.POST) &&
                        user.getEmailAddress() != null) {
                    reviewers.add(user.getEmailAddress());
                }
            });

            to = reviewers.toArray(new String[reviewers.size()]);

            // Figure URL to entry edit page
            String editURL = urlStrategy.getEntryEditURL(entry.getWeblog().getHandle(), entry.getId(), true);

            ResourceBundle resources = ResourceBundle.getBundle(
                    "ApplicationResources", entry.getWeblog().getLocaleInstance());
            StringBuilder sb = new StringBuilder();
            sb.append(
                    MessageFormat.format(
                            resources.getString("weblogEntry.pendingEntrySubject"),
                            new Object[]{
                                    entry.getWeblog().getName(),
                                    entry.getWeblog().getHandle()
                            }));
            subject = sb.toString();
            sb = new StringBuilder();
            sb.append(
                    MessageFormat.format(
                            resources.getString("weblogEntry.pendingEntryContent"),
                            new Object[]{screenName, editURL})
            );
            content = sb.toString();
            sendTextMessage(from, to, cc, bcc, subject, content);
        } catch (MessagingException e) {
            log.error("ERROR: Problem sending pending entry notification email.");
        }
    }

    @Override
    public void sendRegistrationApprovalRequest(User user) {

        if (!isMailEnabled()) {
            return;
        }

        try {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setStatus(UserStatus.ENABLED);
            criteria.setGlobalRole(GlobalRole.ADMIN);
            List<User> admins = userManager.getUsers(criteria);

            // build list of reviewers (website users with author permission)
            List<String> adminEmails = admins.stream().map(User::getEmailAddress).collect(Collectors.toList());
            String[] to = adminEmails.toArray(new String[adminEmails.size()]);

            String userAdminURL = urlStrategy.getActionURL("userAdmin", "/tb-ui/app/admin", null, null, true);

            ResourceBundle resources = ResourceBundle.getBundle("ApplicationResources");
            StringBuilder sb = new StringBuilder();
            sb.append(
                    MessageFormat.format(
                            resources.getString("mailMessage.approveRegistrationSubject"),
                            new Object[]{
                                    user.getScreenName()
                            }));
            String subject = sb.toString();
            sb = new StringBuilder();
            sb.append(
                    MessageFormat.format(
                            resources.getString("mailMessage.approveRegistrationContent"),
                            new Object[]{
                                    user.getScreenName(),
                                    user.getEmailAddress(),
                                    userAdminURL
                            })
            );
            String content = sb.toString();
            sendTextMessage(null, to, new String[0], new String[0], subject, content);

        } catch (MessagingException e) {
            log.error("ERROR: Problem sending pending entry notification email.");
        }
    }

    @Override
    public void sendRegistrationApprovedNotice(User user) {

        if (!isMailEnabled()) {
            return;
        }

        try {
            String[] to = new String[]{user.getEmailAddress()};

            String loginURL = urlStrategy.getLoginURL(true);

            ResourceBundle resources = ResourceBundle.getBundle("ApplicationResources",
                    Locale.forLanguageTag(user.getLocale()));
            StringBuilder sb = new StringBuilder();
            sb.append(
                    MessageFormat.format(
                            resources.getString("mailMessage.registrationApprovedSubject"),
                            new Object[]{
                                    user.getScreenName()
                            }));
            String subject = sb.toString();
            sb = new StringBuilder();
            sb.append(
                    MessageFormat.format(
                            resources.getString("mailMessage.registrationApprovedContent"),
                            new Object[]{
                                    user.getScreenName(),
                                    user.getUserName(),
                                    loginURL
                            })
            );
            String content = sb.toString();
            sendTextMessage(null, to, new String[0], new String[0], subject, content);

        } catch (MessagingException e) {
            log.error("ERROR: Problem sending pending entry notification email.");
        }
    }

    @Override
    public void sendRegistrationRejectedNotice(User user) {

        if (!isMailEnabled()) {
            return;
        }

        try {
            String[] to = new String[]{user.getEmailAddress()};

            ResourceBundle resources = ResourceBundle.getBundle("ApplicationResources");
            StringBuilder sb = new StringBuilder();
            sb.append(
                    MessageFormat.format(
                            resources.getString("mailMessage.registrationRejectedSubject"),
                            new Object[]{
                            }));
            String subject = sb.toString();
            sb = new StringBuilder();
            sb.append(
                    MessageFormat.format(
                            resources.getString("mailMessage.registrationRejectedContent"),
                            new Object[]{
                                    user.getScreenName()
                            })
            );
            String content = sb.toString();
            sendTextMessage(null, to, new String[0], new String[0], subject, content);

        } catch (MessagingException e) {
            log.error("ERROR: Problem sending pending entry notification email.");
        }
    }

    @Override
    public void sendWeblogInvitation(User user, Weblog weblog) {

        if (!isMailEnabled()) {
            return;
        }

        String from = weblog.getCreator().getEmailAddress();
        String[] cc = new String[]{from};
        String[] bcc = new String[0];
        String[] to = new String[]{user.getEmailAddress()};
        String subject;
        String content;

        // Figure URL to entry edit page
        String rootURL = WebloggerStaticConfig.getAbsoluteContextURL();
        String url = rootURL + "/tb-ui/app/home";

        ResourceBundle resources = ResourceBundle.getBundle(
                "ApplicationResources",
                weblog.getLocaleInstance());
        StringBuilder sb = new StringBuilder();
        sb.append(MessageFormat.format(
                resources.getString("members.inviteMemberEmailSubject"),
                new Object[]{
                        weblog.getName(),
                        weblog.getHandle()})
        );
        subject = sb.toString();
        sb = new StringBuilder();
        sb.append(MessageFormat.format(
                resources.getString("members.inviteMemberEmailContent"),
                new Object[]{
                        weblog.getName(),
                        weblog.getHandle(),
                        user.getUserName(),
                        url
                }));
        content = sb.toString();
        try {
            sendTextMessage(from, to, cc, bcc, subject, content);
        } catch (MessagingException ignored) {
        }
    }

    @Override
    public void sendUserActivationEmail(User user) throws MessagingException {

        if (!isMailEnabled()) {
            return;
        }

        ResourceBundle resources = ResourceBundle.getBundle(
                "ApplicationResources", Locale.forLanguageTag(user.getLocale()));

        String[] cc = new String[0];
        String[] bcc = new String[0];
        String[] to = new String[]{user.getEmailAddress()};
        String subject = resources.getString(
                "user.account.activation.mail.subject");
        String content;

        String rootURL = WebloggerStaticConfig.getAbsoluteContextURL();

        StringBuilder sb = new StringBuilder();

        // activationURL=
        String activationURL = rootURL + "/tb-ui/app/login?activationCode=" +
                user.getActivationCode();
        sb.append(MessageFormat.format(
                resources.getString("user.account.activation.mail.content"),
                new Object[]{user.getScreenName(), user.getUserName(), activationURL}));
        content = sb.toString();

        sendHTMLMessage("", to, cc, bcc, subject, content);
    }

    @Override
    public void sendPendingCommentNotice(WeblogEntryComment comment, Map<String, List<String>> messages) {
        if (!isMailEnabled() || comment.getApproved()) {
            return;
        }

        WeblogEntry entry = comment.getWeblogEntry();
        Weblog weblog = entry.getWeblog();
        I18nMessages resources = I18nMessages.getMessages(weblog.getLocaleInstance());
        User user = entry.getCreator();

        Context ctx = new Context(weblog.getLocaleInstance());
        ctx.setVariable("comment", comment);
        String commentURL = urlStrategy.getWeblogCommentsURL(weblog, entry.getAnchor(), true);
        ctx.setVariable("commentURL", commentURL);
        ctx.setVariable("messages", messages);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("bean.entryId", entry.getId());
        String manageURL = urlStrategy.getActionURL("comments", "/tb-ui/app/authoring", weblog, parameters, true);
        ctx.setVariable("manageURL", manageURL);

        String msg = mailTemplateEngine.process("PendingCommentNotice.html", ctx);

        // determine email subject
        String subject = resources.getString("email.comment.moderate.title") + ": ";
        subject += entry.getTitle();

        // send message to email recipients
        try {
            List<UserWeblogRole> bloggerList = userManager.getWeblogRoles(weblog);

            String[] bloggerEmailAddrs = bloggerList.stream()
                    .map(UserWeblogRole::getUser)
                    .map(User::getEmailAddress)
                    .collect(Collectors.toList()).toArray(new String[bloggerList.size()]);

            String from = user.getEmailAddress();

            if (comment.getPending() || weblog.getEmailComments()) {
                sendHTMLMessage(from, bloggerEmailAddrs, null, null, subject, msg);
            }
        } catch (Exception e) {
            log.warn("Exception sending pending comment mail", e);
            log.debug("", e);
        }

        log.debug("Done sending pending comment mail");
    }

    @Override
    public void sendNewCommentNotification(WeblogEntryComment comment) {
        if (!isMailEnabled() ||
                !WebloggerContext.getWebloggerProperties().isUsersCommentNotifications() ||
                !comment.getApproved()) {
            return;
        }

        WeblogEntry entry = comment.getWeblogEntry();
        Weblog weblog = entry.getWeblog();
        I18nMessages resources = I18nMessages.getMessages(weblog.getLocaleInstance());
        User user = entry.getCreator();

        // build list of email addresses to send notification to
        Set<String> subscribers = new TreeSet<>();

        // Get all the subscribers to this comment thread
        List<WeblogEntryComment> priorComments = weblogEntryManager.getComments(
                CommentSearchCriteria.approvedComments(entry, false))
                .stream()
                // don't send a routing email to the person who made the comment.
                .filter(pc -> !comment.getEmail().equals(pc.getEmail()))
                .collect(Collectors.toList());

        for (WeblogEntryComment priorComment : priorComments) {
            // if user has commented twice, count the most recent notify setting
            if (priorComment.getNotify()) {
                log.info("Add to subscribers list: {}", priorComment.getEmail());
                subscribers.add(priorComment.getEmail());
            } else {
                // remove user who doesn't want to be notified
                log.info("Remove from subscribers list: {}", priorComment.getEmail());
                subscribers.remove(priorComment.getEmail());
            }
        }

        Context ctx = new Context(weblog.getLocaleInstance());
        ctx.setVariable("comment", comment);
        String commentURL = urlStrategy.getWeblogCommentsURL(weblog, entry.getAnchor(), true);
        ctx.setVariable("commentURL", commentURL);

        String msg = mailTemplateEngine.process("NewCommentNotification.html", ctx);

        String subject = resources.getString("email.comment.title") + ": " + entry.getTitle();

        // send message to email recipients
        try {
            String from = user.getEmailAddress();

            if (weblog.getEmailComments() &&
                    // if must moderate on, blogger(s) already got pending email, good enough.
                    !WebloggerProperties.CommentPolicy.MUSTMODERATE.equals(weblog.getAllowComments())) {
                List<UserWeblogRole> bloggerList = userManager.getWeblogRoles(weblog);

                String[] bloggerEmailAddrs = bloggerList.stream()
                        .map(UserWeblogRole::getUser)
                        .map(User::getEmailAddress)
                        .collect(Collectors.toList()).toArray(new String[bloggerList.size()]);

                sendHTMLMessage(from, bloggerEmailAddrs, null, null, subject, msg);
            }

            // now send to subscribers
            if (subscribers.size() > 0) {
                // Form array of commenter addrs
                String[] commenterAddrs = subscribers.toArray(new String[subscribers.size()]);

                sendHTMLMessage(from, null, null, commenterAddrs, subject, msg);
            }
        } catch (Exception e) {
            log.warn("Exception sending comment notification mail", e);
            log.debug("", e);
        }

        log.debug("Done sending email message");
    }

    @Override
    public void sendYourCommentWasApprovedNotifications(List<WeblogEntryComment> comments) {

        if (!isMailEnabled() || comments == null || comments.size() < 1) {
            return;
        }

        I18nMessages resources = I18nMessages.getMessages(
                comments.get(0).getWeblogEntry().getWeblog().getLocaleInstance());

        for (WeblogEntryComment comment : comments) {

            // Send email notifications because a new comment has been approved
            sendNewCommentNotification(comment);

            // Send approval notification to author of approved comment
            sendYourCommentWasApprovedNotification(comment, resources);
        }
    }

    private void sendYourCommentWasApprovedNotification(WeblogEntryComment cd, I18nMessages resources) {

        WeblogEntry entry = cd.getWeblogEntry();
        Weblog weblog = entry.getWeblog();
        User user = entry.getCreator();

        String from = user.getEmailAddress();

        // form the message to be sent
        String subject = resources.getString("email.comment.commentApproved");

        StringBuilder msg = new StringBuilder();
        msg.append(resources.getString("email.comment.commentApproved"));
        msg.append("\n\n");
        msg.append(urlStrategy.getWeblogCommentsURL(weblog, entry.getAnchor(), true));

        // send message to author of approved comment
        try {
            sendTextMessage(from, new String[]{cd.getEmail()}, null, null, subject, msg.toString());
        } catch (Exception e) {
            log.warn("Exception sending comment mail: {}", e.getMessage());
            log.debug("", e);
        }
        log.debug("Done sending email message");
    }

    /**
     * This method is used to send a Text Message.
     *
     * @param from    e-mail address of sender
     * @param to      e-mail addresses of recipients
     * @param cc      e-mail address of cc recipients
     * @param bcc     e-mail address of bcc recipients
     * @param subject subject of e-mail
     * @param content the body of the e-mail
     * @throws MessagingException the exception to indicate failure
     */
    private void sendTextMessage(String from, String[] to, String[] cc, String[] bcc,
                                 String subject, String content) throws MessagingException {
        sendMessage(from, to, cc, bcc, subject, content, "text/plain; charset=utf-8");
    }

    /**
     * This method is used to send a HTML Message
     *
     * @param from    e-mail address of sender
     * @param to      e-mail address(es) of recipients
     * @param subject subject of e-mail
     * @param content the body of the e-mail
     * @throws MessagingException the exception to indicate failure
     */
    private void sendHTMLMessage(String from, String[] to, String[] cc, String[] bcc, String subject,
                                 String content) throws MessagingException {
        sendMessage(from, to, cc, bcc, subject, content, "text/html; charset=utf-8");
    }

    private void sendMessage(String from, String[] to, String[] cc, String[] bcc, String subject,
                             String content, String mimeType) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        // n.b. any default from address is expected to be determined by caller.
        if (!StringUtils.isEmpty(from)) {
            InternetAddress sentFrom = new InternetAddress(from);
            message.setFrom(sentFrom);
            log.debug("e-mail from: {}", sentFrom);
        }

        if (to != null) {
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
        boolean bFailedToSome = false;
        SendFailedException sendex = new SendFailedException("Unable to send message to some recipients");

        try {
            // Send to the list of remaining addresses, ignoring the addresses attached to the message
            mailSender.send(message);
        } catch (MailAuthenticationException | MailSendException ex) {
            bFailedToSome = true;
            sendex.setNextException(ex);
        }

        if (bFailedToSome) {
            throw sendex;
        }
    }

}
