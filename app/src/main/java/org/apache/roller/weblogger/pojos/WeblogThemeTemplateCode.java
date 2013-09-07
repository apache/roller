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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

import java.io.Serializable;

/**
 * A pojo that will maintain different template codes for one template
 */
public class WeblogThemeTemplateCode implements Serializable, TemplateCode {

	private static final long serialVersionUID = -1497618963802805151L;
	private String id = UUIDGenerator.generateUUID();
	private String templateId = null;
	// template contents
	private String template = null;
	private String type = null;
	private String contentType = null;
	private String templateLanguage = null;

	public WeblogThemeTemplateCode(String templateId, String type) {
		this.templateId = templateId;
		this.type = type;
	}

	public WeblogThemeTemplateCode() {
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
	public String getType() {
		return type;
	}

	// @Override
	public void setType(String type) {
		this.type = type;
	}

	// ------------------------------------------------------- Good citizenship

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("{");
		buf.append(getId());
		buf.append(", ").append(getTemplateId());
		buf.append(", [ ").append(getTemplate());
		buf.append("] , ").append(getType());
		buf.append("}");
		return buf.toString();
	}

	public boolean equals(Object other) {
		if (other == this) {
            return true;
        }
		if (!(other instanceof WeblogThemeTemplateCode)) {
            return false;
        }
		WeblogThemeTemplateCode o = (WeblogThemeTemplateCode) other;
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

	// @Override
	public String getContentType() {
		return contentType;
	}

	// @Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
