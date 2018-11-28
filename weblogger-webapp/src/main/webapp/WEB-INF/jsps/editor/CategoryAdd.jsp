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
    <s:text name="categoryForm.add.subtitle" />
</p>

<p>
    <b><s:text name="categoriesForm.path" /></b>:<s:property value="category.path" />
</p>

<s:form action="categoryAdd!save">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="categoryId" />
    
    <table>
        
        <tr>
            <td><s:text name="categoryForm.name" /></td>
            <td><s:textfield name="bean.name" size="70" maxlength="255" /></td>
        </tr>
        
        <tr>
            <td><s:text name="categoryForm.description" /></td>
            <td><s:textarea name="bean.description" rows="5" cols="50" /></td>
        </tr>
        
        <tr>
            <td><s:text name="categoryForm.image" /></td>
            <td><s:textarea name="bean.image" rows="5" cols="50" /></td>
        </tr>
        
    </table>
    
    <p>
        <s:submit value="%{getText('categoryForm.save')}" />
        <s:submit value="%{getText('categoryForm.cancel')}" action="categories" />
    </p>
    
</s:form>
