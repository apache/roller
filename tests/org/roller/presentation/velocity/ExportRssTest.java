/*
 * Created on Mar 25, 2004
 */
package org.roller.presentation.velocity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.roller.model.WeblogManager;
import org.roller.pojos.UserData;
import org.roller.presentation.StrutsActionTestBase;

import com.mockrunner.mock.web.MockServletContext;

/**
 * Not really a servlet test, the ExportRss class does require
 * that RollerContext has been loaded and initialized.
 * 
 * @author lance.lavandowska
 */
public class ExportRssTest extends StrutsActionTestBase
{    
    private String oldUploadPath;
    
    /**
     * Not sure how to test that the file was generated, so if
     * there was no Exception we passed.
     */
    public void testExportRecent() throws Exception
    {      
        getRoller().begin(UserData.SYSTEM_USER);
        ExportRss exporter = new ExportRss(mWebsite);
        
        //List entries = getRoller().getWeblogManager().getAllRecentWeblogEntries(new Date(), 5);
        
        List entries = getRoller().getWeblogManager().getWeblogEntries(
                        null,               // userName
                        null,               // startDate
                        new Date(),         // endDate
                        null,               // catName
                        null,               // status
                        null,               // sortby
                        new Integer(5));    // maxEntries
        
        try
        {
            // test RSS output
            exporter.exportEntries(entries, "export-test.rss.xml");

            // test Atom output
            exporter.setExportAtom(true);
            exporter.exportEntries(entries, "export-test.atom.xml");
        }
        catch (Exception e)
        {
            mLogger.error(e);
            // I'm not sure how best to test the output!
            // I guess no exceptions will have to do.
            fail("Find a better way to test than checking Exceptions, bozo!");
        }
        finally 
        {
            getRoller().begin(UserData.SYSTEM_USER);
        }
    }
    
    /*
     * Need to change the UploadPath location for testing. 
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception
    {
        super.setUp();

        getRoller().begin(UserData.SYSTEM_USER);
        //RollerConfigData  rollerConfig = rollerContext.getRollerConfig();
        //oldUploadPath = rollerConfig.getUploadPath();
        //rollerConfig.setUploadPath("build/junk");
        //rollerConfig.save();
        getRoller().commit();        

        MockServletContext mContext = getMockFactory().getMockServletContext();
        mContext.setRealPath("build/junk", "./build/junk");
        
        setupVelocityProperties(mContext);
    }
    
    /**
     * ExportRss needs to load velocity.properties from ServletContext.
     * For the mock implementation we need to set the 'resource stream'.
     * @param mContext
     * @throws IOException
     */
    private void setupVelocityProperties(MockServletContext mContext) throws IOException
    {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("velocity.properties");
        if (is == null) fail("Unable to find velocity.properties");
        BufferedInputStream bis = new BufferedInputStream(is, 1);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int buf = bis.read();
        do
        {
            bos.write(buf);
            buf = bis.read();
        }
        while(buf != -1);
        
        byte[] bytes = bos.toByteArray();
        mContext.setResourceAsStream("/WEB-INF/velocity.properties", bytes);
        try
        {
            bos.close();
            bis.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /*
     * Need to reset the UploadPath after testing. 
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
        //getRoller().begin(UserData.SYSTEM_USER);
        //RollerConfigData  rollerConfig = rollerContext.getRollerConfig();
        //rollerConfig.setUploadPath(oldUploadPath);
        //rollerConfig.save();
        //getRoller().commit();
        
        super.tearDown();
    }

    public static Test suite() 
    {
        return new TestSuite(ExportRssTest.class);
    }
}
