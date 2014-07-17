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

<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/yui/menu/assets/skins/sam/menu.css'/>" />

<script type="text/javascript" src="<s:url value='/roller-ui/yui/yahoo-dom-event/yahoo-dom-event.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/menu/menu-min.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/element/element-min.js' />"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui/button/button-min.js' />"></script>

<script type="text/javascript" src="<s:url value="/roller-ui/scripts/jquery-2.1.1.min.js" />"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/yui3/yui/yui-min.js' />"></script>


<style>
    .mediaObject {
         width:120px;
         height:120px;
    }
    .mediaObjectInfo {
        clear:left;
        width:130px;
        margin-left:5px;
        margin-top:3px;
        font-size:11px;
    }
    .highlight {
        border: 1px solid #aaa;
    }
    #myMenu {
        margin-left: 0;
    }
    span.button {
        height:15px;
        width:15px;
        float:right;
    }
    .yui-button button {
        border-style: none;
        background-color:transparent;
        *overflow:visible;
        cursor:pointer;
    }
    .yui-menu-button button {
        width:15px; height: 15px;
        background-repeat: no-repeat;
        background-position: center;
        background-image: url(<s:url value="/images/add.png"/>);
    }
</style>


<script type="text/javascript">
    toggleState = 'Off'

    function onSelectDirectory(id) {
        window.location = "<s:url action="mediaFileView" />?directoryId=" + id + "&weblog=" + '<s:property value="actionWeblog.handle" />';
    }

    function onToggle() {
        if (toggleState == 'Off') {
            toggleState = 'On';
            toggleFunction(true, 'selectedMediaFiles');
            $("#deleteButton").attr('disabled',false)
            $("#moveButton").attr('disabled',false)
            $("#moveTargetMenu").attr('disabled',false)
        } else {
            toggleState = 'Off';
            toggleFunction(false, 'selectedMediaFiles');
            $("#deleteButton").attr('disabled',true)
            $("#moveButton").attr('disabled',true)
            $("#moveTargetMenu").attr('disabled',true)
        }
    }

    function onDeleteSelected() {
        if ( confirm("<s:text name='mediaFile.delete.confirm' />") ) {
            document.mediaFileViewForm.action='<s:url action="mediaFileView!deleteSelected" />';
            document.mediaFileViewForm.submit();
        }
    }

    function onDeleteFolder() {
        if (confirm("<s:text name='mediaFile.deleteFolder.confirm' />")) {
            document.bookmarks.action='<s:url action="mediaFileView!deleteFolder" />';
            document.bookmarks.submit();
        }
    }

    function onMoveSelected() {
        if ( confirm("<s:text name='mediaFile.move.confirm' />") ) {
            document.mediaFileViewForm.action='<s:url action="mediaFileView!moveSelected" />';
            document.mediaFileViewForm.submit();
        }
    }
    
    function onView() {
        document.mediaFileViewForm.action = "<s:url action='mediaFileView!view' />";
        document.mediaFileViewForm.submit();
    }

    <%-- menu button for each image, launched from the plus sign image --%>

    var menuButtons = {};

    function setupMenuButton(id) {
        if (!menuButtons[id]) {
            var mediaFileMenu = [
                { text: "<s:text name='mediaFile.createWeblogPost' />", value: 1, onclick: { fn: onCreateWeblogPost, obj:id } },
                { text: "<s:text name='mediaFile.createPodcastPost' />", value: 2, onclick: { fn: onCreatePodcastPost, obj:id } }
            ];
            menuButtons[id] = new YAHOO.widget.Button({
                type: "menu", label: "", name: id,
                menu: mediaFileMenu, container: 'addbutton-' + id });
            $('#addbutton-img' + id).hide();
        }
    }

    function onCreateWeblogPost(p_sType, p_aArgs, id) {
        $("#selectedImage").get(0).value = id;
        $("#type").get(0).value = 'weblog';
        $("#createPostForm").get(0).submit();
    }

    function onCreatePodcastPost(p_sType, p_aArgs, id) {
        $("#selectedImage").get(0).value = id;
        $("#type").get(0).value = 'podcast';
        $("#createPostForm").get(0).submit();
    }

    <%-- code to toggle buttons on/off as media file/directory selections change --%>

    $(document).ready(function() {
        $("#deleteButton").attr('disabled',true)
        $("#moveButton").attr('disabled',true)
        $("#moveTargetMenu").attr('disabled',true)

        $("input[type=checkbox]").change(function() {
            var count = 0;
            $("input[type=checkbox]").each(function(index, element) {
                if (element.checked) count++;
            });
            if (count == 0) {
                $("#deleteButton").attr('disabled',true)
                $("#moveButton").attr('disabled',true)
                $("#moveTargetMenu").attr('disabled',true)
            } else {
                $("#deleteButton").attr('disabled',false)
                $("#moveButton").attr('disabled',false)
                $("#moveTargetMenu").attr('disabled',false)
            }
        });
    });
