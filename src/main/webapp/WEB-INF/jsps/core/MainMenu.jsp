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
var msg= {
    confirmResignationTmpl: '<fmt:message key="mainMenu.confirmResignationTmpl"/>',
    unapprovedCommentsTmpl: '<fmt:message key="mainMenu.haveUnapprovedComments"/>'
};
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/mainmenu.js'/>"></script>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/home'/>"/>

<div ng-cloak>

    <span ng-show="ctrl.roles.length == 0">
        <p><fmt:message key="mainMenu.prompt.noBlog" /></p>
    </span>

    <div>
      <div ng-repeat="role in ctrl.roles">
        <span class="mm_weblog_name"><img src='<c:url value="/images/folder.png"/>' />&nbsp;{{role.weblog.name}}</span>

        <table class="mm_table" width="100%" cellpadding="0" cellspacing="0">
           <tr>
           <td valign="top">

               <table cellpadding="0" cellspacing="0">

                   <tr>
                       <td class="mm_subtable_label"><fmt:message key='mainMenu.weblog'/></td>
                       <td><a href='{{role.weblog.absoluteURL}}' target="_blank">{{role.weblog.absoluteURL}}</a></td>
                   </tr>

                   <tr>
                       <td class="mm_subtable_label"><fmt:message key='generic.description' /></td>
                       <td>{{role.weblog.about}}</td>
                   </tr>

                   <tr>
                       <td class="mm_subtable_label"><fmt:message key='generic.role'/></td>
                       <td ng-switch on="role.weblogRole">
                          <span ng-switch-when="OWNER">OWNER</span>
                          <span ng-switch-when="POST">PUBLISHER</span>
                          <span ng-switch-when="EDIT_DRAFT">CONTRIBUTOR</span>
                       </td>
                   </tr>

                   <tr>
                       <td class="mm_subtable_label"><fmt:message key='mainMenu.emailComments' /></td>
                       <td><input type="checkbox" ng-model="role.emailComments" ng-change="ctrl.toggleEmails(role)"></td>
                   </tr>

                   <tr>
                       <td class="mm_subtable_label"><fmt:message key='mainMenu.todaysHits' /></td>
                       <td>{{role.weblog.hitsToday}}</td>
                   </tr>

               </table>

           </td>

           <td class="mm_table_actions" width="20%" align="left" >

                   <img src='<c:url value="/images/table_edit.png"/>' />
                   <a href="<c:url value='/tb-ui/app/authoring/entryAdd'/>?weblogId={{role.weblog.id}}">
                     <fmt:message key="mainMenu.newEntry" />
                   </a>
                   <br>

                   <%-- Show Entries and Comments links for users above EDIT_DRAFT role --%>
                   <span ng-if="role.weblogRole != 'EDIT_DRAFT'">
                       <c:url var="editEntries" value="/tb-ui/app/authoring/entries"/>
                       <img src='<c:url value="/images/table_multiple.png"/>' />
                       <a href="${editEntries}?weblogId={{role.weblog.id}}"><fmt:message key="mainMenu.editEntries" /></a>
                       <br>

                       <c:url var="manageComments" value="/tb-ui/app/authoring/comments"/>
                       <img src='<c:url value="/images/page_white_edit.png"/>' />
                       <a href="${manageComments}?weblogId={{role.weblog.id}}"><fmt:message key="mainMenu.manageComments" /></a>
                       <span ng-if="role.weblog.unapprovedComments > 0">
                            ({{ctrl.getUnapprovedCommentsString(role.weblog.unapprovedComments)}})
                       </span>
                       <br>
                   </span>

                   <%-- Only admins get access to theme and config settings --%>
                   <span ng-if="role.weblogRole == 'OWNER'">

                       <%-- And only show theme option if custom themes are enabled --%>
                       <c:if test="${usersCustomizeThemes}">
                           <img src='<c:url value="/images/layout.png"/>'>
                           <c:url var="weblogTheme" value="/tb-ui/app/authoring/templates"/>
                           <a href='${weblogTheme}?weblogId={{role.weblog.id}}'><fmt:message key="mainMenu.theme" /></a>
                           <br>
                       </c:if>

                       <img src='<c:url value="/images/cog.png"/>' />
                       <c:url var="manageWeblog" value="/tb-ui/app/authoring/weblogConfig"/>
                       <a href='${manageWeblog}?weblogId={{role.weblog.id}}'><fmt:message key="mainMenu.manage" /></a>
                       <br>
                   </span>

                   <%-- disallow owners from resigning from blog --%>
                   <span ng-if="role.weblogRole != 'OWNER'">
                      <img src='<c:url value="/images/delete.png"/>' />
                      <a href="#" data-toggle="modal" data-target="#resignWeblogModal" data-userrole-id="{{role.id}}"
                            data-weblog-name="{{role.weblog.name}}"><fmt:message key='mainMenu.resign'/></a>
                   </span>
           </td>
           </tr>
        </table>
      </div>
    </div>

    <c:if test="${authenticatedUser.hasEffectiveGlobalRole('BLOGCREATOR')}">
        <form method="link" action="<c:url value='/tb-ui/app/createWeblog'/>">
          <div class="control clearfix">
             <input type="submit" value="<fmt:message key='mainMenu.createWeblog'/>">
          </div>
        </form>
    </c:if>

</div>

<!-- Resign from weblog modal -->
<div class="modal fade" id="resignWeblogModal" tabindex="-1" role="dialog" aria-labelledby="resignWeblogTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="resignWeblogTitle"><fmt:message key="mainMenu.confirmResignation"/></h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <span id="resignWeblogMsg"></span>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" id="resignButton" ng-click="ctrl.resignWeblog($event)"
            data-userrole-id="populatedByJS"><fmt:message key='generic.confirm'/></button>
      </div>
    </div>
  </div>
</div>
