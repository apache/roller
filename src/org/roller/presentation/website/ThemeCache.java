package org.roller.presentation.website;

import org.roller.util.LRUCache2;

/**
 * Caches the Theme files to avoid repeated reading of the files from the
 * harddrive.
 * 
 * @author llavandowska
 */
public class ThemeCache
{
	private static ThemeCache INSTANCE = new ThemeCache();
	private static String cacheName = "ThemeFiles";
	/**
	 * How many objects to store in cache.
	 * @TODO Add configuration for maxObjects in theme cache
	 */
	private static int  maxObjects = 500;
	
    private static LRUCache2 cache = new LRUCache2(maxObjects, 30 * 60 * 1000);
	
	/**
	 * How long until an object in cache expires.
	 * @TODO Add configuration for theme cache timeout
	 */
	private long expireInterval = 1000l*60*60*24; // 1 second * 1 min * 1 hr * 24 hours
	
	/**
	 * Should the PreviewResourceLoader cache the Template files.
	 * @TODO Add configuration for enabling theme template caching
	 */
	private static boolean cacheTemplateFiles = false;
		
	/** Private constructor to prevent outside instantiation **/
	private ThemeCache() { }
		
	/**
	 * 
	 */
	public static ThemeCache getInstance()
	{
		return INSTANCE;
	}
	
	/**
	 * 
	 */
	public String putIntoCache(String themeName, String fileName, String template)
	{
		if (cacheTemplateFiles)
		{
			cache.put(themeName+":"+fileName, template);
		}
		return template;

	}
	
	/**
	 * Null will be returned if there is a problem or if caching is "turned
	 * off".
	 */
	public String getFromCache(String themeName, String fileName)
	{
		if (!cacheTemplateFiles) return null;
		return (String) cache.get(themeName + ":" + fileName);
	}
	
	/**
	 * 
	 */
	public void removeFromCache(String themeName, String fileName)
	{
		if (!cacheTemplateFiles) return;
		cache.purge( new String[] { themeName+":"+fileName } );
	}

    
    /**
     * The list of files in a Theme is cached as a String[], the key being the
     * Theme location itself.
     * 
     * @param themeDir
     * @param fileNames
     * @return String[]
     */
    public String[] setFileList(String themeDir, String[] fileNames)
    {
		if (cacheTemplateFiles)
		{
        	    cache.put(themeDir, fileNames);
		}
        return fileNames;

    }
    
    /**
     * The list of files in a Theme is cached as a String[], the key being the
     * Theme location itself.  If caching is turned off this will return null.
     * 
     * @param theme
     * @return String[]
     */
    public String[] getFileList(String themeDir)
    {
		if (!cacheTemplateFiles) return null;
        return (String[])cache.get(themeDir);
    }
}
