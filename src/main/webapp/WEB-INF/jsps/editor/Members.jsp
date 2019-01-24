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
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.7.0/angular.min.js"></script>

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
                <fmt:message key="members.roleDefinitionsTitle" />
            </h3>
            <hr size="1" noshade="noshade" />
            <fmt:message key="members.roleDefinitions" />
		    <br />
		    <br />
        </div>
            </div>
        </div>
    </div>
</div>

<div id="successMessageDiv" class="alert alert-success" role="alert" ng-show="ctrl.messageToShow == 'success'" ng-cloak>
    <fmt:message key="generic.changes.saved"/>
    <button type="button" class="close" data-ng-click="ctrl.messageToShow = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
</div>

<div id="errorMessageDiv" class="alert alert-danger" role="alert" ng-show="ctrl.messageToShow == 'error'" ng-cloak>
    <button type="button" class="close" data-ng-click="ctrl.messageToShow = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
    <b>{{ctrl.errorObj}}</b>
</div>

    <table class="table table-bordered table-hover">
        <thead class="thead-light">
          <tr>
             <th scope="col" width="20%"><fmt:message key="members.userName" /></th>
             <th scope="col" width="20%"><fmt:message key="members.owner" /></th>
             <th scope="col" width="20%"><fmt:message key="members.publisher" /></th>
             <th scope="col" width="20%"><fmt:message key="members.contributor" /></th>
             <th scope="col" width="20%"><fmt:message key="members.remove" /></th>
          </tr>
        </thead>
        <tbody ng-cloak>
            <tr ng-repeat="role in ctrl.roles" id="{{role.user.id}}" ng-class="{pending_member: role.pending}">
                <td>
                  <img src='<c:url value="/images/user.png"/>' border="0" alt="icon" />
                  {{role.user.userName}}
                </td>
                <td>
                  <input type="radio" ng-model="role.weblogRole" value='OWNER'
                        <c:if test="${!userIsAdmin}">disabled</c:if>
                  >
                </td>
                <td>
                  <input type="radio" ng-model="role.weblogRole" value='POST'
                        <c:if test="${!userIsAdmin}">disabled</c:if>
                  >
                </td>
                <td>
                  <input type="radio" ng-model="role.weblogRole" value='EDIT_DRAFT'
                        <c:if test="${!userIsAdmin}">disabled</c:if>
                  >
                </td>
                <td>
                  <input type="radio" ng-model="role.weblogRole" value='NOBLOGNEEDED'
                        <c:if test="${!userIsAdmin}">disabled</c:if>
                  >
                </td>
           </tr>
       </tbody>
    </table>
    <c:if test="${userIsAdmin}">
        <br />

            <div class="control">
               <input ng-click="ctrl.updateRoles()" type="button" value="<fmt:message key='generic.save'/>" />
            </div>

            <br>
            <br>

          <div ng-hide="ctrl.userToAdd" ng-cloak>
               <fmt:message key="members.nobodyToAdd" />
          </div>

          <div ng-show="ctrl.userToAdd" ng-cloak>

              <p><fmt:message key="members.addMemberPrompt" /></p>

              <select ng-model="ctrl.userToAdd" size="1" required>
                <option ng-repeat="(key, value) in ctrl.potentialMembers" value="{{key}}">{{value}}</option>
              </select>

              <fmt:message key="members.roles" />:

              <input type="radio" ng-model="ctrl.userToAddRole" value="OWNER"  />
              <fmt:message key="members.owner" />

              <input type="radio" ng-model="ctrl.userToAddRole" value="POST" />
              <fmt:message key="members.publisher" />

              <input type="radio" ng-model="ctrl.userToAddRole" value="EDIT_DRAFT" checked />
              <fmt:message key="members.contributor" /><br><br>

              <input ng-click="ctrl.addUserToWeblog()" type="button" value="<fmt:message key='generic.add'/>"/>
          </div>
      </c:if>
