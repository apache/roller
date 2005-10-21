<%@ include file="/taglibs.jsp" %>

<div class="searchSidebarHead">
    <div class="menu-tr">
        <div class="menu-tl">
           <h3>&nbsp;</h3>
        </div>
    </div>
</div>

<div class="searchSidebarBody">

     <h3><fmt:message key="mainPage.searchWeblogs" /></h3>

     <form id="searchForm" method="get"
        action="<c:out value="${baseURL}" />/sitesearch.do"
        style="margin: 0; padding: 0" onsubmit="return validateSearch(this)">
        <input type="text" id="q" name="q" size="20"
            maxlength="255" value="<c:out value="${param.q}" />" />
        <input value="&nbsp;»&nbsp;" class="searchButton" type="submit">
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

</div>

