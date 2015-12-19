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
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<script src="<s:url value='/roller-ui/scripts/jquery-2.1.1.min.js' />"></script>
<script src="<s:url value='/webjars/angular/1.2.29/angular.min.js' />"></script>

<script>
function fullPreview(selector) {
    selected = selector.selectedIndex;
    window.open('<s:url value="/roller-ui/authoring/preview/%{actionWeblog.handle}"/>?theme=' + selector.options[selected].value);
}
function updateThemeChooser(selected) {
    if (selected[0].value == 'shared') {
        $('#sharedChooser').addClass("selectedChooser");
        $('#customChooser').removeClass("selectedChooser");
        $('#sharedOptioner').show();
        $('#customOptioner').hide();
    } else {
        $('#customChooser').addClass("selectedChooser");
        $('#sharedChooser').removeClass("selectedChooser");
        $('#customOptioner').show();
        $('#sharedOptioner').hide();
    }
}
</script>

<p class="subtitle">
   <s:text name="themeEditor.subtitle" >
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>

<s:form action="themeEdit!save">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />

    <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
            <td width="50%" valign="top">
                <div id="sharedChooser" class="chooser">
                    <h2><input id="sharedRadio" type="radio" name="themeType" value="shared"
                               <s:if test="!customTheme">checked</s:if>
                               onclick="updateThemeChooser($(this))" />&nbsp;
                    <s:text name="themeEditor.sharedTheme" /></h2>
                    <s:text name="themeEditor.sharedThemeDescription" />
                </div>
            </td>
            <td width="50%" valign="top">
                <div id="customChooser" class="chooser">
                    <h2><input id="customRadio" type="radio" name="themeType" value="custom"
                               <s:if test="customTheme">checked</s:if>
                               onclick="updateThemeChooser($(this))" />&nbsp;
                    <s:text name="themeEditor.customTheme" /></h2>
                    <s:text name="themeEditor.customThemeDescription" />
                </div>
            </td>
        </tr>
    </table>

    <div id="sharedOptioner" class="optioner" style="display:none;">
        <p>
            <s:if test="!customTheme">
                <s:text name="themeEditor.yourCurrentTheme" />:
                <b><s:property value="actionWeblog.theme.name"/></b>
                <%-- The type of stylesheet we are using --%>
                <s:if test="%{sharedThemeCustomStylesheet}">
                    <s:text name="themeEditor.yourCustomStylesheet" />
                </s:if>
                <s:else>
                    <s:text name="themeEditor.yourThemeStyleSheet" />
                </s:else>
            </s:if>
            <s:else>
                <s:text name="themeEditor.selectTheme" />:
            </s:else>
        </p>
    </div>

    <div id="customOptioner" class="optioner" style="display:none;">
        <%-- if already custom, an update must mean an import. --%>
        <s:if test="customTheme">
            <p>
                <span class="warning"><s:text name="themeEditor.importWarning" /></span>
                <s:hidden name="importTheme" value="true" />
            </p>
        </s:if>
        <%-- shared, may be required to do an import if no custom templates present --%>
        <s:else>
            <s:if test="firstCustomization">
                <p>
                    <s:text name="themeEditor.importRequired" />
                    <s:hidden name="importTheme" value="true" />
                </p>
            </s:if>
            <s:else>
                <%-- User has option not just to switch from shared to custom but also override present custom templates --%>
                <s:checkbox name="importTheme"/> <s:text name="themeEditor.importAndOverwriteTemplates" />
                    <tags:help key="themeEditor.importAndOverwriteTemplates.tooltip"/>
            </s:else>
        </s:else>
    </div>

    <div id="themeOptioner" class="optioner" ng-app="themeSelectModule" ng-controller="themeController">
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
            $.ajax({ url: "<s:url value='themedata'/>", async:false,
                success: function(data) { $scope.themes = data; }
            });
            <s:if test="customTheme">
                updateThemeChooser($('#customRadio'));
                $scope.selectedTheme = $scope.themes[0];
            </s:if>
            <s:else>
                updateThemeChooser($('#sharedRadio'));
                $scope.selectedTheme = $.grep($scope.themes, function(e){ return e.id == "<s:property value='themeId'/>"; })[0];
            </s:else>
    }]);
</script>
