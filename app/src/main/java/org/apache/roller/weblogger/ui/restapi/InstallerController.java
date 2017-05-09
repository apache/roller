/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.restapi;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.roller.weblogger.business.WebloggerContext;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.SQLScriptRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Walk user through install process.
 */
@Controller
@RequestMapping(path = "/tb-ui/install")
public class InstallerController {

    private static Logger log = LoggerFactory.getLogger(InstallerController.class);
    private I18nMessages defaultMessages = I18nMessages.getMessages(Locale.getDefault());

    @Autowired
    private DataSource tbDataSource;

    public void setTbDataSource(DataSource tbDataSource) {
        this.tbDataSource = tbDataSource;
    }

    public enum StartupStatus {
        databaseError(true, "installer.databaseConnectionError"),
        tablesMissing(false, "installer.noDatabaseTablesFound"),
        databaseVersionError(true, "installer.databaseVersionError"),
        databaseCreateError(true, "installer.databaseCreateError"),
        needsBootstrapping(false, "installer.tablesCreated"),
        bootstrapError(true, "installer.bootstrappingError");

        boolean error;

        String descriptionKey;

        public boolean isError() {
            return error;
        }

        public String getDescriptionKey() {
            return descriptionKey;
        }

        StartupStatus(boolean error, String descriptionKey) {
            this.error = error;
            this.descriptionKey = descriptionKey;
        }
    }

    @RequestMapping(value = "/install")
    public ModelAndView install(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (WebloggerContext.isBootstrapped()) {
            response.sendRedirect(request.getContextPath()+"/");
            return null;
        }

        Map<String, Object> map = initializeMap();
        List<String> messages = new ArrayList<>();
        map.put("messages", messages);

        // is database accessible?
        try {
            Connection testcon = tbDataSource.getConnection();
            // used if DB creation needed
            map.put("databaseProductName", testcon.getMetaData().getDatabaseProductName());
            testcon.close();
        } catch (Exception e) {
            log.error(defaultMessages.getString("installer.databaseConnectionError"));
            map.put("status", StartupStatus.databaseError);
            map.put("rootCauseException", e.getCause());
            map.put("rootCauseStackTrace", getRootCauseStackTrace(e.getCause()));
            messages.add(e.getMessage());
            return new ModelAndView(".install", map);
        }

        StartupStatus status = checkDatabase(map);
        map.put("status", status);

        // is database schema present?
        if (StartupStatus.tablesMissing.equals(status)) {
            log.info("TightBlog database needs creating, forwarding to creation page");
            return new ModelAndView(".install", map);
        } else if (StartupStatus.databaseVersionError.equals(status) || StartupStatus.bootstrapError.equals(status)) {
            return new ModelAndView(".install", map);
        }

        // all good, TightBlog ready to bootstrap
        return bootstrap(request, response);
    }

    @RequestMapping(value = "/create")
    public ModelAndView createDatabaseTables(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (WebloggerContext.isBootstrapped()) {
            response.sendRedirect(request.getContextPath()+"/");
            return null;
        }
        Map<String, Object> map = initializeMap();
        List<String> messages = new ArrayList<>(100);
        map.put("messages", messages);

        String script = "/createdb.sql";
        Connection con = null;
        SQLScriptRunner runner = null;

        try {
            con = tbDataSource.getConnection();
            String scriptPath = con.getMetaData().getDatabaseProductName().toLowerCase() + script;
            messages.add("Running database script: " + scriptPath);
            runner = new SQLScriptRunner(scriptPath, true);
            runner.runScript(con, true);
            messages.addAll(runner.getMessages());
            map.put("status", StartupStatus.needsBootstrapping);
        } catch (Exception ex) {
            messages.add("ERROR processing database script " + script);
            if (runner != null) {
                messages.addAll(runner.getMessages());
            }
            map.put("status", StartupStatus.databaseCreateError);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }

        return new ModelAndView(".install", map);
    }

    @RequestMapping(value = "/bootstrap")
    public ModelAndView bootstrap(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (WebloggerContext.isBootstrapped()) {
            response.sendRedirect(request.getContextPath()+"/");
            return null;
        }

        Map<String, Object> map = initializeMap();

        try {
            // trigger bootstrapping process
            ServletContext sc = WebloggerContext.getServletContext();
            ApplicationContext ac = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);

            WebloggerContext.bootstrap(ac);
            log.info("EXITING - Bootstrap successful, forwarding to weblogger");
            response.sendRedirect(request.getContextPath());
            return null;
        } catch (Exception e) {
            log.error("Exception", e);
            map.put("rootCauseException", e);
            map.put("rootCauseStackTrace", getRootCauseStackTrace(e));
        }

        map.put("status", StartupStatus.bootstrapError);
        return new ModelAndView(".install", map);
    }

    private Map<String, Object> initializeMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("pageTitle", defaultMessages.getString("install.pageTitle"));
        return map;
    }

    /**
     * Determine if database schema needs to be created.
     */
    private StartupStatus checkDatabase(Map<String, Object> map) {
        Connection con = null;
        try {
            con = tbDataSource.getConnection();

            // does the schema already exist?  -- check a couple of tables to find out
            if (!tableExists(con, "weblog") || !tableExists(con, "weblogger_user")) {
                return StartupStatus.tablesMissing;
            }

            // OK, exists -- does the database schema match that used by the application?
            int applicationVersion = WebloggerStaticConfig.getIntProperty("tightblog.database.expected.version");
            int dbversion = -1;
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "select database_version from weblogger_properties where id = '1'");

            if (rs.next()) {
                dbversion = Integer.parseInt(rs.getString(1));
            }

            if (dbversion != applicationVersion) {
                log.warn("TightBlog DB version {} incompatible with application version {}", dbversion,
                        applicationVersion);
                return StartupStatus.databaseVersionError;
            }
        } catch (Exception e) {
            log.error("Error checking for tables", e);
            map.put("rootCauseException", e);
            map.put("rootCauseStackTrace", getRootCauseStackTrace(e));
            return StartupStatus.bootstrapError;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private String getRootCauseStackTrace(Throwable rootCauseException) {
        String stackTrace = "";
        if (rootCauseException != null) {
            StringWriter sw = new StringWriter();
            rootCauseException.printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString().trim();
        }
        return stackTrace;
    }

    /**
     * Return true if named table exists in database.
     */
    private boolean tableExists(Connection con, String tableName) throws SQLException {
        ResultSet rs = con.getMetaData().getTables(null, null, "%", null);
        while (rs.next()) {
            if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME").toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
