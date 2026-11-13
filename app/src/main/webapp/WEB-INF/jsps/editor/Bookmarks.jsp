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

<%-- Blogroll: the weblog's blogrolls (bookmark folders) on the left, or in a strip above on a
     phone, and the links of one of them. Links are dragged (or moved with the arrow keys or touch-screen buttons on their
     handle) to reorder; selected links can be moved or deleted. Dialogs post with AJAX and reload
     the page, which then shows a short confirmation. Text the script needs is in data-* attributes. --%>

<s:url var="importURL" action="bookmarksImport">
    <s:param name="weblog" value="%{actionWeblog.handle}"/>
</s:url>

<div id="blogrollPage" class="blogroll-page"
     data-folder-id="<s:property value='folderId'/>"
     data-folder-name="<s:property value='folder.name'/>"
     data-folder-url="<s:url action='bookmarks' escapeAmp='false'><s:param name='weblog' value='%{actionWeblog.handle}'/></s:url>"
     data-msg-link-added="<s:text escapeHtml="true" name='bookmarkForm.created'/>"
     data-msg-link-saved="<s:text escapeHtml="true" name='bookmarkForm.updated'/>"
     data-msg-folder-added="<s:text escapeHtml="true" name='folderForm.created'/>"
     data-msg-folder-renamed="<s:text escapeHtml="true" name='folderForm.updatedNamed'/>"
     data-msg-folder-deleted="<s:text escapeHtml="true" name='bookmarksForm.folderDeleted'/>"
     data-msg-deleted-one="<s:text escapeHtml="true" name='bookmarksForm.deletedOne'/>"
     data-msg-deleted-many="<s:text escapeHtml="true" name='bookmarksForm.deletedMany'/>"
     data-msg-moved-one="<s:text escapeHtml="true" name='bookmarksForm.movedOne'/>"
     data-msg-moved-many="<s:text escapeHtml="true" name='bookmarksForm.movedMany'/>"
     data-msg-order-saved="<s:text escapeHtml="true" name='bookmarksForm.orderSaved'/>"
     data-msg-error="<s:text escapeHtml="true" name='generic.error.check.logs'/>"
     data-msg-name-required="<s:text escapeHtml="true" name='bookmarkForm.nameRequired'/>"
     data-msg-url-required="<s:text escapeHtml="true" name='bookmarkForm.urlRequired'/>"
     data-msg-bad-url="<s:text escapeHtml="true" name='bookmarkForm.badUrl'/>"
     data-msg-folder-name-required="<s:text escapeHtml="true" name='bookmarksForm.blogroll.requiredFields'/>"
     data-msg-selected="<s:text escapeHtml="true" name='bookmarksForm.selectedCount'/>"
     data-title-add-link="<s:text escapeHtml="true" name='bookmarksForm.addLink.title'/>"
     data-title-edit-link="<s:text escapeHtml="true" name='bookmarksForm.editLink.title'/>"
     data-title-add-folder="<s:text escapeHtml="true" name='bookmarksForm.addBlogroll.title'/>"
     data-title-rename-folder="<s:text escapeHtml="true" name='bookmarksForm.editBlogroll.title'/>"
     data-title-delete-one="<s:text escapeHtml="true" name='bookmarksForm.deleteOne.title'/>"
     data-title-delete-many="<s:text escapeHtml="true" name='bookmarksForm.deleteMany.title'/>">

<div class="blogroll-header">
    <p class="subtitle mb-0">
        <s:text name="bookmarksForm.subtitle">
            <s:param value="weblog"/>
        </s:text>
    </p>
    <div class="blogroll-header-actions">
        <a class="btn btn-outline-secondary" href="<s:property value='%{importURL}'/>">
            <span class="bi bi-upload" aria-hidden="true"></span>
            <s:text escapeHtml="true" name="bookmarksForm.importBookmarks"/>
        </a>
        <button type="button" id="addFolderButton" class="btn btn-outline-primary">
            <span class="bi bi-folder-plus" aria-hidden="true"></span>
            <s:text escapeHtml="true" name="bookmarksForm.addFolder"/>
        </button>
    </div>
</div>

