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
package org.apache.roller.weblogger.business.themes;

import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * A pojo that maintains device-specific renditions of a single template
 */
public class SharedTemplateRendition implements TemplateRendition {

    private Parser parser = Parser.VELOCITY;
    private RenditionType renditionType = RenditionType.NORMAL;
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
		return renditionType;
	}

    @XmlAttribute(name="device")
	public void setRenditionType(RenditionType type) {
		this.renditionType = type;
	}

    public Parser getParser() {
        return parser;
    }

    public void setParser(Parser renditionLanguage) {
        this.parser = renditionLanguage;
    }

    public String getRendition() {
        return template;
    }

    public void setRendition(String template) {
        this.template = template;
    }

	// ------------------------------------------------------- Good citizenship

	public String toString() {
        return "{" + this.contentsFile + ", [" + this.renditionType +"], " + this.parser + "}";
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SharedTemplateRendition that = (SharedTemplateRendition) o;

        if (parser != that.parser) return false;
        if (renditionType != that.renditionType) return false;
        if (contentsFile != null ? !contentsFile.equals(that.contentsFile) : that.contentsFile != null) return false;
        return template.equals(that.template);
    }

    @Override
    public int hashCode() {
        int result = parser.hashCode();
        result = 31 * result + renditionType.hashCode();
        result = 31 * result + (contentsFile != null ? contentsFile.hashCode() : 0);
        result = 31 * result + template.hashCode();
        return result;
    }
}
