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

<p class="subtitle">
    <s:text name="themeEditor.subtitle">
        <s:param value="actionWeblog.handle"/>
    </s:text>
</p>

<s:form action="themeEdit!save" theme="bootstrap" cssClass="form-vertical">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>

    <%-- Two choices side-by-side: choose Shared or Custom Theme --%>

    <div class="row equal">

        <div id="sharedChooser" class="col-md-6 panel">
            <div class="panel-heading">
                <h3 class="panel-title">
                    <input id="sharedRadio" type="radio" name="themeType" value="shared"
                           <s:if test="!customTheme">checked</s:if> onclick="proposeThemeTypeChange($(this))"/>&nbsp;
                    <s:text name="themeEditor.sharedTheme"/>
                </h3>
            </div>
            <div class="chooser panel-body">
                <s:text name="themeEditor.sharedThemeDescription"/>
            </div>
        </div>

        <div id="customChooser" class="col-md-6 panel">
            <div class="panel-heading">
                <h3 class="panel-title">
                    <input id="customRadio" type="radio" name="themeType" value="custom"
                           <s:if test="customTheme">checked</s:if> onclick="proposeThemeTypeChange($(this))"/>&nbsp;
                    <s:text name="themeEditor.customTheme"/>
                </h3>
            </div>
            <div class="chooser panel-body">
                <s:text name="themeEditor.customThemeDescription"/>
            </div>
        </div>

    </div>

    <%-- ********************************************************************************************************* --%>

        <div id="sharedNoChange" style="display:none;">

            <%-- you have shared theme X --%>
            <p class="lead">
                <s:text name="themeEditor.yourCurrentTheme"/>
                <b><s:property value="actionWeblog.theme.name"/></b>
                <s:if test="%{sharedThemeCustomStylesheet}">
                    <s:text name="themeEditor.yourCustomStylesheet"/>
                </s:if>
                <s:else>
                    <s:text name="themeEditor.yourThemeStyleSheet"/>
                </s:else>
            </p>

        </div>

        <div id="themeChooser" style="display:none;">

            <%-- theme selector with preview image --%>
            <p><s:text name="themeEditor.selectTheme"/></p>
            <p>
                <s:select id="themeSelector" name="selectedThemeId" list="themes"
                    listKey="id" listValue="name" size="1"
                    onchange="proposeSharedThemeChange(this[selectedIndex].value)"/>
            </p>
            <p id="themeDescription"></p>
            <p><img id="themeThumbnail" src=""/></p>

        </div>

        <div id="sharedChangeToShared" style="display:none;">

            <div class="alert-warning" style="margin-top:3em; margin-bottom:2em; padding: 1em">
                <s:text name="themeEditor.proposedSharedThemeChange"/>
            </div>

            <%-- Preview and Update buttons --%>
            <p> <s:text name="themeEditor.previewDescription"/> </p>
            <input type="button" name="themePreview" class="btn"
                value="<s:text name='themeEditor.preview' />"
                onclick="fullPreview($('#themeSelector').get(0))"/>

            <s:submit cssClass="btn btn-default" value="%{getText('themeEditor.save')}"/>

        </div>

        <div id="sharedChangeToCustom" style="display:none;">

            <div class="alert-warning" style="margin-top:3em; margin-bottom:2em; padding: 1em">
                <s:text name="themeEditor.proposedSharedChangeToCustom"/>
            </div>

            <s:if test="firstCustomization">
                <p>
                    <s:text name="themeEditor.importRequired"/>
                    <s:hidden name="importTheme" value="true"/>
                </p>
            </s:if>
            <s:else>
                <p><s:text name="themeEditor.existingTemplatesWarning"/></p>
                <s:checkbox name="importTheme" label="%{getText('themeEditor.importAndOverwriteTemplates')}"/>
            </s:else>

            <%-- Update button --%>
            <s:submit cssClass="btn btn-default" value="%{getText('themeEditor.save')}"/>

        </div>

    <%-- ********************************************************************************************************* --%>

        <div id="customNoChange" style="display:none;">
            <p class="lead"><s:text name="themeEditor.youAreUsingACustomTheme"/></p>
        </div>

        <div id="customChangeToShared" style="display:none;">

            <div class="alert-warning" style="margin-top:3em; margin-bottom:2em; padding: 1em">
                <s:text name="themeEditor.proposedChangeToShared"/>
            </div>

            <%-- Preview and Update buttons --%>
            <p> <s:text name="themeEditor.previewDescription"/> </p>
            <input type="button" name="themePreview" class="btn" style="margin-bottom:2em"
                value="<s:text name='themeEditor.preview' />"
                onclick="fullPreview($('#themeSelector').get(0))"/>

            <s:submit cssClass="btn btn-default" value="%{getText('themeEditor.save')}"/>

        </div>

