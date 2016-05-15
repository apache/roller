/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.pojos;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerCommon;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * A pojo that will maintain different template codes for one template
 */
@Entity
@Table(name="weblog_template_rendition")
public class WeblogTemplateRendition implements TemplateRendition {

	private String id = WebloggerCommon.generateUUID();
    private WeblogTemplate weblogTemplate = null;
	// template contents
	private String template = "";
	private RenditionType renditionType = null;
	private TemplateLanguage templateLanguage = null;

	public WeblogTemplateRendition(WeblogTemplate template, RenditionType renditionType) {
		this.weblogTemplate = template;
		this.renditionType = renditionType;
        weblogTemplate.addTemplateRendition(this);
	}

	public WeblogTemplateRendition() {
	}

	@ManyToOne
	@JoinColumn(name="templateid", nullable=false)
    public WeblogTemplate getWeblogTemplate() {
        return weblogTemplate;
    }

    public void setWeblogTemplate(WeblogTemplate weblogTemplate) {
        this.weblogTemplate = weblogTemplate;
    }

    @Id
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	@Basic(optional=false)
	public String getTemplate() {
		return template;
	}

	@Override
	public void setTemplate(String template) {
		this.template = template;
	}

	@Column(name="device", nullable=false)
	@Enumerated(EnumType.STRING)
	public RenditionType getRenditionType() {
		return renditionType;
	}

	public void setRenditionType(RenditionType renditionType) {
		this.renditionType = renditionType;
	}

	// ------------------------------------------------------- Good citizenship

	public String toString() {
        return "{" + getId()
                + ", " + getWeblogTemplate().getId()
                + ", [ " + getTemplate()
                + "] , " + getRenditionType() + "}";
	}

	public boolean equals(Object other) {
		if (other == this) {
            return true;
        }
		if (!(other instanceof WeblogTemplateRendition)) {
            return false;
        }
		WeblogTemplateRendition o = (WeblogTemplateRendition) other;
		return new EqualsBuilder().append(getWeblogTemplate().getId(), o.getWeblogTemplate().getId())
				.append(getTemplate(), o.getTemplate()).isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder().append(getWeblogTemplate().getId())
				.append(getTemplate()).toHashCode();
	}

	@Column(name="templatelang")
	@Enumerated(EnumType.STRING)
	public TemplateLanguage getTemplateLanguage() {
		return templateLanguage;
	}

	public void setTemplateLanguage(TemplateLanguage templateLanguage) {
		this.templateLanguage = templateLanguage;
	}

}
