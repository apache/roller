<%@ page session="false"
    import="org.roller.pojos.WebsiteData,
            org.roller.presentation.RollerRequest,
            org.roller.util.DateUtil,
            java.text.DateFormat,
            java.util.Date,
            java.util.Locale,
            java.util.TimeZone"
%><style>
.details {
   font-size: 12px;
   color: grey;
   text-align: right;
}
</style><%
String msg = "You are not logged in.";
HttpSession session = request.getSession(false);
if (session != null)
{
    RollerRequest rreq = RollerRequest.getRollerRequest(request);
    if (rreq != null)
    {
        try
        {
            WebsiteData website = rreq.getWebsite();
            if (website != null)
            {
                Date creationTime = new Date( session.getCreationTime() );
                Date accessedTime = new Date( session.getLastAccessedTime() );
                DateFormat sdf = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM, DateFormat.FULL, website.getLocaleInstance() );
                String creationStr = sdf.format( creationTime );
                String accessedStr = sdf.format( accessedTime );
                msg = "You have been logged in since " + creationStr + ".  ";
                msg += "Last access was at " + accessedStr + ".";
            }
        }
        catch (Exception e)
        {
            msg = "You are currently logged in.";
        }
    }
}
%><span class="details"><%= msg %></span>
<script>
    setTimeout("reload()",900000); // 15 minutes
    function reload()
    {
        self.location = "keepalive.jsp";
    }
</script>