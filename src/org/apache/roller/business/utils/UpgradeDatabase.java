/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.business.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.PermissionsData;


/**
 * Upgrade Roller Database.
 */
public class UpgradeDatabase {
    
    private static Log mLogger = LogFactory.getLog(UpgradeDatabase.class);
    
    // the name of the property which holds the dbversion value
    private static final String DBVERSION_PROP = "roller.database.version";
    
    
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
        if(dbversion < 130) {
            UpgradeDatabase.upgradeTo130(con);
            dbversion = 130;
        }
        if (dbversion < 200) {
            UpgradeDatabase.upgradeTo200(con);
            dbversion = 200;
        }
        if(dbversion < 210) {
            UpgradeDatabase.upgradeTo210(con);
            dbversion = 210;
        }
        if(dbversion < 300) {
            UpgradeDatabase.upgradeTo300(con);
            dbversion = 300;
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
            
            setCustomThemeStmt.setString(1, org.apache.roller.pojos.Theme.CUSTOM);
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
                    "select w.id as wid, u.id as uid, u.username as uname from "
                    + "website as w, rolleruser as u where u.id=w.userid");
            PreparedStatement websiteUpdate = con.prepareStatement(
                    "update website set handle=? where id=?");
            PreparedStatement entryUpdate = con.prepareStatement(
                    "update weblogentry set userid=?, status=?, "
                    + "pubtime=pubtime, updatetime=updatetime "
                    + "where publishentry=? and websiteid=?");
            PreparedStatement permsInsert = con.prepareStatement(
                    "insert into roller_user_permissions "
                    + "(id, website_id, user_id, permission_mask, pending) "
                    + "values (?,?,?,?,?)");
            
            // loop through websites, each has a user
            ResultSet websiteSet = websitesQuery.executeQuery();
            while (websiteSet.next()) {
                String websiteid = websiteSet.getString("wid");
                String userid = websiteSet.getString("uid");
                String handle = websiteSet.getString("uname");
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
     * Upgrade database for Roller 2.1.0
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
                    "select website.id,template,website.handle from website,webpage "+
                    "where webpage.id = website.defaultpageid "+
                    "and webpage.link != 'Weblog'");
            
            PreparedStatement selectWeblogTemplate = con.prepareStatement(
                    "select id from webpage where websiteid = ? and link = 'Weblog'");
            
            PreparedStatement updateWeblogTemplate = con.prepareStatement(
                    "update webpage set template = ? where id = ?");
            
            // insert a new template for a website
            PreparedStatement insertWeblogTemplate = con.prepareStatement(
                    "insert into webpage"+
                    "(id, name, description, link, websiteid, template, updatetime) "+
                    "values(?,?,?,?,?,?,?)");
            
            // update the default page for a website
            PreparedStatement updateDefaultPage = con.prepareStatement(
                    "update website set defaultpageid = ? "+
                    "where id = ?");
            
            String description = "This template is used to render the main "+
                    "page of your weblog.";
            ResultSet websiteSet = selectUpdateWeblogs.executeQuery();
            Date now = new Date();
            while (websiteSet.next()) {
                String websiteid = websiteSet.getString(1);
                String template = websiteSet.getString(2);
                String handle = websiteSet.getString(3);
                mLogger.info("Processing website: " + handle);
                
                String defaultpageid = null;
                
                // it's possible that this weblog has a "Weblog" template, but just
                // isn't using it as their default.  if so we need to fix that.
                selectWeblogTemplate.clearParameters();
                selectWeblogTemplate.setString(1, websiteid);
                ResultSet weblogPageSet = selectWeblogTemplate.executeQuery();
                if(weblogPageSet.next()) {
                    // this person already has a "Weblog" template, so update it
                    String id = weblogPageSet.getString(1);
                    
                    updateWeblogTemplate.clearParameters();
                    updateWeblogTemplate.setString(1, template);
                    updateWeblogTemplate.setString(2, id);
                    updateWeblogTemplate.executeUpdate();
                    
                    // make sure and adjust what default page id we want to use
                    defaultpageid = id;
                } else {
                    // no "Weblog" template, so insert a new one
                    insertWeblogTemplate.clearParameters();
                    insertWeblogTemplate.setString( 1, websiteid+"q");
                    insertWeblogTemplate.setString( 2, "Weblog");
                    insertWeblogTemplate.setString( 3, description);
                    insertWeblogTemplate.setString( 4, "Weblog");
                    insertWeblogTemplate.setString( 5, websiteid);
                    insertWeblogTemplate.setString( 6, template);
                    insertWeblogTemplate.setDate(   7, new java.sql.Date(now.getTime()));
                    insertWeblogTemplate.executeUpdate();
                    
                    // set the new default page id
                    defaultpageid = websiteid+"q";
                }
                
                // update defaultpageid value
                updateDefaultPage.clearParameters();
                updateDefaultPage.setString( 1, defaultpageid);
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
     * Upgrade database for Roller 3.0.0
     */
    private static void upgradeTo300(Connection con) throws RollerException {
        try {
            /*
             * For Roller 3.0.0 we are allowing each weblogentry to track a
             * locale now so that we can support multi-lingual blogs.  As part
             * of the upgrade process we want to do 2 things ..
             *
             * 1. make sure all weblogs have a locale
             * 2. set the locale on all entries to the locale for the weblog
             */
            
            mLogger.info("Doing upgrade to 300 ...");
            
            // get system default language
            String locale = java.util.Locale.getDefault().getLanguage();
            
            mLogger.info("Setting website locale to "+locale+" for websites with no locale");
            
            // update all weblogs where locale is "null"
            PreparedStatement updateNullWeblogLocale = con.prepareStatement(
                    "update website set locale = ? where locale is NULL");
            // update all weblogs where locale is empty string ""
            PreparedStatement updateEmptyWeblogLocale = con.prepareStatement(
                    "update website set locale = ? where locale = ''");
            updateNullWeblogLocale.setString( 1, locale);
            updateEmptyWeblogLocale.setString( 1, locale);
            updateNullWeblogLocale.executeUpdate();
            updateEmptyWeblogLocale.executeUpdate();

            
            mLogger.info("Setting weblogentry locales to website locale");
            
            // get all entries and the locale of its website
            PreparedStatement selectWeblogsLocale = con.prepareStatement(
                    "select weblogentry.id,website.locale "+
                    "from weblogentry,website "+
                    "where weblogentry.websiteid = website.id");
            
            // set the locale for an entry
            PreparedStatement updateWeblogLocale = con.prepareStatement(
                    "update weblogentry set locale = ? where id = ?");
            
            ResultSet websiteSet = selectWeblogsLocale.executeQuery();
            while (websiteSet.next()) {
                String entryid = websiteSet.getString(1);
                String entrylocale = websiteSet.getString(2);
                
                // update entry locale
                updateWeblogLocale.clearParameters();
                updateWeblogLocale.setString( 1, entrylocale);
                updateWeblogLocale.setString( 2, entryid);
                updateWeblogLocale.executeUpdate();
            }
            
            
            if (!con.getAutoCommit()) con.commit();
            
            mLogger.info("Upgrade to 300 complete.");
            
        } catch (SQLException e) {
            mLogger.error("Problem upgrading database to version 300", e);
            throw new RollerException("Problem upgrading database to version 300", e);
        }
        
        UpgradeDatabase.updateDatabaseVersion(con, 300);
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

