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

<div class="entryTitleBox">
    <fmt:message key="weblogEntryQuery.queryResults" />
</div>

<div class="entriesBox">
<div class="entriesBoxInner">

<c:forEach var="post" items="${model.recentWeblogEntries}">

    <div class="entryBox">
    
        <roller:link page="/editor/weblog.do">
            <roller:linkparam
                id="<%= RollerRequest.WEBLOGENTRYID_KEY %>"
                name="post" property="id" />
                <roller:linkparam id="method" value="edit" />
                <img src='<c:url value="/images/Edit16.png"/>' border="0" alt="icon" title="Edit" />
                <c:out value="${post.displayTitle}" />
        </roller:link>
        <br />

        <span class="entryDetails">
            <fmt:message key="weblogEdit.category" /> [<c:out value="${post.category.path}" />] |
            <fmt:formatDate value="${post.pubTime}" type="both" dateStyle="medium" timeStyle="medium" />
            <c:if test="${!empty post.link}">
              <a href='<c:out value="${post.link}" />' class="entryDetails">
                 <fmt:message key="weblogEdit.link" />
              </a>
            </c:if>
            <a href='<c:out value="${model.baseURL}" /><c:out value="${post.permaLink}" />'
                class="entrypermalink" title="entry permalink">#</a>
            <br />
            <br />
        </span>

        <div style="overflow:auto">
            <roller:ApplyPlugins name="post" skipFlag="true" scope="page" />
        </div>
        
    </div>

</c:forEach>
</div> <!-- entriesBoxInner -->
</div> <!-- entriesBox -->

<c:if test="${empty model.recentWeblogEntries}" >
   <fmt:message key="weblogEntryQuery.noneFound" />
   <br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br />
</c:if>

<script type="text/javascript">
<!--
try {Start();} catch (e) {};
-->
</script>


