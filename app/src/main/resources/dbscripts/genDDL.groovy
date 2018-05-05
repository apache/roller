package dbscripts

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

    try {
        Template template = Velocity.getTemplate("app/src/main/resources/dbscripts/createdb.vm")
        PrintWriter pw = new PrintWriter("app/src/main/resources/dbscripts/$dbName-createdb.sql")
        template.merge(context, pw)
        pw.flush()
    } catch(Exception e) {
        System.out.println("Exception generating DDL: " + e.getMessage())
    }
}

// source for foreignKeyIndex val: https://db.apache.org/derby/docs/10.4/ref/rrefsqlj13590.html
genScript("derby", "true", "clob(102400)",
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

// source for foreignKeyIndex val: http://hsqldb.org/doc/guide/ch02.html#N10318
genScript("hsql", "true", "longvarchar",
        "bit default 0", "bit default 1",
        "timestamp(3)", "timestamp(3) default 'now'")
