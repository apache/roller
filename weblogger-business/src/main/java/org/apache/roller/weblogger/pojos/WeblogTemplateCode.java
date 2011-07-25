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

import javax.persistence.*;
import java.io.Serializable;

/**
 * A pojo that will maintain different template codes for one template
 */

@Entity
@Table(name = "rol_templatecode")
@NamedQueries({
@NamedQuery(name = "WeblogThemplateCode.getTemplateCodeByType",
        query = "SELECT c FROM WeblogTemplateCode c WHERE c.templateId = ?1 AND c.type =?2"),

     @NamedQuery(name = "WeblogThemplateCode.getTemplateCodesByTemplateId",
        query = "SELECT c FROM WeblogTemplateCode c WHERE c.templateId = ?1 ")
})
public class WeblogTemplateCode implements Serializable {


    private static final long serialVersionUID = -1497618963802805151L;
    private String id = UUIDGenerator.generateUUID();
    private String templateId = null;
    //template contents
    private String template = null;
    private String type = null;

    public WeblogTemplateCode(String templateId, String type){
        this.templateId = templateId;
        this.type = type;
    }

    public WeblogTemplateCode() {
    }

    @Id
    @Column(nullable = false, updatable = false)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(nullable = false, updatable = true, insertable = true)
    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @Basic
    @Column(nullable = false, updatable = true, insertable = true)
    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    @Basic
    @Column(nullable = false, updatable = true, insertable = true)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

 //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.templateId);
        buf.append(", [ ").append(this.template);
        buf.append("] , ").append(this.type);
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WeblogTemplateCode != true) return false;
        WeblogTemplateCode o = (WeblogTemplateCode)other;
        return new EqualsBuilder()
            .append(templateId, o.getTemplateId())
            .append(template, o.getTemplate())
            .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
            .append(getTemplateId())
            .append(getTemplate())
            .toHashCode();
    }

}
