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

<script src="<s:url value="/roller-ui/scripts/jquery-2.1.1.min.js" />"></script>

<style>
    .mediaObject {
         width:120px;
         height:120px;
    }
    .mediaObjectInfo {
        clear:left;
        width:130px;
        margin-left:5px;
        font-size:11px;
    }
    .highlight {
        border: 1px solid #aaa;
    }
    #myMenu {
        margin-left: 0;
    }
</style>

<script>
    function onSelectDirectory(id) {
        window.location = "?directoryId=" + id + "&weblog=" + '<s:property value="actionWeblog.handle" />';
    }
</script>


<%-- ********************************************************************* --%>

<%-- Subtitle and folder path --%>

<s:if test='currentDirectory.name.equals("default")'>

    <p class="subtitle">
        <s:text name="mediaFileImageChooser.subtitle" >
            <s:param value="weblog" />
        </s:text>
    </p>
    </p>
    <p class="pagetip">
        <s:text name="mediaFileImageChooser.rootPageTip" />
    </p>

</s:if>

<s:else>

    <p class="subtitle">
        <s:text name="mediaFileView.folderName"/> /
        <s:iterator id="directory" value="currentDirectoryHierarchy">
            <s:url var="getDirectoryByPathUrl" action="mediaFileImageChooser">
                <s:param name="directoryName" value="#directory.key" />
                <s:param name="weblog" value="%{actionWeblog.handle}" />
            </s:url>
            <s:a href="%{getDirectoryByPathUrl}"><s:property value="#directory.value" /></s:a> /
        </s:iterator>
    </p>
    <p class="pagetip">
        <s:text name="mediaFileImageChooser.dirPageTip" />
    </p>

</s:else>


<%-- || (pager && pager.items.size() > 0) --%>
<s:if test="childFiles || allDirectories">

<s:form id="mediaFileChooserForm" name="mediaFileChooserForm" action="mediaFileView">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="directoryId" />
    <input type="hidden" name="mediaFileId" value="" />


    <%-- ***************************************************************** --%>

    <%-- Media file folder contents --%>

    <script>
        function highlight(el, flag) {
            if (flag) {
                $(el).addClass("highlight");
            } else {
                $(el).removeClass("highlight");
            }
        }
    </script>

    <div  width="720px" height="500px">
        <ul id = "myMenu">

            <s:if test="childFiles.size() == 0">
                <p style="text-align: center"><s:text name="mediaFileView.noFiles"/></p>
            </s:if>

            <%-- --------------------------------------------------------- --%>

            <%-- List media directories first --%>

            <s:iterator id="directory" value="allDirectories">
                <li class="align-images"
                        onmouseover="highlight(this, true)" onmouseout="highlight(this, false)">
                    <div class="mediaObject">
                        <img  border="0" src='<s:url value="/images/folder.png"/>'
                              class="dir-image" alt="mediaFolder.png"
                              onclick="onSelectDirectory('<s:property value="#directory.id"/>')"/>
                    </div>
                    <div class="mediaObjectInfo">
                        <label><s:property value="#directory.name" /></label>
                    </div>
                </li>
            </s:iterator>

            <%-- --------------------------------------------------------- --%>

            <%-- List media files next --%>
            <s:if test="childFiles.size() > 0">

                <s:iterator id="mediaFile" value="childFiles">

                    <li class="align-images"
                        onmouseover="highlight(this, true)" onmouseout="highlight(this, false)">

                        <s:url var="mediaFileURL" includeContext="false"
                            value="%{#mediaFile.permalink}"></s:url>

                        <s:url var="mediaFileThumbnailURL"
                            value="%{#mediaFile.thumbnailURL}"></s:url>

                        <div class="mediaObject"
                             onclick="onSelectMediaFile('<s:property value="#mediaFile.name"/>',
                             '<s:property value="%{mediaFileURL}" />','<s:property value="#mediaFile.isImageFile()"/>')" >

                            <s:if test="#mediaFile.imageFile">

                                <img border="0" src='<s:property value="%{mediaFileThumbnailURL}" />'
                                     width='<s:property value="#mediaFile.thumbnailWidth"/>'
                                     height='<s:property value="#mediaFile.thumbnailHeight"/>' />
                            </s:if>

                        </div>

                        <div class="mediaObjectInfo">
                            <label>
                                <str:truncateNicely upper="50">
                                    <s:property value="#mediaFile.name" />
                                </str:truncateNicely>
                            </label>
                        </div>

                    </li>

                </s:iterator>
            </s:if>

        </ul>
    </div>

    <div style="clear:left;"></div>


</s:form>

</s:if>



<script>
    function onSelectMediaFile(name, url, isImage) {
        window.parent.onSelectMediaFile(name, url, isImage);
    }
</script>
