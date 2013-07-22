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
package org.apache.roller.weblogger.webservices.adminprotocol.sdk;
/*
 * WeblogEntry.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

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
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.Entry.Attributes;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.Entry.Types;

/**
 * This class describes a weblog entry.
 */
public class WeblogEntry extends Entry {
    interface Tags {
        String WEBLOG = "weblog";
        String HANDLE = "handle";
        String NAME = "name";
        String DESCRIPTION = "description";
        String LOCALE = "locale";
        String TIMEZONE = "timezone";
        String DATE_CREATED = "date-created";
        String CREATING_USER = "creating-user";
        String EMAIL_ADDRESS = "email-address";
        String APP_ENTRIES_URL = "app-entries-url";
        String APP_RESOURCES_URL = "app-resources-url";
        String ENABLED = "enabled";
    }
    
    private String handle;
    private String name;
    private String description;
    private Locale locale;
    private TimeZone timezone;
    private Date dateCreated;
    private String creatingUser;
    private String emailAddress;
    private String appEntriesUrl;
    private String appResourcesUrl;
    private Boolean enabled;
    
    public WeblogEntry(Element e, String urlPrefix) {
        populate(e, urlPrefix);
    }
    
    public WeblogEntry(InputStream stream, String urlPrefix) throws JDOMException, IOException {
        SAXBuilder sb = new SAXBuilder();
        Document d = sb.build(stream);
        Element e = d.detachRootElement();
        
        populate(e, urlPrefix);
    }
    
    private void populate(Element e, String urlPrefix) {
        // handle (required)
        Element handleElement = e.getChild(Tags.HANDLE, Service.NAMESPACE);
        if (handleElement != null) {
            setHandle(handleElement.getText());
        }
        
        // href
        String href = urlPrefix + "/" + EntrySet.Types.WEBLOGS + "/" + getHandle();
        setHref(href);
        
        // name
        Element nameElement = e.getChild(Tags.NAME, Service.NAMESPACE);
        if (nameElement != null) {
            setName(nameElement.getText());
        }
        
        // description
        Element descElement = e.getChild(Tags.DESCRIPTION, Service.NAMESPACE);
        if (descElement != null) {
            setDescription(descElement.getText());
        }
        
        // locale
        Element localeElement = e.getChild(Tags.LOCALE, Service.NAMESPACE);
        if (localeElement != null) {
            setLocale(localeElement.getText());
        }
        
        // timezone
        Element tzElement = e.getChild(Tags.TIMEZONE, Service.NAMESPACE);
        if (tzElement != null) {
            setTimezone(tzElement.getText());
        }
        
        // creator
        Element creatorElement = e.getChild(Tags.CREATING_USER, Service.NAMESPACE);
        if (creatorElement != null) {
            setCreatingUser(creatorElement.getText());
        }
        
        // email address
        Element emailElement = e.getChild(Tags.EMAIL_ADDRESS, Service.NAMESPACE);
        if (emailElement != null) {
            setEmailAddress(emailElement.getText());
        }
        
        // created
        Element createdElement = e.getChild(Tags.DATE_CREATED, Service.NAMESPACE);
        if (createdElement != null) {
            setDateCreated(new Date(Long.valueOf(createdElement.getText()).longValue()));
        }
        
        // APP entries URL
        Element appEntriesUrlElement = e.getChild(Tags.APP_ENTRIES_URL, Service.NAMESPACE);
        if (appEntriesUrlElement != null) {
            setAppEntriesUrl(appEntriesUrlElement.getText());
        }
        
        // APP resources URL
        Element appResourcesUrlElement = e.getChild(Tags.APP_RESOURCES_URL, Service.NAMESPACE);
        if (appResourcesUrlElement != null) {
            setAppResourcesUrl(appResourcesUrlElement.getText());
        }
        
        // enabled
        Element enabledElement = e.getChild(Tags.ENABLED, Service.NAMESPACE);
        if (enabledElement != null) {
            setEnabled(Boolean.valueOf(enabledElement.getText()));
        }
    }
    
    
    public WeblogEntry(String handle, String urlPrefix) {
        String href = urlPrefix + "/" + EntrySet.Types.WEBLOGS + "/" + handle;
        setHref(href);
        setHandle(handle);
    }
    
    public String getType() {
        return Types.WEBLOG;
    }
    
