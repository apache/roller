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
<link rel="stylesheet" media="all" href='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<c:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.6.1/angular.min.js"></script>

<script>
var contextPath = "${pageContext.request.contextPath}";
var msg= {
    yesLabel: '<fmt:message key="generic.yes"/>',
    noLabel: '<fmt:message key="generic.no"/>',
    cancelLabel: '<fmt:message key="generic.cancel"/>',
};
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/mainmenu.js'/>"></script>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/home'/>"/>

<div>

    <span ng-show="ctrl.roles.length == 0">
        <p><fmt:message key="mainMenu.prompt.noBlog" /></p>
    </span>

    <div>
      <div ng-repeat="role in ctrl.roles | filter:{ pending: 'true' }">
         <span>
           <fmt:message key="mainMenu.youAreInvited"/>: {{role.weblog.handle}}
           <input type="button" value="<fmt:message key='mainMenu.accept' />" ng-click="ctrl.acceptBlog(role)">
           <input type="button" value="<fmt:message key='mainMenu.decline' />"  ng-click="ctrl.declineBlog(role)">
         </span>
      </div>
      <div ng-repeat="role in ctrl.roles | filter:{ pending: 'false' }">
        <span class="mm_weblog_name"><img src='<c:url value="/images/folder.png"/>' />&nbsp;{{role.weblog.name}}</span>

        <table class="mm_table" width="100%" cellpadding="0" cellspacing="0">
           <tr>
           <td valign="top">

               <table cellpadding="0" cellspacing="0">

                   <tr>
                       <td class="mm_subtable_label"><fmt:message key='mainMenu.weblog'/></td>
                       <td><a href='{{role.weblog.absoluteURL}}'>{{role.weblog.absoluteURL}}</a></td>
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
                       <td class="mm_subtable_label"><fmt:message key='generic.description' /></td>
                       <td>{{role.weblog.about}}</td>
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
                      <a href="#" confirm-resign-dialog="confirm-resign" data-role-id="{{role.id}}"
                                data-weblog-name="{{role.weblog.name}}">
                           <fmt:message key='mainMenu.resign'/>
                      </a>
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

    <div id="confirm-resign" style="display:none">
        <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="mainMenu.confirmResignation"/></p>
    </div>
</div>
