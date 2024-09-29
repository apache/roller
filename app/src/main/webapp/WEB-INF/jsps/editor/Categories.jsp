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

<p class="subtitle">
    <s:text name="categoriesForm.subtitle">
        <s:param value="weblog"/>
    </s:text>
</p>
<p class="pagetip">
    <s:text name="categoriesForm.rootPrompt"/>
</p>

<%-- Form is a table of categories each with checkbox --%>
<s:form action="categories!move">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <s:hidden name="categoryId"/>

    <table class="rollertable table table-striped" width="100%">

        <tr class="rollertable">
            <th width="30%"><s:text name="generic.name"/></th>
            <th width="50%"><s:text name="generic.description"/></th>
            <th width="10%"><s:text name="generic.edit"/></th>
            <th width="10%"><s:text name="categoriesForm.remove"/></th>
        </tr>

        <s:if test="AllCategories != null && !AllCategories.isEmpty">

            <s:iterator var="category" value="AllCategories" status="rowstatus">
                <tr>
                    <td><s:property value="#category.name"/></td>

                    <td><s:property value="#category.description"/></td>

                    <td align="center">

                        <s:set var="categoryId"    value="#category.id" />
                        <s:set var="categoryName"  value="#category.name" />
                        <s:set var="categoryDesc"  value="#category.description" />
                        <s:set var="categoryImage" value="#category.image" />
                        <a href="#" onclick="showCategoryEditModal(
                                '<s:property value="categoryId" />',
                                '<s:property value="categoryName"/>',
                                '<s:property value="categoryDesc"/>',
                                '<s:property value="categoryImage"/>' )">
                            <span class="glyphicon glyphicon-edit"></span>
                        </a>

                    </td>

                    <td class="rollertable" align="center">
                        <s:if test="AllCategories.size() > 1">

                            <s:set var="categoryId"    value="#category.id" />
                            <s:set var="categoryName"  value="#category.name" />
                            <s:set var="categoryInUse" value="#category.inUse.toString()" />
                            <a href="#" onclick="showCategoryDeleteModal(
                                    '<s:property value="categoryId" />',
                                    '<s:property value="categoryName" />',
                                    <s:property value="categoryInUse"/> )" >
                                <span class="glyphicon glyphicon-trash"></span>
                            </a>

                        </s:if>
                    </td>

                </tr>
            </s:iterator>

        </s:if>

        <s:else>
            <tr>
                <td style="vertical-align:middle" colspan="6"><s:text name="categoriesForm.noresults"/></td>
            </tr>
        </s:else>

    </table>

</s:form>


<%-- ============================================================= --%>
<%-- add/edit category modal --%>

<div id="category-edit-modal" class="modal fade category-edit-modal" tabindex="-1" role="dialog">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <div class="modal-header">
                <h3 id="category-edit-title"></h3>
            </div>

            <div class="modal-body">
                <s:form action="categoryEdit" id="categoryEditForm" theme="bootstrap" cssClass="form-horizontal">
                    <s:hidden name="salt"/>
                    <s:hidden name="weblog"/>
                    <s:hidden name="bean.id"/>

                    <%-- action needed here because we are using AJAX to post this form --%>
                    <s:hidden name="action:categoryEdit!save" value="save"/>

                    <s:textfield name="bean.name" label="%{getText('generic.name')}" maxlength="255"
                                 onchange="validateCategory()" onkeyup="validateCategory()" />

                    <s:textfield name="bean.description" label="%{getText('generic.description')}"/>

                    <s:textfield name="bean.image" label="%{getText('categoryForm.image')}"
                                 onchange="validateCategory()" onkeyup="validateCategory()" />
                </s:form>
            </div>

            <div class="modal-footer">
                <p id="feedback-area-edit"></p>
                <button onclick="submitEditedCategory()" class="btn btn-primary">
                    <s:text name="generic.save"/>
                </button>
                <button type="button" class="btn" data-dismiss="modal">
                    <s:text name="generic.cancel"/>
                </button>
            </div>

        </div>
    </div>
</div>

