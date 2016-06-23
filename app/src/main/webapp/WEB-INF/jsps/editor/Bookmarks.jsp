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
    confirmLabel: '<s:text name="generic.confirm"/>',
    saveLabel: '<s:text name="generic.save"/>',
    cancelLabel: '<s:text name="generic.cancel"/>',
    editTitle: '<s:text name="generic.edit"/>',
    addTitle: '<s:text name="bookmarkForm.add.title"/>'
};
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/bookmarks.js'/>"></script>

<p class="subtitle">
    <s:text name="bookmarksForm.subtitle" >
        <s:param value="actionWeblog.handle"/>
    </s:text>
</p>
<p class="pagetip">
    <s:text name="bookmarksForm.rootPrompt" />
</p>

<input id="refreshURL" type="hidden" value="<s:url action='bookmarks'/>?weblogId=<s:property value='%{#parameters.weblogId}'/>"/>
<input type="hidden" id="actionWeblogId" value="<s:property value='%{#parameters.weblogId}'/>"/>

<div id="bookmark-list" ng-app="BookmarkApp" ng-controller="BookmarkController as ctrl">

    <table class="rollertable">

        <thead>
          <tr>
              <th width="5%"><input name="control" type="checkbox" onclick="toggleFunction(this.checked,'selectedBookmarks');"
                  title="<s:text name="bookmarksForm.selectAllLabel"/>"/></th>
              <th width="25%"><s:text name="generic.name" /></th>
              <th width="25%"><s:text name="bookmarksForm.url" /></th>
              <th width="35%"><s:text name="generic.description" /></th>
              <th width="5%"><s:text name="generic.edit" /></th>
              <th width="5%"><s:text name="bookmarksForm.visitLink" /></th>
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
                         title="<s:text name='bookmarksForm.edit.tip' />"/></a>
            </td>
            <td align="center">
                <a href="{{bookmark.url}}" target="_blank">
                    <img src='<s:url value="/images/world_go.png"/>' border="0" alt="icon" title="<s:text name='bookmarksForm.visitLink.tip' />" />
                </a>
            </td>
        </tr>
      </tbody>
    </table>

    <div class="control clearfix">
        <input id="add-link" type="button" value="<s:text name='bookmarksForm.addBookmark'/>">

        <span ng-if="ctrl.bookmarks.length > 0">
            <input id="delete-link" type="button" value="<s:text name='bookmarksForm.delete'/>">
        </span>
    </div>

</div>

<div id="confirm-delete" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name='bookmarksForm.delete.confirm' /></p>
</div>

<div id="bookmark-edit" style="display:none">
    <span id="bookmark-edit-error" style="display:none"><s:text name='bookmarkForm.error.duplicateName'/></span>
    <p class="pagetip">
        <s:text name="bookmarkForm.requiredFields">
            <s:param><s:text name="generic.name"/></s:param>
            <s:param><s:text name="bookmarkForm.url"/></s:param>
        </s:text>
    </p>
    <form>
    <table>
        <tr>
            <td style="width:30%"><label for="bookmark-edit-name"><s:text name='generic.name'/></label></td>
            <td><input id="bookmark-edit-name" maxlength="80" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
        <tr>
            <td><label for="bookmark-edit-url"><s:text name='bookmarkForm.url'/></label></td>
            <td><input id="bookmark-edit-url" maxlength="128" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
        <tr>
            <td><label for="bookmark-edit-description"><s:text name='generic.description'/></label></td>
            <td><input id="bookmark-edit-description" maxlength="128" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
    </table>
    </form>
</div>
