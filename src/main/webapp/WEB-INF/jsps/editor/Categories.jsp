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
<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.7.0/angular.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var msg = {
        addTitle: '<fmt:message key="categories.add.title"/>',
        editTitleTmpl: '<fmt:message key="categories.renameTitleTmpl"/>',
        confirmDeleteTmpl: '<fmt:message key="categories.deleteCategoryTmpl"/>'
    };
    var actionWeblogId = "<c:out value='${param.weblogId}'/>";
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/categories.js'/>"></script>

<div id="errorMessageDiv" class="alert alert-danger" role="alert" ng-show="ctrl.errorObj.errors" ng-cloak>
    <button type="button" class="close" data-ng-click="ctrl.errorObj.errors = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
    <ul>
       <li ng-repeat="item in ctrl.errorObj.errors">{{item.message}}</li>
    </ul>
</div>

<p class="pagetip">
    <fmt:message key="categories.rootPrompt"/>
</p>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/categories'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

    <table class="table table-sm table-bordered table-striped">
        <thead class="thead-light">
        <tr>
            <th width="20%"><fmt:message key="generic.category"/></th>
            <th width="20%"><fmt:message key="categories.column.count"/></th>
            <th width="20%"><fmt:message key="categories.column.firstEntry"/></th>
            <th width="20%"><fmt:message key="categories.column.lastEntry"/></th>
            <th width="10%"><fmt:message key="generic.rename"/></th>
            <th width="10%"><fmt:message key="generic.delete"/></th>
        </tr>
      </thead>
      <tbody ng-cloak>
          <tr ng-repeat="item in ctrl.items | orderBy:'position'">
              <td>{{item.name}}</td>
              <td>{{item.numEntries}}</td>
              <td>{{ctrl.formatDate(item.firstEntry)}}</td>
              <td>{{ctrl.formatDate(item.lastEntry)}}</td>
              <td class="buttontd">
                  <button class="btn btn-warning" data-category-id="{{item.id}}" data-category-name="{{item.name}}" data-action="rename"
                      data-toggle="modal" data-target="#editCategoryModal"><fmt:message key="generic.rename" /></button>
              </td>
              <td class="buttontd">
                  <span ng-if="ctrl.items.length > 1">
                    <button class="btn btn-danger" data-category-id="{{item.id}}" data-category-name="{{item.name}}"
                        data-toggle="modal" data-target="#deleteCategoryModal"><fmt:message key="generic.delete" /></button>
                  </span>
              </td>
          </tr>
      </tbody>
       </table>

    <div class="control clearfix">
        <input type="button" data-toggle="modal" data-target="#editCategoryModal" data-action="add"
            data-category-id="" value="<fmt:message key='categories.addCategory'/>">
    </div>

<!-- Add/Edit Category modal -->
<div class="modal fade" id="editCategoryModal" tabindex="-1" role="dialog" aria-labelledby="editCategoryModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="editCategoryModalTitle"></h5>
      </div>
      <div class="modal-body">
        <span ng-show="ctrl.showUpdateErrorMessage">
            <fmt:message key='categories.error.duplicateName'/><br>
        </span>
        <label for="category-name"><fmt:message key='generic.name'/>:</label>
        <input id="category-name" ng-model="ctrl.itemToEdit.name" maxlength="80" size="40"/>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" ng-click="ctrl.inputClear()" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-warning" ng-disabled="!ctrl.itemToEdit.name" id="saveButton" ng-click="ctrl.updateItem($event)"
            data-action="populatedByJS" data-category-id="populatedByJS">
            <fmt:message key='generic.save'/>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Delete category modal -->
<div class="modal fade" id="deleteCategoryModal" tabindex="-1" role="dialog" aria-labelledby="deleteCategoryModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteCategoryModalTitle"></h5>
      </div>
      <div class="modal-body">
        <p>
        <!-- | filter: { id: '!' + ctrl.itemToDelete.id } -->
            <fmt:message key="categories.deleteMoveToWhere"/>
            <select ng-model="ctrl.targetCategoryId" size="1" required
                ng-options="item.id as item.name for item in ctrl.items | filter: {id : '!' + ctrl.selectedCategoryId }"
            ></select>
        </p>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" ng-disabled="!ctrl.targetCategoryId" ng-click="ctrl.deleteItem()" id="deleteButton"><fmt:message key='generic.delete'/></button>
      </div>
    </div>
  </div>
</div>
