<%@ include file="/taglibs.jsp" %>

<table class="sidebarBox" >
    <tr>
       <td class="sidebarBox"> 
          <div class="menu-tr"><div class="menu-tl">
             <fmt:message key="mainPage.sidebarHelpTitle" />
          </div></div>
       </td>
    </tr>    
    <tr>
        <td>
            <img src="../images/Help16.gif" alt="help-icon" align="bottom" />
            <c:choose>
                <c:when test="${model.groupBloggingEnabled}">
                   <fmt:message key="yourWebsites.groupBloggingEnabled" />  
                </c:when>
                <c:when test="${!model.groupBloggingEnabled}">
                    <fmt:message key="yourWebsites.groupBloggingDisabled" />
                </c:when>
            </c:choose>
        </td>
    </tr>
</table>

<br />
