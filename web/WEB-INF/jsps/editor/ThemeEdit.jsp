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
    element.src="<s:property value="siteURL" />/themes/" + theme + "/sm-theme-" + theme + ".png";
}

function fullPreview(selector) {
    selected=selector.selectedIndex;
    window.open('<s:url value="/roller-ui/authoring/preview/%{actionWeblog.handle}"/>?theme='+selector.options[selected].value, '_preview', '');
}

function toggleThemeOptioner() {
    // just call toggle on both theme optioner choices
    new Effect.toggle('sharedThemeOptioner', 'appear');
    new Effect.toggle('customThemeOptioner', 'appear');
}

function updateThemeChooser(selected) {
    if(selected.value == 'shared') {
        selectedChooser = document.getElementById('sharedChooser');
        selectedOptioner = document.getElementById('sharedOptioner');
        
        otherChooser = document.getElementById('customChooser');
        otherOptioner = document.getElementById('customOptioner');
    } else {
        selectedChooser = document.getElementById('customChooser');
        selectedOptioner = document.getElementById('customOptioner');
        
        otherChooser = document.getElementById('sharedChooser');
        otherOptioner = document.getElementById('sharedOptioner');
    }
    
    // update styling on chooser
    selectedChooser.style.backgroundColor="#CCFFCC";
    selectedChooser.style.border="1px solid #008000";
    otherChooser.style.backgroundColor="#eee";
    otherChooser.style.border="1px solid grey";
    
    // update display of selected optioner
    otherOptioner.style.display="none";
    selectedOptioner.style.display="block";
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
                    <h2><input id="sharedRadio" type="radio" name="themeType" value="shared" <s:if test="!customTheme">checked="true"</s:if> onchange="updateThemeChooser(this)" />&nbsp;Shared Theme</h2>
                    This option is for users who don't want to fuss with designing their weblog on their own and prefer the easier option of using a predefined theme.
                </div>
            </td>
            <td width="50%">
                <div id="customChooser" class="chooser">
                    <h2><input id="customRadio" type="radio" name="themeType" value="custom" <s:if test="customTheme">checked="true"</s:if> onchange="updateThemeChooser(this)" />&nbsp;Custom Theme</h2>
                    This option is for the creative bloggers who want to be able to create a blog design of their own.  Beware though, managing a blog design of your own takes a bit of effort.
                </div>
            </td>
        </tr>
    </table>
    
    <div id="sharedOptioner" class="optioner" style="display:none;">
        <p><s:text name="themeEditor.yourCurrentTheme" />: <b><s:property value="actionWeblog.theme.name"/></b></p>
        
        <p>
            <s:select id="sharedSelector" name="themeId" list="themes" listKey="id" listValue="name" size="1" onchange="previewImage(document.getElementById('sharedPreviewImg'), this[selectedIndex].value)"/>
        </p>
        <p>
            <img id="sharedPreviewImg" src="" />
            <!-- initialize preview image at page load -->
            <script type="text/javascript">
                <s:if test="customTheme">
                    previewImage(document.getElementById('sharedPreviewImg'), '<s:property value="themes[0].id"/>');
                </s:if>
                <s:else>
                    previewImage(document.getElementById('sharedPreviewImg'), '<s:property value="themeId"/>');
                </s:else>
            </script>
        </p>
        <p>
            &raquo; <a href="#" onclick="fullPreview(document.getElementById('sharedSelector'))">See how your blog will look with this theme.</a><br/>
            How can you know if this is really the theme for you until you see it on your blog right?  Click the link above to launch a full page preview of how your blog will look with the selected theme.
        </p>
        <s:if test="actionWeblog.theme.customStylesheet != null">
            <p>
                <s:url action="stylesheetEdit" id="stylesheetEdit" >
                    <s:param name="weblog" value="%{actionWeblog.handle}" />
                </s:url>
                &raquo; <s:a href="%{stylesheetEdit}">Modify the styling of your selected theme.</s:a><br/>
                If you are happy with your theme but want to make a few styling choices of your own such as choosing different fonts, colors, etc, then try making your own stylesheet or borrow one from someoone else using your theme.
            </p>
        </s:if>
        <p><s:submit key="themeEditor.save" /></p>
    </div>
    
    <div id="customOptioner" class="optioner" style="display:none;">
        <p>
            <s:checkbox name="importTheme" />I want to copy the templates from the selected theme into my weblog.<br/>
            <span class="warning">WARNING: this operation may overwrite some of your existing templates.</span>
        </p>
        <p>
            <s:select id="customSelector" name="importThemeId" list="themes" listKey="id" listValue="name" size="1" onchange="previewImage(document.getElementById('customPreviewImg'), this[selectedIndex].value)"/>
        </p>
        <p>
            <img id="customPreviewImg" src="" />
            <!-- initialize preview image at page load -->
            <script type="text/javascript">
            previewImage(document.getElementById('customPreviewImg'), '<s:property value="themes[0].id"/>');
            </script>
        </p>
        <p>
            &raquo; <a href="#" onclick="fullPreview(document.getElementById('customSelector'))">See how your blog will look with this theme.</a><br/>
            How can you know if this is really the theme for you until you see it on your blog right?  Click the link above to launch a full page preview of how your blog will look with the selected theme.
        </p>
        <p><s:submit key="themeEditor.save" /></p>
    </div>
    
</s:form>

<%-- initializes the chooser/optioner display at page load time --%>
<script type="text/javascript">
    <s:if test="customTheme">
        updateThemeChooser(document.getElementById('customRadio'));
    </s:if>
    <s:else>
        updateThemeChooser(document.getElementById('sharedRadio'));
    </s:else>
</script>