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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page import="org.apache.roller.ui.authoring.struts.actions.CommentManagementAction" %>
<%
CommentManagementAction.CommentManagementPageModel model = 
    (CommentManagementAction.CommentManagementPageModel)request.getAttribute("model");
%>
<script type="text/javascript">
<!-- 
function setChecked(val, name) {
    len = document.commentManagementForm.elements.length;
    var i=0;
    for( i=0 ; i<len ; i++) {
        if (document.commentManagementForm.elements[i].name == name) { 
           document.commentManagementForm.elements[i].checked=val;
        }
    }
}
function bulkDelete() {
    if (window.confirm('<fmt:message key="commentManagement.confirmBulkDelete"><fmt:param value="${model.totalMatchingCommentCount}" /></fmt:message>')) {
        document.commentQueryForm.method.value = "bulkDelete";
        document.commentQueryForm.submit();
    }
}

function createRequestObject() {
    var ro;
    var browser = navigator.appName;
    if (browser == "Microsoft Internet Explorer") {
        ro = new ActiveXObject("Microsoft.XMLHTTP");
    } else {
        ro = new XMLHttpRequest();
    }
    return ro;
}
var http = createRequestObject();
var init = false;
var isBusy = false;

function readMoreComment(id) {
    url = "<%= request.getContextPath() %>" + "/roller-ui/authoring/commentdata?id="; + id;
    if (isBusy) return;
    isBusy = true;
    http.open('get', url);
    http.onreadystatechange = handleCommentResponse;
    http.send(null);
}

function handleCommentResponse() {
    if (http.readyState == 4) {
        comment = eval("(" + http.responseText + ")"); 
        commentDiv = document.getElementById("comment-" + comment.id);
        commentDiv.textContent = comment.content;
        linkDiv = document.getElementById("link-" + comment.id);
        linkDiv.parentNode.removeChild(linkDiv);
    }
    isBusy = false;
}
-->
</script>


<%-- ===================================================================== --%>
<%-- Subtitle --%>
<%-- ===================================================================== --%>

<c:choose>
    <%-- Managing comments for one specific weblog entry --%>
    <c:when test="${!empty model.weblogEntry}">
        <p class="subtitle">
            <fmt:message key="commentManagement.entry.subtitle" >
                <fmt:param value="${model.weblogEntry.title}" />
            </fmt:message>
        </p>
        <p>           
            <c:url value="/roller-ui/authoring/weblog.do" var="entryLink">
               <c:param name="method" value="edit" />
               <c:param name="weblog" value="${model.website.handle}" />
               <c:param name="entryId" value="${model.weblogEntry.id}" />
            </c:url>
            <a href='<c:out value="${entryLink}" />'>
                <fmt:message key="commentManagement.returnToEntry"/>
            </a>
        </p>
    </c:when>        
    <%-- Managing comments for one specific weblog --%>
    <c:when test="${!empty model.website}">
        <p class="subtitle">
            <fmt:message key="commentManagement.website.subtitle" >
                <fmt:param value="${model.website.handle}" />
            </fmt:message>
        </p>
    </c:when>
    <c:otherwise>
        <p class="subtitle">
            <fmt:message key="commentManagement.subtitle" />
        </p>
    </c:otherwise>
</c:choose>


<%-- ===================================================================== --%>
<%-- Tip --%>
<%-- ===================================================================== --%>

