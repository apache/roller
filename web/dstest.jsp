<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    
<%-- simple test page to test your Roller datasource setup --%>
<%@ page import="javax.naming.InitialContext" %>
<%@ page import="javax.sql.DataSource" %>
<%@ page import="java.sql.Connection" %>
<html>
<head>
<title>Roller DataSource test</title>
</head>
<body>
<p>
<%
String msg = null; 
Connection con = null;
try
{
    InitialContext ic = new InitialContext();
    DataSource ds = (DataSource)ic.lookup("java:comp/env/jdbc/rollerdb");
    con = ds.getConnection();
    msg = "SUCCESS: Got datasource and connection, class is "+ds.getClass().getName();
}
catch (Exception e)
{
    msg = "FAILURE: exception thrown "+e.getClass().getName();
    e.printStackTrace( new java.io.PrintWriter(out) );
}
finally 
{
    if (con != null) con.close();
}

%>
<%= msg %>
</p>
</body>
</html>
