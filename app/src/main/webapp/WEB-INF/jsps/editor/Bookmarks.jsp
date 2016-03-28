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


<%--
Blogroll link management

We used to call them Bookmarks and Folders, now we call them Blogroll links and Blogrolls.
--%>

<%-- ================================================================================================ --%>

<%-- Main blogroll/folder management interface, a checkbox-table with some buttons  --%>

<p class="subtitle"> <s:text name="bookmarksForm.subtitle" > <s:param value="weblog" /> </s:text> </p>

<s:if test="folder.name == 'default'">
    <p class="pagetip"> <s:text name="bookmarksForm.rootPrompt" /> </p>
</s:if>


<%-- table of blogroll links with selection checkboxes, wrapped in a form --%>

<s:form action="bookmarks!delete" theme="bootstrap" cssClass="form-horizontal">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="folderId" />

    <%-- for default blogroll, show page "tip" and read-only folder name --%>

    <s:if test="folder.name == 'default'">

        <div class="form-group ">
            <label class="col-sm-3 control-label" for="bookmarks_folder_name">
                <s:text name="bookmarksForm.blogrollName"/>
            </label>
            <div class="col-sm-9 controls">
                <div class="form-control"> <s:text name="%{folder.name}"/> </div>
            </div>
        </div>

    </s:if>

    <s:if test="folder.name != 'default'">

        <%-- Blogroll / Folder Name --%>

        <%-- for other blogrolls, show textarea so user can rename it --%>
        <%-- don't use Struts tags here so button can be on same line as text input --%>

        <div class="form-group ">
            <label class="col-sm-3 control-label" for="bookmarks_folder_name">
                <s:text name="bookmarksForm.blogrollName"/>
            </label>
            <div class="col-sm-9 controls">
                <input style="width:55%; float:left" type="text" name="folder.name"
                       value="<s:text name='%{folder.name}'/>" id="bookmarks_folder_name" class="form-control"
                       onchange="nameChanged()"
                       onkeyup="nameChanged()" />
                <button type="button" id="rename_button"
                        class="btn btn-success" style="float:left; margin-left:1em;"
                        onclick="renameFolder(); return false;"
                        onsubmit="return false;" >
                    <s:text name="generic.rename"/>
                </button>
                <button type="button" id="rename_cancel"
                        class="btn btn-default" style="float:left; margin-left:1em;"
                        onclick="cancelRenameFolder(); return false;"
                        onsubmit="return false;" >
                    <s:text name="generic.cancel"/>
                </button>
            </div>
        </div>

    </s:if>

    <s:if test="!allFolders.isEmpty">

        <%-- allow user to select the bookmark folder to view --%>
        <s:select name="viewFolderId" list="allFolders" listKey="id" listValue="name"
                 label="%{getText('bookmarksForm.switchTo')}" onchange="viewChanged()" />

    </s:if>

    <table class="rollertable table table-striped">

        <tr class="rHeaderTr">
            <th width="5%">
                <input name="control" type="checkbox"
                       onclick="toggleFunctionAll(this.checked); selectionChanged()"
                title="<s:text name="bookmarksForm.selectAllLabel"/>"/>
            </th>
            <th class="rollertable" width="25%"><s:text name="generic.name" /></th>
            <th class="rollertable" width="70%"><s:text name="bookmarksForm.url" /></th>
            <th class="rollertable" width="5%"><s:text name="generic.edit" /></th>
        </tr>
        
        <s:if test="folder.bookmarks.size > 0">
        
        <%-- Blogroll links --%>

        <s:iterator id="bookmark" value="folder.bookmarks" status="rowstatus">

            <s:if test="#rowstatus.odd == true">
                <tr class="rollertable_odd">
            </s:if>
            <s:else>
                <tr class="rollertable_even">
            </s:else>
                
                <td class="rollertable center" style="vertical-align:middle">
                    <input type="checkbox" name="selectedBookmarks" onchange="selectionChanged()"
                        title="<s:text name="bookmarksForm.selectOneLabel"><s:param value="#bookmark.name"/></s:text>"
                        value="<s:property value="#bookmark.id"/>" />
                </td>
                
                <td>
                    <str:truncateNicely lower="40" upper="50" >
                        <s:property value="#bookmark.name" />
                    </str:truncateNicely>
                </td>
                
                <td>
                    <s:if test="#bookmark.url != null" >
                        <a href='<s:property value="#bookmark.url" />' target='_blank' >
                            <str:truncateNicely lower="70" upper="90" >
                                <s:property value="#bookmark.url" />
                            </str:truncateNicely>
                            <span class="glyphicon glyphicon-play-circle"></span>
                        </a>
                    </s:if>

                </td>
                
                <td align="center">
                    <s:url var="editUrl" action="bookmarkEdit">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                        <s:param name="bean.id" value="#bookmark.id" />
                        <s:param name="folderId" value="%{folderId}" suppressEmptyParameters="true"/>
                    </s:url>
                    <s:a href="%{editUrl}"> <span class="glyphicon glyphicon-edit"></span> </s:a>
                </td>
                
            </tr>

        </s:iterator>
        
        </s:if>

        <s:else>
            <tr>
                <td style="vertical-align:middle; padding-top: 1em;" colspan="7">
                    <s:text name="bookmarksForm.noresults" />
                </td>
            </tr>
        </s:else>

    </table>

    <%-- Add new blogroll link --%>
    <button type="button"
            class="btn btn-success" style="float:left; margin-right: 0.5em"
            onclick="addBookmark();return false;">
        <s:text name="bookmarksForm.addBookmark"/>
    </button>

    <s:if test="folder.bookmarks.size > 0">

        <%-- Delete-selected button --%>
        <input id="delete_selected"
            value="<s:text name="bookmarksForm.delete"/>"
            type="button" class="btn btn-danger" style="float:right; margin-right: 0.5em"
            onclick="deleteFolder();return false;"/>

    </s:if>

    <s:if test="!allFolders.isEmpty && folder.bookmarks.size > 0">

        <%-- Move-to combo-box --%>
        <s:select name="targetFolderId"
            theme="simple" cssClass="form-control" cssStyle="float:right; width:30%; margin-right: 5em"
            list="allFolders" listKey="id" listValue="name"/>

        <%-- Move-selected button --%>
        <s:submit id="move_selected"
            value="%{getText('bookmarksForm.move')}"
            theme="simple" cssClass="btn btn-warning" cssStyle="float:right; margin-right: 0.5em"
            action="bookmarks!move"
            onclick="onMoveToFolder();return false;"/>

    </s:if>

    <s:if test="folder.name != 'default'">

        <%-- Delete the whole blogroll --%>

        <s:submit value="%{getText('bookmarksForm.deleteFolder')}"
            theme="simple" cssClass="btn btn-danger" cssStyle="float:right; clear:left; margin-top:2em"
            action="bookmarks!deleteFolder"
            onclick="deleteFolder();return false;"/>

    </s:if>

