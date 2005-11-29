<%@ include file="/taglibs.jsp" %>
<%@ page import="org.roller.presentation.weblog.actions.CommentManagementAction" %>
<%@ page import="org.roller.presentation.RollerRequest" %>
<%
//CommentManagementAction.CommentManagementPageModel model = 
    //(CommentManagementAction.CommentManagementPageModel)request.getAttribute("model");
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
-->
</script>

<p class="subtitle">
    <c:choose>
        <%-- Managing comments for one specific weblog entry --%>
        <c:when test="${!empty model.weblogEntry}">
            <fmt:message key="commentManagement.entry.subtitle" >
                <fmt:param value="${model.weblogEntry.title}" />
            </fmt:message>
            <p>
            <a href='<c:out value="${model.baseURL}" /><c:out value="${model.weblogEntry.permaLink}" />'
                class="entrypermalink" title="entry permalink">Return to entry</a>
            </p>
        </c:when>        
        <%-- Managing comments for one specific weblog --%>
        <c:when test="${!empty model.website}">
            <fmt:message key="commentManagement.website.subtitle" >
                <fmt:param value="${model.website.handle}" />
            </fmt:message>
        </c:when>
        <c:otherwise>
            <fmt:message key="commentManagement.subtitle" />
        </c:otherwise>
    </c:choose>
</p>

<p class="pagetip"><fmt:message key="commentManagement.tip" /></p>

<c:choose>
    <c:when test="${!empty model.comments}">
    
        <p class="pagetip"><center>
        <fmt:message key="commentManagement.nowShowing">
            <fmt:param value="${model.commentCount}" />
            <fmt:param value="${model.earliestDate}" />
            <fmt:param value="${model.latestDate}" />
        </fmt:message>
        </center></p>

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
                    | <a href='<c:out value="${model.nextLink}" />'>
                        <fmt:message key="commentManagement.next" /></a>
                    &raquo;
                </center><br />
            </c:when>
            <c:otherwise><br /></c:otherwise>
        </c:choose>

        <html:form action="/editor/commentManagement" method="post">
            <input type="hidden" name="method" value="update"/>
            <html:hidden property="weblog" />
            <html:hidden property="entryid" />
            <html:hidden property="ids" />
            <html:hidden property="offset" />
            <html:hidden property="count" />
            <html:hidden property="startDateString" />
            <html:hidden property="endDateString" />
            <html:hidden property="pendingString" />
            <html:hidden property="spamString" />

        <table class="rollertable" width="100%">
            
            <tr>
                <th class="rollertable" width="5%" style="font-size:80%">
                    <fmt:message key="commentManagement.columnApproved" />
                </th>
                <th class="rollertable" width="5%" style="font-size:80%">
                    <fmt:message key="commentManagement.columnSpam" />
                </th>
                <th class="rollertable" width="5%" style="font-size:80%">
                    <fmt:message key="commentManagement.columnDelete" />
                </th>
                <th class="rollertable" width="85%">
                    <fmt:message key="commentManagement.columnComment" />
                </th>
            </tr>
            
            <c:if test="${model.commentCount > 1}">
                <tr class="actionrow">
                    <td align="center">
                        <fmt:message key="commentManagement.select" /><br/>
                        <a href="#" onclick='setChecked(1,"approvedComments")'>
                            <fmt:message key="commentManagement.all" /></a><br /> 
                        <a href="#" onclick='setChecked(0,"approvedComments")'>
                            <fmt:message key="commentManagement.none" /></a>
                    </td>
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
                    <td>
                        &nbsp;
                    </td>
                </tr>
            </c:if>
            
            <c:forEach var="comment" items="${model.comments}">
            <tr>
                <td>
                    <html:multibox property="approvedComments">
                        <c:out value="${comment.id}" />
                    </html:multibox>
                </td>
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
                
                <%-- <td> with style if comment is pending --%>
                <c:choose>
                    <c:when test="${comment.pending}">
                        <td class="pendingcomment" style="background:#fffcc"> 
                    </c:when>
                    <c:otherwise>
                        <td>
                    </c:otherwise>
                </c:choose>
                                    
                    <%-- start comment details table in table --%>
                    <table style="border:none; padding:0px; margin:0px"> 
                        
                    <tr>
                        <td style="border: none; padding:0px;">
                            <fmt:message key="commentManagement.entryTitled" /></td>
                        <td class="details" style="border: none; padding:0px;">  
                            <a href='<c:out value="${model.baseURL}" /><c:out value="${comment.weblogEntry.permaLink}" />'>
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
                                <c:out value="${comment.url}" /></a>
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
                
                    <br />
                    <span class="details">
                       <c:out value="${comment.content}" escapeXml="false" />
                    </span>
                </td>
            </tr>
            </c:forEach>
        </table>
        <br />

        <input type="submit" name="submit" 
            value='<fmt:message key="commentManagement.update" />' />
        &nbsp;
        <input type="button" name="Cancel" 
            value='Cancel' 
            onclick="window.location.href='<c:out value="${model.link}" />'" />

        </html:form>

    </c:when>
    
    <c:otherwise>
        <fmt:message key="commentManagement.noCommentsFound" />
    </c:otherwise>
    
</c:choose>

