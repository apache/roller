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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.0/jquery-ui.min.css"/>' />
<script src="<s:url value="/tb-ui/scripts/jquery-2.1.1.min.js" />"></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.0/jquery-ui.min.js"/>'></script>

<script>
  $(function() {
    $.ajaxSetup({
        statusCode: { 408: function() { document.categoriesForm.submit(); } }
    });
    $("#category-edit").dialog({
      autoOpen: false,
      height:200,
      modal: true,
      buttons: {
        "<s:text name='generic.save'/>": function() {
            var salt = $("#categories_salt").val();
            var idToUpdate = $(this).data('categoryId');
            var newName = $('#category-edit-name').val().trim();
            if (newName.length > 0) {
                $.ajax({
                    type: "PUT",
                    url: (idToUpdate == '') ?
                        '<%= request.getContextPath()%>/tb-ui/authoring/rest/categories?weblog=<s:text name="weblog"/>&salt=' + salt
                        : '<%= request.getContextPath()%>/tb-ui/authoring/rest/category/' + idToUpdate +'?salt=' + salt,
                    data: JSON.stringify(newName),
                    contentType: "application/json",
                    processData: "false",
                    success: function (data, textStatus, xhr) {
                        if (idToUpdate == '') {
                            document.categoriesForm.submit();
                        } else {
                            $('#catname-' + idToUpdate).text(newName)
                            $('#catid-' + idToUpdate).attr('data-name', newName)
                            $("#category-edit").dialog().dialog( "close" );
                        }
                    },
                    error: function(xhr, status, errorThrown) {
                        if (xhr.status in this.statusCode) return;
                        $('#category-edit-error').css("display", "inline");
                    }
                });
            }
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });

    $("#category-remove").dialog({
      autoOpen: false,
      height:275,
      modal: true,
      buttons: {
        "<s:text name='generic.confirm'/>": function() {
            var salt = $("#categories_salt").val();
            var idToRemove = $(this).data('categoryId');
            var targetCategoryId = $('#category-remove-targetlist').val();
            $.ajax({
                type: "DELETE",
                url: '<%= request.getContextPath()%>/tb-ui/authoring/rest/category/' + idToRemove
                    + '?targetCategoryId=' + targetCategoryId,
                success: function (data, textStatus, xhr) {
                    document.categoriesForm.submit();
                }
            });
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });

    $(".edit-link").click(function(e) {
      e.preventDefault();
      $('#category-edit').dialog('option', 'title', '<s:text name="generic.edit"/>')
      $('#category-edit-name').val($(this).attr("data-name")).select();
      $('#category-edit-error').css("display", "none");
      var dataId = $(this).attr("data-id");
      $.get('<%= request.getContextPath()%>/tb-ui/authoring/rest/categories/loggedin', function () {
         $('#category-edit').data('categoryId', dataId).dialog('open');
      });
    });

    $("#add-link").click(function(e) {
      e.preventDefault();
      $('#category-edit').dialog('option', 'title', '<s:text name="categoryForm.add.title"/>')
      $('#category-edit-name').val('');
      $('#category-edit-error').css("display", "none");
      $.get('<%= request.getContextPath()%>/tb-ui/authoring/rest/categories/loggedin', function () {
         $('#category-edit').data('categoryId',  '').dialog('open');
      });
    });

    $(".remove-link").click(function(e) {
      e.preventDefault();
      var idToRemove = $(this).attr("data-id");
        $.ajax({
            type: "GET",
            url: '<%= request.getContextPath()%>/tb-ui/authoring/rest/categories/inuse?categoryId=' + idToRemove,
            success: function (data, textStatus, xhr) {
                if (data == true) {
                    $('#category-remove-targetlist').empty()
                    $.ajax({
                        type: "GET",
                        url: '<%= request.getContextPath()%>/tb-ui/authoring/rest/categories?weblog=<s:text name="weblog"/>&skipCategoryId=' + idToRemove,
                        dataType: "json",
                        success: function (data, textStatus, xhr) {
                            $.each(data, function(i, d) {
                                $('#category-remove-targetlist').append('<option value="' + d.id + '">' + d.name + '</option>');
                            });
                        }
                    });
                    $('#category-remove-mustmove').css("display", "inline");
                } else {
                    $('#category-remove-mustmove').css("display", "none");
                }
                $('#category-remove').data('categoryId',  idToRemove).dialog('open');
            }
        });
    });
  });
</script>

<p class="subtitle">
    <s:text name="categoriesForm.subtitle" >
        <s:param value="weblog" />
    </s:text>
</p>
<p class="pagetip">
    <s:text name="categoriesForm.rootPrompt" />
</p>


<%-- Form is a table of categories each with checkbox --%>
<s:form id="categoriesForm" action="categories">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="categoryId" /> 

    <table class="rollertable">
        
        <tr class="rollertable">
            <th class="rollertable" width="25%"><s:text name="generic.name" /></th>
            <th class="rollertable" width="7%"><s:text name="generic.edit" /></th>
            <th class="rollertable" width="7%"><s:text name="categoriesForm.remove" /></th>
        </tr>
        
        <s:if test="AllCategories != null && !AllCategories.isEmpty">
        
        <%-- Categories --%>
        <s:iterator id="category" value="AllCategories" status="rowstatus">
            <s:if test="#rowstatus.odd == true">
                <tr class="rollertable_odd">
            </s:if>
            <s:else>
                <tr class="rollertable_even">
            </s:else>
            
                <td class="rollertable" id='catname-<s:property value="#category.id"/>'><s:property value="#category.name" /></td>
                
                <td class="rollertable" align="center">
                    <a href="#" class="edit-link" id='catid-<s:property value="#category.id"/>' data-name='<s:property value="#category.name"/>'
                        data-id='<s:property value="#category.id"/>'><img src='<s:url value="/images/page_white_edit.png"/>'
                        border="0" alt="icon" /></a>
                </td>
                
                <td class="rollertable" align="center">
                    <s:if test="AllCategories.size() > 1">
                        <a href="#" class="remove-link" id='cat-remove-id-<s:property value="#category.id"/>' data-id='<s:property value="#category.id"/>'
                           data-name='<s:property value="#category.name"/>'>
                            <img src='<s:url value="/images/delete.png"/>' border="0" alt="icon" />
                        <a>
                    </s:if>
                </td>
                
            </tr>
        </s:iterator>
        
        </s:if>
        <s:else>
            <tr>
                <td style="vertical-align:middle" colspan="6"><s:text name="categoriesForm.noresults" /></td>
            </tr>
        </s:else>
    </table>

    <div class="control clearfix">
        <s:url var="addCategory" action="categoryAdd">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
        </s:url>
        <input type="button" value="<s:text name='categoriesForm.addCategory'/>" id="add-link"/>
     </div>

</s:form>

<div id="category-edit" style="display:none">
    <span id="category-edit-error" style="display:none"><s:text name='categoryForm.error.duplicateName'/></span>
    <form>
      <label for="name"><s:text name='generic.name'/>:</label>
      <input type="text" id="category-edit-name" class="text ui-widget-content ui-corner-all">
    </form>
</div>

<div id="category-remove" title="<s:text name='categoryDeleteOK.removeCategory'/>" style="display:none">
    <form>
    <div id="category-remove-mustmove" style="display:none">
        <s:text name='categoryDeleteOK.youMustMoveEntries'/>
        <p>
          <s:text name="categoryDeleteOK.moveToWhere" />
          <select id="category-remove-targetlist"/>
        </p>
    </div>
    </form>
</div>
