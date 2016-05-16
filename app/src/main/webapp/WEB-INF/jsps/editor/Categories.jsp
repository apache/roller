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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>'/>
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="<s:url value='/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js'/>"></script>
<script>
    var contextPath = "${pageContext.request.contextPath}";
    var msg = {
        confirmLabel: '<s:text name="generic.confirm"/>',
        saveLabel: '<s:text name="generic.save"/>',
        cancelLabel: '<s:text name="generic.cancel"/>',
        editTitle: '<s:text name="generic.edit"/>',
        addTitle: '<s:text name="categoryForm.add.title"/>'
    };
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/categories.js'/>"></script>

<p class="subtitle">
    <s:text name="categoriesForm.subtitle">
        <s:param value="weblog"/>
    </s:text>
</p>
<p class="pagetip">
    <s:text name="categoriesForm.rootPrompt"/>
</p>

<%-- Form is a table of categories each with checkbox --%>
<s:form id="categoriesForm" action="categories">
    <sec:csrfInput/>
    <s:hidden id="actionWeblog" name="weblog"/>

    <table class="rollertable">

        <tr>
            <th width="25%"><s:text name="generic.name"/></th>
            <th width="7%"><s:text name="generic.edit"/></th>
            <th width="7%"><s:text name="categoriesForm.remove"/></th>
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

                <td id='catname-<s:property value="#category.id"/>'><s:property value="#category.name"/></td>

                <td align="center">
                    <a href="#" class="edit-link" id='catid-<s:property value="#category.id"/>' data-name='<s:property value="#category.name"/>' data-id='<s:property value="#category.id"/>'><img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="icon"/></a>
                </td>

                <td align="center">
                    <s:if test="AllCategories.size() > 1">
                        <a href="#" class="remove-link" id='cat-remove-id-<s:property value="#category.id"/>' data-id='<s:property value="#category.id"/>' data-name='<s:property value="#category.name"/>'>
                            <img src='<s:url value="/images/delete.png"/>' border="0" alt="icon"/>
                        <a>
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

      <div class="control clearfix">
          <input type="button" value="<s:text name='categoriesForm.addCategory'/>" id="add-link"/>
      </div>

</s:form>

    <div id="category-edit" style="display:none">
      <span id="category-edit-error" style="display:none"><s:text name='categoryForm.error.duplicateName'/></span>
      <label for="name"><s:text name='generic.name'/>:</label>
      <input type="text" id="category-edit-name" class="text ui-widget-content ui-corner-all">
    </div>

    <div id="category-remove" title="<s:text name='categoryDeleteOK.removeCategory'/>" style="display:none">
        <div id="category-remove-mustmove" style="display:none">
            <s:text name='categoryDeleteOK.youMustMoveEntries'/>
            <p>
                <s:text name="categoryDeleteOK.moveToWhere"/>
                <select id="category-remove-targetlist"/>
            </p>
        </div>
    </div>
