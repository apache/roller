package org.apache.roller.planet.ui.admin.struts.forms;

import java.io.Serializable;
import org.apache.roller.RollerException;
import java.util.Locale;
import org.apache.struts.action.ActionForm;


/**
 * Formbean for planet subscriptions action.
 *
 * @struts.form name="planetSubscriptionForm"
 */
public class PlanetSubscriptionForm extends ActionForm implements Serializable {
    
    protected java.lang.String id;
    protected java.lang.String feedURL;
    protected java.lang.String siteURL;
    protected java.lang.String title;
    private String groupHandle = null;
    
    
    /** Default empty constructor. */
    public PlanetSubscriptionForm() {}
    
    /** Constructor that takes the data object as argument. */
    public PlanetSubscriptionForm(org.apache.roller.planet.pojos.PlanetSubscriptionData dataHolder, java.util.Locale locale) throws RollerException {
        copyFrom(dataHolder, locale);
    }
    
    
    public String getGroupHandle() {
        return groupHandle;
    }
    
    public void setGroupHandle(String groupHandle) {
        this.groupHandle = groupHandle;
    }
    
    public java.lang.String getId() {
        return this.id;
    }
    
    public void setId( java.lang.String id ) {
        this.id = id;
    }
    
    public java.lang.String getFeedURL() {
        return this.feedURL;
    }
    
    public void setFeedURL( java.lang.String feedURL ) {
        this.feedURL = feedURL;
    }
    
    public java.lang.String getSiteURL() {
        return this.siteURL;
    }
    
    public void setSiteURL( java.lang.String siteURL ) {
        this.siteURL = siteURL;
    }
    
    public java.lang.String getTitle() {
        return this.title;
    }
    
    public void setTitle( java.lang.String title ) {
        this.title = title;
    }
    
    
    /**
     * Copy values from this form bean to the specified data object.
     * Only copies primitive types (Boolean, boolean, String, Integer, int, Timestamp, Date)
     */
    public void copyTo(org.apache.roller.planet.pojos.PlanetSubscriptionData dataHolder, Locale locale) throws RollerException {
        
        dataHolder.setFeedURL(this.feedURL);
        
        dataHolder.setSiteURL(this.siteURL);
        
        dataHolder.setTitle(this.title);
    }
    
    
    /**
     * Copy values from specified data object to this form bean.
     * Includes all types.
     */
    public void copyFrom(org.apache.roller.planet.pojos.PlanetSubscriptionData dataHolder, Locale locale) throws RollerException {
        
        this.id = dataHolder.getId();
        
        this.feedURL = dataHolder.getFeedURL();
        
        this.siteURL = dataHolder.getSiteURL();
        
        this.title = dataHolder.getTitle();
        
    }
    
    
    public void doReset(
            org.apache.struts.action.ActionMapping mapping,
            javax.servlet.ServletRequest request) {
        
        this.id = null;
        
        this.feedURL = null;
        
        this.siteURL = null;
        
        this.title = null;
        
    }
    
    public void reset(
            org.apache.struts.action.ActionMapping mapping,
            javax.servlet.ServletRequest request) {
        doReset(mapping, request);
    }
    
    public void reset(
            org.apache.struts.action.ActionMapping mapping,
            javax.servlet.http.HttpServletRequest request) {
        doReset(mapping, request);
    }
    
}
