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
        
<script>
// <!--  
function save() {
    radios = document.getElementsByTagName("input");
    var removing = false;
    for (var i=0; i<radios.length; i++) {
        if (radios[i].value == -1 && radios[i].checked) {
            removing = true;
        }
    }
    if (removing && !confirm("<s:text name='memberPermissions.confirmRemove' />")) return;
    document.memberPermissionsForm.submit();
}
// -->
</script>

<p class="subtitle">
    <s:text name="memberPermissions.subtitle" >
        <s:param value="actionWeblog.handle" />
    </s:text>
</p>

<p><s:text name="memberPermissions.description" /></p>

<s:form action="members!save">
	<s:hidden name="salt" />
    <s:hidden name="weblog" value="%{actionWeblog.handle}" />
    
    <div style="text-align: right; padding-bottom: 6px;">
        <span class="pendingCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
            <s:text name="commentManagement.pending" />&nbsp;
    </div>
    
    <table class="rollertable">
        <tr class="rHeaderTr">
           <th class="rollertable" width="20%">
               <s:text name="memberPermissions.userName" />
           </th>
           <th class="rollertable" width="20%">
               <s:text name="memberPermissions.administrator" />
           </th>
           <th class="rollertable" width="20%">
               <s:text name="memberPermissions.author" />
           </th>
           <th class="rollertable" width="20%">
               <s:text name="memberPermissions.limited" />
           </th>
           <th class="rollertable" width="20%">
               <s:text name="memberPermissions.remove" />
           </th>
        </tr>
        <s:iterator id="perm" value="weblogPermissions" status="rowstatus">
            <s:if test="#perm.pending">
                <tr class="rollertable_pending">
            </s:if>
            <s:elseif test="#rowstatus.odd == true">
                <tr class="rollertable_odd">
            </s:elseif>
            <s:else>
                <tr class="rollertable_even">
            </s:else>
            
                <td class="rollertable">
                    <img src='<s:url value="/images/user.png"/>' border="0" alt="icon" />
	                <s:property value="#perm.user.userName" />
                </td>               
                <td class="rollertable">
                    <input type="radio" 
                        <s:if test='#perm.hasAction("admin")'>checked</s:if>
                        name='perm-<s:property value="#perm.user.id" />' value="admin" />
                </td>
                <td class="rollertable">
	                <input type="radio" 
                        <s:if test='#perm.hasAction("post")'>checked</s:if>
                        name='perm-<s:property value="#perm.user.id" />' value="post" />
                </td>                
                <td class="rollertable">
                    <input type="radio" 
                        <s:if test='#perm.hasAction("edit_draft")'>checked</s:if>
                        name='perm-<s:property value="#perm.user.id" />' value="edit_draft" />
                </td>                
                <td class="rollertable">
                    <input type="radio" 
                        name='perm-<s:property value="#perm.user.id" />' value="-1" />
                </td>
           </tr>
       </s:iterator>
    </table>
    <br />
     
    <div class="control">
       <s:submit value="%{getText('generic.save')}" />
    </div>
    
</s:form>
