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

<s:form id="createPostForm" action='entryAddWithMediaFile'>
    <s:hidden name="salt"/>
    <input type="hidden" name="weblog" value='<s:property value="actionWeblog.handle" />'/>
    <input type="hidden" name="selectedImage" id="selectedImage"/>
    <input type="hidden" name="type" id="type"/>
</s:form>


<%-- ********************************************************************* --%>

<%-- Subtitle and folder path --%>

<s:if test='currentDirectory.name.equals("default")'>

    <p class="subtitle">
        <s:text name="mediaFileView.subtitle">
            <s:param value="weblog"/>
        </s:text>
    </p>
    <p class="pagetip">
        <s:text name="mediaFileView.rootPageTip"/>
    </p>

</s:if>

<s:elseif test='pager'>

    <p class="subtitle">
        <s:text name="mediaFileView.searchTitle"/>
    </p>
    <p class="pagetip">

            <%-- display summary of the search results and terms --%>

        <s:if test="pager.items.size() > 0">
        <s:text name="mediaFileView.matchingResults">
            <s:param value="pager.items.size()"/>
        </s:text>
        </s:if>
        <s:else>
            <s:text name="mediaFileView.noResults"/>
        </s:else>
            <s:text name="mediaFileView.searchInfo"/>

    <ul>
        <s:if test="!bean.name.isEmpty()">
            <li>
                <s:text name="mediaFileView.filesNamed">
                    <s:param value="bean.name"/>
                </s:text>
            </li>
        </s:if>
        <s:if test="bean.size > 0">
            <li>
                <s:text name="mediaFileView.filesOfSize">
                    <s:param value='bean.sizeFilterTypeLabel'/>
                    <s:param value='bean.size'/>
                    <s:param value='bean.sizeUnitLabel'/>
                </s:text>
            </li>
        </s:if>
        <s:if test="!bean.type.isEmpty()">
            <li>
                <s:text name="mediaFileView.filesOfType">
                    <s:param value='bean.typeLabel'/>
                </s:text>
            </li>
        </s:if>
        <s:if test="!bean.tags.isEmpty()">
            <li>
                <s:text name="mediaFileView.filesTagged">
                    <s:param value="bean.tags"/>
                </s:text>
            </li>
        </s:if>
    </ul>

</s:elseif>

<s:else>

    <p class="subtitle">
        <s:text name="mediaFileView.folderName"/>: <s:property value="%{currentDirectory.name}"/>
    </p>
    <p class="pagetip">
        <s:text name="mediaFileView.dirPageTip"/>
    </p>

</s:else>


