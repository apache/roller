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

<script type="text/javascript">
<!--
function previewImage(element, theme) {
    element.src="<s:property value="siteURL" />/roller-ui/authoring/previewtheme?theme="+theme;
}

function fullPreview(selector) {
    selected=selector.selectedIndex;
    window.open('<s:url value="/roller-ui/authoring/preview/%{actionWeblog.handle}"/>?theme='+selector.options[selected].value, '_preview', '');
}

function updateThemeChooser(selected) {
    if(selected.value == 'shared') {
        $('sharedChooser').style.backgroundColor="#CCFFCC";
        $('sharedChooser').style.border="1px solid #008000";
        $('sharedOptioner').show();
        
        $('customChooser').style.backgroundColor="#eee";
        $('customChooser').style.border="1px solid gray";
        $('customOptioner').hide();
    } else {
        $('customChooser').style.backgroundColor="#CCFFCC";
        $('customChooser').style.border="1px solid #008000";
        $('customOptioner').show();
        
        $('sharedChooser').style.backgroundColor="#eee";
        $('sharedChooser').style.border="1px solid gray";
        $('sharedOptioner').hide();
    }
}
-->
</script>

<p class="subtitle">
   <s:text name="themeEditor.subtitle" >
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>

<s:form action="themeEdit!save">
    <s:hidden name="weblog" />
    
    <table width="100%" cellpadding="0" cellspacing="0">
        <tr>
            <td width="50%">
                <div id="sharedChooser" class="chooser">
                    <h2><input id="sharedRadio" type="radio" name="themeType" value="shared" <s:if test="!customTheme">checked="true"</s:if> onclick="updateThemeChooser(this)" />&nbsp;<s:text name="themeEditor.sharedTheme" /></h2>
                    <s:text name="themeEditor.sharedThemeDescription" />
                </div>
            </td>
            <td width="50%">
                <div id="customChooser" class="chooser">
                    <h2><input id="customRadio" type="radio" name="themeType" value="custom" <s:if test="customTheme">checked="true"</s:if> onclick="updateThemeChooser(this)" />&nbsp;<s:text name="themeEditor.customTheme" /></h2>
                    <s:text name="themeEditor.customThemeDescription" />
                </div>
            </td>
        </tr>
    </table>
    
    <div id="sharedOptioner" class="optioner" style="display:none;">
        <p><s:text name="themeEditor.yourCurrentTheme" />: <b><s:property value="actionWeblog.theme.name"/></b></p>
        
        <p>
            <s:select id="sharedSelector" name="themeId" list="themes" listKey="id" listValue="name" size="1" onchange="previewImage($('sharedPreviewImg'), this[selectedIndex].value)"/>
        </p>
        <p>
            <img id="sharedPreviewImg" src="" />
            <!-- initialize preview image at page load -->
            <script type="text/javascript">
                <s:if test="customTheme">
                    previewImage($('sharedPreviewImg'), '<s:property value="themes[0].id"/>');
                </s:if>
                <s:else>
                    previewImage($('sharedPreviewImg'), '<s:property value="themeId"/>');
                </s:else>
            </script>
        </p>
        <p>
            &raquo; <a href="#" onclick="fullPreview($('sharedSelector'))"><s:text name="themeEditor.previewLink" /></a><br/>
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
        <p><s:submit key="themeEditor.save" /></p>
    </div>
    
    <div id="customOptioner" class="optioner" style="display:none;">
        <p>
            <s:if test="!firstCustomization">
                <s:hidden name="importTheme" value="true" />
                <span class="warning"><s:text name="themeEditor.importRequired" /></span>
            </s:if>
            <s:else>
                <s:checkbox name="importTheme" /><s:text name="themeEditor.import" /><br/>
                <span class="warning"><s:text name="themeEditor.importWarning" /></span>
            </s:else>
        </p>
        <p>
            <s:select id="customSelector" name="importThemeId" list="themes" listKey="id" listValue="name" size="1" onchange="previewImage($('customPreviewImg'), this[selectedIndex].value)"/>
        </p>
        <p>
            <img id="customPreviewImg" src="" />
            <!-- initialize preview image at page load -->
            <script type="text/javascript">
                <s:if test="customTheme">
                    previewImage($('customPreviewImg'), '<s:property value="themes[0].id"/>');
                </s:if>
                <s:else>
                    previewImage($('customPreviewImg'), '<s:property value="themeId"/>');
                </s:else>
            </script>
        </p>
        <p>
            &raquo; <a href="#" onclick="fullPreview($('customSelector'))"><s:text name="themeEditor.previewLink" /></a><br/>
            <s:text name="themeEditor.previewDescription" />
        </p>
        <p><s:submit key="themeEditor.save" /></p>
    </div>
    
</s:form>

<%-- initializes the chooser/optioner display at page load time --%>
<script type="text/javascript">
    <s:if test="customTheme">
        updateThemeChooser($('customRadio'));
    </s:if>
    <s:else>
        updateThemeChooser($('sharedRadio'));
    </s:else>
</script>
