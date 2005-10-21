<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>

      <p>
      This is a page which allows you to set up all sorts of interesting things.
      You need to have cookies enabled for this to work, though.
      </p>

      <form action="<wiki:Variable var="baseURL"/>UserPreferences.jsp" 
            method="POST"
            accept-charset="UTF-8">

         <b>User name:</b> <input type="text" name="username" size="30" value="<wiki:UserName/>" />
         <i>This must be a proper WikiName, no punctuation.</i>
         <br /><br />
         <input type="submit" name="ok" value="Set my preferences!" />
         <input type="hidden" name="action" value="save" />
      </form>

      <hr />

      <h3>Removing your preferences</h3>

      <p>In some cases, you may need to remove the above preferences from the computer.
      Click the button below to do that.  Note that it will remove all preferences
      you've set up, permanently.  You will need to enter them again.</p>

      <div align="center">
      <form action="<wiki:Variable var="baseURL"/>UserPreferences.jsp"
            method="POST"
            accept-charset="UTF-8">
      <input type="submit" name="clear" value="Remove preferences from this computer" />
      </form>
      </div>
