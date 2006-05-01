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