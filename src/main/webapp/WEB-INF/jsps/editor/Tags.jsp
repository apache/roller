<!--
    Copyright 2017 the original author or authors.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>
<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.7.0/angular.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<c:out value='${actionWeblog.id}'/>";
    var msg = {
        replaceTagTitleTmpl: "<fmt:message key='tags.replace.title'/>",
        addTagTitleTmpl: "<fmt:message key='tags.add.title'/>"
    };
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/tags.js'/>"></script>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/tags'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<div id="successMessageDiv" class="alert alert-success" role="alert" ng-show="ctrl.successMessage" ng-cloak>
    {{ctrl.successMessage}}
    <button type="button" class="close" data-ng-click="ctrl.successMessage = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
</div>

<div id="errorMessageDiv" class="alert alert-danger" role="alert" ng-show="ctrl.errorObj.errors" ng-cloak>
    <button type="button" class="close" data-ng-click="ctrl.errorObj.errors = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
    <ul class="list-unstyled">
       <li ng-repeat="item in ctrl.errorObj.errors">{{item.message}}</li>
    </ul>
</div>

<p class="pagetip">
    <fmt:message key="tags.prompt"/>
</p>

<div class="tablenav">

    <span ng-if="ctrl.pageNum > 0 || ctrl.tagsData.hasMore" ng-cloak>
        <center>
            &laquo;
            <input type="button" value="<fmt:message key='weblogEntryQuery.prev'/>"
                ng-disabled="ctrl.pageNum <= 0" ng-click="ctrl.previousPage()">
            |
            <input type="button" value="<fmt:message key='weblogEntryQuery.next'/>"
                ng-disabled="!ctrl.tagsData.hasMore" ng-click="ctrl.nextPage()">
            &raquo;
        </center>
    </span>

    <br>
</div>

<table class="table table-sm  table-bordered table-striped">
    <thead class="thead-light">
        <tr>
            <th width="4%"><input type="checkbox" ng-model="ctrl.checkAll"
                ng-disabled="ctrl.tagData.tags.length == 0" ng-change="ctrl.toggleCheckboxes(ctrl.checkAll)"
                title="<fmt:message key='generic.selectAll'/>"/></th>
            <th width="20%"><fmt:message key="tags.column.tag" /></th>
            <th width="10%"><fmt:message key="categories.column.count" /></th>
            <th width="10%"><fmt:message key="categories.column.firstEntry" /></th>
            <th width="10%"><fmt:message key="categories.column.lastEntry" /></th>
            <th width="14%"></th>
            <th width="13%"></th>
            <th width="13%"></th>
        </tr>
    </thead>
    <tbody>
        <tr ng-repeat="tag in ctrl.tagData.tags" ng-cloak>
            <td class="center" style="vertical-align:middle">
                  <input type="checkbox" name="idSelections" ng-attr-title="checkbox for {{tag.name}}"
                    ng-model="tag.selected" value="{{tag.name}}" />
            </td>
            <td>{{tag.name}}</td>
            <td>{{tag.total}}</td>
            <td>{{ctrl.formatDate(tag.firstEntry)}}</td>
            <td>{{ctrl.formatDate(tag.lastEntry)}}</td>

            <td>
                <a ng-href='{{tag.viewUrl}}' target="_blank"><fmt:message key="tags.column.view" /></a>
            </td>

            <td class="buttontd">
                <button class="btn btn-warning" current-tag="{{tag.name}}" action="replace"
                    current-tag="{{tag.name}}" data-toggle="modal" data-target="#changeTagModal"><fmt:message key="tags.replace" /></button>
            </td>

            <td class="buttontd">
                <button class="btn btn-warning" current-tag="{{tag.name}}" action="add"
                    current-tag="{{tag.name}}" data-toggle="modal" data-target="#changeTagModal"><fmt:message key="generic.add" /></button>
            </td>
        </tr>
    </tbody>
</table>

<div class="control" ng-if="ctrl.tagData.tags.length > 0">
  <span style="padding-left:7px">
    <button ng-disabled="!ctrl.tagsSelected()" data-toggle="modal" data-target="#deleteTagsModal">
        <fmt:message key='generic.deleteSelected'/>
    </button>
  </span>
</div>

<!-- Delete tag modal -->
<div class="modal fade" id="deleteTagsModal" tabindex="-1" role="dialog" aria-labelledby="deleteTagsModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteTagsModalTitle"><fmt:message key='tags.confirmDelete'/></h5>
      </div>
      <div class="modal-body">
        <span id="confirmDeleteMsg"><fmt:message key='tags.deleteWarning'/></span>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" id="deleteButton" ng-click="ctrl.deleteTags()">
            <fmt:message key='generic.delete'/>
        </button>
      </div>
    </div>
  </div>
</div>

<!-- Replace/Add tag modal -->
<div class="modal fade" id="changeTagModal" tabindex="-1" role="dialog" aria-labelledby="changeTagModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="changeTagModalTitle"></h5>
      </div>
      <div class="modal-body">
          <label for="newTag"><fmt:message key='generic.name'/>:</label>
          <input id="newTag" ng-model="ctrl.newTagName" type="text">
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" ng-click="ctrl.inputClear()" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" ng-disabled="!ctrl.newTagName" class="btn btn-warning" id="changeButton" ng-click="ctrl.tagUpdate()"
                action="populatedByJS" data-currentTag="populatedByJS" >
            <fmt:message key='generic.save'/>
        </button>
      </div>
    </div>
  </div>
</div>

<span ng-if="ctrl.tagData.tags.length == 0">
    <fmt:message key="tags.noneFound" />
</span>
