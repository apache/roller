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

<p class="subtitle"><s:text name="bookmarksForm.subtitle"> <s:param value="weblog"/> </s:text></p>

<s:if test="folder.name == 'default'">
    <p class="pagetip"><s:text name="bookmarksForm.rootPrompt"/></p>
</s:if>
<s:else>
    <p class="pagetip"><s:text name="bookmarksForm.otherPrompt"/></p>
</s:else>


<%-- table of blogroll links with selection checkboxes, wrapped in a form --%>

<s:form action="bookmarks!delete" theme="bootstrap" cssClass="form-horizontal">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <s:hidden name="folderId"/>

    <%-- for default blogroll, show page "tip" and read-only folder name --%>

    <s:if test="folder.name == 'default'">

        <div class="form-group ">
            <label class="col-sm-3 control-label" for="bookmarks_folder_name">
                <s:text name="bookmarksForm.blogrollName"/>
            </label>
            <div class="col-sm-9 controls">
                <div class="form-control"><s:text name="%{folder.name}"/></div>
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
                       onkeyup="nameChanged()"/>
                <button type="button" id="rename_button"
                        class="btn btn-success" style="float:left; margin-left:1em;"
                        onclick="renameFolder(); return false;"
                        onsubmit="return false;">
                    <s:text name="generic.rename"/>
                </button>
                <button type="button" id="rename_cancel"
                        class="btn btn-default" style="float:left; margin-left:1em;"
                        onclick="cancelRenameFolder(); return false;"
                        onsubmit="return false;">
                    <s:text name="generic.cancel"/>
                </button>
            </div>
        </div>

    </s:if>

    <%-- allow user to select the bookmark folder to view --%>

    <s:select name="viewFolderId" list="allFolders" listKey="id" listValue="name" emptyOption="true"
              label="%{getText('bookmarksForm.switchTo')}" onchange="viewChanged()" onmouseup="viewChanged()"/>

    <table class="rollertable table table-striped">

        <tr class="rHeaderTr">
            <th width="5%">
                <input name="control" type="checkbox"
                       onclick="toggleFunctionAll(this.checked); selectionChanged()"
                       title="<s:text name="bookmarksForm.selectAllLabel"/>"/>
            </th>
            <th class="rollertable" width="25%"><s:text name="generic.name"/></th>
            <th class="rollertable" width="70%"><s:text name="bookmarksForm.url"/></th>
            <th class="rollertable" width="5%"><s:text name="generic.edit"/></th>
        </tr>

        <s:if test="folder.bookmarks.size > 0">

            <%-- Bookmarks --%>
            <s:iterator var="bookmark" value="folder.bookmarks" status="rowstatus">
                <tr class="rollertable_odd">

                    <td class="rollertable center" style="vertical-align:middle">
                        <input type="checkbox" name="selectedBookmarks" onchange="selectionChanged()"
                               title="<s:text name="bookmarksForm.selectOneLabel"><s:param value="#bookmark.name"/></s:text>"
                               value="<s:property value="#bookmark.id"/>"/>
                    </td>

                    <td>
                        <str:truncateNicely lower="40" upper="50">
                            <s:property value="#bookmark.name"/>
                        </str:truncateNicely>
                    </td>

                    <td>
                        <s:if test="#bookmark.url != null">
                            <a href='<s:property value="#bookmark.url" />' target='_blank'>
                                <str:truncateNicely lower="70" upper="90">
                                    <s:property value="#bookmark.url"/>
                                </str:truncateNicely>
                                <span class="glyphicon glyphicon-play-circle"></span>
                            </a>
                        </s:if>

                    </td>

                    <td align="center">

                        <a href="#" onclick="editBookmark(
                                '<s:property value="#bookmark.id"/>',
                                '<s:property value="#bookmark.name"/>',
                                '<s:property value="#bookmark.url"/>',
                                '<s:property value="#bookmark.feedUrl"/>',
                                '<s:property value="#bookmark.description"/>',
                                '<s:property value="#bookmark.image"/>' )">
                            <span class="glyphicon glyphicon-edit"></span>
                        </a>

                    </td>

                </tr>

            </s:iterator>

        </s:if>

        <s:else>
            <tr>
                <td style="vertical-align:middle; padding-top: 1em;" colspan="7">
                    <s:text name="bookmarksForm.noresults"/>
                </td>
            </tr>
        </s:else>

    </table>

    <%-- Add new blogroll link --%>
    <button type="button" class="btn btn-success" onclick="addBookmark();return false;"
            style="float:left; margin-right: 2em">
        <s:text name="bookmarksForm.addBookmark"/>
    </button>

    <s:if test="!allFolders.isEmpty && folder.bookmarks.size > 0">
        <%-- Move-selected button --%>
        <s:submit id="move_selected" value="%{getText('bookmarksForm.move')}" theme="simple"
                  cssClass="btn btn-warning" cssStyle="float:left; margin-right: 0.5em"
                  action="bookmarks!move" onclick="onMoveToFolder();return false;"/>
        <%-- Move-to combo-box --%>
        <s:select name="targetFolderId" theme="simple"
                  cssClass="form-control" cssStyle="float:left; width:30%; margin-right: 2em"
                  list="allFolders" listKey="id" listValue="name"/>
    </s:if>

    <s:if test="folder.bookmarks.size > 0">
        <%-- Delete-selected button --%>
        <input id="delete_selected" value="<s:text name="bookmarksForm.delete"/>" type="button"
               class="btn btn-danger" style="float:left;"
               onclick="confirmDeleteSelected();return false;"/>
    </s:if>

    <s:if test="folder.name != 'default'">
        <%-- Delete the whole blogroll --%>
        <s:submit value="%{getText('bookmarksForm.deleteFolder')}" theme="simple"
                  cssClass="btn btn-danger" cssStyle="float:right; clear:left; margin-top:2em"
                  action="bookmarks!deleteFolder"
                  onclick="confirmDeleteFolder();return false;"/>

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

    $(document).ready(function () {

        originalName = $("#bookmarks_folder_name:first").val();

        if (!originalName) {
            originalName = 'default';
        }

        renameButton = $("#rename_button:first");
        renameCancel = $("#rename_cancel:first");
        deleteSelectedButton = $("#delete_selected:first");
        moveSelectedButton = $("#move_selected:first");
        viewSelector = $("#bookmarks_viewFolderId:first");
        moveToSelector = $("#bookmarks_targetFolderId:first");

        nameChanged();
        selectionChanged();

        // add the "New Blogroll" option to blogroll selectors
        viewSelector.append(new Option('<s:text name="bookmarksForm.newBlogroll"/>', "new_blogroll"));
    });


    function selectionChanged() {
        var checked = false;
        var selected = $("[name=selectedBookmarks]");
        for (var i in selected) {
            if (selected[i].checked) {
                checked = true;
                break;
            }
        }
        if (checked) {
            deleteSelectedButton.attr("disabled", false);
            deleteSelectedButton.addClass("btn-danger");

            moveSelectedButton.attr("disabled", false);
            moveSelectedButton.addClass("btn-warning");

            moveToSelector.attr("disabled", false);

        } else {
            deleteSelectedButton.attr("disabled", true);
            deleteSelectedButton.removeClass("btn-danger");

            moveSelectedButton.attr("disabled", true);
            moveSelectedButton.removeClass("btn-warning");

            moveToSelector.attr("disabled", true);
        }
    }

    function nameChanged() {
        var newName = $("#bookmarks_folder_name:first").val();
        if (newName && newName !== originalName && newName.trim().length > 0) {
            renameButton.attr("disabled", false);
            renameButton.addClass("btn-success");

            renameCancel.attr("disabled", false);

        } else {
            renameButton.attr("disabled", true);
            renameButton.removeClass("btn-success");

            renameCancel.attr("disabled", true);
        }
    }

    function renameFolder() {

        var newName = $("#bookmarks_folder_name:first").val();
        $("#folderEditForm_bean_name").val(newName);

        var folderId = $("#bookmarks_folderId").val();
        $("#folderEditForm_bean_id").val(folderId);

        // post blogroll via AJAX
        var folderEditForm = $("#folderEditForm")[0];
        $.ajax({
            method: 'post',
            url: folderEditForm.attributes["action"].value,
            data: $("#folderEditForm").serialize(),
            context: document.body

        }).done(function (data, status, response) {

            // kludge: scrape response status from HTML returned by Struts
            var alertEnd = data.indexOf("ALERT_END");
            var notUnique = data.indexOf('<s:text name="bookmarkForm.error.duplicateName" />');
            if (notUnique > 0 && notUnique < alertEnd) {
                alert('<s:text name="bookmarkForm.error.duplicateName" />');

            } else {
                originalName = newName;
                nameChanged();
            }

        }).error(function (data) {
            alert('<s:text name="generic.error.check.logs" />');
        });
    }

    function cancelRenameFolder(event) {
        $("#bookmarks_folder_name:first").val(originalName);
        nameChanged();
    }

    function confirmDeleteSelected() {
        $('#delete-links-modal').modal({show: true});
    }

    function deleteSelected() {
        document.bookmarks[0].submit();
    }

    function confirmDeleteFolder() {
        $('#boomarks_delete_folder_folderId').val($('#bookmarks_folderId:first').val());
        $('#deleteBlogrollName').html('<s:property value="%{folder.name}"/>');
        $('#delete-blogroll-modal').modal({show: true});
    }

    function onMoveToFolder() {
        var bookmarksForm = $("#bookmarks")[0];
        bookmarksForm.action = "bookmarks!move.rol";
        bookmarksForm.submit();
    }

    function viewChanged() {

        var bookmarksForm = $("#bookmarks")[0];
        var folderEditForm = $("#folderEditForm")[0];

        if ("new_blogroll" === bookmarksForm.viewFolderId.value) {
            newBlogroll();

        } else {
            // user changed the blogroll/folder, post to "view" action to get new page
            bookmarksForm.action = "bookmarks!view.rol";
            bookmarksForm.submit();
        }
    }

    function newBlogroll() {

        // user selected New Blogroll option, show the add/edit blogroll modal
        $('#blogroll-edit-title').html('<s:text name="bookmarksForm.addBlogroll.title" />');

        folderEditForm.action = "folderAdd!save.rol";
        folderEditForm.actionName.value = "folderAdd";

        // disable save button until valid name is entered

        $('#addedit-bookmarkfolder-modal').modal({show: true});

        onBlogrollFormChanged();
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
                <s:form action="folderEdit" id="folderEditForm" theme="bootstrap" cssClass="form-horizontal">
                    <s:hidden name="salt"/>
                    <s:hidden name="actionName"/>
                    <s:hidden name="weblog"/>
                    <s:hidden name="bean.id"/>

                    <%-- action needed here because we are using AJAX to post this form --%>
                    <s:hidden name="action:folderEdit!save" value="save"/>

                    <s:textfield name="bean.name" label="%{getText('generic.name')}" maxlength="255"
                                 onchange="onBlogrollFormChanged()"
                                 onkeyup="onBlogrollFormChanged()"
                    />
                </s:form>
            </div> <!-- modal-body-->

            <div class="modal-footer">
                <p id="feedback-area-blogroll-edit"></p>
                <button id="save_blogroll" onclick="submitEditedBlogroll()" class="btn btn-primary">
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

    function onBlogrollFormChanged() {

        var saveBlogrollButton = $('#save_blogroll:first');

        var name = $('#folderEditForm_bean_name:first').val().trim();

        if (name.length > 0) {
            saveBlogrollButton.attr("disabled", false);
            console.log("Button enabled!");

        } else {
            saveBlogrollButton.attr("disabled", true);
            console.log("Button disabled!");
        }

    }


    function submitEditedBlogroll() {

        var folderEditForm = $('#folderEditForm')[0];

        var feedbackAreaBlogrollEdit = $("#feedback-area-blogroll-edit");

        // if name is empty reject and show error message
        if ($("#folderEditForm_bean_name").val().trim() === "") {
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

        }).done(function (data, status, response) {

            // kludge: scrape response status from HTML returned by Struts
            var alertEnd = data.indexOf("ALERT_END");
            var notUnique = data.indexOf('<s:text name="bookmarkForm.error.duplicateName" />');
            if (notUnique > 0 && notUnique < alertEnd) {
                feedbackAreaBlogrollEdit.css("color", "red");
                feedbackAreaBlogrollEdit.html('<s:text name="bookmarkForm.error.duplicateName" />');

            } else {
                feedbackAreaBlogrollEdit.css("color", "green");
                feedbackAreaBlogrollEdit.html('<s:text name="generic.success" />');
                $('#blogroll-edit-modal').modal("hide");

                // kludge get folderId from response header send back by Struts action
                var newFolderId = response.getResponseHeader('folderId');
                viewSelector.append(new Option('', newFolderId));
                $("#bookmarks_viewFolderId").val(newFolderId);

                var bookmarksForm = $("#bookmarks")[0];
                bookmarksForm.action = "bookmarks!view.rol";
                bookmarksForm.submit();
            }

        }).error(function (data) {
            feedbackAreaBlogrollEdit.html('<s:text name="generic.error.check.logs" />');
            feedbackAreaBlogrollEdit.css("color", "red");
        });
    }

