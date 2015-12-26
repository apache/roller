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

<h3><s:text name="mainPage.actions"/></h3>
<hr size="1" noshade="noshade"/>

<p>
    <s:set name="categoryId" value="#bean.id"/>
    <s:set name="categoryName" value="#post.name"/>
    <s:set name="categoryDesc" value="#post.description"/>
    <s:set name="categoryImage" value="#post.image"/>
    <a href="#" onclick="showCategoryAddModal()">
        <span class="glyphicon glyphicon-plus"></span>
        <s:text name="categoriesForm.addCategory"/>
    </a>
</p>


<div id="category-add-modal" class="modal fade category-add-modal" tabindex="-1" role="dialog">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <div class="modal-header">
                <h3> <s:text name="categoryForm.add.title"/> </h3>
            </div>

            <div class="modal-body">
                <s:form id="categoryAddForm" theme="bootstrap" cssClass="form-horizontal">
                    <s:hidden name="salt"/>
                    <s:hidden name="weblog"/>
                    <s:textfield name="bean.name"        label="%{getText('generic.name')}" maxlength="255"/>
                    <s:textfield name="bean.description" label="%{getText('generic.description')}"/>
                    <s:textfield name="bean.image"       label="%{getText('categoryForm.image')}"/>
                </s:form>
            </div>

            <div class="modal-footer">
                <button onclick="submitNewCategory()" class="btn btn-primary">
                    <s:text name="generic.save" />
                </button>
                <button type="button" class="btn" data-dismiss="modal">
                    <s:text name="generic.cancel" />
                </button>
            </div>

        </div>
    </div>
</div>

<script>
    function showCategoryAddModal( postId, postTitle ) {
        $('#category-add-modal').modal({show: true});
    }
    function submitNewCategory() {

        // if name is empty reject and show error message
        if ( $("#categoryAdd_bean_name").val().trim() == "" ) {
            alert("Name is required");
            return;
        }
        
        // post category via Ajax
        $.ajax({
            url: "categoryAdd!save",
            data: $("#categoryAddForm").serialize(),
            context: document.body
        }).done(function(data) {
            alert("Done");
        });
        
        // if post failed, then show error messages
        
        // else dismiss modal
    }
</script>
