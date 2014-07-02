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


<p class="subtitle"><s:text name="planetConfig.subtitle" /></a>
<p><s:text name="planetConfig.prompt" /></a>

<s:form action="planetConfig!save">
	<s:hidden name="salt" />

    <table class="formtableNoDesc">
    
    <s:iterator id="dg" value="globalConfigDef.displayGroups">
    
        <tr>
            <td colspan="3"><h2><s:text name="%{#dg.key}" /></h2></td>
        </tr>
    
        <s:iterator id="pd" value="#dg.propertyDefs">
            
            <tr>
                <td class="label"><s:text name="%{#pd.key}" /></td>
              
                  <%-- "string" type means use a simple textbox --%>
                  <s:if test="#pd.type == 'string'">
                    <td class="field"><input type="text" name='<s:property value="#pd.name"/>' value='<s:property value="properties[#pd.name].value"/>' size="35" /></td>
                  </s:if>
                  
                  <%-- "text" type means use a full textarea --%>
                  <s:elseif test="#pd.type == 'text'">
                    <td class="field">
                      <textarea name='<s:property value="#pd.name"/>' rows="<s:property value="#pd.rows"/>" cols="<s:property value="#pd.cols"/>"><s:property value="properties[#pd.name].value"/></textarea>
                    </td>
                  </s:elseif>
                  
                  <%-- "boolean" type means use a checkbox --%>
                  <s:elseif test="#pd.type == 'boolean'">
                      <s:if test="properties[#pd.name].value == 'true'">
                          <td class="field"><input type="checkbox" name='<s:property value="#pd.name"/>' CHECKED></td>
                      </s:if>
                      <s:else>
                          <td class="field"><input type="checkbox" name='<s:property value="#pd.name"/>'></td>
                      </s:else>
                  </s:elseif>
                  
                  <%-- if it's something we don't understand then use textbox --%>
                  <s:else>
                    <td class="field"><input type="text" name='<s:property value="#pd.name"/>' size="50" /></td>
                  </s:else>
                
                <td class="description"><%-- <s:text name="" /> --%></td>
            </tr>
          
        </s:iterator>
      
        <tr>
            <td colspan="2">&nbsp;</td>
        </tr>
        
    </s:iterator>

    </table>
    
    <div class="control">
        <input class="buttonBox" type="submit" value="<s:text name="generic.save"/>"/>
    </div>
    
</s:form>
