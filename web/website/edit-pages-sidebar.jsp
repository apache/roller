<%@ include file="/taglibs.jsp" %>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            
             <h3><fmt:message key="pagesForm.addNewPage" /></h3>
             <hr size="1" noshade="noshade" />
             
             <html:form action="/editor/page" method="post" focus="name">

                <fmt:message key="pagesForm.name"/>: <input type="text" name="name" size="12" />

                <input type="submit" value='<fmt:message key="pagesForm.add" />' />
                <input type="hidden" name="weblog" value='<c:out value="${model.website.handle}" />' />
                <html:hidden property="method" value="add"/>

             </html:form>
             <br />
             
        </div>
    </div>
</div>	