<div class="blogroll-layout">

    <%-- the blogrolls; a GET link each, so a blogroll can be reloaded, bookmarked and reached with Back --%>
    <nav class="blogroll-nav" aria-label="<s:text escapeHtml="true" name='bookmarksForm.folders'/>">
        <ul class="blogroll-folders">
            <s:iterator var="f" value="allFolders">
                <s:url var="folderURL" action="bookmarks" escapeAmp="false">
                    <s:param name="weblog" value="%{actionWeblog.handle}"/>
                    <s:param name="folderId" value="%{#f.id}"/>
                </s:url>
                <li>
                    <a href="<s:property value='%{folderURL}'/>"
                       class="blogroll-folder<s:if test='#f.id == folderId'> active</s:if>"
                       <s:if test="#f.id == folderId">aria-current="page"</s:if>>
                        <span class="blogroll-folder-name">
                            <s:if test="#f.name == 'default'"><span class="bi bi-star" aria-hidden="true"></span></s:if>
                            <s:property value="plainText(#f.name)"/>
                        </span>
                        <span class="blogroll-folder-count"><s:property value="linkCount(#f)"/></span>
                    </a>
                </li>
            </s:iterator>
        </ul>
    </nav>

    <section class="blogroll-main" aria-labelledby="blogrollTitle">

        <div class="blogroll-main-header">
            <div class="blogroll-title-block">
                <h3 id="blogrollTitle" class="blogroll-title"><s:property value="plainText(folder.name)"/></h3>
                <span class="blogroll-meta">
                    <s:text escapeHtml="true" name="bookmarksForm.linkCount"><s:param value="links.size()"/></s:text>
                    <s:if test="defaultFolder">
                        &middot; <s:text escapeHtml="true" name="bookmarksForm.defaultNote"/>
                    </s:if>
                </span>
            </div>
            <div class="blogroll-main-actions">
                <button type="button" id="addLinkButton" class="btn btn-primary">
                    <span class="bi bi-plus-lg" aria-hidden="true"></span>
                    <s:text escapeHtml="true" name="bookmarksForm.addBookmark"/>
                </button>
                <s:if test="!defaultFolder">
                    <button type="button" id="renameFolderButton" class="btn btn-outline-secondary">
                        <span class="bi bi-pencil" aria-hidden="true"></span>
                        <s:text escapeHtml="true" name="generic.rename"/>
                    </button>
                    <button type="button" id="deleteFolderButton" class="btn btn-outline-danger">
                        <span class="bi bi-trash" aria-hidden="true"></span>
                        <s:text escapeHtml="true" name="bookmarksForm.deleteFolder"/>
                    </button>
                </s:if>
            </div>
        </div>

        <p class="pagetip" id="blogrollTip">
            <s:if test="defaultFolder"><s:text escapeHtml="true" name="bookmarksForm.rootPrompt"/></s:if>
            <s:else><s:text escapeHtml="true" name="bookmarksForm.otherPrompt"/></s:else>
        </p>

        <%-- carries the salt for the reorder post; the link ids are added by the script --%>
        <s:form id="linksReorderForm" action="bookmarks!reorder" theme="simple">
            <s:hidden name="salt"/>
            <s:hidden name="weblog"/>
            <s:hidden name="folderId"/>
        </s:form>

        <s:if test="!links.isEmpty()">

            <%-- move and delete the selected links; the ids are added by the script --%>
            <s:form id="linksMoveForm" action="bookmarks!move" theme="simple" cssClass="blogroll-bulkbar">
                <s:hidden name="salt"/>
                <s:hidden name="weblog"/>
                <s:hidden name="folderId"/>
                <div class="form-check mb-0">
                    <input type="checkbox" class="form-check-input" id="selectAllLinks">
                    <label class="form-check-label" for="selectAllLinks" id="selectedCount"><s:text escapeHtml="true" name="bookmarksForm.selectAllLabel"/></label>
                </div>
                <div class="blogroll-bulk-actions">
                    <s:if test="!moveTargets.isEmpty()">
                        <div class="input-group input-group-sm blogroll-move">
                            <label class="input-group-text" for="linksMoveForm_targetFolderId"><s:text escapeHtml="true" name="bookmarksForm.moveTo"/></label>
                            <s:select name="targetFolderId" id="linksMoveForm_targetFolderId" list="moveTargets"
                                      listKey="id" listValue="name" cssClass="form-select" disabled="true"/>
                            <button type="button" id="moveLinksButton" class="btn btn-outline-secondary" disabled>
                                <s:text escapeHtml="true" name="bookmarksForm.move"/>
                            </button>
                        </div>
                    </s:if>
                    <button type="button" id="deleteLinksButton" class="btn btn-sm btn-outline-danger" disabled>
                        <span class="bi bi-trash" aria-hidden="true"></span>
                        <s:text escapeHtml="true" name="bookmarksForm.delete"/>
                    </button>
                </div>
            </s:form>

            <ul id="linkList" class="link-list" aria-describedby="blogrollTip">
                <s:iterator var="bookmark" value="links">
                    <li class="link-row"
                        data-link-id="<s:property value='#bookmark.id'/>"
                        data-link-name="<s:property value='plainText(#bookmark.name)'/>"
                        data-link-url="<s:property value='#bookmark.url'/>"
                        data-link-feed="<s:property value='#bookmark.feedUrl'/>"
                        data-link-desc="<s:property value='plainText(#bookmark.description)'/>"
                        data-link-stored-name="<s:property value='#bookmark.name'/>"
                        data-link-stored-desc="<s:property value='#bookmark.description'/>"
                        data-link-image="<s:property value='#bookmark.image'/>">

                        <input type="checkbox" class="form-check-input link-select" value="<s:property value='#bookmark.id'/>"
                               aria-label="<s:text escapeHtml="true" name='bookmarksForm.selectOneLabel'><s:param value='plainText(#bookmark.name)'/></s:text>">

                        <button type="button" class="link-handle"
                                aria-label="<s:text escapeHtml="true" name='bookmarksForm.moveHandle'><s:param value='plainText(#bookmark.name)'/></s:text>">
                            <span class="bi bi-grip-vertical" aria-hidden="true"></span>
                        </button>

                        <%-- shown instead of the handle on touch screens, where it cannot be dragged --%>
                        <span class="link-move">
                            <button type="button" class="btn btn-sm btn-outline-secondary link-move-up"
                                    aria-label="<s:text escapeHtml="true" name='bookmarksForm.moveUp'><s:param value='plainText(#bookmark.name)'/></s:text>">
                                <span class="bi bi-chevron-up" aria-hidden="true"></span>
                            </button>
                            <button type="button" class="btn btn-sm btn-outline-secondary link-move-down"
                                    aria-label="<s:text escapeHtml="true" name='bookmarksForm.moveDown'><s:param value='plainText(#bookmark.name)'/></s:text>">
                                <span class="bi bi-chevron-down" aria-hidden="true"></span>
                            </button>
                        </span>

                        <s:if test="#bookmark.image != null && #bookmark.image != ''">
                            <img class="link-image" src="<s:property value='#bookmark.image'/>" alt="" loading="lazy">
                        </s:if>
                        <s:else>
                            <span class="link-image link-image-none" aria-hidden="true"><span class="bi bi-link-45deg"></span></span>
                        </s:else>

                        <div class="link-main">
                            <a href="#" class="link-name link-edit"><s:property value="plainText(#bookmark.name)"/></a>
                            <a class="link-url" href="<s:property value='#bookmark.url'/>" target="_blank" rel="noopener noreferrer"
                               title="<s:text escapeHtml="true" name='bookmarksForm.visitLink.tip'/>"><s:property value="#bookmark.url"/><span
                               class="bi bi-box-arrow-up-right" aria-hidden="true"></span></a>
                            <s:if test="#bookmark.description != null && #bookmark.description != ''">
                                <span class="link-desc"><s:property value="plainText(#bookmark.description)"/></span>
                            </s:if>
                        </div>

                        <%-- OPML import sets a link's feed to its own URL when the file names none --%>
                        <s:if test="#bookmark.feedUrl != null && #bookmark.feedUrl != '' && #bookmark.feedUrl != #bookmark.url">
                            <a class="link-feed" href="<s:property value='#bookmark.feedUrl'/>" target="_blank" rel="noopener noreferrer"
                               title="<s:text escapeHtml="true" name='bookmarksForm.feedurl'/>"
                               aria-label="<s:text escapeHtml="true" name='bookmarksForm.feedOf'><s:param value='plainText(#bookmark.name)'/></s:text>">
                                <span class="bi bi-rss" aria-hidden="true"></span>
                            </a>
                        </s:if>

                        <div class="link-actions">
                            <button type="button" class="btn btn-sm btn-outline-secondary link-edit">
                                <span class="bi bi-pencil" aria-hidden="true"></span>
                                <s:text escapeHtml="true" name="generic.edit"/>
                            </button>
                            <button type="button" class="btn btn-sm btn-outline-danger link-delete">
                                <span class="bi bi-trash" aria-hidden="true"></span>
                                <s:text escapeHtml="true" name="generic.delete"/>
                            </button>
                        </div>
                    </li>
                </s:iterator>
            </ul>
        </s:if>

        <s:else>
            <div class="blogroll-empty">
                <span class="bi bi-link-45deg" aria-hidden="true"></span>
                <p><s:text escapeHtml="true" name="bookmarksForm.noresults"/></p>
                <div class="blogroll-empty-actions">
                    <button type="button" class="btn btn-primary" id="emptyAddLinkButton">
                        <span class="bi bi-plus-lg" aria-hidden="true"></span>
                        <s:text escapeHtml="true" name="bookmarksForm.addBookmark"/>
                    </button>
                    <a class="btn btn-outline-secondary" href="<s:property value='%{importURL}'/>">
                        <span class="bi bi-upload" aria-hidden="true"></span>
                        <s:text escapeHtml="true" name="bookmarksForm.importBookmarks"/>
                    </a>
                </div>
            </div>
        </s:else>
    </section>
