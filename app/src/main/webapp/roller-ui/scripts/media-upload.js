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
 * Upload page (MediaFileAdd.jsp). Chosen or dropped files go into a list; Upload
 * sends the files that are not yet uploaded in one request with format=json, and
 * MediaFileAddResult.jsp answers with a result per file and a fresh salt. Failed
 * files stay in the list for a retry; uploaded files are never sent again. If the
 * connection drops after sending, the outcome is unknown and nothing is retried
 * automatically. Localized strings come from data-* attributes on #mediaUpload;
 * file names are only written with text APIs.
 */
(function () {
    var config = document.getElementById("mediaUpload");
    var form = document.getElementById("uploadForm");
    if (!config || !form || !window.fetch || !window.FormData) {
        return; // the form posts normally
    }

    var input = document.getElementById("fileInput");
    var dropZone = document.getElementById("dropZone");
    var list = document.getElementById("fileList");
    var button = document.getElementById("uploadButton");
    var buttonLabel = document.getElementById("uploadButtonLabel");
    var status = document.getElementById("uploadStatus");
    var continueLink = document.getElementById("continueLink");
    var folder = document.getElementById("uploadFolder");
    var uploadLabel = buttonLabel.textContent;

    var maxFiles = parseInt(config.getAttribute("data-max-files"), 10) || 20;
    var maxFileMB = parseFloat(config.getAttribute("data-max-file-mb")) || 0;

    // entries: {file, state: queued|uploading|uploaded|failed|unknown, error, item}
    var entries = [];
    var uploadedIds = [];
    var busy = false;

    function text(name) {
        return config.getAttribute("data-" + name) || "";
    }

    function format(message) {
        var args = Array.prototype.slice.call(arguments, 1);
        return message.replace(/\{(\d)\}/g, function (match, i) {
            return args[i] !== undefined ? args[i] : match;
        });
    }

    function formatSize(bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        }
        var units = ["KB", "MB", "GB"];
        var value = bytes;
        var unit = -1;
        do {
            value /= 1024;
            unit++;
        } while (value >= 1024 && unit < units.length - 1);
        return (value < 10 ? Math.round(value * 10) / 10 : Math.round(value)) + " " + units[unit];
    }

    function tooLarge(file) {
        return maxFileMB > 0 && file.size > maxFileMB * 1024 * 1024;
    }

    function pending() {
        return entries.filter(function (e) {
            return e.state === "queued" || e.state === "failed";
        });
    }

    // ----- the file list ----------------------------------------------------

    function addFiles(files) {
        Array.prototype.forEach.call(files, function (file) {
            var duplicate = entries.some(function (e) {
                return e.state !== "uploaded" && e.file.name === file.name
                    && e.file.size === file.size && e.file.lastModified === file.lastModified;
            });
            if (!duplicate) {
                var entry = {file: file, state: "queued", error: null};
                if (tooLarge(file)) {
                    entry.state = "failed";
                    entry.error = format(text("too-large"), maxFileMB);
                }
                entries.push(entry);
                entry.item = renderEntry(entry);
                list.appendChild(entry.item);
            }
        });
        input.value = "";
        update();
    }

    function renderEntry(entry) {
        var item = document.createElement("li");
        item.className = "media-upload-item";

        var preview = document.createElement("span");
        preview.className = "media-upload-preview";
        if (/^image\//.test(entry.file.type) && entry.file.size < 20 * 1024 * 1024) {
            var img = document.createElement("img");
            img.alt = "";
            img.src = URL.createObjectURL(entry.file);
            img.onload = function () {
                URL.revokeObjectURL(img.src);
            };
            preview.appendChild(img);
        } else {
            var icon = document.createElement("span");
            icon.className = "bi bi-file-earmark";
            icon.setAttribute("aria-hidden", "true");
            preview.appendChild(icon);
        }

        var name = document.createElement("span");
        name.className = "media-upload-name";
        name.textContent = entry.file.name;

        var size = document.createElement("span");
        size.className = "media-upload-size";
        size.textContent = formatSize(entry.file.size);

        var state = document.createElement("span");
        state.className = "media-upload-state";

        var remove = document.createElement("button");
        remove.type = "button";
        remove.className = "btn btn-sm btn-outline-secondary media-upload-remove";
        remove.setAttribute("aria-label", format(text("remove"), entry.file.name));
        remove.innerHTML = '<span class="bi bi-x-lg" aria-hidden="true"></span>';
        remove.addEventListener("click", function () {
            entries.splice(entries.indexOf(entry), 1);
            item.remove();
            update();
            input.focus();
        });

        item.appendChild(preview);
        item.appendChild(name);
        item.appendChild(size);
        item.appendChild(state);
        item.appendChild(remove);
        return item;
    }

    function update() {
        entries.forEach(function (entry) {
            var state = entry.item.querySelector(".media-upload-state");
            var label = text("status-" + entry.state);
            state.textContent = entry.error ? label + ": " + entry.error : label;
            entry.item.setAttribute("data-state", entry.state);
            entry.item.querySelector(".media-upload-remove").hidden =
                busy || entry.state === "uploaded" || entry.state === "uploading";
        });

        var sendable = pending().filter(function (e) {
            return !tooLarge(e.file);
        });
        var anyFailed = entries.some(function (e) {
            return e.state === "failed";
        });
        var anyUploaded = uploadedIds.length > 0;

        button.disabled = busy || sendable.length === 0 || sendable.length > maxFiles;
        buttonLabel.textContent = anyUploaded && anyFailed ? text("retry") : uploadLabel;

        continueLink.hidden = busy || !anyUploaded;
        continueLink.href = config.getAttribute("data-done-url")
            + "&uploadedIds=" + encodeURIComponent(uploadedIds.join(","));

        if (!busy && !anyUploaded) {
            status.textContent = sendable.length > maxFiles
                ? format(text("too-many"), maxFiles)
                : (sendable.length > 0 ? format(text("selected-count"), sendable.length) : "");
        }
    }

    input.addEventListener("change", function () {
        addFiles(input.files);
    });

    ["dragenter", "dragover"].forEach(function (type) {
        dropZone.addEventListener(type, function (e) {
            e.preventDefault();
            dropZone.classList.add("dragging");
        });
    });
    ["dragleave", "drop"].forEach(function (type) {
        dropZone.addEventListener(type, function (e) {
            e.preventDefault();
            dropZone.classList.remove("dragging");
        });
    });
    dropZone.addEventListener("drop", function (e) {
        if (e.dataTransfer && e.dataTransfer.files.length) {
            addFiles(e.dataTransfer.files);
        }
    });

    // ----- sending ----------------------------------------------------------

    function folderLink() {
        return config.getAttribute("data-folder-url") + "&directoryId="
            + encodeURIComponent(folder.value);
    }

    function showStatus(message, linkText, linkHref) {
        status.textContent = message;
        if (linkText) {
            var link = document.createElement("a");
            link.href = linkHref;
            link.textContent = linkText;
            status.appendChild(document.createTextNode(" "));
            status.appendChild(link);
        }
    }

    form.addEventListener("submit", function (e) {
        e.preventDefault();
        var sending = pending().filter(function (entry) {
            return !tooLarge(entry.file);
        });
        if (busy || sending.length === 0 || sending.length > maxFiles) {
            return;
        }

        var data = new FormData();
        Array.prototype.forEach.call(form.elements, function (field) {
            if (!field.name || field.type === "file" || field.type === "submit" || field.disabled) {
                return;
            }
            if ((field.type === "checkbox" || field.type === "radio") && !field.checked) {
                return;
            }
            data.append(field.name, field.value);
        });
        data.append("format", "json");
        sending.forEach(function (entry) {
            entry.state = "uploading";
            entry.error = null;
            data.append("uploadedFiles", entry.file, entry.file.name);
        });

        busy = true;
        update();
        status.textContent = format(text("uploading"), sending.length);

        fetch(form.action, {method: "POST", body: data, credentials: "same-origin"})
            .then(function (response) {
                var type = response.headers.get("Content-Type") || "";
                if (response.status >= 500) {
                    // the server may have saved files before it failed
                    throw {unknown: true};
                }
                if (!response.ok || type.indexOf("json") === -1) {
                    // refused before saving: too large, bad salt, session expired
                    throw {server: String(response.status)};
                }
                return response.json();
            })
            .then(function (result) {
                if (result.salt) {
                    form.querySelector("input[name='salt']").value = result.salt;
                }
                // match results to the files sent, by name, in order
                result.files.forEach(function (r) {
                    var entry = sending.filter(function (s) {
                        return s.state === "uploading" && s.file.name === r.name;
                    })[0];
                    if (!entry) {
                        return;
                    }
                    if (r.uploaded) {
                        entry.state = "uploaded";
                        uploadedIds.push(r.id);
                    } else {
                        entry.state = "failed";
                        entry.error = r.error;
                    }
                });
                var formError = (result.errors || []).join(" ");
                sending.forEach(function (entry) {
                    if (entry.state === "uploading") {
                        entry.state = "failed";
                        entry.error = formError || null;
                    }
                });
                busy = false;
                update();

                var failed = entries.filter(function (e) {
                    return e.state === "failed";
                }).length;
                if (failed === 0 && uploadedIds.length > 0) {
                    window.location.href = continueLink.href;
                } else if (uploadedIds.length > 0) {
                    showStatus(format(text("some-failed"), uploadedIds.length, failed));
                } else {
                    showStatus(formError || format(text("some-failed"), 0, failed));
                }
            })
            .catch(function (error) {
                busy = false;
                if (error && error.server) {
                    // the server refused the request, so nothing was saved
                    sending.forEach(function (entry) {
                        entry.state = "failed";
                        entry.error = null;
                    });
                    update();
                    showStatus(format(text("server-error"), error.server));
                } else {
                    // the request may or may not have reached the server
                    sending.forEach(function (entry) {
                        entry.state = "unknown";
                    });
                    update();
                    showStatus(text("unknown-outcome"), folder.options[folder.selectedIndex].text,
                        folderLink());
                }
            });
    });

    update();
})();
