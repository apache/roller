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

<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/styles/yui/reset-fonts-grids.css'/>" />
<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/styles/yui/container.css'/>" />
<link rel="stylesheet" type="text/css" href="<s:url value='/roller-ui/styles/yui/menu.css'/>" />

<script type="text/javascript" src="<s:url value='/roller-ui/scripts/yui/yahoo-dom-event.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/scripts/yui/container_core-min.js'/>"></script>
<script type="text/javascript" src="<s:url value='/roller-ui/scripts/yui/menu-min.js'/>"></script>



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

       /*
         The second item in the arguments array (p_aArgs)
         passed back to the "click" event handler is the
         MenuItem instance that was the target of the
         "click" event.
        */

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

         var aMenuItems = ["Delete", "Create Post", "Include in Gallery" ];

       /*
         Instantiate a ContextMenu:  The first argument passed to the constructor
         is the id for the Menu element to be created, the second is an
         object literal of configuration properties.
       */

         var oEweContextMenu = new YAHOO.widget.ContextMenu(
                                          "ewecontextmenu",
                                            {
                                                trigger: oClones.getElementsByClassName("contextMenu"),
                                                itemdata: aMenuItems,
                                                lazyload: true
                                            }
                                        );


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

</script>
<script type="text/javascript">
<!--

function onSelectDirectory(id) {
    document.mediaFileViewForm.directoryId.value = id;
    document.mediaFileViewForm.submit();
}

function onCreateDirectory()
{
    document.mediaFileViewForm.action='<s:url action="mediaFileView!createNewDirectory" />';
    document.mediaFileViewForm.submit();
}

function onDeleteSelected()
{
    if ( confirm("<s:text name='mediaFile.delete.confirm' />") ) {
        document.mediaFileViewForm.action='<s:url action="mediaFileView!deleteSelected" />';
        document.mediaFileViewForm.submit();
    }
}

function onMoveSelected()
{
    if ( confirm("<s:text name='mediaFile.move.confirm' />") ) {
        document.mediaFileViewForm.action='<s:url action="mediaFileView!moveSelected" />';
        document.mediaFileViewForm.submit();
    }
}
function onClose()
{
    document.getElementById('overlay').style.display = 'none';
    document.getElementById('overlay_img').style.visibility = 'hidden';
}
function onClickEdit(mediaFileId)
{
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

<p class="subtitle">
   View Uploaded Files
</p>
<p class="subtitle">
Path: /
<s:iterator id="directory" value="currentDirectoryHierarchy">
    <s:url id="getDirectoryByPathUrl" action="mediaFileView">
        <s:param name="directoryPath" value="#directory.key" />
        <s:param name="weblog" value="%{actionWeblog.handle}" />
    </s:url>
    <s:a href="%{getDirectoryByPathUrl}"><s:property value="#directory.value"
/></s:a> /
</s:iterator>
</p>

<s:form id="mediaFileViewForm" name="mediaFileViewForm" action="mediaFileView" onsubmit="editorCleanup()">
    <s:url id="mediaFileHierarchicalViewURL" action="mediaFileHierarchicalView">
    <s:param name="weblog" value="%{actionWeblog.handle}" />
    </s:url>
    <p><span style="font-weight:bold">Tabular</span> | <s:a href="%{mediaFileHierarchicalViewURL}">Hierarchical</s:a></p>
    <div class="control">
    <span style="padding-left:20px">Sort by:</span>
    <s:select name="sortBy" list="sortOptions" listKey="key" listValue="value" onchange="document.mediaFileViewForm.submit();" />
    </span>
    </div>

    <s:hidden name="weblog" />
    <s:hidden name="directoryId" />
    <input type="hidden" name="mediaFileId" value="" />

    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

  <div  width="720px" height="500px">
  <ul id = "myMenu">

    <s:iterator id="directory" value="childDirectories">
        <li class="align-images" >
        <div style="border:1px solid #000000;width:120px;height:100px;margin:5px;">
        <img  border="0" src='<s:url value="/images/folder.png"/>' class="dir-image" alt="mediaFolder.png" onclick="onSelectDirectory('<s:property value="#directory.id"/>')"/>
        </div>
        <div style="clear:left;width:130px;margin-left:10px;font-size:11px;"><label><s:property value="#directory.name" /></label>
        </div>
        </li>
    </s:iterator>


    <s:iterator id="mediaFile" value="childFiles">
        <li class="align-images" >
        <s:if test="#mediaFile.imageFile">
        <s:url id="mediaFileURL" value="/roller-ui/rendering/media-resources/%{#mediaFile.id}"></s:url>
        </s:if>
        <s:else>
        <s:url id="mediaFileURL" value="/images/page.png"></s:url>
        </s:else>
        <div style="border:1px solid #000000;width:120px;height:100px;margin:5px;">
        <img border="0" src='<s:property value="%{mediaFileURL}" />' <s:if test="#mediaFile.imageFile"> width="120px" height="100px" </s:if> <s:else>style="padding:40px 50px;"</s:else>/>
        </div>
        <div style="clear:left;width:130px;margin-left:5px;font-size:11px;"><label><s:property value="#mediaFile.name" /></label>
        <div style="padding-top:5px;">   <!--  one -->
        <input style="float:left;" type="checkbox" name="selectedMediaFiles" value="<s:property value="#mediaFile.id"/>"/>
        <INPUT TYPE="hidden" id="mediafileidentity" value="<s:property value='#mediaFile.id'/>">

        <div style="float:right;">
        <a  href="#" id="<s:property value='#mediaFile.id'/>" onclick="onClickEdit(this.id)">Edit</a>
        <a  class="contextMenu" href="#">More...</a>
        </div>
        </div>  <!-- one -->
        </div>
        </li>
    </s:iterator>

  </ul>
  </div>

 <div style="margin-left:320px;clear:left;">
 New Directory Name:
 <input style="margin-top:5px;margin-bottom:5px;" type="text"
 name="newDirectoryName" size="30" />
 <input type="button" value="Create" onclick="onCreateDirectory()" />
 </div>
 <div id="overlay_img" style="visibility:hidden">
 </div>




    <%-- ================================================================== --%>
    <%-- Weblog edit or preview --%>






    <%-- ================================================================== --%>
    <%-- plugin chooser --%>




    <%-- ================================================================== --%>
    <%-- advanced settings  --%>


    <%-- ================================================================== --%>
    <%-- the button box --%>

 <br/>
  <div class="control">
     <input type="button" style="padding-left:20px" value="Delete Selected" onclick="onDeleteSelected()" />
     <input type="button" style="padding-left:20px" value="Move Selected" onclick="onMoveSelected()" />
     <span style="padding-left:20px">
     <s:select name="selectedDirectory" list="allDirectories" listKey="id" listValue="path" />
     </span>
  </div>

</s:form>


