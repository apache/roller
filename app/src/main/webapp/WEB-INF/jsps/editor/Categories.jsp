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

<%-- Categories: one list in display order. Rows are dragged (or moved with the arrow keys on
     their handle, or the up and down buttons on touch screens) to reorder; Add, Edit and Delete open dialogs that post with AJAX and reload
     the page, which then shows a short confirmation. Text the script needs is in data-* attributes. --%>

<div id="categoriesPage" class="categories-page"
     data-msg-added="<s:text escapeHtml="true" name='categoryForm.created'/>"
     data-msg-saved="<s:text escapeHtml="true" name='categoryForm.changesSaved'/>"
     data-msg-removed="<s:text escapeHtml="true" name='categoryForm.removed'/>"
     data-msg-order-saved="<s:text escapeHtml="true" name='categoriesForm.orderSaved'/>"
     data-msg-error="<s:text escapeHtml="true" name='generic.error.check.logs'/>"
     data-msg-name-required="<s:text escapeHtml="true" name='categoryForm.requiredFields'/>"
     data-msg-bad-url="<s:text escapeHtml="true" name='categoryForm.badURL'/>"
     data-title-add="<s:text escapeHtml="true" name='categoryForm.add.title'/>"
     data-title-edit="<s:text escapeHtml="true" name='categoryForm.edit.title'/>"
     data-title-delete="<s:text escapeHtml="true" name='categoryDeleteOK.heading'/>">

<div class="categories-header">
    <p class="subtitle mb-0">
        <s:text name="categoriesForm.subtitle">
            <s:param value="weblog"/>
        </s:text>
    </p>
    <button type="button" id="addCategoryButton" class="btn btn-primary">
        <span class="bi bi-plus-lg" aria-hidden="true"></span>
        <s:text escapeHtml="true" name="categoriesForm.addCategory"/>
    </button>
</div>

<p class="pagetip" id="categoriesTip"><s:text escapeHtml="true" name="categoriesForm.rootPrompt"/></p>

<%-- carries the salt for the reorder post; the category ids are added by the script --%>
<s:form id="categoriesReorderForm" action="categories!reorder" theme="simple">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
</s:form>

<s:if test="allCategories.isEmpty()">
    <p class="categories-empty"><s:text escapeHtml="true" name="categoriesForm.noresults"/></p>
</s:if>

