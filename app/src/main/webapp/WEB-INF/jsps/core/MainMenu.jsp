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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var msg= {
    yesLabel: '<s:text name="generic.yes"/>',
    noLabel: '<s:text name="generic.no"/>',
    cancelLabel: '<s:text name="generic.cancel"/>',
};
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/mainmenu.js'/>"></script>

<input id="refreshURL" type="hidden" value="<s:url action='menu'/>"/>

<div id="blog-list" ng-app="mainMenuApp" ng-controller="MainMenuController as ctrl">

    <span ng-if="ctrl.roles.length == 0">
        <p><s:text name="yourWebsites.prompt.noBlog" /></p>
    </span>

    <div id="allBlogs">
      <div ng-repeat="role in ctrl.roles | filter:{ pending: 'true' }">
         <span id="{{role.id}}">
           <s:text name="yourWebsites.youAreInvited"/>: {{role.weblog.handle}}
           <input class="accept-button" type="button" value="<s:text name="yourWebsites.accept" />">
           <input class="decline-button" type="button" value="<s:text name="yourWebsites.decline" />">
         </span>
      </div>
      <div ng-repeat="role in ctrl.roles | filter:{ pending: 'false' }">
        <span class="mm_weblog_name"><img src='<s:url value="/images/folder.png"/>' />&nbsp;{{role.weblog.name}}</span>

        <table class="mm_table" width="100%" cellpadding="0" cellspacing="0">
           <tr id="{{role.id}}" data-name="{{role.weblog.name}}">
           <td valign="top">

               <table cellpadding="0" cellspacing="0">

                   <tr>
                       <td class="mm_subtable_label"><s:text name='yourWebsites.weblog'/></td>
                       <td><a href='{{role.weblog.absoluteURL}}'>{{role.weblog.absoluteURL}}</a></td>
                   </tr>

                   <tr>
                       <td class="mm_subtable_label"><s:text name='generic.role'/></td>
                       <td ng-switch on="role.weblogRole">
                          <span ng-switch-when="OWNER">OWNER</span>
                          <span ng-switch-when="POST">PUBLISHER</span>
                          <span ng-switch-when="EDIT_DRAFT">CONTRIBUTOR</span>
                       </td>
                   </tr>

                   <tr>
                       <td class="mm_subtable_label"><s:text name='generic.description' /></td>
                       <td>{{role.weblog.about}}</td>
                   </tr>

                   <tr>
                       <td class="mm_subtable_label"><s:text name='yourWebsites.todaysHits' /></td>
                       <td>{{role.weblog.hitsToday}}</td>
                   </tr>

               </table>

           </td>

           <td class="mm_table_actions" width="20%" align="left" >

                   <img src='<s:url value="/images/table_edit.png"/>' />
                   <a href="<s:url action='entryAdd' namespace='/tb-ui/authoring'/>?weblogId={{role.weblog.id}}">
                     <s:text name="yourWebsites.newEntry" />
                   </a>
                   <br />

                   <%-- Show Entries and Comments links for users above EDIT_DRAFT role --%>
                   <span ng-if="role.weblogRole != 'EDIT_DRAFT'">
                       <s:url var="editEntries" action="entries" namespace="/tb-ui/authoring"/>
                       <img src='<s:url value="/images/table_multiple.png"/>' />
                       <s:a href="%{editEntries}?weblogId={{role.weblog.id}}"><s:text name="yourWebsites.editEntries" /></s:a>
                       <br />

                       <s:url var="manageComments" action="comments" namespace="/tb-ui/authoring"/>
                       <img src='<s:url value="/images/page_white_edit.png"/>' />
                       <s:a href="%{manageComments}?weblogId={{role.weblog.id}}"><s:text name="yourWebsites.manageComments" /></s:a>
                       <br />
                   </span>

                   <%-- Only admins get access to theme and config settings --%>
                   <span ng-if="role.weblogRole == 'OWNER'">

                       <%-- And only show theme option if custom themes are enabled --%>
                       <s:if test="getProp('themes.customtheme.allowed')">
                           <img src='<s:url value="/images/layout.png"/>'>
                           <s:url var="weblogTheme" action="mediaFileView" namespace="/tb-ui/authoring"/>
                           <s:a href='%{weblogTheme}?weblogId={{role.weblog.id}}'><s:text name="yourWebsites.theme" /></s:a>
                           <br />
                       </s:if>

                       <img src='<s:url value="/images/cog.png"/>' />
                       <s:url var="manageWeblog" action="weblogConfig" namespace="/tb-ui/authoring"/>
                       <s:a href='%{manageWeblog}?weblogId={{role.weblog.id}}'>
                           <s:text name="yourWebsites.manage" />
                       </s:a>
                       <br />
                   </span>

                   <%-- don't allow last admin to resign from blog --%>
                   <span ng-if='role.weblogRole != "OWNER"'>
                      <img src='<s:url value="/images/delete.png"/>' />
                      <a href="#" class="resign-link" data-weblog="{{role.weblog.handle}}">
                          <s:text name='yourWebsites.resign'/>
                      </a>
                   </span>
           </td>
           </tr>
        </table>
      </div>
    </div>

    <s:if test="authenticatedUser.hasEffectiveGlobalRole('BLOGCREATOR')">
        <form method="link" action="<s:url action='createWeblog'/>">
          <div class="control clearfix">
             <input type="submit" value="<s:text name='yourWebsites.createWeblog'/>">
          </div>
        </form>
    </s:if>

    <div id="confirm-resign" style="display:none">
        <p>
           <s:text name="yourWebsites.confirmResignation"/>
        </p>
    </div>
</div>
