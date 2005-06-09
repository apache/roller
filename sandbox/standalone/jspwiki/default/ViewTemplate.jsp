<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
        "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
  <title><wiki:Variable var="applicationname" />: <wiki:PageName /></title>
  <%@ include file="cssinclude.js" %>
  <script src="templates/<wiki:TemplateDir />/search_highlight.js" type="text/javascript"></script>
  <link rel="stylesheet" type="text/css" media="print" href="templates/<wiki:TemplateDir/>/jspwiki_print.css">
  <link rel="alternate stylesheet" type="text/css" href="templates/<wiki:TemplateDir/>/jspwiki_print.css" title="Print friendly">
  <wiki:RSSLink />
</head>

<body bgcolor="#FFFFFF">

<div id="header">
            <div style="float:right"><%@ include file="SearchBox.jsp" %></div>
            <h1 class="pagename"><a name="Top"><wiki:PageName/></a></h1>
            Your trail: <wiki:Breadcrumbs />
</div>

<div id="left">
       <%@ include file="LeftMenu.jsp" %>
       <p>
       <wiki:CheckRequestContext context="view">
          <wiki:Permission permission="edit">
             <wiki:EditLink>Edit this page</wiki:EditLink>
          </wiki:Permission>
       </wiki:CheckRequestContext>
       </p>
       <%@ include file="LeftMenuFooter.jsp" %>
       <p>
           <div align="center">
           <wiki:RSSImageLink title="Aggregate the RSS feed" /><br />
           <wiki:RSSUserlandLink title="Aggregate the RSS feed in Radio Userland!" />
           </div>
       </p>
</div>

<div id="content">

      <wiki:CheckRequestContext context="view">
         <wiki:Include page="PageContent.jsp" />
      </wiki:CheckRequestContext>

      <wiki:CheckRequestContext context="diff">
         <wiki:Include page="DiffContent.jsp" />
      </wiki:CheckRequestContext>

      <wiki:CheckRequestContext context="info">
         <wiki:Include page="InfoContent.jsp" />
      </wiki:CheckRequestContext>

      <wiki:CheckRequestContext context="preview">
         <wiki:Include page="PreviewContent.jsp" />
      </wiki:CheckRequestContext>

      <wiki:CheckRequestContext context="conflict">
         <wiki:Include page="ConflictContent.jsp" />
      </wiki:CheckRequestContext>

      <wiki:CheckRequestContext context="find">
         <wiki:Include page="FindContent.jsp" />
      </wiki:CheckRequestContext>

      <wiki:CheckRequestContext context="prefs">
         <wiki:Include page="PreferencesContent.jsp" />
      </wiki:CheckRequestContext>

      <wiki:CheckRequestContext context="error">
         <wiki:Include page="DisplayMessage.jsp" />
      </wiki:CheckRequestContext>

</div>

</body>

</html>

