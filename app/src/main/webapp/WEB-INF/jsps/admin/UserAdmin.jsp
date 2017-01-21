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
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/useradmin.js'/>"></script>

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

<input type="hidden" id="refreshURL" value="<s:url action='userAdmin'/>"/>

<div id="pendingList">
  <script id="pendingTemplate" type="text/x-jsrender">
    <span id="{{:id}}" style="color:red">New registration request: {{:screenName}} ({{:emailAddress}}):
    <input class="approve-button" type="button" value="<s:text name='yourWebsites.accept' />">
    <input class="decline-button" type="button" value="<s:text name='yourWebsites.decline' />"><br></span>
  </script>
</div>

<p class="subtitle"><s:text name="userAdmin.subtitle" /></p>
<span id="userEdit"><select id="useradmin-select-user"></select>
<input id="select-user" type="button" style="margin:4px" value='<s:text name="generic.edit" />'/></span>

<s:form id="myForm" action="userAdmin">
    <table class="formtable">
      <tbody id="formBody">
        <script id="formTemplate" type="text/x-jsrender">
          <tr id="recordId" data-id="{{:user.id}}">
              <td class="label"><label for="userName"><s:text name="userSettings.username" /></label></td>
              <td class="field">
                <input type="text" size="30" maxlength="30" data-link="user.userName" readonly="true" cssStyle="background: #e5e5e5">
              </td>
              <td class="description">
                <s:text name="userSettings.tip.username" />
              </td>
          </tr>

          <tr>
              <td class="label"><label for="dateCreated"><s:text name="userSettings.accountCreateDate" /></label></td>
              <td class="field"><input type="text" size="30" value="{{:~formatDate(user.dateCreated)}}" readonly></td>
              <td class="description"></td>
          </tr>
          <tr>
              <td class="label"><label for="lastLogin"><s:text name="userSettings.lastLogin" /></label></td>
              <td class="field"><input type="text" size="30" value="{{:~formatDate(user.lastLogin)}}" readonly></td>
              <td class="description"></td>
          </tr>

          <tr>
              <td class="label"><label for="screenName"><s:text name="userSettings.screenname" /></label></td>
              <td class="field"><input type="text" size="30" data-link="user.screenName" onBlur="this.value=this.value.trim()" minlength="3" maxlength="30" required></td>
              <td class="description"><s:text name="userAdmin.tip.screenName" /></td>
          </tr>

          <tr>
              <td class="label"><label for="emailAddress"><s:text name="userSettings.email" /></label></td>
              <td class="field"><input type="email" size="40" data-link="user.emailAddress" onBlur="this.value=this.value.trim()" maxlength="40" required></td>
              <td class="description"><s:text name="userAdmin.tip.email" /></td>
          </tr>

          <s:if test="getProp('authentication.method') == 'db'">
              <tr>
                  <td class="label"><label for="passwordText"><s:text name="userSettings.password" /></label></td>
                  <td class="field">
                  <input type="password" size="20" data-link="credentials.passwordText" onBlur="this.value=this.value.trim()" minlength="8" maxlength="20"></td>
                  <td class="description"><s:text name="userAdmin.tip.password" /></td>
              </tr>
              <tr>
                  <td class="label"><label for="passwordConfirm"><s:text name="userSettings.passwordConfirm" /></label></td>
                  <td class="field">
                  <input type="password" size="20" data-link="credentials.passwordConfirm" onBlur="this.value=this.value.trim()" minlength="8" maxlength="20"></td>
                  <td class="description"><s:text name="userRegister.tip.passwordConfirm" /></td>
              </tr>
          </s:if>

          <tr>
              <td class="label"><label for="locale"><s:text name="userSettings.locale" /></label></td>
              <td class="field">
                  <s:select name="locale" size="1" list="localesList" listValue="displayName" data-link="user.locale" required=""/>
              </td>
              <td class="description"><s:text name="userAdmin.tip.locale" /></td>
          </tr>

          <tr>
              <td class="label"><label for="userStatus"><s:text name="userAdmin.userStatus" /></label></td>
              <td class="field">
                  <s:select name="status" size="1" list="userStatusList" listKey="left" listValue="right" data-link="user.status" required=""/>
              </td>
              <td class="description"><s:text name="userAdmin.tip.userStatus" /></td>
          </tr>

          <tr>
              <td class="label"><label for="globalRole"><s:text name="userAdmin.globalRole" /></label></td>
              <td class="field">
                  <s:select name="globalRole" size="1" list="assignableGlobalRolesList" listKey="left" listValue="right" data-link="user.globalRole" required=""/>
              </td>
              <td class="description"><s:text name="userAdmin.tip.globalRole" /></td>
          </tr>
        </script>
      </tbody>
    </table>

    <br>

    <div class="showinguser" style="display:none">
        <p><s:text name="userAdmin.userMemberOf"/></p>
        <table class="rollertable">
          <thead>
            <tr>
                <th style="width:30%"><s:text name="generic.weblog" /></th>
                <th style="width:10%"><s:text name="userAdmin.pending" /></th>
                <th style="width:10%"><s:text name="generic.role" /></th>
                <th style="width:25%"><s:text name="generic.edit" /></th>
                <th width="width:25%"><s:text name="userAdmin.manage" /></th>
            </tr>
          </thead>
          <tbody id="tableBody">
            <script id="tableTemplate" type="text/x-jsrender">
              <tr id="{{:id}}">
                  <td>
                      <a href='{{:weblog.absoluteURL}}'>
                          {{:weblog.name}} [{{:weblog.handle}}]
                      </a>
                  </td>
                  <td>
                      {{:pending}}
                  </td>
                  <td>
                      {{:weblogRole}}
                  </td>
                  <td>
                      <s:url var="editEntries" action="entries" namespace="/tb-ui/authoring">
                          <s:param name="weblogId" value="{{:weblog.id}}" />
                      </s:url>
                      <img src='<s:url value="/images/page_white_edit.png"/>' />
                      <a href='<s:property value="editEntries" />?weblogId={{:weblog.id}}'>
                      <s:text name="userAdmin.editEntries" /></a>
                  </td>
                  <td>
                      <s:url var="manageWeblog" action="weblogConfig" namespace="/tb-ui/authoring">
                          <s:param name="weblogId" value="{{:weblog.id}}" />
                      </s:url>
                      <img src='<s:url value="/images/page_white_edit.png"/>' />
                      <a href='<s:property value="manageWeblog"/>?weblogId={{:weblog.id}}'>
                      <s:text name="userAdmin.manage" /></a>
                  </td>
              </tr>
            </script>
          </tbody>
        </table>
    </div>

    <br>
    <br>

    <div class="control">
        <s:submit id="save-link" value="%{getText('generic.save')}" />
        <input id="cancel-link" type="button" value="<s:text name='generic.cancel'/>"/>
    </div>

</s:form>