</div>

<div id="blogrollStatus" class="visually-hidden" role="status" aria-live="polite"></div>

<div class="toast-container position-fixed bottom-0 end-0 p-3">
    <div id="blogrollToast" class="toast align-items-center text-bg-success border-0" role="status" aria-atomic="true">
        <div class="d-flex">
            <div class="toast-body"></div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"
                    aria-label="<s:text escapeHtml="true" name='generic.close'/>"></button>
        </div>
    </div>
</div>

<%-- add/edit link dialog --%>

<div id="link-edit-modal" class="modal fade" tabindex="-1" aria-labelledby="link-edit-title" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title fs-5" id="link-edit-title"></h3>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<s:text escapeHtml="true" name='generic.close'/>"></button>
            </div>
            <div class="modal-body">
                <div id="linkEditErrors" class="alert alert-danger d-none" role="alert"></div>
                <s:form action="bookmarkEdit!save" id="linkEditForm" theme="simple" novalidate="novalidate">
                    <s:hidden name="salt"/>
                    <s:hidden name="weblog"/>
                    <s:hidden name="folderId"/>
                    <s:hidden name="bean.id" id="linkEditForm_bean_id"/>

                    <div class="mb-3">
                        <label class="form-label" for="linkEditForm_bean_url"><s:text escapeHtml="true" name="bookmarkForm.url"/></label>
                        <s:textfield name="bean.url" id="linkEditForm_bean_url" value="" maxlength="255" type="url"
                                     cssClass="form-control" required="required" placeholder="https://"
                                     aria-describedby="linkUrlFeedback"/>
                        <div id="linkUrlFeedback" class="invalid-feedback"></div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="linkEditForm_bean_name"><s:text escapeHtml="true" name="generic.name"/></label>
                        <s:textfield name="bean.name" id="linkEditForm_bean_name" value="" maxlength="255"
                                     cssClass="form-control" required="required"
                                     aria-describedby="linkNameFeedback linkNameHelp"/>
                        <div id="linkNameFeedback" class="invalid-feedback"></div>
                        <div id="linkNameHelp" class="form-text"><s:text escapeHtml="true" name="bookmarkForm.nameHelp"/></div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="linkEditForm_bean_description"><s:text escapeHtml="true" name="generic.description"/></label>
                        <s:textfield name="bean.description" id="linkEditForm_bean_description" value="" maxlength="255"
                                     cssClass="form-control"/>
                    </div>

                    <details id="linkAdvanced" class="link-advanced">
                        <summary><s:text escapeHtml="true" name="bookmarkForm.advanced"/></summary>
                        <div class="mt-3 mb-3">
                            <label class="form-label" for="linkEditForm_bean_feedUrl"><s:text escapeHtml="true" name="bookmarkForm.rssUrl"/></label>
                            <s:textfield name="bean.feedUrl" id="linkEditForm_bean_feedUrl" value="" maxlength="255" type="url"
                                         cssClass="form-control" aria-describedby="linkFeedHelp linkFeedFeedback"/>
                            <div id="linkFeedFeedback" class="invalid-feedback"></div>
                            <div id="linkFeedHelp" class="form-text"><s:text escapeHtml="true" name="bookmarkForm.feedHelp"/></div>
                        </div>
                        <div class="mb-1">
                            <label class="form-label" for="linkEditForm_bean_image"><s:text escapeHtml="true" name="bookmarkForm.image"/></label>
                            <s:textfield name="bean.image" id="linkEditForm_bean_image" value="" maxlength="255" type="url"
                                         cssClass="form-control" aria-describedby="linkImageHelp linkImageFeedback"/>
                            <div id="linkImageFeedback" class="invalid-feedback"></div>
                            <div id="linkImageHelp" class="form-text"><s:text escapeHtml="true" name="bookmarkForm.imageHelp"/></div>
                            <img id="linkImagePreview" class="link-image-preview d-none" alt="">
                        </div>
                    </details>
                </s:form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal"><s:text escapeHtml="true" name="generic.cancel"/></button>
                <button type="button" id="linkSaveButton" class="btn btn-primary"><s:text escapeHtml="true" name="generic.save"/></button>
            </div>
        </div>
    </div>
