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

package org.apache.roller.weblogger.business.themes;

import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 * A pojo that maintains device-specific renditions of a single template
 */
public class SharedThemeTemplateRendition implements Serializable, TemplateRendition {

	private String templateId = null;
	private String template = null;
	private RenditionType type = null;
	private TemplateLanguage templateLanguage = null;
	private Date lastModified = null;

	public SharedThemeTemplateRendition(String templateId, RenditionType type) {
		this.templateId = templateId;
		this.type = type;
	}

	public SharedThemeTemplateRendition() {
	}

	// @Override
	public String getTemplate() {
		return template;
	}

	// @Override
	public void setTemplate(String template) {
		this.template = template;
	}

	// @Override
	public String getTemplateId() {
		return templateId;
	}

	// @Override
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	// @Override
	public RenditionType getType() {
		return type;
	}

	// @Override
	public void setType(RenditionType type) {
		this.type = type;
	}
	
	/**
	 * Gets the last modified. File system date.
	 * 
	 * @return the last modified
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * Sets the last modified. File system date.
	 * 
	 * @param lastModified
	 *            the new last modified
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	// ------------------------------------------------------- Good citizenship

	public String toString() {
        return "{" + this.templateId + ", [ " + this.template +"] , " + this.type + "}";
	}

	public boolean equals(Object other) {
		if (other == this) {
            return true;
        }
		if (!(other instanceof SharedThemeTemplateRendition)) {
            return false;
        }
		SharedThemeTemplateRendition o = (SharedThemeTemplateRendition) other;
		return new EqualsBuilder().append(templateId, o.getTemplateId())
				.append(template, o.getTemplate()).isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder().append(getTemplateId())
				.append(getTemplate()).toHashCode();
	}

	// @Override
	public TemplateLanguage getTemplateLanguage() {
		return templateLanguage;
	}

	// @Override
	public void setTemplateLanguage(TemplateLanguage templateLanguage) {
		this.templateLanguage = templateLanguage;
	}

}
