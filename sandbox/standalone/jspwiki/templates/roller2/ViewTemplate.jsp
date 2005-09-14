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
           <img class="bannerlogo" src='<wiki:BaseURL/>/templates/roller/tan/logo.gif' />
        </div>
        <div class="bannerStatusBox">
           <div class="bannerLeft">
              Your trail: <wiki:Breadcrumbs maxpages="5"  />
           </div>
           <div class="bannerRight">
              <a href="/main.do">Roller</a> 
              ||<a href="/planet.do">Planet</a> 
              | <a href="/wiki">Wiki</a>
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

