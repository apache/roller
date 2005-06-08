package org.roller.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.roller.RollerException;
import org.roller.model.BookmarkManager;
import org.roller.model.Roller;
import org.roller.pojos.Assoc;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderAssoc;
import org.roller.pojos.FolderData;
import org.roller.pojos.WebsiteData;
import org.roller.util.Utilities;

import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Abstract base implementation using PersistenceStrategy.
 * @author Dave Johnson
 * @author Lance Lavandowska
 */
public abstract class BookmarkManagerImpl implements BookmarkManager
{
    protected Roller mRoller = null;
	protected PersistenceStrategy mStrategy; 

    private static Log mLogger =
        LogFactory.getFactory().getInstance(BookmarkManagerImpl.class);
        
    public BookmarkManagerImpl(PersistenceStrategy pstrategy, Roller roller)
    {
        mStrategy = pstrategy;
        mRoller = roller;    
    }
    
	//---------------------------------------------------------- Bookmark CRUD

    public BookmarkData createBookmark()
    {
       BookmarkData bd = new BookmarkData();
       return bd;
    }

    public BookmarkData createBookmark(
        FolderData parent, 
        String name, 
        String desc, 
        String url, 
        String feedUrl,
        Integer weight, 
        Integer priority, 
        String image)
    {
       BookmarkData bd = new BookmarkData(
           parent, name, desc, url, feedUrl, weight, priority, image);
       return bd;
    }

	public BookmarkData retrieveBookmark(String id) throws RollerException 
	{
        BookmarkData bd = (BookmarkData)
			mStrategy.load(id, BookmarkData.class);
        if (bd != null) bd.setBookmarkManager(this);
        return bd;
	}

//	public void storeBookmark(BookmarkData data) throws RollerException 
//	{
//		mStrategy.store(data);
//	}

	public void removeBookmark(String id) throws RollerException 
	{
		mStrategy.remove(id, BookmarkData.class);
	}

	//------------------------------------------------------------ Folder CRUD
    
    /** 
     * @see org.roller.model.BookmarkManager#createFolder()
     */
    public FolderData createFolder()
    {
       FolderData fd = new FolderData();
       return fd;
    }

    /** 
     * @see org.roller.model.BookmarkManager#createFolder()
     */
    public FolderData createFolder(
        FolderData parent,
        String name, 
        String desc, 
        WebsiteData website)
    {
       FolderData fd = new FolderData(parent, name, desc, website);
       return fd;
    }

	/** 
     * Retrieve folder and lazy-load it's sub-folders and bookmarks. 
     */
	public FolderData retrieveFolder(String id) throws RollerException
	{
		return (FolderData)mStrategy.load(id, FolderData.class);
	}
	
    //------------------------------------------------------------ Operations

    public void importBookmarks( 
        WebsiteData website, String folderName, String opml)
        throws RollerException
    {
        String msg = "importBookmarks";
        try
        {
            FolderData newFolder = getFolder(website, folderName);
            if (newFolder == null) 
            {
                newFolder = createFolder(
                    getRootFolder(website), folderName, folderName, website);
                newFolder.save();
            }

            // Build JDOC document OPML string
            SAXBuilder builder = new SAXBuilder();
            StringReader reader = new StringReader( opml );
            Document doc = builder.build( reader );
            
            // Iterate through children of OPML body, importing each
            Element body = doc.getRootElement().getChild("body");
            Iterator iter = body.getChildren().iterator();
            while (iter.hasNext())
            {
                Element elem = (Element)iter.next();
                importOpmlElement( website, elem, newFolder );
            }
        }
        catch (org.jdom.JDOMException e)
        {
            mLogger.error(msg,e);
            throw new RollerException(msg,e);
        }
    }