    public Document toDocument() {
        Element weblog = new Element(Tags.WEBLOG, Service.NAMESPACE);
        Document doc = new Document(weblog);
        
        // link
        weblog.setAttribute(Attributes.HREF, getHref());
        
        // handle (required)
        String handle = getHandle();
        if (handle != null && handle.length() > 0) {
            Element handleElement = new Element(Tags.HANDLE, Service.NAMESPACE);
            Text handleText = new Text(handle);
            handleElement.addContent(handleText);
            weblog.addContent(handleElement);
        }
        
        // name
        String name = getName();
        if (name != null) {
            Element nameElement = new Element(Tags.NAME, Service.NAMESPACE);
            Text nameText = new Text(name);
            nameElement.addContent(nameText);
            weblog.addContent(nameElement);
        }
        
        // description
        String desc = getDescription();
        if (desc != null) {
            Element descElement = new Element(Tags.DESCRIPTION, Service.NAMESPACE);
            Text descText = new Text(desc);
            descElement.addContent(descText);
            weblog.addContent(descElement);
        }
        
        // locale
        Locale locale = getLocale();
        if (locale != null) {
            Element localeElement = new Element(Tags.LOCALE, Service.NAMESPACE);
            Text localeText = new Text(locale.toString());
            localeElement.addContent(localeText);
            weblog.addContent(localeElement);
        }
        
        // timezone
        TimeZone tz = getTimezone();
        if (tz != null) {
            Element tzElement = new Element(Tags.TIMEZONE, Service.NAMESPACE);
            Text tzText = new Text(tz.getID());
            tzElement.addContent(tzText);
            weblog.addContent(tzElement);
        }
        
        // creating user
        String creator = getCreatingUser();
        if (creator != null) {
            Element creatorElement = new Element(Tags.CREATING_USER, Service.NAMESPACE);
            Text creatorText = new Text(creator);
            creatorElement.addContent(creatorText);
            weblog.addContent(creatorElement);
        }
        
        // email address
        String email = getEmailAddress();
        if (email != null) {
            Element emailElement = new Element(Tags.EMAIL_ADDRESS, Service.NAMESPACE);
            Text emailText = new Text(email);
            emailElement.addContent(emailText);
            weblog.addContent(emailElement);
        }
        
        // creation date
        Element dateCreatedElement = new Element(Tags.DATE_CREATED, Service.NAMESPACE);
        Date datedCreated = getDateCreated();
        if (dateCreated != null) {
            Text createdText = new Text(String.valueOf(dateCreated.getTime()));
            dateCreatedElement.addContent(createdText);
            weblog.addContent(dateCreatedElement);
        }
        
        // APP entries URL
        Element appEntriesUrlElement = new Element(Tags.APP_ENTRIES_URL, Service.NAMESPACE);
        String appEntriesUrl = getAppEntriesUrl();
        if (appEntriesUrl != null) {
            Text appEntriesUrlText = new Text(appEntriesUrl);
            appEntriesUrlElement.addContent(appEntriesUrlText);
            weblog.addContent(appEntriesUrlElement);
        }
        
        // APP entries URL
        Element appResourcesUrlElement = new Element(Tags.APP_RESOURCES_URL, Service.NAMESPACE);
        String appResourcesUrl = getAppResourcesUrl();
        if (appResourcesUrl != null) {
            Text appResourcesUrlText = new Text(appResourcesUrl);
            appResourcesUrlElement.addContent(appResourcesUrlText);
            weblog.addContent(appResourcesUrlElement);
        }
        
        // enabled
        Element enabledElement = new Element(Tags.ENABLED, Service.NAMESPACE);
        Boolean enabled = getEnabled();
        if (enabled != null) {
            Text enabledText = new Text(getEnabled().toString());
            enabledElement.addContent(enabledText);
            weblog.addContent(enabledElement);
        }
        
        return doc;
    }
    
    /** Test if a user entry is equal to this user entry. */
    public boolean equals(Object o) {
        if ( o == null || o.getClass() != this.getClass()) {
            return false;
        }
        
        WeblogEntry other = (WeblogEntry)o;
        
        if (!areEqual(getEmailAddress(), other.getEmailAddress())) {
            return false;
        }
        if (!areEqual(getHandle(), other.getHandle())) {
            return false;
        }
        if (!areEqual(getLocale(), other.getLocale())) {
            return false;
        }
        if (!areEqual(getName(), other.getName())) {
            return false;
        }
        if (!areEqual(getDescription(), other.getDescription())) {
            return false;
        }
        if (!areEqual(getTimezone(), other.getTimezone())) {
            return false;
        }
        if (!areEqual(getEnabled(), other.getEnabled())) {
            return false;
        }
        
        return super.equals(o);
    }
    
    public String getHandle() {
        return handle;
    }
    
    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Locale getLocale() {
        return locale;
    }
    
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    public void setLocale(String localeString) {
        this.locale = new LocaleString(localeString).getLocale();
    }
    
    
    public TimeZone getTimezone() {
        return timezone;
    }
    
    public void setTimezone(TimeZone timezone) {
        this.timezone = timezone;
    }
    
    public void setTimezone(String timezoneString) {
        this.timezone = TimeZone.getTimeZone(timezoneString);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Date getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public String getCreatingUser() {
        return creatingUser;
    }
    
    public void setCreatingUser(String creatingUser) {
        this.creatingUser = creatingUser;
    }
    
    public String getEmailAddress() {
        return emailAddress;
    }
    
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    public String getAppEntriesUrl() {
        return appEntriesUrl;
    }
    
    public void setAppEntriesUrl(String appEntriesUrl) {
        this.appEntriesUrl = appEntriesUrl;
    }
    
    public String getAppResourcesUrl() {
        return appResourcesUrl;
    }
    
    public void setAppResourcesUrl(String appResourcesUrl) {
        this.appResourcesUrl = appResourcesUrl;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
