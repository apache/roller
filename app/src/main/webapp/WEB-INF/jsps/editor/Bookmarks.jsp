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
<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="<c:url value='/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js'/>"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.6.1/angular.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var msg= {
        confirmLabel: '<fmt:message key="generic.confirm"/>',
        saveLabel: '<fmt:message key="generic.save"/>',
        cancelLabel: '<fmt:message key="generic.cancel"/>',
        editTitle: '<fmt:message key="generic.edit"/>',
        addTitle: '<fmt:message key="bookmarks.add.title"/>'
    };
    var actionWeblogId = "<c:out value='${param.weblogId}'/>";
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/bookmarks.js'/>"></script>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/bookmarks'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<p class="subtitle">
    <fmt:message key="bookmarks.subtitle" >
        <fmt:param value="${actionWeblog.handle}"/>
    </fmt:message>
</p>

<p class="pagetip">
    <fmt:message key="bookmarks.rootPrompt" />
</p>

<table class="rollertable">
    <thead>
      <tr>
          <th width="5%"><input name="control" type="checkbox" onclick="toggleFunction(this.checked,'idSelections');"
              title="<fmt:message key='bookmarks.selectAllLabel'/>"/></th>
          <th width="25%"><fmt:message key="generic.name" /></th>
          <th width="25%"><fmt:message key="bookmarks.url" /></th>
          <th width="35%"><fmt:message key="generic.description" /></th>
          <th width="5%"><fmt:message key="generic.edit" /></th>
          <th width="5%"><fmt:message key="bookmarks.visitLink" /></th>
      </tr>
    </thead>
    <tbody id="tableBody">
      <tr ng-repeat="item in ctrl.items | orderBy:'position'" ng-class-even="'altrow'">
        <td class="center" style="vertical-align:middle">
            <input type="checkbox" name="idSelections" ng-attr-title="checkbox for {{item.name}}" value="{{item.id}}" />
        </td>
        <td>{{item.name}}</td>
        <td>{{item.url}}</td>
        <td>{{item.description}}</td>
        <td align="center">
            <a update-dialog="edit-dialog" ng-click="ctrl.editItem(item)">
                <img src='<c:url value="/images/page_white_edit.png"/>' border="0" alt="icon"
                     title="<fmt:message key='generic.edit'/>"/>
            </a>
        </td>
        <td align="center">
            <a ng-href="{{item.url}}" target="_blank">
                <img src='<c:url value="/images/world_go.png"/>' border="0" alt="icon" title="<fmt:message key='bookmarks.visitLink.tip' />" />
            </a>
        </td>
    </tr>
  </tbody>
</table>

<div class="control clearfix">
    <input type="button" update-dialog="edit-dialog" ng-click="ctrl.addItem()" value="<fmt:message key='bookmarks.addBookmark'/>">

    <span ng-if="ctrl.items.length > 0">
        <input confirm-delete-dialog="confirm-delete" type="button" value="<fmt:message key='bookmarks.delete'/>">
    </span>
</div>

<div id="confirm-delete" title="<fmt:message key='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key='bookmarks.delete.confirm' /></p>
</div>

<div id="edit-dialog" style="display:none">
    <span ng-show="ctrl.showUpdateErrorMessage">
        <fmt:message key='bookmarks.error.duplicateName'/>
    </span>
    <p class="pagetip">
        <fmt:message key="bookmarks.requiredFields">
            <fmt:param><fmt:message key="generic.name"/></fmt:param>
            <fmt:param><fmt:message key="bookmarks.url"/></fmt:param>
        </fmt:message>
    </p>
    <form>
    <table>
        <tr>
            <td style="width:30%"><label for="name"><fmt:message key='generic.name'/></label></td>
            <td><input id="name" ng-model="ctrl.itemToEdit.name" maxlength="80" size="50"/></td>
        </tr>
        <tr>
            <td><label for="url"><fmt:message key='bookmarks.url'/></label></td>
            <td><input id="url" ng-model="ctrl.itemToEdit.url" maxlength="128" size="50"/></td>
        </tr>
        <tr>
            <td><label for="description"><fmt:message key='generic.description'/></label></td>
            <td><input id="description" ng-model="ctrl.itemToEdit.description" maxlength="128" size="50"/></td>
        </tr>
    </table>
    </form>
</div>