<script>

    var feedbackAreaEdit = $("#feedback-area-edit");

    function showCategoryEditModal( id, name, desc, image ) {
        feedbackAreaEdit.html("");
        $('#category-edit-title').html('<s:text name="categoryForm.edit.title" />');

        $('#categoryEditForm_bean_id').val(id);
        $('#categoryEditForm_bean_name').val(name);
        $('#categoryEditForm_bean_description').val(desc);
        $('#categoryEditForm_bean_image').val(image);

        $('#category-edit-modal').modal({show: true});

    }

    function validateCategory() {

        var saveCategoryButton = $('#categoryEditForm:first');

        var categoryName = $("#categoryEditForm_bean_name").val();
        var imageURL = $("#categoryEditForm_bean_image").val();

        if (!categoryName || categoryName.trim() === '') {
            saveCategoryButton.attr("disabled", true);
            feedbackAreaEdit.html('<s:text name="categoryForm.requiredFields" />');
            feedbackAreaEdit.css("color", "red");
            return;
        }

        if (imageURL && imageURL.trim() !== '') {
            if (!isValidUrl(imageURL)) {
                saveCategoryButton.attr("disabled", true);
                feedbackAreaEdit.html('<s:text name="categoryForm.badURL" />');
                feedbackAreaEdit.css("color", "red");
                return;
            }
        }

        feedbackAreaEdit.html('');
        saveCategoryButton.attr("disabled", false);
    }

    function submitEditedCategory() {

        // if name is empty reject and show error message
        if ($("#categoryEditForm_bean_name").val().trim() === "") {
            feedbackAreaEdit.html('<s:text name="categoryForm.requiredFields" />');
            feedbackAreaEdit.css("color", "red");
            return;
        }

        // post category via AJAX
        $.ajax({
            method: 'post',
            url: "categoryEdit!save.rol",
            data: $("#categoryEditForm").serialize(),
            context: document.body

        }).done(function (data) {

            // kludge: scrape response status from HTML returned by Struts
            var alertEnd = data.indexOf("ALERT_END");
            var notUnique = data.indexOf('<s:text name="categoryForm.error.duplicateName" />');
            var notValid = data.indexOf('<s:text name="categoryForm.error.invalidName" />');
            if (notUnique > 0 && notUnique < alertEnd) {
                feedbackAreaEdit.css("color", "red");
                feedbackAreaEdit.html('<s:text name="categoryForm.error.duplicateName" />');
            } else if (notValid > 0 && notValid < alertEnd) {
                feedbackAreaEdit.css("color", "red");
                feedbackAreaEdit.html('<s:text name="categoryForm.error.invalidName" />');
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


<%-- ============================================================= --%>
<%-- delete confirmation modal --%>

<div id="delete-category-modal" class="modal fade delete-category-modal" tabindex="-1" role="dialog">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <div class="modal-header">
                <h3>
                    <s:text name="categoryDeleteOK.removeCategory"/>:
                    <span id="category-name"></span>
                </h3>
            </div>

            <s:form action="categoryRemove!remove" theme="bootstrap" cssClass="form-horizontal">
                <s:hidden name="salt"/>
                <s:hidden name="weblog"/>
                <s:hidden name="removeId"/>
                
                <div class="modal-body">

                    <div id="category-in-use" style="display:none">
                        <p>
                            <s:text name="categoryDeleteOK.warningCatInUse"/>
                            <s:text name="categoryDeleteOK.youMustMoveEntries"/>
                        </p>
                        <s:text name="categoryDeleteOK.moveToWhere"/>
                        <s:select name="targetCategoryId" list="allCategories" listKey="id" listValue="name"/>
                    </div>

                    <div id="category-empty" style="display:none">
                        <p><s:text name="categoryDeleteOK.noEntriesInCat"/></p>
                    </div>
                    
                    <p> <strong><s:text name="categoryDeleteOK.areYouSure"/></strong> </p>
                </div>

                <div class="modal-footer">
                    <s:submit cssClass="btn btn-danger" value="%{getText('generic.yes')}"/>&nbsp;
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <s:text name="generic.no" />
                    </button>
                </div>

            </s:form>

        </div>
    </div>
</div>

<script>

    function showCategoryDeleteModal( id, name, inUse ) {
        $('#categoryRemove_removeId').val(id);
        $('#categoryEdit_bean_name').val(name);
        $('#category-name').html(name);
        if ( inUse ) {
            $('#category-in-use').css('display','block');
            $('#category-emtpy').css('display', 'none');
        } else {
            $('#category-in-use').css('display', 'none');
            $('#category-emtpy').css('display', 'block');
        }
        populateCategorySelect(id);
        $('#delete-category-modal').modal({show: true});
    }

    function populateCategorySelect(removeId) {
        const allCategories = [];

        <s:iterator value="allCategories" var="category">
        allCategories.push({
            id: '<s:property value="#category.id"/>',
            name: '<s:property value="#category.name"/>'
        });
        </s:iterator>

        const select = $('#categoryRemove_targetCategoryId');
        select.empty();
        allCategories.forEach(function(category) {
            if (category.id !== removeId) {
                select.append(new Option(category.name, category.id));
            }
        });
    }

</script>
