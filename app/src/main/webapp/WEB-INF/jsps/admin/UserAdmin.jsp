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
<%@ taglib uri="/struts-tags" prefix="s" %>
<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsviews/0.9.75/jsviews.min.js"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
</script>
<script src="<c:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/useradmin.js'/>"></script>

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

<input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/admin/userAdmin'/>"/>

<div id="pendingList">
  <script id="pendingTemplate" type="text/x-jsrender">
    <span id="{{:id}}" style="color:red">New registration request: {{:screenName}} ({{:emailAddress}}):
    <input class="approve-button" type="button" value="<fmt:message key='yourWebsites.accept' />">
    <input class="decline-button" type="button" value="<fmt:message key='yourWebsites.decline' />"><br></span>
  </script>
</div>

<p class="subtitle"><fmt:message key="userAdmin.subtitle" /></p>
<span id="userEdit"><select id="useradmin-select-user"></select>
<input id="select-user" type="button" style="margin:4px" value='<fmt:message key="generic.edit" />'/></span>

<s:form id="myForm" action="userAdmin">
    <table class="formtable">
      <tbody id="formBody">
        <script id="formTemplate" type="text/x-jsrender">
          <tr id="recordId" data-id="{{:user.id}}">
              <td class="label"><label for="userName"><fmt:message key="userSettings.username" /></label></td>
              <td class="field">
                <input type="text" size="30" maxlength="30" data-link="user.userName" readonly="true" cssStyle="background: #e5e5e5">
              </td>
              <td class="description">
                <fmt:message key="userSettings.tip.username" />
              </td>
          </tr>

          <tr>
              <td class="label"><label for="dateCreated"><fmt:message key="userSettings.accountCreateDate" /></label></td>
              <td class="field"><input type="text" size="30" value="{{:~formatDate(user.dateCreated)}}" readonly></td>
              <td class="description"></td>
          </tr>
          <tr>
              <td class="label"><label for="lastLogin"><fmt:message key="userSettings.lastLogin" /></label></td>
              <td class="field"><input type="text" size="30" value="{{:~formatDate(user.lastLogin)}}" readonly></td>
              <td class="description"></td>
          </tr>

          <tr>
              <td class="label"><label for="screenName"><fmt:message key="userSettings.screenname" /></label></td>
              <td class="field"><input type="text" size="30" data-link="user.screenName" onBlur="this.value=this.value.trim()" minlength="3" maxlength="30" required></td>
              <td class="description"><fmt:message key="userAdmin.tip.screenName" /></td>
          </tr>

          <tr>
              <td class="label"><label for="emailAddress"><fmt:message key="userSettings.email" /></label></td>
              <td class="field"><input type="email" size="40" data-link="user.emailAddress" onBlur="this.value=this.value.trim()" maxlength="40" required></td>
              <td class="description"><fmt:message key="userAdmin.tip.email" /></td>
          </tr>

          <c:if test="${action.getProp('authentication.method') == 'db'}">
              <tr>
                  <td class="label"><label for="passwordText"><fmt:message key="userSettings.password" /></label></td>
                  <td class="field">
                  <input type="password" size="20" data-link="credentials.passwordText" onBlur="this.value=this.value.trim()" minlength="8" maxlength="20"></td>
                  <td class="description"><fmt:message key="userAdmin.tip.password" /></td>
              </tr>
              <tr>
                  <td class="label"><label for="passwordConfirm"><fmt:message key="userSettings.passwordConfirm" /></label></td>
                  <td class="field">
                  <input type="password" size="20" data-link="credentials.passwordConfirm" onBlur="this.value=this.value.trim()" minlength="8" maxlength="20"></td>
                  <td class="description"><fmt:message key="userRegister.tip.passwordConfirm" /></td>
              </tr>
          </c:if>

          <tr>
              <td class="label"><label for="locale"><fmt:message key="userSettings.locale" /></label></td>
              <td class="field">
                  <s:select name="locale" size="1" list="localesList" listValue="displayName" data-link="user.locale" required=""/>
              </td>
              <td class="description"><fmt:message key="userAdmin.tip.locale" /></td>
          </tr>

          <tr>
              <td class="label"><label for="userStatus"><fmt:message key="userAdmin.userStatus" /></label></td>
              <td class="field">
                  <s:select name="status" size="1" list="userStatusList" listKey="left" listValue="right" data-link="user.status" required=""/>
              </td>
              <td class="description"><fmt:message key="userAdmin.tip.userStatus" /></td>
          </tr>

          <tr>
              <td class="label"><label for="globalRole"><fmt:message key="userAdmin.globalRole" /></label></td>
              <td class="field">
                  <s:select name="globalRole" size="1" list="assignableGlobalRolesList" listKey="left" listValue="right" data-link="user.globalRole" required=""/>
              </td>
              <td class="description"><fmt:message key="userAdmin.tip.globalRole" /></td>
          </tr>
        </script>
      </tbody>
    </table>

    <br>

    <div class="showinguser" style="display:none">
        <p><fmt:message key="userAdmin.userMemberOf"/></p>
        <table class="rollertable">
          <thead>
            <tr>
                <th style="width:30%"><fmt:message key="generic.weblog" /></th>
                <th style="width:10%"><fmt:message key="userAdmin.pending" /></th>
                <th style="width:10%"><fmt:message key="generic.role" /></th>
                <th style="width:25%"><fmt:message key="generic.edit" /></th>
                <th width="width:25%"><fmt:message key="userAdmin.manage" /></th>
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
                      <c:url var="editEntries" value="/tb-ui/authoring/entries.rol">
                          <c:param name="weblogId" value="{{:weblog.id}}" />
                      </c:url>
                      <img src='<c:url value="/images/page_white_edit.png"/>' />
                      <a href='<c:out value="${editEntries}" />?weblogId={{:weblog.id}}'>
                      <fmt:message key="userAdmin.editEntries" /></a>
                  </td>
                  <td>
                      <c:url var="manageWeblog" value="/tb-ui/authoring/weblogConfig.rol">
                          <c:param name="weblogId" value="{{:weblog.id}}" />
                      </c:url>
                      <img src='<c:url value="/images/page_white_edit.png"/>' />
                      <a href='<c:out value="${manageWeblog}"/>?weblogId={{:weblog.id}}'>
                      <fmt:message key="userAdmin.manage" /></a>
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
        <input id="cancel-link" type="button" value="<fmt:message key='generic.cancel'/>"/>
    </div>

</s:form>
