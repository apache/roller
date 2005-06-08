package org.roller.business.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Upgrade Roller database from 0.9.8 to 0.9.9.
 * 
 * Creates root Category for each Website and associations for each Category.
 * Sets each Website's default Category and default Blogger.com Category
 * Creates associations for each Folder.
 */
public class UpgradeDatabase
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(UpgradeDatabase.class);

    public static void upgradeDatabase(Connection con) throws RollerException
    {        
        try
        {   
            Statement versionStatement = con.createStatement();
            ResultSet versionResultSet = 
              versionStatement.executeQuery("select dbversion from rollerconfig");
            versionResultSet.next();
            String dbversion = versionResultSet.getString(1);
            if (dbversion != null) return;
            
            // Prepated statements for all queries in the loop
            
            PreparedStatement rootCatStatement = con.prepareStatement(
               "select a.id from weblogcategoryassoc as a, weblogcategory as c "+
               "where c.websiteid=? and a.categoryid=c.id and a.ancestorid is null and a.relation='PARENT'");                        
            
            PreparedStatement rootCatCreateStatement = con.prepareStatement(
               "insert into weblogcategory (id,name,description,websiteid,image) "+
               "values (?,'root','root',?,NULL)");                        
            
            PreparedStatement updateWebsiteStatement = con.prepareStatement(
               "update website set bloggercatid=?, defaultcatid=? where id=?");                        
            
            PreparedStatement catsStatement = con.prepareStatement(
               "select id from weblogcategory where websiteid=? and id<>?");                        
            
            PreparedStatement assocCreateStatement = con.prepareStatement(
               "insert into weblogcategoryassoc (id,categoryid,ancestorid,relation) "+
               "values (?,?,?,'PARENT')");                        

            PreparedStatement rootFolderStatement = con.prepareStatement(
                "select id from folder where websiteid=? and parentid is null");                        
      
            PreparedStatement foldersStatement = con.prepareStatement(
                "select id,parentid from folder where websiteid=?");                        
      
            PreparedStatement folderAssocCreateStatement = con.prepareStatement(
                "insert into folderassoc (id,folderid,ancestorid,relation) "+
                "values (?,?,?,'PARENT')");                        

            // loop through all websites
            Statement websitesStatement = con.createStatement();
            ResultSet websitesResultSet = 
                websitesStatement.executeQuery("select id from website");
            while (websitesResultSet.next()) 
            {
                String websiteId = websitesResultSet.getString(1);
                mLogger.info("Upgrading website id="+websiteId);
                
                rootCatStatement.clearParameters();
                rootCatStatement.setString(1, websiteId);
                ResultSet rootCatResultSet = rootCatStatement.executeQuery();
                
                
                if (!rootCatResultSet.first()) // if website has no root cat
                {
                    // then create one
                    rootCatCreateStatement.clearParameters();
                    rootCatCreateStatement.setString(1, websiteId+"R");
                    rootCatCreateStatement.setString(2, websiteId);
                    rootCatCreateStatement.executeUpdate();
                    
                    // and make it the default one for the website
                    updateWebsiteStatement.clearParameters();
                    updateWebsiteStatement.setString(1, websiteId+"R");
                    updateWebsiteStatement.setString(2, websiteId+"R");
                    updateWebsiteStatement.setString(3, websiteId);
                    updateWebsiteStatement.executeUpdate();
                    
                    // and create an association for it
                    assocCreateStatement.clearParameters();
                    assocCreateStatement.setString(1, websiteId+"A0");
                    assocCreateStatement.setString(2, websiteId+"R");
                    assocCreateStatement.setString(3, null);
                    assocCreateStatement.executeUpdate();

                    // and create associations for all of it's children
                    catsStatement.clearParameters();
                    catsStatement.setString(1, websiteId);
                    catsStatement.setString(2, websiteId+"R");
                    ResultSet cats = catsStatement.executeQuery();
                    int count = 1;
                    while (cats.next())
                    {
                        String catid = cats.getString(1);
                        assocCreateStatement.clearParameters();
                        assocCreateStatement.setString(1, websiteId+"A"+count++);
                        assocCreateStatement.setString(2, catid);
                        assocCreateStatement.setString(3, websiteId+"R");
                        assocCreateStatement.executeUpdate();
                    }
                    mLogger.debug("   Created root categories and associations");
                    
                    // determine root bookmark folder of website
                    rootFolderStatement.clearParameters();
                    rootFolderStatement.setString(1, websiteId);
                    ResultSet rootFolderResultSet = rootFolderStatement.executeQuery();
                    rootFolderResultSet.next();
                    String rootFolderId = rootFolderResultSet.getString(1);
                    
                    // create associations for all children fo root folder
                    foldersStatement.clearParameters();
                    foldersStatement.setString(1, websiteId);
                    ResultSet folders = foldersStatement.executeQuery();
                    while (folders.next())
                    {
                        String id = folders.getString(1);
                        String parentId = folders.getString(2);
                        folderAssocCreateStatement.clearParameters();
                        folderAssocCreateStatement.setString(1, id+"R");
                        folderAssocCreateStatement.setString(2, id);
                        if (parentId == null)
                        {
                            folderAssocCreateStatement.setString(3, null);
                        }
                        else
                        {
                            folderAssocCreateStatement.setString(3, rootFolderId);
                        }
                        folderAssocCreateStatement.executeUpdate();
                    }
                    mLogger.debug("   Created folder associations");
                }
            }
            
            Statement versionUpdateStatement = con.createStatement();
            versionUpdateStatement.executeUpdate(
                "update rollerconfig set dbversion='1.0'");
            mLogger.info("Database upgrade complete.");
        }
        catch (SQLException e)
        {
            mLogger.error("ERROR in database upgrade",e);
            throw new RollerException("ERROR in database upgrade",e);
        }
    }
}
