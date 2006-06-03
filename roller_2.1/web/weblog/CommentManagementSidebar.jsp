<%@ include file="/taglibs.jsp" %>
<%@ page import="org.roller.presentation.weblog.actions.CommentManagementAction" %>
<%
CommentManagementAction.CommentManagementPageModel model = 
    (CommentManagementAction.CommentManagementPageModel)request.getAttribute("model");
%>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">

<div class="sidebarInner">

<h3><fmt:message key="commentManagement.sidebarTitle" /></h3>
<hr size="1" noshade="noshade" />

<p><fmt:message key="commentManagement.sidebarDescription" /></p>
    
 <% String path = model.getWebsite()==null 
    ? "/admin/commentQuery" : "/editor/commentQuery"; %>
 <html:form action="<%= path %>" method="post">
 
    <c:if test="${!empty model.website}">
        <input name="weblog" type="hidden" value='<c:out value="${model.website.handle}" />' />
    </c:if>
    <c:if test="${!empty model.weblogEntry}">
        <input name="entryid" type="hidden" value='<c:out value="${model.weblogEntry.id}" />' />
    </c:if>
        
    <div class="sideformrow">
        <label for="searchString" class="sideformrow">
            <fmt:message key="commentManagement.searchString" /></label>   
        <html:text property="searchString" /></input>
    </div>
    <br />

   <%-- ========================================================= --%>
   <%-- filter by date --%>
  
    <div class="sideformrow">
        <label for="pendingString" class="sideformrow">
            <fmt:message key="commentManagement.pendingStatus" />
            <br />
            <br />
            <br />
            <br />            
            <br />
        </label> 
        <div>
        <div>
            <html:radio property="pendingString" value="ALL">
                <fmt:message key="commentManagement.all" /></html:radio><br />
            <html:radio property="pendingString" value="ONLY_PENDING">
                <fmt:message key="commentManagement.onlyPending" /></html:radio><br />
            <html:radio property="pendingString" value="ONLY_APPROVED">
                <fmt:message key="commentManagement.onlyApproved" /></html:radio><br />
            <html:radio property="pendingString" value="ONLY_DISAPPROVED" >
                <fmt:message key="commentManagement.onlyDisapproved" /></html:radio><br />
        </div>
    </div> 
    <br />

    <div class="sideformrow">
        <label for="spamString" class="sideformrow">
            <fmt:message key="commentManagement.spamStatus" />   
            <br />
            <br />
            <br />
            <br />
        </label>
        <div>
            <html:radio property="spamString" value="ALL">
                <fmt:message key="commentManagement.all" /></html:radio><br />
            <html:radio property="spamString" value="NO_SPAM">
                <fmt:message key="commentManagement.noSpam" /></html:radio><br />
            <html:radio property="spamString" value="ONLY_SPAM" >
                <fmt:message key="commentManagement.onlySpam" /></html:radio><br />
        </div>
    </div>
    <br />
    
    <br />
    <div class="sideformrow">
        <label for="startDateString" class="sideformrow">
           <fmt:message key="commentManagement.startDate" />:
        </label>
        <roller:Date property="startDateString" formName="commentQueryForm"
            dateFormat='<%= model.getShortDateFormat() %>' />
    </div>
        
    <div class="sideformrow">
        <label for="endDateString" class="sideformrow">
           <fmt:message key="commentManagement.endDate" />:
        </label>
        <roller:Date property="endDateString" formName="commentQueryForm"
            dateFormat='<%= model.getShortDateFormat() %>' />
    </div>
    <br />
    
    <input type="submit" name="post" 
        value='<fmt:message key="commentManagement.query" />' />
    </input>
    <input type="hidden" name="method" value="query"/>
            
</html:form>

<br />
<br />
</div> <!-- sidebarInner -->

        </div>
    </div>
</div>