</div>

<%-- add/rename blogroll dialog --%>

<div id="folder-edit-modal" class="modal fade" tabindex="-1" aria-labelledby="folder-edit-title" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title fs-5" id="folder-edit-title"></h3>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<s:text escapeHtml="true" name='generic.close'/>"></button>
            </div>
            <div class="modal-body">
                <div id="folderEditErrors" class="alert alert-danger d-none" role="alert"></div>
                <s:form action="folderEdit!save" id="folderEditForm" theme="simple" novalidate="novalidate">
                    <s:hidden name="salt"/>
                    <s:hidden name="weblog"/>
                    <s:hidden name="bean.id" id="folderEditForm_bean_id" value=""/>
                    <label class="form-label" for="folderEditForm_bean_name"><s:text escapeHtml="true" name="bookmarksForm.blogrollName"/></label>
                    <s:textfield name="bean.name" id="folderEditForm_bean_name" value="" maxlength="255"
                                 cssClass="form-control" required="required" aria-describedby="folderNameFeedback"/>
                    <div id="folderNameFeedback" class="invalid-feedback"></div>
                </s:form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal"><s:text escapeHtml="true" name="generic.cancel"/></button>
                <button type="button" id="folderSaveButton" class="btn btn-primary"><s:text escapeHtml="true" name="generic.save"/></button>
            </div>
        </div>
    </div>
