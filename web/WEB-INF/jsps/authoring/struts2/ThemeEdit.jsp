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
function previewImage(theme) {
    document.preview.src="<s:property value="siteURL" />/themes/" + theme + "/sm-theme-" + theme + ".png";
}

function fullPreview() {
    selected=document.themes.themeId.selectedIndex;
    window.open('<s:url value="/roller-ui/authoring/preview/%{actionWeblog.handle}"/>?theme='+document.themes.themeId.options[selected].value, '_preview', '');
}
-->
</script>

<p class="subtitle">
   <s:text name="themeEditor.subtitle" >
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>  
<p class="pagetip">
   <s:text name="themeEditor.tip" />
</p>

<s:form action="themes!save">
    <s:hidden name="weblog" value="%{actionWeblog.handle}" />
    
    <p><s:text name="themeEditor.yourCurrentTheme" />: <b><s:property value="actionWeblog.theme.name"/></b></p>
    
    <p>
        <s:select name="themeId" list="themes" listKey="id" listValue="name" size="1" onchange="previewImage(this[selectedIndex].value)"/>
        <input type="button" value="<s:text name="weblogEdit.fullPreviewMode" />" onclick="fullPreview()" />
    </p>
    <p>
        <img name="preview" src="" />
        <!-- initialize preview image at page load -->
        <script type="text/javascript">
                        previewImage('<s:property value="actionWeblog.theme.id"/>');
        </script>
    </p>
    <p><s:submit key="themeEditor.save" /></p>
    
</s:form>
