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

package org.apache.roller.weblogger.pojos;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

import java.io.Serializable;

/**
 * A pojo that will maintain different template codes for one template
 */
public class CustomTemplateRendition implements Serializable, TemplateRendition {

	private static final long serialVersionUID = -1497618963802805151L;
	private String id = UUIDGenerator.generateUUID();
	private String templateId = null;
	// template contents
	private String template = null;
	private RenditionType type = null;
	private String templateLanguage = null;

	public CustomTemplateRendition(String templateId, RenditionType type) {
		this.templateId = templateId;
		this.type = type;
	}

	public CustomTemplateRendition() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	// ------------------------------------------------------- Good citizenship

	public String toString() {
        return "{" + getId()
                + ", " + getTemplateId()
                + ", [ " + getTemplate()
                + "] , " + getType() + "}";
	}

	public boolean equals(Object other) {
		if (other == this) {
            return true;
        }
		if (!(other instanceof CustomTemplateRendition)) {
            return false;
        }
		CustomTemplateRendition o = (CustomTemplateRendition) other;
		return new EqualsBuilder().append(getTemplateId(), o.getTemplateId())
				.append(getTemplate(), o.getTemplate()).isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder().append(getTemplateId())
				.append(getTemplate()).toHashCode();
	}

	// @Override
	public String getTemplateLanguage() {
		return templateLanguage;
	}

	// @Override
	public void setTemplateLanguage(String templateLanguage) {
		this.templateLanguage = templateLanguage;
	}

}
