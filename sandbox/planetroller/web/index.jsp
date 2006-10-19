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
<%@ page import="org.apache.roller.planet.business.PlanetManager" %>
<%@ page import="org.apache.roller.planet.business.PlanetFactory" %>
<%@ page import="org.apache.roller.planet.pojos.PlanetConfigData" %>
<%@ page import="org.apache.roller.planet.pojos.PlanetGroupData" %>
<%
    String fwd = "/main.faces";
    PlanetManager pmgr= PlanetFactory.getPlanet().getPlanetManager();
    PlanetConfigData pconfig = pmgr.getConfiguration();
    if (pconfig != null && pconfig.getDefaultGroup() != null) {
        PlanetGroupData group = pconfig.getDefaultGroup();
        fwd = "/" + group.getHandle() + "/";
    }
%>
<jsp:forward page="<%= fwd %>" />
