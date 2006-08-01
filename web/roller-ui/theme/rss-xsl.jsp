<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
   xmlns:dc="http://purl.org/dc/elements/1.1/" version="1.0">
<xsl:output method="xml"  />
<xsl:template match="/">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><xsl:value-of select="rss/channel/title"/></title>
<style type="text/css">
#banner {
    margin: 0px;
    padding: 0px 0px 0px 0px;
}
.bannerBox {
    width: 100%;
}
.bannerStatusBox {
    width: 100%;
}
.sidebarBodyHead {
    height: 25px;
}
.searchSidebarHead {
   height: 5px;
}
.searchSidebarBody {
   margin: 0px 0px 0px 0px;
}
.searchSidebarBody input {
   margin: 5px;
}
#menu {
    padding: 0px 10px 0px 10px;
}
#content {
}
#centercontent_wrap {
    float: left;
    display: inline;
    width: 100%;
}
#centercontent {
    margin: 10px;
}
#rightcontent_wrap {
    float: right;
    display: inline;
}
#rightcontent {
    margin: 10px;
}
#footer {
    clear: both;
    padding: 15px 0px 15px 0px;
    font-size: smaller;
    text-align: center;
}
.prop {
    height: 300px;
    float: right;
    width: 1px;
}
.clear {
    clear: both;
    height: 1px;
    overflow: hidden;
}
</style>
</head>
<body>	

<div id="banner">
    <div class="bannerStatusBox">   
        <table class="bannerStatusBox" cellpadding="0" cellspacing="0">
        <tr>
        <td class="bannerLeft">
            RSS 2.0
        </td>
        <td class="bannerRight">  
            <xsl:value-of select="rss/channel/generator" />
        </td>
        </tr>
        </table>    
    </div>
</div>
    
<div id="wrapper">
    <div id="leftcontent_wrap">
        <div id="leftcontent"> 
        
        </div>
    </div>
    
    <div id="centercontent_wrap">
        <div id="centercontent"> 
            
            <h1>RSS newsfeed</h1>

            <p>This page is an <a href="http://blogs.law.harvard.edu/tech/rss">RSS</a> 
            newsfeed, an XML data representation of the latest entries
            from a Roller weblog. If you have a newsfeed reader or aggregator, you can 
            subscribe to this newsfeed. To subscribe, copy the URL from your browser's 
            address bar above and copy it into your newsfeed reader.</p>
            
            <h1>Latest items in newsfeed [<xsl:value-of select="rss/channel/title"/>]</h1>

            <ol>
                <xsl:for-each select="rss/channel/item">       
                <li>
                    <h4><a><xsl:attribute name="href"><xsl:value-of select="guid"/></xsl:attribute><xsl:value-of select="title"/></a></h4>
                    Published <xsl:value-of select="pubDate"/> by <xsl:value-of select="dc:creator" />
                </li>
                </xsl:for-each>
            </ol>
            <br />      
            <hr />
            <p>To learn more about RSS visit <a href="http://blogs.law.harvard.edu/tech/rss">http://blogs.law.harvard.edu/tech/rss</a></p>

        </div>
    </div>
    
    <div id="rightcontent_wrap">
        <div id="rightcontent"> 
           <br />
        </div>
    </div>
 
</div>

<div id="footer">
   <br />
</div> 
        
<div id="datetagdiv" 
   style="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;">
</div>

</body>
</html>
</xsl:template>
</xsl:stylesheet>