</s:form>


<%-- -------------------------------------------------------- --%>

<%-- JavaScript for Main blogroll/folder management interface --%>

<script>

    var originalName;
    var renameButton;
    var renameCancel;
    var deleteSelectedButton;
    var moveSelectedButton;
    var viewSelector;
    var moveToSelector;

    $( document ).ready(function() {

        originalName = $("#bookmarks_folder_name:first").val();

        if ( !originalName ) {
            originalName = 'default';
        }

        renameButton         = $("#rename_button:first");
        renameCancel         = $("#rename_cancel:first");
        deleteSelectedButton = $("#delete_selected:first");
        moveSelectedButton   = $("#move_selected:first");
        viewSelector         = $("#bookmarks_viewFolderId:first");
        moveToSelector       = $("#bookmarks_targetFolderId:first");

        nameChanged();
        selectionChanged();

        // add the "New Blogroll" option to blogroll selectors
        viewSelector.append(
                new Option('<s:text name="bookmarksForm.newBlogroll"/>', "new_blogroll" ));
        moveToSelector.append(
                new Option( '<s:text name="bookmarksForm.newBlogroll"/>', "new_blogroll" ));
    });

    function nameChanged() {
        var newName = $("#bookmarks_folder_name:first").val();
        if ( newName != originalName ) {
            renameButton.attr("disabled", false );
            renameButton.addClass("btn-success");

            renameCancel.attr("disabled", false );

        } else {
            renameButton.attr("disabled", true );
            renameButton.removeClass("btn-success");

            renameCancel.attr("disabled", true );
        }
    }

    function selectionChanged() {
        var checked = false;
        var selected = $("[name=selectedBookmarks]");
        for ( var i in selected ) {
            if ( selected[i].checked ) {
                checked = true;
                break;
            }
        }
        if ( checked ) {
            deleteSelectedButton.attr("disabled", false );
            deleteSelectedButton.addClass("btn-danger");

            moveSelectedButton.attr("disabled", false );
            moveSelectedButton.addClass("btn-warning");

            moveToSelector.attr("disabled", false);

        } else {
            deleteSelectedButton.attr("disabled", true );
            deleteSelectedButton.removeClass("btn-danger");

            moveSelectedButton.attr("disabled", true );
            moveSelectedButton.removeClass("btn-warning");

            moveToSelector.attr("disabled", true);
        }
    }

    function renameFolder( event ) {
        event.preventDefault();
    }

    function cancelRenameFolder( event ) {
        $("#bookmarks_folder_name:first").val( originalName );
        nameChanged();
    }

    function confirmDelete() {
        // TODO: do not use plain old DHTML confirm here
        if (confirm("<s:text name='bookmarksForm.delete.confirm' />")) {
            document.bookmarks.submit();
        }
    }

    function deleteFolder() {

        $('#boomarks_delete_folder_folderId').val('<s:text name="%{folder.id}"/>');

        $('#deleteBlogrollName').html('<s:text name="%{folder.name}"/>');

        $('#delete-blogroll-modal').modal({show: true});

        <%--
        if (confirm("<s:text name='bookmarksForm.deleteFolder.confirm' />")) {
            document.bookmarks.action = '<s:url action="bookmarks!deleteFolder" />';
            document.bookmarks.submit();
        }
        --%>
    }

    function onMoveToFolder() {
        // TODO: do not use plain old DHTML confirm here
        if (confirm("<s:text name='bookmarksForm.move.confirm' />")) {
            document.bookmarks.action = '<s:url action="bookmarks!move" />';
            document.bookmarks.submit();
        }
    }

    function viewChanged() {

        var bookmarksForm = $("#bookmarks")[0];
        var folderEditForm = $("#folderEditForm")[0];

        if ( "new_blogroll" == bookmarksForm.viewFolderId.value ) {

            // user selected New Blogroll option, show the add/edit blogroll modal
            $('#blogroll-edit-title').html('<s:text name="bookmarksForm.addBlogroll.title" />');

            folderEditForm.action = "folderAdd!save.rol";
            folderEditForm.actionName.value = "folderAdd";

            $('#addedit-bookmarkfolder-modal').modal({show: true});

        } else {
            // user changed the blogroll/folder, post to "view" action to get new page
            bookmarksForm.action = "bookmarks!view.rol";
            bookmarksForm.submit();
        }
    }

