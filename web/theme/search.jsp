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
