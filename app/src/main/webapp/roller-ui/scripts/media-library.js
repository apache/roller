/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/

/*
 * Media library page (MediaFileView.jsp, MediaFileSidebar.jsp, MediaFileCard.jsp).
 * Localized strings and action URLs arrive in data-* attributes; file names are
 * only ever written with text APIs.
 */
$(function () {
    var library = $("#mediaLibrary");
    if (!library.length) {
        return;
    }
    var urls = $("#mediaFileUrls");
    var form = document.mediaFileViewForm;

    function format(message, value) {
        return message.replace("{0}", value);
    }

    // ----- selection ---------------------------------------------------------

    function selected() {
        return $(".media-select:checked");
    }

    function updateSelection() {
        var count = selected().length;
        $(".media-select").each(function () {
            $(this).closest(".media-card").toggleClass("selected", this.checked);
        });
        $("#selectedCount").text(count > 0 ? format(library.data("selected-count"), count) : "");
        $("#moveButton, #deleteButton, #moveTargetMenu, #clearSelectionButton").prop("disabled", count === 0);
    }

    $(document).on("change", ".media-select", updateSelection);

    $("#selectPageButton").on("click", function () {
        $(".media-select").prop("checked", true);
        updateSelection();
    });

    $("#clearSelectionButton").on("click", function () {
        $(".media-select").prop("checked", false);
        updateSelection();
    });

    updateSelection();

    // ----- actions on the selection, the folder and the view -----------------

    function submitTo(url) {
        form.action = url;
        form.submit();
    }

    $("#deleteButton").on("click", function () {
        if (confirm(format(library.data("confirm-delete"), selected().length))) {
            submitTo(urls.data("delete-selected-url"));
        }
    });

    $("#moveButton").on("click", function () {
        if (confirm(library.data("confirm-move"))) {
            submitTo(urls.data("move-selected-url"));
        }
    });

    $("#deleteFolderButton").on("click", function () {
        if (confirm(library.data("confirm-delete-folder"))) {
            submitTo(urls.data("delete-folder-url"));
        }
    });

    $("#sortByMenu").on("change", function () {
        form.submit();
    });

    // ----- folders -------------------------------------------------------------

    // narrow screens pick the folder from a list in the header
    $("#folderPicker").on("change", function () {
        window.location.href = this.value;
    });

    // ----- new folder (sidebar) ----------------------------------------------

    var newFolderName = $("#newDirectoryName");
    var newFolderButton = $("#newDirectoryButton");

    newFolderName.on("input", function () {
        newFolderButton.prop("disabled", $.trim(newFolderName.val()).length === 0);
    }).on("keydown", function (e) {
        if (e.key === "Enter" && !newFolderButton.prop("disabled")) {
            e.preventDefault();
            newFolderButton.click();
        }
    });

    newFolderButton.on("click", function () {
        form.newDirectoryName.value = $.trim(newFolderName.val());
        submitTo(urls.data("create-folder-url"));
    });

    // ----- search result pages -----------------------------------------------

    $(document).on("click", ".media-search-page", function () {
        $("#beanPageNum").val($(this).data("page"));
        $("#mediaFileSearchForm").submit();
    });

    // ----- previews ----------------------------------------------------------

    // A thumbnail that fails to load shows the file type icon instead.
    document.addEventListener("error", function (e) {
        var img = e.target;
        if (img.tagName === "IMG" && img.closest(".media-card-preview")) {
            var icon = document.createElement("span");
            icon.className = "bi " + (img.getAttribute("data-fallback-icon") || "bi-file-earmark");
            icon.setAttribute("aria-hidden", "true");
            img.replaceWith(icon);
        }
    }, true);

    // ----- details dialog ----------------------------------------------------

    var editor = $("#mediaFileEditor");
    var dialog = $("#mediafile_edit_lightbox");
    var opener = null;

    $(document).on("click", ".mediafile-edit-target", function () {
        opener = this;
        $("#edit-subtitle").text($(this).attr("data-mediafile-name"));
        editor.attr("src", urls.data("edit-url") + "&mediaFileId="
            + encodeURIComponent($(this).attr("data-mediafile-id")));
        dialog.modal("show");
    });

    dialog.on("hidden.bs.modal", function () {
        editor.attr("src", "about:blank");
        if (opener) {
            opener.focus();
        }
    });

    // called by MediaFileEdit.jsp and MediaFileEditSuccess.jsp inside the dialog
    window.onEditSuccess = function () {
        dialog.modal("hide");
        form.submit();
    };
    window.onEditCancelled = function () {
        dialog.modal("hide");
    };
});
