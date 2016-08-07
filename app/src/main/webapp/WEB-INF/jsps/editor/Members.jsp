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
<script src='<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var weblogId = "<s:property value='actionWeblog.id'/>";
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/members.js'/>"></script>

<input id="refreshURL" type="hidden" value="<s:url action='members'/>?weblogId=<s:property value='%{#parameters.weblogId}'/>"/>

<p class="subtitle">
    <s:text name="memberPermissions.subtitle" >
        <s:param value="actionWeblog.handle" />
    </s:text>
</p>

<p><s:text name="memberPermissions.description" /></p>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">
            <div class="sidebarInner">
            <h3>
                <s:text name="memberPermissions.permissionsHelpTitle" />
            </h3>
            <hr size="1" noshade="noshade" />
            <s:text name="memberPermissions.permissionHelp" />
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
      <p><s:text name="generic.changes.saved"/></p>
    </s:if>
  </div>

    <div style="text-align: right; padding-bottom: 6px;">
        <span class="pendingCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
            <s:text name="commentManagement.pending" />&nbsp;
    </div>

    <table class="rollertable">
        <thead>
          <tr>
             <th width="20%"><s:text name="memberPermissions.userName" /></th>
             <th width="20%"><s:text name="memberPermissions.administrator" /></th>
             <th width="20%"><s:text name="memberPermissions.author" /></th>
             <th width="20%"><s:text name="memberPermissions.limited" /></th>
             <th width="20%"><s:text name="memberPermissions.remove" /></th>
          </tr>
        </thead>
        <tbody>
            <tr ng-repeat="role in ctrl.roles" id="{{role.user.id}}" ng-class="{rollertable_pending: role.pending}">
                <td>
                  <img src='<s:url value="/images/user.png"/>' border="0" alt="icon" />
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
       <input ng-click="ctrl.updateRoles()" type="button" value="<s:text name='generic.save'/>" />
    </div>

<br>
<br>

  <p><s:text name="inviteMember.prompt" /></p>
  <div>
      <select ng-model="ctrl.userToInvite" size="1" required>
        <option ng-repeat="(key, value) in ctrl.potentialMembers" value="{{key}}">{{value}}</option>
      </select>

      <label for="permissionString" class="formrow" /><s:text name="inviteMember.permissions" /></label>

      <input type="radio" ng-model="ctrl.inviteeRole" value="OWNER"  />
      <s:text name="inviteMember.administrator" />

      <input type="radio" ng-model="ctrl.inviteeRole" value="POST" />
      <s:text name="inviteMember.author" />

      <input type="radio" ng-model="ctrl.inviteeRole" value="EDIT_DRAFT" checked />
      <s:text name="inviteMember.limited" /><br><br>

      <input ng-click="ctrl.inviteUser()" type="button" value="<s:text name='inviteMember.button.save'/>"/>
  </div>

</div>