<c:choose>
    <c:when test="${model.pendingCommentCount > 0}">
        <p class="pagetip"><fmt:message key="commentManagement.pendingTip" /></p>    
    </c:when>
    <c:when test="${!empty model.website}">
        <p class="pagetip"><fmt:message key="commentManagement.tip" /></p>    
    </c:when>
    <c:otherwise>
        <p class="pagetip"><fmt:message key="commentManagement.globalTip" /></p>    
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${!empty model.comments}">
   
        <%-- ============================================================= --%>
        <%-- Number of comments and date message --%>
        <%-- ============================================================= --%>
        
        <div class="tablenav">
            
        <div style="float:left;">
            <fmt:message key="commentManagement.nowShowing">
                <fmt:param value="${model.commentCount}" />
            </fmt:message>
        </div>
        <div style="float:right;">
            <fmt:formatDate value="${model.latestDate}" type="both" 
                dateStyle="short" timeStyle="short" />
            --- 
            <fmt:formatDate value="${model.earliestDate}" type="both" 
                dateStyle="short" timeStyle="short" />
        </div>
        <br />
        
        
        <%-- ============================================================= --%>
        <%-- Next / previous links --%>
        <%-- ============================================================= --%>
        
        <c:choose>
            <c:when test="${!empty model.prevLink && !empty model.nextLink}">
                <br /><center>
                    &laquo;
                    <a href='<c:out value="${model.prevLink}" />'>
                        <fmt:message key="commentManagement.prev" /></a>
                    | <a href='<c:out value="${model.nextLink}" />'>
                        <fmt:message key="commentManagement.next" /></a>
                    &raquo;
                </center><br />
            </c:when>
            <c:when test="${!empty model.prevLink}">
                <br /><center>
                    &laquo;
                    <a href='<c:out value="${model.prevLink}" />'>
                        <fmt:message key="commentManagement.prev" /></a>
                    | <fmt:message key="commentManagement.next" />
                    &raquo;
                </center><br />
            </c:when>
            <c:when test="${!empty model.nextLink}">
                <br /><center>
                    &laquo;
                    <fmt:message key="commentManagement.prev" />
                    | <a class="" href='<c:out value="${model.nextLink}" />'>
                        <fmt:message key="commentManagement.next" /></a>
                    &raquo;
                </center><br />
            </c:when>
            <c:otherwise><br /></c:otherwise>
        </c:choose>
        
        </div> <%-- class="tablenav" --%>

        
        <%-- ============================================================= --%>
        <%-- Bulk comment delete link --%>
        <%-- ============================================================= --%>
        
        <c:if test="${model.showBulkDeleteLink}">
            <p>
            <fmt:message key="commentManagement.bulkDeletePrompt1">
                <fmt:param value="${model.totalMatchingCommentCount}" />
            </fmt:message>
            <a href="#" onclick="bulkDelete()">
                <fmt:message key="commentManagement.bulkDeletePrompt2" />
            </a>
            </p>
        </c:if>
        
        <%-- ============================================================= --%>
        <%-- Comment table / form with checkboxes --%>
        <%-- ============================================================= --%>
        
        <% String path = model.getWebsite()==null 
                ? "/roller-ui/admin/commentManagement" : "/roller-ui/authoring/commentManagement"; %>
        <html:form action="<%= path %>" method="post">
        
            <input type="hidden" name="method" value="update"/>
            <c:if test="${!empty model.website}">
                <input name="weblog" type="hidden" 
                    value='<c:out value="${model.website.handle}" />' />
            </c:if>
            <c:if test="${!empty model.weblogEntry}">
                <input name="entryId" type="hidden" 
                    value='<c:out value="${model.weblogEntry.id}" />' />
            </c:if>
            <html:hidden property="ids" />
            <html:hidden property="offset" />
            <html:hidden property="count" />
            <html:hidden property="startDateString" />
            <html:hidden property="endDateString" />
            <html:hidden property="pendingString" />
            <html:hidden property="spamString" />

        <table class="rollertable" width="100%">
            
           <%-- ======================================================== --%>
           <%-- Comment table header --%>
           
           <tr>
                <c:if test="${!empty model.website}">
                    <th class="rollertable" width="5%">
                        <fmt:message key="commentManagement.columnApproved" />
                    </th>
                </c:if>
                <th class="rollertable" width="5%">
                    <fmt:message key="commentManagement.columnSpam" />
                </th>
                <th class="rollertable" width="5%" >
                    <fmt:message key="commentManagement.columnDelete" />
                </th>
                <th class="rollertable">
                    <fmt:message key="commentManagement.columnComment" />
                </th>
            </tr>
            
           <%-- ======================================================== --%>
           <%-- Select ALL and NONE buttons --%>
           
            <c:if test="${model.commentCount > 1}">
                <tr class="actionrow">
                    <c:if test="${!empty model.website}">
                        <td align="center">
                            <fmt:message key="commentManagement.select" /><br/>

                            <a href="#" onclick='setChecked(1,"approvedComments")'>
                                <fmt:message key="commentManagement.all" /></a><br /> 

                            <a href="#" onclick='setChecked(0,"approvedComments")'>
                                <fmt:message key="commentManagement.none" /></a>
                        </td>
                    </c:if>
                    <td align="center">
                        <fmt:message key="commentManagement.select" /><br/>

                        <a href="#" onclick='setChecked(1,"spamComments")'>
                            <fmt:message key="commentManagement.all" /></a><br />  

                        <a href="#" onclick='setChecked(0,"spamComments")'>
                            <fmt:message key="commentManagement.none" /></a>
                    </td>
                    <td align="center">
                        <fmt:message key="commentManagement.select" /><br/>

                        <a href="#" onclick='setChecked(1,"deleteComments")'>
                            <fmt:message key="commentManagement.all" /></a><br /> 

                        <a href="#" onclick='setChecked(0,"deleteComments")'>
                            <fmt:message key="commentManagement.none" /></a>
                    </td>
                    <td align="right">
                        <br />
                        <span class="pendingCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
                        <fmt:message key="commentManagement.pending" />&nbsp;&nbsp;
                        <span class="spamCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span> 
                        <fmt:message key="commentManagement.spam" />&nbsp;&nbsp;
                    </td>
                </tr>
            </c:if>
            
            <%-- ========================================================= --%>
            <%-- Loop through comments --%>
            <%-- ========================================================= --%>

            <c:forEach var="comment" items="${model.comments}">
            <tr>
                <c:choose>
                    <c:when test="${!empty model.website}">
                        <td>
                            <html:multibox property="approvedComments">
                                <c:out value="${comment.id}" />
                            </html:multibox>
                        </td>
                    </c:when>
                    <c:otherwise>
                        <html:hidden property="approvedComments"/>
                    </c:otherwise>
                </c:choose>
                <td>
                    <html:multibox property="spamComments">
                        <c:out value="${comment.id}" />
                    </html:multibox>
                </td>
                <td>
                    <html:multibox property="deleteComments">
                            <c:out value="${comment.id}" />
                    </html:multibox>
                </td>
                
                <%-- ======================================================== --%>
                <%-- Display comment details and text --%>
           
                <%-- <td> with style if comment is spam or pending --%>               
                <c:choose>
                    <c:when test="${comment.spam}">
                        <td class="spamcomment"> 
                    </c:when>
                    <c:when test="${comment.pending}">
                        <td class="pendingcomment"> 
                    </c:when>
                    <c:otherwise>
                        <td>
                    </c:otherwise>
                </c:choose>
                                    
                    <%-- comment details table in table --%>
                    <table style="border:none; padding:0px; margin:0px">                         
                    <tr>
                        <td style="border: none; padding:0px;">
                            <fmt:message key="commentManagement.entryTitled" /></td>
                        <td class="details" style="border: none; padding:0px;">  
                            <a href='<c:out value="${comment.weblogEntry.permalink}" />'>
                               <c:out value="${comment.weblogEntry.title}" /></a>
                        </td>
                    </tr>  
                      
                    <tr>
                        <td style="border: none; padding:0px;">
                            <fmt:message key="commentManagement.commentBy" /></td>
                        <td class="details" style="border: none; padding:0px;">
                            <c:choose>
                                <c:when test="${!empty comment.email && !empty comment.name}">
                                    <fmt:message key="commentManagement.commentByBoth" >
                                        <fmt:param value="${comment.name}" />
                                        <fmt:param value="${comment.email}" />
                                        <fmt:param value="mailto" />
                                        <fmt:param value="${comment.remoteHost}" />
                                     </fmt:message>
                                </c:when>
                                <c:when test="${!empty comment.name}">
                                    <fmt:message key="commentManagement.commentByName" >
                                        <fmt:param value="${comment.name}" />
                                        <fmt:param value="${comment.remoteHost}" />
                                     </fmt:message>
                                </c:when>
                                <c:when test="${!empty comment.email}">
                                    <fmt:message key="commentManagement.commentByName" >
                                        <fmt:param value="${comment.email}" />
                                        <fmt:param value="${comment.remoteHost}" />
                                     </fmt:message>
                                </c:when>
                                <c:otherwise>
                                    <fmt:message key="commentManagement.commentByIP" >
                                        <fmt:param value="${comment.remoteHost}" />
                                     </fmt:message>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${!empty comment.url}">
                                <br /><a href='<c:out value="${comment.url}" />'>
                                <str:truncateNicely upper="60" appendToEnd="..."><c:out value="${comment.url}" /></str:truncateNicely></a>
                            </c:if>
                        </td>
                    </tr>                                
                    <tr>
                        <td style="border: none; padding:0px;">
                            <fmt:message key="commentManagement.postTime" /></td>
                        <td class="details" style="border: none; padding:0px;">
                            <c:out value="${comment.postTime}" /></td>
                    </tr>                                       
                    </table> <%-- end comment details table in table --%>
                
                    <%-- comment content --%>
                    <br />
                    <span class="details">
                                                
                        <c:choose>
                            <c:when test="${fn:length(comment.content) > 1000}">
                                <pre><div id="comment-<c:out value="${comment.id}"/>"><str:wordWrap width="72"><str:truncateNicely upper="1000" appendToEnd="..."><c:out value="${comment.content}" escapeXml="true" /></str:truncateNicely></str:wordWrap></div></pre>                                    
                                <div id="link-<c:out value="${comment.id}"/>">
                                    <a onclick='readMoreComment("<c:out value="${comment.id}"/>")'>
                                        <fmt:message key="commentManagement.readmore" />
                                    </a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <pre><str:wordWrap><c:out value="${comment.content}" escapeXml="true" /></str:wordWrap></pre>   
                            </c:otherwise>
                        </c:choose> 
                       
                    </span>
                    
                </td>
            </tr>
            </c:forEach>
        </table>
        <br />

        <%-- ========================================================= --%>
        <%-- Save changes and  cancel buttons --%>
        <%-- ========================================================= --%>
            
        <input type="submit" name="submitButton" 
            value='<fmt:message key="commentManagement.update" />' />
        &nbsp;
        
        <c:choose>
            <c:when test="${!empty model.weblogEntry}">
                <input type="button" name="Cancel" value='<fmt:message key="application.cancel" />' 
                    onclick="window.location.href='<c:out value="${entryLink}" />'" />
            </c:when>
            <c:otherwise>
                <input type="button" name="Cancel" value='<fmt:message key="application.cancel" />' 
                    onclick="window.location.href='<c:out value="${model.link}" />'" />
            </c:otherwise>
        </c:choose>  

        </html:form>

    </c:when>
    
    <c:otherwise>
        <fmt:message key="commentManagement.noCommentsFound" />
    </c:otherwise>
    
</c:choose>