</s:form>

<script type="text/javascript">

    var proposedChangeType = ""
    var proposedThemeId = ""
    var originalThemeId = "<s:property value="themeId"/>"
    var originalType = ""

    $.when( $.ready ).then(function() {

        <s:if test="customTheme">
        originalType = "custom"
        updateView($('#customRadio'));
        previewImage('<s:property value="themes[0].id"/>');
        </s:if>

        <s:else>
        originalType = "shared"
        updateView($('#sharedRadio'));
        previewImage('<s:property value="themeId"/>');
        </s:else>
    });

    function proposeThemeTypeChange(selected) {

        if (selected[0].value === 'shared') {
            proposedChangeType = "shared"

            themeSelector = $('#themeSelector')[0]
            index = themeSelector.selectedIndex;
            previewImage(themeSelector.options[index].value)

        } else {
            proposedChangeType = "custom"
        }
        updateView(selected)
    }

    function proposeSharedThemeChange(themeId) {
        proposedThemeId = themeId;
        if ( originalType !== "custom" && proposedThemeId !== originalThemeId ) {
        }
        previewImage(themeId)
        updateView($('sharedRadio'))
    }

    function previewImage(themeId) {
        $.ajax({
            url: "<s:url value='themedata'/>",
            data: {theme: themeId}, success: function (data) {
                $('#themeDescription').html(data.description);
                $('#themeThumbnail').attr('src', '<s:property value="siteURL" />' + data.previewPath);
            }
        });
    }

    function fullPreview(selector) {
        selected = selector.selectedIndex;
        window.open('<s:url value="/roller-ui/authoring/preview/%{actionWeblog.handle}"/>?theme='
            + selector.options[selected].value);
    }

    function updateView(selected) {

        if (selected[0].value === 'shared') {

            $('#sharedChooser').addClass("panel-success");
            $('#customChooser').removeClass("panel-success");

            $('#themeChooser').show();

            $('#customNoChange').hide();
            $('#customChangeToShared').hide();

            if ( proposedThemeId === "") {
                $('#sharedChangeToShared').hide();
                $('#sharedChangeToCustom').hide();

            } else if ( proposedThemeId != originalThemeId ) {

                if ( originalType === "shared" ) {
                    $('#sharedChangeToShared').show();
                    $('#sharedChangeToCustom').hide();
                }  else {
                    $('#customChangeToShared').show();
                    $('#sharedChangeToShared').hide();
                    $('#sharedChangeToCustom').hide();
                }
            }

        } else {

            $('#customChooser').addClass("panel-success");
            $('#sharedChooser').removeClass("panel-success");

            $('#themeChooser').hide();

            $('#sharedNoChange').hide();
            $('#customChangeToShared').hide();

            if ( proposedChangeType !== "" || proposedChangeType !== originalType ) {
                $('#sharedChangeToCustom').show();
                $('#customNoChange').hide();
            } else {
                $('#customNoChange').show();
            }

        }
    }

</script>