</script>


<%-- ================================================================================================ --%>

<%-- add/edit blogroll / folder form --%>

<div id="addedit-bookmarkfolder-modal" class="modal fade addedit-bookmarkfolder-modal" tabindex="-1" role="dialog">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <div class="modal-header">
                <h3 id="blogroll-edit-title"></h3>
            </div>

            <div class="modal-body">
                <s:form action="#" id="folderEditForm" theme="bootstrap" cssClass="form-horizontal">
                    <s:hidden name="salt" />
                    <s:hidden name="actionName" />
                    <s:hidden name="weblog" />
                    <s:hidden name="bean.id" />

                    <%-- action needed here because we are using AJAX to post this form --%>
                    <s:hidden name="action:folderEdit!save" value="save"/>

                    <s:textfield name="bean.name" label="%{getText('generic.name')}" maxlength="255"/>
                </s:form>
            </div> <!-- modal-body-->

            <div class="modal-footer">
                <p id="feedback-area-blogroll-edit"></p>
                <button onclick="submitEditedBlogroll()" class="btn btn-primary">
                    <s:text name="generic.save"/>
                </button>
                <button type="button" class="btn" data-dismiss="modal">
                    <s:text name="generic.cancel"/>
                </button>
            </div>

        </div> <!-- modal-content-->

    </div> <!-- modal-dialog -->

</div>

<script>

    <%-- JavaScript for add/edit blogroll modal --%>

    function submitEditedBlogroll() {

        var folderEditForm = $("#folderEditForm")[0];

        var feedbackAreaBlogrollEdit = $("#feedback-area-blogroll-edit");

        // if name is empty reject and show error message
        if ($("#folderEditForm_bean_name").val().trim() == "") {
            feedbackAreaBlogrollEdit.html('<s:text name="bookmarksForm.blogroll.requiredFields" />');
            feedbackAreaBlogrollEdit.css("color", "red");
            return;
        }

        // post blogroll via AJAX
        $.ajax({
            method: 'post',
            url: folderEditForm.attributes["action"].value,
            data: $("#folderEditForm").serialize(),
            context: document.body

        }).done(function (data) {

            // kludge: scrape response status from HTML returned by Struts
            var alertEnd = data.indexOf("ALERT_END");
            if (data.indexOf('<s:text name="bookmarkForm.error.duplicateName" />') < alertEnd) {
                feedbackAreaBlogrollEdit.css("color", "red");
                feedbackAreaBlogrollEdit.html('<s:text name="bookmarkForm.error.duplicateName" />');

            } else {
                feedbackAreaBlogrollEdit.css("color", "green");
                feedbackAreaBlogrollEdit.html('<s:text name="generic.success" />');
                $('#blogroll-edit-modal').modal("hide");
                location.reload(true);
            }

        }).error(function (data) {
            feedbackAreaBlogrollEdit.html('<s:text name="generic.error.check.logs" />');
            feedbackAreaBlogrollEdit.css("color", "red");
        });
    }