</script>


<%-- ========================================================================================== --%>

<%-- delete blogroll confirmation modal --%>

<div id="delete-links-modal" class="modal fade delete-links-modal" tabindex="-1" role="dialog">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <div class="modal-header">
                <h3>
                    <s:text name="bookmarksForm.delete.confirm"/>
                </h3>
            </div>

            <s:form theme="bootstrap" cssClass="form-horizontal">
                <div class="modal-body">
                    <s:text name="bookmarksForm.delete.areYouSure" />
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn" value="%{getText('generic.yes')}" onclick="deleteSelected()">
                        <s:text name="generic.yes"/>
                    </button>
                    &nbsp;
                    <button type="button" class="btn btn-default btn-primary" data-dismiss="modal">
                        <s:text name="generic.no"/>
                    </button>
                </div>
            </s:form>

        </div>
    </div>
</div>


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

            <s:form id="boomarks_delete_folder" action="bookmarks!deleteFolder" theme="bootstrap"
                    cssClass="form-horizontal">
                <s:hidden name="salt"/>
                <s:hidden name="weblog"/>
                <s:hidden name="folderId"/>

                <div class="modal-body">
                    <s:text name="blogrollDeleteOK.areYouSure" />
                    <span id="deleteBlogrollName"></span>?
                </div>

                <div class="modal-footer">
                    <s:submit cssClass="btn" value="%{getText('generic.yes')}"/>&nbsp;
                    <button type="button" class="btn btn-default btn-primary" data-dismiss="modal">
                        <s:text name="generic.no"/>
                    </button>
                </div>

            </s:form>

        </div>
    </div>
