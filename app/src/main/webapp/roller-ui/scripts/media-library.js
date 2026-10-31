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
 * Media library (MediaFileView.jsp, MediaFileSidebar.jsp, MediaFileCard.jsp) and
 * media file details (MediaFileEdit.jsp). Localized strings and action URLs arrive
 * in data-* attributes; file names are only ever written with text APIs.
 */
$(function () {

    function format(message, value) {
        return message.replace("{0}", value);
    }

    // A preview that fails to load shows the file type icon instead.
    document.addEventListener("error", function (e) {
        var img = e.target;
        if (img.tagName === "IMG" && img.closest(".media-card-preview, .media-details-preview")) {
            var icon = document.createElement("span");
            icon.className = "bi " + (img.getAttribute("data-fallback-icon") || "bi-file-earmark");
            icon.setAttribute("aria-hidden", "true");
            img.replaceWith(icon);
        }
    }, true);

    var library = $("#mediaLibrary");
    if (library.length) {
        initLibrary(library);
    }
    var details = $("#mediaDetails");
    if (details.length) {
        initDetails(details);
    }

    function initLibrary(library) {
        var urls = $("#mediaFileUrls");
        var form = document.mediaFileViewForm;

        // ----- selection ---------------------------------------------------

        function selected() {
            return $(".media-select:checked");
        }

        // while files are selected, the selection bar replaces the type, sort and layout bar
        function updateSelection() {
            var count = selected().length;
            $(".media-select").each(function () {
                $(this).closest(".media-card").toggleClass("selected", this.checked);
            });
            library.toggleClass("selecting", count > 0);
            $(".media-results-bar").prop("hidden", count > 0);
            $("#selectionBar").prop("hidden", count === 0);
            $("#selectedCount").text(count > 0 ? format(library.data("selected-count"), count) : "");
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

        // ----- actions on the selection and the folder -----------------------

        function submitTo(url) {
            form.action = url;
            form.submit();
        }

        $("#deleteButton").on("click", function () {
            if (confirm(format(library.data("confirm-delete"), selected().length))) {
                submitTo(urls.data("delete-selected-url"));
            }
        });

        // moving can be undone, so it needs no confirmation
        $("#moveTargetMenu").on("click", "[data-folder-id]", function () {
            form.selectedDirectory.value = $(this).data("folder-id");
            submitTo(urls.data("move-selected-url"));
        });

        $("#deleteFolderButton").on("click", function () {
            if (confirm(library.data("confirm-delete-folder"))) {
                submitTo(urls.data("delete-folder-url"));
            }
        });

        // ----- search, sort and folders --------------------------------------

        var searchForm = $("#mediaFileSearchForm");

        // leave empty and default criteria out of the URL; the type chips and the
        // sort menu sit outside the form and join it through their form attribute
        searchForm.on("submit", function () {
            $(this.elements).filter("[name]").each(function () {
                var empty = this.value === "" || this.value === "mediaFileView.any"
                    || (this.name === "bean.scope" && this.value === "folder")
                    || (this.name === "bean.sortBy" && this.value === "newest");
                var sizeUnused = /^bean\.size/.test(this.name) && !Number($("#beanSize").val());
                if (empty || sizeUnused) {
                    this.disabled = true;
                }
            });
        });

        // fields disabled above stay disabled when the browser restores this page
        $(window).on("pageshow", function () {
            $(searchForm[0].elements).prop("disabled", false);
        });

        $("#sortByMenu, [name='bean.type']").on("change", function () {
            searchForm.trigger("submit");
        });

        // narrow screens pick the folder from a list in the header
        $("#folderPicker").on("change", function () {
            window.location.href = this.value;
        });

        // ----- grid or list, remembered in this browser ----------------------

        var grid = $("#mediaGrid");

        function showView(view) {
            grid.toggleClass("media-list", view === "list");
            $("[data-media-view]").each(function () {
                var on = $(this).data("media-view") === view;
                $(this).attr("aria-pressed", String(on)).toggleClass("active", on);
            });
        }

        var savedView = "grid";
        try {
            savedView = localStorage.getItem("roller.mediaView") || "grid";
        } catch (e) {
            // storage unavailable; use the grid
        }
        showView(savedView);

        $("[data-media-view]").on("click", function () {
            var view = $(this).data("media-view");
            showView(view);
            try {
                localStorage.setItem("roller.mediaView", view);
            } catch (e) {
                // storage unavailable; the choice lasts for this page only
            }
        });

        // ----- new folder (sidebar) ------------------------------------------

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
    }

    function initDetails(details) {

        // ----- copy URL ------------------------------------------------------

        var urlField = $("#mediaFileURL");
        var status = $("#copyStatus");

        $("#copyURLButton").on("click", function () {
            var url = urlField.val();
            function copied() {
                status.text(details.data("copied"));
            }
            function failed() {
                urlField.trigger("focus").trigger("select");
                status.text(details.data("copy-failed"));
            }
            if (navigator.clipboard && window.isSecureContext) {
                navigator.clipboard.writeText(url).then(copied, failed);
            } else {
                failed();
            }
        });

        // ----- delete ----------------------------------------------------------

        $("#deleteMediaFileForm").on("submit", function (e) {
            if (!confirm(details.data("confirm-delete"))) {
                e.preventDefault();
            }
        });

        // ----- warn before leaving with unsaved edits --------------------------

        var editForm = $("#mediaFileEditForm");
        var saved = editForm.serialize();
        var submitting = false;

        editForm.on("submit", function () {
            submitting = true;
        });

        $(window).on("beforeunload", function (e) {
            if (!submitting && editForm.serialize() !== saved) {
                e.preventDefault();
                e.originalEvent.returnValue = details.data("unsaved");
                return details.data("unsaved");
            }
        });
    }
});
