package org.roller.business.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.roller.pojos.PermissionsData;


/**
 * Upgrade Roller Database.
 */
public class UpgradeDatabase {
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(UpgradeDatabase.class);
    
    // the name of the property which holds the dbversion value
    private static final String DBVERSION_PROP = "roller.database.version";
    
    // old version ... does nothing
    public static void upgradeDatabase(Connection con) throws RollerException {}
    
    
    /**
     * Upgrade database if dbVersion is older than desiredVersion.
     */
    public static void upgradeDatabase(Connection con, String desiredVersion) 
        throws RollerException {
        
        int myVersion = 0;
        int dbversion = -1;
        
        // NOTE: this assumes a maximum of 3 digits for the version number
        //       so if we get to 10.0 then we'll need to upgrade this
        
        // strip out non-digits
        desiredVersion = desiredVersion.replaceAll("\\Q.\\E", "");
        desiredVersion = desiredVersion.replaceAll("\\D", "");
        if(desiredVersion.length() > 3)
            desiredVersion = desiredVersion.substring(0, 3);
        
        // parse to an int
        try {
            int parsed = Integer.parseInt(desiredVersion);
            
            if(parsed < 100)
                myVersion = parsed * 10;
            else
                myVersion = parsed;
        } catch(Exception e) {}
        
        
        // get the current db version
        try {
            Statement stmt = con.createStatement();
            
            // just check in the roller_properties table
            ResultSet rs = stmt.executeQuery(
                    "select value from roller_properties where name = '"+DBVERSION_PROP+"'");
            
            if(rs.next()) {
                dbversion = Integer.parseInt(rs.getString(1));
                
            } else {
                // tough to know if this is an upgrade with no db version :/
                // however, if roller_properties is not empty then we at least
                // we have someone upgrading from 1.2.x
                rs = stmt.executeQuery("select count(*) from roller_properties");
                if(rs.next()) {
                    if(rs.getInt(1) > 0)
                        dbversion = 120;
                }
            }
            
        } catch(Exception e) {
            // that's strange ... hopefully we didn't need to upgrade
            mLogger.error("Couldn't lookup current database version", e);
            return;
        }
        
        mLogger.debug("Database version = "+dbversion);
        mLogger.debug("Desired version = "+myVersion);
            
        if(dbversion < 0) {
            mLogger.info("New installation found, setting db version to "+myVersion);
            UpgradeDatabase.setDatabaseVersion(con, myVersion);
            return;
        } else if(dbversion >= myVersion) {
            mLogger.info("Database is current, no upgrade needed");
            return;
        }
        
        mLogger.info("Database is old, beginning upgrade to version "+myVersion);
        
        // iterate through each upgrade as needed
        // to add to the upgrade sequence simply add a new "if" statement
        // for whatever version needed and then define a new method upgradeXXX()
        if(dbversion < 130) 
        {
            UpgradeDatabase.upgradeTo130(con);
            dbversion = 130;
        }
        if (dbversion < 200)
        {
            UpgradeDatabase.upgradeTo200(con);
            dbversion = 200;
        }
        if(dbversion < 210) {
            UpgradeDatabase.upgradeTo210(con);
            dbversion = 210;
        }
        
        // make sure the database version is the exact version
        // we are upgrading too.
        UpgradeDatabase.updateDatabaseVersion(con, myVersion);
    }
    
    
    /**
     * Upgrade database for Roller 1.3.0
     */
    private static void upgradeTo130(Connection con) throws RollerException {
        try {
            /*
             * The new theme management code is going into place and it uses
             * the old website.themeEditor attribute to store a users theme.
             *
             * In pre-1.3 Roller *all* websites are considered to be using a
             * custom theme, so we need to make sure this is properly defined
             * by setting the theme on all websites to custom.
             *
             * NOTE: If we don't do this then nothing would break, but some users
             * would be suprised that their template customizations are no longer
             * in effect because they are using a shared theme instead.
             */
            
            mLogger.info("Doing upgrade to 130 ...");
            mLogger.info("Ensuring that all website themes are set to custom");
            
            PreparedStatement setCustomThemeStmt = con.prepareStatement(
                    "update website set editortheme = ?");
            
            setCustomThemeStmt.setString(1, org.roller.pojos.Theme.CUSTOM);
            setCustomThemeStmt.executeUpdate();
            
            if (!con.getAutoCommit()) con.commit();
            
            mLogger.info("Upgrade to 130 complete.");
            
        } catch (SQLException e) {
            mLogger.error("Problem upgrading database to version 130", e);
            throw new RollerException("Problem upgrading database to version 130", e);
        }
        
        // If someone is upgrading to 1.3.x then we are setting the db version
        // for the first time.  Normally we would just updateDatabaseVersion()
        UpgradeDatabase.setDatabaseVersion(con, 130);
    }