</div>


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
                    <s:text name="%{#subtitleKey}"> </s:text> <span id="subtitle_folder_name"></span>
                </h3>

                <div id="bookmark_required_fields" role="alert" class="alert">
                    <s:text name="bookmarkForm.requiredFields"/>
                </div>

            </div>
            <%-- modal header --%>

            <div class="modal-body">

                <s:form action="bookmarkEdit" theme="bootstrap" cssClass="form-horizontal">
                    <s:hidden name="salt"/>
                    <s:hidden name="weblog"/>
                    <%--
                        Edit action uses folderId for redirection back to proper bookmarks folder on cancel
                        (as configured in struts.xml); add action also, plus to know which folder to put new
                        bookmark in.
                    --%>
                    <s:hidden name="folderId"/>
                    <s:hidden name="bean.id"/>

                    <s:textfield name="bean.name" maxlength="255"
                                 onchange="onBookmarkFormChanged()"
                                 onkeyup="onBookmarkFormChanged()"
                                 label="%{getText('generic.name')}"/>

                    <s:textfield name="bean.url" maxlength="255"
                                 onchange="onBookmarkFormChanged()"
                                 onkeyup="onBookmarkFormChanged()"
                                 label="%{getText('bookmarkForm.url')}"/>

                    <s:textfield name="bean.feedUrl" maxlength="255"
                                 onchange="onBookmarkFormChanged()"
                                 onkeyup="onBookmarkFormChanged()"
                                 label="%{getText('bookmarkForm.rssUrl')}"/>

                    <s:textfield name="bean.description" maxlength="255"
                                 onchange="onBookmarkFormChanged()"
                                 onkeyup="onBookmarkFormChanged()"
                                 label="%{getText('generic.description')}"/>

                    <s:textfield name="bean.image" maxlength="255"
                                 onchange="onBookmarkFormChanged()"
                                 onkeyup="onBookmarkFormChanged()"
                                 label="%{getText('bookmarkForm.image')}"/>
                </s:form>

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


        </div>
        <%-- modal content --%>

    </div>
    <%-- modal dialog --%>

