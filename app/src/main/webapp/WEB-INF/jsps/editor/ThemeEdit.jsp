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
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js' />"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>

<script>
function fullPreview(selector) {
    selected = selector.selectedIndex;
    window.open('<s:url value="/tb-ui/authoring/preview/%{actionWeblog.handle}"/>?theme=' + selector.options[selected].value);
}
</script>

<p class="subtitle">
   <s:text name="themeEditor.subtitle" >
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>

<s:form action="themeEdit!save">
    <sec:csrfInput/>
    <s:hidden name="weblog" />

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
    </div>

    <div class="control">
        <span style="padding-left:7px">
            <input type="button" name="themePreview"
                            value="<s:text name='themeEditor.preview' />"
                            onclick="fullPreview($('#themeSelector').get(0))" />

            <s:submit value="%{getText('themeEditor.save')}" />
        </span>
    </div>

</s:form>

<%-- initializes the chooser/optioner/themeImport display at page load time --%>
<script>
    angular.module('themeSelectModule', [])
        .controller('themeController', ['$scope', function($scope) {
            var myUrl = '<s:url value="/tb-ui/authoring/rest/themes/"/><s:property value="actionWeblog.theme"/>'
            $.ajax({ url: myUrl, async:false,
                success: function(data) { $scope.themes = data; }
            });
            $scope.selectedTheme = $scope.themes[0];
    }]);
</script>
