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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>'/>
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script src="<s:url value='/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js'/>"></script>
<script>
    var contextPath = "${pageContext.request.contextPath}";
    var msg = {
        confirmLabel: '<s:text name="generic.confirm"/>',
        saveLabel: '<s:text name="generic.save"/>',
        cancelLabel: '<s:text name="generic.cancel"/>',
        editTitle: '<s:text name="generic.edit"/>',
        addTitle: '<s:text name="categoryForm.add.title"/>'
    };
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/categories.js'/>"></script>

<p class="subtitle">
    <s:text name="categoriesForm.subtitle">
        <s:param value="weblog"/>
    </s:text>
</p>
<p class="pagetip">
    <s:text name="categoriesForm.rootPrompt"/>
</p>

<input id="refreshURL" type="hidden" value="<s:url action='categories'/>"/>
<input type="hidden" id="actionWeblog" value="<s:property value='%{#parameters.weblog}'/>"/>

<div id="category-list" ng-app="tightBlogApp" ng-controller="CategoryController as ctrl">

    <table class="rollertable">
        <thead>
        <tr>
            <th width="25%"><s:text name="generic.name"/></th>
            <th width="7%"><s:text name="generic.edit"/></th>
            <th width="7%"><s:text name="categoriesForm.remove"/></th>
        </tr>
      </thead>
      <tbody id="tableBody">
        <tr id="{{category.id}}" ng-repeat="category in ctrl.categories | orderBy:'position'" ng-class-even="'altrow'">
          <td class="category-name">{{category.name}}</td>
          <td align="center">
              <a href="#" class="edit-link">
                <img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="icon"/>
              </a>
          </td>
          <td align="center">
              <span ng-if="ctrl.categories.length > 1">
                  <a href="#" class="delete-link">
                      <img src='<s:url value="/images/delete.png"/>' border="0" alt="icon"/>
                  </a>
              </span>
          </td>
        </tr>
      </tbody>
       </table>

      <div class="control clearfix">
          <input type="button" value="<s:text name='categoriesForm.addCategory'/>" id="add-link">
      </div>

</div>

    <div id="category-edit" style="display:none">
      <span id="category-edit-error" style="display:none"><s:text name='categoryForm.error.duplicateName'/></span>
      <label for="name"><s:text name='generic.name'/>:</label>
      <input type="text" id="category-edit-name" class="text ui-widget-content ui-corner-all">
    </div>

    <div id="category-remove" title="<s:text name='categoryDeleteOK.removeCategory'/>" style="display:none">
        <div id="category-remove-mustmove" style="display:none">
            <s:text name='categoryDeleteOK.youMustMoveEntries'/>
            <p>
                <s:text name="categoryDeleteOK.moveToWhere"/>
                <select id="category-remove-targetlist"/>
            </p>
        </div>
    </div>
