<!--
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
-->
<%@ include file="/taglibs.jsp" %>
<%
// this just makes the name for a custom theme available to our jstl EL
String customTheme = org.apache.roller.pojos.Theme.CUSTOM;
request.setAttribute("customTheme", customTheme);

boolean allowCustom = org.apache.roller.config.RollerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed");
request.setAttribute("allowCustom", new Boolean(allowCustom));
%>
<p class="subtitle">
   <fmt:message key="themeEditor.subtitle" >
       <fmt:param value="${model.website.handle}" />
   </fmt:message>
</p>  
<p class="pagetip">
   <fmt:message key="themeEditor.tip" />
</p>

<form action="themeEditor.do" method="post">

    <input type=hidden name="weblog" value='<c:out value="${model.website.handle}" />' />
    <input type=hidden name="method" value="preview" />

    <table width="95%">

        <tr>
            <td>
                <p>
                    <fmt:message key="themeEditor.yourCurrentTheme" />: <b><c:out value="${currentTheme}"/></b><br/>
                    
                    <c:choose>
                        <c:when test="${currentTheme ne previewTheme}" >
                            <fmt:message key="themeEditor.themeBelowIsCalled" /> <b><c:out value="${previewTheme}" /></b><br/>
                            <fmt:message key="themeEditor.savePrompt" /><br/>
                            <input type="button" 
                                value='<fmt:message key="themeEditor.save" />'
                                name="saveButton" 
                                onclick="this.form.method.value='save';this.form.submit()"
                                tabindex="4" />
                            &nbsp;&nbsp;
                            <input type="button" 
                                value='<fmt:message key="themeEditor.cancel" />'
                                name="cancelButton" 
                                onclick="this.form.method.value='edit';this.form.submit()"
                                tabindex="4" />
                        </c:when>
                        
                        <c:when test="${(currentTheme ne customTheme) and allowCustom}">
                            <fmt:message key="themeEditor.youMayCustomize" /><br/>
                            <input type="button" 
                                value='<fmt:message key="themeEditor.customize" />'
                                name="customizeButton" 
                                onclick="this.form.method.value='customize';this.form.submit()"
                                tabindex="4" />
                        </c:when>
                  </c:choose>	
		</p>
            </td>
        </tr>

        <tr>
            <td>&nbsp;</td>
        </tr>

        <tr>
            <td>	
                <p>
                <fmt:message key="themeEditor.selectTheme" /> : 
                <select name="theme" size="1" onchange="this.form.submit()" >
                    <c:forEach var="themeName" items="${themesList}">
                        <c:choose>
                            <c:when test="${themeName eq previewTheme}">
                                <option value="<c:out value="${themeName}"/>" selected>
                                    <c:out value="${themeName}"/>
                                </option>
                            </c:when>
                            <c:otherwise>
                                <option value="<c:out value="${themeName}"/>">
                                    <c:out value="${themeName}"/>
                                </option>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </select>
                </p>
            </td>
        </tr>
	
        <tr>
            <td>
                <iframe name="preview" id="preview" 
                src='<%= request.getContextPath() %>/roller-ui/authoring/preview/<c:out value="${model.website.handle}" />?theme=<c:out value="${previewTheme}"/>' 
                frameborder=1 width="100%" height="400" 
                marginheight="0" marginwidth="0"></iframe>
            </td>
        </tr>
	
    </table>

</form>


<script type="text/javascript">
    <!--
    function save()
    {
    //alert(document.themeEditorForm.method.value);
    document.themeEditorForm.method.value = "save";
    document.themeEditorForm.submit();
    }
    // -->
</script>


