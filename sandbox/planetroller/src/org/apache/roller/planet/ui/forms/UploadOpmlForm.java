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

import java.io.InputStreamReader;
import java.util.Iterator;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

public class UploadOpmlForm {
    private static Log log = LogFactory.getLog(UploadOpmlForm.class);
    private String groupid;
    private UploadedFile uploadedFile;
    
    public String load(HttpServletRequest request) throws Exception {
        setGroupid(request.getParameter("groupid"));        
        return "uploadOpml";
    }
    
    public String presentUploadForm() throws Exception {
        FacesContext fctx = FacesContext.getCurrentInstance();
        return load((HttpServletRequest)fctx.getExternalContext().getRequest());
    }
    
    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }
    
    public String processUpload() throws Exception {        
        PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
        PlanetGroupData group = pmgr.getGroupById(getGroupid());        
        SAXBuilder builder = new SAXBuilder();           
        Document doc = builder.build(new InputStreamReader(uploadedFile.getInputStream()));
        Element body = doc.getRootElement().getChild("body");
        Iterator iter = body.getChildren().iterator();
        while (iter.hasNext()) {
            Element elem = (Element)iter.next();
            importOpmlElement(group, elem);
        }
        PlanetFactory.getPlanet().flush();
        return "uploadOpml";
    }

    private void importOpmlElement(PlanetGroupData group, Element elem) throws Exception {

        String text = elem.getAttributeValue("text");        
        String title = elem.getAttributeValue("title");
        title =  null!=title ? title : text;
        
        String url = elem.getAttributeValue("url");
        String xmlUrl = elem.getAttributeValue("xmlUrl");
        xmlUrl =  null!=xmlUrl ? xmlUrl : url;

        String htmlUrl = elem.getAttributeValue("htmlUrl");
        url = null!=htmlUrl ? htmlUrl : url;
                
        if (elem.getChildren().size()==0) {
            // Leaf element, store a subscription
            if (null != title && null != xmlUrl) {  
                PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager(); 
                
                // If subscription already exists in Planet, just use it
                PlanetSubscriptionData sub = pmgr.getSubscription(xmlUrl);
                
                // Otherwise create a new one
                if (sub == null) {      
                    sub = new PlanetSubscriptionData();
                    sub.setTitle(title);
                    sub.setFeedURL(xmlUrl);
                    sub.setSiteURL(url);  
                    pmgr.saveSubscription(sub);
                } 
                try {
                    // Save sub and add it to group
                    sub.getGroups().add(group);
                    pmgr.saveSubscription(sub);
                    pmgr.saveGroup(group);
                    
                } catch (RollerException e) {
                    log.debug("ERROR: importing subscription with feedURL: " + xmlUrl);
                }
            }
        } else {          
            // Import folder's children
            Iterator iter = elem.getChildren("outline").iterator();
            while ( iter.hasNext() ) {
                Element subelem = (Element)iter.next();
                importOpmlElement(group, subelem);
            }
        }
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }
}
