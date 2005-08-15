<%@ include file="/taglibs.jsp" %>

<div class="sidebarfade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">

             <h3><fmt:message key="mainPage.searchWeblogs" /></h3>
             <hr />
             
        <form id="searchForm" method="get"
            action="<c:out value="${baseURL}" />/search"
            style="margin: 0; padding: 0" onsubmit="return validateSearch(this)">
            <input type="text" id="q" name="q" size="20"
                maxlength="255" value="<c:out value="${param.q}" />" />
            <input type="submit" value="<fmt:message key="mainPage.searchButton" />" />
        </form>
        <script type="text/javascript"> 
            // <!--
            function validateSearch(form) {
                if (form.q.value == "") {
                    alert("Please enter a search term to continue.");
                    form.q.focus();
                    return false;
                }
                return true;
            } 
            // -->
        </script>
        
			<br />
			
            </div>
        </div>
    </div>
</div>	

<br />

<div class="sidebarfade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarBody">

             <h3><fmt:message key="mainPage.hotWeblogs" /></h3>
             <hr />
        
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

    
    