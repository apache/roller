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
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.roller.util.Utilities;

/**
 * Roller password utility: don't run this unless you know what you are doing!</br >
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
 * java -cp ./WEB-INF/lib/rollerbeans.jar;./jdbc.jar org.apache.roller.business.utils.PasswordUtility<br />
 * 
 * <br />Options:<br />
 * 
 * -save &lt;file-name&gt;: Save username/passwords in property file<br />
 * -encrypt               : turn on encryption and encrypt passwords<br />
 * -restore &lt;file-name>   : turn off encryption and restore passwords from file<br />
 * -reset &lt;username&gt; &lt;password&gt;: reset users password<br />
 * -grant_admin &lt;username&gt;<br />
 * -revoke_admin &lt;username&gt;</p>
 */
public class PasswordUtility 
{
    public static void main(String[] args) throws Exception
    {
        Properties props = new Properties();
        props.load(new FileInputStream("rollerdb.properties"));
        
        String algorithm = props.getProperty("algorithm");
        
        Connection con = ConsistencyCheck.createConnection(props,"");
        
        if (args.length == 2 && args[0].equals("-save")) 
        {
            savePasswords(con, args[1]);
        }
        else if (args.length == 1 && args[0].equals("-encrypt")) 
        {
            encryptionOn(con, algorithm);
        }
        else if (args.length == 2 && args[0].equals("-restore")) 
        {
            encryptionOff(con, args[1]);
        }
        else if (args.length == 3 && args[0].equals("-reset")) 
        {
            resetPassword(con, args[1], args[2], algorithm);
        }
        else if (args.length == 2 && args[0].equals("-grant_admin")) 
        {
            grantAdmin(con, args[1]);
        }
        else if (args.length == 2 && args[0].equals("-revoke_admin")) 
        {
            revokeAdmin(con, args[1]);
        }
        else 
        {
            System.out.println("");
            System.out.println("USAGE: save passwords to a properties file");
            System.out.println("   rollerpw -save <file-name>");
            System.out.println("");
            System.out.println("USAGE: turn ON password encryption and encrypt existing passwords");
            System.out.println("   rollerpw -encrypt");
            System.out.println("");
            System.out.println("USAGE: turn OFF password encryption and restore saved passwords");
            System.out.println("   rollerpw -restore <file-name>");
            System.out.println("");
            System.out.println("USAGE: reset a user password");
            System.out.println("   rollerpw -password <username> <new-password>");
            System.out.println("");
            System.out.println("USAGE: grant admin rights to user");
            System.out.println("   rollerpw -grant_admin <username>");
            System.out.println("");
            System.out.println("USAGE: revoke admin right from user");
            System.out.println("   rollerpw -revoke_admin <username>");
            System.out.println("");
        }
    }
    
    /** 
     * Saves usernames and passwords to properties file, passwords keyed by usernames 
     */
    private static void savePasswords(
                    Connection con, String fileName) throws Exception
    {
        Properties newprops = new Properties();
        PreparedStatement userquery = con.prepareStatement(
           "select username,passphrase from rolleruser");
        ResultSet users = userquery.executeQuery();
        while (users.next()) 
        {
            String username = users.getString(1);
            String passphrase = users.getString(2);
            newprops.put(username, passphrase);
        }
        FileOutputStream fos = new FileOutputStream(fileName);
        newprops.save(fos, "Generated by Roller Password Utility");
        fos.close();
    }

    /** 
     * Encrypt all passwords in rolleruser and turn ON encryption flag in rollerconfig
     */
    private static void encryptionOn(
                    Connection con, String algorithm) throws Exception
    {
        PreparedStatement userQuery = con
        	.prepareStatement("select username,passphrase from rolleruser");
        PreparedStatement userUpdate = con
        	.prepareStatement("update rolleruser set passphrase=? where username=?");
        PreparedStatement configUpdate = con
			.prepareStatement("update rollerconfig set encryptpasswords=?");

        Properties props = new Properties();
        ResultSet users = userQuery.executeQuery();
        while (users.next())
        {
            String username = users.getString(1);
            String passphrase = users.getString(2);
            props.put(username, passphrase);
        }
        Enumeration usernames = props.keys();
        while (usernames.hasMoreElements())
        {
            String username = (String)usernames.nextElement();
            String passphrase = (String)props.get(username);
            userUpdate.clearParameters();
            userUpdate.setString(1, Utilities.encodePassword(passphrase, algorithm));
            userUpdate.setString(2, username);
            userUpdate.executeUpdate();
            System.out.println("Encrypted password for user: " + username);
        }
        
        configUpdate.setBoolean(1, true);
        configUpdate.executeUpdate();
    }

