/*
 * UserEntry.java
 *
 * Created on January 17, 2006, 12:44 PM
 */

package org.roller.presentation.atomadminapi;

import java.util.Date;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.roller.pojos.UserData;
import org.roller.presentation.atomadminapi.Entry.Types;

/**
 * This class describes a user entry; a user weblog resource.
 * @author jtb
 */
class UserEntry extends Entry {
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
    private String locale;
    private String timezone;
    private Date dateCreated;
    private String emailAddress;
    
    /** Construct a user entry from a JDOM element. */
    public UserEntry(Element e, String urlPrefix) throws Exception {
        // name
        Element nameElement = e.getChild(Tags.NAME, NAMESPACE);
        if (nameElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.NAME);
        }
        setName(nameElement.getText());
        String href = urlPrefix + "/" + EntrySet.Types.USERS + "/" + getName();
        setHref(href);
        
        // full name
        Element fullNameElement = e.getChild(Tags.FULL_NAME, NAMESPACE);
        if (fullNameElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.FULL_NAME);
        }
        setFullName(fullNameElement.getText());
        
        // password
        Element passwordElement = e.getChild(Tags.PASSWORD, NAMESPACE);
        if (passwordElement == null) {
            throw new Exception("ERROR: Missing element: " + Tags.PASSWORD);
        }
        setPassword(passwordElement.getText());
        
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
    
    /** Construct a user entry from a Roller UserData object. */
    public UserEntry(UserData ud, String urlPrefix) {
        String href = urlPrefix + "/" + EntrySet.Types.USERS + "/" + ud.getUserName();
        
        setHref(href);
        setName(ud.getUserName());
        setFullName(ud.getFullName());
        setPassword(ud.getPassword());
        setLocale(ud.getLocale());
        setTimezone(ud.getTimeZone());
        setEmailAddress(ud.getEmailAddress());
        setDateCreated(ud.getDateCreated());
    }
        
    public String getType() {
        return Types.USER;
    }
    
    public Document toDocument() {
        Element user = new Element(Tags.USER, NAMESPACE);
        Document doc = new Document(user);
        
        // href
        user.setAttribute(Attributes.HREF, getHref());
               
        // name
        Element name = new Element(Tags.NAME, AtomAdminService.NAMESPACE);
        Text nameText = new Text(getName());
        name.addContent(nameText);
        user.addContent(name);
       
        // full name
        Element fullName = new Element(Tags.FULL_NAME, NAMESPACE);
        Text fullNameText = new Text(getFullName());
        fullName.addContent(fullNameText);
        user.addContent(fullName);
        
        // password
        Element password = new Element(Tags.PASSWORD, NAMESPACE);
        Text passwordText = new Text(getPassword());
        password.addContent(passwordText);
        user.addContent(password);
        
        // locale
        Element locale = new Element(Tags.LOCALE, AtomAdminService.NAMESPACE);
        Text localeText = new Text(getLocale());
        locale.addContent(localeText);
        user.addContent(locale);
        
        // timezone
        Element tz = new Element(Tags.TIMEZONE, AtomAdminService.NAMESPACE);
        Text tzText = new Text(getTimezone());
        tz.addContent(tzText);
        user.addContent(tz);
                
        // email address
        Element email = new Element(Tags.EMAIL_ADDRESS, AtomAdminService.NAMESPACE);
        Text emailText = new Text(String.valueOf(getEmailAddress()));
        email.addContent(emailText);
        user.addContent(email);
        
        // creation date (optional)
        Element created = new Element(Tags.DATE_CREATED, AtomAdminService.NAMESPACE);
        Date datedCreated = getDateCreated();
        if (getDateCreated() != null) {
            Text createdText = new Text(String.valueOf(getDateCreated().getTime()));
            created.addContent(createdText);
            user.addContent(created);
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
    public String getLocale() {
        return locale;
    }
    
    /** Set the locale string of this user entry. */
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    /** Get the timezone string of this user entry. */
    public String getTimezone() {
        return timezone;
    }
    
    /** Set the timezone string of this user entry. */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
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
    
    /** This object, as a Roller UserData object. */
    public UserData toUserData() {
        UserData ud = new UserData();
        ud.setUserName(getName());
        ud.setFullName(getFullName());
        ud.setPassword(getPassword());
        ud.setEmailAddress(getEmailAddress());
        ud.setLocale(getLocale());
        ud.setTimeZone(getTimezone());
        ud.setDateCreated(getDateCreated());
        
        return ud;
    }
}
