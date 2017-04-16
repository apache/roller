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
    var msg = {
        confirmLabel: '<fmt:message key="generic.confirm"/>',
        saveLabel: '<fmt:message key="generic.save"/>',
        cancelLabel: '<fmt:message key="generic.cancel"/>',
        editTitle: '<fmt:message key="generic.edit"/>',
        addTitle: '<fmt:message key="categories.add.title"/>'
    };
    var actionWeblogId = "<c:out value='${param.weblogId}'/>";
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/categories.js'/>"></script>

<p class="subtitle">
    <fmt:message key="categories.subtitle">
        <fmt:param value="${actionWeblog.handle}"/>
    </fmt:message>
</p>

<p class="pagetip">
    <fmt:message key="categories.rootPrompt"/>
</p>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/categories'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

    <table class="rollertable">
        <thead>
        <tr>
            <th width="25%"><fmt:message key="generic.name"/></th>
            <th width="7%"><fmt:message key="generic.edit"/></th>
            <th width="7%"><fmt:message key="categories.remove"/></th>
        </tr>
      </thead>
      <tbody>
          <tr ng-repeat="item in ctrl.items | orderBy:'position'" ng-class-even="'altrow'">
              <td>{{item.name}}</td>
              <td align="center">
                <a edit-dialog="edit-dialog" ng-click="ctrl.setEditItem(item)">
                    <img src='<c:url value="/images/page_white_edit.png"/>' border="0" alt="icon"
                         title="<fmt:message key='generic.edit'/>"/>
                </a>
              </td>
              <td align="center">
                  <span ng-if="ctrl.items.length > 1">
                      <a confirm-delete-dialog="delete-dialog" name-to-delete="{{item.name}}" ng-click="ctrl.setDeleteItem(item)">
                          <img src="<c:url value='/images/delete.png'/>" border="0" alt="icon"
                              title="<fmt:message key='generic.delete'/>"/>
                      </a>
                  </span>
              </td>
          </tr>
      </tbody>
       </table>

    <div class="control clearfix">
        <input type="button" add-dialog="edit-dialog" ng-click="ctrl.addItem()" value="<fmt:message key='categories.addCategory'/>">
    </div>

    <div id="edit-dialog" style="display:none">
        <span ng-show="ctrl.showUpdateErrorMessage">
            <fmt:message key='categories.error.duplicateName'/><br>
        </span>
        <label for="name"><fmt:message key='generic.name'/>:</label>
        <input id="name" ng-model="ctrl.itemToEdit.name" maxlength="80" size="50"/>
    </div>

    <div id="delete-dialog" title="<fmt:message key='categories.deleteRemoveCategory'/>" style="display:none">
        <p>
            <fmt:message key="categories.deleteMoveToWhere"/>
            <select ng-model="ctrl.targetCategoryId" size="1" required>
               <option ng-repeat="item in ctrl.items | filter: { id: '!' + ctrl.itemToDelete.id }" value="{{item.id}}">{{item.name}}</option>
            </select>
        </p>
    </div>
