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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsviews/0.9.75/jsviews.min.js"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var authMethod = '<c:out value="${action.getProp('authentication.method')}"/>';
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/profile.js'/>"></script>

<div id="errorMessageDiv" class="errors" style="display:none">
  <script id="errorMessageTemplate" type="text/x-jsrender">
  <b>{{:errorMessage}}</b>
  <ul>
     {{for errors}}
     <li>{{>#data}}</li>
     {{/for}}
  </ul>
  </script>
</div>

<div id="errorMessageNoLDAPAuth" class="errors" style="display:none">
  <span><fmt:message key="Register.error.ldap.notauthenticated"/></span>
</div>

<div class="ldapok">

<div id="successMessageDiv" class="messages" style="display:none">
  <s:if test="authenticatedUser != null">
    <p><fmt:message key="generic.changes.saved"/></p>
  </s:if>
  <s:else>
    <p><fmt:message key="welcome.accountCreated" /></p>
    <p><a id="a_clickHere" href="<s:url action='login-redirect'/>" ><fmt:message key="welcome.clickHere" /></a>
    <fmt:message key="welcome.toLoginAndPost" /></p>
  </s:else>
</div>

<div id="successMessageNeedActivation" class="messages" style="display:none">
  <p><fmt:message key="welcome.accountCreated" /></p>
  <p><fmt:message key="welcome.user.account.not.activated" /></p>
</div>

<%-- Below populated for logged-in user profile edit only --%>
<input type="hidden" id="userId" value="<c:out value='${authenticatedUser.id}'/>"/>

<s:if test="authenticatedUser == null">
    <s:set var="usernameTipKey">userRegister.tip.userName</s:set>
    <s:set var="passwordTipKey">userRegister.tip.password</s:set>
    <s:set var="passwordConfirmTipKey">userRegister.tip.passwordConfirm</s:set>
    <s:set var="saveButtonText">userRegister.button.save</s:set>
    <input type="hidden" id="refreshURL" value="<s:url action='register'/>"/>
    <input type="hidden" id="cancelURL" value="${pageContext.request.contextPath}"/>
    <div class="notregistered">
      <p><fmt:message key="userRegister.prompt" /></p>
    </div>
</s:if>
<s:else>
    <s:set var="usernameTipKey">userSettings.tip.username</s:set>
    <s:set var="passwordTipKey">userSettings.tip.password</s:set>
    <s:set var="passwordConfirmTipKey">userSettings.tip.passwordConfirm</s:set>
    <s:set var="saveButtonText">generic.save</s:set>
    <input type="hidden" id="refreshURL" value="<s:url action='profile'/>"/>
    <input type="hidden" id="cancelURL" value="<s:url action='menu'/>"/>
    <p class="subtitle"><fmt:message key="yourProfile.subtitle" /></p>
</s:else>

<s:form id="myForm">

<table class="formtable">
  <tbody id="formBody">
    <script id="formTemplate" type="text/x-jsrender">
      <tr id="recordId" data-id="{{:user.id}}">
          <td class="label"><label for="userName"><fmt:message key="userSettings.username" /></label></td>
          <td class="field">
            <s:if test="getProp('authentication.method') == 'ldap'">
                <strong>{{:user.userName}}</strong>
            </s:if>
            <s:else>
              <s:if test="authenticatedUser == null">
                <input type="text" size="30" minlength="5" maxlength="25" data-link="user.userName" onBlur="this.value=this.value.trim()" required>
              </s:if>
              <s:else>
                <input type="text" size="30" data-link="user.userName" readonly="true">
              </s:else>
            </s:else>
          </td>
          <td class="description"><fmt:message key="${usernameTipKey}" /></td>
      </tr>

      <tr>
          <td class="label"><label for="screenName"><fmt:message key="userSettings.screenname" /></label></td>
          <td class="field"><input type="text" size="30" data-link="user.screenName" onBlur="this.value=this.value.trim()" minlength="3" maxlength="30" required></td>
          <td class="description"><fmt:message key="userRegister.tip.screenName" /></td>
      </tr>

      <tr>
          <td class="label"><label for="emailAddress"><fmt:message key="userSettings.email" /></label></td>
          <td class="field"><input type="email" size="40" data-link="user.emailAddress" onBlur="this.value=this.value.trim()" maxlength="40" required></td>
          <td class="description"><fmt:message key="userAdmin.tip.email" /></td>
      </tr>

      <tr>
          <td class="label"><label for="locale"><fmt:message key="userSettings.locale" /></label></td>
          <td class="field">
              <s:select name="locale" size="1" list="localesList" listValue="displayName" data-link="user.locale" required=""/>
          </td>
          <td class="description"><fmt:message key="userRegister.tip.locale" /></td>
      </tr>

      <s:if test="getProp('authentication.method') == 'db'">
          <tr>
              <td class="label"><label for="passwordText"><fmt:message key="userSettings.password" /></label></td>
              <td class="field">
                  <s:if test="authenticatedUser == null">
                    <input required type="password" size="20" data-link="credentials.passwordText" onBlur="this.value=this.value.trim()" minlength="8" maxlength="20">
                  </s:if>
                  <s:else>
                    <input type="password" size="20" data-link="credentials.passwordText" onBlur="this.value=this.value.trim()" minlength="8" maxlength="20">
                  </s:else>
              </td>
              <td class="description"><fmt:message key="${passwordTipKey}" /></td>
          </tr>
          <tr>
              <td class="label"><label for="passwordConfirm"><fmt:message key="userSettings.passwordConfirm" /></label></td>
              <td class="field">
                  <s:if test="authenticatedUser == null">
                    <input required type="password" size="20" data-link="credentials.passwordConfirm" onBlur="this.value=this.value.trim()" minlength="8" maxlength="20">
                  </s:if>
                  <s:else>
                    <input type="password" size="20" data-link="credentials.passwordConfirm" onBlur="this.value=this.value.trim()" minlength="8" maxlength="20">
                  </s:else>
              </td>
              <td class="description"><fmt:message key="${passwordConfirmTipKey}" /></td>
          </tr>
      </s:if>
    </script>
  </tbody>
</table>

<br />

<div class="notregistered">
<s:submit id="save-link" value="%{getText(#saveButtonText)}" />
<input id="cancel-link" type="button" value="<fmt:message key='generic.cancel'/>"/>
</div>
</s:form>
</div>
