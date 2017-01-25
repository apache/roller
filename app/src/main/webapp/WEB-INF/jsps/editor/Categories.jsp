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
<link rel="stylesheet" media="all" href='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>'/>
<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script src="<c:url value='/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js'/>"></script>
<script>
    var contextPath = "${pageContext.request.contextPath}";
    var msg = {
        confirmLabel: '<fmt:message key="generic.confirm"/>',
        saveLabel: '<fmt:message key="generic.save"/>',
        cancelLabel: '<fmt:message key="generic.cancel"/>',
        editTitle: '<fmt:message key="generic.edit"/>',
        addTitle: '<fmt:message key="categoryForm.add.title"/>'
    };
</script>
<script src="<c:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/categories.js'/>"></script>

<p class="subtitle">
    <fmt:message key="categoriesForm.subtitle">
        <fmt:param value="${actionWeblog.handle}"/>
    </fmt:message>
</p>
<p class="pagetip">
    <fmt:message key="categoriesForm.rootPrompt"/>
</p>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/authoring/categories.rol'/>?weblogId=<c:out value='${param.weblogId}'/>"/>
<input type="hidden" id="actionWeblogId" value="<c:out value='${param.weblogId}'/>"/>

<div id="category-list" ng-app="tightBlogApp" ng-controller="CategoryController as ctrl">

    <table class="rollertable">
        <thead>
        <tr>
            <th width="25%"><fmt:message key="generic.name"/></th>
            <th width="7%"><fmt:message key="generic.edit"/></th>
            <th width="7%"><fmt:message key="categoriesForm.remove"/></th>
        </tr>
      </thead>
      <tbody id="tableBody">
        <tr id="{{category.id}}" ng-repeat="category in ctrl.categories | orderBy:'position'" ng-class-even="'altrow'">
          <td class="category-name">{{category.name}}</td>
          <td align="center">
              <a href="#" class="edit-link">
                <img src='<c:url value="/images/page_white_edit.png"/>' border="0" alt="icon"/>
              </a>
          </td>
          <td align="center">
              <span ng-if="ctrl.categories.length > 1">
                  <a href="#" class="delete-link">
                      <img src='<c:url value="/images/delete.png"/>' border="0" alt="icon"/>
                  </a>
              </span>
          </td>
        </tr>
      </tbody>
       </table>

      <div class="control clearfix">
          <input type="button" value="<fmt:message key='categoriesForm.addCategory'/>" id="add-link">
      </div>

</div>

    <div id="category-edit" style="display:none">
      <span id="category-edit-error" style="display:none"><fmt:message key='categoryForm.error.duplicateName'/></span>
      <label for="name"><fmt:message key='generic.name'/>:</label>
      <input type="text" id="category-edit-name" class="text ui-widget-content ui-corner-all">
    </div>

    <div id="category-remove" title="<fmt:message key='categoryDeleteOK.removeCategory'/>" style="display:none">
        <div id="category-remove-mustmove" style="display:none">
            <fmt:message key='categoryDeleteOK.youMustMoveEntries'/>
            <p>
                <fmt:message key="categoryDeleteOK.moveToWhere"/>
                <select id="category-remove-targetlist"/>
            </p>
        </div>
    </div>
