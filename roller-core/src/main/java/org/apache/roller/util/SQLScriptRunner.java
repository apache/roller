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

package org.apache.roller.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;


/**
 * SQL script runner, parses script and allows you to run it. 
 * You can run the script multiple times if necessary.
 * Assumes that anything on an input line after "--" or ";" can be ignored.
 */
public class SQLScriptRunner {
    
    private List<String> commands = new ArrayList<String>();
    private List<String> messages = new ArrayList<String>();
    private boolean      failed = false;
    private boolean      errors = false;
        
    
    /** Creates a new instance of SQLScriptRunner */
    public SQLScriptRunner(InputStream is) throws IOException {
        
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String command = ""; 
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            
            if (!line.startsWith("--")) { // ignore lines starting with "--"    
                
                if (line.indexOf("--") > 0) {
                    // trim comment off end of line
                    line = line.substring(0, line.indexOf("--")).trim();
                }
                
                // add line to current command
                command += line.trim();
                if (command.endsWith(";")) { 
                    // ";" is end of command, so add completed command to list
                    String cmd = command.substring(0, command.length() - 1);
                    String[] cmdArray = StringUtils.split(cmd);
                    cmd = StringUtils.join(cmdArray, " ");
                    commands.add(cmd);
                    command = "";
                } else if (StringUtils.isNotEmpty(command)) {
                    command += " "; // still more command coming so add space
                }
            } 
        }
        in.close();    
    }
    
    
    /** Creates a new instance of SQLScriptRunner */
    public SQLScriptRunner(String scriptPath) throws IOException {
        this(new FileInputStream(scriptPath));
    }
    
    
    /** Number of SQL commands in script */
    public int getCommandCount() {
        return commands.size();
    }
    
    
    /** Return messages from last run of script, empty if no previous run */
    public List<String> getMessages() {
        return messages;
    }
    
    
    /** Returns true if last call to runScript() threw an exception */
    public boolean getFailed() {
        return failed;
    }
    
    
    /** Returns true if last run had any errors */
    public boolean getErrors() {
        return errors;
    }
    
    
    /** Run script, logs messages, and optionally throws exception on error */
    public void runScript(
            Connection con, boolean stopOnError) throws SQLException {
        failed = false;
        errors = false;
        for (String command : commands) {
            
            // run each command
            try {
                Statement stmt = con.createStatement();
                stmt.executeUpdate(command);
                if (!con.getAutoCommit()) con.commit();
                
                // on success, echo command to messages
                successMessage(command);
                
            } catch (SQLException ex) {
                // add error message with text of SQL command to messages
                errorMessage("ERROR: SQLException executing SQL [" + command 
                        + "] : " + ex.getLocalizedMessage());
                // add stack trace to messages
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                errorMessage(sw.toString());
                if (stopOnError) {
                    failed = true;
                    throw ex;
                }
            }
        }
    }
    
    
    private void errorMessage(String msg) {
        messages.add(msg);
    }    
    
    
    private void successMessage(String msg) {
        messages.add(msg);
    }
    
}
