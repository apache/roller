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
package org.apache.roller.planet.ui.forms;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetConfigData;
import org.apache.roller.util.MailUtil;

/**
 * Registration for folks who wish to have their blog added to the aggregation.
 */
public class RegistrationForm {    
    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");
            
    private String blogTitle;
    private String feedURL;
    private String blogURL;
    
    private String fullName;
    private String email;
    private String relationship;
    private String otherID;
    private String otherURL;
    
    private boolean agreeToTerms = false;
    
    /** Creates a new instance of RegistrationForm */
    public RegistrationForm() {
    }
    
    public String register() throws Exception {
        StringBuffer sb = new StringBuffer();
        
        sb.append(bundle.getString("regBloggerRequestedAddition"));
        sb.append("\n\n");
        
        sb.append(bundle.getString("regBlogTitle")).append(":\n    ");
        sb.append(blogTitle);
        sb.append("\n");
        
        sb.append(bundle.getString("regBlogURL")).append(":\n    ");
        sb.append(blogURL);
        sb.append("\n");
        
        sb.append(bundle.getString("regFeedURL")).append(":\n    ");
        sb.append(feedURL);
        sb.append("\n");
        
        sb.append(bundle.getString("regFullName")).append(":\n    ");
        sb.append(fullName);
        sb.append("\n");
        
        sb.append(bundle.getString("regEmail")).append(":\n    ");
        sb.append(email);
        sb.append("\n");
        
        sb.append(bundle.getString("regRelationship")).append(":\n    ");
        sb.append(relationship);
        sb.append("\n");
        
        sb.append(bundle.getString("regOtherID")).append(":\n    ");
        sb.append(otherID);
        sb.append("\n");
        
        sb.append(bundle.getString("regOtherURL")).append(":\n    ");
        sb.append(otherURL);
        sb.append("\n");
                      
        PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
        PlanetConfigData config = pmgr.getConfiguration();
        String subject = MessageFormat.format(bundle.getString("regSubject"), new Object[] {config.getSiteURL()});
        String content = sb.toString();
        String adminEmail = config.getAdminEmail();            
        Context ctx = (Context)new InitialContext().lookup("java:comp/env");
        Session session = (Session) ctx.lookup("mail/Session");
        MailUtil.sendTextMessage(session, adminEmail, new String[] {adminEmail}, null, null, subject, content);
        
        return "thanksForRegistering";
    }

    public String getBlogTitle() {
        return blogTitle;
    }

    public void setBlogTitle(String blogTitle) {
        this.blogTitle = blogTitle;
    }

    public String getFeedURL() {
        return feedURL;
    }

    public void setFeedURL(String feedURL) {
        this.feedURL = feedURL;
    }

    public String getBlogURL() {
        return blogURL;
    }

    public void setBlogURL(String blogURL) {
        this.blogURL = blogURL;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtherID() {
        return otherID;
    }

    public void setOtherID(String otherID) {
        this.otherID = otherID;
    }

    public String getOtherURL() {
        return otherURL;
    }

    public void setOtherURL(String otherURL) {
        this.otherURL = otherURL;
    }

    public boolean isAgreeToTerms() {
        return agreeToTerms;
    }

    public void setAgreeToTerms(boolean agreeToTerms) {
        this.agreeToTerms = agreeToTerms;
    }
    
    public void checkAgreeToTerms(FacesContext context, UIComponent component, Object value) {
        if (value == null || !(value instanceof Boolean)) return;
        Boolean agreed = (Boolean)value;
        if (!agreed.booleanValue()) {
            FacesMessage msg = new FacesMessage();
            msg.setDetail(bundle.getString("regMustAgreeToTerms"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
        return;
    }     
    
    public void checkURL(FacesContext context, UIComponent component, Object value) {
        if (value == null || !(value instanceof String)) return;
        try {
            URL url = new URL((String)value);
        } catch (MalformedURLException ex) {
            FacesMessage msg = new FacesMessage();
            msg.setDetail(bundle.getString("errorBadURL"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
        return;
    }   
}
