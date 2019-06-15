<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>


<p class="subtitle"><s:text name="planetConfig.subtitle"/></p>
<p><s:text name="planetConfig.prompt"/></p>


<s:form action="planetConfig!save" theme="bootstrap" cssClass="form-horizontal">
    <s:hidden name="salt"/>

    <s:iterator var="dg" value="globalConfigDef.displayGroups">

        <h2><s:text name="%{#dg.key}"/></h2>

        <s:iterator var="pd" value="#dg.propertyDefs">

            <%-- "string" type means use a simple textbox --%>
            <s:if test="#pd.type == 'string'">
                <s:textfield name="%{#pd.name}" label="%{getText(#pd.key)}" size="35"
                             value="%{properties[#pd.name].value} "/>
            </s:if>

            <%-- "text" type means use a full textarea --%>
            <s:elseif test="#pd.type == 'text'">
                <s:textarea name="%{#pd.name}" label="%{getText(#pd.key)}" rows="#pd.rows" cols="#pd.cols"
                            value="%{properties[#pd.name].value} "/>
            </s:elseif>

            <%-- "boolean" type means use a checkbox --%>
            <s:elseif test="#pd.type == 'boolean'">

                <s:if test="properties[#pd.name].value == 'true'">
                    <s:checkbox name="%{#pd.name}" label="%{getText(#pd.key)}" cssClass="boolean"
                                fieldValue="true" checked="true" onchange="formChanged()"/>
                </s:if>
                <s:if test="properties[#pd.name].value != 'true'">
                    <s:checkbox name="%{#pd.name}" label="%{getText(#pd.key)}" cssClass="boolean"
                                fieldValue="false" onchange="formChanged()"/>
                </s:if>

            </s:elseif>

            <%-- if it's something we don't understand then use textbox --%>
            <s:else>
                <s:textfield name="%{#pd.name}" label="%{getText(#pd.key)}" size="35"
                             value="%{properties[#pd.name].value}"/>
            </s:else>

        </s:iterator>

    </s:iterator>

     <input class="btn btn-default" type="submit" value="<s:text name="generic.save"/>"/>

</s:form>
