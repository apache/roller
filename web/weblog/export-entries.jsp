<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>
<%@ page import="org.roller.presentation.BasePageModel" %>
<%
BasePageModel model = (BasePageModel)request.getAttribute("model");
%>

<h1><fmt:message key="weblogEntryExport.title" /></h1>

<roller:StatusMessage/>

<html:form action="/editor/exportEntries" method="post" focus="title">

    <html:hidden name="method" property="method" value="export"/>

    <h3><fmt:message key="weblogEntryQuery.section.dateRange" /></h3>
    
    <div class="row">
        <label style="width:20%; float:left; text-align:right; padding: 2px" for="startDateString">
           <fmt:message key="weblogEntryQuery.label.startDate" />:
        </label>
        <roller:Date property="startDateString" dateFormat='<%= model.getShortDateFormat() %>' />
    </div>
    
    <div class="row">
        <label style="width:20%; float:left; text-align:right; padding: 2px" for="endDateString">
           <fmt:message key="weblogEntryQuery.label.endDate" />:
        </label>
        <roller:Date property="endDateString" dateFormat='<%= model.getShortDateFormat() %>' />
    </div>
    
    <h3><fmt:message key="weblogEntryQuery.section.format" /></h3>
    
    <div class="row">
        <label style="width:20%; float:left; text-align:right; padding: 2px" for="fileBy">
            <fmt:message key="weblogEntryQuery.label.separateEntries" />:
        </label>
        <select name="fileBy">
            <option><fmt:message key="weblogEntryQuery.label.day" /></option>
            <option selected="selected"><fmt:message key="weblogEntryQuery.label.month" /></option>
            <option><fmt:message key="weblogEntryQuery.label.year" /></option>
        </select>
    </div>
    
    <div class="row">
        <label style="width:20%; float:left; text-align:right; padding: 2px" for="exportFormat">
            <fmt:message key="weblogEntryQuery.label.exportTo" />:
        </label>
        <input type="radio" name="exportFormat" value="Atom" checked="checked">
            <fmt:message key="weblogEntryQuery.label.atom" /></input>
        <br />
        <input type="radio" name="exportFormat" value="RSS" >
            <fmt:message key="weblogEntryQuery.label.rss" /></input>
    </div>
    
    <div class="buttonBox">
        <input type="button" name="post" 
            value='<fmt:message key="weblogEntryQuery.button.export" />' onclick="submit()" />
    </div>
    
</html:form>

<%@ include file="/theme/footer.jsp" %>
