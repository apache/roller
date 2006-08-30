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
String ctxPath = request.getContextPath();
boolean allowNewUsers = RollerRuntimeConfig.getBooleanProperty("users.registration.enabled");
if (!allowNewUsers && !request.isUserInRole("admin"))
{ %>
    <span style="warning"><fmt:message key="newuser.newUserCreationDisabled" /></span>
<% }
else
{ %>
<script type="text/javascript">
<!--
function cancel() {
    document.userFormEx.method.value="cancel"; 
    document.userFormEx.submit();
}
-->
</script>

<p><fmt:message key="userRegister.prompt" /></p>

<html:form action="/roller-ui/user" method="post" focus="fullName">
    <html:hidden property="method" name="method" value="add"/></input>
    <html:hidden property="id" /></input>
    <html:hidden property="adminCreated" /></input>
    <html:hidden property="enabled" /></input>

<table class="formtable">

<tr>
    <td class="label"><label for="fullName" /><fmt:message key="userSettings.fullname" /></label></td>
    <td class="field"><html:text property="fullName" size="30" maxlength="30" /></td>
    <td class="description"><fmt:message key="userRegister.tip.fullName" /></td>
</tr>

<c:choose>
<c:when test="${userFormEx.dataFromSSO == true}">
<tr>
    <td class="label"><label for="userName" /><fmt:message key="userSettings.username" /></label></td>
    <td class="field"><strong><c:out value="${userFormEx.userName}" /></strong></td>
    <td class="description"><fmt:message key="userRegister.tip.userName" /></td>
</tr>
</c:when>
<c:otherwise>
<tr>
    <td class="label"><label for="userName" /><fmt:message key="userSettings.username" /></label></td>
    <td class="field"><html:text property="userName" size="30" maxlength="30" /></td>
    <td class="description"><fmt:message key="userRegister.tip.userName" /></td>
</tr>
</c:otherwise>
</c:choose>
<c:if test="${userFormEx.dataFromSSO == false}">
<tr>
    <td class="label"><label for="passwordText" /><fmt:message key="userSettings.password" /></label></td>
    <td class="field">
       <html:password property="passwordText" size="20" maxlength="20" />
       <html:hidden property="password" />
   </td>
    <td class="description"><fmt:message key="userRegister.tip.password" /></td>
</tr>

<tr>
    <td class="label"><label for="passwordConfirm" /><fmt:message key="userSettings.passwordConfirm" /></label></td>
    <td class="field"><html:password property="passwordConfirm" size="20" maxlength="20" /></td>
    <td class="description"><fmt:message key="userRegister.tip.passwordConfirm" /></td>
</tr>
</c:if>

<tr>
    <td class="label"><label for="emailAddress" /><fmt:message key="userSettings.email" /></label></td>
    <td class="field"><html:text property="emailAddress" size="40" maxlength="40" /></td>
    <td class="description"><fmt:message key="userRegister.tip.email" /></td>
</tr>

<tr>
    <td class="label"><label for="locale" /><fmt:message key="userSettings.locale" /></label></td>
    <td class="field">
       <html:select property="locale" size="1" >
          <html:options collection="locales" property="value" labelProperty="label"/>
       </html:select>
    </td>
    <td class="description"><fmt:message key="userRegister.tip.locale" /></td>
</tr>
    
<tr>
    <td class="label"><label for="timeZone" /><fmt:message key="userSettings.timeZone" /></label></td>
    <td class="field">
       <html:select property="timeZone" size="1" >
           <html:options collection="timeZones" property="value" labelProperty="label"/>
       </html:select>
    </td>
    <td class="description"><fmt:message key="userRegister.tip.timeZone" /></td>
</tr>
    
</table>
<br />

<input type="submit" value='<fmt:message key="userRegister.button.save" />'></input>
<input type="button" value='<fmt:message key="userSettings.button.cancel" />' onclick="cancel()"></input>
    
</html:form>

<%
} // end allowNewUsers check
%>


