/*
 * Created on May 4, 2004
 */
package org.roller.presentation;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.roller.RollerException;
import org.roller.RollerTestBase;
import org.roller.business.FileManagerTest;
import org.roller.pojos.RollerConfigData;
import org.roller.presentation.velocity.ExportRssTest;

import com.mockrunner.mock.web.MockServletContext;
 
/**
 * TODO: revisit this class once Atom 1.0 support comes to Rome
 * @author lance.lavandowska
 */
public class ArchiveParserTest extends RollerTestBase
{    
    MockServletContext mContext = null;
    RollerConfigData  rollerConfig = null;
    
    private static String FILE_LOCATION = "./build/junk/";
    private static String RSS_ARCHIVE = "export-test.rss.xml";
    private static String ATOM_ARCHIVE = "export-test.atom.xml";
    
    public void _testAtomParsing() throws RollerException 
    {
        File archiveFile = new File(FILE_LOCATION + 
                                    mWebsite.getHandle() + 
                                    "/" + ATOM_ARCHIVE);
        parseFile(archiveFile);
    }
    
    public void _testRssParsing() throws RollerException 
    {
        File archiveFile = new File(FILE_LOCATION + 
                                    mWebsite.getHandle() + 
                                    "/" + RSS_ARCHIVE);
        parseFile(archiveFile);
    }
    
    /**
     * @param archiveFile
     * @throws RollerException
     */
    private void parseFile(File archiveFile) throws RollerException
    {
        if (archiveFile.exists())
        {    
            //ArchiveParser parser = new ArchiveParser(getRoller(), mWebsite, archiveFile);
            //getRoller().begin(UserData.SYSTEM_USER);
            String result = null; // parser.parse();
            getRoller().commit();
            assertTrue(result.length() > 0);
            System.out.println(result);
        }
        else
        {
            //try again, use export test to create necessary files
            ExportRssTest exportTest = new ExportRssTest();
            try
            {
                exportTest.setUp();
                exportTest.testExportRecent();
                exportTest.tearDown();
                
                parseFile(archiveFile);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            // if it *still* isn't there, then fail
            if (!archiveFile.exists())
            {
                fail(archiveFile.getAbsolutePath() + " does not exist.");
            }
        }
    }

    /*
     * Need to change the UploadPath location for testing. 
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception
    {
        super.setUp();

        //rollerConfig = rollerContext.getRollerConfig();
    }

    /*
     * Need to reset the UploadPath after testing. 
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public static Test suite()
    {
        return new TestSuite(ArchiveParserTest.class);
    }
}
