/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 Groovy script runs the createdb.vm Velocity template to generate database-specific table
 creation scripts for TightBlog, placing the generated files in its resources/dbscripts folder.

 See this subproject's build.gradle for run instructions.
 */
import org.apache.velocity.VelocityContext
import org.apache.velocity.Template
import org.apache.velocity.app.Velocity

def genScript = { String dbName, String foreignKeyIndex, String textType,
  String booleanFalseType, String booleanTrueType, String timestampType,
  String timestampNow ->

    Velocity.init()
    VelocityContext context = new VelocityContext()
    context.put("FOREIGN_KEY_MAKES_INDEX", foreignKeyIndex)
    context.put("TEXT_SQL_TYPE", textType)
    context.put("BOOLEAN_SQL_TYPE_FALSE", booleanFalseType)
    context.put("BOOLEAN_SQL_TYPE_TRUE", booleanTrueType)
    context.put("TIMESTAMP_SQL_TYPE", timestampType)
    context.put("TIMESTAMP_SQL_TYPE_DEFNOW", timestampNow)
    context.put("DB_NAME", dbName)

    try {
        Template template = Velocity.getTemplate("src/main/resources/createdb.vm")
        PrintWriter pw = new PrintWriter("../../src/main/resources/dbscripts/$dbName-createdb.sql")
        template.merge(context, pw)
        pw.flush()
    } catch(Exception e) {
        System.out.println("Exception generating DDL: " + e.getMessage())
    }
}

// source for foreignKeyIndex val: https://db.apache.org/derby/docs/10.4/ref/rrefsqlj13590.html
genScript("apachederby", "true", "clob(102400)",
        "smallint default 0", "smallint default 1",
        "timestamp", "timestamp default current_timestamp")

// source for foreignKeyIndex val: http://dev.mysql.com/doc/refman/5.7/en/create-table-foreign-keys.html
genScript("mysql", "true", "text",
        "tinyint(1) default 0", "tinyint(1) default 1",
        "datetime(3)", "datetime(3) default CURRENT_TIMESTAMP(3)")

// source for foreignKeyIndex val: http://www.postgresql.org/docs/9.1/static/ddl-constraints.html
genScript("postgresql", "false", "text",
        "boolean default false", "boolean default true",
        "timestamp(3) with time zone", "timestamp(3) default now()")

