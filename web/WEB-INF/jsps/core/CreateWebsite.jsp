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
<script type="text/javascript">
<!--
function previewImage(theme) {
    <% String ctxPath = request.getContextPath(); %>
    document.preview.src="<%= ctxPath %>/themes/" + theme + "/sm-theme-" + theme + ".png";
}
function cancel() {
    document.createWebsiteForm.method.value="cancel"; 
    document.createWebsiteForm.submit();
}
function handlePreview() {
	previewSpan = document.getElementById("handlePreview");
	var n1 = previewSpan.childNodes[0];
    var n2 = document.createTextNode(document.createWebsiteForm.handle.value);
    if (n1 == null) {
	    previewSpan.appendChild(n2);
    } else {
	    previewSpan.replaceChild(n2, n1);
    }
}
-->
</script>

<p class="subtitle"><fmt:message key="createWebsite.prompt" /></p>

<br /> 
<html:form action="/roller-ui/createWebsite" method="post" focus="handle">
<input type="hidden" name="method" value="save"></input> 

<table class="formtable">

<tr>
    <td class="label"><label for="name" /><fmt:message key="createWebsite.name" /></label></td>
    <td class="field"><html:text property="name" size="30" maxlength="30" /></td>
    <td class="description"><fmt:message key="createWebsite.tip.name" /></td>
</tr>

<tr>
    <td class="label"><label for="description" /><fmt:message key="createWebsite.description" /></td>
    <td class="field"><html:text property="description" size="30" maxlength="30" /></td>
    <td class="description"><fmt:message key="createWebsite.tip.description" /></td>
</tr>

<tr>
    <td class="label"><label for="handle" /><fmt:message key="createWebsite.handle" /></label></td>
    <td class="field">
        <html:text property="handle" size="30" maxlength="30" onkeyup="handlePreview()" /><br />
        <span style="text-size:70%">
            <fmt:message key="createWebsite.weblogUrl" />:&nbsp;
            <c:out value="${model.absoluteURL}" />/<span id="handlePreview" style="color:red">handle</span>
        </span>
    </td>
    <td class="description"><fmt:message key="createWebsite.tip.handle" /></td>
</tr>

<tr>
    <td class="label"><label for="emailAddress" /><fmt:message key="createWebsite.emailAddress" /></label></td>
    <td class="field"><html:text property="emailAddress" size="40" maxlength="50" /></td>
    <td class="description"><fmt:message key="createWebsite.tip.email" /></td>
</tr>

<tr>
    <td class="label"><label for="locale" /><fmt:message key="createWebsite.locale" /></label></td>
    <td class="field">
       <html:select property="locale" size="1" >
          <html:options collection="locales" property="value" labelProperty="label"/>
       </html:select>    
    </td>
    <td class="description"><fmt:message key="createWebsite.tip.locale" /></td>
</tr>

<tr>
    <td class="label"><label for="timeZone" /><fmt:message key="createWebsite.timeZone" /></label></td>
    <td class="field">
       <html:select property="timeZone" size="1" >
           <html:options collection="timeZones" property="value" labelProperty="label"/>
       </html:select>
    </td>
    <td class="description"><fmt:message key="createWebsite.tip.timezone" /></td>
</tr>

<tr>
    <td class="label"><label for="theme" /><fmt:message key="createWebsite.theme" /></label></td>
    <td class="field">
       <html:select property="theme" size="1" onchange="previewImage(this[selectedIndex].value)">
           <html:options name="model" property="themes" />
       </html:select>
       <br />
       <br />
       <img name="preview" src='<%= request.getContextPath() %>/themes/<c:out value="${model.themes[0]}"/>/sm-theme-<c:out value="${model.themes[0]}" />.png' />
    </td>
    <td class="description"><fmt:message key="createWebsite.tip.theme" /></td>
</tr>
</table>

<br />
   
<input type="submit" value='<fmt:message key="createWebsite.button.save" />'></input>
<input type="button" value='<fmt:message key="createWebsite.button.cancel" />' onclick="cancel()"></input>
    
</html:form>




