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
 * Categories page (Categories.jsp): add, edit and delete dialogs that post with
 * AJAX, and drag (or arrow-key) reordering. After a change the page reloads and
 * shows a short confirmation, carried across the reload in sessionStorage.
 */
(function ($) {
    "use strict";

    var page = document.getElementById("categoriesPage");
    if (!page) {
        return;
    }

    var FLASH_KEY = "roller.categories.flash";
    var list = document.getElementById("categoryList");
    var editModal = new bootstrap.Modal(document.getElementById("category-edit-modal"));
    var deleteModal = new bootstrap.Modal(document.getElementById("delete-category-modal"));
    var editForm = document.getElementById("categoryEditForm");
    var removeForm = document.getElementById("categoryRemoveForm");
    var nameField = document.getElementById("categoryEditForm_bean_name");
    var descField = document.getElementById("categoryEditForm_bean_description");
    var imageField = document.getElementById("categoryEditForm_bean_image");
    var imagePreview = document.getElementById("categoryImagePreview");
    var saveButton = document.getElementById("categorySaveButton");
    var deleteButton = document.getElementById("categoryDeleteButton");
    var targetSelect = document.getElementById("categoryRemove_targetCategoryId");

    function text(name) {
        return page.dataset[name] || "";
    }

    // messages come from resource bundles, which write a parameter as {0}
    function format(message, value) {
        // a function replacement, so a "$&" in the name is not expanded
        return message.replace("{0}", function () {
            return value;
        });
    }

    function rowOf(element) {
        return element.closest(".category-row");
    }

    // --- confirmation after a change -------------------------------------

    function showToast(message, isError) {
        var toast = document.getElementById("categoryToast");
        toast.querySelector(".toast-body").textContent = message;
        toast.classList.toggle("text-bg-success", !isError);
        toast.classList.toggle("text-bg-danger", !!isError);
        bootstrap.Toast.getOrCreateInstance(toast).show();
    }

    function reloadWith(message, categoryName) {
        try {
            sessionStorage.setItem(FLASH_KEY, JSON.stringify({ message: message, name: categoryName }));
        } catch (e) {
            // storage unavailable: reload without the confirmation
        }
        location.reload();
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
        list.querySelectorAll(".category-row").forEach(function (row) {
            if (flash.name && row.dataset.categoryName === flash.name) {
                row.classList.add("category-row-changed");
                row.scrollIntoView({ block: "nearest" });
            }
        });
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

    // --- add and edit --------------------------------------------------------

    function setInvalid(field, feedbackId, message) {
        field.classList.toggle("is-invalid", !!message);
        document.getElementById(feedbackId).textContent = message || "";
    }

    function imageUrl() {
        var value = imageField.value.trim();
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

    function validate() {
        var nameOk = nameField.value.trim() !== "";
        var image = imageUrl();
        setInvalid(nameField, "categoryNameFeedback", nameOk ? "" : text("msgNameRequired"));
        setInvalid(imageField, "categoryImageFeedback", image === null ? text("msgBadUrl") : "");
        imagePreview.classList.toggle("d-none", !image);
        if (image && imagePreview.getAttribute("src") !== image) {
            imagePreview.setAttribute("src", image);
        }
        saveButton.disabled = !nameOk || image === null;
        return !saveButton.disabled;
    }

    function openEditor(row) {
        var adding = !row;
        document.getElementById("category-edit-title").textContent = adding ? text("titleAdd") : text("titleEdit");
        document.getElementById("categoryEditForm_bean_id").value = adding ? "" : row.dataset.categoryId;
        nameField.value = adding ? "" : row.dataset.categoryName;
        descField.value = adding ? "" : row.dataset.categoryDesc;
        imageField.value = adding ? "" : row.dataset.categoryImage;
        showErrors(document.getElementById("categoryEditErrors"), []);
        setInvalid(nameField, "categoryNameFeedback", "");
        setInvalid(imageField, "categoryImageFeedback", "");
        imagePreview.classList.add("d-none");
        imagePreview.removeAttribute("src");
        saveButton.disabled = adding;
        showChooser(false);
        if (!adding) {
            validate();
        }
        editModal.show();
    }

    function saveCategory() {
        if (!validate()) {
            return;
        }
        var adding = document.getElementById("categoryEditForm_bean_id").value === "";
        var name = nameField.value.trim();
        saveButton.disabled = true;
        post(editForm).done(function (html) {
            var errors = actionErrors(html);
            if (errors.length > 0) {
                showErrors(document.getElementById("categoryEditErrors"), errors);
                saveButton.disabled = false;
            } else {
                reloadWith(format(adding ? text("msgAdded") : text("msgSaved"), name), name);
            }
        }).fail(function () {
            showErrors(document.getElementById("categoryEditErrors"), [text("msgError")]);
            saveButton.disabled = false;
        });
    }

    nameField.addEventListener("input", validate);
    imageField.addEventListener("input", validate);
    imagePreview.addEventListener("error", function () {
        imagePreview.classList.add("d-none");
    });
    saveButton.addEventListener("click", saveCategory);
    // Enter in a field saves; the form has no submit button and is never posted directly
    editForm.addEventListener("submit", function (event) {
        event.preventDefault();
    });
    editForm.addEventListener("keydown", function (event) {
        if (event.key === "Enter" && event.target.matches("input")) {
            event.preventDefault();
            saveCategory();
        }
    });
    document.getElementById("addCategoryButton").addEventListener("click", function () {
        openEditor(null);
    });
    document.getElementById("category-edit-modal").addEventListener("shown.bs.modal", function () {
        nameField.focus();
    });

    // --- choosing an image from the media library ---------------------------
    // Only when uploads are on; otherwise the page has no chooser and these are null.

    var chooser = document.getElementById("categoryImageChooser");
    var chooserFrame = document.getElementById("categoryImageChooserFrame");
    var editTitle = document.getElementById("category-edit-title");
    var formTitle = "";

    // the dialog shows either the form or the chooser, which needs a wider dialog
    function showChooser(on) {
        if (!chooser) {
            return;
        }
        var showing = !chooser.classList.contains("d-none");
        if (on === showing) {
            return;
        }
        if (on) {
            formTitle = editTitle.textContent;
            editTitle.textContent = chooser.dataset.title;
            chooserFrame.setAttribute("src", chooser.dataset.src);
        } else {
            editTitle.textContent = formTitle || editTitle.textContent;
            chooserFrame.setAttribute("src", "about:blank");
        }
        chooser.classList.toggle("d-none", !on);
        editForm.classList.toggle("d-none", on);
        document.querySelector("#category-edit-modal .modal-dialog").classList.toggle("modal-lg", on);
        document.getElementById("categoryChooserBackButton").classList.toggle("d-none", !on);
        document.getElementById("categoryCancelButton").classList.toggle("d-none", on);
        saveButton.classList.toggle("d-none", on);
    }

    if (chooser) {
        document.getElementById("categoryImageChooseButton").addEventListener("click", function () {
            showChooser(true);
        });
        document.getElementById("categoryChooserBackButton").addEventListener("click", function () {
            showChooser(false);
            document.getElementById("categoryImageChooseButton").focus();
        });
        document.getElementById("category-edit-modal").addEventListener("hidden.bs.modal", function () {
            showChooser(false);
        });

        // called by the chooser in the frame (MediaFileImageChooser.jsp); it lists only images
        window.onSelectMediaFile = function (name, url, isImage) {
            if (isImage !== "true") {
                return;
            }
            imageField.value = url;
            showChooser(false);
            validate();
            imageField.focus();
        };
    }

    // --- delete ----------------------------------------------------------------

    function openDelete(row) {
        var id = row.dataset.categoryId;
        var inUse = row.dataset.inUse === "true";
        document.getElementById("category-delete-title").textContent = format(text("titleDelete"), row.dataset.categoryName);
        document.getElementById("categoryRemove_removeId").value = id;
        document.getElementById("categoryDeleteText").textContent = row.dataset.deleteText;
        showErrors(document.getElementById("categoryDeleteErrors"), []);

        // the other categories, in display order, after the "choose" option
        while (targetSelect.options.length > 1) {
            targetSelect.remove(1);
        }
        list.querySelectorAll(".category-row").forEach(function (other) {
            if (other.dataset.categoryId !== id) {
                targetSelect.add(new Option(other.dataset.categoryName, other.dataset.categoryId));
            }
        });
        targetSelect.value = "";

        // a category nothing uses needs no target; a disabled select is not posted
        document.getElementById("categoryMoveTarget").classList.toggle("d-none", !inUse);
        targetSelect.disabled = !inUse;
        deleteButton.disabled = inUse;
        deleteButton.dataset.categoryName = row.dataset.categoryName;
        deleteModal.show();
    }

    targetSelect.addEventListener("change", function () {
        deleteButton.disabled = targetSelect.value === "";
    });

    deleteButton.addEventListener("click", function () {
        var name = deleteButton.dataset.categoryName;
        deleteButton.disabled = true;
        post(removeForm).done(function (html) {
            var errors = actionErrors(html);
            if (errors.length > 0) {
                showErrors(document.getElementById("categoryDeleteErrors"), errors);
                deleteButton.disabled = false;
            } else {
                reloadWith(format(text("msgRemoved"), name), null);
            }
        }).fail(function () {
            showErrors(document.getElementById("categoryDeleteErrors"), [text("msgError")]);
            deleteButton.disabled = false;
        });
    });

    // --- row actions -----------------------------------------------------------

    list.addEventListener("click", function (event) {
        var edit = event.target.closest(".category-edit-link");
        var remove = event.target.closest(".category-delete-link");
        if (edit) {
            event.preventDefault();
            openEditor(rowOf(edit));
        } else if (remove) {
            openDelete(rowOf(remove));
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
        var ids = Array.prototype.map.call(list.querySelectorAll(".category-row"), function (row) {
            return "&categoryIds=" + encodeURIComponent(row.dataset.categoryId);
        }).join("");
        post(document.getElementById("categoriesReorderForm"), ids).done(function (html) {
            var errors = actionErrors(html);
            if (errors.length > 0) {
                showToast(errors.join(" "), true);
            } else {
                document.getElementById("categoryStatus").textContent = text("msgOrderSaved");
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
            handle: ".category-handle",
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
        var handle = event.target.closest(".category-handle");
        if (!handle || (event.key !== "ArrowUp" && event.key !== "ArrowDown")) {
            return;
        }
        event.preventDefault();
        if (moveRow(rowOf(handle), event.key === "ArrowUp")) {
            handle.focus();
        }
    });

    list.addEventListener("click", function (event) {
        var button = event.target.closest(".category-move-up, .category-move-down");
        if (button) {
            moveRow(rowOf(button), button.classList.contains("category-move-up"));
            button.focus();
        }
    });

    showFlash();
}(jQuery));
