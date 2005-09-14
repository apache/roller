<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

      <table width="100%" cellspacing="0" cellpadding="0" border="0">
         <tr>
            <td align="left">
                <h1 class="pagename">Adding comment to <wiki:PageName/></h1></td>
            <td align="right">
                <%@ include file="SearchBox.jsp" %>
            </td>
         </tr>
      </table>

      <p><hr></p>

      <wiki:InsertPage/>

      <p>
      <h3>Please enter your comments below:</h3>
      </p>

      <form action="<wiki:CommentLink format="url" />" method="POST" 
            accept-charset="<wiki:ContentEncoding />">

      <p>
      <%-- These are required parts of this form.  If you do not include these,
           horrible things will happen.  Do not modify them either. --%>

      <%-- FIXME: This is not required, is it? --%>
      <input type="hidden" name="page"     value="<wiki:PageName/>" />
      <input type="hidden" name="action"   value="save" />
      <input type="hidden" name="edittime" value="<%=pageContext.getAttribute("lastchange", PageContext.REQUEST_SCOPE )%>" />

      <%-- End of required area --%>

      <textarea class="editor" wrap="virtual" name="text" rows="15" cols="60"></textarea>

      <p>
      <label for="authorname">Your name</label>
      <input type="text" name="author" id="authorname" value="<wiki:UserName/>" />
      <label for="rememberme">Remember me?</label>
      <input type="checkbox" name="remember" id="rememberme" />
      </p>

      <p>      
      <input type="submit" name="ok" value="Save" />
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <input type="submit" name="preview" value="Preview" />
      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
      <input type="submit" name="cancel" value="Cancel" />
      </p>
      </form>

      <p>
      <wiki:NoSuchPage page="EditPageHelp">
         Ho hum, it seems that the EditPageHelp<wiki:EditLink page="EditPageHelp">?</wiki:EditLink>
         page is missing.  Someone must've done something to the installation...
      </wiki:NoSuchPage>
      </p>

      <wiki:InsertPage page="EditPageHelp" />

