<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

<%-- Inserts page content. --%>

      <%-- If the page is an older version, then offer a note and a possibility
           to restore this version as the latest one. --%>

      <wiki:CheckVersion mode="notlatest">
         <font color="red">
            <p class="versionnote">This is version <wiki:PageVersion/>.  
            It is not the current version, and thus it cannot be edited.<br />
            <wiki:LinkTo>[Back to current version]</wiki:LinkTo>&nbsp;&nbsp;
            <wiki:EditLink version="this">[Restore this version]</wiki:EditLink></p>
         </font>
         <hr />
      </wiki:CheckVersion>

      <%-- Inserts no text if there is no page. --%>

      <wiki:InsertPage />

      <wiki:NoSuchPage>
           <!-- FIXME: Should also note when a wrong version has been fetched. -->
           This page does not exist.  Why don't you go and
           <wiki:EditLink>create it</wiki:EditLink>?
      </wiki:NoSuchPage>

      <br clear="all" />

      <wiki:HasAttachments>
         <b>Attachments:</b>

         <div class="attachments" align="center">
         <table width="90%">
         <wiki:AttachmentsIterator id="att">
             <tr>
             <td><wiki:LinkTo><%=att.getFileName()%></wiki:LinkTo></td>
             <td><wiki:PageInfoLink><img src="images/attachment_big.png" border="0" alt="Info on <%=att.getFileName()%>" /></wiki:PageInfoLink></td>
             <td><%=att.getSize()%> bytes</td>
             </tr>
         </wiki:AttachmentsIterator>
         </table>
         </div>
      </wiki:HasAttachments>

      <p><hr />
      <table border="0" width="100%" class="pageactions">
        <tr>
          <td align="left">
             <a href="#Top">Go to top</a>&nbsp;&nbsp;
             <wiki:Permission permission="edit">
                 <wiki:EditLink>Edit this page</wiki:EditLink>&nbsp;&nbsp;
             </wiki:Permission>
             <wiki:PageInfoLink>More info...</wiki:PageInfoLink>&nbsp;&nbsp;
             <a href="javascript:window.open('<wiki:UploadLink format="url" />','Upload','width=640,height=480,toolbar=1,menubar=1,scrollbars=1,resizable=1,').focus()">Attach file...</a>
             <br />
          </td>
        </tr>
        <tr>
          <td align="left">
             <font size="-1">
             
             <wiki:CheckVersion mode="latest">
                 <i>This page last changed on <wiki:DiffLink version="latest" newVersion="previous"><wiki:PageDate/></wiki:DiffLink> by <wiki:Author />.</i>
             </wiki:CheckVersion>

             <wiki:CheckVersion mode="notlatest">
                 <i>This particular version was published on <wiki:PageDate/> by <wiki:Author /></i>.
             </wiki:CheckVersion>
 
             <wiki:NoSuchPage>
                 <i>Page not created yet.</i>
             </wiki:NoSuchPage>

             </font>
          </td>
        </tr>
      </table>
      </p>
