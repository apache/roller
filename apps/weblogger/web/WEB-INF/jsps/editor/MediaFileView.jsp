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
<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/yui/menu/assets/skins/sam/menu.css'/>" />
<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/yui/button/assets/skins/sam/button.css'/>" />

<script type="text/javascript" src="<s:url value='/roller-ui/yui/yahoo-dom-event/yahoo-dom-event.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/container/container-min.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/menu/menu-min.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/element/element-min.js' />"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/button/button-min.js' />"></script>

<script type="text/javascript" src='<s:url value="/roller-ui/scripts/jquery-1.3.1.min.js" />'></script>


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

    function onSelectDirectory(id) {
        window.location = "?directoryId=" + id + "&weblog=" + '<s:property value="actionWeblog.handle" />';
    }

    function onCreateDirectory() {
        document.mediaFileViewForm.action='<s:url action="mediaFileView!createNewDirectory" />';
        document.mediaFileViewForm.submit();
    }

    function onDeleteSelected() {
        if ( confirm("<s:text name='mediaFile.delete.confirm' />") ) {
            document.mediaFileViewForm.action='<s:url action="mediaFileView!deleteSelected" />';
            document.mediaFileViewForm.submit();
        }
    }

    function onMoveSelected() {
        if ( confirm("<s:text name='mediaFile.move.confirm' />") ) {
            document.mediaFileViewForm.action='<s:url action="mediaFileView!moveSelected" />';
            document.mediaFileViewForm.submit();
        }
    }

</script>


<%-- ********************************************************************* --%>

<%-- Subtitle and folder path --%>

<s:if test='currentDirectory.path.equals("/")'>

    <p class="subtitle">
        <s:text name="mediaFileView.subtitle" >
            <s:param value="weblog" />
        </s:text>
    </p>
    </p>
    <p class="pagetip">
        <s:text name="mediaFileView.rootPageTip" />
    </p>

</s:if>

<s:elseif test='pager'>

    <p class="subtitle">
        <s:text name="mediaFileView.searchTitle" />
    </p>
    <p class="pagetip">

        <%-- display summary of the search results and terms --%>

        <s:if test="pager.items.size() > 0">
            <s:text name="mediaFileView.matchingResults">
                <s:param value="pager.items.size()" />
            </s:text>
        </s:if>
        <s:else>
            <s:text name="mediaFileView.noResults" />
        </s:else>
        <s:text name="mediaFileView.searchInfo" />

        <ul>
            <s:if test="!bean.name.isEmpty()">
                <li>
                    <s:text name="mediaFileView.filesNamed">
                        <s:param value="bean.name" />
                    </s:text>
                </li>
            </s:if>
            <s:if test="bean.size > 0">
                <li>
                    <s:text name="mediaFileView.filesOfSize">
                        <s:param value='bean.sizeFilterTypeLabel' />
                        <s:param value='bean.size' />
                        <s:param value='bean.sizeUnitLabel' />
                    </s:text>
                </li>
            </s:if>
            <s:if test="!bean.type.isEmpty()">
                <li>
                    <s:text name="mediaFileView.filesOfType">
                        <s:param value='bean.typeLabel' />
                    </s:text>
                </li>
            </s:if>
            <s:if test="!bean.tags.isEmpty()">
                <li>
                    <s:text name="mediaFileView.filesTagged">
                        <s:param value="bean.tags" />
                    </s:text>
                </li>
            </s:if>
        </ul>

    </p>
    <br />

</s:elseif>

<s:else>

    <p class="subtitle">
        <s:text name="mediaFileView.path"/> /
        <s:iterator id="directory" value="currentDirectoryHierarchy">
            <s:url id="getDirectoryByPathUrl" action="mediaFileView">
                <s:param name="directoryPath" value="#directory.key" />
                <s:param name="weblog" value="%{actionWeblog.handle}" />
            </s:url>
            <s:a href="%{getDirectoryByPathUrl}"><s:property value="#directory.value" /></s:a> /
        </s:iterator>
    </p>
    <p class="pagetip">
        <s:text name="mediaFileView.dirPageTip" />
    </p>

