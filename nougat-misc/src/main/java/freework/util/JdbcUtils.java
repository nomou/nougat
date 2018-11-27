/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import freework.codec.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 */
public abstract class JdbcUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcUtils.class);

    public static final String HSQL = "hsql";
    public static final String H2 = "h2";
    public static final String MYSQL = "mysql";
    public static final String ORACLE = "oracle";
    public static final String POSTGRESQL = "postgresql";
    public static final String DB2 = "db2";
    public static final String DERBY = "derby";
    public static final String SQL_SERVER = "sqlserver";
    public static final String JTDS = "jtds";
    public static final String SQLITE = "sqlite";

    private static final String HSQL_DRIVER = "org.hsqldb.jdbcDriver";
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    // private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver"; // 9i 后建议使用
    private static final String DB2_DRIVER = "com.ibm.db2.jdbc.app.DB2Driver";
    private static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String JTDS_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
    private static final String SQLITE_DRIVER = "org.sqlite.JDBC";

    /* ****************************
     *       MetaData
     * ****************************/

    /**
     * 从 Connection DatabaseMetaData 中符合条件的 Schema
     */
    public static List<DbSchema> readSchemas(Connection conn) throws SQLException {
        return readSchemas(conn.getMetaData());
    }

    /**
     * 从 DatabaseMetaData 中符合条件的 Schema
     */
    public static List<DbSchema> readSchemas(DatabaseMetaData meta) throws SQLException {
        List<DbSchema> schemas = new ArrayList<DbSchema>();
        String connSchema = getConnectionSchemaName(meta);

        ResultSet rs = null;
        try {
            rs = meta.getSchemas();
            while (rs.next()) {
                String name = rs.getString("TABLE_SCHEM");
                schemas.add(new DbSchema(name, name.equals(connSchema)));
            }
        } finally {
            close(rs);
        }

        return schemas;
    }

    /**
     * 从 Connection DatabaseMetaData 中符合条件的Table
     */
    public static List<DbTable> readTables(Connection conn, String schemaPattern, String tablePattern, String[] tabTypes) throws SQLException {
        return readTables(conn.getMetaData(), schemaPattern, tablePattern, tabTypes);
    }

    /**
     * 从 DatabaseMetaData 中符合条件的Table
     */
    public static List<DbTable> readTables(DatabaseMetaData meta, String schemaPattern, String tablePattern, String[] tabTypes) throws SQLException {
        List<DbTable> tables = new ArrayList<DbTable>();

        ResultSet rs = null;
        try {
            rs = meta.getTables(null, schemaPattern, tablePattern, tabTypes);
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEM");
                String table = rs.getString("TABLE_NAME");
                String type = rs.getString("TABLE_TYPE");
                String remarks = rs.getString("REMARKS");
                tables.add(new DbTable(schema, table, type, remarks));
            }
        } finally {
            close(rs);
        }
        return tables;
    }

    /**
     * 从 Connection DatabaseMetaData 中符合条件的 Column
     */
    public static List<DbColumn> readColumns(Connection conn, String schemaPattern, String tablePattern, String columnPattern) throws SQLException {
        return readColumns(conn.getMetaData(), schemaPattern, tablePattern, columnPattern);
    }

    /**
     * 从 DatabaseMetaData 中符合条件的 Column
     */
    public static List<DbColumn> readColumns(DatabaseMetaData dbMeta, String schemaPattern, String tablePattern, String columnPattern) throws SQLException {
        List<DbColumn> columns = new ArrayList<DbColumn>();

        ResultSet rs = null;
        try {
            rs = dbMeta.getColumns(null, schemaPattern, tablePattern, columnPattern);
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEM");
                String table = rs.getString("TABLE_NAME");
                int position = rs.getInt("ORDINAL_POSITION");
                String column = rs.getString("COLUMN_NAME");
                String type = rs.getString("TYPE_NAME");    /* vendor type */
                int sqlType = rs.getInt("DATA_TYPE");   /* jdbc type */
                // typeName = new StringTokenizer( rs.getString("TYPE_NAME"), "() " ).nextToken();

                int length = rs.getInt("COLUMN_SIZE");
                int scale = 0;
                if (length > 0) {
                    scale = rs.getInt("DECIMAL_DIGITS");
                }

                boolean nullable = DatabaseMetaData.columnNullable == rs.getInt("NULLABLE");
                Object colDefault = "NULL";
                try {
                /*-
                 * oracle APEX_030200.WWV_COLUMN_EXCEPTIONS.OBSOLETE_DATE
                  * 读取会抛出异常 Stream is Closed
                  * 测试下 Oracle SQL Developer 读取也有问题(null 显示为"(null)", 但是这个字段为 "null", 也无法修改)
                 */
                    colDefault = rs.getObject("COLUMN_DEF");
                } catch (SQLException ignore) {
                    // ignore
                    // LOG.warn
                }
                // oracle 需要在连接时设置 remarks 反馈, 通过以下两种方式
                // 1. props.put("remarksReporting", "true"), DriverMananger.getConnection(url, props)
                // 2. ((OracleConnection) conn).setRemarksReporting(true)
                String remarks = rs.getString("REMARKS");

                columns.add(new DbColumn(schema, table, position, column, type, sqlType, length, scale, nullable, colDefault, remarks));
            }
        } finally {
            close(rs);
        }
        return columns;
    }

    /**
     * 获取当前 Connection 的 Schema
     */
    private static String getConnectionSchemaName(DatabaseMetaData meta) throws SQLException {
        /*
        since 1.7
        return connection.getSchema();
        */
        // TODO Others DB
        String url = meta.getURL();
        return meta.getUserName();
    }

    /* *****************
     *   MetaData POJO
     * *****************/

    public static class DbSchema {
        public final String name;
        public final boolean isDefault;

        protected DbSchema(String name, boolean isDefault) {
            this.name = name;
            this.isDefault = isDefault;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * 数据表
     */
    public static class DbTable {
        public final String schema;
        public final String name;
        public final String type;       // 表类型, "VIEW", "TABLE" 等
        public final String remarks;    // 表备注

        protected DbTable(String schema, String name, String type, String remarks) {
            this.schema = schema;
            this.name = name;
            this.type = type;
            this.remarks = remarks;
        }

        @Override
        public String toString() {
            return schema + "." + name;
        }
    }

    /**
     * 数据列
     */
    public static class DbColumn {
        public final String schema;
        public final String table;
        public final int position;  // 列位置
        public final String name;   // 列名称
        public final String type;   // 供应商数据类型名称
        public final int sqlType;   // JDBC Type
        public final int length;    // 长度或数值精度 length or precision */
        public final int scale;     // 小数位数
        public final boolean nullable;      // 是否可为空
        public final Object defaultValue;
        public final String remarks;        // 列备注

        protected DbColumn(String schema, String table, int position, String name, String type, int sqlType, int length, int scale, boolean nullable, Object defaultValue, String remarks) {
            this.schema = schema;
            this.table = table;
            this.position = position;
            this.name = name;
            this.type = type;
            this.sqlType = sqlType;
            this.length = length;
            this.scale = scale;
            this.nullable = nullable;
            this.defaultValue = defaultValue;
            this.remarks = remarks;
        }

        @Override
        public String toString() {
            return name + " " + type + (0 < length ? "(" + length + (0 < scale ? "," + scale : "") + ")" : "");
        }
    }

    /* ******************************
     *           Query
     * ******************************/

    /**
     * SQL Query
     */
    public static List<Map<String, Object>> executeQuery(DataSource dataSource, String sql, Object... parameters) throws SQLException {
        return executeQuery(dataSource, sql, Arrays.asList(parameters));
    }

    /**
     * SQL Query
     */
    public static List<Map<String, Object>> executeQuery(DataSource dataSource, String sql, List<Object> parameters) throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return executeQuery(conn, sql, parameters);
        } finally {
            close(conn);
        }
    }

    /**
     * SQL Query
     */
    public static List<Map<String, Object>> executeQuery(Connection conn, String sql, Object... parameters) throws SQLException {
        return executeQuery(conn, sql, Arrays.asList(parameters));
    }

    /**
     * SQL Query
     */
    public static List<Map<String, Object>> executeQuery(Connection conn, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);
            setParameters(stmt, parameters);
             rs = stmt.executeQuery();
            return resultSetToListMap(rs, null);
        } finally {
            close(rs);
            close(stmt);
        }
    }

    /**
     * SQL Query
     */
    /*
    public static ResultSet executeQueryOnly(Connection conn, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            setParameters(stmt, parameters);
            return stmt.executeQuery();
        } finally {
            close(stmt);
        }
    }

*/
    /* *******************************
     *         Update
     * *******************************/

    /**
     * 执行 Sql Update 操作, 返回受影响的记录数
     */
    public static int executeUpdate(DataSource dataSource, String sql, Object... parameters) throws SQLException {
        return executeUpdate(dataSource, sql, Arrays.asList(parameters));
    }

    /**
     * 执行 Sql Update 操作, 返回受影响的记录数
     *
     * @param dataSource
     * @param sql
     * @param parameters
     * @return
     * @throws java.sql.SQLException
     */
    public static int executeUpdate(DataSource dataSource, String sql, List<Object> parameters) throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            int updateCount = executeUpdate(conn, sql, parameters);
            if(!conn.getAutoCommit()) {
                conn.commit();
            }
            return updateCount;
        } finally {
            close(conn);
        }
    }

    /**
     * 执行 update 操作, 返回受影响记录数
     *
     * @param conn
     * @param sql
     * @param parameters
     * @return
     * @throws java.sql.SQLException
     */
    public static int executeUpdate(Connection conn, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement stmt = null;

        int updateCount;
        try {
            stmt = conn.prepareStatement(sql);

            setParameters(stmt, parameters);

            updateCount = stmt.executeUpdate();
        } finally {
            close(stmt);
        }

        return updateCount;
    }

    /* *************************
     *     SQL Execute
     * *************************/

    /**
     * 执行 SQL 语句
     *
     * @param dataSource
     * @param sql
     * @param parameters
     * @throws java.sql.SQLException
     */
    public static void execute(DataSource dataSource, String sql, Object... parameters) throws SQLException {
        execute(dataSource, sql, Arrays.asList(parameters));
    }

    /**
     * 执行 SQL 语句
     *
     * @param dataSource
     * @param sql
     * @param parameters
     * @throws java.sql.SQLException
     */
    public static void execute(DataSource dataSource, String sql, List<Object> parameters) throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            execute(conn, sql, parameters);
        } finally {
            close(conn);
        }
    }

    /**
     * 执行 SQL 语句
     *
     * @param conn
     * @param sql
     * @param parameters
     * @throws java.sql.SQLException
     */
    public static void execute(Connection conn, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            setParameters(stmt, parameters);

            stmt.execute();
        } finally {
            JdbcUtils.close(stmt);
        }
    }

    /* ***********************
     *      Parameters
     * ***********************/

    /**
     * 将给定参数设置到 PreparedStatement statment 对象中
     */
    public static void setParameters(PreparedStatement stmt, Object... parameters) throws SQLException {
        setParameters(stmt, Arrays.asList(parameters));
    }

    /**
     * 将给定参数设置到 PreparedStatement statment 对象中
     */
    public static void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        for (int i = 0, size = parameters.size(); i < size; ++i) {
            Object param = parameters.get(i);
            stmt.setObject(i + 1, param);   // jdbc 索引从 1 开始
        }
    }

    /* *************************
     *       ResultSet
     * *************************/

    public static List<Map<String, Object>> resultSetToListMap(ResultSet rs, List<Map<String, Object>> rows) throws SQLException {
        if (rows == null) rows = new ArrayList<Map<String, Object>>();

        ResultSetMetaData rsMeta = rs.getMetaData();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();

            for (int i = 0, size = rsMeta.getColumnCount(); i < size; ++i) {
                String columName = rsMeta.getColumnLabel(i + 1);
                Object value = rs.getObject(i + 1);
                row.put(columName, value);
            }

            rows.add(row);
        }

        return rows;
    }

    public static List<Object[]> resultSetToListArray(ResultSet rs, List<Object[]> rows) throws SQLException {
        if (rows == null) rows = new ArrayList<Object[]>();

        ResultSetMetaData rsMeta = rs.getMetaData();
        int columnCount = rsMeta.getColumnCount();

        Object[] headers = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
            headers[i] = rsMeta.getColumnLabel(i + 1);
        }
        rows.add(headers);

        while (rs.next()) {
            Object[] row = new Object[columnCount];

            for (int i = 0; i < columnCount; ++i) {
                row[i] = rs.getObject(i + 1);
            }

            rows.add(row);
        }

        return rows;

    }

    /**
     * 在终端打印 ReusltSet
     *
     * @param rs
     * @throws java.sql.SQLException
     */
    public static void printResultSet(ResultSet rs) throws SQLException {
        printResultSet(rs, System.out);
    }

    public static void printResultSet(ResultSet rs, PrintStream out) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();

        int columnCount = metadata.getColumnCount();
        for (int columnIndex = 1; columnIndex <= columnCount; ++columnIndex) {
            if (columnIndex != 1) {
                out.print('\t');
            }
            out.print(metadata.getColumnName(columnIndex));
        }

        out.println();

        while (rs.next()) {

            for (int columnIndex = 1; columnIndex <= columnCount; ++columnIndex) {
                if (columnIndex != 1) {
                    out.print('\t');
                }

                int type = metadata.getColumnType(columnIndex);

                if (type == Types.VARCHAR || type == Types.CHAR || type == Types.NVARCHAR || type == Types.NCHAR) {
                    out.print(rs.getString(columnIndex));
                } else if (type == Types.DATE) {
                    Date date = rs.getDate(columnIndex);
                    if (rs.wasNull()) {
                        out.print("null");
                    } else {
                        out.print(date.toString());
                    }
                } else if (type == Types.BIT) {
                    boolean value = rs.getBoolean(columnIndex);
                    if (rs.wasNull()) {
                        out.print("null");
                    } else {
                        out.print(Boolean.toString(value));
                    }
                } else if (type == Types.BOOLEAN) {
                    boolean value = rs.getBoolean(columnIndex);
                    if (rs.wasNull()) {
                        out.print("null");
                    } else {
                        out.print(Boolean.toString(value));
                    }
                } else if (type == Types.TINYINT) {
                    byte value = rs.getByte(columnIndex);
                    if (rs.wasNull()) {
                        out.print("null");
                    } else {
                        out.print(Byte.toString(value));
                    }
                } else if (type == Types.SMALLINT) {
                    short value = rs.getShort(columnIndex);
                    if (rs.wasNull()) {
                        out.print("null");
                    } else {
                        out.print(Short.toString(value));
                    }
                } else if (type == Types.INTEGER) {
                    int value = rs.getInt(columnIndex);
                    if (rs.wasNull()) {
                        out.print("null");
                    } else {
                        out.print(Integer.toString(value));
                    }
                } else if (type == Types.BIGINT) {
                    long value = rs.getLong(columnIndex);
                    if (rs.wasNull()) {
                        out.print("null");
                    } else {
                        out.print(Long.toString(value));
                    }
                } else if (type == Types.TIMESTAMP) {
                    out.print(String.valueOf(rs.getTimestamp(columnIndex)));
                } else if (type == Types.DECIMAL) {
                    out.print(String.valueOf(rs.getBigDecimal(columnIndex)));
                } else if (type == Types.CLOB) {
                    out.print(String.valueOf(rs.getString(columnIndex)));
                } else if (type == Types.JAVA_OBJECT) {
                    Object objec = rs.getObject(columnIndex);

                    if (rs.wasNull()) {
                        out.print("null");
                    } else {
                        out.print(String.valueOf(objec));
                    }
                } else if (type == Types.LONGVARCHAR) {
                    Object objec = rs.getString(columnIndex);

                    if (rs.wasNull()) {
                        out.print("null");
                    } else {
                        out.print(String.valueOf(objec));
                    }
                } else if (type == Types.NULL) {
                    out.print("null");
                } else {
                    Object objec = rs.getObject(columnIndex);

                    if (rs.wasNull()) {
                        out.print("null");
                    } else {
                        if (objec instanceof byte[]) {
                            byte[] bytes = (byte[]) objec;
                            String text = Hex.encode(bytes);
                            out.print(text);
                        } else {
                            out.print(String.valueOf(objec));
                        }
                    }
                }
            }
            out.println();
        }
    }

    /**
     * 向表中插入数据
     *
     * @param dataSource
     * @param tableName
     * @param data
     * @throws java.sql.SQLException
     */
    public static void insertToTable(DataSource dataSource, String tableName, Map<String, Object> data) throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            insertToTable(conn, tableName, data);
        } finally {
            close(conn);
        }
    }

    public static void insertToTable(Connection conn, String tableName, Map<String, Object> data) throws SQLException {
        String sql = makeInsertToTableSql(tableName, data.keySet());
        List<Object> parameters = new ArrayList<Object>(data.values());
        execute(conn, sql, parameters);
    }

    /**
     * 根据给定的表名构建 PreparedStatement Insert Sql
     *
     * @param tableName
     * @param names
     * @return
     */
    public static String makeInsertToTableSql(String tableName, Collection<String> names) {
        StringBuilder sql = new StringBuilder() //
                .append("insert into ") //
                .append(tableName) //
                .append("("); //

        int nameCount = 0;
        for (String name : names) {
            if (nameCount > 0) {
                sql.append(",");
            }
            sql.append(name);
            nameCount++;
        }
        sql.append(") values (");
        for (int i = 0; i < nameCount; ++i) {
            if (i != 0) {
                sql.append(",");
            }
            sql.append("?");
        }
        sql.append(")");

        return sql.toString();
    }

    /**
     * 根据 jdbc url 获取驱动 driver class
     *
     * @param rawUrl
     * @return
     * @throws java.sql.SQLException
     */
    public static String getDriverClassName(String rawUrl) throws SQLException {
        if (rawUrl.startsWith("jdbc:derby:")) {
            return DERBY_DRIVER;
        } else if (rawUrl.startsWith("jdbc:mysql:")) {
            return MYSQL_DRIVER;
        } else if (rawUrl.startsWith("jdbc:oracle:") || rawUrl.startsWith("JDBC:oracle:")) {
            return ORACLE_DRIVER;
        } else if (rawUrl.startsWith("jdbc:microsoft:")) {
            return "com.microsoft.jdbc.sqlserver.SQLServerDriver";
        } else if (rawUrl.startsWith("jdbc:sqlserver:")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (rawUrl.startsWith("jdbc:jtds:")) {
            return JTDS_DRIVER;
        } else if (rawUrl.startsWith("jdbc:hsqldb:")) {
            return HSQL_DRIVER;
        } else if (rawUrl.startsWith("jdbc:db2:")) {
            return DB2_DRIVER;
        } else if (rawUrl.startsWith("jdbc:sqlite:")) {
            return SQLITE_DRIVER;
        } else if (rawUrl.startsWith("jdbc:h2:")) {
            return H2_DRIVER;
        } else {
            throw new SQLException("unkow jdbc driver : " + rawUrl);
        }

    }

    /**
     * 根据 jdbc url 获取数据库类型
     *
     * @param rawUrl
     * @return
     */
    public static String getDbType(String rawUrl) {
        if (rawUrl == null) {
            return null;
        }

        if (rawUrl.startsWith("jdbc:derby:")) {
            return DERBY;
        } else if (rawUrl.startsWith("jdbc:mysql:")) {
            return MYSQL;
        } else if (rawUrl.startsWith("jdbc:oracle:")) {
            return ORACLE;
        } else if (rawUrl.startsWith("jdbc:postgresql:") || rawUrl.startsWith("jdbc:vertica:")) {
            return POSTGRESQL;
        } else if (rawUrl.startsWith("jdbc:microsoft:")) {
            return SQL_SERVER;
        } else if (rawUrl.startsWith("jdbc:jtds:")) {
            return JTDS;
        } else if (rawUrl.startsWith("jdbc:hsqldb:")) {
            return HSQL;
        } else if (rawUrl.startsWith("jdbc:db2:")) {
            return DB2;
        } else if (rawUrl.startsWith("jdbc:sqlite:")) {
            return SQLITE;
        } else if (rawUrl.startsWith("jdbc:h2:")) {
            return H2;
        } else {
            return null;
        }
    }

    /**
     * 获取 {@link java.sql.Types} 的名称
     *
     * @param sqlType
     * @return
     */
    public static String getTypeName(int sqlType) {
        switch (sqlType) {
            case Types.ARRAY:
                return "ARRAY";

            case Types.BIGINT:
                return "BIGINT";

            case Types.BINARY:
                return "BINARY";

            case Types.BIT:
                return "BIT";

            case Types.BLOB:
                return "BLOB";

            case Types.BOOLEAN:
                return "BOOLEAN";

            case Types.CHAR:
                return "CHAR";

            case Types.CLOB:
                return "CLOB";

            case Types.DATALINK:
                return "DATALINK";

            case Types.DATE:
                return "DATE";

            case Types.DECIMAL:
                return "DECIMAL";

            case Types.DISTINCT:
                return "DISTINCT";

            case Types.DOUBLE:
                return "DOUBLE";

            case Types.FLOAT:
                return "FLOAT";

            case Types.INTEGER:
                return "INTEGER";

            case Types.JAVA_OBJECT:
                return "JAVA_OBJECT";

            case Types.LONGNVARCHAR:
                return "LONGNVARCHAR";

            case Types.LONGVARBINARY:
                return "LONGVARBINARY";

            case Types.NCHAR:
                return "NCHAR";

            case Types.NCLOB:
                return "NCLOB";

            case Types.NULL:
                return "NULL";

            case Types.NUMERIC:
                return "NUMERIC";

            case Types.NVARCHAR:
                return "NVARCHAR";

            case Types.REAL:
                return "REAL";

            case Types.REF:
                return "REF";

            case Types.ROWID:
                return "ROWID";

            case Types.SMALLINT:
                return "SMALLINT";

            case Types.SQLXML:
                return "SQLXML";

            case Types.STRUCT:
                return "STRUCT";

            case Types.TIME:
                return "TIME";

            case Types.TIMESTAMP:
                return "TIMESTAMP";

            case Types.TINYINT:
                return "TINYINT";

            case Types.VARBINARY:
                return "VARBINARY";

            case Types.VARCHAR:
                return "VARCHAR";

            default:
                return "OTHER";

        }
    }


    public static Driver createDriver(String driverClassName) throws SQLException {
        return createDriver(null, driverClassName);
    }

    public static Driver createDriver(ClassLoader classLoader, String driverClassName) throws SQLException {
        Class<?> clazz = null;
        if (classLoader != null) {
            try {
                clazz = classLoader.loadClass(driverClassName);
                Driver c;
            } catch (ClassNotFoundException ignore) {
            }
        }

        if (clazz == null) {
            try {
                ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
                if (contextLoader != null) {
                    clazz = contextLoader.loadClass(driverClassName);
                }
            } catch (ClassNotFoundException ignore) {
            }
        }

        if (clazz == null) {
            try {
                clazz = Class.forName(driverClassName);
            } catch (ClassNotFoundException e) {
                throw new SQLException(e.getMessage(), e);
            }
        }

        try {
            return (Driver) clazz.newInstance();
        } catch (IllegalAccessException e) {
            throw new SQLException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    /**
     * 关闭连接
     *
     * @param conn
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                LOG.error("close connection error", e);
            }
        }
    }

    /**
     * 关闭 Statement
     *
     * @param statement
     */
    public static void close(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (Exception e) {
                LOG.error("close statement error", e);
            }
        }
    }

    /**
     * 关闭 ResultSet
     *
     * @param rs
     */
    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                LOG.error("close resultset error", e);
            }
        }
    }

    private JdbcUtils() {
    }
}
