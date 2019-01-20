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

    <div class="row row-display-flex">

        <div class="col-xs-6">
            <div class="panel panel-default">
                <div class="panel-body" id="sharedChooser">
                    <h3>
                        <input id="sharedRadio" type="radio" name="themeType" value="shared"
                            <s:if test="!customTheme">checked</s:if> onclick="proposeThemeTypeChange($(this))"/>&nbsp;
                        <s:text name="themeEditor.sharedTheme"/>
                    </h3>
                    <s:text name="themeEditor.sharedThemeDescription"/>
                </div>
            </div>
        </div>

        <div class="col-xs-6">
            <div class="panel panel-default">
                <div class="panel-body" id="customChooser">
                    <h3>
                        <input id="customRadio" type="radio" name="themeType" value="custom"
                            <s:if test="customTheme">checked</s:if> onclick="proposeThemeTypeChange($(this))"/>&nbsp;
                        <s:text name="themeEditor.customTheme"/>
                    </h3>
                    <s:text name="themeEditor.customThemeDescription"/>
                </div>
            </div>
        </div>

    </div>

    <%-- ================================================= --%>

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

    <%-- ================================================= --%>

    <div id="themeChooser" style="display:none;">

        <%-- theme selector with preview image --%>
        <p class="lead"><s:text name="themeEditor.selectTheme"/></p>
        <p>
            <s:select id="themeSelector" name="selectedThemeId" list="themes" style="width:20em"
                listKey="id" listValue="name" size="1"
                onchange="proposeSharedThemeChange(this[selectedIndex].value)"/>
        </p>
        <p><s:text name="themeEditor.thisTheme"/> <p id="themeDescription"></p>
        <p><img id="themeThumbnail" src="" class="img-responsive img-thumbnail" style="max-width: 30em" /></p>

    </div>

    <%-- ================================================= --%>

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

        <input type="button" class="btn" onclick="cancelChanges()" value="<s:text name='generic.cancel'/>" />

    </div>

    <%-- ================================================= --%>

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

        <input type="button" class="btn" onclick="cancelChanges()" value="<s:text name='generic.cancel'/>" />

    </div>

    <%-- ================================================= --%>

    <div id="customNoChange" style="display:none;">
        <p class="lead"><s:text name="themeEditor.youAreUsingACustomTheme"/></p>
    </div>

    <%-- ================================================= --%>

    <div id="customChangeToShared" style="display:none;">

        <div class="alert-warning" style="margin-top:3em; margin-bottom:2em; padding: 1em">
            <s:text name="themeEditor.proposedChangeToShared"/>
        </div>

        <%-- Preview and Update buttons --%>
        <p> <s:text name="themeEditor.previewDescription"/> </p>
        <input type="button" name="themePreview" class="btn"
            value="<s:text name='themeEditor.preview' />"
            onclick="fullPreview($('#themeSelector').get(0))"/>

        <s:submit cssClass="btn btn-default" value="%{getText('themeEditor.save')}"/>

        <input type="button" class="btn" onclick="cancelChanges()" value="<s:text name='generic.cancel'/>" />

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
            proposedThemeId = originalThemeId
            proposedChangeType = "custom"
        }
        updateView(selected)
    }

    function proposeSharedThemeChange(themeId) {
        proposedThemeId = themeId;
        previewImage(themeId)
        updateView($('#sharedRadio'))
    }

    function cancelChanges() {

        proposedThemeId = originalThemeId;
        proposedChangeType = originalType;

        hideAll();

        if ( originalType === "custom" ) {
            $("#sharedRadio").prop("checked", false);
            $("#customRadio").prop("checked", true);
            updateView($("#customRadio"));

        } else {
            $("#sharedRadio").prop("checked", true);
            $("#customRadio").prop("checked", false);
            updateView($("#sharedRadio"));
            $("#themeSelector").val(originalThemeId).change();
            previewImage(originalThemeId)
        }

    }

    function hideAll() {
        $('#themeChooser').hide();
        $('#customNoChange').hide();
        $('#customChangeToShared').hide();
        $('#sharedChangeToShared').hide();
        $('#sharedNoChange').hide();
        $('#sharedChangeToCustom').hide();
    }

    function previewImage(themeId) {
        $.ajax({
            url: "<s:url value='themedata'/>",
            data: {theme: themeId}, success: function (data) {
                $('#themeDescription').html(data.description);
                thumbnail = $('#themeThumbnail');
                thumbnail.attr('src', '<s:property value="siteURL" />' + data.previewPath);
            }
        });
    }

    function fullPreview(selector) {
        selected = selector.selectedIndex;
        window.open('<s:url value="/roller-ui/authoring/preview/%{actionWeblog.handle}"/>?theme='
            + selector.options[selected].value);
    }

    function updateView(selected) {

        changed =
               (proposedThemeId    !== "" && proposedThemeId    !== originalThemeId)
            || (proposedChangeType !== "" && proposedChangeType !== originalType )

        if (selected[0].value === 'shared') {

            $('#sharedChooser').css("background", "#bfb")
            $('#customChooser').css("background", "white")

            $('#themeChooser').show();

            $('#customNoChange').hide();
            $('#customChangeToShared').hide();

            if ( !changed ) {
                $('#sharedNoChange').show();
                $('#sharedChangeToShared').hide();
                $('#sharedChangeToCustom').hide();

            } else {

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

            $('#sharedChooser').css("background", "white")
            $('#customChooser').css("background", "#bfb")

            $('#themeChooser').hide();

            $('#sharedNoChange').hide();
            $('#sharedChangeToShared').hide();
            $('#sharedChangeToCustom').hide();

            $('#customChangeToShared').hide();

            if ( !changed ) {
                $('#customNoChange').show();
            } else {
                $('#sharedChangeToCustom').show();
                $('#customNoChange').hide();
            }

        }
    }

</script>
