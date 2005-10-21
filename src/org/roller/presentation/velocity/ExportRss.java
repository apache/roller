package org.roller.presentation.velocity;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.util.RegexUtil;
import org.roller.util.StringUtils;
import org.roller.util.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Does a lot of the same work as ContextLoader in preparing
 * a VelocityContext for parsing.  However, it is ignorant of
 * any HttpServletRequest related features, and so has 
 * considerably trimmed down information.
 * 
 * Created on Mar 25, 2004
 * @author lance.lavandowska
 */
public class ExportRss
{
    private VelocityEngine ve = null;
    private VelocityContext ctx = null;
    private UserData user = null;
    private boolean exportAtom;
    
    private static final String RSS_TEMPLATE = "org/roller/presentation/velocity/export_rss.vm";
    private static final String ATOM_TEMPLATE = "org/roller/presentation/velocity/export_atom.vm";
    
    public ExportRss(WebsiteData website) throws Exception
    {
        Properties props = new Properties();
        props.load(RollerContext.getServletContext().
                   getResourceAsStream("/WEB-INF/velocity.properties"));
        ve = new VelocityEngine();
        ve.info("*******************************************");
        ve.info("Initializing VelocityEngine for ExportRss");
        ve.init( props );
        ve.info("Done initializing VelocityEngine for ExportRss");
        ve.info("************************************************");
        
        ctx = new VelocityContext();
        
        RollerContext rollerCtx = RollerContext.getRollerContext(
                                      RollerContext.getServletContext());
        loadPageHelper();
        
        loadDates(website);
        
        loadWebsiteInfo(rollerCtx, website);

        loadTheRest(rollerCtx);
    }
    
    public void setExportAtom(boolean atom)
    {
        exportAtom = atom;
    }
    
    /**
     * Export the given entries using export_rss.vm.
     * 
     * @param entries
     * @throws ResourceNotFoundException
     * @throws ParseErrorException
     * @throws Exception
     */
    public void exportEntries(Collection entries, String fileName) throws ResourceNotFoundException, ParseErrorException, Exception 
    {
        ctx.put("entries", entries);
        
        String templateFile = RSS_TEMPLATE;
        if (exportAtom) templateFile = ATOM_TEMPLATE;
        Template template = ve.getTemplate( templateFile, "utf-8" );
        StringWriter sw = new StringWriter();
        template.merge(ctx, sw);
        
        writeResultsToFile((String)ctx.get("uploadPath"), sw, fileName);
    }

    /**
     * @param sw
     */
    private void writeResultsToFile(String filePath, StringWriter sw, String fileName) 
        throws RollerException, IOException
    {
        filePath += "/" + user.getUserName();
        new java.io.File( filePath ).mkdirs(); // create dir path on drive
        
        filePath += "/" + fileName;
        
        File outputFile = new java.io.File( filePath );
        FileOutputStream out = null;
        try
        {
            //outputFile.createNewFile();
            out = new FileOutputStream( outputFile );
            out.write( sw.toString().getBytes() );
            out.flush();
        }
        catch ( FileNotFoundException e )
        {
            throw new RollerException( "Unable to write to: " + outputFile.getAbsolutePath(), e );
        }
        finally
        {
            try
            {
                if ( out != null )
                {
                    out.close();
                }
            }
            catch ( java.io.IOException ioe )
            {
                System.err.println( "ExportRss unable to close OutputStream" );
            }
        }
    }

    /**
     * Load miscellaneous values into the Context.
     * @param rollerCtx
     */
    private void loadTheRest(RollerContext rollerCtx)
    {
        ctx.put("utilities",       new Utilities() );
        ctx.put("stringUtils",     new StringUtils() );        
        ctx.put("entryLength",     new Integer(-1));
    }

    /**
     * Load information pertaining to the Website and
     * its associated User.
     * @param rollerCtx
     */
    private void loadWebsiteInfo(RollerContext rollerCtx, WebsiteData website)
    {
        ctx.put("website",       website);
        ctx.put("userName",      user.getUserName() );
        ctx.put("fullName",      user.getFullName() );
        ctx.put("emailAddress",  user.getEmailAddress() );

        ctx.put("encodedEmail",  RegexUtil.encode(user.getEmailAddress()));
        ctx.put("obfuscatedEmail",  RegexUtil.obfuscateEmail(user.getEmailAddress()));

        
        // custom figureResourcePath() due to no "request" object
        StringBuffer sb = new StringBuffer();
        String uploadDir = null;
        try {
            uploadDir = RollerFactory.getRoller().getFileManager().getUploadDir();
        } catch(Exception e) {}

        ctx.put("uploadPath", uploadDir);
    }

    /**
     * Load time-related information.
     * @param website
     */
    private void loadDates(WebsiteData website)
    {
        try
        {
            // Add current time and last updated times to context
            Date updateTime = RollerFactory.getRoller().getWeblogManager()
                .getWeblogLastPublishTime(website, null);                        
            ctx.put("updateTime",   updateTime);
        }
        catch (RollerException e)
        {                       
            ctx.put("updateTime",   new Date());
        }
        ctx.put("now",              new Date());
        
        // setup Locale for future rendering
        Locale locale = website.getLocaleInstance();
        ctx.put("locale", locale);
        
        // setup Timezone for future rendering
        ctx.put("timezone", website.getTimeZoneInstance());

        // date formats need to be run through the Localized
        // SimpleDateFormat and pulled back out as localized patterns.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", locale);  
        sdf.setTimeZone( (TimeZone)ctx.get("timezone") );    
        ctx.put("plainFormat",     sdf.toLocalizedPattern());
        
        sdf.applyPattern("EEEE MMMM dd, yyyy");
        ctx.put("toStringFormat",  sdf.toLocalizedPattern());
        
        sdf.applyPattern("MMM dd yyyy, hh:mm:ss a z");
        ctx.put("timestampFormat", sdf.toLocalizedPattern());
        
        ctx.put("dateFormatter", sdf );
    }
    /**
     * Create a PageHelper.  Note that will have no values
     * necessary in parsing a Web request (such as /page) -
     * it is only useful for the restricted export_rss.vm
     * and has no PagePlugins either.  We want the exported
     * Entry.text to be the raw values.
     */
    private void loadPageHelper()
    {
        // Add Velocity page helper to context
        PageHelper pageHelper = new PageHelper(null, null, ctx);
        // set no PagePlugins - we *do not* want to render them.
        ctx.put("pageHelper", pageHelper );
    }
}
