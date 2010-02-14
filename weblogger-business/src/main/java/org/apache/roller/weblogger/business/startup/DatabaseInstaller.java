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

package org.apache.roller.weblogger.business.startup;

import org.apache.roller.util.SQLScriptRunner;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.DatabaseProvider;
import org.apache.roller.weblogger.pojos.WeblogPermission;


/**
 * Handles the install/upgrade of the Roller Weblogger database when the user
 * has configured their installation type to 'auto'.
 */
public class DatabaseInstaller {
    
    private static Log log = LogFactory.getLog(DatabaseInstaller.class);
    
    private final DatabaseProvider db;
    private final DatabaseScriptProvider scripts;
    private final String version;
    private List<String> messages = new ArrayList<String>();
    
    // the name of the property which holds the dbversion value
    private static final String DBVERSION_PROP = "roller.database.version";
    
    
    public DatabaseInstaller(DatabaseProvider dbProvider, DatabaseScriptProvider scriptProvider) {
        db = dbProvider;
        scripts = scriptProvider;
        
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/roller-version.properties"));
        } catch (IOException e) {
            log.error("roller-version.properties not found", e);
        }
        
        version = props.getProperty("ro.version", "UNKNOWN");
    }
    
    
    /** 
     * Determine if database schema needs to be upgraded.
     */
    public boolean isCreationRequired() {
        Connection con = null;
        try {            
            con = db.getConnection();
            
            // just check for a couple key Roller tables
            if (tableExists(con, "rolleruser") && tableExists(con, "userrole")) {
                return false;
            }
            
        } catch (Throwable t) {
            throw new RuntimeException("Error checking for tables", t);            
        } finally {
            try { if (con != null) con.close(); } catch (Exception ignored) {}
        }
        
        return true;
    }
    
    
    /** 
     * Determine if database schema needs to be upgraded.
     */
    public boolean isUpgradeRequired() {
        int desiredVersion = parseVersionString(version);
        int databaseVersion;
        try {
            databaseVersion = getDatabaseVersion();
        } catch (StartupException ex) {
            throw new RuntimeException(ex);
        }
        
        // if dbversion is unset then assume a new install, otherwise compare
        if (databaseVersion < 0) {
            // if this is a fresh db then we need to set the database version
            Connection con = null;
            try {
                con = db.getConnection();
                setDatabaseVersion(con, version);
            } catch (Exception ioe) {
                errorMessage("ERROR setting database version");
            } finally {
                try {
                    if (con != null) {
                        con.close();
                    }
                } catch (Exception ignored) {
                }
            }

            return false;
        } else {
            return databaseVersion < desiredVersion;
        }
    }
    
    
    public List<String> getMessages() {
        return messages;
    }
    
    
    private void errorMessage(String msg) {
        messages.add(msg);
        log.error(msg);
    }    
    
    
    private void errorMessage(String msg, Throwable t) {
        messages.add(msg);
        log.error(msg, t);
    }
    
    
    private void successMessage(String msg) {
        messages.add(msg);
        log.trace(msg);
    }
    
    
    /**
     * Create datatabase tables.
     */
    public void createDatabase() throws StartupException {
        
        log.info("Creating Roller Weblogger database tables.");
        
        Connection con = null;
        SQLScriptRunner create = null;
        try {
            con = db.getConnection();
            String handle = getDatabaseHandle(con);
            create = new SQLScriptRunner(scripts.getDatabaseScript(handle + "/createdb.sql"));
            create.runScript(con, true);
            messages.addAll(create.getMessages());
            
            setDatabaseVersion(con, version);
            
        } catch (SQLException sqle) {
            log.error("ERROR running SQL in database creation script", sqle);
            if (create != null) messages.addAll(create.getMessages());
            errorMessage("ERROR running SQL in database creation script");
            throw new StartupException("Error running sql script", sqle); 
            
        } catch (Exception ioe) {
            log.error("ERROR running database creation script", ioe);
            if (create != null) messages.addAll(create.getMessages());
            errorMessage("ERROR reading/parsing database creation script");
            throw new StartupException("Error running SQL script", ioe);

        } finally {
            try { if (con != null) con.close(); } catch (Exception ignored) {}
        }
    }
    
    
    /**
     * Upgrade database if dbVersion is older than desiredVersion.
     */
    public void upgradeDatabase(boolean runScripts) throws StartupException {
        
        int myVersion = parseVersionString(version);
        int dbversion = getDatabaseVersion();
        
        log.debug("Database version = "+dbversion);
        log.debug("Desired version = "+myVersion);
       
        Connection con = null;
        try {
            con = db.getConnection();
            if(dbversion < 0) {
                String msg = "Cannot upgrade database tables, Roller database version cannot be determined";
                errorMessage(msg);
                throw new StartupException(msg);
            } else if(dbversion >= myVersion) {
                log.info("Database is current, no upgrade needed");
                return;
            }

            log.info("Database is old, beginning upgrade to version "+myVersion);

            // iterate through each upgrade as needed
            // to add to the upgrade sequence simply add a new "if" statement
            // for whatever version needed and then define a new method upgradeXXX()
            if(dbversion < 130) {
                upgradeTo130(con, runScripts);
                dbversion = 130;
            }
            if (dbversion < 200) {
                upgradeTo200(con, runScripts);
                dbversion = 200;
            }
            if(dbversion < 210) {
                upgradeTo210(con, runScripts);
                dbversion = 210;
            }
            if(dbversion < 230) {
                upgradeTo230(con, runScripts);
                dbversion = 230;
            }
            if(dbversion < 240) {
                upgradeTo240(con, runScripts);
                dbversion = 240;
            }
            if(dbversion < 300) {
                upgradeTo300(con, runScripts);
                dbversion = 300;
            }
            if(dbversion < 310) {
                upgradeTo310(con, runScripts);
                dbversion = 310;
            }
            if(dbversion < 400) {
                upgradeTo400(con, runScripts);
                dbversion = 400;
            }
            if(dbversion < 500) {
                upgradeTo500(con, runScripts);
                dbversion = 500;
            }
            
            // make sure the database version is the exact version
            // we are upgrading too.
            updateDatabaseVersion(con, myVersion);
        
        } catch (SQLException e) {
            throw new StartupException("ERROR obtaining connection");
        } finally {
            try { if (con != null) con.close(); } catch (Exception ignored) {}
        }
    }
    
    
    /**
     * Upgrade database for Roller 1.3.0
     */
    private void upgradeTo130(Connection con, boolean runScripts) throws StartupException {
        SQLScriptRunner runner = null;
        try {
            if (runScripts) {
                String handle = getDatabaseHandle(con);
                String scriptPath = handle + "/120-to-130-migration.sql";
                successMessage("Running database upgrade script: "+scriptPath);                
                runner = new SQLScriptRunner(scripts.getDatabaseScript(scriptPath));
                runner.runScript(con, true);
                messages.addAll(runner.getMessages());
            }
            
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
            
            successMessage("Doing upgrade to 130 ...");
            successMessage("Ensuring that all website themes are set to custom");
            
            PreparedStatement setCustomThemeStmt = con.prepareStatement(
                    "update website set editortheme = ?");
            
            setCustomThemeStmt.setString(1, org.apache.roller.weblogger.pojos.WeblogTheme.CUSTOM);
            setCustomThemeStmt.executeUpdate();
            
            if (!con.getAutoCommit()) con.commit();
            
            successMessage("Upgrade to 130 complete.");
            
        } catch (Exception e) {
            log.error("ERROR running 310 database upgrade script", e);
            if (runner != null) messages.addAll(runner.getMessages());
            
            errorMessage("Problem upgrading database to version 130", e);  
            throw new StartupException("Problem upgrading database to version 130", e);
        }
        
        updateDatabaseVersion(con, 130);
    }
    
    /**
     * Upgrade database for Roller 2.0.0
     */
    private void upgradeTo200(Connection con, boolean runScripts) throws StartupException {
        SQLScriptRunner runner = null;
        try {
            if (runScripts) {
                String handle = getDatabaseHandle(con);
                String scriptPath = handle + "/130-to-200-migration.sql";
                successMessage("Running database upgrade script: "+scriptPath);                
                runner = new SQLScriptRunner(scripts.getDatabaseScript(scriptPath));
                runner.runScript(con, true);
                messages.addAll(runner.getMessages());
            }
            
            successMessage("Doing upgrade to 200 ...");
            successMessage("Populating roller_user_permissions table");
            
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
                    "insert into roller_permissions "
                    + "(id, username, actions, objectid, objecttype, pending, datecreated) "
                    + "values (?,?,?,?,?,?,?)");
            
            // loop through websites, each has a user
            java.sql.Date now = new java.sql.Date(new Date().getTime());
            ResultSet websiteSet = websitesQuery.executeQuery();
            while (websiteSet.next()) {
                String websiteid = websiteSet.getString("wid");
                String userid = websiteSet.getString("uid");
                String username = websiteSet.getString("uname");
                successMessage("Processing website: " + username);
                
                // use website user's username as website handle
                websiteUpdate.clearParameters();
                websiteUpdate.setString(1, username);
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
                permsInsert.setString( 2, username);
                permsInsert.setString( 3, WeblogPermission.ADMIN);
                permsInsert.setString( 4, websiteid);
                permsInsert.setString( 5, "Weblog");
                permsInsert.setBoolean(6, false);
                permsInsert.setDate(   7, now);
                permsInsert.setBoolean(5, false);
                permsInsert.executeUpdate();
            }
            
            if (!con.getAutoCommit()) con.commit();
            
            successMessage("Upgrade to 200 complete.");
            
        } catch (Exception e) {
            log.error("ERROR running 310 database upgrade script", e);
            if (runner != null) messages.addAll(runner.getMessages());
            
            errorMessage("Problem upgrading database to version 200", e);
            throw new StartupException("Problem upgrading database to version 200", e);
        }
        
        updateDatabaseVersion(con, 200);
    }
    
    
    /**
     * Upgrade database for Roller 2.1.0
     */
    private void upgradeTo210(Connection con, boolean runScripts) throws StartupException {
        SQLScriptRunner runner = null;
        try {
            if (runScripts) {
                String handle = getDatabaseHandle(con);
                String scriptPath = handle + "/200-to-210-migration.sql";
                successMessage("Running database upgrade script: "+scriptPath);                
                runner = new SQLScriptRunner(scripts.getDatabaseScript(scriptPath));
                runner.runScript(con, true);
                messages.addAll(runner.getMessages());
            }
            
            /*
             * For Roller 2.1.0 we are going to standardize some of the
             * weblog templates and make them less editable.  To do this
             * we need to do a little surgery.
             *
             * The goal for this upgrade is to ensure that ALL weblogs now have
             * the required "Weblog" template as their default template.
             */
            
            successMessage("Doing upgrade to 210 ...");
            successMessage("Ensuring that all weblogs use the 'Weblog' template as their default page");
            
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
                successMessage("Processing website: " + handle);
                
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
            
            successMessage("Upgrade to 210 complete.");
            
        } catch (Exception e) {
            log.error("ERROR running 310 database upgrade script", e);
            if (runner != null) messages.addAll(runner.getMessages());
            
            log.error("Problem upgrading database to version 210", e);
            throw new StartupException("Problem upgrading database to version 210", e);
        }
        
        updateDatabaseVersion(con, 210);
    }
    
    
    /**
     * Upgrade database for Roller 2.3.0
     */
    private void upgradeTo230(Connection con, boolean runScripts) throws StartupException {
        SQLScriptRunner runner = null;
        try {
            if (runScripts) {
                String handle = getDatabaseHandle(con);
                String scriptPath = handle + "/210-to-230-migration.sql";
                successMessage("Running database upgrade script: "+scriptPath);                
                runner = new SQLScriptRunner(scripts.getDatabaseScript(scriptPath));
                runner.runScript(con, true);
                messages.addAll(runner.getMessages());
            }
        } catch (Exception e) {
            log.error("ERROR running 310 database upgrade script", e);
            if (runner != null) messages.addAll(runner.getMessages());
            
            errorMessage("Problem upgrading database to version 230", e);
            throw new StartupException("Problem upgrading database to version 230", e);
        }
        
        updateDatabaseVersion(con, 230);
    }
    
    
    /**
     * Upgrade database for Roller 2.4.0
     */
    private void upgradeTo240(Connection con, boolean runScripts) throws StartupException {
        SQLScriptRunner runner = null;
        try {
            if (runScripts) {
                String handle = getDatabaseHandle(con);
                String scriptPath = handle + "/230-to-240-migration.sql";
                successMessage("Running database upgrade script: "+scriptPath);                
                runner = new SQLScriptRunner(scripts.getDatabaseScript(scriptPath));
                runner.runScript(con, true);
                messages.addAll(runner.getMessages());
            }
        } catch (Exception e) {
            log.error("ERROR running 310 database upgrade script", e);
            if (runner != null) messages.addAll(runner.getMessages());
            
            errorMessage("Problem upgrading database to version 240", e);
            throw new StartupException("Problem upgrading database to version 240", e);
        }
        
        updateDatabaseVersion(con, 240);
    }
    
    
    /**
     * Upgrade database for Roller 3.0.0
     */
    private void upgradeTo300(Connection con, boolean runScripts) throws StartupException {
        SQLScriptRunner runner = null;
        try {
            if (runScripts) {
                String handle = getDatabaseHandle(con);
                String scriptPath = handle + "/240-to-300-migration.sql";
                successMessage("Running database upgrade script: "+scriptPath);                
                runner = new SQLScriptRunner(scripts.getDatabaseScript(scriptPath));
                runner.runScript(con, true);
                messages.addAll(runner.getMessages());
            }
            
            /*
             * For Roller 3.0.0 we are allowing each weblogentry to track a
             * locale now so that we can support multi-lingual blogs.  As part
             * of the upgrade process we want to do 2 things ..
             *
             * 1. make sure all weblogs have a locale
             * 2. set the locale on all entries to the locale for the weblog
             */
            
            successMessage("Doing upgrade to 300 ...");
            
            // get system default language
            String locale = java.util.Locale.getDefault().getLanguage();
            
            successMessage("Setting website locale to "+locale+" for websites with no locale");
            
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

            
            successMessage("Setting weblogentry locales to website locale");
            
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
            
            successMessage("Upgrade to 300 complete.");
            
        } catch (Exception e) {
            log.error("ERROR running 310 database upgrade script", e);
            if (runner != null) messages.addAll(runner.getMessages());
            
            errorMessage("Problem upgrading database to version 300", e);
            throw new StartupException("Problem upgrading database to version 300", e);
        }
        
        updateDatabaseVersion(con, 300);
    }
    
    
    /**
     * Upgrade database for Roller 3.1.0
     */
    private void upgradeTo310(Connection con, boolean runScripts) throws StartupException {
        SQLScriptRunner runner = null;
        try {
            if (runScripts) {
                String handle = getDatabaseHandle(con);
                String scriptPath = handle + "/300-to-310-migration.sql";
                successMessage("Running database upgrade script: "+scriptPath);                
                runner = new SQLScriptRunner(scripts.getDatabaseScript(scriptPath));
                runner.runScript(con, true);
                messages.addAll(runner.getMessages());
            }
        } catch (Exception e) {
            log.error("ERROR running 310 database upgrade script", e);
            if (runner != null) messages.addAll(runner.getMessages());
            
            errorMessage("Problem upgrading database to version 310", e);
            throw new StartupException("Problem upgrading database to version 310", e);
        }
        
        updateDatabaseVersion(con, 310);
    }
    
    
    /**
     * Upgrade database for Roller 4.0.0
     */
    private void upgradeTo400(Connection con, boolean runScripts) throws StartupException {
        
        successMessage("Doing upgrade to 400 ...");
        
        // first we need to run upgrade scripts 
        SQLScriptRunner runner = null;
        try {    
            if (runScripts) {
                String handle = getDatabaseHandle(con);
                String scriptPath = handle + "/310-to-400-migration.sql";
                successMessage("Running database upgrade script: "+scriptPath);                
                runner = new SQLScriptRunner(scripts.getDatabaseScript(scriptPath));
                runner.runScript(con, true);
                messages.addAll(runner.getMessages());
            }
        } catch(Exception ex) {
            log.error("ERROR running 400 database upgrade script", ex);
            if (runner != null) messages.addAll(runner.getMessages());
            
            errorMessage("Problem upgrading database to version 400", ex);
            throw new StartupException("Problem upgrading database to version 400", ex);
        }
        
        
        // now upgrade hierarchical objects data model
        try {
            successMessage("Populating parentid columns for weblogcategory and folder tables");
            
            // Populate parentid in weblogcategory and folder tables.
            //
            // We'd like to do something like the below, but few databases 
            // support multiple table udpates, which are part of SQL-99
            //
            // update weblogcategory, weblogcategoryassoc 
            //   set weblogcategory.parentid = weblogcategoryassoc.ancestorid 
            //   where 
            //      weblogcategory.id = weblogcategoryassoc.categoryid 
            //      and weblogcategoryassoc.relation = 'PARENT';
            //
            // update folder,folderassoc 
            //   set folder.parentid = folderassoc.ancestorid 
            //   where 
            //      folder.id = folderassoc.folderid 
            //      and folderassoc.relation = 'PARENT';
            
            PreparedStatement selectParents = con.prepareStatement(
                "select categoryid, ancestorid from weblogcategoryassoc where relation='PARENT'");
            PreparedStatement updateParent = con.prepareStatement(
                "update weblogcategory set parentid=? where id=?");            
            ResultSet parentSet = selectParents.executeQuery();
            while (parentSet.next()) {
                String categoryid = parentSet.getString(1);
                String parentid = parentSet.getString(2);                
                updateParent.clearParameters();
                updateParent.setString( 1, parentid);
                updateParent.setString( 2, categoryid);
                updateParent.executeUpdate();
            }
            
            selectParents = con.prepareStatement(
                "select folderid, ancestorid from folderassoc where relation='PARENT'");
            updateParent = con.prepareStatement(
                "update folder set parentid=? where id=?");            
            parentSet = selectParents.executeQuery();
            while (parentSet.next()) {
                String folderid = parentSet.getString(1);
                String parentid = parentSet.getString(2);                
                updateParent.clearParameters();
                updateParent.setString( 1, parentid);
                updateParent.setString( 2, folderid);
                updateParent.executeUpdate();
            }
            
            if (!con.getAutoCommit()) con.commit();
           
            successMessage("Done populating parentid columns.");
            
        } catch (Exception e) {
            errorMessage("Problem upgrading database to version 320", e);
            throw new StartupException("Problem upgrading database to version 320", e);
        }
        
        
        try {
            successMessage("Populating path columns for weblogcategory and folder tables.");
                        
            // Populate path in weblogcategory and folder tables.
            //
            // It would be nice if there was a simple sql solution for doing
            // this, but sadly the only real way to do it is through brute
            // force walking the hierarchical trees.  Luckily, it seems that
            // most people don't create multi-level hierarchies, so hopefully
            // this won't be too bad
            
            // set path to '/' for nodes with no parents (aka root nodes)
            PreparedStatement setRootPaths = con.prepareStatement(
                "update weblogcategory set path = '/' where parentid is NULL");
            setRootPaths.clearParameters();
            setRootPaths.executeUpdate();
            
            // select all nodes whose parent has no parent (aka 1st level nodes)
            PreparedStatement selectL1Children = con.prepareStatement(
                "select f.id, f.name from weblogcategory f, weblogcategory p "+
                    "where f.parentid = p.id and p.parentid is NULL");
            // update L1 nodes with their path (/<name>)
            PreparedStatement updateL1Children = con.prepareStatement(
                "update weblogcategory set path=? where id=?");
            ResultSet L1Set = selectL1Children.executeQuery();
            while (L1Set.next()) {
                String id = L1Set.getString(1);
                String name = L1Set.getString(2);                
                updateL1Children.clearParameters();
                updateL1Children.setString( 1, "/"+name);
                updateL1Children.setString( 2, id);
                updateL1Children.executeUpdate();
            }
            
            // now for the complicated part =(
            // we need to keep iterating over L2, L3, etc nodes and setting
            // their path until all nodes have been updated.
            
            // select all nodes whose parent path has been set, excluding L1 nodes
            PreparedStatement selectLxChildren = con.prepareStatement(
                "select f.id, f.name, p.path from weblogcategory f, weblogcategory p "+
                    "where f.parentid = p.id and p.path <> '/' "+
                    "and p.path is not NULL and f.path is NULL");
            // update Lx nodes with their path (<parentPath>/<name>)
            PreparedStatement updateLxChildren = con.prepareStatement(
                "update weblogcategory set path=? where id=?");
            
            // this loop allows us to run this part of the upgrade process as
            // long as is necessary based on the depth of the hierarchy, and
            // we use the do/while construct to ensure it's run at least once
            int catNumCounted = 0;
            do {
                log.debug("Doing pass over Lx children for categories");
                
                // reset count for each iteration of outer loop
                catNumCounted = 0;
                
                ResultSet LxSet = selectLxChildren.executeQuery();
                while (LxSet.next()) {
                    String id = LxSet.getString(1);
                    String name = LxSet.getString(2);
                    String parentPath = LxSet.getString(3);
                    updateLxChildren.clearParameters();
                    updateLxChildren.setString( 1, parentPath+"/"+name);
                    updateLxChildren.setString( 2, id);
                    updateLxChildren.executeUpdate();
                    
                    // count the updated rows
                    catNumCounted++;
                }
                
                log.debug("Updated "+catNumCounted+" Lx category paths");
            } while(catNumCounted > 0);
            
            
            
            // set path to '/' for nodes with no parents (aka root nodes)
            setRootPaths = con.prepareStatement(
                "update folder set path = '/' where parentid is NULL");
            setRootPaths.clearParameters();
            setRootPaths.executeUpdate();
            
            // select all nodes whose parent has no parent (aka 1st level nodes)
            selectL1Children = con.prepareStatement(
                "select f.id, f.name from folder f, folder p "+
                    "where f.parentid = p.id and p.parentid is NULL");
            // update L1 nodes with their path (/<name>)
            updateL1Children = con.prepareStatement(
                "update folder set path=? where id=?");
            L1Set = selectL1Children.executeQuery();
            while (L1Set.next()) {
                String id = L1Set.getString(1);
                String name = L1Set.getString(2);                
                updateL1Children.clearParameters();
                updateL1Children.setString( 1, "/"+name);
                updateL1Children.setString( 2, id);
                updateL1Children.executeUpdate();
            }
            
            // now for the complicated part =(
            // we need to keep iterating over L2, L3, etc nodes and setting
            // their path until all nodes have been updated.
            
            // select all nodes whose parent path has been set, excluding L1 nodes
            selectLxChildren = con.prepareStatement(
                "select f.id, f.name, p.path from folder f, folder p "+
                    "where f.parentid = p.id and p.path <> '/' "+
                    "and p.path is not NULL and f.path is NULL");
            // update Lx nodes with their path (/<name>)
            updateLxChildren = con.prepareStatement(
                "update folder set path=? where id=?");
            
            // this loop allows us to run this part of the upgrade process as
            // long as is necessary based on the depth of the hierarchy, and
            // we use the do/while construct to ensure it's run at least once
            int folderNumUpdated = 0;
            do {
                log.debug("Doing pass over Lx children for folders");
                
                // reset count for each iteration of outer loop
                folderNumUpdated = 0;
                
                ResultSet LxSet = selectLxChildren.executeQuery();
                while (LxSet.next()) {
                    String id = LxSet.getString(1);
                    String name = LxSet.getString(2);
                    String parentPath = LxSet.getString(3);
                    updateLxChildren.clearParameters();
                    updateLxChildren.setString( 1, parentPath+"/"+name);
                    updateLxChildren.setString( 2, id);
                    updateLxChildren.executeUpdate();
                    
                    // count the updated rows
                    folderNumUpdated++;
                }
                
                log.debug("Updated "+folderNumUpdated+" Lx folder paths");
            } while(folderNumUpdated > 0);
            
            if (!con.getAutoCommit()) con.commit();
           
            successMessage("Done populating path columns.");
            
        } catch (SQLException e) {
            log.error("Problem upgrading database to version 320", e);
            throw new StartupException("Problem upgrading database to version 320", e);
        }
        
        
        // 4.0 changes the planet data model a bit, so we need to clean that up
        try {
            successMessage("Merging planet groups 'all' and 'external'");
            
            // Move all subscriptions in the planet group 'external' to group 'all'
            
            String allGroupId = null;
            PreparedStatement selectAllGroupId = con.prepareStatement(
                "select id from rag_group where handle = 'all'");
            ResultSet rs = selectAllGroupId.executeQuery();
            if (rs.next()) {
                allGroupId = rs.getString(1);
            }
            
            String externalGroupId = null;
            PreparedStatement selectExternalGroupId = con.prepareStatement(
                "select id from rag_group where handle = 'external'");            
            rs = selectExternalGroupId.executeQuery();
            if (rs.next()) {
                externalGroupId = rs.getString(1);
            }
            
            // we only need to merge if both of those groups already existed
            if(allGroupId != null && externalGroupId != null) {
                PreparedStatement updateGroupSubs = con.prepareStatement(
                        "update rag_group_subscription set group_id = ? where group_id = ?");
                updateGroupSubs.clearParameters();
                updateGroupSubs.setString( 1, allGroupId);
                updateGroupSubs.setString( 2, externalGroupId);
                updateGroupSubs.executeUpdate();
                
                // we no longer need the group 'external'
                PreparedStatement deleteExternalGroup = con.prepareStatement(
                        "delete from rag_group where handle = 'external'");
                deleteExternalGroup.executeUpdate();
                
            // if we only have group 'external' then just rename it to 'all'
            } else if(allGroupId == null && externalGroupId != null) {
                
                // rename 'external' to 'all'
                PreparedStatement renameExternalGroup = con.prepareStatement(
                        "update rag_group set handle = 'all' where handle = 'external'");
                renameExternalGroup.executeUpdate();
            }
            
            if (!con.getAutoCommit()) con.commit();
           
            successMessage("Planet group 'external' merged into group 'all'.");
            
        } catch (Exception e) {
            errorMessage("Problem upgrading database to version 400", e);
            throw new StartupException("Problem upgrading database to version 400", e);
        }
        
        
        // update local planet subscriptions to use new local feed format
        try {
            successMessage("Upgrading local planet subscription feeds to new feed url format");
            
            // need to start by looking up absolute site url
            PreparedStatement selectAbsUrl = 
                    con.prepareStatement("select value from roller_properties where name = 'site.absoluteurl'");
            String absUrl = null;
            ResultSet rs = selectAbsUrl.executeQuery();
            if(rs.next()) {
                absUrl = rs.getString(1);
            }
            
            if(absUrl != null && absUrl.length() > 0) {
                PreparedStatement selectSubs = 
                        con.prepareStatement("select id,feed_url,author from rag_subscription");
            
            PreparedStatement updateSubUrl = 
                    con.prepareStatement("update rag_subscription set last_updated=last_updated, feed_url = ? where id = ?");
            
            ResultSet rset = selectSubs.executeQuery();
            while (rset.next()) {
                String id = rset.getString(1);
                String feed_url = rset.getString(2);
                String handle = rset.getString(3);
                
                // only work on local feed urls
                if (feed_url.startsWith(absUrl)) {
                    // update feed_url to 'weblogger:<handle>'
                    updateSubUrl.clearParameters();
                    updateSubUrl.setString( 1, "weblogger:"+handle);
                    updateSubUrl.setString( 2, id);
                    updateSubUrl.executeUpdate();
                }
            }
            }
            
            if (!con.getAutoCommit()) con.commit();
           
            successMessage("Comments successfully updated to use new comment plugins.");
            
        } catch (Exception e) {
            errorMessage("Problem upgrading database to version 400", e);
            throw new StartupException("Problem upgrading database to version 400", e);
        }
        
        
        // upgrade comments to use new plugin mechanism
        try {
            successMessage("Upgrading existing comments with content-type & plugins");
            
            // look in db and see if comment autoformatting is enabled
            boolean autoformatEnabled = false;
            String autoformat = null;
            PreparedStatement selectIsAutoformtEnabled = con.prepareStatement(
                "select value from roller_properties where name = 'users.comments.autoformat'");
            ResultSet rs = selectIsAutoformtEnabled.executeQuery();
            if (rs.next()) {
                autoformat = rs.getString(1);
                if(autoformat != null && "true".equals(autoformat)) {
                    autoformatEnabled = true;
                }
            }
            
            // look in db and see if comment html escaping is enabled
            boolean htmlEnabled = false;
            String escapehtml = null;
            PreparedStatement selectIsEscapehtmlEnabled = con.prepareStatement(
                "select value from roller_properties where name = 'users.comments.escapehtml'");
            ResultSet rs1 = selectIsEscapehtmlEnabled.executeQuery();
            if (rs1.next()) {
                escapehtml = rs1.getString(1);
                // NOTE: we allow html only when html escaping is OFF
                if(escapehtml != null && !"true".equals(escapehtml)) {
                    htmlEnabled = true;
                }
            }
            
            // first lets set the new 'users.comments.htmlenabled' property
            PreparedStatement addCommentHtmlProp = con.prepareStatement("insert into roller_properties(name,value) values(?,?)");
            addCommentHtmlProp.clearParameters();
            addCommentHtmlProp.setString(1, "users.comments.htmlenabled");
            if(htmlEnabled) {
                addCommentHtmlProp.setString(2, "true");
            } else {
                addCommentHtmlProp.setString(2, "false");
            }
            addCommentHtmlProp.executeUpdate();
            
            // determine content-type for existing comments
            String contentType = "text/plain";
            if(htmlEnabled) {
                contentType = "text/html";
            }
            
            // determine plugins for existing comments
            String plugins = "";
            if(htmlEnabled && autoformatEnabled) {
                plugins = "HTMLSubset,AutoFormat";
            } else if(htmlEnabled) {
                plugins = "HTMLSubset";
            } else if(autoformatEnabled) {
                plugins = "AutoFormat";
            }
            
            // set new comment plugins configuration property 'users.comments.plugins'
            PreparedStatement addCommentPluginsProp = 
                    con.prepareStatement("insert into roller_properties(name,value) values(?,?)");
            addCommentPluginsProp.clearParameters();
            addCommentPluginsProp.setString(1, "users.comments.plugins");
            addCommentPluginsProp.setString(2, plugins);
            addCommentPluginsProp.executeUpdate();
            
            // set content-type for all existing comments
            PreparedStatement updateCommentsContentType = 
                    con.prepareStatement("update roller_comment set posttime=posttime, contenttype = ?");
            updateCommentsContentType.clearParameters();
            updateCommentsContentType.setString(1, contentType);
            updateCommentsContentType.executeUpdate();

            // set plugins for all existing comments
            PreparedStatement updateCommentsPlugins = 
                    con.prepareStatement("update roller_comment set posttime=posttime, plugins = ?");
            updateCommentsPlugins.clearParameters();
            updateCommentsPlugins.setString(1, plugins);
            updateCommentsPlugins.executeUpdate();
            
            if (!con.getAutoCommit()) con.commit();
           
            successMessage("Comments successfully updated to use new comment plugins.");
            
        } catch (Exception e) {
            errorMessage("Problem upgrading database to version 400", e);
            throw new StartupException("Problem upgrading database to version 400", e);
        }
        
        // finally, upgrade db version string to 400
        updateDatabaseVersion(con, 400);
    }
    
    
    /**
     * Upgrade database for Roller 4.1.0
     */
    private void upgradeTo500(Connection con, boolean runScripts) throws StartupException {
        
        // first we need to run upgrade scripts 
        SQLScriptRunner runner = null;
        try {    
            if (runScripts) {
                String handle = getDatabaseHandle(con);
                String scriptPath = handle + "/400-to-500-migration.sql";
                successMessage("Running database upgrade script: "+scriptPath);                
                runner = new SQLScriptRunner(scripts.getDatabaseScript(scriptPath));
                runner.runScript(con, true);
                messages.addAll(runner.getMessages());
            }
        } catch(Exception ex) {
            log.error("ERROR running 500 database upgrade script", ex);
            if (runner != null) messages.addAll(runner.getMessages());
            
            errorMessage("Problem upgrading database to version 500", ex);
            throw new StartupException("Problem upgrading database to version 500", ex);
        }        
    }


    /**
     * Use database product name to get the database script directory name.
     */
    public String getDatabaseHandle(Connection con) throws SQLException {
        
        String productName = con.getMetaData().getDatabaseProductName();
        String handle = "mysql";
        if (       productName.toLowerCase().indexOf("mysql") != -1) {
            handle =  "mysql";
        } else if (productName.toLowerCase().indexOf("derby") != -1) {
            handle =  "derby";
        } else if (productName.toLowerCase().indexOf("hsql") != -1) {
            handle =  "hsqldb";
        } else if (productName.toLowerCase().indexOf("postgres") != -1) {
            handle =  "postgresql";
        } else if (productName.toLowerCase().indexOf("oracle") != -1) {
            handle =  "oracle";
        } else if (productName.toLowerCase().indexOf("microsoft") != -1) {
            handle =  "mssql";
        } else if (productName.toLowerCase().indexOf("db2") != -1) {   
            handle =  "db2";
        }
        
        return handle;
    }

    
    /**
     * Return true if named table exists in database.
     */
    private boolean tableExists(Connection con, String tableName) throws SQLException {
        String[] types = {"TABLE"};
        ResultSet rs = con.getMetaData().getTables(null, null, "%", null);
        while (rs.next()) {
            if (tableName.toLowerCase().equals(rs.getString("TABLE_NAME").toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    
    private int getDatabaseVersion() throws StartupException {
        int dbversion = -1;
        
        // get the current db version
        Connection con = null;
        try {
            con = db.getConnection();
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
            log.error("Couldn't lookup current database version", e);           
        } finally {
            try { if (con != null) con.close(); } catch (Exception ignored) {}
        }       
        return dbversion;
    }
    
    
    private int parseVersionString(String vstring) {        
        int myversion = 0;
        
        // NOTE: this assumes a maximum of 3 digits for the version number
        // so if we get to 10.0 then we'll need to upgrade this
        
        // strip out non-digits
        vstring = vstring.replaceAll("\\Q.\\E", "");
        vstring = vstring.replaceAll("\\D", "");
        if(vstring.length() > 3)
            vstring = vstring.substring(0, 3);
        
        // parse to an int
        try {
            int parsed = Integer.parseInt(vstring);            
            if(parsed < 100) myversion = parsed * 10;
            else myversion = parsed;
        } catch(Exception e) {}  
        
        return myversion;
    }
    

    /**
     * Insert a new database.version property.
     * This should only be called once for new installations
     */
    private void setDatabaseVersion(Connection con, String version) 
            throws StartupException {
        setDatabaseVersion(con, parseVersionString(version));
    }

    /**
     * Insert a new database.version property.
     * This should only be called once for new installations
     */
    private void setDatabaseVersion(Connection con, int version)
            throws StartupException {
        
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("insert into roller_properties "+
                    "values('"+DBVERSION_PROP+"', '"+version+"')");
            
            log.debug("Set database verstion to "+version);
        } catch(SQLException se) {
            throw new StartupException("Error setting database version.", se);
        }
    }
    
    
    /**
     * Update the existing database.version property
     */
    private void updateDatabaseVersion(Connection con, int version)
            throws StartupException {
        
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("update roller_properties "+
                    "set value = '"+version+"'"+
                    "where name = '"+DBVERSION_PROP+"'");
            
            log.debug("Updated database verstion to "+version);
        } catch(SQLException se) {
            throw new StartupException("Error setting database version.", se);
        } 
    }
    
}
