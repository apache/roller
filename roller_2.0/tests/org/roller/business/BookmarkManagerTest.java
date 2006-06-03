
package org.roller.business;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
 
import org.roller.RollerException;
import org.roller.RollerPermissionsException;
import org.roller.model.BookmarkManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WebsiteData;
import org.roller.util.Utilities;
import org.roller.RollerTestBase;

/**
 * Test Roller Bookmark Management.
 */
public class BookmarkManagerTest extends RollerTestBase
{
    //------------------------------------------------------------------------
    public BookmarkManagerTest()
    {
        super();
    }

    //------------------------------------------------------------------------
    public BookmarkManagerTest(String name)
    {
        super(name);
    }

    //------------------------------------------------------------------------
    public static Test suite()
    {
        return new TestSuite(BookmarkManagerTest.class);
    }

    //------------------------------------------------------------------------
    public static void main(String args[])
    {
        junit.textui.TestRunner.run( BookmarkManagerTest.class );
    }

    //------------------------------------------------------------------------

    public void testAddBookmarkToFolder() throws RollerException
    {
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        FolderData folder = null;
        BookmarkData bookmark1 = null, bookmark2 = null;

        getRoller().begin(UserData.SYSTEM_USER);

        WebsiteData website =
            getRoller().getUserManager().retrieveWebsite(mWebsite.getId());
        FolderData root = bmgr.getRootFolder(website);

        folder = bmgr.createFolder();
        folder.setName("TestFolder1");
        folder.setDescription("created by testAddBookmarkToFolder()");
        folder.setWebsite(website);
        folder.setParent(root);
        folder.save();

        // Add bookmark by adding to folder
        bookmark1 = bmgr.createBookmark(
            folder,
            "TestBookmark1",
            "created by testAddBookmarkToFolder()",
            "http://www.example.com",
            "http://www.example.com/rss.xml",
            new Integer(1),
            new Integer(12),
            "test.jpg");
        folder.addBookmark(bookmark1);

        // Add another bookmark
        bookmark2 = bmgr.createBookmark(
            folder,
            "TestBookmark2",
            "created by testAddBookmarkToFolder()",
            "http://www.example.com",
            "http://www.example.com/rss.xml",
            new Integer(1),
            new Integer(12),
            "test.jpf");
        folder.addBookmark(bookmark2);

        getRoller().commit();

        FolderData testFolder = null;
        BookmarkData bookmarkb = null, bookmarka = null;

        getRoller().begin(UserData.SYSTEM_USER);
        // See that two bookmarks were stored
        testFolder = bmgr.retrieveFolder(folder.getId());
        assertEquals(2, testFolder.getBookmarks().size());
        bookmarka = (BookmarkData)testFolder.getBookmarks().iterator().next();
        bookmarkb = (BookmarkData)testFolder.getBookmarks().iterator().next();
        // Remove one bookmark
        testFolder.removeBookmark(bookmarka);
        bmgr.removeBookmark(bookmarka.getId());
        getRoller().commit();

        getRoller().begin(UserData.SYSTEM_USER);
        // Folder should now contain one bookmark
        testFolder = bmgr.retrieveFolder(folder.getId());
        assertEquals(1, testFolder.getBookmarks().size());
        getRoller().release();

        // Remove folder
        getRoller().begin(UserData.SYSTEM_USER);
        testFolder = bmgr.retrieveFolder(folder.getId());
        testFolder.remove();
        getRoller().commit();

        getRoller().begin(UserData.SYSTEM_USER);
        // Folder and one remaining bookmark should be gone
        assertNull( bmgr.retrieveBookmark(bookmarkb.getId()) );
        assertNull( bmgr.retrieveFolder(folder.getId()) );
        getRoller().release();
    }

    //------------------------------------------------------------------------
    public void testBookmarkImport() throws Exception
    {
        importBookmarks("/bookmarks.opml");
    }

