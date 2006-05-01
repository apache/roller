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
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
  <link rel="stylesheet" type="text/css" href="<wiki:BaseURL/>templates/<wiki:TemplateDir/>/jspwiki.css" />
  <%@ include file="/scripts/cssinclude.js" %>
  <script src="<wiki:BaseURL/>scripts/search_highlight.js" type="text/javascript"></script>
  <script src="<wiki:BaseURL/>scripts/jspwiki-common.js" type="text/javascript"></script>
  <meta http-equiv="Content-Type" content="text/html; charset=<wiki:ContentEncoding />" />
  <link rel="search" href="<wiki:LinkTo format="url" page="FindPage"/>"            title="Search <wiki:Variable var="ApplicationName" />" />
  <link rel="help"   href="<wiki:LinkTo format="url" page="TextFormattingRules"/>" title="Help" />
  <link rel="start"  href="<wiki:LinkTo format="url" page="Main"/>"                title="Front page" />
  <link rel="stylesheet" type="text/css" media="print" href="<wiki:BaseURL/>templates/<wiki:TemplateDir/>/jspwiki_print.css" />
  <link rel="alternate stylesheet" type="text/css" href="<wiki:BaseURL/>templates/<wiki:TemplateDir/>/jspwiki_print.css" title="Print friendly" />
  <link rel="alternate stylesheet" type="text/css" href="<wiki:BaseURL/>templates/<wiki:TemplateDir/>/jspwiki.css" title="Standard" />
  <link rel="icon" type="image/png" href="<wiki:BaseURL/>images/favicon.png" />
  <link rel="stylesheet" type="text/css" href="<wiki:BaseURL/>templates/<wiki:TemplateDir/>/layout.css" />
  <link rel="stylesheet" type="text/css" href="<wiki:BaseURL/>templates/<wiki:TemplateDir/>/roller.css" />
  <link rel="stylesheet" type="text/css" href="<wiki:BaseURL/>templates/<wiki:TemplateDir/>/layout.css" />
  <link rel="stylesheet" type="text/css" href="<wiki:BaseURL/>templates/<wiki:TemplateDir/>/menu.css" />
  <link rel="stylesheet" type="text/css" href="<wiki:BaseURL/>templates/<wiki:TemplateDir/>/tan/colors.css" />
  <wiki:FeedDiscovery />

