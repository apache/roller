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

<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/yui/assets/skins/sam/container.css'/>" />
<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/yui/menu/assets/menu.css'/>" />

<script type="text/javascript" src="<s:url value='/roller-ui/yui/yahoo-dom-event/yahoo-dom-event.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/container/container_core-min.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/container/container-min.js' />"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/menu/menu-min.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/dragdrop/dragdrop-min.js' />"></script>

<script type="text/javascript" src="<s:url value="/roller-ui/scripts/jquery-1.4.2.min.js" />"></script>


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

<script type="text/javascript">
<!--
    function onSelectDirectory(id) {
        window.location = "?directoryId=" + id + "&weblog=" + '<s:property value="actionWeblog.handle" />';
    }
-->
</script>


<%-- ********************************************************************* --%>

<%-- Subtitle and folder path --%>

<s:if test='currentDirectory.path.equals("/")'>

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
        <s:text name="mediaFileView.path"/> /
        <s:iterator id="directory" value="currentDirectoryHierarchy">
            <s:url id="getDirectoryByPathUrl" action="mediaFileImageChooser">
                <s:param name="directoryPath" value="#directory.key" />
                <s:param name="weblog" value="%{actionWeblog.handle}" />
            </s:url>
            <s:a href="%{getDirectoryByPathUrl}"><s:property value="#directory.value" /></s:a> /
        </s:iterator>
    </p>
    <p class="pagetip">
        <s:text name="mediaFileImageChooser.dirPageTip" />
    </p>

</s:else>


<s:if test="childFiles || childDirectories || (pager && pager.items.size() > 0)">

<s:form id="mediaFileChooserForm" name="mediaFileChooserForm" action="mediaFileView">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="directoryId" />
    <input type="hidden" name="mediaFileId" value="" />


    <%-- ***************************************************************** --%>

    <%-- Media file folder contents --%>

    <script type="text/javascript">
        function highlight(el, flag) {
            if (flag) {
                YAHOO.util.Dom.addClass(el, "highlight");
            } else {
                YAHOO.util.Dom.removeClass(el, "highlight");
            }
        }
    </script>


    <div  width="720px" height="500px">
        <ul id = "myMenu">

            <s:if test="childDirectories.size() == 0 && childFiles.size() ==0">
                <p style="text-align: center"><s:text name="mediaFileView.noFiles"/></p>
            </s:if>

            <%-- --------------------------------------------------------- --%>

            <%-- List media directories first --%>

            <s:iterator id="directory" value="childDirectories">
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

            <s:iterator id="mediaFile" value="childFiles">

                <li class="align-images"
                        onmouseover="highlight(this, true)" onmouseout="highlight(this, false)">

                        <s:url id="mediaFileURL" includeContext="false"
                            value="%{#mediaFile.permalink}"></s:url>

                        <s:url id="mediaFileThumbnailURL"
                            value="%{#mediaFile.thumbnailURL}"></s:url>

                    <div class="mediaObject"
                         onclick="onSelectImage('<s:property value="#mediaFile.name"/>','<s:property value="%{mediaFileURL}" />')" >

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

        </ul>
    </div>

    <div style="clear:left;"></div>


</s:form>

</s:if>



<script type="text/javascript">
<!--
    function onSelectImage(name, url) {
        window.parent.onSelectImage(name, url);
    }
-->
</script>
