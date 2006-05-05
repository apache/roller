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
/*
 * UserEntry.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

package org.apache.roller.webservices.adminapi.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;
import org.apache.roller.webservices.adminapi.sdk.Entry.Attributes;
import org.apache.roller.webservices.adminapi.sdk.Entry.Types;

/**
 * This class describes a user entry; a user weblog resource.
 * @author jtb
 */
public class UserEntry extends Entry {
    /** XML tags that define a user entry. */
    static interface Tags {
        public static final String USER = "user";
        public static final String NAME = "name";
        public static final String FULL_NAME = "full-name";
        public static final String PASSWORD = "password";
        public static final String EMAIL_ADDRESS = "email-address";
        public static final String LOCALE = "locale";
        public static final String TIMEZONE = "timezone";
        public static final String DATE_CREATED = "date-created";
    }
    
    private String name;
    private String fullName;
    private String password;
    private Locale locale;
    private TimeZone timezone;
    private Date dateCreated;
    private String emailAddress;
    
    /** Construct an empty user entry */
    public UserEntry(String name, String urlPrefix) {
        setName(name);
        String href = urlPrefix + "/" + EntrySet.Types.USERS + "/" + name;                
        setHref(href);
    }
    
    /** Construct a user entry from a JDOM element. */
    public UserEntry(Element e, String urlPrefix) throws MissingElementException {
        populate(e, urlPrefix);
    }
    
    public UserEntry(InputStream stream, String urlPrefix) throws JDOMException, IOException, MissingElementException {               
        SAXBuilder sb = new SAXBuilder();
        Document d = sb.build(stream);
        Element e = d.detachRootElement();
        
        populate(e, urlPrefix);        
    }
    
    private void populate(Element e, String urlPrefix) throws MissingElementException {
        // name
        Element nameElement = e.getChild(Tags.NAME, NAMESPACE);
        if (nameElement == null) {
            throw new MissingElementException("ERROR: Missing element", e.getName(), Tags.NAME);
        }
        setName(nameElement.getText());
        
        // href
        String href = urlPrefix + "/" + EntrySet.Types.USERS + "/" + getName();
        setHref(href);
        
        // full name
        Element fullNameElement = e.getChild(Tags.FULL_NAME, NAMESPACE);
        if (fullNameElement == null) {
            throw new MissingElementException("ERROR: Missing element", e.getName(), Tags.FULL_NAME);
        }
        setFullName(fullNameElement.getText());
        
        // password
        // this is optional
        Element passwordElement = e.getChild(Tags.PASSWORD, NAMESPACE);
        if (passwordElement != null) {
            setPassword(passwordElement.getText());            
        }
        
        // locale
        Element localeElement = e.getChild(Tags.LOCALE, Service.NAMESPACE);
        if (localeElement == null) {
            throw new MissingElementException("ERROR: Missing element", e.getName(), Tags.LOCALE);
        }
        setLocale(localeElement.getText());
        
        // timezone
        Element tzElement = e.getChild(Tags.TIMEZONE, Service.NAMESPACE);
        if (tzElement == null) {
            throw new MissingElementException("ERROR: Missing element", e.getName(), Tags.TIMEZONE);
        }
        setTimezone(tzElement.getText());
        
        // email address
        Element emailElement = e.getChild(Tags.EMAIL_ADDRESS, Service.NAMESPACE);
        if (emailElement == null) {
            throw new MissingElementException("ERROR: Missing element", e.getName(), Tags.EMAIL_ADDRESS);
        }
        setEmailAddress(emailElement.getText());
        
        // created (optional)
        Element createdElement = e.getChild(Tags.DATE_CREATED, Service.NAMESPACE);
        if (createdElement != null) {
            setDateCreated(new Date(Long.valueOf(createdElement.getText()).longValue()));
        }
    }
    
        
    public String getType() {
        return Types.USER;
    }
    
