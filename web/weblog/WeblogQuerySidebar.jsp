<%@ include file="/taglibs.jsp" %>

<%@ page import="org.roller.presentation.weblog.actions.WeblogQueryPageModel" %>
<%
WeblogQueryPageModel model = (WeblogQueryPageModel)request.getAttribute("model");
%>

<div class="sidebarfade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">

<h3><fmt:message key="weblogEntryQuery.sidebarTitle" /></h3>
<hr />

<p><fmt:message key="weblogEntryQuery.sidebarDescription" /></p>

<html:form action="/editor/weblogQuery" method="post" focus="title">

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

  <%-- ========================================================= --%>
  <%-- limit number of results --%>
  
    <div class="sideformrow">
        <label for="maxEntries" class="sideformrow">
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
  <%-- search button --%>
  
    <br />
    <input type="button" name="post"
        value='<fmt:message key="weblogEntryQuery.button.query" />' 
        onclick="submit()">
    </input>
    <html:hidden name="method" property="method" value="query"/>

</html:form>

            </div>
        </div>
    </div>
</div>


