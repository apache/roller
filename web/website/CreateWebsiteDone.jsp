<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<h1><fmt:message key="createWebsiteDone.title" /></h1>

<p><fmt:message key="createWebsiteDone.message"/></p>

<div class="formrow">
   <label class="formrow" style="font-weight:bold">
       <fmt:message key="createWebsiteDone.name"/></label>
   <c:out value="${model.website.name}" />
</div>

<div class="formrow">
   <label class="formrow" style="font-weight:bold">
       <fmt:message key="createWebsiteDone.description"/></label>
   <c:out value="${model.website.description}" />
</div>

<div class="formrow">
   <label class="formrow" style="font-weight:bold">
       <fmt:message key="createWebsiteDone.handle"/></label>
   <c:out value="${model.website.handle}" />
</div>

<div class="formrow">
   <label class="formrow" style="font-weight:bold">
       <fmt:message key="createWebsiteDone.weblogURL"/></label>
   <c:out value="${model.weblogURL}" />
</div>

<div class="formrow">
   <label class="formrow" style="font-weight:bold">
       <fmt:message key="createWebsiteDone.rssURL"/></label>
   <c:out value="${model.rssURL}" />
</div>

<%@ include file="/theme/footer.jsp" %>


