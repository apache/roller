<%--
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
--%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><s:text name="Menu.pageTitle" /></title>
        <link rel="stylesheet" type="text/css" href="<s:url value="/planet-ui/css/planet.css" />" />
    </head>
    <body>
        <div id="wrapper">
            
            <h1><s:text name="Menu.heading" /></h1>
            
            <p><a href='<s:url value="/" />'><s:text name="Menu.mainPlanetLink" /></a></p>
            
            <p><a href='<s:url action="Register" />'><s:text name="Menu.mainRegistrationLink" /></a></p>
            
            <p><a href='<s:url action="ConfigForm" namespace="/planet-ui/admin" />'><s:text name="Menu.mainConsoleLink" /></a></p>
            
        </div>
    </body>
</html>
