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

<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title><wiki:Variable var="applicationname" />: <wiki:PageName /></title>
  <wiki:Include page="commonheader.jsp"/>
  <wiki:CheckVersion mode="notlatest">
        <meta name="robots" content="noindex,nofollow" />
  </wiki:CheckVersion>
</head>

<body class="view" bgcolor="#FFFFFF">

<div id="wrapper">
   
    <div id="banner">
        <div class="bannerBox">
           <img class="bannerlogo" src='<wiki:BaseURL/>/templates/roller2/tan/logo.gif' />
        </div>
        <div class="bannerStatusBox">
           <div class="bannerLeft">
              Your trail: <wiki:Breadcrumbs maxpages="5"  />
           </div>
           <div class="bannerRight">
              <a href="/roller/main.do">Roller</a>  
              | <a href="/wiki">JSPWiki</a>
           </div>
        </div>
    </div>
    
    <div id="leftcontent"> 
    </div>
    
    <div id="centercontent"> 
       <h1 class="pagename"><a name="Top"><wiki:PageName/></a></h1>
       <wiki:Content/>
    </div>
    
    <div id="rightcontent">

       <div class="sidebarfade">
          <div class="menu-tr">
             <div class="menu-tl">
                <div class="sidebarBody">
                   <h3>Search</h3>
                   <br />
                   <wiki:Include page="SearchBox.jsp"/>
                   <br />
                </div>
             </div>
          </div>
       </div>

       <div class="sidebarfade">
          <div class="menu-tr">
             <div class="menu-tl">
                <div class="sidebarBody">
                   <h3>Quick Links</h3>
                   <br />
                   <wiki:Include page="LeftMenu.jsp"/>
                   <wiki:Include page="LeftMenuFooter.jsp"/>
                   <wiki:CheckRequestContext context="view">
                       <wiki:Permission permission="edit">
                           <wiki:EditLink>Edit this page</wiki:EditLink>
                       </wiki:Permission>
                   </wiki:CheckRequestContext>
                   <br />
                   <br />
                   <wiki:RSSImageLink title="Aggregate the RSS feed" />
                   <br />
                   <br />
                </div>
             </div>
          </div>
       </div>

    </div>  
 
</div>

</body>

</html>

