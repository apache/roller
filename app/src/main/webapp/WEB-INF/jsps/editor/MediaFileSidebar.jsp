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

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarInner">

                <br />
                <b><s:text name="mediaFileSidebar.actions" /></b>
                <br />
                <br />

                <img src='<s:url value="/images/image_add.png"/>' border="0"alt="icon" />
                <s:url var="mediaFileAddURL" action="mediaFileAdd">
                    <s:param name="weblog" value="%{actionWeblog.handle}" />
                    <s:param name="directoryName" value="%{directoryName}" />
                </s:url>
                <a href='<s:property escape="false" value="%{mediaFileAddURL}" />'
                    <s:if test="actionName.equals('mediaFileAdd')">style='font-weight:bold;'</s:if> >
                    <s:text name="mediaFileSidebar.add" />
                </a>

                <br /><br />
                <div>
                    <img src='<s:url value="/images/folder_add.png"/>' border="0"alt="icon" />
                    <s:text name="mediaFileView.addDirectory" /><br />
                    <div style="padding-left:2em; padding-top:1em">
                        <s:text name="mediaFileView.directoryName" />
                        <input type="text" id="newDirectoryName" name="newDirectoryName" size="10" maxlength="25" onBlur="this.value=this.value.trim()"/>
                        <input type="button" id="newDirectoryButton"
                            value='<s:text name="mediaFileView.create" />' onclick="onCreateDirectory()" />
                    </div>
                </div>

                <br/><br/><br/>
            </div>
        </div>
    </div>
</div>



<script>

function onCreateDirectory() {
    document.mediaFileViewForm.newDirectoryName.value = $("#newDirectoryName").get(0).value;
    document.mediaFileViewForm.action='<s:url action="mediaFileView!createNewDirectory" />';
    document.mediaFileViewForm.submit();
}

$("#newDirectoryButton").ready(function () {
    $("#newDirectoryName").bind("keyup", maintainDirectoryButtonState);
    $("#newDirectoryButton").attr("disabled", true);
});

function maintainDirectoryButtonState(e) {
    if ( jQuery.trim($("#newDirectoryName").get(0).value).length == 0) {
        $("#newDirectoryButton").attr("disabled", true);
    } else {
        $("#newDirectoryButton").attr("disabled", false);
    }
}

</script>
