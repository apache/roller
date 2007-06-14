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

package org.apache.roller.weblogger.business;

import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.RollerConfig;


/**
 * Encapsulates Roller mail configuration, returns mail sessions.
 */
public class MailProvider {
    
    private static final Log log = LogFactory.getLog(MailProvider.class);
    
    private enum ConfigurationType {JNDI_NAME, MAIL_PROPERTIES; }
    
    private static MailProvider singletonInstance = null;
    
    private Session session = null;
    
    private ConfigurationType type = ConfigurationType.JNDI_NAME;
    
    private String jndiName = null;
    private String mailHostname = null;
    private int    mailPort = -1;
    private String mailUsername = null;
    private String mailPassword = null;

        
    /** Creates a new instance of MailProvider */
    public MailProvider() throws WebloggerException {
        String connectionTypeString = RollerConfig.getProperty("mail.configurationType"); 
        if ("properties".equals(connectionTypeString)) {
            type = ConfigurationType.MAIL_PROPERTIES;
        }
        jndiName =     RollerConfig.getProperty("mail.jndi.name");
        mailHostname = RollerConfig.getProperty("mail.hostname");
        mailUsername = RollerConfig.getProperty("mail.username");
        mailPassword = RollerConfig.getProperty("mail.password");
        try {
            mailPort = Integer.parseInt(RollerConfig.getProperty("mail.port"));
        } catch (Throwable t) {
            log.warn("mail server port not a valid integer, ignoring");
        }
        
        // init and connect now so we fail early
        if (type == ConfigurationType.JNDI_NAME) {            
            String name = "java:comp/env/" + jndiName;
            try {
                Context ctx = (Context) new InitialContext();
                session = (Session) ctx.lookup(name);
            } catch (NamingException ex) {
                throw new WebloggerException("ERROR looking up mail-session with JNDI name: " + name);
            }
        } else {
            Properties props = new Properties();
            props.put("mail.smtp.host", mailHostname);
            props.put("mail.smtp.auth", "true");
            if (mailPort != -1) props.put("mail.smtp.port", ""+mailPort);
            session = Session.getDefaultInstance(props, null);
        }
        try {
            Transport transport = getTransport();
            transport.close();
        } catch (Throwable t) {
            throw new WebloggerException("ERROR connecting to mail server", t);
        }
        
    }
    
    public static MailProvider getMailProvider() throws WebloggerException {
        if (singletonInstance == null) {
            singletonInstance = new MailProvider();
        }
        return singletonInstance;
    }   
    
    public static boolean isMailConfigured() {
        return singletonInstance != null;
    }
    
    public Session getSession() {
        return session;
    }
    
    /**
     * Create and connect to transport, caller is responsible for closing transport.
     */
    public Transport getTransport() throws NoSuchProviderException, MessagingException {
        Transport transport = null;
        if (type == ConfigurationType.MAIL_PROPERTIES) {
            // Configure transport ourselves using mail properties
            transport = session.getTransport("smtp"); 
            if (mailUsername != null && mailPassword != null && mailPort != -1) {
                transport.connect(mailHostname, mailPort, mailUsername, mailPassword); 
            } else if (mailUsername != null && mailPassword != null) {
                transport.connect(mailHostname, mailUsername, mailPassword); 
            }
        } else {
            // Assume container set things up properly
            transport = session.getTransport(); 
            transport.connect();
        }
        return transport;
    }
}
