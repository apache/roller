<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>
<%@ page import="org.roller.presentation.weblog.actions.WeblogQueryPageModel" %>
<%
WeblogQueryPageModel model = (WeblogQueryPageModel)request.getAttribute("model");
%>

<h1><fmt:message key="weblogEntryQuery.title" /></h1>

<table class="rollertable">

   <c:forEach var="post" items="${model.recentWeblogEntries}">
      <tr>
         <td class="rollertable_entry" width="100%" >

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

            <roller:ApplyPlugins name="post" skipFlag="true" scope="page" />

         </td>
       <tr>
   </c:forEach>
   
   <c:if test="${empty model.recentWeblogEntries}" >
      <tr>
          <td height="400px">
              <fmt:message key="weblogEntryQuery.noneFound" />
          </td>
      </tr>
   </c:if>
   
</table>

<%@ include file="/theme/footer.jsp" %>


<script type="text/javascript">
<!--
try {Start();} catch (e) {};
-->
</script>