</script>


<%-- ========================================================================================== --%>

<%-- delete blogroll confirmation modal --%>

<div id="delete-blogroll-modal" class="modal fade delete-blogroll-modal" tabindex="-1" role="dialog">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <div class="modal-header">
                <h3>
                    <s:text name="blogrollDeleteOK.removeBlogroll"/>:
                    <span id="blogroll-name"></span>
                </h3>
            </div>

            <s:form id="boomarks_delete_folder" action="bookmarks!deleteFolder" theme="bootstrap" cssClass="form-horizontal">
                <s:hidden name="salt"/>
                <s:hidden name="weblog"/>
                <s:hidden name="folderId"/>

                <div class="modal-body">
                    <s:text name="blogrollDeleteOK.areYouSure"></s:text>
                    <span id="deleteBlogrollName"></span>?
                </div>

                <div class="modal-footer">
                    <s:submit cssClass="btn" value="%{getText('generic.yes')}"/>&nbsp;
                    <button type="button" class="btn btn-default btn-primary" data-dismiss="modal">
                        <s:text name="generic.no" />
                    </button>
                </div>

            </s:form>

        </div>
    </div>
</div>

<script>

    function showBlogrollDeleteModal( id, name ) {
        $('#blogrollRemove_removeId').val(id);
        $('#blogroll-name').html(name);
        $('#delete-remove-modal').modal({show: true});
    }

</script>


<%-- ================================================================================================ --%>

<%-- add/edit link form: a modal --%>

<div id="addedit-bookmark-modal" class="modal fade addedit-bookmark-modal" tabindex="-1" role="dialog">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <div class="modal-header">

                <%-- Titling, processing actions different between add and edit --%>
                <s:if test="actionName == 'bookmarkEdit'">
                    <s:set var="subtitleKey">bookmarkForm.edit.subtitle</s:set>
                    <s:set var="mainAction">bookmarkEdit</s:set>
                </s:if>
                <s:else>
                    <s:set var="subtitleKey">bookmarkForm.add.subtitle</s:set>
                    <s:set var="mainAction">bookmarkAdd</s:set>
                </s:else>

                <h3>
                    <s:text name="%{#subtitleKey}" > </s:text> <span id="subtitle_folder_name"></span>
                </h3>

                <div id="bookmark_required_fields" role="alert" class="alert">
                    <s:text name="bookmarkForm.requiredFields" />
                </div>

            </div> <%-- modal header --%>

            <s:form action="bookmark" theme="bootstrap" cssClass="form-horizontal">
                <s:hidden name="salt" />
                <s:hidden name="weblog" />
                <%--
                    Edit action uses folderId for redirection back to proper bookmarks folder on cancel
                    (as configured in struts.xml); add action also, plus to know which folder to put new
                    bookmark in.
                --%>
                <s:hidden name="folderId" />
                <s:if test="actionName == 'bookmarkEdit'">
                    <%-- bean for bookmark add does not have a bean id yet --%>
                    <s:hidden name="bean.id" />
                </s:if>

                <div class="modal-body">

                    <s:textfield name="bean.name" maxlength="255"
                                 onchange="onBookmarkFormChanged()"
                                 onkeyup ="onBookmarkFormChanged()"
                                 label="%{getText('generic.name')}" />

                    <s:textfield name="bean.url" maxlength="255"
                                 onchange="onBookmarkFormChanged()"
                                 onkeyup ="onBookmarkFormChanged()"
                                 label="%{getText('bookmarkForm.url')}" />

                    <s:textfield name="bean.feedUrl" maxlength="255"
                                 onchange="onBookmarkFormChanged()"
                                 onkeyup ="onBookmarkFormChanged()"
                                 label="%{getText('bookmarkForm.rssUrl')}" />

                    <s:textfield name="bean.description" maxlength="255"
                                 onchange="onBookmarkFormChanged()"
                                 onkeyup ="onBookmarkFormChanged()"
                                 label="%{getText('generic.description')}" />

                    <s:textfield name="bean.image" maxlength="255"
                                 onchange="onBookmarkFormChanged()"
                                 onkeyup ="onBookmarkFormChanged()"
                                 label="%{getText('bookmarkForm.image')}" />
                </div>

                <div class="modal-body">
                    <div class="modal-footer">
                        <p id="feedback-area-edit"></p>
                        <button type="button" id="save_bookmark" onclick="saveBookmark()" class="btn btn-primary">
                            <s:text name="generic.save"/>
                        </button>
                        <button type="button" class="btn" data-dismiss="modal">
                            <s:text name="generic.cancel"/>
                        </button>
                    </div>
                </div>

            </s:form>

        </div> <%-- modal content --%>

    </div> <%-- modal dialog --%>

