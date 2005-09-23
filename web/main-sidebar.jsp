<%@ include file="/taglibs.jsp" %>

<div class="sidebarBodyHead">
    <div class="menu-tr">
        <div class="menu-tl">
            <h3><fmt:message key="mainPage.hotWeblogs" /></h3>		
        </div>
    </div>
</div>	

<div class="sidebarBody">       
    <c:if test="${not empty popularWebsites}"> <%-- to prevent invalid XHTML --%>
    <ul>
        <c:forEach var="site" items="${popularWebsites}">
           <li>
               <a href='<c:out value="${baseURL}" />/page/<c:out value="${site.userName}" />'
                  title='<c:out value="${site.userName}" />' >
                  <str:truncateNicely lower="45" 
                    upper="45" ><c:out 
                    value="${site.websiteName}" /></str:truncateNicely></a>:
               <c:out value="${site.hits}" /> <fmt:message key="mainPage.hits" />
           </li>
        </c:forEach>
    </ul>
    </c:if>
    <br />
</div>


    
    