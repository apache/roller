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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var msg= {
    confirmLabel: '<fmt:message key="generic.confirm"/>',
    saveLabel: '<fmt:message key="generic.save"/>',
    cancelLabel: '<fmt:message key="generic.cancel"/>',
    editTitle: '<fmt:message key="generic.edit"/>',
    addTitle: '<fmt:message key="bookmarkForm.add.title"/>'
};
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/bookmarks.js'/>"></script>

<p class="subtitle">
    <fmt:message key="bookmarksForm.subtitle" >
        <fmt:param value="${actionWeblog.handle}"/>
    </fmt:message>
</p>
<p class="pagetip">
    <fmt:message key="bookmarksForm.rootPrompt" />
</p>

<input id="refreshURL" type="hidden" value="<s:url action='bookmarks'/>?weblogId=<c:out value='${param.weblogId}'/>"/>
<input type="hidden" id="actionWeblogId" value="<c:out value='${param.weblogId}'/>"/>

<div id="bookmark-list" ng-app="BookmarkApp" ng-controller="BookmarkController as ctrl">

    <table class="rollertable">

        <thead>
          <tr>
              <th width="5%"><input name="control" type="checkbox" onclick="toggleFunction(this.checked,'selectedBookmarks');"
                  title="<fmt:message key="bookmarksForm.selectAllLabel"/>"/></th>
              <th width="25%"><fmt:message key="generic.name" /></th>
              <th width="25%"><fmt:message key="bookmarksForm.url" /></th>
              <th width="35%"><fmt:message key="generic.description" /></th>
              <th width="5%"><fmt:message key="generic.edit" /></th>
              <th width="5%"><fmt:message key="bookmarksForm.visitLink" /></th>
          </tr>
        </thead>
        <tbody id="tableBody">
          <tr id="{{bookmark.id}}" ng-repeat="bookmark in ctrl.bookmarks | orderBy:'position'" ng-class-even="'altrow'">
            <td class="center" style="vertical-align:middle">
                <input type="checkbox" name="selectedBookmarks" title="checkbox for {{bookmark.name}}" value="{{bookmark.id}}" />
            </td>
            <td class="bookmark-name">{{bookmark.name}}</td>
            <td class="bookmark-url">{{bookmark.url}}</td>
            <td class="bookmark-description">{{bookmark.description}}</td>
            <td align="center">
                <a href="#" class="edit-link"><img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="icon"
                         title="<fmt:message key='bookmarksForm.edit.tip' />"/></a>
            </td>
            <td align="center">
                <a href="{{bookmark.url}}" target="_blank">
                    <img src='<s:url value="/images/world_go.png"/>' border="0" alt="icon" title="<fmt:message key='bookmarksForm.visitLink.tip' />" />
                </a>
            </td>
        </tr>
      </tbody>
    </table>

    <div class="control clearfix">
        <input id="add-link" type="button" value="<fmt:message key='bookmarksForm.addBookmark'/>">

        <span ng-if="ctrl.bookmarks.length > 0">
            <input id="delete-link" type="button" value="<fmt:message key='bookmarksForm.delete'/>">
        </span>
    </div>

</div>

<div id="confirm-delete" title="<fmt:message key='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key='bookmarksForm.delete.confirm' /></p>
</div>

<div id="bookmark-edit" style="display:none">
    <span id="bookmark-edit-error" style="display:none"><fmt:message key='bookmarkForm.error.duplicateName'/></span>
    <p class="pagetip">
        <fmt:message key="bookmarkForm.requiredFields">
            <fmt:param><fmt:message key="generic.name"/></fmt:param>
            <fmt:param><fmt:message key="bookmarkForm.url"/></fmt:param>
        </fmt:message>
    </p>
    <form>
    <table>
        <tr>
            <td style="width:30%"><label for="bookmark-edit-name"><fmt:message key='generic.name'/></label></td>
            <td><input id="bookmark-edit-name" maxlength="80" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
        <tr>
            <td><label for="bookmark-edit-url"><fmt:message key='bookmarkForm.url'/></label></td>
            <td><input id="bookmark-edit-url" maxlength="128" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
        <tr>
            <td><label for="bookmark-edit-description"><fmt:message key='generic.description'/></label></td>
            <td><input id="bookmark-edit-description" maxlength="128" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
    </table>
    </form>
</div>
