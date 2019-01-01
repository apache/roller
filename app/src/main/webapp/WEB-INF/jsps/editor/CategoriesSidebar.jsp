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
    <s:set var="categoryId" value="#bean.id"/>
    <s:set var="categoryName" value="#post.name"/>
    <s:set var="categoryDesc" value="#post.description"/>
    <s:set var="categoryImage" value="#post.image"/>

    <a href="#" onclick="showCategoryAddModal()">
        <span class="glyphicon glyphicon-plus"></span>
        <s:text name="categoriesForm.addCategory"/>
    </a>
</p>

<script>

    var feedbackArea = $("#feedback-area");

    function showCategoryAddModal() {

        feedbackAreaEdit.html("");
        $('#category-edit-title').html('<s:text name="categoryForm.add.title" />');

        $('#categoryEditForm_bean_id').val("");
        $('#categoryEditForm_bean_name').val("");
        $('#categoryEditForm_bean_description').val("");
        $('#categoryEditForm_bean_image').val("");

        validateCategory();

        $('#category-edit-modal').modal({show: true});
    }

</script>

