package org.roller.presentation.atomadminapi.sdk;
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
import org.roller.presentation.atomadminapi.sdk.Entry.Attributes;
import org.roller.presentation.atomadminapi.sdk.Entry.Types;

/**
 * This class describes a weblog entry. 
 */
public class WeblogEntry extends Entry {
    static interface Tags {
        public static final String WEBLOG = "weblog";
        public static final String HANDLE = "handle";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String LOCALE = "locale";
        public static final String TIMEZONE = "timezone";
        public static final String DATE_CREATED = "date-created";
        public static final String CREATING_USER = "creating-user";
        public static final String EMAIL_ADDRESS = "email-address";
        public static final String APP_ENTRIES_URL = "app-entries-url";        
        public static final String APP_RESOURCES_URL = "app-resources-url";                
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
    
    public WeblogEntry(Element e, String urlPrefix) throws MissingElementException {
        populate(e, urlPrefix);
    }
    
    public WeblogEntry(InputStream stream, String urlPrefix) throws JDOMException, IOException, MissingElementException {               
        SAXBuilder sb = new SAXBuilder();
        Document d = sb.build(stream);
        Element e = d.detachRootElement();
        
        populate(e, urlPrefix);        
    }
    
    private void populate(Element e, String urlPrefix) throws MissingElementException {
        // handle
        Element handleElement = e.getChild(Tags.HANDLE, Service.NAMESPACE);
        if (handleElement == null) {
            throw new MissingElementException("ERROR: Missing element", e.getName(), Tags.HANDLE);
        }
        setHandle(handleElement.getText());
        
        // href
        String href = urlPrefix + "/" + EntrySet.Types.WEBLOGS + "/" + getHandle();        
        setHref(href);        
        
        // name
        Element nameElement = e.getChild(Tags.NAME, Service.NAMESPACE);
        if (nameElement == null) {
            throw new MissingElementException("ERROR: Missing element", e.getName(), Tags.NAME);
        }
        setName(nameElement.getText());
        
        // description
        Element descElement = e.getChild(Tags.DESCRIPTION, Service.NAMESPACE);
        if (descElement == null) {
            throw new MissingElementException("ERROR: Missing element", e.getName(), Tags.DESCRIPTION);
        }
        setDescription(descElement.getText());
        
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
        
        // creator
        Element creatorElement = e.getChild(Tags.CREATING_USER, Service.NAMESPACE);
        if (creatorElement == null) {
            throw new MissingElementException("ERROR: Missing element", e.getName(), Tags.CREATING_USER);
        }
        setCreatingUser(creatorElement.getText());
        
        // email address
        Element emailElement = e.getChild(Tags.EMAIL_ADDRESS, Service.NAMESPACE);
        if (emailElement == null) {
            throw new MissingElementException("ERROR: Missing element: ", e.getName(), Tags.EMAIL_ADDRESS);
        }
        setEmailAddress(emailElement.getText());        
        
        // created (optional)
        Element createdElement = e.getChild(Tags.DATE_CREATED, Service.NAMESPACE);
        if (createdElement != null) {
            setDateCreated(new Date(Long.valueOf(createdElement.getText()).longValue()));
        }              
        
        // APP entries URL (optional)
        Element appEntriesUrlElement = e.getChild(Tags.APP_ENTRIES_URL, Service.NAMESPACE);
        if (appEntriesUrlElement != null) {
            setAppEntriesUrl(appEntriesUrlElement.getText());
        }                      
        
        // APP resources URL (optional)
        Element appResourcesUrlElement = e.getChild(Tags.APP_RESOURCES_URL, Service.NAMESPACE);
        if (appResourcesUrlElement != null) {
            setAppResourcesUrl(appResourcesUrlElement.getText());
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
        
        // handle
        Element handle = new Element(Tags.HANDLE, Service.NAMESPACE);
        Text handleText = new Text(getHandle());
        handle.addContent(handleText);
        weblog.addContent(handle);
        
        // name
        Element name = new Element(Tags.NAME, Service.NAMESPACE);
        Text nameText = new Text(getName());
        name.addContent(nameText);
        weblog.addContent(name);
        
        // description
        Element desc = new Element(Tags.DESCRIPTION, Service.NAMESPACE);
        Text descText = new Text(getDescription());
        desc.addContent(descText);
        weblog.addContent(desc);
        
        // locale
        Element locale = new Element(Tags.LOCALE, Service.NAMESPACE);
        Text localeText = new Text(getLocale().toString());
        locale.addContent(localeText);
        weblog.addContent(locale);
        
        // timezone
        Element tz = new Element(Tags.TIMEZONE, Service.NAMESPACE);
        Text tzText = new Text(getTimezone().getID());
        tz.addContent(tzText);
        weblog.addContent(tz);
        
        // creating user
        Element creator = new Element(Tags.CREATING_USER, Service.NAMESPACE);
        Text creatorText = new Text(String.valueOf(getCreatingUser()));
        creator.addContent(creatorText);
        weblog.addContent(creator);
        
        // email address
        Element email = new Element(Tags.EMAIL_ADDRESS, Service.NAMESPACE);
        Text emailText = new Text(String.valueOf(getEmailAddress()));
        email.addContent(emailText);
        weblog.addContent(email);        
        
        // creation date (optional)
        Element created = new Element(Tags.DATE_CREATED, Service.NAMESPACE);
        Date datedCreated = getDateCreated();
        if (dateCreated != null) {
            Text createdText = new Text(String.valueOf(dateCreated.getTime()));
            created.addContent(createdText);
            weblog.addContent(created);
        }

        // APP entries URL (optional)
        Element appEntriesUrlElement = new Element(Tags.APP_ENTRIES_URL, Service.NAMESPACE);
        String appEntriesUrl = getAppEntriesUrl();
        if (appEntriesUrl != null) {
            Text appEntriesUrlText = new Text(appEntriesUrl);
            appEntriesUrlElement.addContent(appEntriesUrlText);
            weblog.addContent(appEntriesUrlElement);
        }

        // APP entries URL (optional)
        Element appResourcesUrlElement = new Element(Tags.APP_RESOURCES_URL, Service.NAMESPACE);
        String appResourcesUrl = getAppResourcesUrl();
        if (appResourcesUrl != null) {
            Text appResourcesUrlText = new Text(appResourcesUrl);
            appResourcesUrlElement.addContent(appResourcesUrlText);
            weblog.addContent(appResourcesUrlElement);
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
}
