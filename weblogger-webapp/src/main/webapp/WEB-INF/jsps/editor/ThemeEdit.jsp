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
<script type="text/javascript" src="<s:url value="/roller-ui/scripts/jquery-1.4.2.min.js" />"></script>

<script type="text/javascript">
<!--
function previewImage(q, theme) {
    q.attr('src','<s:property value="siteURL" />/roller-ui/authoring/previewtheme?theme=' + theme);
}
function fullPreview(selector) {
    selected = selector.selectedIndex;
    window.open('<s:url value="/roller-ui/authoring/preview/%{actionWeblog.handle}"/>?theme='+selector.options[selected].value, '_preview', '');
}
function updateThemeChooser(selected) {
    if (selected[0].value == 'shared') {
        $('#sharedChooser').css('background','#CCFFCC'); 
        $('#sharedChooser').css('border','1px solid #008000'); 
        $('#sharedOptioner').show();

        $('#customChooser').css('background','#eee'); 
        $('#customChooser').css('border','1px solid #gray'); 
        $('#customOptioner').hide();
    } else {
        $('#customChooser').css('background','#CCFFCC'); 
        $('#customChooser').css('border','1px solid #008000'); 
        $('#customOptioner').show();

        $('#sharedChooser').css('background','#eee'); 
        $('#sharedChooser').css('border','1px solid #gray'); 
        $('#sharedOptioner').hide();
    }
}
function toggleImportThemeDisplay() {
    $('themeImport').toggle();
}
-->
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
                <div id="sharedChooser" class="chooser" style="height: 8em">
                    <h2><input id="sharedRadio" type="radio" name="themeType" value="shared"
                               <s:if test="!customTheme">checked="true"</s:if>
                               onclick="updateThemeChooser($(this))" />&nbsp;
                    <s:text name="themeEditor.sharedTheme" /></h2>
                    <s:text name="themeEditor.sharedThemeDescription" />
                </div>
            </td>
            <td width="50%" valign="top">
                <div id="customChooser" class="chooser" style="height: 8em">
                    <h2><input id="customRadio" type="radio" name="themeType" value="custom"
                               <s:if test="customTheme">checked="true"</s:if>
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
            </s:if>
            <s:else>
                <s:text name="themeEditor.selectTheme" />
            </s:else>
        </p>

        <p>
            <s:select id="sharedSelector" name="themeId" list="themes"
                      listKey="id" listValue="name" size="1"
                      onchange="previewImage($('#sharedPreviewImg'), this[selectedIndex].value)"/>
        </p>
        <p>
            <img id="sharedPreviewImg" src="" />
            <!-- initialize preview image at page load -->
            <script type="text/javascript">
                <s:if test="customTheme">
                    previewImage($('#sharedPreviewImg'), '<s:property value="themes[0].id"/>');
                </s:if>
                <s:else>
                    previewImage($('#sharedPreviewImg'), '<s:property value="themeId"/>');
                </s:else>
            </script>
        </p>
        <p>
            &raquo; <a href="#" onclick="fullPreview($('#sharedSelector').get(0))">
            <s:text name="themeEditor.previewLink" /></a><br/>
            <s:text name="themeEditor.previewDescription" />
        </p>

        <s:if test="!customTheme && actionWeblog.theme.customStylesheet != null">
            <p>
                <s:url action="stylesheetEdit" id="stylesheetEdit" >
                    <s:param name="weblog" value="%{actionWeblog.handle}" />
                </s:url>
                &raquo; <s:a href="%{stylesheetEdit}"><s:text name="themeEditor.customStylesheetLink" /></s:a><br/>
                <s:text name="themeEditor.customStylesheetDescription" />
            </p>
        </s:if>
        <p><s:submit value="%{getText('themeEditor.save')}" /></p>
    </div>

    <div id="customOptioner" class="optioner" style="display:none;">

        <s:if test="firstCustomization">
            <p>
                <s:hidden name="importTheme" value="true" />
                <span class="warning"><s:text name="themeEditor.importRequired" /></span>
            </p>
        </s:if>
        <s:else>
            <s:if test="customTheme">
                <p>
                    <s:url id="templatesUrl" action="templates">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                    </s:url>
                    &raquo; <s:a href="%{templatesUrl}"><s:text name="themeEditor.templatesLink" /></s:a><br/>
                    <s:text name="themeEditor.templatesDescription" />
                </p>
            </s:if>

            <p>
                <s:checkbox name="importTheme" onclick="$('#themeImport').toggle();" />
                <s:text name="themeEditor.import" />
            </p>
        </s:else>

        <div id="themeImport" style="display:none;">
            <s:if test="customTheme">
                <p>
                    <span class="warning"><s:text name="themeEditor.importWarning" /></span>
                </p>
            </s:if>

            <p>
                <s:select id="customSelector" name="importThemeId" list="themes"
                          listKey="id" listValue="name" size="1"
                          onchange="previewImage($('#customPreviewImg'), this[selectedIndex].value)"/>
            </p>
            <p>
                <img id="customPreviewImg" src="" />
                <!-- initialize preview image at page load -->
                <script type="text/javascript">
                <s:if test="customTheme">
                    previewImage($('#customPreviewImg'), '<s:property value="themes[0].id"/>');
                </s:if>
                <s:else>
                    previewImage($('#customPreviewImg'), '<s:property value="themeId"/>');
                </s:else>
                </script>
            </p>
            <p>
                &raquo; <a href="#" onclick="fullPreview($('#customSelector').get(0))">
                <s:text name="themeEditor.previewLink" /></a><br/>
                <s:text name="themeEditor.previewDescription" />
            </p>
        </div>

        <p><s:submit value="%{getText('themeEditor.save')}" /></p>
    </div>

</s:form>

<%-- initializes the chooser/optioner/themeImport display at page load time --%>
<script type="text/javascript">
    <s:if test="customTheme">
        updateThemeChooser($('#customRadio'));
    </s:if>
    <s:else>
        updateThemeChooser($('#sharedRadio'));
    </s:else>

    <s:if test="firstCustomization">
        $('#themeImport').show();
    </s:if>
</script>
