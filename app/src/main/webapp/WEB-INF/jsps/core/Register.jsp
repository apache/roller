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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsviews/0.9.75/jsviews.min.js"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var authMethod = '<s:property value="getProp('authentication.method')"/>';
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/register.js'/>"></script>

<div id="errorMessageDiv" style="color:red;display:none">
  <script id="errorMessageTemplate" type="text/x-jsrender">
  <b>{{:errorMessage}}</b>
  <ul>
     {{for errors}}
     <li>{{>#data}}</li>
     {{/for}}
  </ul>
  </script>
</div>

<div id="errorMessageNoLDAPAuth" style="color:red;display:none">
  <span>Registration unavailable: LDAP authentication not detected.</span>
</div>

<div class="ldapok">

<div id="successMessageDiv" style="display:none">
  <p><s:text name="welcome.accountCreated" /></p>
  <p><a id="a_clickHere" href="<s:url action='login-redirect'/>" ><s:text name="welcome.clickHere" /></a>
  <s:text name="welcome.toLoginAndPost" /></p>
</div>

<div id="successMessageNeedActivation" style="display:none">
  <p><s:text name="welcome.accountCreated" /></p>
  <p><s:text name="welcome.user.account.not.activated" /></p>
</div>

<input type="hidden" id="refreshURL" value="<s:url action='register'/>"/>
<input type="hidden" id="cancelURL" value="${pageContext.request.contextPath}"/>

<div class="notregistered">
<p><s:text name="userRegister.prompt" /></p>
</div>

<s:form id="myForm" action="register" >
    <s:hidden name="bean.id" />
    <s:hidden name="bean.enabled" />

<table class="formtable">
  <tbody id="formBody">
    <script id="formTemplate" type="text/x-jsrender">
    <tr>
        <td colspan="3">
            <h2><s:text name="userRegister.heading.identification" /></h2>
            <p><s:text name="userRegister.tip.identification" /></p>
        </td>
    </tr>

    <s:if test="getProp('authentication.method') == 'ldap'">
       <tr id="recordId" data-id="{{:id}}">
            <td class="label"><label for="userName"><s:text name="userSettings.username" /></label></td>
            <td class="field"><strong data-link="userName"></strong></td>
            <td class="description"><s:text name="userRegister.tip.userName" /></td>
        </tr>
    </s:if>
    <s:else>
        <tr id="recordId" data-id="{{:id}}">
            <td class="label"><label for="userName"><s:text name="userSettings.username" /></label></td>
            <td class="field">
               <input type="text" size="30" data-link="userName" onBlur="this.value=this.value.trim()" minlength="5" maxlength="20" required>
            </td>
            <td class="description">
               <s:text name="userAdmin.tip.userName" />
            </td>
        </tr>
    </s:else>

    <tr>
        <td class="label"><label for="screenName"><s:text name="userSettings.screenname" /></label></td>
        <td class="field"><input type="text" size="30" data-link="screenName" onBlur="this.value=this.value.trim()" minlength="3" maxlength="30" required></td>
        <td class="description"><s:text name="userRegister.tip.screenName" /></td>
    </tr>

    <tr>
        <td class="label"><label for="emailAddress"><s:text name="userSettings.email" /></label></td>
        <td class="field"><input type="email" size="40" data-link="emailAddress" onBlur="this.value=this.value.trim()" maxlength="40" required></td>
        <td class="description"><s:text name="userAdmin.tip.email" /></td>
    </tr>

    <tr>
        <td class="label"><label for="locale"><s:text name="userSettings.locale" /></label></td>
        <td class="field">
          <s:select name="locale" size="1" list="localesList" listValue="displayName" data-link="locale" required=""/>
        </td>
        <td class="description"><s:text name="userRegister.tip.locale" /></td>
    </tr>

    <s:if test="getProp('authentication.method') == 'db'">
        <tr>
            <td colspan="3">
                <h2><s:text name="userRegister.heading.authentication" /></h2>

                <s:if test="authMethod == 'DATABASE'">
                <p><s:text name="userRegister.tip.enter.password" /></p>
                </s:if>
            </td>
        </tr>

        <tr>
            <td class="label"><label for="passwordText"><s:text name="userSettings.password" /></label></td>
            <td class="field">
                <input type="password" size="20" data-link="password" onBlur="this.value=this.value.trim()" minlength="8" maxlength="20" required></td>
            <td class="description"><s:text name="userAdmin.tip.password" /></td>
        </tr>
        <tr>
            <td class="label"><label for="passwordConfirm"><s:text name="userSettings.passwordConfirm" /></label></td>
            <td class="field">
                <input type="password" size="20" data-link="passwordConfirm" onBlur="this.value=this.value.trim()" minlength="8" maxlength="20" required></td>
            <td class="description"><s:text name="userRegister.tip.passwordConfirm" /></td>
        </tr>
      </s:if>
    </script>
  </tbody>
</table>

<br />

<div class="notregistered">
<h2><s:text name="userRegister.heading.ready" /></h2>

<p id="readytip"><s:text name="userRegister.tip.ready" /></p>

<s:submit id="save-link" value="%{getText('userRegister.button.save')}" />
<input id="cancel-link" type="button" value="<s:text name="generic.cancel"/>"/>
</div>

</s:form>
</div>
