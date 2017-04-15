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
    addTitle: '<fmt:message key="pingTarget.addTarget"/>'
};
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/pingtargets.js'/>"></script>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/admin/pingTargets'/>"/>

<div class="messages" ng-show="ctrl.successMessage" ng-cloak>
    <p>{{ctrl.successMessage}}</p>
</div>

<div class="errors" ng-show="ctrl.errorMessage" ng-cloak>
    <p>{{ctrl.errorMessage}}</p>
</div>

<p class="subtitle">
    <fmt:message key="pingTargets.subtitle" />
</p>

<p><fmt:message key="pingTargets.explanation"/></p>

<table class="rollertable">
  <thead>
    <tr>
        <th width="20%"><fmt:message key="generic.name" /></th>
        <th width="50%"><fmt:message key="pingTarget.pingUrl" /></th>
        <th width="15%" colspan="2"><fmt:message key="pingTarget.autoEnabled" /></th>
        <th width="5%"><fmt:message key="generic.edit" /></th>
        <th width="5%"><fmt:message key="pingTarget.test" /></th>
        <th width="5%"><fmt:message key="pingTarget.remove" /></th>
    </tr>
  </thead>
  <tbody id="tableBody">
      <tr ng-repeat="(key,item) in ctrl.listData" ng-class-even="'altrow'">
        <td>{{item.name}}</td>
        <td>{{item.pingUrl}}</td>
        <td>
           <span style="font-weight: bold;" ng-cloak>
           <span ng-show="item.enabled">
               <fmt:message key="pingTarget.enabled"/>
           </span>
           <span ng-hide="item.enabled">
               <fmt:message key="pingTarget.disabled"/>
           </span>
           </span>
        </td>
        <td class="change-state-cell" align="center">
           <input type="button" ng-click="ctrl.toggleEnabled(item)"
              ng-value="item.enabled ? '<fmt:message key="pingTarget.disable"/>' : '<fmt:message key="pingTarget.enable"/>'"/>
        </td>
        <td align="center">
            <a update-dialog="edit-dialog" ng-click="ctrl.editPingTarget(item)">
                <img src='<c:url value="/images/page_white_edit.png"/>' alt="<fmt:message key='generic.edit'/>" />
            </a>
        </td>
        <td align="center">
            <input type="button" value="<fmt:message key='pingTarget.test'/>" ng-click="ctrl.pingTest(item)"/>
        </td>
        <td align="center">
            <a confirm-delete-dialog="confirm-delete-dialog" id-to-delete="{{item.id}}" name-to-delete="{{item.name}}">
                <img src='<c:url value="/images/delete.png"/>' alt="<fmt:message key='pingTarget.remove'/>" />
            </a>
        </td>
      </tr>
  </tbody>
</table>

<div class="control clearfix">
    <input type="button" update-dialog="edit-dialog" value="<fmt:message key='pingTarget.addTarget'/>"/>
</div>

<div id="confirm-delete-dialog" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="pingTarget.confirmRemove"/></p>
</div>

<div id="edit-dialog" style="display:none">
    <span ng-show="ctrl.showUpdateErrorMessage"><fmt:message key='pingTarget.nameOrUrlNotUnique'/></span>
    <table>
        <input id="pingtarget-edit-id" type="hidden" ng-model="ctrl.pingTargetToEdit.id"/>
        <tr>
            <td style="width:30%"><label for="name"><fmt:message key='generic.name'/></label></td>
            <td><input id="name" ng-model="ctrl.pingTargetToEdit.name" minlength="1" maxlength="40" size="50"/></td>
        </tr>
        <tr>
            <td><label for="url"><fmt:message key='pingTarget.pingUrl'/></label></td>
            <td><input id="url" ng-model="ctrl.pingTargetToEdit.pingUrl" minlength="1" maxlength="128" size="50"/></td>
        </tr>
    </table>
</div>
