/*
 * Created on Apr 2, 2004
 */
package org.roller.presentation;


/**
 * TODO: revisit this class once Atom 1.0 support comes to Rome
 * @author lance.lavandowska
 */
public class ArchiveParser
{
//    protected static final Log mLogger = 
//        LogFactory.getFactory().getInstance(ArchiveParser.class);
//    
//    private Roller roller;
//    private WebsiteData website;
//    private File archiveFile;
//
//    private Timestamp current;
//
//    private WeblogCategoryData defaultCategory;
//
//    private WeblogCategoryData rootCategory;
//
//    private IndexManager indexMgr;
// 
//
//    /**
//     * @param rreq
//     * @param f
//     */
//    public ArchiveParser(Roller roller, WebsiteData website, File f) throws RollerException
//    {
//        this.roller = roller;
//        this.website = website;
//        archiveFile = f;
//        
//        current = new Timestamp( System.currentTimeMillis());
//        defaultCategory = website.getDefaultCategory();
//        rootCategory = roller.getWeblogManager().getRootWeblogCategory(website);
//        indexMgr = roller.getIndexManager();
//    }
//
//    public String parse() throws RollerException
//    {        
//        StringBuffer buf = new StringBuffer();
//        
//        // parse file and convert to WeblogEntryDatas
//        Feed atomFeed = getAtomFeed();
//        if (atomFeed != null)
//        {    
//            importAtom(buf, atomFeed);
//        }
//        else
//        {    
//            // not an Atom feed, try RSS
//            ChannelIF channel = getInformaChannel();
//            
//            if (channel != null && channel.getItems()!= null)
//            {
//                importRSS(buf, channel);
//            }
//        }
//        
//        return buf.toString();
//    }
//
//    /**
//     * @return
//     * @throws FileNotFoundException
//     * @throws IOException
//     */
//    private Feed getAtomFeed()
//    {
//        Feed feed = null;
//        BufferedInputStream bis = null;
//        try
//        {
//            FileInputStream fis = new FileInputStream(archiveFile);
//            bis = new BufferedInputStream(fis);
//            // we need AtomFeedReader for Roller-specific elements
//            AtomFeedReader reader = new AtomFeedReader(bis);
//            // current 'version' of Atom4J parses on init, next version won't
//            if (reader.getFeed() == null) 
//            {
//                reader.parse(); 
//            }
//            feed = reader.getFeed();
//        }
//        catch (FileNotFoundException e)
//        {
//            mLogger.debug("You told me to read a non-existant file.", e);
//        }
//        catch (IOException e)
//        {
//            mLogger.debug("Digester throws IOException for no reason I can tell.", e);
//        }
//        finally
//        {
//            try
//            {
//                if (bis != null) bis.close();
//            }
//            catch (IOException e1)
//            {
//                mLogger.error("Unable to close stream to " + archiveFile);
//            }
//        }
//        return feed;
//    }
//
//    /**
//     * @param channel
//     * @return
//     */
//    private ChannelIF getInformaChannel()
//    {
//        ChannelIF channel = null;
//        BufferedInputStream bis = null;
//        try
//        {
//            FileInputStream fis = new FileInputStream(archiveFile);
//            bis = new BufferedInputStream(fis);
//            channel = RSSParser.parse(new ChannelBuilder(), bis);
//        }
//        catch (FileNotFoundException e)
//        {
//            e.printStackTrace();
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        catch (ParseException e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            try
//            {
//                if (bis != null) bis.close();
//            }
//            catch (IOException e1)
//            {
//                mLogger.error("Unable to close stream to " + archiveFile);
//            }
//        }
//        return channel;
//    }
//
//    /**
//     * 
//     */
//    private void importAtom(StringBuffer buf, Feed atomFeed) throws RollerException
//    {
//        AtomEntry atomEntry;
//        WeblogEntryData entry = null;
//        HashMap entryMap = new HashMap(); // map of Roller entries
//        WeblogCategoryData category;
//        HashMap categoryMap = new HashMap();
//        categoryMap.put("defaultCategory", defaultCategory);
//        Collection entries = atomFeed.getEntries();
//        if (entries != null)
//        {
//            Iterator it = entries.iterator();
//            while (it.hasNext())
//            {
//                entry = null; //reset
//
//                // create new Entry from AtomEntry
//                atomEntry = (AtomEntry)it.next();
//
//                // test to see if this Entry is a Comment (it's
//                // parent should already exist).
//                /* Added by Roller's AtomEntry */
//                if (atomEntry.getAnnotation() != null)
//                {
//                    createComment(atomEntry, entryMap);
//                    continue;
//                }
//
//                if (atomEntry.getId() != null)
//                {
//                    entry = roller.getWeblogManager().retrieveWeblogEntry(atomEntry.getId());
//                }
//                if (entry == null)
//                {
//                    category = null;
//                    /* Atom doesn't currently have a Category definition.
//                     Added by Roller's AtomEntry */
//                    // return WeblogCategoryData for getCategories
//                    if (atomEntry.getCategories() != null)
//                    {
//                        Iterator cIt = atomEntry.getCategories().iterator();
//                        if (cIt.hasNext())
//                        {
//                            String catPath = (String)cIt.next();
//                            category = locateCategory(catPath, categoryMap);
//                        }
//                    }
//                    if (category == null) category = defaultCategory;
//
//                    entry = entryFromAtom(buf, atomEntry, entryMap, category);
//
//                    indexEntry(entry);
//                }
//                else
//                {
//                    entryMap.put(entry.getId(), entry);
//                    buf.append("An Entry already exists for id: " + atomEntry.getId() + ".<br />");
//                }
//            }
//        }
//    }
//
//    /**
//     * Convert an AtomEntry to a WeblogEntryData.
//     * @param buf
//     * @param atomEntry
//     * @param entryMap
//     * @param category
//     * @return
//     * @throws RollerException
//     */
//    private WeblogEntryData entryFromAtom(StringBuffer buf, AtomEntry atomEntry, HashMap entryMap, WeblogCategoryData category) throws RollerException
//    {        
//        System.out.println(atomEntry);
//        String title = atomEntry.getTitle().getText();
//        String content = "";
//        Date issued = new Date(current.getTime());
//        Date modified = new Date(current.getTime());     
//        String id = atomEntry.getId();
//        if (atomEntry.getContent() != null) 
//        {
//            content = atomEntry.getContent().getText();
//        }
//        if (atomEntry.getIssued() != null) issued = atomEntry.getIssued();
//        if (atomEntry.getModified()!= null) modified = atomEntry.getModified();
//        
//        WeblogEntryData entry = new WeblogEntryData(
//            null, category, website, 
//            title, (String)null, 
//            content, (String)null, 
//            new Timestamp(issued.getTime()),
//            new Timestamp(modified.getTime()), 
//            Boolean.TRUE);
//        entry.save();
//        // store entry in local cache for Comments' to lookup
//        if (id == null) id = entry.getId();
//        entryMap.put(id, entry);
//        
//        buf.append("\"").append(title).append("\" imported.<br />\n");
//        return entry;
//    }
//
//    /**
//     * @param atomEntry
//     * @param entryMap
//     */
//    private void createComment(AtomEntry atomEntry, HashMap entryMap) throws RollerException
//    {
//        // first try to get the Entry from local cache
//        CommentData comment = roller.getWeblogManager().retrieveComment(atomEntry.getId());
//        if (comment == null)
//        {    
//            String entryId = atomEntry.getAnnotation();
//            WeblogEntryData entry = (WeblogEntryData) entryMap.get(entryId);
//            if (entry == null)
//            {
//                // now try getting it from database
//                entry = roller.getWeblogManager().retrieveWeblogEntry(entryId);
//            }
//            if (entry != null)
//            {    
//                comment = new CommentData(
//                    null, 
//                    entry, 
//                    atomEntry.getAuthor().getName(), 
//                    atomEntry.getAuthor().getEmail(), 
//                    atomEntry.getAuthor().getUrl(), 
//                    atomEntry.getContent().getText(), 
//                    new Timestamp(atomEntry.getIssued().getTime()), 
//                    Boolean.FALSE, Boolean.FALSE);
//                comment.save();
//            }
//            else
//            {
//                mLogger.warn("Unable to find parent WeblogEntry for id: " + entryId +
//                             ".\n\tComment not created: " + atomEntry.getTitle().getText());
//            }
//        }
//        else
//        {
//            mLogger.info("A Comment already exists for id: " + atomEntry.getId());
//        }
//    }
//
//    /**
//     * @param rreq
//     * @param buf
//     * @param channel
//     * @throws RollerException
//     */
//    private void importRSS(StringBuffer buf, ChannelIF channel) throws RollerException
//    {       
//        ItemIF item;
//        WeblogEntryData entry = null;
//        WeblogCategoryData category;
//        HashMap categoryMap = new HashMap();
//        categoryMap.put("defaultCategory", defaultCategory);
//        Iterator it = channel.getItems().iterator();
//        while (it.hasNext())
//        {
//            entry = null; //reset
//            item = (ItemIF)it.next();
//            
//            if (item.getGuid() != null && !item.getGuid().isPermaLink())
//            {
//                entry = roller.getWeblogManager().retrieveWeblogEntry(item.getGuid().getLocation());
//            }
//            
//            if (entry == null)
//            {    
//                category = null;
//                // return WeblogCategoryData for getCategories
//                if (item.getCategories() != null)
//                {
//                    Iterator cIt = item.getCategories().iterator();
//                    if (cIt.hasNext()) 
//                    {
//                        // see if we've already created a category for this String
//                        CategoryIF catIF = (CategoryIF)cIt.next();
//                        category = locateCategory(catIF.getTitle(), categoryMap);
//                    }
//                }
//                if (category == null) category = defaultCategory;
//                
//                entry = entryFromRSS(buf, item, category);
//                
//                indexEntry(entry);
//            }
//            else
//            {
//                buf.append("An Entry already exists for id: " + entry.getId() + ".<br />");
//            }
//        }
//    }
//
//    /**
//     * @param entry
//     */
//    private void indexEntry(WeblogEntryData entry) throws RollerException
//    {
//        // index the new Entry
//        indexMgr.addEntryIndexOperation(entry);
//    }
//
//    /**
//     * Convert an RSS Item to a WeblogEntryData.
//     * @param buf
//     * @param item
//     * @param category
//     * @return
//     * @throws RollerException
//     */
//    private WeblogEntryData entryFromRSS(StringBuffer buf, ItemIF item, WeblogCategoryData category) throws RollerException
//    {
//        WeblogEntryData entry;
//        // make sure there is an item date
//        if (item.getDate() == null)
//        {
//            item.setDate(new Date(current.getTime()));
//        }
//        
//        entry = new WeblogEntryData(
//            (String)null, category, website, 
//            item.getTitle(), (String)null, 
//            item.getDescription(), (String)null, 
//            new Timestamp(item.getDate().getTime()),
//            new Timestamp(item.getDate().getTime()), 
//            Boolean.TRUE);
//        entry.save();
//        buf.append("\"").append(item.getTitle()).append("\" imported.<br />\n");
//        return entry;
//    }
//
//    /**
//     * Iterate over Item's Categories, if any, using the first one.  
//     * Try to match against any we've already pulled.  
//     * If none found locally, check against the database.  
//     * If we still don't find a match, create one and store it locally.
//     * If there are no Item Categories, use defaultCategory
//     * 
//     * @param mapping
//     * @param actionForm
//     * @param request
//     * @param response
//     * @return
//     * @throws IOException
//     * @throws ServletException
//     */
//    private WeblogCategoryData locateCategory(
//                                  String catName, HashMap categoryMap) 
//    throws RollerException
//    {
//        WeblogCategoryData category = (WeblogCategoryData)categoryMap.get(catName);
//        if (category == null) // not in local map
//        {
//            // look for it in database, by path
//            category = roller.getWeblogManager()
//                .getWeblogCategoryByPath(website, category, catName);
//                        
//            if (category == null) // not in database
//            {    
//                // create a new one
//                category = new WeblogCategoryData(null, 
//                    website, rootCategory, 
//                    catName, catName, null);
//                category.save();
//            }
//            categoryMap.put(catName, category);
//        }
//        
//        return category;
//    }
}