<s:if test="childFiles || (pager && pager.items.size() > 0)">

    <s:form id="mediaFileViewForm" name="mediaFileViewForm" action="mediaFileView" theme="bootstrap">
        <s:hidden name="salt"/>
        <s:hidden name="weblog"/>
        <s:hidden name="directoryId"/>
        <s:hidden name="newDirectoryName"/>
        <input type="hidden" name="mediaFileId" value=""/>

        <div class="image-controls">

            <s:if test="!allDirectories.isEmpty">
                <%-- Folder to View combo-box --%>
                <span><s:text name="mediaFileView.viewFolder"/>:</span>
                <s:select id="viewDirectoryMenu" name="viewDirectoryId"
                          list="allDirectories" listKey="id" listValue="name" onchange="onView()"/>
            </s:if>

            <span><s:text name="mediaFileView.sortBy"/>:</span>
            <s:select id="sortByMenu" name="sortBy" list="sortOptions" listKey="key" listValue="value"
                      onchange="document.mediaFileViewForm.submit();"/>

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

        <div id="imageGrid" class="panel panel-default">
            <div class="panel-body">

                <ul>

                    <s:if test="!pager">

                        <%-- ----------------------------------------------------- --%>

                        <%-- NOT SEARCH RESULTS --%>

                        <s:if test="childFiles.size() ==0">
                            <s:text name="mediaFileView.noFiles"/>
                        </s:if>

                        <%-- List media files --%>

                        <s:iterator var="mediaFile" value="childFiles">

                            <li class="align-images"
                                onmouseover="highlight(this, true)" onmouseout="highlight(this, false)">

                                <div class="mediaObject" onclick="onClickEdit(
                                        '<s:property value="#mediaFile.id"/>',
                                        '<s:property value="#mediaFile.name"/>' )">

                                    <s:if test="#mediaFile.imageFile">
                                        <img border="0" src='<s:property value="%{#mediaFile.thumbnailURL}" />'
                                             width='<s:property value="#mediaFile.thumbnailWidth" />'
                                             height='<s:property value="#mediaFile.thumbnailHeight" />'
                                             title='<s:property value="#mediaFile.name" />'
                                             alt='<s:property value="#mediaFile.name" />'
                                            <%-- onclick="onClickEdit('<s:property value="#mediaFile.id"/>')" --%> />
                                    </s:if>

                                    <s:else>
                                        <s:url var="mediaFileURL" value="/images/page.png"/>
                                        <img border="0" src='<s:property value="%{mediaFileURL}" />'
                                             style="padding:40px 50px;"
                                             alt='<s:property value="#mediaFile.name"/>'
                                            <%-- onclick="onClickEdit('<s:property value="#mediaFile.id"/>')" --%> />
                                    </s:else>

                                </div>

                                <div class="mediaObjectInfo">

                                    <input type="checkbox"
                                           name="selectedMediaFiles"
                                           value="<s:property value="#mediaFile.id"/>"/>
                                    <input type="hidden" id="mediafileidentity"
                                           value="<s:property value='#mediaFile.id'/>"/>

                                    <str:truncateNicely lower="47" upper="47">
                                        <s:property value="#mediaFile.name"/>
                                    </str:truncateNicely>

                                </div>

                            </li>

                        </s:iterator>

                    </s:if>

                    <s:else>

                        <%-- ----------------------------------------------------- --%>

                        <%-- SEARCH RESULTS --%>

                        <s:iterator var="mediaFile" value="pager.items">

                            <li class="align-images"
                                onmouseover="highlight(this, true)" onmouseout="highlight(this, false)">

                                <div class="mediaObject" onclick="onClickEdit(
                                        '<s:property value="#mediaFile.id"/>',
                                        '<s:property value="#mediaFile.name"/>' )">

                                    <s:if test="#mediaFile.imageFile">
                                        <img border="0" src='<s:property value="%{#mediaFile.thumbnailURL}" />'
                                             width='<s:property value="#mediaFile.thumbnailWidth"/>'
                                             height='<s:property value="#mediaFile.thumbnailHeight"/>'
                                             title='<s:property value="#mediaFile.name" />'
                                             alt='<s:property value="#mediaFile.name"/>'/>
                                    </s:if>

                                    <s:else>
                                        <s:url var="mediaFileURL" value="/images/page.png"/>
                                        <img border="0" src='<s:property value="%{mediaFileURL}" />'
                                             style="padding:40px 50px;" alt='<s:property value="#mediaFile.name"/>'/>
                                    </s:else>

                                </div>

                                <div class="mediaObjectInfo">

                                    <input type="checkbox"
                                           name="selectedMediaFiles"
                                           value="<s:property value="#mediaFile.id"/>"/>
                                    <input type="hidden" id="mediafileidentity"
                                           value="<s:property value='#mediaFile.id'/>">

                                    <str:truncateNicely lower="40" upper="50">
                                        <s:property value="#mediaFile.name"/>
                                    </str:truncateNicely>

                                    <span class="button" id="addbutton-<s:property value='#mediaFile.id' />">
                                    <img id="addbutton-img<s:property value='#mediaFile.id' />"
                                         src="<s:url value="/images/add.png"/>"/>
                                </span>

                                </div>

                            </li>

                        </s:iterator>

                    </s:else>

                </ul>

            </div>
        </div>

        <div style="clear:left;"></div>

        <s:if test="(!pager && childFiles.size() > 0) || (pager && pager.items.size() > 0) || (currentDirectory.name != 'default' && !pager)">

            <div class="image-controls">

                <s:if test="(!pager && childFiles.size() > 0) || (pager && pager.items.size() > 0)">
                    <input id="toggleButton" type="button" class="btn" style="display: inline"
                           value='<s:text name="generic.toggle" />' onclick="onToggle()"/>

                    <input id="deleteButton" type="button" class="btn btn-danger" style="display: inline"
                           value='<s:text name="mediaFileView.deleteSelected" />' onclick="onDeleteSelected()"/>

                    <input id="moveButton" type="button" class="btn btn-primary" style="display: inline"
                           value='<s:text name="mediaFileView.moveSelected" />' onclick="onMoveSelected()"/>

                </s:if>

                <s:select id="moveTargetMenu" name="selectedDirectory" cssStyle="display: inline; width: 15em"
                          list="allDirectories" listKey="id" listValue="name"/>

                <s:if test="currentDirectory.name != 'default' && !pager">
                    <s:submit value="%{getText('mediaFileView.deleteFolder')}" cssClass="btn"
                              action="mediaFileView!deleteFolder" onclick="onDeleteFolder();return false;"/>
                </s:if>

            </div>

        </s:if>

    </s:form>

