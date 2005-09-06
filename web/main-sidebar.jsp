<%@ include file="/taglibs.jsp" %>

<div class="sidebarfade_hotblogs">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">

             <h3><fmt:message key="mainPage.hotWeblogs" /></h3>
             <hr size="1" noshade="noshade" />
        
	      <c:if test="${not empty popularWebsites}"> <%-- to prevent invalid XHTML --%>
	        <ul style="list-style-type:none; padding-left:2px; margin: 0px">
	        <c:forEach var="site" items="${popularWebsites}">
	           <li style="list-style-type:none; padding-left:2px; margin: 0px">
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
        </div>
    </div>
</div>	

    
    