</div> <%-- modal --%>


<%-- ---------------------------------------------------- --%>

<%-- JavaScript for add/edit link modal --%>

<script>

    function addBookmark() {

        var saveBookmarkButton = $('#save_bookmark:first');
        saveBookmarkButton.attr("disabled", true );

        var elem = $('#bookmark_required_fields:first');
        elem.html('<s:text name="bookmarkForm.requiredFields" />');
        elem.removeClass("alert-success");
        elem.removeClass("alert-danger");
        elem.addClass("alert-info");

        $('#bookmark_bean_name:first').val('');
        $('#bookmark_bean_url:first').val('');
        $('#bookmark_bean_image:first').val('');
        $('#bookmark_bean_feedUrl:first').val('');

        $('#subtitle_folder_name:first').html(originalName);

        $('#addedit-bookmark-modal').modal({show: true});
    }

    function onBookmarkFormChanged() {

        var saveBookmarkButton = $('#save_bookmark:first');

        var name    = $('#bookmark_bean_name:first').val().trim();
        var url     = $('#bookmark_bean_url:first').val().trim();
        var image   = $('#bookmark_bean_image:first').val().trim();
        var feedUrl = $('#bookmark_bean_feedUrl:first').val().trim();

        var badUrls = [];

        if ( url.length > 0 ) {
            if ( !isValidUrl(url) ) {
                badUrls.push("Bookmark URL")
            }
        }

        if ( image.length > 0 ) {
            if ( !isValidUrl(image) ) {
                badUrls.push("Image URL")
            }
        }

        if ( feedUrl.length > 0 ) {
            if ( !isValidUrl(feedUrl) ) {
                badUrls.push("Newsfeed URL")
            }
        }

        var elem = $('#bookmark_required_fields:first');
        var message = '';

        if ( name.length > 0 && url.length > 0 && badUrls.length == 0 ) {
            saveBookmarkButton.attr("disabled", false);

            message = '<s:text name="generic.looksGood" />';
            elem.removeClass("alert-info");
            elem.removeClass("alert-danger");
            elem.addClass("alert-success");
            elem.html(message);

        } else {
            saveBookmarkButton.attr("disabled", true);

            if ( name.length == 0 || url.length == 0 ) {
                message = '<s:text name="bookmarkForm.required" />';
            }
            if ( badUrls.length > 0 ) {
                message = '<s:text name="bookmarkForm.badUrls" />';
                var sep = " ";
                for ( i in badUrls ) {
                    message = message + sep + badUrls[i];
                    sep  = ", ";
                }
            }
            elem.removeClass("alert-info");
            elem.removeClass("alert-success");
            elem.addClass("alert-danger");
            elem.html(message);
        }
    }

    function isValidUrl( url ) {
        if(/^(http|https|ftp):\/\/[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/i.test(url)) {
            return true;
        } else {
            return false;
        }
    }

    function saveBookmark() {

        var feedbackAreaEdit = $("#feedback-area-edit");

        // post bookmark via AJAX
        $.ajax({
            method: 'post',
            url: "bookmarkEdit!save.rol",
            data: $("#bookmark").serialize(),
            context: document.body

        }).done(function (data) {

            // kludge: scrape response status from HTML returned by Struts
            var alertEnd = data.indexOf("ALERT_END");
            if (data.indexOf('<s:text name="bookmarkForm.error.duplicateName" />') < alertEnd) {
                feedbackAreaEdit.css("color", "red");
                feedbackAreaEdit.html('<s:text name="bookmarkForm.error.duplicateName" />');

            } else {
                feedbackAreaEdit.css("color", "green");
                feedbackAreaEdit.html('<s:text name="generic.success" />');
                $('#category-edit-modal').modal("hide");
                location.reload(true);
            }

        }).error(function (data) {
            feedbackAreaEdit.html('<s:text name="generic.error.check.logs" />');
            feedbackAreaEdit.css("color", "red");
        });
    }

</script>
