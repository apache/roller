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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<p class="subtitle"><s:text name="configForm.subtitle" /></p>
<p><s:text name="configForm.prompt" /></p>

<s:form action="globalConfig!save">
	<s:hidden name="salt" />

    <table class="formtableNoDesc">
    
    <s:iterator id="dg" value="globalConfigDef.displayGroups">
    
        <tr>
            <td colspan="3"><h2><s:text name="%{#dg.key}" /></h2></td>
        </tr>
    
        <s:iterator id="pd" value="#dg.propertyDefs">
            
            <tr>
                <td class="label"><s:text name="%{#pd.key}" /></td>
                
                  <%-- special condition for comment plugins --%>
                  <s:if test="#pd.name == 'users.comments.plugins'">
                      <td class="field"><s:checkboxlist theme="roller" list="pluginsList"
                        name="commentPlugins" listKey="id" listValue="name" /></td>
                  </s:if>

                  <%-- special condition for front page blog --%>
                  <s:elseif test="#pd.name == 'site.frontpage.weblog.handle'">
                      <td class="field">
                          <select name='<s:property value="#pd.name"/>'>
                                <option value=''>
                                    <s:text name="configForm.none" />
                                </option>                              <s:iterator id="weblog" value="weblogs">
                                <option value='<s:property value="#weblog.handle"/>'
                                    <s:if test='properties[#pd.name].value == #weblog.handle'>selected='true'</s:if> >
                                    <s:property value="#weblog.name"/>
                                </option>
                              </s:iterator>
                          </select>
                      </td>
                  </s:elseif>

                  <%-- "string" type means use a simple textbox --%>
                  <s:elseif test="#pd.type == 'string'">
                    <td class="field"><input type="text" name='<s:property value="#pd.name"/>'
                        value='<s:property value="properties[#pd.name].value"/>' size="35" /></td>
                  </s:elseif>
                  
                  <%-- "text" type means use a full textarea --%>
                  <s:elseif test="#pd.type == 'text'">
                    <td class="field">
                      <textarea name='<s:property value="#pd.name"/>'
                                rows="<s:property value="#pd.rows"/>"
                                cols="<s:property value="#pd.cols"/>"><s:property value="properties[#pd.name].value"/>
                      </textarea>
                    </td>
                  </s:elseif>
                  
                  <%-- "boolean" type means use a checkbox --%>
                  <s:elseif test="#pd.type == 'boolean'">
                      <s:if test="properties[#pd.name].value == 'true'">
                          <td class="field"><input type="checkbox" 
                            name='<s:property value="#pd.name"/>' CHECKED></td>
                      </s:if>
                      <s:else>
                          <td class="field"><input type="checkbox"
                            name='<s:property value="#pd.name"/>'></td>
                      </s:else>
                  </s:elseif>
                  
                  <%-- if it's something we don't understand then use textbox --%>
                  <s:else>
                    <td class="field"><input type="text"
                        name='<s:property value="#pd.name"/>' size="50" /></td>
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
