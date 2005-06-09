<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<h3>
<img src='<c:url value="/images/Bookmark24.png"/>' alt="bookmark" align="absmiddle" />
<c:if test="${state == 'add'}">
    <fmt:message key="bookmarkForm.addBookmark" />
</c:if>
<c:if test="${state == 'edit'}">
    <fmt:message key="bookmarkForm.editBookmark" />
</c:if>
<c:if test="${state == 'correct'}">
    <fmt:message key="bookmarkForm.correctBookmark" />
</c:if>
</h3>

<p>
<b><fmt:message key="bookmarksForm.path" /></b>:
<c:if test="${!empty parentFolder.parent.path}">
    <c:out value="${parentFolder.parent.path}" />
</c:if>
<c:if test="${empty parentFolder.parent.path}">
    /
</c:if>
</p>

<html:form action="/editor/bookmarkSave" method="post" focus="name">

    <html:hidden property="method" value="update"/></input>
    <html:hidden property="id" /></input>

    <input type="hidden" name="<%= RollerRequest.FOLDERID_KEY %>" 
        value="<%= request.getAttribute(RollerRequest.FOLDERID_KEY) %>" />
						   
    <table>
    							 
	<tr>
	    <td><fmt:message key="bookmarkForm.name" /></td>
	    <td><html:text property="name" maxlength="255" size="70" /></input></td>
    </tr>

	<tr>
	    <td><fmt:message key="bookmarkForm.description" /></td>
	    <td><html:textarea property="description" rows="5" cols="50" /></td>
    </tr>

	<tr>
	    <td><fmt:message key="bookmarkForm.url" /></td>                
	    <td><html:text property="url" maxlength="255" size="70" /></input></td>
    </tr>
						  
	<tr>
	    <td><fmt:message key="bookmarkForm.rssUrl" /></td>         
	    <td><html:text property="feedUrl" maxlength="255" size="70" /></input></td>
    </tr>
							
	<tr>
	    <td><fmt:message key="bookmarkForm.image" /></td>          
	    <td><html:text property="image" maxlength="255" size="70" /></input></td>
    </tr>

	<tr>
	    <td><fmt:message key="bookmarkForm.priority" /></td>         
	    <td><html:text property="priority" maxlength="255" size="5" /></input></td>
    </tr>

	<tr>
        <td><fmt:message key="bookmarkForm.weight" /></td>          
	    <td><html:text property="weight" maxlength="255" size="5" /></input></td>
    </tr>
							
    </table>

    <p>
    <input type="submit" value="<fmt:message key='bookmarkForm.save'/>" />
    <input type="button" value="<fmt:message key='bookmarkForm.cancel' />" 
        onclick="window.location = 'bookmarks.do?method=selectFolder&amp;folderid=<%=
        request.getAttribute(RollerRequest.FOLDERID_KEY) %>'" />
    </p>

</html:form>

<%@ include file="/theme/footer.jsp" %>