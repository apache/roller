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

<script type="text/javascript">
<!--
function validate() {
    valid = false;
    for (i=0; i<5; i++) {
       var field = document.getElementById("uploadFile" + i);
       if (!isblank(field.value)) {
          valid = true;
          break;
       }
    }
    if (!valid) window.alert('<s:text name="uploadFiles.noFilesSpecified" />');
    else document.forms[0].submit();
}
// -->
</script>

<p class="subtitle">
    <s:text name="uploadFiles.subtitle" >
        <s:param value="actionWeblog.handle" />
    </s:text>
</p>  
<p class="pagetip">
    <s:text name="uploadFiles.tip" />
</p>
<p class="pagetip">
    <s:text name="uploadFiles.quotaNote">
        <s:param value="getProp('uploads.file.maxsize')" />
        <s:param value="getProp('uploads.dir.maxsize')" />
    </s:text>
</p>

<%-- --------------------------
File upload form, but only if it's enabled and weblog is under quota
--%>

<s:if test="!getBooleanProp('uploads.enabled')">
    <span class="error"><s:text name="uploadFiles.uploadDisabled" /></span>
</s:if>
<s:elseif test="overQuota">
    <span class="error"><s:text name="uploadFiles.exceededQuota" /></span>
    <br />
</s:elseif>
<s:else>
    <s:form name="uploadFiles" action="resources!upload" method="POST" enctype="multipart/form-data">
        <s:hidden name="weblog" />
        <s:hidden name="path" />
        
        <br />
        
        <s:file name="uploadedFiles" size="30" /><br />
        
        <div id="fileControl1" class="miscControl">
            <s:file name="uploadedFiles" size="30" /><br />
        </div>                     
        
        <div id="fileControl2" class="miscControl">
            <s:file name="uploadedFiles" size="30" /><br />
        </div>                     
        
        <div id="fileControl3" class="miscControl">
            <s:file name="uploadedFiles" size="30" /><br />
        </div>                     
        
        <div id="fileControl4" class="miscControl">
            <s:file name="uploadedFiles" size="30" /><br />
        </div>      
        
        <br />
        
        <s:submit type="button" key="uploadFiles.upload" onclick="validate()" />
        <br />
        <br />
        
    </s:form>
</s:else>


<h1><s:text name="uploadFiles.manageFiles" /></h1>

<%-- --------------------------
Create directory form
--%>
<s:if test="path == null">
    <s:form name="createSubdir" action="resources!createSubdir">
        <s:hidden name="weblog" />
        <s:hidden name="path" />
        
        <b><s:text name="uploadFiles.createDir" /></b> <s:textfield name="newDir" size="20" />&nbsp;
        <s:submit key="uploadFiles.createDirButton" />
        
        <br />
        <br />
        
    </s:form>
</s:if>


<%-- --------------------------
Table of files, each with link, size and checkbox
--%>
<s:form action="resources!remove">
    <s:hidden name="weblog" />
    <s:hidden name="path"/>
    
    <table class="rollertable">

        <tr class="rHeaderTr">
            <th class="rollertable" width="95%">Filename</th>
            <th class="rollertable">Size</td>
            <th class="rollertable">Delete</td>
        </tr>
        
        <s:if test="path != null">
            <tr class="rollertable_even">
                <td class="rollertable">
                    <s:url id="dirUrl" action="resources">
                        <s:param name="weblog" value="actionWeblog.handle" />
                    </s:url>
                    <img src='<c:url value="/images/folder.png"/>' style="padding:0px" />
                    <s:a href="%{dirUrl}">..</s:a>
                </td>
                <td class="rollertable" align="right">
                    &nbsp;
                </td>
                <td class="rollertable" align="center">
                    &nbsp;
                </td>
           </tr>
        </s:if>
        <s:iterator id="file" value="files" status="rowstatus">
            
            <s:if test="#rowstatus.odd == true">
                <tr class="rollertable_odd">
            </s:if>
            <s:else>
                <tr class="rollertable_even">
            </s:else>
            <td class="rollertable">
                <s:if test="#file.directory">
                    <s:url id="dirUrl" action="resources">
                        <s:param name="weblog" />
                        <s:param name="path" value="#file.path" />
                    </s:url>
                    <img src='<c:url value="/images/folder.png"/>' style="padding:0px" />
                    <s:a href="%{dirUrl}"><s:property value="#file.name" /></s:a>
                </s:if>
                <s:else>
                    <img src='<s:url value="/images/image.png"/>' style="padding:0px" />
                    <a href='<c:out value="${model.resourcesBaseURL}" /><s:property value="#file.path" />'>
                        <s:property value="#file.name" />
                    </a>
                </s:else>
            </td>
            <td class="rollertable" align="right">
                <fmt:formatNumber value="${ffile.length / 1024}" type="number" maxFractionDigits="2" />&nbsp;KB
            </td>
            <td class="rollertable" align="center">
                <input type="checkbox" name="deleteIds" value="<s:property value="#file.path" />" />
            </td>
            </tr>
            
        </s:iterator>

       <tr>
           <td></td>
           <td></td>
           <td><fmt:formatNumber value="${model.totalSize / 1024}" type="number" maxFractionDigits="2" />&nbsp;KB</td>
       </tr>
    </table>
    
    <table>
       <tr>
          <td align="left">
             <s:submit key="uploadFiles.button.delete" />
          </td>
       </tr>
    </table>
    
</s:form>

<%--
Added by Matt Raible since the focus javascript generated by Struts uses
a name reference and IE seems to only focus on file inputs via elements[0]?
--%>
<script type="text/javascript">
<!--
    document.forms[0].elements[0].focus();
// -->
</script>
