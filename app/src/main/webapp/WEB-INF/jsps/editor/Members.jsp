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
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.6.1/angular.min.js"></script>

<script>
var contextPath = "${pageContext.request.contextPath}";
var weblogId = "<c:out value='${actionWeblog.id}'/>";
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/members.js'/>"></script>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/members'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<p class="subtitle">
    <fmt:message key="members.subtitle" >
        <fmt:param value="${actionWeblog.handle}"/>
    </fmt:message>
</p>

<p><fmt:message key="members.description" /></p>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">
            <div class="sidebarInner">
            <h3>
                <fmt:message key="members.permissionsHelpTitle" />
            </h3>
            <hr size="1" noshade="noshade" />
            <fmt:message key="members.permissionHelp" />
		    <br />
		    <br />
        </div>
            </div>
        </div>
    </div>
</div>

  <div id="errorMessageDiv" class="errors" style="display:none">
    <b>{{ctrl.errorObj}}</b>
  </div>

  <div id="successMessageDiv" class="messages" style="display:none">
    <c:if test="${weblogId != null}">
      <p><fmt:message key="generic.changes.saved"/></p>
    </c:if>
  </div>

    <div style="text-align: right; padding-bottom: 6px;">
        <span class="pendingCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
            <fmt:message key="commentManagement.pending" />&nbsp;
    </div>

    <table class="rollertable">
        <thead>
          <tr>
             <th width="20%"><fmt:message key="members.userName" /></th>
             <th width="20%"><fmt:message key="members.owner" /></th>
             <th width="20%"><fmt:message key="members.publisher" /></th>
             <th width="20%"><fmt:message key="members.contributor" /></th>
             <th width="20%"><fmt:message key="members.remove" /></th>
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

  <div ng-hide="ctrl.userToInvite" ng-cloak>
       <fmt:message key="members.nobodyToInvite" />
  </div>

  <div ng-show="ctrl.userToInvite" ng-cloak>

      <p><fmt:message key="members.inviteMemberPrompt" /></p>

      <select ng-model="ctrl.userToInvite" size="1" required>
        <option ng-repeat="(key, value) in ctrl.potentialMembers" value="{{key}}">{{value}}</option>
      </select>

      <fmt:message key="members.permissions" />:

      <input type="radio" ng-model="ctrl.inviteeRole" value="OWNER"  />
      <fmt:message key="members.owner" />

      <input type="radio" ng-model="ctrl.inviteeRole" value="POST" />
      <fmt:message key="members.publisher" />

      <input type="radio" ng-model="ctrl.inviteeRole" value="EDIT_DRAFT" checked />
      <fmt:message key="members.contributor" /><br><br>

      <input ng-click="ctrl.inviteUser()" type="button" value="<fmt:message key='members.inviteMember'/>"/>
  </div>