</s:else>


<s:if test="childFiles || childDirectories || (pager && pager.items.size() > 0)">

  <s:form id="mediaFileViewForm" name="mediaFileViewForm" action="mediaFileView">

    <%--
    <s:url id="mediaFileHierarchicalViewURL" action="mediaFileHierarchicalView">
        <s:param name="weblog" value="%{actionWeblog.handle}" />
    </s:url>
    <p><span style="font-weight:bold"><s:text name="mediaFileView.tabular" /></span> |
        <s:a href="%{mediaFileHierarchicalViewURL}"><s:text name="mediaFileView.hierarchy" /></s:a></p>
    --%>

    <div class="control">
        <span style="padding-left:20px"><s:text name="mediaFileView.sortby" /></span>
        <s:select name="sortBy" list="sortOptions" listKey="key"
                  listValue="value"
                  onchange="document.mediaFileViewForm.submit();" />
        </span>
    </div>

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

            <s:if test="!pager">

                <%-- ----------------------------------------------------- --%>

                <%-- NOT SEARCH RESULTS --%>

                <s:if test="childDirectories.size() == 0 && childFiles.size() ==0">
                    <p style="text-align: center"><s:text name="mediaFileView.noFiles"/></p>
                </s:if>

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

                <%-- List media files next --%>

                <s:iterator id="mediaFile" value="childFiles">

                    <li class="align-images"
                            onmouseover="highlight(this, true)" onmouseout="highlight(this, false)">

                        <div class="mediaObject"
                             onclick="onClickEdit('<s:property value="#mediaFile.id"/>')" >

                            <s:if test="#mediaFile.imageFile">
                                <s:url id="mediaFileURL"
                                    value="/%{#mediaFile.weblog.handle}/mediaresource/%{#mediaFile.id}?t=true"></s:url>
                                <img border="0" src='<s:property value="%{mediaFileURL}" />'
                                     width='<s:property value="#mediaFile.thumbnailWidth"/>'
                                     height='<s:property value="#mediaFile.thumbnailHeight"/>' />
                            </s:if>

                            <s:else>
                                <s:url id="mediaFileURL" value="/images/page.png"></s:url>
                                <img border="0" src='<s:property value="%{mediaFileURL}" />'
                                     style="padding:40px 50px;" />
                            </s:else>

                        </div>

                        <div class="mediaObjectInfo">

                            <label class="mediaFile">
                                <str:truncateNicely lower="40" upper="50">
                                    <s:property value="#mediaFile.name" />
                                </str:truncateNicely>
                                <input type="checkbox" style="float:right"
                                       name="selectedMediaFiles"
                                       value="<s:property value="#mediaFile.id"/>"/>
                                <inut type="hidden" id="mediafileidentity"
                                       value="<s:property value='#mediaFile.id'/>">
                            </label>

                        </div>

                    </li>

                </s:iterator>

            </s:if>

            <s:else>

                <%-- ----------------------------------------------------- --%>

                <%-- SEARCH RESULTS --%>

                <s:iterator id="mediaFile" value="pager.items">

                    <li class="align-images"
                            onmouseover="highlight(this, true)" onmouseout="highlight(this, false)">

                        <div class="mediaObject"
                             onclick="onClickEdit('<s:property value="#mediaFile.id"/>')" >

                            <s:if test="#mediaFile.imageFile">
                                <s:url id="mediaFileURL"
                                    value="/%{#mediaFile.weblog.handle}/mediaresource/%{#mediaFile.id}?t=true"></s:url>
                                <img border="0" src='<s:property value="%{mediaFileURL}" />'
                                     width='<s:property value="#mediaFile.thumbnailWidth"/>'
                                     height='<s:property value="#mediaFile.thumbnailHeight"/>' />
                            </s:if>

                            <s:else>
                                <s:url id="mediaFileURL" value="/images/page.png"></s:url>
                                <img border="0" src='<s:property value="%{mediaFileURL}" />'
                                     style="padding:40px 50px;" />
                            </s:else>

                        </div>

                        <div class="mediaObjectInfo">

                            <label>
                                <str:truncateNicely lower="40" upper="50">
                                    <s:property value="#mediaFile.name" />
                                </str:truncateNicely>
                                <input type="checkbox" style="float:right"
                                       name="selectedMediaFiles"
                                       value="<s:property value="#mediaFile.id"/>"/>
                                <inut type="hidden" id="mediafileidentity"
                                       value="<s:property value='#mediaFile.id'/>">
                            </label>


                        </div>

                    </li

                </s:iterator>

            </s:else>

        </ul>
    </div>

    <div style="clear:left;"></div>


    <%-- ***************************************************************** --%>

    <%-- Delete and move controls --%>

    <br/>
    <div class="control">
        <input type="button" style="padding-left:20px"
           value='<s:text name="mediaFileView.deleteSelected" />' onclick="onDeleteSelected()" />
        <input type="button" style="padding-left:20px"
           value=<s:text name="mediaFileView.moveSelected" /> onclick="onMoveSelected()" />
        <span style="padding-left:20px">
            <s:select name="selectedDirectory" list="allDirectories" listKey="id" listValue="path" />
        </span>
    </div>


  <s:if test="!pager">
    <br/>
    <br/>
    <br/>

    <%-- Only show Create New Directory control when NOT showing search results --%>

    <div>
        <s:text name="mediaFileView.newDirName" />
        <input type="text" id="newDirectoryName" name="newDirectoryName" size="30" />
        <input type="button" value='<s:text name="mediaFileView.create" />' onclick="onCreateDirectory()" />
    </div>
  </s:if>

