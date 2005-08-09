<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>
<%@ page import="org.roller.presentation.weblog.actions.WeblogQueryPageModel" %>
<%
WeblogQueryPageModel model = (WeblogQueryPageModel)request.getAttribute("model");
%>

<h1><fmt:message key="weblogEntryQuery.title" /></h1>
<roller:StatusMessage/>

<p><fmt:message key="weblogEntryQuery.description" /></p>

<html:form action="/editor/weblogQuery" method="post" focus="title">

  <%-- ========================================================= --%>
  <%-- filter by category --%>
  
    <div class="formrow">
        <label for="categoryId" class="formrow">
            <fmt:message key="weblogEntryQuery.label.category" /></label>          
        <html:select property="categoryId" size="1" tabindex="4">
            <html:option key="weblogEntryQuery.label.any" value="" />
            <html:optionsCollection name="model" property="categories" value="id" label="path"  />
        </html:select>
    </div>
  
  <%-- ========================================================= --%>
  <%-- filter by date --%>
  
    <div class="formrow">
        <label for="startDateString" class="formrow">
           <fmt:message key="weblogEntryQuery.label.startDate" />:
        </label>
        <roller:Date property="startDateString" dateFormat='<%= model.getShortDateFormat() %>' />
    </div>

    <div class="formrow">
        <label for="endDateString" class="formrow">
           <fmt:message key="weblogEntryQuery.label.endDate" />:
        </label>
        <roller:Date property="endDateString" dateFormat='<%= model.getShortDateFormat() %>' />
    </div>

  <%-- ========================================================= --%>
  <%-- filter by status --%>

    <div class="formrow">
        <label for="status" class="formrow">
            <fmt:message key="weblogEntryQuery.label.status" />:
        </label>        
        <html:radio property="status" value="ALL">
            <fmt:message key="weblogEntryQuery.label.allEntries" /></html:radio> 
        <html:radio property="status" value="DRAFT">
            <fmt:message key="weblogEntryQuery.label.draftOnly" /></html:radio>
        <html:radio property="status" value="PUBLISHED" >
            <fmt:message key="weblogEntryQuery.label.publishedOnly" /></html:radio>
    </div>

  <%-- ========================================================= --%>
  <%-- limit number of results --%>
  
    <div class="formrow">
        <label for="maxEntries" class="formrow">
            <fmt:message key="weblogEntryQuery.label.maxEntries" />
        </label>
        <html:select property="maxEntries" size="1" tabindex="4">
            <html:option value="25" />
            <html:option value="50" />
            <html:option value="75" />
            <html:option value="100" />
        </html:select>
    </div>
    
  <%-- ========================================================= --%>
  <%-- search button --%>
  
    <br />
    <div class="control">
        <input type="button" name="post"
            value='<fmt:message key="weblogEntryQuery.button.query" />' onclick="submit()"></input>
    </div>
    <br />
    <html:hidden name="method" property="method" value="query"/>

</html:form>

<h1><fmt:message key="weblogEntryQuery.section.searchResults" /></h1>

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
</table>

<%@ include file="/theme/footer.jsp" %>


<script type="text/javascript">
<!--
try {Start();} catch (e) {};
-->
</script>


