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

import java.io.FileInputStream;
import java.sql.*;
import java.util.*;

/**
 * Roller database consistency checker.<br />
 * Don't run this unless you know what you are doing!</br >
 * 
 * <p>Configuration:<br />
 * 
 * Program looks in current directory for db.properties file with database
 * connection properties driverClassName and connectionUrl. 
 * 
 * Program expects JDBC driver jar to be on classpath.</p>
 * 
 * <p>Usage:<br />
 * 
 * java -cp ./WEB-INF/lib/rollerbeans.jar org.apache.roller.business.utils.ConsistencyCheck<br />
 * 
 * <br />Options:<br />
 * -v Verbose<br />
 * -purge Delete orphans</p>
 */
public class ConsistencyCheck 
{
    /** 
     * Consistency checker, find and optionally delete orphans. 
     */
    public static void main(String[] args) throws Exception
    {
        Properties props = new Properties();
        props.load(new FileInputStream("rollerdb.properties"));
        Connection con = createConnection(props,"");
        
        boolean delete = false;
        boolean verbose = false;
        if (args.length > 0) 
        {
            if ("-purge".equals(args[0])) 
            {
                delete = true;
            }
            else if ("-v".equals(args[0]))
            {
                verbose = true;
            }
        }        
        
        findAndDeleteOrphans(con, delete, verbose);
    }
    
    /** 
     * Create connection based on properties:<br/>
     * - driverClassName<br/>
     * - connectionUrl<br/>
     * - userName<br/>
     * - password<br/>
     */
    public static Connection createConnection(Properties props, String prefix) 
        throws Exception
    {
        Connection con = null;
        if (prefix == null) 
        {
            prefix = "";
        }
        String driverClassName = props.getProperty(prefix+"driverClassName");
        String connectionUrl = props.getProperty(prefix+"connectionUrl");
        String userName = props.getProperty(prefix+"userName");
        String password = props.getProperty(prefix+"password");
        
        Class.forName(driverClassName);
        if (userName != null && password != null)
        {
           con = DriverManager.getConnection(connectionUrl, userName, password);
        }
        else
        {
           con = DriverManager.getConnection(connectionUrl);
        }
        return con;
    }
    
    /** Find and optionally delete all safely deletable orphans. */
    public static void findAndDeleteOrphans(Connection con, boolean delete, boolean verbose) 
        throws SQLException
    {
        // websites with bad user?
        findOrphans(con, "website", "userid", "rolleruser", delete, verbose);
        
        // userroles with bad user?
        findOrphans(con, "userrole", "userid", "rolleruser", delete, verbose);
        
        // folders with bad website?
        findOrphans(con, "folder", "websiteid", "website", delete, verbose);
        
        // bookmarks with bad folder?
        findOrphans(con, "bookmark", "folderid", "folder", delete, verbose);
        
        // weblogcategories with bad website?
        findOrphans(con, "weblogcategory", "websiteid", "website", delete, verbose);
        
        // weblogcategoryassocs with bad category?
        findOrphans(con, "weblogcategoryassoc", "categoryid", "weblogcategory", delete, verbose);
        
        // weblog entries with bad website?
        findOrphans(con, "weblogentry", "websiteid", "website", delete, verbose);
                
        // comments with bad weblogentry?
        findOrphans(con, "comment", "entryid", "weblogentry", delete, verbose);
        
        // Referers with bad website?
        findOrphans(con, "referer", "websiteid", "website", delete, verbose);
        
        // Referers with bad website?
        findOrphans(con, "referer", "entryid", "weblogentry", delete, verbose);              
        
        if (delete)
        {
            correctWeblogEntries(con);
            correctWebsites(con);
            correctFolderTrees(con, delete);
        }
    }
    
    /**
     * @param con
     * @param delete
     */
    private static void correctFolderTrees(Connection con, boolean delete) throws SQLException
    {
        PreparedStatement rootStatement = con.prepareStatement(
            "select a.id from folder as f, folderassoc as a where "+
            "f.websiteid=? and f.id=a.folderid and "+
            "a.relation='PARENT' and a.ancestorid is null");
        PreparedStatement childrenStatement = con.prepareStatement(
            "select id from folderassoc where ancestorid=?");

        // loop through all websites
        Statement websitesStatement = con.createStatement();
        ResultSet websitesResultSet = 
            websitesStatement.executeQuery("select id from website");
        while (websitesResultSet.next()) 
        {
            String websiteId = websitesResultSet.getString(1);
            //debug("Website "+websiteId);
            
            // find root folder(s)
            List rootIds = new LinkedList();
            rootStatement.clearParameters();
            rootStatement.setString(1, websiteId);
            ResultSet rootResultSet = rootStatement.executeQuery();
            while (rootResultSet.next())
            {
                rootIds.add(rootResultSet.getString(1));
            }
            if (rootIds.size() > 1) 
            {
                // too many roots, need to figure out which are bogus
                Iterator rootIter = rootIds.iterator();
                while (rootIter.hasNext())
                {
                    String rootId = (String)rootIter.next();
                    childrenStatement.clearParameters();
                    childrenStatement.setString(1, rootId);
                    ResultSet childrenResultSet = childrenStatement.executeQuery();
                    List childIds = new LinkedList();
                    while (childrenResultSet.next()) 
                    {
                        childIds.add(childrenResultSet.getString(1));
                    }
                    if (childIds.size() == 0)
                    {
                        debug("Folder "+rootId+" in website "+websiteId+"is a bogus root folder!");
                    }
                }
            }
            else if (rootIds.size() == 0)
            {
                debug("Website "+websiteId+" has no root folder!");
            }
        }
    }

