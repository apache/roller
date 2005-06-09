<%@ page import="com.ecyrd.jspwiki.Release" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<!-- LeftMenuFooter is automatically generated from a Wiki page called "LeftMenuFooter" -->

<p>
    <wiki:InsertPage page="LeftMenuFooter" />
    <wiki:NoSuchPage page="LeftMenuFooter">
        <p><hr /></p>
        <p align="center">
        <i>No LeftMenuFooter!</i><br />
        <wiki:EditLink page="LeftMenuFooter">Please make one.</wiki:EditLink><br />
        </p>
        <p><hr /></p>
    </wiki:NoSuchPage>
</p>

<!-- End of automatically generated page -->

   <br /><br /><br />
   <div align="center" class="small">
   <%=Release.APPNAME%> v<%=Release.getVersionString()%>
   </div>


