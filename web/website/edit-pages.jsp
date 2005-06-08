
<%@ include file="/theme/header.jsp" %>

<roller:StatusMessage/>

<h1><fmt:message key="pagesForm.title" /></h1>

<%-- table of pages --%>
<table class="rollertable">
    <tr>
        <th width="10%"><fmt:message key="pagesForm.name" /></th>
        <th width="10%"><fmt:message key="pagesForm.link" /></th>
        <th width="70%"><fmt:message key="pagesForm.description" /></th>
        <th width="5%"><fmt:message key="pagesForm.edit" /></th>
        <th width="5%"><fmt:message key="pagesForm.remove" /></th>
    </tr>
    <logic:iterate id="p" name="pages" >
        <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">

            <td><bean:write name="p" property="name" /></td>
            <td><bean:write name="p" property="link" /></td>
            <td><bean:write name="p" property="description" /></td>

            <%-- links to edit and remove page actions --%>
            <td class="center">
               <roller:link forward="editPage">
                  <roller:linkparam id="username" name="user" property="userName" />
                  <roller:linkparam id="pageid" name="p" property="id" />
                  <img src="images/Edit16.png" border="0" alt="icon" />
               </roller:link> 
            </td>
 	
            <td class="center">
               <roller:link forward="removePage.ok">
                  <roller:linkparam id="username" name="user" property="userName" />
                  <roller:linkparam id="pageid" name="p" property="id" />
                   <img src="images/Remove16.gif" border="0" alt="icon" />
               </roller:link>
            </td>
 	
        </roller:row>
    </logic:iterate>
</table>

<p><fmt:message key="pagesForm.hiddenNote" /></p>

<%-- form to add a new page --%>
<h2><fmt:message key="pagesForm.addNewPage" /></h2>
<html:form action="/page" method="post" focus="name">

    <fmt:message key="pagesForm.name"/>: <html:text property="name" size="30"/>
	
	<input type="submit" value='<fmt:message key="pagesForm.add" />' />
	<input type="hidden" property="template" 
		value="<html><body><fmt:message key="pagesForm.emptyPage" /></body></html>" />
	<html:hidden property="method" value="add"/>
	
</html:form>

<%@ include file="/theme/footer.jsp" %>

