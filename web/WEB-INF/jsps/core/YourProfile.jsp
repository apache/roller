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

<p class="subtitle"><fmt:message key="yourProfile.description" /></p>

<html:form action="/roller-ui/yourProfile" method="post" focus="fullName">
    <input type="hidden" name="method" value="save"></input> 
           
<table class="formtable">

<tr>
    <td class="label"><label for="userName" /><fmt:message key="userSettings.username" /></label></td>
    <td class="field"><html:text style="background: #e5e5e5" property="userName" readonly="true" /></td>
    <td class="description"><fmt:message key="yourProfile.tip.userName" /></td>
</tr>

<tr>
    <td class="label"><label for="fullName" /><fmt:message key="userSettings.fullname" /></label></td>
    <td class="field"><html:text property="fullName" size="30" maxlength="30" /></td>
    <td class="description"><fmt:message key="userRegister.tip.fullName" /></td>
</tr>

<c:if test="${cookieLogin != 'true'}">
    <tr>
        <td class="label"><label for="passwordText" /><fmt:message key="userSettings.password" /></label></td>
        <td class="field">
           <html:password property="passwordText" size="20" maxlength="20" />
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

<input type="submit" value='<fmt:message key="userSettings.button.save" />' ></input>

</html:form>



