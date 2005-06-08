/*
 * Created on May 4, 2004
 */
package org.roller.presentation;

import com.mockrunner.mock.web.MockServletContext;

import org.roller.RollerException;
import org.roller.pojos.RollerConfig;

import java.io.File;

/**
 * @author lance.lavandowska
 */
public class ArchiveParserTest extends ServletTestBase
{    
    MockServletContext mContext = null;
    RollerConfig  rollerConfig = null;
    
    private static String FILE_LOCATION = "./build/junk/";
    private static String RSS_ARCHIVE = "export-test.rss.xml";
    private static String ATOM_ARCHIVE = "export-test.atom.xml";
    
    public void testAtomParsing() throws RollerException 
    {
        File archiveFile = new File(FILE_LOCATION + 
                                    mWebsite.getUser().getUserName() + 
                                    "/" + ATOM_ARCHIVE);
        parseFile(archiveFile);
    }
    
    public void testRssParsing() throws RollerException 
    {
        File archiveFile = new File(FILE_LOCATION + 
                                    mWebsite.getUser().getUserName() + 
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
            ArchiveParser parser = new ArchiveParser(getRoller(), mWebsite, archiveFile);
            //getRoller().begin();
            String result = parser.parse();
            getRoller().commit();
            assertTrue(result.length() > 0);
            System.out.println(result);
        }
        else
        {
            fail(archiveFile.getAbsolutePath() + " does not exist.");
        }
    }

    /*
     * Need to change the UploadPath location for testing. 
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception
    {
        super.setUp();

        rollerConfig = rollerContext.getRollerConfig();
    }

    /*
     * Need to reset the UploadPath after testing. 
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    

}
