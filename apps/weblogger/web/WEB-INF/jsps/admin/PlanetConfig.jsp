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

<%-- Start by parsing our config defs using the jstl xml toolkit --%>
<%-- Then we'll progress through the config defs and print out the form --%>
<x:parse var="configDefs">
  <%= org.apache.roller.planet.config.PlanetRuntimeConfig.getRuntimeConfigDefsAsString() %>
</x:parse>


<p class="subtitle"><s:text name="planetConfig.subtitle" /></a>
<p><s:text name="planetConfig.prompt" /></a>

<s:form action="planetConfig">

<input type="hidden" name="method" value="update">

    <table class="formtableNoDesc">
    
    <x:forEach select="$configDefs//config-def[@name='global-properties']/display-group">
        <c:set var="displayGroupKey"><x:out select="@key"/></c:set>
    
        <tr>
            <td colspan="3"><h2><s:text name="${displayGroupKey}" /></h2></td>
        </tr>
    
        <x:forEach select="property-def">
            <c:set var="propLabelKey"><x:out select="@key"/></c:set>
            <c:set var="name"><x:out select="@name"/></c:set>
        
            <tr>
                <td class="label"><s:text name="${propLabelKey}" /></td>
              
                <%-- choose the right html input element for the display --%>
                <x:choose>
                
                  <%-- "string" type means use a simple textbox --%>
                  <x:when select="type='string'">
                    <td class="field"><input type="text" name='<c:out value="${name}"/>' value='<c:out value="${PlanetProps[name].value}"/>' size="35" /></td>
                  </x:when>
                  
                  <%-- "text" type means use a full textarea --%>
                  <x:when select="type='text'">
                    <td class="field">
                      <textarea name='<c:out value="${name}"/>' rows="<x:out select="rows"/>" cols="<x:out select="cols"/>"><c:out value="${PlanetProps[name].value}"/></textarea>
                    </td>
                  </x:when>
                  
                  <%-- "boolean" type means use a checkbox --%>
                  <x:when select="type='boolean'">
                    <c:choose>
                      <c:when test="${PlanetProps[name].value eq 'true'}">
                          <td class="field"><input type="checkbox" name='<c:out value="${name}"/>' CHECKED></td>
                      </c:when>
                      <c:otherwise>
                          <td class="field"><input type="checkbox" name='<c:out value="${name}"/>'></td>
                      </c:otherwise>
                    </c:choose>
                  </x:when>
                  
                  <%-- if it's something we don't understand then use textbox --%>
                  <x:otherwise>
                    <td class="field"><input type="text" name='<c:out value="${name}"/>' size="50" /></td>
                  </x:otherwise>
                </x:choose>
                
                <td class="description"><%-- <s:text name="" /> --%></td>
            </tr>
          
        </x:forEach>
      
        <tr>
            <td colspan="2">&nbsp;</td>
        </tr>
        
    </x:forEach>

    </table>
    
    <s:submit cssClass="buttonBox" key="configForm.save" />
    
</s:form>
