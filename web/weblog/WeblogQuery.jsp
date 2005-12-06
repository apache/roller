<%@ include file="/taglibs.jsp" %>
<%@ page import="org.roller.presentation.weblog.actions.WeblogQueryPageModel" %>
<%
WeblogQueryPageModel model = (WeblogQueryPageModel)request.getAttribute("model");
%>

<p class="subtitle">
    <fmt:message key="weblogEntryQuery.subtitle" >
        <fmt:param value="${model.website.handle}" />
    </fmt:message>
</p>
<p class="pagetip">
    <fmt:message key="weblogEntryQuery.tip" />
</p>

<table class="rollertable" width="100%">
    
<tr>
    <th class="rollertable">
        <fmt:message key="weblogEntryQuery.weblogEntries" />
    </th>
</tr>
<tr class="actionrow">
    <td align="right" valign="center">
        <span class="draftEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span> 
        <fmt:message key="weblogEntryQuery.draft" />&nbsp;&nbsp;
        <span class="pendingEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <fmt:message key="weblogEntryQuery.pending" />&nbsp;&nbsp;
    </td> 
</tr>

<c:forEach var="post" items="${model.recentWeblogEntries}">
    <tr>
        <%-- <td> with style if comment is spam or pending --%>               
        <c:choose>
            <c:when test='${post.status == "DRAFT"}'>
                <td class="draftentry"> 
            </c:when>
            <c:when test='${post.status == "PENDING"}'>
                <td class="pendingentry"> 
            </c:when>
            <c:otherwise>
                <td>
            </c:otherwise>
        </c:choose>
        
        <%-- entry details table in table --%>
        <table style="border:none; padding:0px; margin:0px">                                               
        <tr> <%-- title --%>
            <td style="border: none; padding:0px;">
               <fmt:message key="weblogEntryQuery.title" />
            </td>
            <td class="details" style="border: none; padding:0px;">
                 <c:out value="${post.displayTitle}" />
                 [<roller:link page="/editor/weblog.do">
                    <roller:linkparam
                        id="<%= RollerRequest.WEBLOGENTRYID_KEY %>"
                        name="post" property="id" />
                    <roller:linkparam id="method" value="edit" />
                    <fmt:message key="weblogEntryQuery.edit" />
                </roller:link>]
            </td>
        </tr>                                                                    
        <c:if test='${post.status == "PUBLISHED"}'>
            <tr> <%-- link --%>
                <td style="border: none; padding:0px;">
                   <fmt:message key="weblogEntryQuery.link" />
                </td>
                <td class="details" style="border: none; padding:0px;">
                    <a href='<c:out value="${model.baseURL}" /><c:out value="${post.permaLink}" />'>
                        <c:out value="${model.baseURL}" /><c:out value="${post.permaLink}" />
                    </a>
                </td>
            </tr> 
        </c:if>
        <c:choose>
            <c:when test='${post.status == "PUBLISHED"}'>
                <tr> <%-- pubtime --%>
                    <td style="border: none; padding:0px;">
                       <fmt:message key="weblogEntryQuery.pubTime" />
                    </td>
                    <td class="details" style="border: none; padding:0px;">
                        <fmt:formatDate value="${post.pubTime}" type="both" 
                            dateStyle="medium" timeStyle="medium" />
                    </td>
                </tr>    
            </c:when>
            <c:otherwise>
                <tr> <%-- updatetime --%>
                    <td style="border: none; padding:0px;">
                       <fmt:message key="weblogEntryQuery.updateTime" />
                    </td>
                    <td class="details" style="border: none; padding:0px;">
                        <fmt:formatDate value="${post.updateTime}" type="both" 
                            dateStyle="medium" timeStyle="medium" />
                            &nbsp;(<b><c:out value="${post.status}" /></b>)
                    </td>
                </tr>                                                                                                                                      
            </c:otherwise>
        </c:choose>
        <tr> <%-- category --%>
            <td style="border: none; padding:0px;">
               <fmt:message key="weblogEntryQuery.category" />
            </td>
            <td class="details" style="border: none; padding:0px;">
                <c:out value="${post.category.path}" />
            </td>
        </tr>
        
        </table> <%-- end entry details table in table --%>
        <br />
   
        <div style="overflow:auto">
            <roller:ApplyPlugins name="post" skipFlag="true" scope="page" />
        </div>
        
    </td></tr>
</c:forEach>
</table>

<c:if test="${empty model.recentWeblogEntries}" >
   <fmt:message key="weblogEntryQuery.noneFound" />
   <br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br />
</c:if>

<script type="text/javascript">
<!--
try {Start();} catch (e) {};
-->
</script>


