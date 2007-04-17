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
<% response.setContentType("text/html; charset=UTF-8"); %>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<%@ page import="org.apache.roller.business.Roller" %>
<%@ page import="org.apache.roller.business.RollerFactory" %>

<%@ page import="org.apache.roller.pojos.UserData" %>
<%@ page import="org.apache.roller.pojos.WebsiteData" %>
<%@ page import="org.apache.roller.pojos.RollerConfigData" %>

<%@ page import="org.apache.roller.config.RollerConfig" %>
<%@ page import="org.apache.roller.config.RollerRuntimeConfig" %>
<%@ page import="org.apache.roller.config.RollerConfig" %>

<%@ page import="org.apache.roller.ui.core.RequestConstants" %>
<%@ page import="org.apache.roller.ui.core.BasePageModel" %>
<%@ page import="org.apache.roller.ui.core.RollerSession" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles2" prefix="tiles" %>
<%@ taglib uri="/struts-tags" prefix="s" %>