</div>

<%-- delete links dialog --%>

<div id="delete-links-modal" class="modal fade" tabindex="-1" aria-labelledby="delete-links-title" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title fs-5" id="delete-links-title"></h3>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<s:text escapeHtml="true" name='generic.close'/>"></button>
            </div>
            <div class="modal-body">
                <div id="deleteLinksErrors" class="alert alert-danger d-none" role="alert"></div>
                <s:form id="linksDeleteForm" action="bookmarks!delete" theme="simple">
                    <s:hidden name="salt"/>
                    <s:hidden name="weblog"/>
                    <s:hidden name="folderId"/>
                </s:form>
                <p class="mb-0"><s:text escapeHtml="true" name="bookmarksForm.delete.areYouSure"/></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal"><s:text escapeHtml="true" name="generic.cancel"/></button>
                <button type="button" id="deleteLinksConfirm" class="btn btn-danger"><s:text escapeHtml="true" name="generic.delete"/></button>
            </div>
        </div>
    </div>
</div>

<%-- delete blogroll dialog --%>

<s:if test="!defaultFolder">
<div id="delete-folder-modal" class="modal fade" tabindex="-1" aria-labelledby="delete-folder-title" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title fs-5" id="delete-folder-title">
                    <s:text escapeHtml="true" name="blogrollDeleteOK.removeBlogroll"><s:param value="plainText(folder.name)"/></s:text>
                </h3>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<s:text escapeHtml="true" name='generic.close'/>"></button>
            </div>
            <div class="modal-body">
                <div id="deleteFolderErrors" class="alert alert-danger d-none" role="alert"></div>
                <s:form id="folderDeleteForm" action="bookmarks!deleteFolder" theme="simple">
                    <s:hidden name="salt"/>
                    <s:hidden name="weblog"/>
                    <s:hidden name="folderId"/>
                </s:form>
                <p class="mb-0">
                    <s:if test="!links.isEmpty()">
                        <s:text escapeHtml="true" name="blogrollDeleteOK.areYouSure"><s:param value="plainText(folder.name)"/><s:param value="links.size()"/></s:text>
                    </s:if>
                    <s:else>
                        <s:text escapeHtml="true" name="blogrollDeleteOK.empty"><s:param value="plainText(folder.name)"/></s:text>
                    </s:else>
                </p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal"><s:text escapeHtml="true" name="generic.cancel"/></button>
                <button type="button" id="deleteFolderConfirm" class="btn btn-danger"><s:text escapeHtml="true" name="bookmarksForm.deleteFolder"/></button>
            </div>
        </div>
    </div>
</div>
</s:if>

</div>

<script src="<s:url value='/roller-ui/scripts/blogroll.js'/>"></script>