</s:form>

</s:if>



<%-- ***************************************************************** --%>

<%-- code to create new weblog post when Media File Edit lightbox requests it --%>

<script type="text/javascript">

    function onCreateWeblogPost(mediaFileId) {
        $("#selectedImage").get(0).value = mediaFileId;
        $("#createPostForm").get(0).submit();
    }

    function onCreatePodcastPost(enclosureURL) {
        $("#enclosureUrl").get(0).value = enclosureURL;
        $("#createPostForm").get(0).submit();
    }

</script>

<s:form id="createPostForm" action='entryAddWithMediaFile'>
    <input type="hidden" name="weblog" value='<s:property value="actionWeblog.handle" />' />
    <input type="hidden" name="selectedImage" id="selectedImage" />
    <input type="hidden" name="enclosureUrl" id="enclosureUrl" />
</s:form>


<%-- ***************************************************************** --%>

<%-- code to launch Media File Edit lightbox when user clicks a media file --%>

<script type="text/javascript">

    function onClickEdit(mediaFileId) {
        <s:url id="mediaFileEditURL" action="mediaFileEdit">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
        </s:url>
        $("#mediaFileEditor").attr('src',
            '<s:property value="%{mediaFileEditURL}" />' + '&mediaFileId=' + mediaFileId);
        YAHOO.mediaFileEditor.lightbox.show();
    }

    function onClose() {
        $("#mediaFileEditor").attr('src','about:blank');
        YAHOO.mediaFileEditor.lightbox.hide();
    }

    YAHOO.namespace("mediaFileEditor");
    function init() {
        YAHOO.mediaFileEditor.lightbox = new YAHOO.widget.Panel(
            "mediafile_edit_lightbox", {
                modal:    true,
                width:   "600px",
                height:  "600px",
                visible: false,
                fixedcenter: true,
                constraintoviewport: true
            }
        );
        YAHOO.mediaFileEditor.lightbox.render(document.body);
    }
    YAHOO.util.Event.addListener(window, "load", init);

</script>

<div id="mediafile_edit_lightbox" style="visibility:hidden">
    <div class="hd">Media File Editor</div>
    <div class="bd">
        <iframe id="mediaFileEditor"
                style="visibility:inherit"
                height="100%"
                width="100%"
                frameborder="no"
                scrolling="auto">
        </iframe>
    </div>
    <div class="ft"></div>
</div>
