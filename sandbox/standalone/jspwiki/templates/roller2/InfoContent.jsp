<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<wiki:PageExists>

   <form action="<wiki:Variable var="baseurl"/>Delete.jsp" method="POST" 
                 accept-charset="<wiki:ContentEncoding />">

   <table cellspacing="4">
       <tr>
           <td><b>Page name</b></td>
           <td><wiki:LinkTo><wiki:PageName /></wiki:LinkTo></td>
       </tr>

       <wiki:PageType type="attachment">
           <tr>
              <td><b>Parent page</b></td>
              <td><wiki:LinkToParent><wiki:ParentPageName /></wiki:LinkToParent></td>
           </tr>
       </wiki:PageType>

       <tr>
           <td><b>Page last modified</b></td>
           <td><wiki:PageDate /></td>
       </tr>

       <tr>
           <td><b>Current page version</b></td>
           <td><wiki:PageVersion>No versions.</wiki:PageVersion></td>
       </tr>

       <tr>
           <td valign="top"><b>Page revision history</b></td>
           <td>
               <table border="1" cellpadding="4">
                   <tr>
                        <th>Version</th>                        
                        <th>Date <wiki:PageType type="page">(and differences to current)</wiki:PageType></th>
                        <th>Author</th>
                        <th>Size</th>
                        <wiki:PageType type="page">                        
                            <th>Changes from previous</th>
                        </wiki:PageType>
                        <wiki:Permission permission="delete">
                            <th>Delete</th>
                        </wiki:Permission>
                   </tr>
                   <wiki:HistoryIterator id="currentPage">
                     <tr>
                         <td>
                             <wiki:LinkTo version="<%=Integer.toString(currentPage.getVersion())%>">
                                  <wiki:PageVersion/>
                             </wiki:LinkTo>
                         </td>

                         <td>
                             <wiki:PageType type="page">
                             <wiki:DiffLink version="latest" 
                                            newVersion="<%=Integer.toString(currentPage.getVersion())%>">
                                 <wiki:PageDate/>
                             </wiki:DiffLink>
                             </wiki:PageType>

                             <wiki:PageType type="attachment">
                                 <wiki:PageDate/>
                             </wiki:PageType>
                         </td>

                         <td><wiki:Author /></td>
                         <td><wiki:PageSize /></td>

                         <wiki:PageType type="page">
                           <td>
                              <% if( currentPage.getVersion() > 1 ) { %>
                                   <wiki:DiffLink version="<%=Integer.toString(currentPage.getVersion())%>" 
                                                  newVersion="<%=Integer.toString(currentPage.getVersion()-1)%>">
                                       from version <%=currentPage.getVersion()-1%> to <%=currentPage.getVersion()%>
                                   </wiki:DiffLink>
                               <% } %>
                           </td>
                         </wiki:PageType>

                         <wiki:Permission permission="delete">
                             <td>
                                 <input type="checkbox" name="delete-<%=currentPage.getVersion()%>" />
                             </td>
                         </wiki:Permission>
                     </tr>
                   </wiki:HistoryIterator>
               </table>
           </td>
      </tr>
</table>

    <wiki:Permission permission="delete">
       <input type="submit" name="delete" value="Delete marked versions"/>
    </wiki:Permission>

    </form>
             
    <br />
    <wiki:PageType type="page">
       <wiki:LinkTo>Back to <wiki:PageName/></wiki:LinkTo>
    </wiki:PageType>
    <wiki:PageType type="attachment">

       <form action="<wiki:Variable var="baseurl"/>attach" method="POST" enctype="multipart/form-data">

           <%-- Do NOT change the order of wikiname and content, otherwise the 
                servlet won't find its parts. --%>

           <input type="hidden" name="page" value="<wiki:Variable var="pagename"/>" />

           In order to update this attachment with a newer version, find the
           file using "Browse", then click on "Update".

           <p>
           <input type="file" name="content" />
           <input type="submit" name="upload" value="Update" />
           <input type="hidden" name="action" value="upload" />
           <input type="hidden" name="nextpage" value="<wiki:PageInfoLink format="url"/>" />
           </p>
           </form>


    </wiki:PageType>

</wiki:PageExists>


<wiki:NoSuchPage>
    This page does not exist.  Why don't you go and
    <wiki:EditLink>create it</wiki:EditLink>?
</wiki:NoSuchPage>