<ul id="categoryList" class="category-list" aria-describedby="categoriesTip">
    <s:iterator var="category" value="allCategories">
        <s:url var="entriesURL" escapeAmp="false" action="entries">
            <s:param name="weblog" value="%{actionWeblog.handle}"/>
            <s:param name="bean.categoryName" value="%{#category.name}"/>
        </s:url>
        <li class="category-row"
            data-category-id="<s:property value='#category.id'/>"
            data-category-name="<s:property value='#category.name'/>"
            data-category-desc="<s:property value='#category.description'/>"
            data-category-image="<s:property value='#category.image'/>"
            data-in-use="<s:property value='isInUse(#category.id)'/>"
            data-delete-text="<s:if test='getEntryCount(#category.id) > 0'><s:text escapeHtml="true" name='categoryDeleteOK.entriesInCategory'><s:param value='getEntryCount(#category.id)'/></s:text></s:if><s:elseif test='isInUse(#category.id)'><s:text escapeHtml="true" name='categoryDeleteOK.warningCatInUse'/></s:elseif><s:else><s:text escapeHtml="true" name='categoryDeleteOK.noEntriesInCat'/></s:else>">

            <button type="button" class="category-handle"
                    aria-label="<s:text escapeHtml="true" name='categoriesForm.moveHandle'><s:param value='#category.name'/></s:text>">
                <span class="bi bi-grip-vertical" aria-hidden="true"></span>
            </button>

            <%-- shown instead of the handle on touch screens, where it cannot be dragged --%>
            <span class="category-move">
                <button type="button" class="btn btn-sm btn-outline-secondary category-move-up"
                        aria-label="<s:text escapeHtml="true" name='categoriesForm.moveUp'><s:param value='#category.name'/></s:text>">
                    <span class="bi bi-chevron-up" aria-hidden="true"></span>
                </button>
                <button type="button" class="btn btn-sm btn-outline-secondary category-move-down"
                        aria-label="<s:text escapeHtml="true" name='categoriesForm.moveDown'><s:param value='#category.name'/></s:text>">
                    <span class="bi bi-chevron-down" aria-hidden="true"></span>
                </button>
            </span>

            <s:if test="#category.image != null && #category.image != ''">
                <img class="category-image" src="<s:property value='#category.image'/>" alt="" loading="lazy">
            </s:if>

            <div class="category-main">
                <a href="#" class="category-name category-edit-link"><s:property value="#category.name"/></a>
                <s:if test="#category.description != null && #category.description != ''">
                    <span class="category-desc"><s:property value="#category.description"/></span>
                </s:if>
            </div>

            <s:if test="getEntryCount(#category.id) > 0">
                <a class="category-count" href="<s:property value='%{entriesURL}'/>"
                   title="<s:text escapeHtml="true" name='categoriesForm.viewEntries'><s:param value='#category.name'/></s:text>">
                    <s:text escapeHtml="true" name="categoriesForm.entryCount"><s:param value="getEntryCount(#category.id)"/></s:text>
                </a>
            </s:if>
            <s:else>
                <span class="category-count"><s:text escapeHtml="true" name="categoriesForm.entryCount"><s:param value="0"/></s:text></span>
            </s:else>

            <div class="category-actions">
                <button type="button" class="btn btn-sm btn-outline-secondary category-edit-link">
                    <span class="bi bi-pencil" aria-hidden="true"></span>
                    <s:text escapeHtml="true" name="generic.edit"/>
                </button>
                <s:if test="allCategories.size() > 1">
                    <button type="button" class="btn btn-sm btn-outline-danger category-delete-link">
                        <span class="bi bi-trash" aria-hidden="true"></span>
                        <s:text escapeHtml="true" name="generic.delete"/>
                    </button>
                </s:if>
                <s:else>
                    <%-- a disabled button gets no hover events, so the wrapper carries the tooltip --%>
                    <span class="d-inline-block" tabindex="0" title="<s:text escapeHtml="true" name='categoriesForm.lastCategory'/>">
                        <button type="button" class="btn btn-sm btn-outline-danger" disabled>
                            <span class="bi bi-trash" aria-hidden="true"></span>
                            <s:text escapeHtml="true" name="generic.delete"/>
                        </button>
                    </span>
                </s:else>
            </div>
        </li>
    </s:iterator>
</ul>

<div id="categoryStatus" class="visually-hidden" role="status" aria-live="polite"></div>

<div class="toast-container position-fixed bottom-0 end-0 p-3">
    <div id="categoryToast" class="toast align-items-center text-bg-success border-0" role="status" aria-atomic="true">
        <div class="d-flex">
            <div class="toast-body"></div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"
                    aria-label="<s:text escapeHtml="true" name='generic.close'/>"></button>
        </div>
    </div>
</div>

<%-- add/edit dialog --%>

