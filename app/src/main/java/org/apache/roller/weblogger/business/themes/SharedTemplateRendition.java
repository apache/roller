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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business.themes;

import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

/**
 * A pojo that maintains device-specific renditions of a single template
 */
public class SharedTemplateRendition implements TemplateRendition, Serializable {

    private TemplateLanguage templateLanguage = TemplateLanguage.VELOCITY;
    private RenditionType type = RenditionType.NORMAL;
    private String contentsFile = null;
	private String template = null;

	public SharedTemplateRendition() {
	}

    public String getContentsFile() {
        return contentsFile;
    }

    public void setContentsFile(String contentsFile) {
        this.contentsFile = contentsFile;
    }

	public RenditionType getRenditionType() {
		return type;
	}

    @XmlAttribute
	public void setRenditionType(RenditionType type) {
		this.type = type;
	}

    public TemplateLanguage getTemplateLanguage() {
        return templateLanguage;
    }

    public void setTemplateLanguage(TemplateLanguage templateLanguage) {
        this.templateLanguage = templateLanguage;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

	// ------------------------------------------------------- Good citizenship

	public String toString() {
        return "{" + this.template + ", [ " + this.template +"] , " + this.type + "}";
	}

	public boolean equals(SharedTemplateRendition other) {
		return other == this || new EqualsBuilder()
				.append(template, other.getTemplate())
                .append(templateLanguage, other.getTemplateLanguage())
                .append(type, other.getRenditionType())
                .isEquals();
	}

	public int hashCode() {
		return new HashCodeBuilder().append(getTemplate()).toHashCode();
	}

}
