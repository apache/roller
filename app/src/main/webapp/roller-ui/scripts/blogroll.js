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
 * Blogroll page (Bookmarks.jsp): add, edit and delete dialogs for links and
 * blogrolls that post with AJAX, moving selected links to another blogroll,
 * and drag (or arrow-key) reordering. After a change the page reloads and
 * shows a short confirmation, carried across the reload in sessionStorage.
 */
(function ($) {
    "use strict";

    var page = document.getElementById("blogrollPage");
    if (!page) {
        return;
    }

    var FLASH_KEY = "roller.blogroll.flash";
    var list = document.getElementById("linkList");

    function text(name) {
        return page.dataset[name] || "";
    }

    // messages come from resource bundles, which write parameters as {0}, {1}
    function format(message) {
        var args = Array.prototype.slice.call(arguments, 1);
        return message.replace(/\{(\d)\}/g, function (match, i) {
            return args[i] !== undefined ? args[i] : match;
        });
    }

    function byId(id) {
        return document.getElementById(id);
    }

    // --- confirmation after a change -------------------------------------

    function showToast(message, isError) {
        var toast = byId("blogrollToast");
        toast.querySelector(".toast-body").textContent = message;
        toast.classList.toggle("text-bg-success", !isError);
        toast.classList.toggle("text-bg-danger", !!isError);
        bootstrap.Toast.getOrCreateInstance(toast).show();
    }

    // reloads the page, or opens another blogroll, then shows the message and
    // highlights the links named, by id or by name
    function reloadWith(message, highlight, folderId) {
        try {
            sessionStorage.setItem(FLASH_KEY, JSON.stringify({ message: message, highlight: highlight || [] }));
        } catch (e) {
            // storage unavailable: reload without the confirmation
        }
        location.href = text("folderUrl") + "&folderId=" + encodeURIComponent(folderId || text("folderId"));
    }

    function showFlash() {
        var flash = null;
        try {
            flash = JSON.parse(sessionStorage.getItem(FLASH_KEY));
            sessionStorage.removeItem(FLASH_KEY);
        } catch (e) {
            return;
        }
        if (!flash) {
            return;
        }
        showToast(flash.message);
        if (list) {
            list.querySelectorAll(".link-row").forEach(function (row) {
                if (flash.highlight.indexOf(row.dataset.linkId) >= 0
                        || flash.highlight.indexOf(row.dataset.linkName) >= 0) {
                    row.classList.add("link-row-changed");
                    row.scrollIntoView({ block: "nearest" });
                }
            });
        }
    }

    // --- posting -----------------------------------------------------------

    function post(form, extra) {
        var data = $(form).serialize() + (extra || "");
        return $.ajax({ method: "post", url: form.action, data: data }).done(function (html) {
            refreshSalt(html);
        }).fail(function (xhr) {
            // the salt just sent is spent whether or not the post worked
            if (xhr && xhr.responseText) {
                refreshSalt(xhr.responseText);
            }
        });
    }

    function showErrors(box, errors) {
        box.textContent = errors.join(" ");
        box.classList.toggle("d-none", errors.length === 0);
    }

    // posts a dialog's form; errors from the server go in the dialog
    function submit(form, extra, button, errorBox, onSuccess) {
        button.disabled = true;
        post(form, extra).done(function (html) {
            var errors = actionErrors(html);
            if (errors.length > 0) {
                showErrors(errorBox, errors);
                button.disabled = false;
            } else {
                onSuccess(html);
            }
        }).fail(function () {
            showErrors(errorBox, [text("msgError")]);
            button.disabled = false;
        });
    }

    function setInvalid(field, feedbackId, message) {
        field.classList.toggle("is-invalid", !!message);
        byId(feedbackId).textContent = message || "";
    }

    // "" for an empty field, null for one that isn't an http(s) URL
    function httpUrl(field) {
        var value = field.value.trim();
        if (!value) {
            return "";
        }
        try {
            var url = new URL(value);
            return url.protocol === "http:" || url.protocol === "https:" ? url.href : null;
        } catch (e) {
            return null;
        }
    }

    // --- add and edit a link ----------------------------------------------

    var linkModal = new bootstrap.Modal(byId("link-edit-modal"));
    var linkForm = byId("linkEditForm");
    var linkUrl = byId("linkEditForm_bean_url");
    var linkName = byId("linkEditForm_bean_name");
    var linkDesc = byId("linkEditForm_bean_description");
    var linkFeed = byId("linkEditForm_bean_feedUrl");
    var linkImage = byId("linkEditForm_bean_image");
    var linkPreview = byId("linkImagePreview");
    var linkSave = byId("linkSaveButton");
    var linkErrors = byId("linkEditErrors");
    var nameTouched = false;
    var touched = {};

    // shows a field's error only once the user has typed in it, or on save
    function validateLink(all) {
        var url = httpUrl(linkUrl);
        var feed = httpUrl(linkFeed);
        var image = httpUrl(linkImage);
        var nameOk = linkName.value.trim() !== "";
        var show = function (field) {
            return all || touched[field.id];
        };
        setInvalid(linkUrl, "linkUrlFeedback", !show(linkUrl) ? "" :
            url === "" ? text("msgUrlRequired") : url === null ? text("msgBadUrl") : "");
        setInvalid(linkName, "linkNameFeedback", show(linkName) && !nameOk ? text("msgNameRequired") : "");
        setInvalid(linkFeed, "linkFeedFeedback", feed === null ? text("msgBadUrl") : "");
        setInvalid(linkImage, "linkImageFeedback", image === null ? text("msgBadUrl") : "");
        linkPreview.classList.toggle("d-none", !image);
        if (image && linkPreview.getAttribute("src") !== image) {
            linkPreview.setAttribute("src", image);
        }
        var ok = nameOk && url !== "" && url !== null && feed !== null && image !== null;
        linkSave.disabled = !ok;
        return ok;
    }

    // a new link's name starts as the site's host name, until the user types one
    function suggestName() {
        if (nameTouched || linkForm.dataset.adding !== "true") {
            return;
        }
        var url = httpUrl(linkUrl);
        linkName.value = url ? new URL(url).hostname.replace(/^www\./, "") : "";
    }

    function openLinkEditor(row) {
        var adding = !row;
        var data = adding ? {} : row.dataset;
        byId("link-edit-title").textContent = format(adding ? text("titleAddLink") : text("titleEditLink"), text("folderName"));
        linkForm.dataset.adding = adding ? "true" : "false";
        linkForm.action = linkForm.action.replace(/bookmark(Add|Edit)!save/, adding ? "bookmarkAdd!save" : "bookmarkEdit!save");
        byId("linkEditForm_bean_id").value = adding ? "" : data.linkId;
        linkUrl.value = data.linkUrl || "";
        linkName.value = data.linkName || "";
        linkDesc.value = data.linkDesc || "";
        linkFeed.value = data.linkFeed || "";
        linkImage.value = data.linkImage || "";
        byId("linkAdvanced").open = !!(data.linkFeed || data.linkImage);
        nameTouched = !adding;
        touched = {};
        showErrors(linkErrors, []);
        linkPreview.classList.add("d-none");
        linkPreview.removeAttribute("src");
        validateLink(false);
        linkModal.show();
    }

    function saveLink() {
        if (!validateLink(true)) {
            return;
        }
        var adding = linkForm.dataset.adding === "true";
        var name = linkName.value.trim();
        submit(linkForm, "", linkSave, linkErrors, function () {
            reloadWith(format(adding ? text("msgLinkAdded") : text("msgLinkSaved"), name), [name]);
        });
    }

    [linkUrl, linkName, linkFeed, linkImage].forEach(function (field) {
        field.addEventListener("input", function () {
            touched[field.id] = true;
            if (field === linkName) {
                nameTouched = true;
            }
            if (field === linkUrl) {
                suggestName();
            }
            validateLink(false);
        });
    });
    linkPreview.addEventListener("error", function () {
        linkPreview.classList.add("d-none");
    });
    linkSave.addEventListener("click", saveLink);
    linkForm.addEventListener("submit", function (event) {
        event.preventDefault();
    });
    linkForm.addEventListener("keydown", function (event) {
        if (event.key === "Enter" && event.target.matches("input")) {
            event.preventDefault();
            saveLink();
        }
    });
    byId("link-edit-modal").addEventListener("shown.bs.modal", function () {
        (linkForm.dataset.adding === "true" ? linkUrl : linkName).focus();
    });
    ["addLinkButton", "emptyAddLinkButton"].forEach(function (id) {
        var button = byId(id);
        if (button) {
            button.addEventListener("click", function () {
                openLinkEditor(null);
            });
        }
    });

    // --- add and rename a blogroll ------------------------------------------

    var folderModal = new bootstrap.Modal(byId("folder-edit-modal"));
    var folderForm = byId("folderEditForm");
    var folderName = byId("folderEditForm_bean_name");
    var folderSave = byId("folderSaveButton");
    var folderErrors = byId("folderEditErrors");

    function validateFolder(show) {
        var ok = folderName.value.trim() !== "";
        setInvalid(folderName, "folderNameFeedback", show && !ok ? text("msgFolderNameRequired") : "");
        folderSave.disabled = !ok;
        return ok;
    }

    function openFolderEditor(renaming) {
        byId("folder-edit-title").textContent = renaming ? text("titleRenameFolder") : text("titleAddFolder");
        folderForm.dataset.adding = renaming ? "false" : "true";
        folderForm.action = folderForm.action.replace(/folder(Add|Edit)!save/, renaming ? "folderEdit!save" : "folderAdd!save");
        byId("folderEditForm_bean_id").value = renaming ? text("folderId") : "";
        folderName.value = renaming ? text("folderName") : "";
        showErrors(folderErrors, []);
        validateFolder(false);
        folderModal.show();
    }

    function saveFolder() {
        if (!validateFolder(true)) {
            return;
        }
        var adding = folderForm.dataset.adding === "true";
        var name = folderName.value.trim();
        submit(folderForm, "", folderSave, folderErrors, function (html) {
            // a new blogroll is opened: its id is in the page the save returns
            var created = new DOMParser().parseFromString(html, "text/html").getElementById("blogrollPage");
            var folderId = adding && created ? created.dataset.folderId : null;
            reloadWith(format(adding ? text("msgFolderAdded") : text("msgFolderRenamed"), name), [], folderId);
        });
    }

    folderName.addEventListener("input", function () {
        validateFolder(true);
    });
    folderSave.addEventListener("click", saveFolder);
    folderForm.addEventListener("submit", function (event) {
        event.preventDefault();
        saveFolder();
    });
    byId("folder-edit-modal").addEventListener("shown.bs.modal", function () {
        folderName.focus();
        folderName.select();
    });
    byId("addFolderButton").addEventListener("click", function () {
        openFolderEditor(false);
    });
    if (byId("renameFolderButton")) {
        byId("renameFolderButton").addEventListener("click", function () {
            openFolderEditor(true);
        });
    }

    // --- delete a blogroll ---------------------------------------------------

    if (byId("delete-folder-modal")) {
        var folderDeleteModal = new bootstrap.Modal(byId("delete-folder-modal"));
        byId("deleteFolderButton").addEventListener("click", function () {
            showErrors(byId("deleteFolderErrors"), []);
            folderDeleteModal.show();
        });
        byId("deleteFolderConfirm").addEventListener("click", function () {
            var name = text("folderName");
            submit(byId("folderDeleteForm"), "", this, byId("deleteFolderErrors"), function () {
                // the default blogroll opens: no folder id
                reloadWith(format(text("msgFolderDeleted"), name), [], "");
            });
        });
    }

    // the rest works on the links, when there are any
    if (!list) {
        showFlash();
        return;
    }

    // --- selecting, moving and deleting links -----------------------------------

    var selectAll = byId("selectAllLinks");
    var selectedCount = byId("selectedCount");
    var selectAllLabel = selectedCount.textContent;
    var moveSelect = byId("linksMoveForm_targetFolderId");
    var moveButton = byId("moveLinksButton");
    var deleteButton = byId("deleteLinksButton");
    var deleteModal = new bootstrap.Modal(byId("delete-links-modal"));
    var deleteConfirm = byId("deleteLinksConfirm");
    var toDelete = [];

    function boxes() {
        return Array.prototype.slice.call(list.querySelectorAll(".link-select"));
    }

    function selectedIds() {
        return boxes().filter(function (box) {
            return box.checked;
        }).map(function (box) {
            return box.value;
        });
    }

    function idsParam(ids) {
        return ids.map(function (id) {
            return "&selectedBookmarks=" + encodeURIComponent(id);
        }).join("");
    }

    function namesOf(ids) {
        return boxes().filter(function (box) {
            return ids.indexOf(box.value) >= 0;
        }).map(function (box) {
            return box.closest(".link-row").dataset.linkName;
        });
    }

    function selectionChanged() {
        var ids = selectedIds();
        var all = boxes().length;
        selectAll.checked = ids.length === all;
        selectAll.indeterminate = ids.length > 0 && ids.length < all;
        selectedCount.textContent = ids.length ? format(text("msgSelected"), ids.length) : selectAllLabel;
        deleteButton.disabled = ids.length === 0;
        if (moveSelect) {
            moveSelect.disabled = ids.length === 0;
            moveButton.disabled = ids.length === 0;
        }
        boxes().forEach(function (box) {
            box.closest(".link-row").classList.toggle("link-row-selected", box.checked);
        });
    }

    selectAll.addEventListener("change", function () {
        boxes().forEach(function (box) {
            box.checked = selectAll.checked;
        });
        selectionChanged();
    });
    list.addEventListener("change", function (event) {
        if (event.target.matches(".link-select")) {
            selectionChanged();
        }
    });

    function openDelete(ids) {
        toDelete = ids;
        var names = namesOf(ids);
        byId("delete-links-title").textContent = ids.length === 1
            ? format(text("titleDeleteOne"), names[0])
            : format(text("titleDeleteMany"), ids.length);
        showErrors(byId("deleteLinksErrors"), []);
        deleteConfirm.disabled = false;
        deleteModal.show();
    }

    deleteButton.addEventListener("click", function () {
        openDelete(selectedIds());
    });
    deleteConfirm.addEventListener("click", function () {
        submit(byId("linksDeleteForm"), idsParam(toDelete), deleteConfirm, byId("deleteLinksErrors"), function () {
            reloadWith(format(toDelete.length === 1 ? text("msgDeletedOne") : text("msgDeletedMany"), toDelete.length));
        });
    });

    if (moveButton) {
        moveButton.addEventListener("click", function () {
            var ids = selectedIds();
            var target = moveSelect.options[moveSelect.selectedIndex];
            moveButton.disabled = true;
            post(byId("linksMoveForm"), idsParam(ids)).done(function (html) {
                var errors = actionErrors(html);
                if (errors.length > 0) {
                    showToast(errors.join(" "), true);
                    moveButton.disabled = false;
                } else {
                    // open the target blogroll, with the moved links highlighted
                    reloadWith(format(ids.length === 1 ? text("msgMovedOne") : text("msgMovedMany"), ids.length, target.text),
                            ids, target.value);
                }
            }).fail(function () {
                showToast(text("msgError"), true);
                moveButton.disabled = false;
            });
        });
    }

    // --- row actions -----------------------------------------------------------

    list.addEventListener("click", function (event) {
        var edit = event.target.closest(".link-edit");
        var remove = event.target.closest(".link-delete");
        if (edit) {
            event.preventDefault();
            openLinkEditor(edit.closest(".link-row"));
        } else if (remove) {
            openDelete([remove.closest(".link-row").dataset.linkId]);
        }
    });

    // --- reordering ------------------------------------------------------------

    var saveTimer = null;
    var saving = false;
    var saveAgain = false;

    // Each post spends the page's salt and returns a new one, so only one save
    // can be in flight. A change made meanwhile is sent afterwards, from the
    // list as it is then.
    function saveOrder() {
        if (saving) {
            saveAgain = true;
            return;
        }
        saving = true;
        var ids = Array.prototype.map.call(list.querySelectorAll(".link-row"), function (row) {
            return "&bookmarkIds=" + encodeURIComponent(row.dataset.linkId);
        }).join("");
        post(byId("linksReorderForm"), ids).done(function (html) {
            var errors = actionErrors(html);
            if (errors.length > 0) {
                showToast(errors.join(" "), true);
            } else {
                byId("blogrollStatus").textContent = text("msgOrderSaved");
                showToast(text("msgOrderSaved"));
            }
        }).fail(function () {
            showToast(text("msgError"), true);
        }).always(function () {
            saving = false;
            if (saveAgain) {
                saveAgain = false;
                saveOrder();
            }
        });
    }

    // keyboard moves can come quickly, one after another; save once they stop
    function saveOrderSoon() {
        clearTimeout(saveTimer);
        saveTimer = setTimeout(saveOrder, 600);
    }

    if ($.fn.sortable && list.children.length > 1) {
        $(list).sortable({
            handle: ".link-handle",
            // the default also cancels drags that start on a button, and the handle is one
            cancel: "input, textarea, select, option",
            axis: "y",
            containment: "parent",
            tolerance: "pointer",
            update: saveOrder
        });
    }

    // one step up or down; the arrow keys on the handle and the buttons shown
    // on touch screens (where the handle cannot be dragged) both use it
    function moveRow(row, up) {
        var sibling = up ? row.previousElementSibling : row.nextElementSibling;
        if (!sibling) {
            return false;
        }
        if (up) {
            list.insertBefore(row, sibling);
        } else {
            list.insertBefore(sibling, row);
        }
        saveOrderSoon();
        return true;
    }

    list.addEventListener("keydown", function (event) {
        var handle = event.target.closest(".link-handle");
        if (!handle || (event.key !== "ArrowUp" && event.key !== "ArrowDown")) {
            return;
        }
        event.preventDefault();
        if (moveRow(handle.closest(".link-row"), event.key === "ArrowUp")) {
            handle.focus();
        }
    });

    list.addEventListener("click", function (event) {
        var button = event.target.closest(".link-move-up, .link-move-down");
        if (button) {
            moveRow(button.closest(".link-row"), button.classList.contains("link-move-up"));
            button.focus();
        }
    });

    selectionChanged();
    showFlash();
}(jQuery));