    //----------------------------------------------------------------
    private void importOpmlElement(
        WebsiteData website, Element elem, FolderData parent)
        throws RollerException
    {
        String text = elem.getAttributeValue("text");
        String title = elem.getAttributeValue("title");
        String desc = elem.getAttributeValue("description");
        String url = elem.getAttributeValue("url");
        //String type = elem.getAttributeValue("type");
        String xmlUrl = elem.getAttributeValue("xmlUrl");
        String htmlUrl = elem.getAttributeValue("htmlUrl");
        
        title =   null!=title ? title : text;
        desc =    null!=desc ? desc : title;
        xmlUrl =  null!=xmlUrl ? xmlUrl : url;
        url =     null!=htmlUrl ? htmlUrl : url;
        
        if (!elem.hasChildren() && null != title && (null != url || null != xmlUrl)  )
        {
            // Store a bookmark
            BookmarkData bd = createBookmark(
                parent,
                title,
                desc,
                url,
                xmlUrl,
                new Integer(0),
                new Integer(100),
                null);
             
            parent.addBookmark(bd);
        }
        else
        {
            // Store a folder            
            FolderData fd = createFolder(
                    parent, 
                    title, 
                    desc, 
                    parent.getWebsite());
            fd.save();
            
            // Import folder's children
            Iterator iter = elem.getChildren("outline").iterator();           
            while ( iter.hasNext() )
            {
                Element subelem = (Element)iter.next();
                importOpmlElement( website, subelem, fd  );
            }
        }
    }

    //----------------------------------------------------------------
    public void moveFolderContents(FolderData src, FolderData dest) 
        throws RollerException
    {
        // Add to destination folder
        LinkedList deleteList = new LinkedList();
        Iterator srcBookmarks = src.getBookmarks().iterator();
        while (srcBookmarks.hasNext())
        {
            BookmarkData bd = (BookmarkData)srcBookmarks.next();
            deleteList.add(bd);

            BookmarkData movedBd = new BookmarkData();
            movedBd.setData(bd);
            movedBd.setId(null);
            
            dest.addBookmark(movedBd);
        }
        
        // Remove from source folder
        Iterator deleteIter = deleteList.iterator();
        while (deleteIter.hasNext())
        {
            BookmarkData bd = (BookmarkData)deleteIter.next();
            src.removeBookmark(bd);
            removeBookmark(bd.getId());
        }        
    }

    //----------------------------------------------------------------
    public void deleteFolderContents(FolderData src) 
    throws RollerException
    {
        Iterator srcBookmarks = src.getBookmarks().iterator();
        while (srcBookmarks.hasNext())
        {
            BookmarkData bd = (BookmarkData)srcBookmarks.next();
            removeBookmark(bd.getId());

        }   
    }
    
    //---------------------------------------------------------------- Queries
    
    public FolderData getFolder(WebsiteData website, String folderPath) 
        throws RollerException
    {
        return getFolderByPath(website, null, folderPath);
    }
    
    public String getPath(FolderData folder) throws RollerException
    {
        if (null == folder.getParent())
        {
            return new String("/");
        }
        else
        {
            String parentPath = getPath(folder.getParent());
            parentPath = "/".equals(parentPath) ? "" : parentPath;
            return parentPath + "/" + folder.getName();
        }
    }

    public FolderData getFolderByPath(
        WebsiteData website, FolderData folder, String path)
        throws RollerException
    {
        final Iterator folders;
        final String[] pathArray = Utilities.stringToStringArray(path, "/");
        
        if (folder == null && (null == path || "".equals(path.trim())))  
        {
            throw new RollerException("Bad arguments.");      
        }
        
        if (path.trim().equals("/"))
        {
            return getRootFolder(website);
        }
        else if (folder == null || path.trim().startsWith("/"))
        {                   
            folders = getRootFolder(website).getFolders().iterator();
        }
        else
        {
            folders = folder.getFolders().iterator();
        }
        
        while (folders.hasNext())
        {
            FolderData possibleMatch = (FolderData)folders.next();
            if (possibleMatch.getName().equals(pathArray[0])) 
            {
                if (pathArray.length == 1) 
                {
                    return possibleMatch;
                }
                else
                {
                    String[] subpath = new String[pathArray.length - 1];
                    System.arraycopy(pathArray, 1, subpath, 0, subpath.length);
                
                    String pathString= Utilities.stringArrayToString(subpath,"/");
                    return getFolderByPath(website, possibleMatch, pathString);     
                }
            }
        }       
        
        // The folder did not match and neither did any subfolders
        return null;
    }

    //----------------------------------------------- FolderAssoc CRUD
    
    public Assoc createFolderAssoc()
    {
        return new FolderAssoc();
    }

    public Assoc createFolderAssoc(
        FolderData folder, 
        FolderData ancestor, 
        String relation) throws RollerException
    {
        return new FolderAssoc(null, folder, ancestor, relation);
    }

    public FolderAssoc retrieveFolderAssoc(String id) throws RollerException
    {
        return (FolderAssoc)mStrategy.load(id, FolderAssoc.class);
    }
}


