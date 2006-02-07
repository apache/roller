/*
 * WeblogEntry.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

package org.roller.presentation.atomadminapi;

import java.util.Date;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.atomadminapi.Entry.Types;

/**
 *
 * @author jtb
 */
class WeblogEntry extends Entry {
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
    }
    
    private String handle;
    private String name;
    private String description;
    private String locale;
    private String timezone;
    private Date dateCreated;
    private String creatingUser;
    private String emailAddress;
    
    public WeblogEntry(Element e, String urlPrefix) throws Exception {
        // handle
        Element handleElement = e.getChild(Tags.HANDLE, AtomAdminService.NAMESPACE);
        if (handleElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.HANDLE);
        }
        setHandle(handleElement.getText());
        String href = urlPrefix + "/" + EntrySet.Types.WEBLOGS + "/" + getHandle();        
        setHref(href);        
        
        // name
        Element nameElement = e.getChild(Tags.NAME, AtomAdminService.NAMESPACE);
        if (nameElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.NAME);
        }
        setName(nameElement.getText());
        
        // description
        Element descElement = e.getChild(Tags.DESCRIPTION, AtomAdminService.NAMESPACE);
        if (descElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.DESCRIPTION);
        }
        setDescription(descElement.getText());
        
        // locale
        Element localeElement = e.getChild(Tags.LOCALE, AtomAdminService.NAMESPACE);
        if (localeElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.LOCALE);
        }
        setLocale(localeElement.getText());
        
        // timezone
        Element tzElement = e.getChild(Tags.TIMEZONE, AtomAdminService.NAMESPACE);
        if (tzElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.TIMEZONE);
        }
        setTimezone(tzElement.getText());
        
        // creator
        Element creatorElement = e.getChild(Tags.CREATING_USER, AtomAdminService.NAMESPACE);
        if (creatorElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.CREATING_USER);
        }
        setCreatingUser(creatorElement.getText());
        
        // email address
        Element emailElement = e.getChild(Tags.EMAIL_ADDRESS, AtomAdminService.NAMESPACE);
        if (emailElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.EMAIL_ADDRESS);
        }
        setEmailAddress(emailElement.getText());        
        
        // created (optional)
        Element createdElement = e.getChild(Tags.DATE_CREATED, AtomAdminService.NAMESPACE);
        if (createdElement != null) {
            setDateCreated(new Date(Long.valueOf(createdElement.getText()).longValue()));
        }              
    }
    
    public WeblogEntry(WebsiteData wd, String urlPrefix) {
        String href = urlPrefix + "/" + EntrySet.Types.WEBLOGS + "/" + wd.getHandle();
        
        setHref(href);
        setHandle(wd.getHandle());
        setName(wd.getName());
        setDescription(wd.getDescription());
        setLocale(wd.getLocale());
        setTimezone(wd.getTimeZone());
        setCreatingUser(wd.getCreator().getUserName());
        setEmailAddress(wd.getEmailAddress());        
        setDateCreated(wd.getDateCreated());
    }
    
    public String getType() {
        return Types.WEBLOG;
    }
    
    public Document toDocument() {        
        Element weblog = new Element(Tags.WEBLOG, AtomAdminService.NAMESPACE);
        Document doc = new Document(weblog);
        
        // link
        weblog.setAttribute(Attributes.HREF, getHref());
        
        // handle
        Element handle = new Element(Tags.HANDLE, AtomAdminService.NAMESPACE);
        Text handleText = new Text(getHandle());
        handle.addContent(handleText);
        weblog.addContent(handle);
        
        // name
        Element name = new Element(Tags.NAME, AtomAdminService.NAMESPACE);
        Text nameText = new Text(getName());
        name.addContent(nameText);
        weblog.addContent(name);
        
        // description
        Element desc = new Element(Tags.DESCRIPTION, AtomAdminService.NAMESPACE);
        Text descText = new Text(getDescription());
        desc.addContent(descText);
        weblog.addContent(desc);
        
        // locale
        Element locale = new Element(Tags.LOCALE, AtomAdminService.NAMESPACE);
        Text localeText = new Text(getLocale());
        locale.addContent(localeText);
        weblog.addContent(locale);
        
        // timezone
        Element tz = new Element(Tags.TIMEZONE, AtomAdminService.NAMESPACE);
        Text tzText = new Text(getTimezone());
        tz.addContent(tzText);
        weblog.addContent(tz);
        
        // creating user
        Element creator = new Element(Tags.CREATING_USER, AtomAdminService.NAMESPACE);
        Text creatorText = new Text(String.valueOf(getCreatingUser()));
        creator.addContent(creatorText);
        weblog.addContent(creator);
        
        // email address
        Element email = new Element(Tags.EMAIL_ADDRESS, AtomAdminService.NAMESPACE);
        Text emailText = new Text(String.valueOf(getEmailAddress()));
        email.addContent(emailText);
        weblog.addContent(email);        
        
        // creation date (optional)
        Element created = new Element(Tags.DATE_CREATED, AtomAdminService.NAMESPACE);
        Date datedCreated = getDateCreated();
        if (dateCreated != null) {
            Text createdText = new Text(String.valueOf(dateCreated.getTime()));
            created.addContent(createdText);
            weblog.addContent(created);
        }
        
        return doc;
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
    
    public String getLocale() {
        return locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
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
}