    private static void correctWeblogEntries(Connection con) throws SQLException
    {
        List entries = findOrphans(con, "weblogentry", "categoryid", "weblogcategory", false, false);
        Iterator entryIter = entries.iterator();
        while (entryIter.hasNext())
        {
            String entryid = (String) entryIter.next();
            Statement websiteSt = con.createStatement();
            ResultSet websiteRs = websiteSt.executeQuery(
                "select websiteid from weblogentry where id="+entryid);
            websiteRs.first();
            String websiteid = websiteRs.getString(0);
            
            String rootid = getRootCategoryId(con, websiteid);
            Statement st = con.createStatement();
            st.executeUpdate("update weblogentry set categoryid='"+rootid+"' "
                           +" where id='"+entryid+"'");
        }   
    }
    
    public static void correctWebsites(Connection con) throws SQLException
    {
        List websites = findOrphans(con, "website", "defaultcatid", "weblogcategory", false, false);
        Iterator websiteIter = websites.iterator();
        while (websiteIter.hasNext())
        {
            String websiteid = (String) websiteIter.next();
            String rootid = getRootCategoryId(con, websiteid);
            Statement st = con.createStatement();
            st.executeUpdate("update website set defaultcatid='"+rootid+"' "
                    +" where id='"+websiteid+"'");
        }
    
        websites = findOrphans(con, "website", "bloggercatid", "weblogcategory", false, false);
        websiteIter = websites.iterator();
        while (websiteIter.hasNext())
        {
            String websiteid = (String) websiteIter.next();
            String rootid = getRootCategoryId(con, websiteid);
            Statement st = con.createStatement();
            st.executeUpdate("update website set bloggercatid='"+rootid+"' "
                            +"where id='"+websiteid+"'");
        }
    }
    
    public static String getRootCategoryId(Connection con, String websiteid)
        throws SQLException
    {
        Statement st = con.createStatement();
        String query = 
            "select c.id from weblogcategory as c, weblogcategoryassoc as a "
            +"where a.categoryid=c.id and a.ancestorid is null "
            +"and c.websiteid ='"+websiteid+"'";
        //System.out.println(query);
        ResultSet rs = st.executeQuery(query);        
        rs.next();
        return rs.getString(1);
    }
    
    /** Find orphans, records in a manytable that refer to a onetable that 
     * no longer exists.
     * @param con       Database connection to be used.
     * @param manytable Name of the manytable.
     * @param fkname    Name of the foreign key field in the manytable.
     * @param onetable  Name of the onetable.
     * @param delete    True if orphans in manytable are to be deleted.
     * @return          List of orphans found (will be empty if delete is true.
     * @throws SQLException
     */
    public static List findOrphans(
        Connection con, String manytable, String fkname, String onetable, boolean delete, boolean verbose) 
        throws SQLException
    {
        List orphans = new LinkedList();
        
        Statement stall = con.createStatement();
        ResultSet rsall = stall.executeQuery(
            "select id,"+fkname+" as fk from "+manytable);
        while (rsall.next()) 
        {
            String id = rsall.getString("id");
            String fk = rsall.getString("fk");
            if (fk != null)
            {
                Statement stone = con.createStatement();
                ResultSet rsone = stone.executeQuery(
                    "select id from "+onetable+" where id='"+fk+"' limit 1");
                if (!rsone.next()) 
                {
                    orphans.add(id);  
                    System.out.println("   Found orphan in "+manytable+" id="+id); 
                }                
            }
        } 
        
        if (!delete)
        {
            debug("Orphans found in "+manytable+" = "+orphans.size());
            if (verbose)
            {
                Iterator iter = orphans.iterator();
                while (iter.hasNext())
                {
                    String id = (String) iter.next();
                    debug("   "+manytable+" id="+id);
                }
            }
        }
        else
        {
            debug("Deleting orphans found in "+manytable+" count = "+orphans.size());            
            Iterator iter = orphans.iterator();
            while (iter.hasNext())
            {
                String id = (String) iter.next();
                Statement stdel = con.createStatement();
                stdel.executeUpdate("delete from "+manytable+" where id='"+id+"'");
            }
            orphans = new LinkedList();
        }
        return orphans;
    }
    
    private static void debug(String msg) 
    {
        System.out.println("DEBUG: "+msg);   
    }
}
