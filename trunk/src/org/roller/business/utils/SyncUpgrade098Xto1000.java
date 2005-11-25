package org.roller.business.utils;

import java.sql.*;
import java.util.*;
import java.io.*;

/**
 * Synchronize a source 0.9.8.X database with a destination 1.0.0.0 database.
 */
public class SyncUpgrade098Xto1000 
{
    private Map rootCategoryIds = new Hashtable(); // keyed by website id
    private static boolean debug = true;
    
    public void syncUpgrade(Connection srccon, Connection destcon) 
    throws Exception
    {    
        rootCategoryIds = buildRootCategoryMap(destcon);        
        
        syncRolleruserTable(srccon,destcon);
        syncUserroleTable(srccon,destcon);
        syncWebsiteTable(srccon,destcon);
        syncWebpageTable(srccon,destcon);
        syncWeblogcategoryTable(srccon,destcon);        
        syncFolderTable(srccon,destcon);
        syncBookmarkTable(srccon,destcon);
        syncWeblogentryTable(srccon,destcon);
        syncCommentTable(srccon,destcon);
        syncRefererTable(srccon,destcon);
        
        ConsistencyCheck.findAndDeleteOrphans(destcon, true, debug);
    }
    public Map buildRootCategoryMap(Connection destcon) throws Exception 
    {        
        Hashtable map = new Hashtable();
        Statement destStmt = destcon.createStatement();
        ResultSet destSet = destStmt.executeQuery(
           "select c.websiteid,c.id from "
           +"weblogcategory as c, weblogcategoryassoc as a "
           +"where c.id=a.categoryid and a.ancestorid is null");
        while (destSet.next()) 
        {
            String websiteid = destSet.getString(1);
            String categoryid = destSet.getString(2);
            map.put(websiteid, categoryid);
        }
        return map;
    }
    private void info(String s) 
    {
        System.out.println(s);
    }
    private void debug(String s) 
    {
        if (debug) System.out.println(s);
    }
    private void purgeDeleted(Connection srccon, Connection destcon, String tableName) 
    throws Exception
    {
        PreparedStatement destRows = destcon.prepareStatement(
            "select id from "+tableName);
        PreparedStatement deleteRow = destcon.prepareStatement(
            "delete from "+tableName+" where id=?");
        PreparedStatement srcExists = srccon.prepareStatement(
            "select id from "+tableName+" where id=?");
        ResultSet destSet = destRows.executeQuery();
        while (destSet.next())
        {
            String id = destSet.getString(1);
            srcExists.clearParameters();
            srcExists.setString(1, id);
            ResultSet existsSet = srcExists.executeQuery();
            if (!existsSet.next() && !id.endsWith("R")) // kludge alert
            {
                deleteRow.clearParameters();
                deleteRow.setString(1, id);
                deleteRow.executeUpdate();
                info("Deleting from "+tableName+" id="+id);
            }
        }        
    }
    private void purgeAssocs(Connection destcon, String assocTable, String mainTable, String fkeyName) 
    throws Exception
    {
        info("--- purgeAssocs --- "+assocTable);
        PreparedStatement assocRows = destcon.prepareStatement(
            "select id,"+fkeyName+",ancestorid from "+assocTable);
        PreparedStatement mainExists = destcon.prepareStatement(
            "select id from "+mainTable+" where id=?");
        PreparedStatement deleteMain = destcon.prepareStatement(
            "delete from "+assocTable+" where "+fkeyName+"=?");
        PreparedStatement ancestorExists = destcon.prepareStatement(
            "select id from "+mainTable+" where id=?");
        PreparedStatement deleteAncestor = destcon.prepareStatement(
            "delete from "+assocTable+" where ancestorid=?");
        ResultSet assocSet = assocRows.executeQuery();
        while (assocSet.next())
        {
            String id = assocSet.getString(1);
            String fkey = assocSet.getString(2);
            String akey = assocSet.getString(3);
            mainExists.clearParameters();
            mainExists.setString(1, fkey);
            ResultSet existsSet = mainExists.executeQuery();
            if (!existsSet.next())
            {
                deleteMain.clearParameters();
                deleteMain.setString(1, fkey);
                deleteMain.executeUpdate();
                info("Deleting from "+assocTable+" where "+fkeyName+"="+id);
            }
            ancestorExists.clearParameters();
            ancestorExists.setString(1, akey);
            ResultSet ancestorSet = ancestorExists.executeQuery();
            if (!ancestorSet.next())
            {
                deleteAncestor.clearParameters();
                deleteAncestor.setString(1, akey);
                deleteAncestor.executeUpdate();
                info("Deleting from "+assocTable+" where ancestorid="+id);
            }
        }            
    }
    private void syncRolleruserTable(Connection srccon, Connection destcon) 
    throws Exception
    {
        info("--- syncRolleruserTable ---");        
        Set existing = new  TreeSet();         
        PreparedStatement destExistsStmt = destcon.prepareStatement(
            "select id from rolleruser where id=?");
        PreparedStatement destInsert = destcon.prepareStatement(
            "insert into rolleruser "
            +"(id,username,passphrase,fullname,emailaddress,datecreated) "
            +"values (?,?,?,?,?,?)");
        PreparedStatement destUpdate = destcon.prepareStatement(
            "update rolleruser set id=?, username=?, passphrase=?, "
            +"fullname=?, emailaddress=?, datecreated=? where id=?");

        Statement srcStmt = srccon.createStatement();
        ResultSet srcSet = srcStmt.executeQuery(
           "select id,username,passphrase,fullname,emailaddress,datecreated "
           +"from rolleruser");
        while (srcSet.next()) 
        {
            String id = srcSet.getString(1);
            existing.add(id);
            destExistsStmt.clearParameters();
            destExistsStmt.setString(1,id);
            ResultSet destSet = destExistsStmt.executeQuery();
            if (!destSet.first())
            {
                debug("Inserting rolleruser id="+id);
                destInsert.clearParameters();
                destInsert.setString(1, srcSet.getString(1));
                destInsert.setString(2, srcSet.getString(2));
                destInsert.setString(3, srcSet.getString(3));
                destInsert.setString(4, srcSet.getString(4));
                destInsert.setString(5, srcSet.getString(5));
                destInsert.setDate(  6, srcSet.getDate(6));
                destInsert.executeUpdate();
            }
            else
            {
                debug("Updating rolleruser id="+id);
                destUpdate.clearParameters();
                destUpdate.setString(1, srcSet.getString(1));
                destUpdate.setString(2, srcSet.getString(2));
                destUpdate.setString(3, srcSet.getString(3));
                destUpdate.setString(4, srcSet.getString(4));
                destUpdate.setString(5, srcSet.getString(5));
                destUpdate.setDate(  6, srcSet.getDate(6));
                destUpdate.setString(7, srcSet.getString(1));
                destUpdate.executeUpdate();
            }                
        } 
        purgeDeleted(srccon,destcon,"rolleruser");
    }
    private void syncUserroleTable(Connection srccon, Connection destcon) 
    throws Exception
    {
        info("--- syncUserroleTable ---");
        
        PreparedStatement destExistsStmt = destcon.prepareStatement(
            "select id from userrole where id=?");
        PreparedStatement destInsert = destcon.prepareStatement(
            "insert into userrole (id,rolename,username,userid) "
            +"values (?,?,?,?)");
        PreparedStatement destUpdate = destcon.prepareStatement(
            "update userrole set id=?, rolename=?, username=?, userid=? "
            +"where id=?");
        PreparedStatement parentExistsStmt = destcon.prepareStatement(
            "select id from rolleruser where id=?");        

        Statement srcStmt = srccon.createStatement();
        ResultSet srcSet = srcStmt.executeQuery(
           "select id,rolename,username,userid from userrole");
        while (srcSet.next()) 
        {
            String id = srcSet.getString(1);
            String userid = srcSet.getString(4);
            
            destExistsStmt.clearParameters();
            destExistsStmt.setString(1, id);
            ResultSet destSet = destExistsStmt.executeQuery();
            
            parentExistsStmt.clearParameters();
            parentExistsStmt.setString(1, userid);
            ResultSet parentSet = parentExistsStmt.executeQuery();
            
            boolean parentExists = parentSet.first();
            if (!destSet.first() && parentExists)
            {
                debug("Inserting userrole id="+id);
                destInsert.clearParameters();
                destInsert.setString(1, srcSet.getString(1));
                destInsert.setString(2, srcSet.getString(2));
                destInsert.setString(3, srcSet.getString(3));
                destInsert.setString(4, srcSet.getString(4));
                destInsert.executeUpdate();
            }
            else if (parentExists)
            {
                debug("Updating userrole id="+id);
                destUpdate.clearParameters();
                destUpdate.setString(1, srcSet.getString(1));
                destUpdate.setString(2, srcSet.getString(2));
                destUpdate.setString(3, srcSet.getString(3));
                destUpdate.setString(4, srcSet.getString(4));
                destUpdate.setString(5, srcSet.getString(1));
                destUpdate.executeUpdate();
            }                
            else 
            {
                info("Not copying userrole id="+id);
            }
        }     
        purgeDeleted(srccon,destcon,"userrole");
    }
    private void syncWebsiteTable(Connection srccon, Connection destcon) 
    throws Exception
    {
        info("--- syncWebsiteTable ---");
        
        int id_num=1;
        int name_num=2;
        int description_num=3;
        int userid_num=4;
        int defaultpageid_num=5; 
        int weblogdayid_num=6;
        int ignorewords_num=7;    
        int enablebloggerapi_num=8; 
        int editorpage_num=9;    
        int bloggercatid_num=10;   
        int allowcomments_num=11; 
        
        PreparedStatement destExistsStmt = destcon.prepareStatement(
            "select id from website where id=?");
        PreparedStatement destInsert = destcon.prepareStatement(
            "insert into website (id,name,description,userid,defaultpageid,"
            +"weblogdayid,ignorewords,enablebloggerapi,editorpage,"
            +"bloggercatid,allowcomments,defaultcatid) values (?,?,?,?,?,?,?,?,?,?,?,?)");
        PreparedStatement destUpdate = destcon.prepareStatement(
        "update website set id=?,name=?,description=?,userid=?,defaultpageid=?,"
            +"weblogdayid=?,ignorewords=?,enablebloggerapi=?,editorpage=?,"
            +"bloggercatid=?,allowcomments=? where id=?");
        PreparedStatement parentExistsStmt = destcon.prepareStatement(
            "select id from rolleruser where id=?");
        
        PreparedStatement insertRootCategory = destcon.prepareStatement(
            "insert into weblogcategory (id,name,description,websiteid,image) "+
            "values (?,'root','root',?,NULL)");                        
        PreparedStatement insertRootCategoryAssoc = destcon.prepareStatement(
           "insert into weblogcategoryassoc (id,categoryid,ancestorid,relation)"
            +" values (?,?,NULL,'PARENT')");                        
        
        Statement srcStmt = srccon.createStatement();
        ResultSet srcSet = srcStmt.executeQuery(
            "select id,name,description,userid,defaultpageid,weblogdayid,"
           +"ignorewords,enablebloggerapi,editorpage,bloggercatid,allowcomments"
           +" from website");        
        while (srcSet.next()) 
        {
            String id = srcSet.getString(id_num);
            String userid = srcSet.getString(userid_num);
            
            destExistsStmt.clearParameters();
            destExistsStmt.setString(id_num, id);            
            ResultSet destSet = destExistsStmt.executeQuery();
            
            parentExistsStmt.clearParameters();
            parentExistsStmt.setString(id_num, userid);
            ResultSet parentSet = parentExistsStmt.executeQuery();
            boolean parentExists = parentSet.first();
            
            if (!destSet.first() && parentExists)
            {
                debug("Inserting website id="+id);
                destInsert.clearParameters();
                destInsert.setString(id_num, srcSet.getString(id_num));
                destInsert.setString(name_num, srcSet.getString(name_num));
                destInsert.setString(description_num, srcSet.getString(description_num));
                destInsert.setString(userid_num, srcSet.getString(userid_num));
                destInsert.setString(defaultpageid_num, srcSet.getString(defaultpageid_num));
                destInsert.setString(weblogdayid_num, srcSet.getString(weblogdayid_num));
                destInsert.setString(ignorewords_num, srcSet.getString(ignorewords_num));
                destInsert.setBoolean(enablebloggerapi_num, srcSet.getBoolean(enablebloggerapi_num));
                destInsert.setString(editorpage_num, srcSet.getString(editorpage_num));
                destInsert.setString(bloggercatid_num, srcSet.getString(bloggercatid_num));
                destInsert.setBoolean(allowcomments_num, srcSet.getBoolean(allowcomments_num));
                destInsert.setString(12, id+"R"); // default category
                destInsert.executeUpdate();
                
                // 098 had no root category per website, so create one
                insertRootCategory.clearParameters();
                insertRootCategory.setString(1, id+"R");
                insertRootCategory.setString(2, id);
                insertRootCategory.executeUpdate();
                rootCategoryIds.put(id, id+"R"); // and add it to map
                debug("   Inserting root weblogcategory id="+id+"R");
                
                // Create category assoc to go with root category
                insertRootCategoryAssoc.clearParameters();
                insertRootCategoryAssoc.setString(1, id+"A");
                insertRootCategoryAssoc.setString(2, id+"R");
                insertRootCategoryAssoc.executeUpdate();
                debug("   Inserting root weblogcategoryassoc id="+id+"A");
            }
            else if (parentExists)
            {
                debug("Updating website id="+id);
                destUpdate.clearParameters();
                destUpdate.setString(id_num, srcSet.getString(id_num));
                destUpdate.setString(name_num, srcSet.getString(name_num));
                destUpdate.setString(description_num, srcSet.getString(description_num));
                destUpdate.setString(userid_num, srcSet.getString(userid_num));
                destUpdate.setString(defaultpageid_num, srcSet.getString(defaultpageid_num));
                destUpdate.setString(weblogdayid_num, srcSet.getString(weblogdayid_num));
                destUpdate.setString(ignorewords_num, srcSet.getString(ignorewords_num));
                destUpdate.setBoolean(enablebloggerapi_num, srcSet.getBoolean(enablebloggerapi_num));
                destUpdate.setString(editorpage_num, srcSet.getString(editorpage_num));
                destUpdate.setString(bloggercatid_num, srcSet.getString(bloggercatid_num));
                destUpdate.setBoolean(allowcomments_num, srcSet.getBoolean(allowcomments_num));
                destUpdate.setString(12, srcSet.getString(id_num));
                destUpdate.executeUpdate();
            }   
            else 
            {
                info("Not copying website id="+id);
            }
        }     
        purgeDeleted(srccon,destcon,"website");
    }    
    private void syncWebpageTable(Connection srccon, Connection destcon) throws Exception
    {
        info("--- syncWebpageTable ---");
        
        String columns = "id,name,description,link,websiteid,template,updatetime";
        int id_num=1;
        int name_num=2;
        int description_num=3;
        int link_num=4;
        int websiteid_num=5;
        int template_num=6;
        int updatetime_num=7;
        
        PreparedStatement destExistsStmt = destcon.prepareStatement(
            "select id from webpage where id=?");
        PreparedStatement destInsert = destcon.prepareStatement(
            "insert into webpage "+"("+columns+") "+"values (?,?,?,?,?,?,?)");
        PreparedStatement destUpdate = destcon.prepareStatement(
            "update webpage set id=?,name=?,description=?,link=?,websiteid=?,"
            +"template=?,updatetime=? where id=?");
        PreparedStatement parentExistsStmt = destcon.prepareStatement(
            "select id from website where id=?");        

        Statement srcStmt = srccon.createStatement();
        ResultSet srcSet = srcStmt.executeQuery("select "+columns+" from webpage");
        while (srcSet.next()) 
        {
            String id = srcSet.getString(id_num);
            String userid = srcSet.getString(websiteid_num);
            
            destExistsStmt.clearParameters();
            destExistsStmt.setString(id_num, id);
            ResultSet destSet = destExistsStmt.executeQuery();
            
            parentExistsStmt.clearParameters();
            parentExistsStmt.setString(id_num, userid);
            ResultSet parentSet = parentExistsStmt.executeQuery();
            
            boolean parentExists = parentSet.first();
            if (!destSet.first() && parentExists)
            {
                debug("Inserting webpage id="+id);
                destInsert.clearParameters();
                destInsert.setString(id_num, srcSet.getString(id_num));
                destInsert.setString(name_num, srcSet.getString(name_num));
                destInsert.setString(description_num, srcSet.getString(description_num));
                destInsert.setString(link_num, srcSet.getString(link_num));
                destInsert.setString(websiteid_num, srcSet.getString(websiteid_num));
                destInsert.setString(template_num, srcSet.getString(template_num));
                destInsert.setTimestamp(updatetime_num, srcSet.getTimestamp(updatetime_num));
                destInsert.executeUpdate();
            }
            else if (parentExists)
            {
                debug("Updating webpage id="+id);
                destUpdate.clearParameters();
                destUpdate.setString(id_num, srcSet.getString(id_num));
                destUpdate.setString(name_num, srcSet.getString(name_num));
                destUpdate.setString(description_num, srcSet.getString(description_num));
                destUpdate.setString(link_num, srcSet.getString(link_num));
                destUpdate.setString(websiteid_num, srcSet.getString(websiteid_num));
                destUpdate.setString(template_num, srcSet.getString(template_num));
                destUpdate.setTimestamp(updatetime_num, srcSet.getTimestamp(updatetime_num));
                destUpdate.setString(8, srcSet.getString(id_num));
                destUpdate.executeUpdate();
            }                
            else 
            {
                info("Not copying webpage id="+id);
            }
        }     
        purgeDeleted(srccon,destcon,"webpage");
    }
    private void syncWeblogcategoryTable(Connection srccon, Connection destcon) throws Exception
    {
        info("--- syncWeblogcategoryTable ---");
        
        String columns = "id,name,description,websiteid,image";
        int id_num=1;
        int name_num=2;
        int description_num=3;
        int websiteid_num=4;
        int image_num=5;
        
        PreparedStatement destExistsStmt = destcon.prepareStatement(
            "select id from weblogcategory where id=?");
        PreparedStatement destInsert = destcon.prepareStatement(
            "insert into weblogcategory "+"("+columns+") "+"values (?,?,?,?,?)");
        PreparedStatement destUpdate = destcon.prepareStatement(
            "update weblogcategory set id=?,name=?,description=?,websiteid=?,"
            +"image=? where id=?");
        PreparedStatement parentExistsStmt = destcon.prepareStatement(
            "select id from website where id=?");        

        PreparedStatement assocInsert = destcon.prepareStatement(
            "insert into weblogcategoryassoc "
            +"(id,categoryid,ancestorid,relation) "+"values (?,?,?,'PARENT')");

        Statement srcStmt = srccon.createStatement();
        ResultSet srcSet = srcStmt.executeQuery("select "+columns+" from weblogcategory");
        while (srcSet.next()) 
        {
            String id = srcSet.getString(id_num);
            String websiteid = srcSet.getString(websiteid_num);
            
            destExistsStmt.clearParameters();
            destExistsStmt.setString(id_num, id);
            ResultSet destSet = destExistsStmt.executeQuery();
            
            parentExistsStmt.clearParameters();
            parentExistsStmt.setString(id_num, websiteid);
            ResultSet parentSet = parentExistsStmt.executeQuery();
            
            boolean parentExists = parentSet.first();
            if (!destSet.first() && parentExists)
            {
                debug("Inserting weblogcategory id="+id);
                destInsert.clearParameters();
                destInsert.setString(id_num, id);
                destInsert.setString(name_num, srcSet.getString(name_num));
                destInsert.setString(description_num, srcSet.getString(description_num));
                destInsert.setString(websiteid_num, srcSet.getString(websiteid_num));
                destInsert.setString(image_num, srcSet.getString(image_num));
                destInsert.executeUpdate();
                
                // Create category assoc for new category 
                assocInsert.clearParameters();
                assocInsert.setString(1, id+"A");
                assocInsert.setString(2, id);
                assocInsert.setString(3, (String)rootCategoryIds.get(websiteid));
                assocInsert.executeUpdate();                
            }
            else if (parentExists)
            {
                debug("Updating weblogcategory id="+id);
                destUpdate.clearParameters();
                destUpdate.setString(id_num, id);
                destUpdate.setString(name_num, srcSet.getString(name_num));
                destUpdate.setString(description_num, srcSet.getString(description_num));
                destUpdate.setString(websiteid_num, srcSet.getString(websiteid_num));
                destUpdate.setString(image_num, srcSet.getString(image_num));
                destUpdate.setString(6, srcSet.getString(id_num));
                destUpdate.executeUpdate();
            }                
            else 
            {
                info("Not copying weblogcategory id="+id);
            }
        }     
        purgeDeleted(srccon,destcon,"weblogcategory");
        purgeAssocs(destcon,"weblogcategoryassoc","weblogcategory","categoryid");
    }
    private void syncFolderTable(Connection srccon, Connection destcon) throws Exception
    {
        info("--- syncFolderTable ---");
        
        String columns = "id,name,description,parentid,websiteid";
        int id_num=1;
        int name_num=2;
        int description_num=3;
        int parentid_num=4;
        int websiteid_num=5;
        
        PreparedStatement destExistsStmt = destcon.prepareStatement(
            "select id from folder where id=?");
        PreparedStatement destInsert = destcon.prepareStatement(
            "insert into folder "+"("+columns+") "+"values (?,?,?,?,?)");
        PreparedStatement destUpdate = destcon.prepareStatement(
            "update folder set id=?,name=?,description=?,parentid=?,websiteid=?"
            +" where id=?");
        PreparedStatement parentExistsStmt = destcon.prepareStatement(
            "select id from website where id=?");        

        PreparedStatement assocInsert = destcon.prepareStatement(
            "insert into folderassoc "
            +"(id,folderid,ancestorid,relation) "+"values (?,?,?,'PARENT')");

        Statement srcStmt = srccon.createStatement();
        ResultSet srcSet = srcStmt.executeQuery("select "+columns+" from folder");
        while (srcSet.next()) 
        {
            String id = srcSet.getString(id_num);
            String userid = srcSet.getString(websiteid_num);
            
            destExistsStmt.clearParameters();
            destExistsStmt.setString(id_num, id);
            ResultSet destSet = destExistsStmt.executeQuery();
            
            parentExistsStmt.clearParameters();
            parentExistsStmt.setString(id_num, userid);
            ResultSet parentSet = parentExistsStmt.executeQuery();
            
            boolean parentExists = parentSet.first();
            if (!destSet.first() && parentExists)
            {
                debug("Inserting folder id="+id);
                destInsert.clearParameters();
                destInsert.setString(id_num, id);
                destInsert.setString(name_num, srcSet.getString(name_num));
                destInsert.setString(description_num, srcSet.getString(description_num));
                destInsert.setString(parentid_num, srcSet.getString(parentid_num));
                destInsert.setString(websiteid_num, srcSet.getString(websiteid_num));
                destInsert.executeUpdate();
                
                // Create folder assoc for new folder 
                assocInsert.clearParameters();
                assocInsert.setString(1, id+"A");
                assocInsert.setString(2, id);
                assocInsert.setString(3, srcSet.getString(parentid_num));
                assocInsert.executeUpdate();                
            }
            else if (parentExists)
            {
                debug("Updating folder id="+id);
                destUpdate.clearParameters();
                destUpdate.setString(id_num, id);
                destUpdate.setString(name_num, srcSet.getString(name_num));
                destUpdate.setString(description_num, srcSet.getString(description_num));
                destUpdate.setString(parentid_num, srcSet.getString(parentid_num));
                destUpdate.setString(websiteid_num, srcSet.getString(websiteid_num));
                destUpdate.setString(6, srcSet.getString(id_num));
                destUpdate.executeUpdate();
            }                
            else 
            {
                info("Not copying folder id="+id);
            }
        }     
        purgeDeleted(srccon,destcon,"folder");
        purgeAssocs(destcon,"folderassoc","folder","folderid");
    }
    private void syncBookmarkTable(Connection srccon, Connection destcon) throws Exception
    {
        info("--- syncBookmarkTable ---");
        
        String columns = "id,folderid,name,description,url,weight,priority,image,feedurl";
        int id_num=1;
        int folderid_num=2;
        int name_num=3;
        int description_num=4;
        int url_num=5;
        int weight_num=6;
        int priority_num=7;
        int image_num=8;
        int feedurl_num=9;
        
        PreparedStatement destExistsStmt = destcon.prepareStatement(
            "select id from bookmark where id=?");
        PreparedStatement destInsert = destcon.prepareStatement(
            "insert into bookmark "+"("+columns+") "+"values (?,?,?,?,?,?,?,?,?)");
        PreparedStatement destUpdate = destcon.prepareStatement(
            "update bookmark set id=?,folderid=?,name=?,description=?,url=?,weight=?,priority=?,image=?,feedurl=?"
            +" where id=?");
        PreparedStatement parentExistsStmt = destcon.prepareStatement(
            "select id from folder where id=?");        

        Statement srcStmt = srccon.createStatement();
        ResultSet srcSet = srcStmt.executeQuery("select "+columns+" from bookmark");
        while (srcSet.next()) 
        {
            String id = srcSet.getString(id_num);
            String folderid = srcSet.getString(folderid_num);
            
            destExistsStmt.clearParameters();
            destExistsStmt.setString(id_num, id);
            ResultSet destSet = destExistsStmt.executeQuery();
            
            parentExistsStmt.clearParameters();
            parentExistsStmt.setString(id_num, folderid);
            ResultSet parentSet = parentExistsStmt.executeQuery();
            
            boolean parentExists = parentSet.first();
            if (!destSet.first() && parentExists)
            {
                debug("Inserting bookmark id="+id);
                destInsert.clearParameters();
                destInsert.setString(id_num, id);
                destInsert.setString(folderid_num, srcSet.getString(folderid_num));
                destInsert.setString(name_num, srcSet.getString(name_num));
                destInsert.setString(description_num, srcSet.getString(name_num));
                destInsert.setString(url_num, srcSet.getString(url_num));
                destInsert.setInt(weight_num, srcSet.getInt(weight_num));
                destInsert.setInt(priority_num, srcSet.getInt(priority_num));
                destInsert.setString(image_num, srcSet.getString(image_num));
                destInsert.setString(feedurl_num, srcSet.getString(feedurl_num));
                destInsert.executeUpdate();
            }
            else if (parentExists)
            {
                debug("Updating bookmark id="+id);
                destUpdate.clearParameters();
                destUpdate.setString(id_num, id);
                destUpdate.setString(folderid_num, srcSet.getString(folderid_num));
                destUpdate.setString(name_num, srcSet.getString(name_num));
                destUpdate.setString(description_num, srcSet.getString(name_num));
                destUpdate.setString(url_num, srcSet.getString(url_num));
                destUpdate.setString(weight_num, srcSet.getString(weight_num));
                destUpdate.setString(priority_num, srcSet.getString(priority_num));
                destUpdate.setString(image_num, srcSet.getString(image_num));
                destUpdate.setString(feedurl_num, srcSet.getString(feedurl_num));
                destUpdate.setString(10, srcSet.getString(id_num));
                destUpdate.executeUpdate();
            }                
            else 
            {
                info("Not copying bookmark id="+id);
            }
        }     
        purgeDeleted(srccon,destcon,"bookmark");
    }
    private void syncWeblogentryTable(Connection srccon, Connection destcon) throws Exception
    {
        info("--- syncWeblogentryTable ---");
        
        String columns = "id,anchor,title,text,pubtime,updatetime,websiteid,categoryid,publishentry";
        int id_num=1;
        int anchor_num=2;
        int title_num=3;
        int text_num=4;
        int pubtime_num=5;
        int updatetime_num=6;
        int websiteid_num=7;
        int categoryid_num=8;
        int publishentry_num=9;
        
        PreparedStatement destExistsStmt = destcon.prepareStatement(
            "select id from weblogentry where id=?");
        PreparedStatement destInsert = destcon.prepareStatement(
            "insert into weblogentry "+"("+columns+") "+"values (?,?,?,?,?,?,?,?,?)");
        PreparedStatement destUpdate = destcon.prepareStatement(
            "update weblogentry set id=?,anchor=?,title=?,text=?,pubtime=?,"
            +"updatetime=?,websiteid=?,categoryid=?,publishentry=?"
            +" where id=?");
        
        PreparedStatement parentExistsStmt = destcon.prepareStatement(
            "select id from website where id=?");        
        PreparedStatement categoryExistsStmt = destcon.prepareStatement(
            "select id from weblogcategory where id=?");        

        Statement srcStmt = srccon.createStatement();
        ResultSet srcSet = srcStmt.executeQuery("select "+columns+" from weblogentry");
        while (srcSet.next()) 
        {
            String id = srcSet.getString(id_num);
            String websiteid = srcSet.getString(websiteid_num);
            String categoryid = srcSet.getString(categoryid_num);
            
            destExistsStmt.clearParameters();
            destExistsStmt.setString(id_num, id);
            ResultSet destSet = destExistsStmt.executeQuery();
            
            parentExistsStmt.clearParameters();
            parentExistsStmt.setString(id_num, websiteid);
            ResultSet parentSet = parentExistsStmt.executeQuery();
            boolean parentExists = parentSet.first();
            
            categoryExistsStmt.clearParameters();
            categoryExistsStmt.setString(id_num, categoryid);
            ResultSet categorySet = categoryExistsStmt.executeQuery();
            boolean categoryExists = categorySet.first();
            
            if (!destSet.first() && parentExists && categoryExists)
            {
                debug("Inserting weblogentry id="+id);
                destInsert.clearParameters();
                destInsert.setString(id_num, id);
                destInsert.setString(anchor_num, srcSet.getString(anchor_num));
                destInsert.setString(title_num, srcSet.getString(title_num));
                destInsert.setString(text_num, srcSet.getString(text_num));
                destInsert.setTimestamp(pubtime_num, srcSet.getTimestamp(pubtime_num));
                destInsert.setTimestamp(updatetime_num, srcSet.getTimestamp(updatetime_num));
                destInsert.setString(websiteid_num, srcSet.getString(websiteid_num));
                destInsert.setString(categoryid_num, srcSet.getString(categoryid_num));
                destInsert.setBoolean(publishentry_num, srcSet.getBoolean(publishentry_num));
                destInsert.executeUpdate();
            }
            else if (parentExists && categoryExists)
            {
                debug("Updating weblogentry id="+id);
                destUpdate.clearParameters();
                destUpdate.setString(id_num, id);
                destUpdate.setString(anchor_num, srcSet.getString(anchor_num));
                destUpdate.setString(title_num, srcSet.getString(title_num));
                destUpdate.setString(text_num, srcSet.getString(text_num));
                destUpdate.setTimestamp(pubtime_num, srcSet.getTimestamp(pubtime_num));
                destUpdate.setTimestamp(updatetime_num, srcSet.getTimestamp(updatetime_num));
                destUpdate.setString(websiteid_num, srcSet.getString(websiteid_num));
                destUpdate.setString(categoryid_num, srcSet.getString(categoryid_num));
                destUpdate.setBoolean(publishentry_num, srcSet.getBoolean(publishentry_num));
                destUpdate.setString(10, srcSet.getString(id_num));
                destUpdate.executeUpdate();
            }                
            else 
            {
                info("Not copying weblogentry id="+id);
            }
        }     
        purgeDeleted(srccon,destcon,"weblogentry");
    }
    private void syncCommentTable(Connection srccon, Connection destcon) throws Exception
    {
        info("--- syncCommentTable ---");
        
        String columns = "id,entryid,name,email,url,content,posttime";
        int id_num=1;
        int entryid_num=2;
        int name_num=3;
        int email_num=4;
        int url_num=5;
        int content_num=6;
        int posttime_num=7;
        
        PreparedStatement destExistsStmt = destcon.prepareStatement(
            "select id from comment where id=?");
        PreparedStatement destInsert = destcon.prepareStatement(
            "insert into comment "+"("+columns+") "+"values (?,?,?,?,?,?,?)");
        PreparedStatement destUpdate = destcon.prepareStatement(
            "update comment set id=?,entryid=?,name=?,email=?,url=?,content=?,posttime=?"
            +" where id=?");
        
        PreparedStatement parentExistsStmt = destcon.prepareStatement(
            "select id from weblogentry where id=?");        

        Statement srcStmt = srccon.createStatement();
        ResultSet srcSet = srcStmt.executeQuery("select "+columns+" from comment");
        while (srcSet.next()) 
        {
            String id = srcSet.getString(id_num);
            String entryid = srcSet.getString(entryid_num);
            
            destExistsStmt.clearParameters();
            destExistsStmt.setString(id_num, id);
            ResultSet destSet = destExistsStmt.executeQuery();
            
            parentExistsStmt.clearParameters();
            parentExistsStmt.setString(id_num, entryid);
            ResultSet parentSet = parentExistsStmt.executeQuery();
            boolean parentExists = parentSet.first();
            
            if (!destSet.first() && parentExists)
            {
                debug("Inserting comment id="+id);
                destInsert.clearParameters();
                destInsert.setString(id_num, id);
                destInsert.setString(entryid_num, srcSet.getString(entryid_num));
                destInsert.setString(name_num, srcSet.getString(name_num));
                destInsert.setString(email_num, srcSet.getString(email_num));
                destInsert.setString(url_num, srcSet.getString(url_num));
                destInsert.setString(content_num, srcSet.getString(content_num));
                destInsert.setTimestamp(posttime_num, srcSet.getTimestamp(posttime_num));
                destInsert.executeUpdate();
            }
            else if (parentExists)
            {
                debug("Updating comment id="+id);
                destUpdate.clearParameters();
                destUpdate.setString(id_num, id);
                destUpdate.setString(entryid_num, srcSet.getString(entryid_num));
                destUpdate.setString(name_num, srcSet.getString(name_num));
                destUpdate.setString(email_num, srcSet.getString(email_num));
                destUpdate.setString(url_num, srcSet.getString(url_num));
                destUpdate.setString(content_num, srcSet.getString(content_num));
                destUpdate.setTimestamp(posttime_num, srcSet.getTimestamp(posttime_num));
                destUpdate.setString(8, srcSet.getString(id_num));
                destUpdate.executeUpdate();
            }                
            else 
            {
                info("Not copying comment id="+id);
            }
        }     
        purgeDeleted(srccon,destcon,"comment");
    }
    private void syncRefererTable(Connection srccon, Connection destcon) throws Exception
    {
        info("--- syncRefererTable ---");
        
        String columns = "id,websiteid,entryid,datestr,refurl,refpermalink,"
            +"reftime,requrl,title,excerpt,dayhits,totalhits,visible,duplicate";
        int id_num = 1;
        int websiteid_num = 2;
        int entryid_num = 3;
        int datestr_num = 4;
        int refurl_num = 5;
        int refpermalink_num = 6;        
        int reftime_num = 7;
        int requrl_num = 8;
        int title_num = 9;
        int excerpt_num = 10;
        int dayhits_num = 11;
        int totalhits_num = 12;
        int visible_num = 13;
        int duplicate_num = 14;
        
        PreparedStatement destExistsStmt = destcon.prepareStatement(
            "select id from referer where id=?");
        PreparedStatement destInsert = destcon.prepareStatement(
            "insert into referer "+"("+columns+") "
            +"values (?,?,?,?,?, ?,?,?,?,?, ?,?,?,?)");
        PreparedStatement destUpdate = destcon.prepareStatement(
            "update referer set id=?,websiteid=?,entryid=?,datestr=?,refurl=?,"
            +"refpermalink=?,reftime=?,requrl=?,title=?,excerpt=?,dayhits=?,"
            +"totalhits=?,visible=?,duplicate=?"
            +" where id=?");
        
        PreparedStatement parentExistsStmt = destcon.prepareStatement(
            "select id from website where id=?");        
        
        Statement srcStmt = srccon.createStatement();
        // only sync those with excerpts
        ResultSet srcSet = srcStmt.executeQuery(
                "select "+columns+" from referer where excerpt is not null");
        while (srcSet.next()) 
        {
            String id = srcSet.getString(id_num);
            String entryid = srcSet.getString(entryid_num);
            String websiteid = srcSet.getString(websiteid_num);
            
            destExistsStmt.clearParameters();
            destExistsStmt.setString(id_num, id);
            ResultSet destSet = destExistsStmt.executeQuery();
                        
            parentExistsStmt.clearParameters();
            parentExistsStmt.setString(id_num, websiteid);
            ResultSet parentSet = parentExistsStmt.executeQuery();
            boolean parentExists = parentSet.first() || websiteid == null;

            if (!destSet.first() && parentExists)
            {
                debug("Inserting referer id="+id);
                destInsert.clearParameters();
                
                destInsert.setString(id_num, id);
                destInsert.setString(websiteid_num, srcSet.getString(websiteid_num));
                destInsert.setString(entryid_num, srcSet.getString(entryid_num));
                destInsert.setString(datestr_num, srcSet.getString(datestr_num));
                destInsert.setString(refurl_num, srcSet.getString(refurl_num));
                
                destInsert.setString(refpermalink_num, srcSet.getString(refpermalink_num));
                destInsert.setString(reftime_num, srcSet.getString(reftime_num));
                destInsert.setString(requrl_num, srcSet.getString(requrl_num));
                destInsert.setString(title_num, srcSet.getString(title_num));
                destInsert.setString(excerpt_num, srcSet.getString(excerpt_num));
                
                destInsert.setString(dayhits_num, srcSet.getString(dayhits_num));
                destInsert.setString(totalhits_num, srcSet.getString(totalhits_num));
                destInsert.setString(visible_num, srcSet.getString(visible_num));
                destInsert.setString(duplicate_num, srcSet.getString(duplicate_num));
                
                destInsert.executeUpdate();
            }
            else if (parentExists)
            {
                debug("Updating referer id="+id);
                destUpdate.clearParameters();
                
                destUpdate.setString(id_num, id);
                destUpdate.setString(websiteid_num, srcSet.getString(websiteid_num));
                destUpdate.setString(entryid_num, srcSet.getString(entryid_num));
                destUpdate.setString(datestr_num, srcSet.getString(datestr_num));
                destUpdate.setString(refurl_num, srcSet.getString(refurl_num));
                
                destUpdate.setString(refpermalink_num, srcSet.getString(refpermalink_num));
                destUpdate.setString(reftime_num, srcSet.getString(reftime_num));
                destUpdate.setString(requrl_num, srcSet.getString(requrl_num));
                destUpdate.setString(title_num, srcSet.getString(title_num));
                destUpdate.setString(excerpt_num, srcSet.getString(excerpt_num));
                
                destUpdate.setString(dayhits_num, srcSet.getString(dayhits_num));
                destUpdate.setString(totalhits_num, srcSet.getString(totalhits_num));
                destUpdate.setString(visible_num, srcSet.getString(visible_num));
                destUpdate.setString(duplicate_num, srcSet.getString(duplicate_num));
                
                destUpdate.setString(15, srcSet.getString(id_num));
                destUpdate.executeUpdate();
            }                
        }     
        purgeDeleted(srccon,destcon,"referer");
    }
    /** for now... just for testing */
    public static void main(String[] args) throws Exception 
    {
        if (args.length > 0) 
        {
            if ("-debug".equals(args[0])) 
            {
                debug = true;
            }
        }        
        Properties props = new Properties();
        props.load(new FileInputStream("rollerdb.properties"));
        Connection destcon = ConsistencyCheck.createConnection(props,"");
        Connection srccon = ConsistencyCheck.createConnection(props,"src.");
        
        new SyncUpgrade098Xto1000().syncUpgrade(srccon, destcon);
    }
}