</div> <%-- modal --%>


<%-- ---------------------------------------------------- --%>

<%-- JavaScript for add/edit link modal --%>

<script>

    function addBookmark() {

        var saveBookmarkButton = $('#save_bookmark:first');
        saveBookmarkButton.attr("disabled", true);

        var elem = $('#bookmark_required_fields:first');
        elem.html('<s:text name="bookmarkForm.requiredFields" />');
        elem.removeClass("alert-success");
        elem.removeClass("alert-danger");
        elem.addClass("alert-info");

        $('#bookmarkEdit_bean_name:first').val('');
        $('#bookmarkEdit_bean_url:first').val('');
        $('#bookmarkEdit_bean_image:first').val('');
        $('#bookmarkEdit_bean_feedUrl:first').val('');

        $('#subtitle_folder_name:first').html(originalName);

        $('#addedit-bookmark-modal').modal({show: true});
    }


    function editBookmark(id, name, url, feedUrl, description, image) {

        var saveBookmarkButton = $('#save_bookmark:first');
        saveBookmarkButton.attr("disabled", true);

        var elem = $('#bookmark_required_fields:first');
        elem.html('<s:text name="bookmarkForm.requiredFields" />');
        elem.removeClass("alert-success");
        elem.removeClass("alert-danger");
        elem.addClass("alert-info");

        $('#bookmarkEdit_bean_id:first').val(id);
        $('#bookmark_folderId').val(id);
        $('#bookmarkEdit_bean_name:first').val(name);
        $('#bookmarkEdit_bean_url:first').val(url);
        $('#bookmarkEdit_bean_feedUrl:first').val(feedUrl);
        $('#bookmarkEdit_bean_description:first').val(description);
        $('#bookmarkEdit_bean_image:first').val(image);

        $('#subtitle_folder_name:first').html(originalName);

        $('#addedit-bookmark-modal').modal({show: true});
    }


    function onBookmarkFormChanged() {

        var saveBookmarkButton = $('#save_bookmark:first');

        var name = $('#bookmarkEdit_bean_name:first').val().trim();
        var url = $('#bookmarkEdit_bean_url:first').val().trim();
        var image = $('#bookmarkEdit_bean_image:first').val().trim();
        var feedUrl = $('#bookmarkEdit_bean_feedUrl:first').val().trim();

        var badUrls = [];

        if (url.length > 0) {
            if (!isValidUrl(url)) {
                badUrls.push("Bookmark URL")
            }
        }

        if (image.length > 0) {
            if (!isValidUrl(image)) {
                badUrls.push("Image URL")
            }
        }

        if (feedUrl.length > 0) {
            if (!isValidUrl(feedUrl)) {
                badUrls.push("Newsfeed URL")
            }
        }

        var elem = $('#bookmark_required_fields:first');
        var message = '';

        if (name.length > 0 && url.length > 0 && badUrls.length === 0) {
            saveBookmarkButton.attr("disabled", false);

            message = '<s:text name="generic.looksGood" />';
            elem.removeClass("alert-info");
            elem.removeClass("alert-danger");
            elem.addClass("alert-success");
            elem.html(message);

        } else {
            saveBookmarkButton.attr("disabled", true);

            if (name.length === 0 || url.length === 0) {
                message = '<s:text name="bookmarkForm.required" />';
            }
            if (badUrls.length > 0) {
                message = '<s:text name="bookmarkForm.badUrls" />';
                var sep = " ";
                for (i in badUrls) {
                    message = message + sep + badUrls[i];
                    sep = ", ";
                }
            }
            elem.removeClass("alert-info");
            elem.removeClass("alert-success");
            elem.addClass("alert-danger");
            elem.html(message);
        }
    }


    function saveBookmark() {

        var feedbackAreaEdit = $("#feedback-area-edit");

        // post bookmark via AJAX
        $.ajax({
            method: 'post',
            url: "bookmarkEdit!save.rol",
            data: $("#bookmarkEdit").serialize(),
            context: document.body

        }).done(function (data) {

            // kludge: scrape response status from HTML returned by Struts
            var alertEnd = data.indexOf("ALERT_END");
            var notUnique = data.indexOf('<s:text name="bookmarkForm.error.duplicateName" />');
            if (notUnique > 0 && notUnique < alertEnd) {
                feedbackAreaEdit.css("color", "red");
                feedbackAreaEdit.html('<s:text name="bookmarkForm.error.duplicateName" />');

            } else {
                feedbackAreaEdit.css("color", "green");
                feedbackAreaEdit.html('<s:text name="generic.success" />');
                $('#addedit-bookmark-modal').modal("hide");

                // cause page to be reloaded so that edit appears
                // and make sure we end up on the right folder
                bookmarksForm = $("#bookmarks")[0];
                bookmarksForm.viewFolderId.value = bookmarksForm.folderId.value;
                viewChanged();
            }

        }).error(function (data) {
            feedbackAreaEdit.html('<s:text name="generic.error.check.logs" />');
            feedbackAreaEdit.css("color", "red");
        });
    }

</script>
