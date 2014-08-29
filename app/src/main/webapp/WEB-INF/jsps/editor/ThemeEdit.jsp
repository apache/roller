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

<script>
function previewImage(themeId) {
  $.ajax({ url: "<s:url value='themedata'/>",
    data: {theme:themeId}, success: function(data) {
      $('#themeDescription').html(data.description);
      $('#themeThumbnail').attr('src','<s:property value="siteURL" />' + data.previewPath);
    }
  });
}
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
                <s:if test="%{customStylesheet}">
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
        <s:if test="firstCustomization">
            <p>
                <s:text name="themeEditor.importRequired" />
            </p>
        </s:if>
        <s:else>
            <p>
                <span class="warning"><s:text name="themeEditor.importWarning" /></span>
            </p>
        </s:else>
    </div>

    <div id="themeOptioner" class="optioner">
        <p>
            <s:select id="themeSelector" name="selectedThemeId" list="themes"
                      listKey="id" listValue="name" size="1"
                      onchange="previewImage(this[selectedIndex].value)"/>
        </p>

        <p id="themeDescription"></p>
        <p>
            <img id="themeThumbnail" src="" />
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
    <s:if test="customTheme">
        updateThemeChooser($('#customRadio'));
        previewImage('<s:property value="themes[0].id"/>');
    </s:if>
    <s:else>
        updateThemeChooser($('#sharedRadio'));
        previewImage('<s:property value="themeId"/>');
    </s:else>
</script>
