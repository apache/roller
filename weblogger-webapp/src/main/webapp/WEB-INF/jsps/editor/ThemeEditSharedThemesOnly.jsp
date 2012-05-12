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

function fullPreview() {
    selected=document.getElementById('themeEdit_themeId').selectedIndex;
    window.open('<s:url value="/roller-ui/authoring/preview/%{actionWeblog.handle}"/>?theme='+document.getElementById('themeEdit_themeId').options[selected].value, '_preview', '');
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
    <s:hidden name="themeType" value="shared" />
    
    <p><s:text name="themeEditor.yourCurrentTheme" />: <b><s:property value="actionWeblog.theme.name"/></b></p>
    
    <div>
        <p>
            <s:select name="themeId" list="themes" listKey="id" listValue="name" size="1" onchange="previewImage(document.getElementById('previewImg'), this[selectedIndex].value)"/>
        </p>
        <p>
            <img id="previewImg" src="" />
            <!-- initialize preview image at page load -->
            <script type="text/javascript">
            previewImage(document.getElementById('previewImg'), '<s:property value="themeId"/>');
            </script>
        </p>
        <p>
            &raquo; <a href="#" onclick="fullPreview()">See how your blog will look with this theme.</a><br/>
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
        
        <p><s:submit value="%{getText('themeEditor.save')}" /></p>
    </div>
    
</s:form>
