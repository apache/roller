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

<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/styles/yui/container.css'/>" />
<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/styles/yui/menu.css'/>" />

<script type="text/javascript" src="<s:url value='/roller-ui/scripts/yui/yahoo-dom-event.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/scripts/yui/container_core-min.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/scripts/yui/menu-min.js'/>"></script>

<script type="text/javascript" src="<s:url value="/roller-ui/scripts/jquery-1.3.1.min.js" />"></script>


<style>
    body   {
        margin:0;
        padding:0;
        text-align:left;
    }
    h1     {
        font-size:20px;
        font-weight:bold;
    }
    .yui-overlay {
        position:fixed;
        background: #ffffff;
        z-index: 112;
        color:#000000;
        border: 4px solid #525252;
        text-align:left;
        top: 50%;
        left: 50%;
    }
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
    YAHOO.util.Event.onContentReady("myMenu", function () {
        var oClones = this;

        function findMediaFileIdForLineItem(lineItemNode) {
            var findMediaFileIdNode = function(node) {
                return (node.id == 'mediafileidentity');
            }
            var temp_elements = YAHOO.util.Dom.getElementsBy(findMediaFileIdNode,"input",lineItemNode);
            return temp_elements[0].value;
        }

        function deleteMedia(p_oLI) {
            var lineItemNode = YAHOO.util.Dom.getAncestorByTagName(p_oLI, "LI");
            var hidden_mediaFileId_value = findMediaFileIdForLineItem(lineItemNode);
            document.mediaFileViewForm.mediaFileId.value=hidden_mediaFileId_value;
            document.mediaFileViewForm.action='<s:url action="mediaFileView!delete" />';
            document.mediaFileViewForm.submit();
        }

        function createPost(p_oLI) {
            var lineItemNode = YAHOO.util.Dom.getAncestorByTagName(p_oLI, "LI");
            var hidden_mediaFileId_value = findMediaFileIdForLineItem(lineItemNode);
            document.mediaFileViewForm.mediaFileId.value = hidden_mediaFileId_value;
            document.mediaFileViewForm.action = '<s:url action="entryAddWithMediaFile"></s:url>';
            document.mediaFileViewForm.submit();
        }

        function includeMedia(p_oLI) {
            var lineItemNode = YAHOO.util.Dom.getAncestorByTagName(p_oLI, "LI");
            var hidden_mediaFileId_value = findMediaFileIdForLineItem(lineItemNode);
            document.mediaFileViewForm.mediaFileId.value = hidden_mediaFileId_value;
            document.mediaFileViewForm.action = '<s:url action="mediaFileView!includeInGallery" />';
            document.mediaFileViewForm.submit();
        }

        function onEweContextMenuClick(p_sType, p_aArgs) {

            var oItem = p_aArgs[1], // The MenuItem that was clicked
            oTarget = this.contextEventTarget,
            oLI;

            if (oItem) {

                oLI = oTarget.className == "contextMenu" ?
                    oTarget : YAHOO.util.Dom.getAncestorByClassName(oTarget, "contextMenu");

                switch (oItem.index) {

                    case 0:     // delete
                        deleteMedia(oLI);
                        break;
                    case 1:     // create post
                        createPost(oLI);
                        break;
                    case 2:     // include in gallery
                        includeMedia(oLI);
                        break;
                }
            }
        }

        /*
         Array of text labels for the MenuItem instances to be
         added to the ContextMenu instanc.
         */
        var aMenuItems = [
            '<s:text name="mediaFileView.delete" />',
            '<s:text name="mediaFileView.createPost" />',
            '<s:text name="mediaFileView.includeInGallery" />' ];

        /*
         Instantiate a ContextMenu:  The first argument passed to the constructor
         is the id for the Menu element to be created, the second is an
         object literal of configuration properties.
         */
        var oEweContextMenu = new YAHOO.widget.ContextMenu("ewecontextmenu", {
            trigger: oClones.getElementsByClassName("contextMenu"),
            itemdata: aMenuItems,
            lazyload: true
        });

        oEweContextMenu.configTrigger = configTrigger;

        oEweContextMenu.trigger = oClones.getElementsByClassName("contextMenu");

        // "render" event handler for the ewe context menu
        function onContextMenuRender(p_sType, p_aArgs) {
            //  Add a "click" event handler to the ewe context menu
            this.subscribe("click", onEweContextMenuClick);
        }

        // Add a "render" event handler to the ewe context menu
        oEweContextMenu.subscribe("render", onContextMenuRender);
    });

    YAHOO.example = function() {

        var $D = YAHOO.util.Dom;
        var $E = YAHOO.util.Event;
        return {
            init : function() {
                var overlay_img = new YAHOO.widget.Overlay("overlay_img", { fixedcenter:true,
                    visible:false,
                    width:"569px",height:"550px"
                });

                overlay_img.render();
                var overlay = document.createElement('div');
                overlay.id = 'overlay';
                // Assign 100% height and width
                overlay.style.width = '100%';
                overlay.style.height = '100%';

                document.getElementsByTagName('body')[0].appendChild(overlay);
                overlay.style.display = 'none';
            }
        };

    }();

    YAHOO.util.Event.addListener(window, "load", YAHOO.example.init);

    function configTrigger(p_sType, p_aArgs, p_oMenu) {
        window.alert("HI");
        var oTrigger = p_aArgs[0];
        if (oTrigger) {
            if (this._oTrigger) {
                this._removeEventHandlers();
            }
            this._oTrigger = oTrigger;
            Event.on(oTrigger, EVENT_TYPES.CONTEXT_MENU, this._onTriggerContextMenu, this, true);
            Event.on(oTrigger, EVENT_TYPES.CLICK, this._onTriggerClick, this, true);
        }
        else {
            this._removeEventHandlers();
        }
    }


