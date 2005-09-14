<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.Release" %>

<p>
    <wiki:InsertPage page="LeftMenu" />
</p>

<hr />

<p>
    <wiki:UserCheck status="known">
        <b>Hello </b><br />
        <wiki:Translate>[<wiki:UserName />]</wiki:Translate>
    </wiki:UserCheck>

    <wiki:UserCheck status="unknown">
        <tt>
        Set your name in<br />
        <wiki:LinkTo page="UserPreferences">UserPreferences</wiki:LinkTo>
        </tt>    
    </wiki:UserCheck>
    <wiki:Include page="LoginBox.jsp" />
</p>

<!-- End of automatically generated page -->

