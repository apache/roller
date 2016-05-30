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
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/profile.js'/>"></script>

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

<div id="successMessageDiv" style="display:none">
  <span>Information has been saved.</span>
</div>

<input type="hidden" id="refreshURL" value="<s:url action='profile'/>?id=<s:property value='%{#parameters.id}'/>"/>
<input type="hidden" id="cancelURL" value="<s:url action='menu'/>"/>
<input type="hidden" id="userId" value="<s:property value='%{#parameters.id}'/>"/>

<p class="subtitle"><s:text name="userAdmin.title.editUser" /></p>

<s:form id="myForm" action="profile">
    <table class="formtable">
      <tbody id="formBody">
        <script id="formTemplate" type="text/x-jsrender">
        <tr id="recordId" data-id="{{:id}}">
            <td class="label"><label for="userName"><s:text name="userSettings.username" /></label></td>
            <td class="field">
              <input type="text" size="30" maxlength="30" data-link="userName" readonly="true" cssStyle="background: #e5e5e5">
            </td>
            <td class="description">
              <s:text name="userSettings.tip.username" />
            </td>
        </tr>

        <tr>
            <td class="label"><label for="screenName"><s:text name="userSettings.screenname" /></label></td>
            <td class="field"><input type="text" size="30" data-link="screenName" onBlur="this.value=this.value.trim()" minlength="3" maxlength="30" required></td>
            <td class="description"><s:text name="userAdmin.tip.screenName" /></td>
        </tr>

        <tr>
            <td class="label"><label for="emailAddress"><s:text name="userSettings.email" /></label></td>
            <td class="field"><input type="email" size="40" data-link="emailAddress" onBlur="this.value=this.value.trim()" maxlength="40" required></td>
            <td class="description"><s:text name="userAdmin.tip.email" /></td>
        </tr>

        <s:if test="getProp('authentication.method') == 'db'">
            <tr>
                <td class="label"><label for="passwordText"><s:text name="userSettings.password" /></label></td>
                <td class="field">
                    <input type="password" size="20" data-link="password" onBlur="this.value=this.value.trim()" minlength="8" maxlength="20"></td>
                <td class="description"><s:text name="userAdmin.tip.password" /></td>
            </tr>
            <tr>
                <td class="label"><label for="passwordConfirm"><s:text name="userSettings.passwordConfirm" /></label></td>
                <td class="field">
                    <input type="password" size="20" data-link="passwordConfirm" onBlur="this.value=this.value.trim()" minlength="8" maxlength="20"></td>
                <td class="description"><s:text name="userRegister.tip.passwordConfirm" /></td>
            </tr>
        </s:if>

        <tr>
            <td class="label"><label for="locale"><s:text name="userSettings.locale" /></label></td>
            <td class="field">
                <s:select name="locale" size="1" list="localesList" listValue="displayName" data-link="locale" required=""/>
            </td>
            <td class="description"><s:text name="userAdmin.tip.locale" /></td>
        </tr>
        </script>
      </tbody>
    </table>

    <br />

    <s:submit id="save-link" value="%{getText('generic.save')}" />
    <input id="cancel-link" type="button" value="<s:text name="generic.cancel"/>"/>

</s:form>