    public Document toDocument() {
        Element userElement = new Element(Tags.USER, NAMESPACE);
        Document doc = new Document(userElement);
        
        // href
        String href = getHref();
        if (href != null) {
            userElement.setAttribute(Attributes.HREF, href);
        }
        
        // name
        String name = getName();
        if (name != null) {
            Element nameElement = new Element(Tags.NAME, Service.NAMESPACE);
            Text nameText = new Text(getName());
            nameElement.addContent(nameText);
            userElement.addContent(nameElement);
        }
        
        // full name
        String fullName = getFullName();
        if (fullName != null) {
            Element fullNameElement = new Element(Tags.FULL_NAME, NAMESPACE);
            Text fullNameText = new Text(getFullName());
            fullNameElement.addContent(fullNameText);
            userElement.addContent(fullNameElement);
        }
        
        // password
        String password = getPassword();
        if (password != null) {
            Element passwordElement = new Element(Tags.PASSWORD, NAMESPACE);
            Text passwordText = new Text(getPassword());
            passwordElement.addContent(passwordText);
            userElement.addContent(passwordElement);
        }
        
        // locale
        Locale locale = getLocale();
        if (locale != null ) {
            Element localeElement = new Element(Tags.LOCALE, Service.NAMESPACE);
            Text localeText = new Text(getLocale().toString());
            localeElement.addContent(localeText);
            userElement.addContent(localeElement);
        }
        
        // timezone
        TimeZone timezone = getTimezone();
        if (timezone != null) {
            Element timezoneElement = new Element(Tags.TIMEZONE, Service.NAMESPACE);
            Text timezoneText = new Text(timezone.getID());
            timezoneElement.addContent(timezoneText);
            userElement.addContent(timezoneElement);
        }
        
        // email address
        String emailAddress = getEmailAddress();
        if (emailAddress != null) {
            Element emailAddressElement = new Element(Tags.EMAIL_ADDRESS, Service.NAMESPACE);
            Text emailAddressText = new Text(String.valueOf(emailAddress));
            emailAddressElement.addContent(emailAddressText);
            userElement.addContent(emailAddressElement);
        }
        
        // creation date (optional)
        Date datedCreated = getDateCreated();
        if (dateCreated != null) {
            Element dateCreatedElement = new Element(Tags.DATE_CREATED, Service.NAMESPACE);
            Text dateCreatedText = new Text(String.valueOf(dateCreated.getTime()));
            dateCreatedElement.addContent(dateCreatedText);
            userElement.addContent(dateCreatedElement);
        }
        
        return doc;
    }
    
    /** Get the user name of this user entry. */
    public String getName() {
        return name;
    }
    
    /** Set of the user name of this user entry. */
    public void setName(String name) {
        this.name = name;
    }
    
    /** Get the full name of this user entry. */
    public String getFullName() {
        return fullName;
    }
    
    /** Set the full name of this user entry. */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    /** Get the password of this user entry. */
    public String getPassword() {
        return password;
    }
    
    /** Set the password of this user entry. */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /** Get the locale string of this user entry. */
    public Locale getLocale() {
        return locale;
    }
    
    /** Set the locale of this user entry. */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    /** Set the locale string of this user entry. */
    public void setLocale(String localeString) {
        this.locale = new LocaleString(localeString).getLocale();
    }    
    
    /** Get the timezone string of this user entry. */
    public TimeZone getTimezone() {
        return timezone;
    }
    
    /** Set the timezone of this user entry. */
    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }

    /** Set the timezone string of this user entry. */
    public void setTimezone(String timezoneString) {
        this.timezone = TimeZone.getTimeZone(timezoneString);
    }
    
    /** Get the date created of this user entry. */
    public Date getDateCreated() {
        return dateCreated;
    }
    
    /** Set the date created of this user entry. */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    /** Get the email address of this user entry. */
    public String getEmailAddress() {
        return emailAddress;
    }
    
    /** Set the email address of this user entry. */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    /** Test if a user entry is equal to this user entry. */
    public boolean equals(Object o) {
        if ( o == null || o.getClass() != this.getClass()) { 
            return false;        
        }
        
        UserEntry other = (UserEntry)o;
        
        if (!areEqual(getEmailAddress(), other.getEmailAddress())) {
            return false;
        }
        if (!areEqual(getFullName(), other.getFullName())) {
            return false;
        }
        if (!areEqual(getLocale(), other.getLocale())) {
            return false;
        }
        if (!areEqual(getName(), other.getName())) {
            return false;
        }
        if (!areEqual(getTimezone(), other.getTimezone())) {
            return false;
        }
        
        return super.equals(o);
    }
    
}
