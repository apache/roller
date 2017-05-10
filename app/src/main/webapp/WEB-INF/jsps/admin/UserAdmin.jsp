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
<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.6.1/angular.min.js"></script>

<script>
   var contextPath = "${pageContext.request.contextPath}";
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/useradmin.js'/>"></script>

<input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/app/admin/userAdmin'/>"/>

<div id="successMessageDiv" class="messages" ng-show="ctrl.successMessage" ng-cloak>
    <p>{{ctrl.successMessage}}</p>
</div>

<div id="errorMessageDiv" class="errors" ng-show="ctrl.errorObj.errorMessage" ng-cloak>
    <p>{{ctrl.errorObj.errorMessage}}</p>
    <ul>
       <li ng-repeat="item in ctrl.errorObj.errors">{{item}}</li>
    </ul>
</div>

<div id="pendingList">
   <span ng-repeat="item in ctrl.pendingList" style='color:red'>New registration request: {{item.screenName}} ({{item.emailAddress}}):
   <input ng-click="ctrl.approveUser(item.id)" type="button" value="<fmt:message key='mainMenu.accept' />">
   <input ng-click="ctrl.declineUser(item.id)" type="button" value="<fmt:message key='mainMenu.decline' />"><br></span>
</div>

<p class="subtitle"><fmt:message key="userAdmin.subtitle" /></p>
<span id="userEdit">
    <select ng-model="ctrl.userToEdit" size="1">
        <option ng-repeat="(key, value) in ctrl.userList" value="{{key}}">{{value}}</option>
    </select>
    <input ng-click="ctrl.loadUser()" type="button" style="margin:4px" value='<fmt:message key="generic.edit" />'/>
</span>

<table class="formtable" ng-show="ctrl.userBeingEdited">
  <tr>
      <td class="label"><label for="userName"><fmt:message key="userSettings.username" /></label></td>
      <td class="field">
        <input type="text" size="30" maxlength="30" ng-model="ctrl.userBeingEdited.userName" readonly cssStyle="background: #e5e5e5">
      </td>
      <td class="description">
        <fmt:message key="userSettings.tip.username" />
      </td>
  </tr>

  <tr>
      <td class="label"><label for="dateCreated"><fmt:message key="userSettings.accountCreateDate" /></label></td>
      <td class="field">{{ctrl.userBeingEdited.dateCreated | date:'short'}}</td>
      <td class="description"></td>
  </tr>

  <tr>
      <td class="label"><label for="lastLogin"><fmt:message key="userSettings.lastLogin" /></label></td>
      <td class="field">{{ctrl.userBeingEdited.lastLogin | date:'short'}}</td>
      <td class="description"></td>
  </tr>

  <tr>
      <td class="label"><label for="screenName"><fmt:message key="userSettings.screenname" /></label></td>
      <td class="field"><input type="text" size="30" ng-model="ctrl.userBeingEdited.screenName" minlength="3" maxlength="30" required></td>
      <td class="description"><fmt:message key="userAdmin.tip.screenName" /></td>
  </tr>

  <tr>
      <td class="label"><label for="emailAddress"><fmt:message key="userSettings.email" /></label></td>
      <td class="field"><input type="email" size="40" ng-model="ctrl.userBeingEdited.emailAddress" maxlength="40" required></td>
      <td class="description"><fmt:message key="userAdmin.tip.email" /></td>
  </tr>

  <c:if test="${authenticationMethod == 'DB'}">
      <tr>
          <td class="label"><label for="passwordText"><fmt:message key="userSettings.password" /></label></td>
          <td class="field">
          <input type="password" size="20" ng-model="ctrl.userCredentials.passwordText" minlength="8" maxlength="20"></td>
          <td class="description"><fmt:message key="userAdmin.tip.password" /></td>
      </tr>
      <tr>
          <td class="label"><label for="passwordConfirm"><fmt:message key="userSettings.passwordConfirm" /></label></td>
          <td class="field">
          <input type="password" size="20" ng-model="ctrl.userCredentials.passwordConfirm" minlength="8" maxlength="20"></td>
          <td class="description"><fmt:message key="userRegister.tip.passwordConfirm" /></td>
      </tr>
  </c:if>

  <tr>
      <td class="label"><label for="locale"><fmt:message key="userSettings.locale" /></label></td>
      <td class="field">
          <select ng-model="ctrl.userBeingEdited.locale" size="1">
              <option ng-repeat="(key, value) in ctrl.metadata.locales" value="{{key}}">{{value}}</option>
          </select>
      </td>
      <td class="description"><fmt:message key="userAdmin.tip.locale" /></td>
  </tr>

  <tr>
      <td class="label"><label for="userStatus"><fmt:message key="userAdmin.userStatus" /></label></td>
      <td class="field">
          <select ng-model="ctrl.userBeingEdited.status" size="1">
              <option ng-repeat="(key, value) in ctrl.metadata.userStatuses" value="{{key}}">{{value}}</option>
          </select>
      </td>
      <td class="description"><fmt:message key="userAdmin.tip.userStatus" /></td>
  </tr>

  <tr>
      <td class="label"><label for="globalRole"><fmt:message key="userAdmin.globalRole" /></label></td>
      <td class="field">
          <select ng-model="ctrl.userBeingEdited.globalRole" size="1">
              <option ng-repeat="(key, value) in ctrl.metadata.globalRoles" value="{{key}}">{{value}}</option>
          </select>
      </td>
      <td class="description"><fmt:message key="userAdmin.tip.globalRole" /></td>
  </tr>
</table>

<br>

<div class="showinguser" ng-show="ctrl.userBeingEdited">
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
      <tbody>
          <tr ng-repeat="weblogRole in ctrl.userBlogList">
              <td>
                  <a ng-href='{{weblogRole.weblog.absoluteURL}}'>
                      {{weblogRole.weblog.name}} [{{weblogRole.weblog.handle}}]
                  </a>
              </td>
              <td>
                  {{weblogRole.pending}}
              </td>
              <td>
                  {{weblogRole.weblogRole}}
              </td>
              <td>
                  <img src='<c:url value="/images/page_white_edit.png"/>' />
                  <a target="_blank" ng-href="<c:url value='/tb-ui/app/authoring/entries'/>?weblogId={{weblogRole.weblog.id}}">
                      <fmt:message key="userAdmin.editEntries" />
                  </a>
              </td>
              <td>
                  <img src='<c:url value="/images/page_white_edit.png"/>' />
                  <a target="_blank" ng-href="<c:url value='/tb-ui/app/authoring/weblogConfig'/>?weblogId={{weblogRole.weblog.id}}">
                      <fmt:message key="userAdmin.manage" /></a>
                  </a>
              </td>
          </tr>
      </tbody>
    </table>
</div>

<br>
<br>

<div class="control" ng-show="ctrl.userBeingEdited">
    <input class="buttonBox" type="button" value="<fmt:message key='generic.save'/>" ng-click="ctrl.updateUser()"/>
    <input class="buttonBox" type="button" value="<fmt:message key='generic.cancel'/>" ng-click="ctrl.cancelChanges()"/>
</div>
