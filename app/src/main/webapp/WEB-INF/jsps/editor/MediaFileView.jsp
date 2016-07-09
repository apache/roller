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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>

<script>
  $(function() {
    $(".delete-file-link").click(function(e) {
      e.preventDefault();
      $('#confirm-delete-file').dialog('open');
    });
    $(".delete-folder-link").click(function(e) {
      e.preventDefault();
      $('#confirm-delete-folder').dialog('open');
    });
    $(".move-file-link").click(function(e) {
      e.preventDefault();
      $('#confirm-move-file').dialog('open');
    });

    $("#confirm-delete-file").dialog({
      autoOpen: false,
      resizable: true,
      height:200,
      modal: true,
      buttons: {
        "<s:text name='generic.delete'/>": function() {
          document.mediaFileViewForm.action='<s:url action="mediaFileView!deleteSelected" />';
          document.mediaFileViewForm.submit();
          $( this ).dialog( "close" );
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });
    $("#confirm-delete-folder").dialog({
      autoOpen: false,
      resizable: true,
      height:200,
      modal: true,
      buttons: {
        "<s:text name='generic.delete'/>": function() {
          document.mediaFileViewForm.action='<s:url action="mediaFileView!deleteFolder" />';
          document.mediaFileViewForm.submit();
          $( this ).dialog( "close" );
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });
    $("#confirm-move-file").dialog({
      autoOpen: false,
      resizable: true,
      height:200,
      modal: true,
      buttons: {
        "<s:text name='generic.yes'/>": function() {
          document.mediaFileViewForm.action='<s:url action="mediaFileView!moveSelected" />';
          document.mediaFileViewForm.submit();
          $( this ).dialog( "close" );
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });
  });
</script>

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
</style>


<script>
    toggleState = 'Off'

    function onSelectDirectory(id) {
        window.location = "<s:url action="mediaFileView" />?directoryId=" + id + "&weblogId=" + '<s:property value="%{actionWeblog.id}" />';
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

    function onView() {
        document.mediaFileViewForm.action = "<s:url action='mediaFileView!view' />";
        document.mediaFileViewForm.submit();
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


<%-- ********************************************************************* --%>

<%-- Subtitle and folder path --%>

<s:if test='currentDirectory.name.equals("default")'>

    <p class="subtitle">
        <s:text name="mediaFileView.subtitle" >
           <s:param value="actionWeblog.handle"/>
        </s:text>
    </p>
    </p>
    <p class="pagetip">
        <s:text name="mediaFileView.rootPageTip" />
    </p>

</s:if>

<s:else>
    <p class="subtitle">
        <s:text name="mediaFileView.folderName"/>: <s:text name="currentDirectory.name" />
    </p>
</s:else>

<s:if test="childFiles">

  <s:form id="mediaFileViewForm" name="mediaFileViewForm" action="mediaFileView">
    <sec:csrfInput/>
    <s:hidden name="weblogId" />
    <s:hidden name="directoryId" />
    <s:hidden name="newDirectoryName" />
    <input type="hidden" name="mediaFileId" value="" />

    <div class="control">
        <span style="padding-left:7px">
            <s:text name="mediaFileView.sortBy" />:
            <s:select id="sortByMenu" name="sortBy" list="sortOptions" listKey="left" listValue="right"
                  onchange="document.mediaFileViewForm.submit();" />
        </span>

        <span style="float:right">
            <s:if test="!allDirectories.isEmpty">
                <%-- Folder to View combo-box --%>
                <s:text name="mediaFileView.viewFolder" />:
                <s:select name="viewDirectoryId" list="allDirectories" listKey="id" listValue="name" onchange="onView()" />
            </s:if>
        </span>
    </div>

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

                <s:if test="childFiles.size() ==0">
                    <p style="text-align: center"><s:text name="mediaFileView.noFiles"/></p>
                </s:if>

                <%-- List media files --%>

                <s:iterator var="mediaFile" value="childFiles">

                    <li class="align-images"
                            onmouseover="highlight(this, true)" onmouseout="highlight(this, false)">

                        <div class="mediaObject">

                            <s:url var="editUrl" action="mediaFileEdit">
                                <s:param name="weblogId" value="%{actionWeblog.id}" />
                                <s:param name="directoryName" value="currentDirectory.name" />
                                <s:param name="mediaFileId" value="#mediaFile.id" />
                            </s:url>

                            <s:a href="%{editUrl}">
                                <s:if test="#mediaFile.imageFile">
                                    <img border="0" src='<s:property value="%{#mediaFile.thumbnailURL}" />'
                                         width='<s:property value="#mediaFile.thumbnailWidth"/>'
                                         height='<s:property value="#mediaFile.thumbnailHeight"/>'
                                         alt='<s:property value="#mediaFile.altText" />'
                                         title='<s:property value="#mediaFile.name" />' />
                                </s:if>

                                <s:else>
                                    <s:url var="mediaFileURL" value="/images/page_white.png"></s:url>
                                    <img border="0" src='<s:property value="%{mediaFileURL}" />'
                                         alt='<s:property value="#mediaFile.altText" />'
                                         style="padding:40px 50px;" />
                                </s:else>
                            </s:a>
                        </div>

                        <div class="mediaObjectInfo">

                            <input type="checkbox"
                                   name="selectedMediaFiles"
                                   value="<s:property value="#mediaFile.id"/>" />
                            <input type="hidden" id="mediafileidentity"
                                   value="<s:property value='#mediaFile.id'/>" />

                            <str:truncateNicely lower="47" upper="47">
                                <s:property value="#mediaFile.name" />
                            </str:truncateNicely>
                       </div>
                    </li>
                </s:iterator>
        </ul>
    </div>

    <div style="clear:left;"></div>

    <div class="control clearfix" style="margin-top: 15px">

        <s:if test="childFiles.size() > 0">
            <span style="padding-left:7px;margin-top: 20px">
                <input id="toggleButton" type="button"
                   value='<s:text name="generic.toggle" />' onclick="onToggle()" />

                <input id="deleteButton" type="button"
                   value='<s:text name="mediaFileView.deleteSelected" />' class="delete-file-link"/>

                <input id="moveButton" type="button"
                   value='<s:text name="mediaFileView.moveSelected" />' class="move-file-link"/>

                <s:select id="moveTargetMenu" name="selectedDirectory" list="allDirectories" listKey="id" listValue="name" />
            </span>
        </s:if>

        <s:if test="currentDirectory.name != 'default'">
            <span style="float:right;">
                <s:submit value="%{getText('mediaFileView.deleteFolder')}" action="mediaFileView!deleteFolder" class="delete-folder-link"/>
            </span>
        </s:if>
    </div>

</s:form>

<div id="confirm-delete-file" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name='mediaFile.delete.confirm' /></p>
</div>

<div id="confirm-delete-folder" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name='mediaFile.deleteFolder.confirm' /></p>
</div>

<div id="confirm-move-file" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name='mediaFile.move.confirm' /></p>
</div>

</s:if>

<br/>
<br/>
<br/>
