/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.planet.pojos;

import java.io.Serializable;
import org.apache.roller.pojos.*;

import org.apache.roller.pojos.PersistentObject;

/**
 * @struts.form include-all="true"
 * @hibernate.class lazy="false" table="rag_config"
 * @author Dave Johnson
 */
public class PlanetConfigData extends PersistentObject implements Serializable
{
    /** Database ID */
    protected String id;
    
    /** Deftault group of planet */
    protected PlanetGroupData defaultGroup;
    
    /** Base URL of planet */
    protected String siteURL;
    
    /** Proxy port or null if none */
    protected String proxyHost; 
    
    /** proxy port, ignored if proxyHost is null */
    protected int proxyPort;
    
    /** Name of template to be used for main page */
    protected String mainPage;
    
    /** Name of template to be used for groups that don't provide a template */
    protected String groupPage;
    
    /** Name of administrator responsible for site */
    protected String adminName = null;
    
    /** Email of administrator responsible for site */
    protected String adminEmail = null;
    
    /** Title of site */
    protected String title = null;
    
    /** Description of site */
    protected String description = null;
    
    /** Where to place the generated files */
    protected String outputDir = null;
    
    /** Where to find the Velocity templates on disk */
    protected String templateDir = null;
    
    /** Location for caching newsfeed data */
    protected String cacheDir = "";
    
    
    //----------------------------------------------------------- persistent fields
    /** 
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
     */
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }

    /** 
     * @hibernate.many-to-one column="default_group_id" cascade="all" not-null="false"
     */
    public PlanetGroupData getDefaultGroup()
    {
        return defaultGroup;
    }
    public void setDefaultGroup(PlanetGroupData group)
    {
        this.defaultGroup = group;
    }

    /** 
     * @hibernate.property column="group_page" non-null="false" unique="false"
     */
    public String getGroupPage()
    {
        return groupPage;
    }    
    public void setGroupPage(String groupPage)
    {
        this.groupPage = groupPage;
    }
    
    /** 
     * @hibernate.property column="main_page" non-null="false" unique="false"
     */
    public String getMainPage()
    {
        return mainPage;
    }
    public void setMainPage(String mainPage)
    {
        this.mainPage = mainPage;
    }
    
    /** 
     * @hibernate.property column="proxy_host" non-null="false" unique="false"
     */
    public String getProxyHost()
    {
        return proxyHost;
    }
    public void setProxyHost(String proxyHost)
    {
        this.proxyHost = proxyHost;
    }
    
    /** 
     * @hibernate.property column="proxy_port" non-null="false" unique="false"
     */
    public int getProxyPort()
    {
        return proxyPort;
    }
    public void setProxyPort(int proxyPort)
    {
        this.proxyPort = proxyPort;
    }

    /** 
     * @hibernate.property column="site_url" non-null="false" unique="false"
     */
    public String getSiteURL()
    {
        return siteURL;
    }
    public void setSiteURL(String siteURL)
    {
        this.siteURL = siteURL;
    }
    
    /** 
     * @hibernate.property column="admin_email" non-null="true" unique="false"
     */
    public String getAdminEmail()
    {
        return adminEmail;
    }
    public void setAdminEmail(String adminEmail)
    {
        this.adminEmail = adminEmail;
    }
    
    /** 
     * @hibernate.property column="admin_name" non-null="false" unique="false"
     */
    public String getAdminName()
    {
        return adminName;
    }
    public void setAdminName(String adminName)
    {
        this.adminName = adminName;
    }

    /** 
     * @hibernate.property column="output_dir" non-null="false" unique="false"
     */
    public String getOutputDir()
    {
        return outputDir;
    }
    public void setOutputDir(String outputDir)
    {
        this.outputDir = outputDir;
    }
    
    /** 
     * @hibernate.property column="template_dir" non-null="false" unique="false"
     */
    public String getTemplateDir()
    {
        return templateDir;
    }
    public void setTemplateDir(String templateDir)
    {
        this.templateDir = templateDir;
    }

    /** 
     * @hibernate.property column="description" non-null="false" unique="false"
     */
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }

    /** 
     * @hibernate.property column="title" non-null="true" unique="false"
     */
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }

    /** 
     * @hibernate.property column="cache_dir" non-null="true" unique="false"
     */
    public String getCacheDir()
    {
        return cacheDir;
    }
    public void setCacheDir(String dir)
    {
        cacheDir = dir;
    }

    //-------------------------------------------------------------- implementation
    public void setData(PersistentObject vo)
    {
        // TODO Auto-generated method stub
    }
}