    //------------------------------------------------------------------------
    public void importBookmarks(String fileName) throws Exception
    {
        getRoller().begin(UserData.SYSTEM_USER);
        InputStream fis = this.getClass().getResourceAsStream(fileName);
        getRoller().getBookmarkManager().importBookmarks(
            mWebsite, "ZZZ_imports_ZZZ", fileToString(fis));
        getRoller().commit();
        getRoller().release();

        FolderData fd = null;

        getRoller().begin(UserData.SYSTEM_USER);
        fd = getRoller().getBookmarkManager().getFolder(mWebsite, "ZZZ_imports_ZZZ");
        assertTrue("no bookmarks found", fd.retrieveBookmarks(true).size() > 0 );
        fd.remove();
        getRoller().commit();
    }

    //------------------------------------------------------------------------
    public String fileToString( InputStream is ) throws java.io.IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s = null;
        StringBuffer sb = new StringBuffer();
        while ( (s=br.readLine()) != null )
        {
            sb.append( s );
        }
        return sb.toString();
    }

    //------------------------------------------------------------------------

    /**
     * Creates folder tree like this:
     *    root/
     *       dest/
     *       f1/
     *          b1
     *          f2/
     *             f3/
     */
    public void testMoveFolderContents() throws RollerException
    {
        WebsiteData wd = null;
        BookmarkManager bmgr = getRoller().getBookmarkManager();

        getRoller().begin(UserData.SYSTEM_USER);

        wd = getRoller().getUserManager().retrieveWebsite(mWebsite.getId());
        FolderData root = bmgr.getRootFolder(wd);

        FolderData dest = bmgr.createFolder();
        dest.setName("dest");
        dest.setParent(root);
        dest.setWebsite(wd);
        dest.save();

        // create source folder f1
        FolderData f1 = bmgr.createFolder();
        f1.setName("f1");
        f1.setParent(root);
        f1.setWebsite(wd);
        f1.save();

        // create bookmark b1 inside source folder f1
        BookmarkData b1 = bmgr.createBookmark(
            f1, "b1", "testbookmark",
            "http://example.com", "http://example.com/rss",
            new Integer(1), new Integer(1), "image.gif");
        b1.save();

        // create folder f2 inside f1
        FolderData f2 = bmgr.createFolder();
        f2.setName("f2");
        f2.setParent(f1);
        f2.setWebsite(wd);
        f2.save();

        // create bookmark b2 inside folder f2
        BookmarkData b2 = bmgr.createBookmark(
            f2, "b2", "testbookmark",
            "http://example.com", "http://example.com/rss",
            new Integer(1), new Integer(1), "image.gif");
        b2.save();

        // create folder f3 inside folder f2
        FolderData f3 = bmgr.createFolder();
        f3.setName("f3");
        f3.setParent(f2);
        f3.setWebsite(wd);
        f3.save();

        // crete bookmark b3 inside folder f3
        BookmarkData b3 = bmgr.createBookmark(
            f3, "b3", "testbookmark",
            "http://example.com", "http://example.com/rss",
            new Integer(1), new Integer(1), "image.gif");
        b3.save();

        getRoller().commit();
        
        // test that parent cannot be moved into child
        boolean safe = false;
        try 
        {
            getRoller().begin(UserData.SYSTEM_USER);     
                   
            // Move folder into one of it's children
            f1 = bmgr.retrieveFolder(f1.getId());       
            f3 = bmgr.retrieveFolder(f3.getId());       
            bmgr.moveFolderContents(f1, f3);
            f3.save();
            f1.save();
            getRoller().commit(); 
        }
        catch (RollerException e)
        {
            safe = true;
        }
        assertTrue(safe);

        getRoller().begin(UserData.SYSTEM_USER);

        // verify number of entries in each folder
        dest = bmgr.retrieveFolder(dest.getId());
        f1 = bmgr.retrieveFolder(f1.getId());
        assertEquals(0, dest.retrieveBookmarks(true).size());
        assertEquals(0, dest.retrieveBookmarks(false).size());
        assertEquals(1, f1.retrieveBookmarks(false).size());

        List f1list = f1.retrieveBookmarks(true);
        assertEquals(3, f1list.size());

        // move contents of source category c1 to destination catetory dest
        f1.moveContents(dest);

        getRoller().commit();



        getRoller().begin(UserData.SYSTEM_USER);

        // after move, verify number of entries in eacch folder
        dest = bmgr.retrieveFolder(dest.getId());
        f1 = bmgr.retrieveFolder(f1.getId());
        assertEquals(3, dest.retrieveBookmarks(true).size());
        assertEquals(3, dest.retrieveBookmarks(false).size());
        assertEquals(0, f1.retrieveBookmarks(true).size());
        assertEquals(0, f1.retrieveBookmarks(false).size());

        getRoller().commit();
    }

    /** Test bookmark folder paths. */
    public void testPaths() throws Exception
    {
        WebsiteData wd = null;
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        try
        {
            getRoller().begin(UserData.SYSTEM_USER);

            wd = getRoller().getUserManager().retrieveWebsite(mWebsite.getId());
            FolderData root = bmgr.getRootFolder(wd);

            FolderData f1 = bmgr.createFolder();
            f1.setName("f1");
            f1.setParent(root);
            f1.setWebsite(wd);
            f1.save();

            FolderData f2 = bmgr.createFolder();
            f2.setName("f2");
            f2.setParent(f1);
            f2.setWebsite(wd);
            f2.save();

            FolderData f3 = bmgr.createFolder();
            f3.setName("f3");
            f3.setParent(f2);
            f3.setWebsite(wd);
            f3.save();

            getRoller().commit();
        }
        catch (RollerException e)
        {
            getRoller().release();
            mLogger.error(e);
        }

        try
        {
            getRoller().begin(UserData.SYSTEM_USER);

            assertEquals("f1",bmgr.getFolderByPath(wd, null, "/f1").getName());
            assertEquals("f2",bmgr.getFolderByPath(wd, null, "/f1/f2").getName());
            assertEquals("f3",bmgr.getFolderByPath(wd, null, "/f1/f2/f3").getName());

            FolderData f3 = bmgr.getFolderByPath(wd, null, "/f1/f2/f3");
            String pathString = bmgr.getPath(f3);
            String[] pathArray = Utilities.stringToStringArray(pathString,"/");
            assertEquals("f1", pathArray[0]);
            assertEquals("f2", pathArray[1]);
            assertEquals("f3", pathArray[2]);

            getRoller().release();
        }
        catch (RollerException e)
        {
            getRoller().release();
            mLogger.error(e);
        }
    }

    /** Ensure that duplicate folder name will throw RollerException */
    public void testUniquenessOfFolderNames() throws Exception
    {
        boolean exception = false;
        WebsiteData wd = null;
        FolderData f3 = null;
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        try
        {
            getRoller().begin(UserData.SYSTEM_USER);

            wd = getRoller().getUserManager().retrieveWebsite(mWebsite.getId());
            FolderData root = bmgr.getRootFolder(wd);

            FolderData f1 = bmgr.createFolder();
            f1.setName("f1");
            f1.setParent(root);
            f1.setWebsite(wd);
            f1.save();

            // first child folder
            FolderData f2 = bmgr.createFolder();
            f2.setName("f2");
            f2.setParent(f1);
            f2.setWebsite(wd);
            f2.save();

            getRoller().commit();

            getRoller().begin(UserData.SYSTEM_USER);

            // child folder with same name as first
            f3 = bmgr.createFolder();
            f3.setName("f2");
            f3.setParent(f1);
            f3.setWebsite(wd);
            f3.save();

            getRoller().commit();
        }
        catch (RollerException e)
        {
            exception = true;
        }

        assertTrue(exception);
    }


}