<div id="category-edit-modal" class="modal fade" tabindex="-1" aria-labelledby="category-edit-title" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title fs-5" id="category-edit-title"></h3>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<s:text escapeHtml="true" name='generic.close'/>"></button>
            </div>
            <div class="modal-body">
                <div id="categoryEditErrors" class="alert alert-danger d-none" role="alert"></div>
                <s:form action="categoryEdit!save" id="categoryEditForm" theme="simple" novalidate="novalidate">
                    <s:hidden name="salt"/>
                    <s:hidden name="weblog"/>
                    <s:hidden name="bean.id" id="categoryEditForm_bean_id"/>

                    <div class="mb-3">
                        <label class="form-label" for="categoryEditForm_bean_name"><s:text escapeHtml="true" name="generic.name"/></label>
                        <s:textfield name="bean.name" id="categoryEditForm_bean_name" value="" maxlength="255"
                                     cssClass="form-control" required="required"
                                     aria-describedby="categoryNameFeedback"/>
                        <div id="categoryNameFeedback" class="invalid-feedback"></div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label" for="categoryEditForm_bean_description"><s:text escapeHtml="true" name="generic.description"/></label>
                        <s:textfield name="bean.description" id="categoryEditForm_bean_description" value=""
                                     maxlength="255" cssClass="form-control"/>
                    </div>

                    <div class="mb-1">
                        <label class="form-label" for="categoryEditForm_bean_image"><s:text escapeHtml="true" name="categoryForm.image"/></label>
                        <div class="input-group has-validation">
                            <s:textfield name="bean.image" id="categoryEditForm_bean_image" value="" maxlength="255"
                                         cssClass="form-control" type="url"
                                         aria-describedby="categoryImageHelp categoryImageFeedback"/>
                            <s:if test="mediaLibraryEnabled">
                                <button type="button" id="categoryImageChooseButton" class="btn btn-outline-secondary">
                                    <span class="bi bi-images" aria-hidden="true"></span>
                                    <s:text escapeHtml="true" name="categoryForm.chooseImage"/>
                                </button>
                            </s:if>
                            <div id="categoryImageFeedback" class="invalid-feedback"></div>
                        </div>
                        <div id="categoryImageHelp" class="form-text"><s:text escapeHtml="true" name="%{mediaLibraryEnabled ? 'categoryForm.imageHelpMedia' : 'categoryForm.imageHelp'}"/></div>
                        <img id="categoryImagePreview" class="category-image-preview d-none" alt="">
                    </div>
                </s:form>
                <s:if test="mediaLibraryEnabled">
                    <%-- the media chooser, images only; shown in place of the form --%>
                    <s:url var="imageChooserURL" action="mediaFileImageChooser" namespace="overlay" escapeAmp="false">
                        <s:param name="weblog" value="%{actionWeblog.handle}"/>
                        <s:param name="bean.type" value="imageChooserType"/>
                    </s:url>
                    <div id="categoryImageChooser" class="d-none"
                         data-src="<s:property value='%{imageChooserURL}'/>"
                         data-title="<s:text escapeHtml="true" name='categoryForm.chooseImage.title'/>">
                        <iframe id="categoryImageChooserFrame" class="category-image-chooser"
                                title="<s:text escapeHtml="true" name='categoryForm.chooseImage.title'/>"></iframe>
                    </div>
                </s:if>
            </div>
            <div class="modal-footer">
                <s:if test="mediaLibraryEnabled">
                    <button type="button" id="categoryChooserBackButton" class="btn btn-outline-secondary d-none me-auto">
                        <span class="bi bi-arrow-left" aria-hidden="true"></span>
                        <s:text escapeHtml="true" name="categoryForm.chooseImage.back"/>
                    </button>
                </s:if>
                <button type="button" id="categoryCancelButton" class="btn btn-outline-secondary" data-bs-dismiss="modal"><s:text escapeHtml="true" name="generic.cancel"/></button>
                <button type="button" id="categorySaveButton" class="btn btn-primary"><s:text escapeHtml="true" name="generic.save"/></button>
            </div>
        </div>
    </div>
</div>

<%-- delete dialog --%>

<div id="delete-category-modal" class="modal fade" tabindex="-1" aria-labelledby="category-delete-title" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title fs-5" id="category-delete-title"></h3>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="<s:text escapeHtml="true" name='generic.close'/>"></button>
            </div>
            <div class="modal-body">
                <div id="categoryDeleteErrors" class="alert alert-danger d-none" role="alert"></div>
                <s:form action="categoryRemove!remove" id="categoryRemoveForm" theme="simple">
                    <s:hidden name="salt"/>
                    <s:hidden name="weblog"/>
                    <s:hidden name="removeId" id="categoryRemove_removeId"/>
                    <p id="categoryDeleteText"></p>
                    <div id="categoryMoveTarget" class="mb-2">
                        <label class="form-label" for="categoryRemove_targetCategoryId"><s:text escapeHtml="true" name="categoryDeleteOK.moveToWhere"/></label>
                        <%-- options are filled in by the script, without the category being deleted --%>
                        <s:select name="targetCategoryId" id="categoryRemove_targetCategoryId" list="{}"
                                  headerKey="" headerValue="%{getText('categoryDeleteOK.chooseCategory')}"
                                  cssClass="form-select"/>
                    </div>
                </s:form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal"><s:text escapeHtml="true" name="generic.cancel"/></button>
                <button type="button" id="categoryDeleteButton" class="btn btn-danger"><s:text escapeHtml="true" name="categoryDeleteOK.delete"/></button>
            </div>
        </div>
    </div>
</div>

</div>

<script src="<s:url value='/roller-ui/scripts/categories.js'/>"></script>
