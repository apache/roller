
<br />
<br />

        <div id="footer" class="clearfix">
            <a href="http://www.rollerweblogger.org">
                Powered by Roller Weblogger</a> | 
                
            <a href="http://opensource.atlassian.com/projects/roller/Dashboard.jspa">
                <fmt:message key="footer.reportIssue" /></a> | 
                
            <a href="http://www.rollerweblogger.org/wiki/Wiki.jsp?page=UserGuide">
                <fmt:message key="footer.userGuide" /></a> | 
                
            <a href="http://www.rollerweblogger.org/wiki/Wiki.jsp?page=RollerMacros">
                <fmt:message key="footer.macros" /></a> | 
                
            <a href="http://sourceforge.net/mail/?group_id=47722">
                <fmt:message key="footer.mailingLists" /></a>
        </div><!-- end footer -->
    
</div> <!-- end centercontent --> 

<div id="rightcontent"> 
    <c:import url="/theme/status.jsp" />
    <c:if test="${!empty leftPage}">
        <c:import url="${leftPage}" />
    </c:if>
</div>

</div> <!-- end wrapper -->

</body>
</html>

<% } catch (Exception e) { e.printStackTrace(); } %>