    /** 
     * Restore passwords in rolleruser and turn OFF encryption flag in rollerconfig
     */
    private static void encryptionOff(
                    Connection con, String fileName) throws Exception
    {
        PreparedStatement userUpdate = con
			.prepareStatement("update rolleruser set passphrase=? where username=?");
        PreparedStatement configUpdate = con
			.prepareStatement("update rollerconfig set encryptpasswords=?");

        Properties props = new Properties();
        props.load(new FileInputStream(fileName));
        Enumeration usernames = props.keys();
        while (usernames.hasMoreElements())
        {
            String username = (String)usernames.nextElement();
            String password = (String)props.get(username);
            userUpdate.clearParameters();
            userUpdate.setString(1, password);
            userUpdate.setString(2, username);
            userUpdate.executeUpdate();
        }
        
        configUpdate.setBoolean(1, false);
        configUpdate.executeUpdate();
    }

    /** 
     * Reset user's password to specified value using specified algorythm (if needed) 
     */
    private static void resetPassword(
                    Connection con, String username, String password, String algorithm) 
    	    throws Exception
    {
		PreparedStatement encryptionQuery = 
            con.prepareStatement("select encryptpasswords from rollerconfig");
		PreparedStatement userUpdate = 
            con.prepareStatement("update rolleruser set passphrase=? where username=?");
		
		ResultSet rs = encryptionQuery.executeQuery();
		rs.next();
		boolean encryption = rs.getBoolean(1);
		
		String newpassword = 
		    encryption ? Utilities.encodePassword(password, algorithm) : password;
		userUpdate.setString(1, newpassword);
		userUpdate.setString(2, username);
		userUpdate.executeUpdate();
    }   
    
    /**
     * Grant admin role to user by adding admin role for user to userrole table
     */
    private static void grantAdmin(Connection con, String userName) throws Exception
    {
        // Find userid of specified user
        String userid = null;
        PreparedStatement userQuery = con.prepareStatement(
           "select id from rolleruser where username=?");    
        userQuery.setString(1, userName);
        ResultSet userRS = userQuery.executeQuery();
        if (!userRS.next()) 
        {
            System.err.println("ERROR: username not found in database");
            return;
        }
        else 
        {
            userid = userRS.getString(1);
        }
        
        // Is user already an admin?
        PreparedStatement roleQuery = con.prepareStatement(
           "select username from userrole where username=? and rolename='admin'");
        roleQuery.setString(1, userName);
        ResultSet roleRS = roleQuery.executeQuery();
        if (!roleRS.next()) // then no, user is not admin
        {
            // Add admin role for user
            PreparedStatement adminInsert = con.prepareStatement(
               "insert into userrole (id,rolename,username,userid) values (?,?,?,?)");
            adminInsert.setString(1, userName);
            adminInsert.setString(2, "admin");
            adminInsert.setString(3, userName);
            adminInsert.setString(4, userid);
            adminInsert.executeUpdate();
            System.out.println("User granted admin role");
        }
        else 
        {
            System.out.println("User was already an admin");
        }
    }

    /**
     * Revoke admin role from user by removing admin role from userrole table
     */
    private static void revokeAdmin(Connection con, String userName) throws Exception
    {
        // Find userid of specified user
        String userid = null;
        PreparedStatement userQuery = con.prepareStatement(
           "select id from rolleruser where username=?");    
        userQuery.setString(1, userName);
        ResultSet userRS = userQuery.executeQuery();
        if (!userRS.next()) 
        {
            System.err.println("ERROR: username not found in database");
            return;
        }
        else 
        {
            userid = userRS.getString(1);
        }
        
        // Delete user's admin entries from userrole table
        PreparedStatement roleDelete = con.prepareStatement(
           "delete from userrole where userid=? and rolename='admin'");
        roleDelete.setString(1, userid);
        roleDelete.executeUpdate();
    }
}
