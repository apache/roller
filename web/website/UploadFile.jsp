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
var max = 9;
var count = 0;
function toggleMore(targetId) {
    if (document.getElementById) {
        ++count;
        var id = targetId + count;
        target = document.getElementById(id);
        target.style.display = "inline";
        if (count == max) {
           target = document.getElementById("moreToggle");
           target.style.display = "none";
        }
        target = document.getElementById("lessToggle");
        target.style.display = "inline";
    }
}
function toggleLess(targetId) {
    if (document.getElementById) {
        var id = targetId + count;
        target = document.getElementById(id);
        target.style.display = "none";
        field = document.getElementById("uploadFile" + count);
        field.value = "";
        count--;
        if (count == 0) {
           target = document.getElementById("lessToggle");
           target.style.display = "none";
        }
        target = document.getElementById("moreToggle");
        target.style.display = "inline";
   } 
   fileChanged();
}
<%-- 
Would be nice to disable submit button until a file has been
chosen, but Netscape7 won't fire fileupload's onselected event.
leaving this commented out for the day we drop support or Netscape7
--%>
function fileChanged() {
//   disabled = true;
//   for (i=0; i<=9; i++) {
//      field = document.getElementById("uploadFile" + i);
//      if (!isblank(field.value)) {
//         disabled = false;
//         break;
//      }
//   }
//   document.forms[0].submitButton.disabled = disabled;
}
-->
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
            
            <input type="file" name="uploadedFile0" id="uploadFile0" size="30"     onselect="fileChanged()" onkeyup="fileChanged()" value="" /><br />
              
            <div id="fileControl1" class="miscControl" style="display:none">
                <input type="file" name="uploadedFile1" id="uploadFile1" size="30" onselect="fileChanged()" onkeyup="fileChanged()" /><br />
            </div>                     
            
            <div id="fileControl2" class="miscControl" style="display:none">
                <input type="file" name="uploadedFile2" id="uploadFile2" size="30" onselect="fileChanged()" onkeyup="fileChanged()" /><br />
            </div>                     
            
            <div id="fileControl3" class="miscControl" style="display:none">
                <input type="file" name="uploadedFile3" id="uploadFile3" size="30" onselect="fileChanged()" onkeyup="fileChanged()" /><br />
            </div>                     
            
            <div id="fileControl4" class="miscControl" style="display:none">
                <input type="file" name="uploadedFile4" id="uploadFile4" size="30" onselect="fileChanged()" onkeyup="fileChanged()" /><br />
            </div>      
               
            <div id="fileControl5" class="miscControl" style="display:none">
                <input type="file" name="uploadedFile5" id="uploadFile5" size="30" onselect="fileChanged()" onkeyup="fileChanged()" /><br />
            </div>      
               
            <div id="fileControl6" class="miscControl" style="display:none">
                <input type="file" name="uploadedFile6" id="uploadFile6" size="30" onselect="fileChanged()" onkeyup="fileChanged()" /><br />
            </div>      
               
            <div id="fileControl7" class="miscControl" style="display:none">
                <input type="file" name="uploadedFile7" id="uploadFile7" size="30" onselect="fileChanged()" onkeyup="fileChanged()" /><br />
            </div>      
               
            <div id="fileControl8" class="miscControl" style="display:none">
                <input type="file" name="uploadedFile8" id="uploadFile8" size="30" onselect="fileChanged()" onkeyup="fileChanged()" /><br />
            </div>      
               
            <div id="fileControl9" class="miscControl" style="display:none">
                <input type="file" name="uploadedFile9" id="uploadFile9" size="30" onselect="fileChanged()" onkeyup="fileChanged()" /><br />
            </div>          
               
            <div id="lessToggle" style="display:none; float:left;">
                <a onclick="javascript:toggleLess('fileControl')">
                   <img src='<c:url value="/images/delete.png"/>' style="padding:4px" title="Remove last from upload list" />
                </a>
            </div>
            <div id="moreToggle" style="display:inline; float:left">
                <a onclick="javascript:toggleMore('fileControl')">
                    <img src='<c:url value="/images/add.png"/>' style="padding:4px" title="Add file to upload list" />
                </a>
            </div>
         
            <br />
            <br />
            
            <%-- 
            Would be nice to disable submit button until a file has been
            chosen, but Netscape7 won't fire fileupload's onselected event,
            so here we have the submit button enabled from the start
            --%>
            <input name="submitButton" type="submit" value='<%= bundle.getString("uploadFiles.upload") %>' /> 
            <input type="hidden" name="method" value="upload" />
            <input type="hidden" name="weblog" value='<%= model.getWebsite().getHandle() %>'>
            <br />
            <br />
            
        </form>
    </c:otherwise>
</c:choose>

<%-- --------------------------
Table of files, each with link, size and checkbox
--%>

<h1><fmt:message key="uploadFiles.manageFiles" /></h1>    
<html:form action="/editor/uploadFiles" method="post">

    <table class="rollertable">

        <tr class="rHeaderTr">
            <th class="rollertable" width="95%">Filename</th>
            <th class="rollertable">Size</td>
            <th class="rollertable">Delete</td>
        </tr>

        <c:forEach var="loopfile" items="${model.files}" >
           <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">
                <td class="rollertable">
                    <img src='<c:url value="/images/image.png"/>' style="padding:0px" />
                    <a href='<c:out value="${model.resourcesBaseURL}" />/<c:out value="${loopfile.name}" />'>
                        <c:out value="${loopfile.name}" />
                    </a>
                </td>
                <td class="rollertable" align="right">
                    <fmt:formatNumber value="${loopfile.length / 1024}" type="number" maxFractionDigits="2" />&nbsp;KB
                </td>
                <td class="rollertable" align="center">
                   <input type="checkbox" name="deleteFiles" value='<c:out value="${loopfile.name}" />' />
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




