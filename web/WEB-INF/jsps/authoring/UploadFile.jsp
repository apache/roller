<!--
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
-->
<%@ include file="/taglibs.jsp" %>
<%@ page import="org.apache.struts.util.RequestUtils" %>
<%@ page import="java.util.Hashtable" %>
<%@ page import="java.util.ResourceBundle" %>

<%
BasePageModel model = (BasePageModel)request.getAttribute("model");
ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");
%>

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
    if (!valid) window.alert('<fmt:message key="uploadFiles.noFilesSpecified" />');
    else document.forms[0].submit();
}
// -->
</script>

<p class="subtitle">
    <fmt:message key="uploadFiles.subtitle" >
        <fmt:param value="${model.website.handle}" />
    </fmt:message>
</p>  
<p class="pagetip">
    <fmt:message key="uploadFiles.tip" />
</p>
<p class="pagetip">
<fmt:message key="uploadFiles.quotaNote">
   <fmt:param value="${model.maxFileMB}" />
   <fmt:param value="${model.maxDirMB}" />
</fmt:message>
</p>

<%-- --------------------------
File upload form, but only if it's enabled and weblog is under quota
--%>

<c:choose>
    <c:when test="${!model.uploadEnabled}">
       <span class="error"><fmt:message key="uploadFiles.uploadDisabled" /></span>
    </c:when>
    <c:when test="${model.overQuota}">
       <span class="error"><fmt:message key="uploadFiles.exceededQuota" /></span>
       <br />
    </c:when>
    <c:otherwise>
         <% String edit = RequestUtils.computeURL( pageContext,
           "uploadFiles", null, null, null, new Hashtable(), null, false); %>
        <form name="uploadFiles" method="post" action="<%= edit %>" enctype="multipart/form-data">
            <br />
            
            <input type="file" name="uploadedFile0" id="uploadFile0" size="30" /><br />
              
            <div id="fileControl1" class="miscControl">
                <input type="file" name="uploadedFile1" id="uploadFile1" size="30" /><br />
            </div>                     
            
            <div id="fileControl2" class="miscControl">
                <input type="file" name="uploadedFile2" id="uploadFile2" size="30" /><br />
            </div>                     
            
            <div id="fileControl3" class="miscControl">
                <input type="file" name="uploadedFile3" id="uploadFile3" size="30" /><br />
            </div>                     
            
            <div id="fileControl4" class="miscControl">
                <input type="file" name="uploadedFile4" id="uploadFile4" size="30" /><br />
            </div>      

            <br />
            
            <input name="submitButton" type="button" onclick="validate()"
                value='<%= bundle.getString("uploadFiles.upload") %>' /> 
            <input type="hidden" name="method" value="upload" />
            <input type="hidden" name="weblog" value='<%= model.getWebsite().getHandle() %>'>
            <input type="hidden" name="path" value='<c:out value="${model.path}"/>'>
            <br />
            <br />
            
        </form>
    </c:otherwise>
</c:choose>
        

<h1><fmt:message key="uploadFiles.manageFiles" /></h1>

<%-- --------------------------
Create directory form
--%>
<c:if test="${model.showingRoot}">
<% String create = RequestUtils.computeURL( pageContext,
   "uploadFiles", null, null, null, new Hashtable(), null, false); %>
<form name="createSubdir" method="post" action="<%= create %>">
    <input type="hidden" name="method" value="createSubdir" />
    <input type="hidden" name="weblog" value='<c:out value="${model.website.handle}"/>'>
    <input type="hidden" name="path" value='<c:out value="${model.path}"/>'>
    
    <b><fmt:message key="uploadFiles.createDir" /></b> <input type="text" name="newDir" size="20" />&nbsp;
    <input type="submit" value='<fmt:message key="uploadFiles.createDirButton" />' /> 
    
    <br />
    <br />

</form>
</c:if>

<%-- --------------------------
Table of files, each with link, size and checkbox
--%>
<html:form action="/roller-ui/authoring/uploadFiles" method="post">
    <html:hidden property="path"/>
    
    <table class="rollertable">

        <tr class="rHeaderTr">
            <th class="rollertable" width="95%">Filename</th>
            <th class="rollertable">Size</td>
            <th class="rollertable">Delete</td>
        </tr>

        <c:if test="${!model.showingRoot}">
            <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">
                <td class="rollertable">
                    <c:url var="dirUrl" value="/roller-ui/authoring/uploadFiles.do">
                        <c:param name="path" value="${model.parentPath}" />
                    </c:url>
                    <img src='<c:url value="/images/folder.png"/>' style="padding:0px" />
                    <a href='<c:out value="${dirUrl}" />'>..</a>
                </td>
                <td class="rollertable" align="right">
                    &nbsp;
                </td>
                <td class="rollertable" align="center">
                    &nbsp;
                </td>
           </roller:row>
        </c:if>
        <c:forEach var="loopfile" items="${model.files}" >
           <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">
                <td class="rollertable">
                    <c:choose>
                        <c:when test="${loopfile.directory}">
                            <c:url var="dirUrl" value="/roller-ui/authoring/uploadFiles.do">
                                <c:param name="path" value="${loopfile.path}" />
                            </c:url>
                            <img src='<c:url value="/images/folder.png"/>' style="padding:0px" />
                            <a href='<c:out value="${dirUrl}" />'>
                                <c:out value="${loopfile.name}" />
                            </a>
                        </c:when>
                        <c:otherwise>
                            <img src='<c:url value="/images/image.png"/>' style="padding:0px" />
                            <a href='<c:out value="${model.resourcesBaseURL}" /><c:out value="${loopfile.path}" />'>
                                <c:out value="${loopfile.name}" />
                            </a>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="rollertable" align="right">
                    <fmt:formatNumber value="${loopfile.length / 1024}" type="number" maxFractionDigits="2" />&nbsp;KB
                </td>
                <td class="rollertable" align="center">
                   <input type="checkbox" name="deleteFiles" value='<c:out value="${loopfile.path}" />' />
                </td>
           </roller:row>

        </c:forEach>

       <tr>
           <td></td>
           <td></td>
           <td><fmt:formatNumber value="${model.totalSize / 1024}" type="number" maxFractionDigits="2" />&nbsp;KB</td>
       </tr>
    </table>
    
    <table>
       <tr>
          <td align="left">
             <input type="submit" value='<fmt:message key="uploadFiles.button.delete" />' /></input>
          </td>
       </tr>
    </table>
    
    <input type=hidden name="weblog" value='<c:out value="${model.website.handle}" />' />
    <input type="hidden" name="method" value="delete"></input>
    
</html:form>

<%--
Added by Matt Raible since the focus javascript generated by Struts uses
a name reference and IE seems to only focus on file inputs via elements[0]?
--%>
<script type="text/javascript">
<!--
    document.forms[0].elements[0].focus();
// -->
</script>




