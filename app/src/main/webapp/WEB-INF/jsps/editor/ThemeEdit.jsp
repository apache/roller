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
<script src='<c:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.6.1/angular.min.js"></script>

<script>
var contextPath = "${pageContext.request.contextPath}";
var weblogHandle = "<c:out value='${actionWeblog.handle}'/>";
var currentTheme = "<c:out value='${actionWeblog.theme}'/>";
var weblogId = "<c:out value='${weblogId}'/>";
var msg= {
  confirmLabel: '<fmt:message key="generic.confirm"/>',
  cancelLabel: '<fmt:message key="generic.cancel"/>'
};
var templatePageUrl = "<c:url value='/tb-ui/authoring/templates.rol'/>?weblogId=" + weblogId;
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/themeedit.js'/>"></script>

<div id="errorMessageDiv" class="errors" ng-show="ctrl.errorObj.errorMessage" ng-cloak>
    <p>{{ctrl.errorObj.errorMessage}}</p>
    <ul>
       <li ng-repeat="item in ctrl.errorObj.errors">{{item}}</li>
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
            <img ng-src="{{ctrl.metadata.relativeSiteURL}}{{ctrl.metadata.sharedThemeMap[ctrl.selectedTheme].previewPath}}"/>
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
            <button confirm-switch-dialog="confirm-switch"><fmt:message key='themeEdit.save' /></button>
        </span>
    </div>

</div>

<div id="confirm-switch" title="<fmt:message key='themeEdit.confirmTitle'/>" style="display:none">
    <fmt:message key="themeEdit.youSure"/>
    <br>
    <br>
    <span class="warning">
        <fmt:message key="themeEdit.switchWarning" />
    </span>
</div>