</s:if>


<%-- ================================================================================================ --%>

<%-- view image modal --%>

<div id="mediafile_edit_lightbox" class="modal fade" tabindex="-1" role="dialog">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <div class="modal-header">
                <h3 class="subtitle">
                    <s:text name="mediaFileEdit.subtitle"/><b><span id="edit-subtitle"></span></b>
                </h3>
            </div>

            <div class="modal-body">
                <iframe id="mediaFileEditor"
                        style="visibility:inherit"
                        height="700" <%-- pixels, sigh, this is suboptimal--%>
                        width="100%"
                        frameborder="no"
                        scrolling="auto">
                </iframe>
            </div>

            <div class="modal-footer"></div>

        </div>
    </div>

</div>


<script>
    toggleState = 'Off'

    function onClickEdit(mediaFileId, mediaFileName) {
        <s:url var="mediaFileEditURL" action="mediaFileEdit">
        <s:param name="weblog" value="%{actionWeblog.handle}" />
        </s:url>
        $('#edit-subtitle').html(mediaFileName)
        $('#mediaFileEditor').attr('src', '<s:property value="%{mediaFileEditURL}" />' + '&mediaFileId=' + mediaFileId);
        $('#mediafile_edit_lightbox').modal({show: true});
    }

    function onEditSuccess() {
        onEditCancelled();
        document.mediaFileViewForm.submit();
    }

    function onEditCancelled() {
        $('#mediafile_edit_lightbox').modal('hide');
        $("#mediaFileEditor").attr('src', 'about:blank');
    }

    function onSelectDirectory(id) {
        window.location = "<s:url action="mediaFileView" />?directoryId="
            + id + "&weblog=" + '<s:property value="actionWeblog.handle" />';
    }

    function onToggle() {
        if (toggleState === 'Off') {
            toggleState = 'On';
            toggleFunction(true, 'selectedMediaFiles');
            $("#deleteButton").attr('disabled', false);
            $("#moveButton").attr('disabled', false);
            $("#moveTargetMenu").attr('disabled', false);
        } else {
            toggleState = 'Off';
            toggleFunction(false, 'selectedMediaFiles');
            $("#deleteButton").attr('disabled', true);
            $("#moveButton").attr('disabled', true);
            $("#moveTargetMenu").attr('disabled', true)
        }
    }

    function onDeleteSelected() {
        if (confirm("<s:text name='mediaFile.delete.confirm' />")) {
            document.mediaFileViewForm.action = '<s:url action="mediaFileView!deleteSelected" />';
            document.mediaFileViewForm.submit();
        }
    }

    function onDeleteFolder() {
        if (confirm("<s:text name='mediaFile.deleteFolder.confirm' />")) {
            document.bookmarks.action = '<s:url action="mediaFileView!deleteFolder" />';
            document.bookmarks.submit();
        }
    }

    function onMoveSelected() {
        if (confirm("<s:text name='mediaFile.move.confirm' />")) {
            document.mediaFileViewForm.action = '<s:url action="mediaFileView!moveSelected" />';
            document.mediaFileViewForm.submit();
        }
    }

    function onView() {
        document.mediaFileViewForm.action = "<s:url action='mediaFileView!view' />";
        document.mediaFileViewForm.submit();
    }

    <%-- code to toggle buttons on/off as media file/directory selections change --%>

    $(document).ready(function () {
        $("#deleteButton").attr('disabled', true);
        $("#moveButton").attr('disabled', true);
        $("#moveTargetMenu").attr('disabled', true);

        $("input[type=checkbox]").change(function () {
            var count = 0;
            $("input[type=checkbox]").each(function (index, element) {
                if (element.checked) count++;
            });
            if (count === 0) {
                $("#deleteButton").attr('disabled', true);
                $("#moveButton").attr('disabled', true);
                $("#moveTargetMenu").attr('disabled', true)
            } else {
                $("#deleteButton").attr('disabled', false);
                $("#moveButton").attr('disabled', false);
                $("#moveTargetMenu").attr('disabled', false)
            }
        });
    });

</script>