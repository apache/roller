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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="<s:url value='/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsviews/0.9.75/jsviews.min.js"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var weblogHandle = "<c:out value='${actionWeblog.handle}'/>";
var msg= {
  confirmLabel: '<fmt:message key="generic.confirm"/>',
  cancelLabel: '<fmt:message key="generic.cancel"/>'
};
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/themeedit.js'/>"></script>

<div id="success-message" class="messages" style="display:none">
	<ul>
        <li><span class="textSpan"></span></li>
	</ul>
</div>

<div id="failure-message" class="errors" style="display:none">
  <script id="errorMessageTemplate" type="text/x-jsrender">
  <b>{{:errorMessage}}</b>
  <ul>
     {{for errors}}
     <li>{{>#data}}</li>
     {{/for}}
  </ul>
  </script>
  <span class="textSpan"></span>
</div>

<p class="subtitle">
   <fmt:message key="themeEditor.subtitle" >
       <fmt:param value="${actionWeblog.handle}"/>
   </fmt:message>
</p>

<input type="hidden" id="recordId" value="<c:out value='${param.weblogId}'/>"/>
<input type="hidden" id="refreshURL" value="<s:url action='themeEdit'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<s:form id="themeForm" action="templates">
    <sec:csrfInput/>
    <s:hidden name="weblogId"/>

    <div class="optioner">
        <p>
            <fmt:message key="themeEditor.yourCurrentTheme" />:
            <b><c:out value="${actionWeblog.theme}"/></b>
        </p>
    </div>

    <div class="optioner" ng-app="themeSelectModule" ng-controller="themeController">
        <p>
            <select id="themeSelector" name="selectedThemeId" size="1"
            ng-model="selectedTheme" ng-options="theme as theme.name for theme in themes track by theme.id"></select>
        </p>

        <p>{{ selectedTheme.description }}</p>
        <p>
            <img ng-src="<c:out value='${siteURL}'/>{{ selectedTheme.previewPath }}"/>
        </p>
        <p>
            <fmt:message key="themeEditor.previewDescription" />
        </p>
        <p>
          <span class="warning">
              <fmt:message key="themeEditor.switchWarning" />
          </span>
        </p>
    </div>

    <div class="control">
        <span style="padding-left:7px">
            <input type="button" name="themePreview"
                            value="<fmt:message key='themeEditor.preview' />"
                            onclick="fullPreview($('#themeSelector').get(0))" />

            <input type="button" id="update-button" value="<fmt:message key='themeEditor.save' />" />
        </span>
    </div>

</s:form>

<div id="confirm-switch" title="<fmt:message key='themeEditor.confirmTitle'/>" style="display:none">
    <fmt:message key="themeEditor.youSure"/>
    <br>
    <br>
    <span class="warning">
        <fmt:message key="themeEditor.switchWarning" />
    </span>
</div>

<%-- initializes the chooser/optioner/themeImport display at page load time --%>
<script>
    angular.module('themeSelectModule', [])
        .controller('themeController', ['$scope', function($scope) {
            var currentWeblog = $('#actionweblog').val();
            var myUrl = "<s:url value='/tb-ui/authoring/rest/themes/'/><c:out value='${actionWeblog.theme}'/>"
            $.ajax({ url: myUrl, async:false,
                success: function(data) { $scope.themes = data; }
            });
            $scope.selectedTheme = $scope.themes[0];
    }]);
</script>
