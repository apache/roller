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
<script type="text/javascript">
<!-- 
function setChecked(val) 
{
    len = document.categories.elements.length;
    var i=0;
    for( i=0 ; i<len ; i++) 
    {
        document.categories.elements[i].checked=val;
    }
}
function onMove() 
{
    if ( confirm("<s:text name='categoriesForm.move.confirm' />") ) 
    {
        document.categories.method.value = "moveSelected";
        document.categories.submit();
    }
}
//-->
</script>

<s:if test="categoryPath.isEmpty">
    <p class="subtitle">
        <s:text name="categoriesForm.subtitle" >
            <s:param value="weblog" />
        </s:text>
    </p>  
    <p class="pagetip">
        <s:text name="categoriesForm.rootPrompt" />
    </p>
</s:if>

<s:else>
    <p class="subtitle">
    <s:text name="categoriesForm.path" />: /
    <s:iterator id="pathItem" value="categoryPath">
        <s:url id="pathUrl" action="categories">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
            <s:param name="categoryId" value="#pathItem.id" />
        </s:url>
        <s:a href="%{pathUrl}"><s:property value="#pathItem.name" /></s:a> / 
    </s:iterator>
    <p>
    <p><s:text name="categoriesForm.categoryPrompt" /></p>
</s:else>


<%-- Form is a table of categories each with checkbox --%>
<s:form action="categories!move">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="categoryId" /> 
    
    <%-- Select-all button --%>
    <input type="button" value="<s:text name='categoriesForm.checkAll' />" onclick="setChecked(1)" /></input>
    
    <%-- Select-none button --%>
    <input type="button" value="<s:text name='categoriesForm.checkNone' />" onclick="setChecked(0)" /></input>
    
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    
    <%-- Move-selected button --%>
    <s:submit value="%{getText('categoriesForm.move')}" onclick="onMove()" />
    
    <%-- Move-to combo-box --%>
    <s:select name="targetCategoryId" list="allCategories" listKey="id" listValue="path" />
    
    <p />
    
    <table class="rollertable">
        
        <tr class="rollertable">
            <th class="rollertable" width="5%">&nbsp;</td>
            <th class="rollertable" width="5%">&nbsp;</td>
            <th class="rollertable" width="30%"><s:text name="categoriesForm.name" /></td>
            <th class="rollertable" width="45%"><s:text name="categoriesForm.description" /></td>
            <th class="rollertable" width="5%"><s:text name="categoriesForm.edit" /></td>
            <th class="rollertable" width="5%"><s:text name="categoriesForm.remove" /></td>
        </tr>
        
        <%-- Categories --%>
        <s:iterator id="category" value="category.weblogCategories" status="rowstatus">
            <s:if test="#rowstatus.odd == true">
                <tr class="rollertable_odd">
            </s:if>
            <s:else>
                <tr class="rollertable_even">
            </s:else>
            
                <td class="rollertable">
                    <input type="checkbox" name="selectedCategories" value="<s:property value="#category.id"/>" />
                </td>
                
                <td class="rollertable" align="center"><img src='<s:url value="/images/folder.png"/>' alt="icon" /></td>
                
                <td class="rollertable">
                    <s:url id="categoryUrl" action="categories">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                        <s:param name="categoryId" value="#category.id" />
                    </s:url>
                    <s:a href="%{categoryUrl}"><str:truncateNicely lower="15" upper="20" ><s:property value="#category.name" /></str:truncateNicely></s:a>
                </td>
                
                <td class="rollertable">
                    <str:truncateNicely lower="30" upper="35" ><s:property value="#category.description" /></str:truncateNicely>
                </td>
                
                <td class="rollertable" align="center">
                    <s:url id="editUrl" action="categoryEdit">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                        <s:param name="bean.id" value="#category.id" />
                    </s:url>
                    <s:a href="%{editUrl}"><img src='<s:url value="/images/page_white_edit.png"/>' border="0" alt="icon" /></s:a>
                </td>
                
                <td class="rollertable" align="center">
                    <s:url id="removeUrl" action="categoryRemove">
                        <s:param name="weblog" value="%{actionWeblog.handle}" />
                        <s:param name="removeId" value="#category.id" />
                    </s:url>
                    <s:a href="%{removeUrl}"><img src='<s:url value="/images/delete.png"/>' border="0" alt="icon" /></s:a>
                </td>
                
            </tr>
        </s:iterator>
        
    </table>
    
</s:form>
