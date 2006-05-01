<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
-->
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
