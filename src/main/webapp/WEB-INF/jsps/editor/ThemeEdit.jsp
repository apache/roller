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
<script src='<c:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.7.0/angular.min.js"></script>

<script>
var contextPath = "${pageContext.request.contextPath}";
var weblogHandle = "<c:out value='${actionWeblog.handle}'/>";
var currentTheme = "<c:out value='${actionWeblog.theme}'/>";
var weblogId = "<c:out value='${weblogId}'/>";
var templatePageUrl = "<c:url value='/tb-ui/app/authoring/templates'/>?weblogId=" + weblogId;
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/themeedit.js'/>"></script>

<div id="errorMessageDiv" class="alert alert-danger" role="alert" ng-show="ctrl.errorObj.errors" ng-cloak>
    <button type="button" class="close" data-ng-click="ctrl.errorObj.errors = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
    <ul>
       <li ng-repeat="item in ctrl.errorObj.errors">{{item.message}}</li>
    </ul>
</div>

<p class="subtitle">
   <fmt:message key="themeEdit.subtitle" >
       <fmt:param value="${actionWeblog.handle}"/>
   </fmt:message>
</p>

<input type="hidden" id="weblogId" value="<c:out value='${param.weblogId}'/>"/>
<input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/app/authoring/themeEdit'/>?weblogId=<c:out value='${weblogId}'/>"/>

<div class="formtable">

    <div class="optioner">
        <p>
            <fmt:message key="themeEdit.yourCurrentTheme" />:
            <b><c:out value="${actionWeblog.theme}"/></b>
        </p>
    </div>

    <div class="optioner">
        <p>
            <select ng-model="ctrl.selectedTheme" size="1">
                <option ng-repeat="(key, theme) in ctrl.metadata.sharedThemeMap" value="{{key}}">{{theme.name}}</option>
            </select>
        </p>

        <p>{{ctrl.metadata.sharedThemeMap[ctrl.selectedTheme].description}}</p>
        <p>
            <img ng-src="{{ctrl.metadata.absoluteSiteURL}}{{ctrl.metadata.sharedThemeMap[ctrl.selectedTheme].previewPath}}"/>
        </p>
        <p>
            <fmt:message key="themeEdit.previewDescription" />
        </p>
        <p>
          <span class="warning">
              <fmt:message key="themeEdit.switchWarning" />
          </span>
        </p>
    </div>

    <div class="control">
        <span style="padding-left:7px">
            <button ng-click="ctrl.previewTheme()"><fmt:message key='themeEdit.preview' /></button>
            <button data-toggle="modal" data-target="#switchThemeModal"><fmt:message key='themeEdit.save' /></button>
        </span>
    </div>

</div>

<!-- Switch theme modal -->
<div class="modal fade" id="switchThemeModal" tabindex="-1" role="dialog" aria-labelledby="switchThemeTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="switchThemeTitle"><fmt:message key="themeEdit.confirmSwitch"/></h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
        <span id="confirmSwitchMsg"><fmt:message key="themeEdit.switchWarning" /></span>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" ng-click="ctrl.switchTheme()"><fmt:message key='generic.confirm'/></button>
      </div>
    </div>
  </div>
</div>
