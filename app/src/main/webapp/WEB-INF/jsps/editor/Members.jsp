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
<script src='<c:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var weblogId = "<c:out value='${actionWeblog.id}'/>";
</script>
<script src="<c:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/members.js'/>"></script>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/authoring/members.rol'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<p class="subtitle">
    <fmt:message key="memberPermissions.subtitle" >
        <fmt:param value="${actionWeblog.handle}"/>
    </fmt:message>
</p>

<p><fmt:message key="memberPermissions.description" /></p>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">
            <div class="sidebarInner">
            <h3>
                <fmt:message key="memberPermissions.permissionsHelpTitle" />
            </h3>
            <hr size="1" noshade="noshade" />
            <fmt:message key="memberPermissions.permissionHelp" />
		    <br />
		    <br />
        </div>
            </div>
        </div>
    </div>
</div>

<div ng-app="membersApp" ng-controller="MembersController as ctrl">
  <div id="errorMessageDiv" class="errors" style="display:none">
    <b>{{ctrl.errorObj}}</b>
  </div>

  <div id="successMessageDiv" class="messages" style="display:none">
    <s:if test="weblogId != null">
      <p><fmt:message key="generic.changes.saved"/></p>
    </s:if>
  </div>

    <div style="text-align: right; padding-bottom: 6px;">
        <span class="pendingCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
            <fmt:message key="commentManagement.pending" />&nbsp;
    </div>

    <table class="rollertable">
        <thead>
          <tr>
             <th width="20%"><fmt:message key="memberPermissions.userName" /></th>
             <th width="20%"><fmt:message key="memberPermissions.administrator" /></th>
             <th width="20%"><fmt:message key="memberPermissions.author" /></th>
             <th width="20%"><fmt:message key="memberPermissions.limited" /></th>
             <th width="20%"><fmt:message key="memberPermissions.remove" /></th>
          </tr>
        </thead>
        <tbody>
            <tr ng-repeat="role in ctrl.roles" id="{{role.user.id}}" ng-class="{rollertable_pending: role.pending}">
                <td>
                  <img src='<c:url value="/images/user.png"/>' border="0" alt="icon" />
                  {{role.user.userName}}
                </td>
                <td>
                  <input type="radio" ng-model="role.weblogRole" value='OWNER'>
                </td>
                <td>
                  <input type="radio" ng-model="role.weblogRole" value='POST'>
                </td>
                <td>
                  <input type="radio" ng-model="role.weblogRole" value='EDIT_DRAFT'>
                </td>
                <td>
                  <input type="radio" ng-model="role.weblogRole" value='NOBLOGNEEDED'/>
                </td>
           </tr>
       </tbody>
    </table>
    <br />

    <div class="control">
       <input ng-click="ctrl.updateRoles()" type="button" value="<fmt:message key='generic.save'/>" />
    </div>

<br>
<br>

  <p><fmt:message key="inviteMember.prompt" /></p>
  <div>
      <select ng-model="ctrl.userToInvite" size="1" required>
        <option ng-repeat="(key, value) in ctrl.potentialMembers" value="{{key}}">{{value}}</option>
      </select>

      <label for="permissionString" class="formrow" /><fmt:message key="inviteMember.permissions" /></label>

      <input type="radio" ng-model="ctrl.inviteeRole" value="OWNER"  />
      <fmt:message key="inviteMember.administrator" />

      <input type="radio" ng-model="ctrl.inviteeRole" value="POST" />
      <fmt:message key="inviteMember.author" />

      <input type="radio" ng-model="ctrl.inviteeRole" value="EDIT_DRAFT" checked />
      <fmt:message key="inviteMember.limited" /><br><br>

      <input ng-click="ctrl.inviteUser()" type="button" value="<fmt:message key='inviteMember.button.save'/>"/>
  </div>

</div>