</script>


<s:form id="createPostForm" action='entryAddWithMediaFile'>
	<s:hidden name="salt" />
    <input type="hidden" name="weblog" value='<s:property value="actionWeblog.handle" />' />
    <input type="hidden" name="selectedImage" id="selectedImage" />
    <input type="hidden" name="type" id="type" />
</s:form>


<%-- ********************************************************************* --%>

<%-- Subtitle and folder path --%>

<s:if test='currentDirectory.name.equals("default")'>

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
        <s:text name="mediaFileView.folderName"/>: <s:text name="currentDirectory.name" />
    </p>
    <p class="pagetip">
        <s:text name="mediaFileView.dirPageTip" />
    </p>

</s:else>


<s:if test="childFiles || (pager && pager.items.size() > 0)">

  <s:form id="mediaFileViewForm" name="mediaFileViewForm" action="mediaFileView">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="directoryId" />
    <s:hidden name="newDirectoryName" />
    <input type="hidden" name="mediaFileId" value="" />

    <div class="control">
        <span style="padding-left:7px">
            <s:text name="mediaFileView.sortby" />
            <s:select id="sortByMenu" name="sortBy" list="sortOptions" listKey="key"
                  listValue="value"
                  onchange="document.mediaFileViewForm.submit();" />
        </span>

        <span style="float:right">
            <s:if test="!allDirectories.isEmpty">
                <%-- View button
                <s:submit type="button" action="mediaFileView!view" key="mediaFileView.viewFolder" /> --%>

                <%-- Folder to View combo-box --%>
                <s:select name="viewDirectoryId" list="allDirectories" listKey="id" listValue="name" onchange="onView()" />

            </s:if>
        </span>
    </div>

    <%-- ***************************************************************** --%>

    <%-- Media file folder contents --%>

    <script type="text/javascript">
        function highlight(el, flag) {
            if (flag) {
                YUI().use('node', function (Y) {
                    Y.one(el).addClass("highlight");
                });
            } else {
                YUI().use('node', function (Y) {
                    Y.one(el).removeClass("highlight");
                });
            }
        }
    </script>

    <div  width="720px" height="500px">
        <ul id = "myMenu">

            <s:if test="!pager">

                <%-- ----------------------------------------------------- --%>

                <%-- NOT SEARCH RESULTS --%>

                <s:if test="childFiles.size() ==0">
                    <p style="text-align: center"><s:text name="mediaFileView.noFiles"/></p>
                </s:if>

                <%-- List media files --%>

                <s:iterator id="mediaFile" value="childFiles">

                    <li class="align-images"
                            onmouseover="highlight(this, true)" onmouseout="highlight(this, false)">

                        <div class="mediaObject"
                             onclick="onClickEdit('<s:property value="#mediaFile.id"/>')" >

                            <s:if test="#mediaFile.imageFile">
                                <img border="0" src='<s:property value="%{#mediaFile.thumbnailURL}" />'
                                     width='<s:property value="#mediaFile.thumbnailWidth"/>'
                                     height='<s:property value="#mediaFile.thumbnailHeight"/>'
                                     title='<s:property value="#mediaFile.name" />' />
                            </s:if>

                            <s:else>
                                <s:url id="mediaFileURL" value="/images/page.png"></s:url>
                                <img border="0" src='<s:property value="%{mediaFileURL}" />'
                                     style="padding:40px 50px;" alt="logo" />
                            </s:else>

                        </div>

                        <div class="mediaObjectInfo"
                             onmouseover='setupMenuButton("<s:property value='#mediaFile.id' />")'>

                            <input type="checkbox"
                                   name="selectedMediaFiles"
                                   value="<s:property value="#mediaFile.id"/>" />
                            <input type="hidden" id="mediafileidentity"
                                   value="<s:property value='#mediaFile.id'/>" />

                            <str:truncateNicely lower="47" upper="47">
                                <s:property value="#mediaFile.name" />
                            </str:truncateNicely>

                            <span class="button" id="addbutton-<s:property value='#mediaFile.id' />">
                                <img id="addbutton-img<s:property value='#mediaFile.id' />"
                                     src="<s:url value="/images/add.png"/>"  alt="logo" />
                            </span>

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
                                <img border="0" src='<s:property value="%{#mediaFile.thumbnailURL}" />'
                                     width='<s:property value="#mediaFile.thumbnailWidth"/>'
                                     height='<s:property value="#mediaFile.thumbnailHeight"/>'
                                     title='<s:property value="#mediaFile.name" />' />
                            </s:if>

                            <s:else>
                                <s:url id="mediaFileURL" value="/images/page.png"></s:url>
                                <img border="0" src='<s:property value="%{mediaFileURL}" />'
                                     style="padding:40px 50px;" />
                            </s:else>

                        </div>

                        <div class="mediaObjectInfo"
                             onmouseover='setupMenuButton("<s:property value='#mediaFile.id' />")'>

                                <input type="checkbox"
                                       name="selectedMediaFiles"
                                       value="<s:property value="#mediaFile.id"/>"/>
                                <input type="hidden" id="mediafileidentity"
                                       value="<s:property value='#mediaFile.id'/>">

                                <str:truncateNicely lower="40" upper="50">
                                    <s:property value="#mediaFile.name" />
                                </str:truncateNicely>

                                <span class="button" id="addbutton-<s:property value='#mediaFile.id' />">
                                    <img id="addbutton-img<s:property value='#mediaFile.id' />"
                                         src="<s:url value="/images/add.png"/>" />
                                </span>

                        </div>

                    </li>

                </s:iterator>

            </s:else>

        </ul>
    </div>

    <div style="clear:left;"></div>

    <s:if test="(!pager && childFiles.size() > 0) || (pager && pager.items.size() > 0) || (currentDirectory.name != 'default' && !pager)">
        <div class="control">

            <span style="padding-left:7px">
                <s:if test="(!pager && childFiles.size() > 0) || (pager && pager.items.size() > 0)">
                    <input id="toggleButton" type="button"
                       value='<s:text name="mediaFileView.toggleSelected" />' onclick="onToggle()" />

                    <input id="deleteButton" type="button"
                       value='<s:text name="mediaFileView.deleteSelected" />' onclick="onDeleteSelected()" />

                    <input id="moveButton" type="button"
                       value='<s:text name="mediaFileView.moveSelected" />' onclick="onMoveSelected()" />

                    <s:select id="moveTargetMenu" name="selectedDirectory" list="allDirectories" listKey="id" listValue="name" />
                </s:if>
            </span>

            <s:if test="currentDirectory.name != 'default' && !pager">
                <!-- need a clearfix to use this float -->
                <!-- <span style="float:right"> -->
                    <s:submit value="%{getText('mediaFileView.deleteFolder')}" action="mediaFileView!deleteFolder" onclick="onDeleteFolder();return false;"/>
                <!-- </span> -->
            </s:if>
        </div>
    </s:if>

