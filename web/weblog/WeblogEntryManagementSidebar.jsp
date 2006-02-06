<%@ include file="/taglibs.jsp" %>

<%@ page import="org.roller.presentation.weblog.actions.WeblogEntryManagementAction" %>
<%
WeblogEntryManagementAction.PageModel model = 
    (WeblogEntryManagementAction.PageModel)request.getAttribute("model");
%>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">

<div class="sidebarInner">
        
<h3><fmt:message key="weblogEntryQuery.sidebarTitle" /></h3>
<hr size="1" noshade="noshade" />

<p><fmt:message key="weblogEntryQuery.sidebarDescription" /></p>

<html:form action="/editor/weblogEntryManagement" method="post" focus="title">
    <input type="hidden" name="weblog" value='<c:out value="${model.website.handle}" />' />
    <html:hidden name="method" property="method" value="query"/>
    <html:hidden property="count" />
    <html:hidden property="offset" />

  <%-- ========================================================= --%>
  <%-- filter by category --%>
  
    <div class="sideformrow">
        <label for="categoryId" class="sideformrow">
            <fmt:message key="weblogEntryQuery.label.category" /></label>          
        <html:select property="categoryId" size="1" tabindex="4">
            <html:option key="weblogEntryQuery.label.any" value="" />
            <html:optionsCollection name="model" property="categories" value="id" label="path"  />
        </html:select>
    </div>
    <br />
    <br />

  <%-- ========================================================= --%>
  <%-- filter by date --%>
  
    <div class="sideformrow">
        <label for="startDateString" class="sideformrow">
           <fmt:message key="weblogEntryQuery.label.startDate" />:
        </label>
        <roller:Date property="startDateString" dateFormat='<%= model.getShortDateFormat() %>' />
    </div>

    <div class="sideformrow">
        <label for="endDateString" class="sideformrow">
           <fmt:message key="weblogEntryQuery.label.endDate" />:
        </label>
        <roller:Date property="endDateString" dateFormat='<%= model.getShortDateFormat() %>' />
    </div>
    <br />
    <br />
    
  <%-- ========================================================= --%>
  <%-- filter by status --%>

    <div class="sideformrow">
        <label for="status" class="sideformrow">
            <fmt:message key="weblogEntryQuery.label.status" />:
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
        </label> 
        <div>
        <html:radio property="status" value="ALL">
            <fmt:message key="weblogEntryQuery.label.allEntries" /></html:radio> 
        <br />
        <html:radio property="status" value="DRAFT">
            <fmt:message key="weblogEntryQuery.label.draftOnly" /></html:radio>
        <br />
        <html:radio property="status" value="PUBLISHED" >
            <fmt:message key="weblogEntryQuery.label.publishedOnly" /></html:radio>
        <br />
        <html:radio property="status" value="PENDING" >
            <fmt:message key="weblogEntryQuery.label.pendingOnly" /></html:radio>
        </div>
    </div>

  <%-- ========================================================= --%>
  <%-- sort by --%>

    <div class="sideformrow">
        <label for="status" class="sideformrow">
            <fmt:message key="weblogEntryQuery.label.sortby" />:
            <br />
            <br />
        </label> 
        <div>
        <html:radio property="sortby" value="pubTime">
            <fmt:message key="weblogEntryQuery.label.pubTime" /></html:radio> 
        <br />
        <html:radio property="sortby" value="updateTime">
            <fmt:message key="weblogEntryQuery.label.updateTime" /></html:radio>
        </div>
    </div>

  <%-- ========================================================= --%>
  <%-- search button --%>
  
    <br />
    <input type="button" name="post"
        value='<fmt:message key="weblogEntryQuery.button.query" />' 
        onclick="submit()">
    </input>
    
</html:form>

<br />
<br />
</div> <!-- sidebarInner -->

        </div>
    </div>
</div>


