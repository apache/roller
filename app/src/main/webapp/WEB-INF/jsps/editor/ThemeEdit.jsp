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
var weblogHandle = "<s:property value='actionWeblog.handle'/>";
var msg= {
  confirmLabel: '<s:text name="generic.confirm"/>',
  cancelLabel: '<s:text name="generic.cancel"/>'
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
   <s:text name="themeEditor.subtitle" >
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>

<input type="hidden" id="recordId" value="<s:property value='%{#parameters.weblogId}'/>"/>
<input type="hidden" id="refreshURL" value="<s:url action='themeEdit'/>?weblogId=%{#parameters.weblogId}"/>

<s:form id="themeForm" action="templates">
    <sec:csrfInput/>
    <s:hidden name="weblogId"/>

    <div class="optioner">
        <p>
            <s:text name="themeEditor.yourCurrentTheme" />:
            <b><s:property value="actionWeblog.theme"/></b>
        </p>
    </div>

    <div class="optioner" ng-app="themeSelectModule" ng-controller="themeController">
        <p>
            <select id="themeSelector" name="selectedThemeId" size="1"
            ng-model="selectedTheme" ng-options="theme as theme.name for theme in themes track by theme.id"></select>
        </p>

        <p>{{ selectedTheme.description }}</p>
        <p>
            <img ng-src="<s:property value='siteURL'/>{{ selectedTheme.previewPath }}"/>
        </p>
        <p>
            <s:text name="themeEditor.previewDescription" />
        </p>
        <p>
          <span class="warning">
              <s:text name="themeEditor.switchWarning" />
          </span>
        </p>
    </div>

    <div class="control">
        <span style="padding-left:7px">
            <input type="button" name="themePreview"
                            value="<s:text name='themeEditor.preview' />"
                            onclick="fullPreview($('#themeSelector').get(0))" />

            <input type="button" id="update-button" value="<s:text name='themeEditor.save' />" />
        </span>
    </div>

</s:form>

<div id="confirm-switch" title="<s:text name='themeEditor.confirmTitle'/>" style="display:none">
    <s:text name="themeEditor.youSure"/>
    <br>
    <br>
    <span class="warning">
        <s:text name="themeEditor.switchWarning" />
    </span>
</div>

<%-- initializes the chooser/optioner/themeImport display at page load time --%>
<script>
    angular.module('themeSelectModule', [])
        .controller('themeController', ['$scope', function($scope) {
            var currentWeblog = $('#actionweblog').val();
            var myUrl = '<s:url value="/tb-ui/authoring/rest/themes/"/><s:property value='actionWeblog.theme'/>'
            $.ajax({ url: myUrl, async:false,
                success: function(data) { $scope.themes = data; }
            });
            $scope.selectedTheme = $scope.themes[0];
    }]);
</script>
