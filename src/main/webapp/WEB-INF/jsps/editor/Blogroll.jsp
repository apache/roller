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
    var msg= {
        editTitle: '<fmt:message key="generic.edit"/>',
        addTitle: '<fmt:message key="blogroll.addLink"/>'
    };
    var actionWeblogId = "<c:out value='${param.weblogId}'/>";
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/blogroll.js'/>"></script>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/bookmarks'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<div id="errorMessageDiv" class="alert alert-danger" role="alert" ng-show="ctrl.errorObj.errors" ng-cloak>
    <button type="button" class="close" data-ng-click="ctrl.errorObj.errors = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
    <ul>
       <li ng-repeat="item in ctrl.errorObj.errors">{{item.message}}</li>
    </ul>
</div>

<p class="pagetip">
    <fmt:message key="blogroll.rootPrompt" />
</p>

<table class="table table-sm table-bordered table-striped">
    <thead class="thead-light">
      <tr>
          <th width="5%"><input name="control" type="checkbox" ng-model="ctrl.checkAll"
              ng-disabled="ctrl.items.length == 0" ng-change="ctrl.toggleCheckboxes(ctrl.checkAll)"
              title="<fmt:message key='blogroll.selectAllLabel'/>"/></th>
          <th width="25%"><fmt:message key="blogroll.linkLabel" /></th>
          <th width="25%"><fmt:message key="blogroll.urlHeader" /></th>
          <th width="35%"><fmt:message key="generic.description" /></th>
          <th width="10%"><fmt:message key="generic.edit" /></th>
      </tr>
    </thead>
    <tbody id="tableBody" ng-cloak>
      <tr ng-repeat="item in ctrl.items | orderBy:'position'">
        <td class="center" style="vertical-align:middle">
            <input type="checkbox" name="idSelections" ng-attr-title="checkbox for {{item.name}}"
                ng-model="item.selected" value="{{item.id}}" />
        </td>
        <td>{{item.name}}</td>
        <td><a target="_blank" ng-href="{{item.url}}">{{item.url}}</a></td>
        <td>{{item.description}}</td>
        <td class="buttontd">
            <button class="btn btn-warning" data-action="edit"
               data-toggle="modal" data-target="#editLinkModal" ng-click="ctrl.editItem(item)"><fmt:message key="generic.edit" /></button>
        </td>
    </tr>
  </tbody>
</table>

<div class="control clearfix">
    <input type="button" data-toggle="modal" data-target="#editLinkModal" data-action="add"
        value="<fmt:message key='blogroll.addLink'/>" ng-click="ctrl.addItem()">

    <span ng-if="ctrl.items.length > 0">
        <button ng-disabled="!ctrl.itemsSelected()" data-toggle="modal" data-target="#deleteLinksModal">
            <fmt:message key='generic.deleteSelected'/>
        </button>
    </span>
</div>

<!-- Add/Edit Link modal -->
<div class="modal fade" id="editLinkModal" tabindex="-1" role="dialog" aria-labelledby="editLinkModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="editLinkModalTitle"></h5>
      </div>
      <div class="modal-body">
        <p class="pagetip">
            <fmt:message key="blogroll.requiredFields">
                <fmt:param><fmt:message key="blogroll.linkLabel"/></fmt:param>
                <fmt:param><fmt:message key="blogroll.url"/></fmt:param>
            </fmt:message>
        </p>
        <form>
            <div class="form-group">
                <label for="name" class="col-form-label"><fmt:message key='blogroll.linkLabel'/></label>
                <input type="text" class="form-control" ng-model="ctrl.itemToEdit.name" maxlength="80"/>
            </div>
            <div class="form-group">
                <label for="url" class="col-form-label"><fmt:message key='blogroll.url'/></label>
                <input type="text" class="form-control" ng-model="ctrl.itemToEdit.url" maxlength="128"/>
            </div>
            <div class="form-group">
                <label for="description" class="col-form-label"><fmt:message key='generic.description'/></label>
                <input type="text" class="form-control" ng-model="ctrl.itemToEdit.description" maxlength="128"/>
            </div>
        </form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" ng-click="ctrl.inputClear()" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-warning" ng-disabled="!ctrl.itemToEdit.name || !ctrl.itemToEdit.url" id="saveButton" ng-click="ctrl.updateItem()">
            <fmt:message key='generic.save'/>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Delete selected links modal -->
<div class="modal fade" id="deleteLinksModal" tabindex="-1" role="dialog" aria-labelledby="deleteLinksModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteLinksModalTitle"><fmt:message key='generic.confirm.delete'/></h5>
      </div>
      <div class="modal-body">
        <span id="confirmDeleteMsg"><fmt:message key='blogroll.deleteWarning'/></span>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" id="deleteButton" ng-click="ctrl.deleteLinks()">
            <fmt:message key='generic.delete'/>
        </button>
      </div>
    </div>
  </div>
</div>