</s:form>

</s:if>


<%-- ***************************************************************** --%>

<div id="mediafile_edit_lightbox">
    <div id="panelHeader" class="yui3-widget-hd"></div>
    <div class="yui3-widget-bd">
        <iframe id="mediaFileEditor"
                style="visibility:inherit"
                height="100%"
                width="100%"
                frameborder="no"
                scrolling="auto">
        </iframe>
    </div>
    <div class="yui3-widget-ft"></div>
</div>

<script>
    <%-- launch modal "lightbox" Media File Edit page --%>
    var mediapanel;
    YUI().use('panel', function (Y) {
        mediapanel = new Y.Panel({
            srcNode: '#mediafile_edit_lightbox',
            modal  : true,
            width  : 600,
            height : 700,
            visible: false,
            centered: true,
            constrain: true
        });
        mediapanel.render();
        <%-- Adding title bar text after it is made invisible above to avoid its
             brief appearance on the screen after saves within the panel.
        --%>
        document.getElementById('panelHeader').innerHTML = '<s:text name="mediaFileEdit.popupTitle"/>';
    });


    function onClickEdit(mediaFileId) {
        <s:url id="mediaFileEditURL" action="mediaFileEdit">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
        </s:url>
        $("#mediaFileEditor").attr('src',
            '<s:property value="%{mediaFileEditURL}" />' + '&mediaFileId=' + mediaFileId);
        mediapanel.show();
    }

    function onEditSuccess() {
        $("#mediaFileEditor").attr('src','about:blank');
        mediapanel.hide();
        document.getElementById('panelHeader').style.visibility = 'hidden';
        document.mediaFileViewForm.submit();
    }

    function onEditCancelled() {
        $("#mediaFileEditor").attr('src','about:blank');
        mediapanel.hide();
    }
</script>

<br/>
<br/>
<br/>