    /**
     * Upgrade database for Roller 2.0.0
     */
    private static void upgradeTo200(Connection con) throws RollerException {
        try {
            mLogger.info("Doing upgrade to 200 ...");
            mLogger.info("Populating roller_user_permissions table");
            
            PreparedStatement websitesQuery = con.prepareStatement(
                "select w.id, u.id, u.username from "
              + "website as w, rolleruser as u where u.id=w.userid");
            PreparedStatement websiteUpdate = con.prepareStatement(
                "update website set handle=? where id=?");         
            PreparedStatement entryUpdate = con.prepareStatement(
                "update weblogentry set userid=?, status=?, "
              + "pubtime=pubtime, updatetime=updatetime "
              + "where publishentry=? and websiteid=?");            
            PreparedStatement permsInsert = con.prepareStatement(
                "insert roller_user_permissions "
              + "(id, website_id, user_id, permission_mask, pending) "
              + "values (?,?,?,?,?)");
             
            // loop through websites, each has a user
            ResultSet websiteSet = websitesQuery.executeQuery();
            while (websiteSet.next()) {
                String websiteid = websiteSet.getString("w.id");
                String userid = websiteSet.getString("u.id");
                String handle = websiteSet.getString("u.username");
                mLogger.info("Processing website: " + handle);
                       
                // use website user's username as website handle
                websiteUpdate.clearParameters();
                websiteUpdate.setString(1, handle);
                websiteUpdate.setString(2, websiteid);
                websiteUpdate.executeUpdate();
                
                // update all of pubished entries to include userid and status
                entryUpdate.clearParameters();
                entryUpdate.setString( 1, userid);
                entryUpdate.setString( 2, "PUBLISHED");
                entryUpdate.setBoolean(3, true);
                entryUpdate.setString( 4, websiteid);
                entryUpdate.executeUpdate();                               
                
                // update all of draft entries to include userid and status
                entryUpdate.clearParameters();
                entryUpdate.setString( 1, userid);
                entryUpdate.setString( 2, "DRAFT");
                entryUpdate.setBoolean(3, false);
                entryUpdate.setString( 4, websiteid);
                entryUpdate.executeUpdate();                               
                
                // add  permission for user in website
                permsInsert.clearParameters();
                permsInsert.setString( 1, websiteid+"p");
                permsInsert.setString( 2, websiteid);
                permsInsert.setString( 3, userid);
                permsInsert.setShort(  4, PermissionsData.ADMIN);
                permsInsert.setBoolean(5, false);
                permsInsert.executeUpdate();
            }
            
            if (!con.getAutoCommit()) con.commit();
            
            mLogger.info("Upgrade to 200 complete.");
            
        } catch (SQLException e) {
            mLogger.error("Problem upgrading database to version 200", e);
            throw new RollerException("Problem upgrading database to version 200", e);
        }
        
        UpgradeDatabase.updateDatabaseVersion(con, 200);
    }


    /**
     * Upgrade database for Roller 1.3.0
     */
    private static void upgradeTo210(Connection con) throws RollerException {
        try {
            /*
             * For Roller 2.1.0 we are going to standardize some of the
             * weblog templates and make them less editable.  To do this
             * we need to do a little surgery.
             *
             * The goal for this upgrade is to ensure that ALL weblogs now have
             * the required "Weblog" template as their default template.
             */
            
            mLogger.info("Doing upgrade to 210 ...");
            mLogger.info("Ensuring that all weblogs use the 'Weblog' template as their default page");
            
            // this query will give us all websites that have modified their
            // default page to link to something other than "Weblog"
            PreparedStatement selectUpdateWeblogs = con.prepareStatement(
                    "select website.id,template from website,webpage "+
                    "where webpage.id = website.defaultpageid "+
                    "and webpage.link != 'Weblog'");
            
            // insert a new template for a website
            PreparedStatement insertWeblogTemplate = con.prepareStatement(
                    "insert into webpage"+
                    "(id, name, description, link, websiteid, template) "+
                    "values(?,?,?,?,?,?)");
            
            // update the default page for a website
            PreparedStatement updateDefaultPage = con.prepareStatement(
                    "update website set defaultpageid = ? "+
                    "where id = ?");
            
            String description = "This template is used to render the main "+
                    "page of your weblog.";
            ResultSet websiteSet = selectUpdateWeblogs.executeQuery();
            while (websiteSet.next()) {
                String websiteid = websiteSet.getString(1);
                String template = websiteSet.getString(2);
                mLogger.info("Processing website: " + websiteid);
                
                // insert new Weblog template
                insertWeblogTemplate.clearParameters();
                insertWeblogTemplate.setString( 1, websiteid+"q");
                insertWeblogTemplate.setString( 2, "Weblog");
                insertWeblogTemplate.setString( 3, description);
                insertWeblogTemplate.setString( 4, "Weblog");
                insertWeblogTemplate.setString( 5, websiteid);
                insertWeblogTemplate.setString( 6, template);
                insertWeblogTemplate.executeUpdate();
                
                // update defaultpageid value
                updateDefaultPage.clearParameters();
                updateDefaultPage.setString( 1, websiteid+"q");
                updateDefaultPage.setString( 2, websiteid);
                updateDefaultPage.executeUpdate();
            }
            
            
            if (!con.getAutoCommit()) con.commit();
            
            mLogger.info("Upgrade to 210 complete.");
            
        } catch (SQLException e) {
            mLogger.error("Problem upgrading database to version 210", e);
            throw new RollerException("Problem upgrading database to version 210", e);
        }
        
        UpgradeDatabase.updateDatabaseVersion(con, 210);
    }
    
    
    /**
     * Insert a new database.version property.
     *
     * This should only be called once for new installations
     */
    private static void setDatabaseVersion(Connection con, int version) 
        throws RollerException {
        
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("insert into roller_properties "+
                    "values('"+DBVERSION_PROP+"', '"+version+"')");
            
            mLogger.debug("Set database verstion to "+version);
        } catch(SQLException se) {
            throw new RollerException("Error setting database version.", se);
        }
    }
    
    
    /**
     * Update the existing database.version property
     */
    private static void updateDatabaseVersion(Connection con, int version) 
        throws RollerException {
        
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("update roller_properties "+
                    "set value = '"+version+"'"+
                    "where name = '"+DBVERSION_PROP+"'");
            
            mLogger.debug("Updated database verstion to "+version);
        } catch(SQLException se) {
            throw new RollerException("Error setting database version.", se);
        }
    }
}