</script>


<script type="text/javascript">
    <!--

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

    function onClose() {
        document.getElementById('overlay').style.display = 'none';
        document.getElementById('overlay_img').style.visibility = 'hidden';
    }

    function onClickEdit(mediaFileId) {
        var browser=navigator.appName;
        document.getElementById("overlay_img").style.visibility = "visible";
        document.getElementById('overlay').style.display = 'block';

        var frame = document.createElement('iframe');
        frame.setAttribute("id","myframe");
        frame.setAttribute("frameborder","no");
        frame.setAttribute("scrolling","auto");
        frame.setAttribute('src','<s:url action="mediaFileEdit"><s:param name="weblog" value="%{actionWeblog.handle}" /></s:url>&mediaFileId='+mediaFileId );
        frame.style.width="100%";
        frame.style.height="100%";
        if (browser=="Microsoft Internet Explorer")
        {
            document.getElementById("overlay_img").style.top= "40px";
            document.getElementById("overlay_img").style.left= "170px";
        }
        document.getElementById("overlay_img").innerHTML = '<div ><a href="#" class="container-close" onclick="onClose()"></a></div>';
        document.getElementById("overlay_img").appendChild(frame);

    }

    -->
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

<s:form id="mediaFileViewForm" name="mediaFileViewForm" action="mediaFileView" onsubmit="editorCleanup()">

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

                <%-- NOT SEARCH RESULTS --%>

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
                                <a class="contextMenu" href="#">
                                    <img  border="0"
                                        src='<s:url value="/images/control_play.png"/>' alt="[v]" />
                                </a>
                                <str:truncateNicely upper="50">
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
                                <a class="contextMenu" href="#">
                                    <img  border="0"
                                        src='<s:url value="/images/control_play.png"/>' alt="[v]" />
                                </a>
                                <str:truncateNicely upper="50">
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


    <%-- ***************************************************************** --%>

    <div style="clear:left;"></div>

    <div id="overlay_img" style="visibility:hidden">
    </div>


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

</s:form>

    <br/>
    <br/>
    <br/>

    <%-- Create new direcrtory --%>

    <div>
        <s:text name="mediaFileView.newDirName" />
        <input style="margin-top:5px;margin-bottom:5px;" type="text"
               name="newDirectoryName" size="30" />
        <input type="button" value='<s:text name="mediaFileView.create" />' onclick="onCreateDirectory()" />
    </div>

</s:if>

<%--
<div id="mediafile_edit_lightbox" style="visibility:hidden">
    <div class="hd">Media File Editor</div>
    <div class="bd">
        <iframe src="http://sun.com" style="visibility:inherit" height="0" width="0"></iframe>
    </div>
    <div class="ft"></div>
</div>
--%>

