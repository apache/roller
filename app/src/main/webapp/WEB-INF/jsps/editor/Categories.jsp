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

<%-- JavaScript for categories table --%>
<script>
    function onMove() {
        if (confirm("<s:text name='categoriesForm.move.confirm' />")) {
            document.categories.method.value = "moveSelected";
            document.categories.submit();
        }
    }
</script>

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

            <s:iterator id="category" value="AllCategories" status="rowstatus">
                <tr>
                    <td><s:property value="#category.name"/></td>

                    <td><s:property value="#category.description"/></td>

                    <td align="center">

                        <s:set name="categoryId"    value="#category.id" />
                        <s:set name="categoryName"  value="#category.name" />
                        <s:set name="categoryDesc"  value="#category.description" />
                        <s:set name="categoryImage" value="#category.image" />
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
                            
                            <s:set name="categoryId"    value="#category.id" />
                            <s:set name="categoryName"  value="#category.name" />
                            <s:set name="categoryInUse" value="#category.inUse.toString()" />
                            <a href="#" onclick="showCategoryDeleteModal(
                                    '<s:property value="categoryId" />',
                                    '<s:property value="categoryName" />',
                                    '<s:property value="categoryInUse"/>' )" >
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

            <s:set var="mainAction">categoryEdit</s:set>
            
            <div class="modal-header">
                <h3><s:text name="categoryForm.edit.title" /></h3>
                <p class="pagetip">
                    <s:text name="categoryForm.requiredFields">
                        <s:param><s:text name="generic.name"/></s:param>
                    </s:text>
                </p>
            </div>
            
            <s:form action="categoryEdit!save" theme="bootstrap" cssClass="form-horizontal">
                <s:hidden name="salt"/>
                <s:hidden name="weblog"/>
                <s:hidden name="bean.id"/>

                <div class="modal-body">
                    <s:textfield name="bean.name"        label="%{getText('generic.name')}" maxlength="255"/>
                    <s:textfield name="bean.description" label="%{getText('generic.description')}"/>
                    <s:textfield name="bean.image"       label="%{getText('categoryForm.image')}"/>
                </div>

                <div class="modal-footer">
                    <s:submit cssClass="btn btn-primary" 
                              value="%{getText('generic.save')}" action="%{#mainAction}!save"/>
                    <button type="button" class="btn" data-dismiss="modal">
                        <s:text name="generic.cancel" />
                    </button>
                </div>
            </s:form>

        </div>
    </div>
</div>

<script>
    function showCategoryEditModal( id, name, desc, image ) {
        $('#categoryEdit_bean_id').val(id);
        $('#categoryEdit_bean_name').val(name);
        $('#categoryEdit_bean_description').val(desc);
        $('#categoryEdit_bean_image').val(image);
        $('#category-edit-modal').modal({show: true});
    }

    <%--
    (function() {
        $('form > input').keyup(function() {

            var empty = false;
            $('form > input').each(function() {
                if ($(this).val() == '') {
                    empty = true;
                }
            });

            if (empty) {
                $('#register').attr('disabled', 'disabled'); 
            } else {
                $('#register').removeAttr('disabled'); 
            }
        });
    })()
    --%>
    
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

                    <div id="category-in-use" class="alert alert-danger" role="alert" style="display:none">
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
                    <s:submit cssClass="btn" 
                              value="%{getText('generic.yes')}"/>&nbsp;
                    <button type="button" class="btn btn-default btn-primary" data-dismiss="modal">
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
        $('#delete-category-modal').modal({show: true});
    }
</script>
