/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.jdbc;

import freework.jdbc.statement.DelegatingNamedParameterStatement;
import freework.jdbc.statement.NamedParameterStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.Driver;
import java.sql.DriverManager;
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
import java.util.Locale;
import java.util.Map;

/**
 */
public abstract class Jdbc {
    private static final Logger LOGGER = LoggerFactory.getLogger(Jdbc.class);

    public static final String DERBY = "derby";
    public static final String FIREBIRD_SQL = "firebirdsql";
    public static final String H2 = "h2";
    public static final String HSQL = "hsql";
    public static final String MYSQL = "mysql";
    public static final String ORACLE = "oracle";
    public static final String POSTGRE_SQL = "postgresql";
    public static final String DB2 = "db2";
    public static final String SQL_SERVER = "sqlserver";
    public static final String JTDS = "jtds";
    public static final String SQLITE = "sqlite";

    private static final String HSQL_DRIVER = "org.hsqldb.jdbcDriver";
    private static final String H2_DRIVER = "org.h2.Driver";
    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    // private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver"; // 9i 后建议使用
    private static final String POSTGRE_SQL_DRIVER = "org.postgresql.Driver";
    private static final String DB2_DRIVER = "com.ibm.db2.jdbc.app.DB2Driver";
    private static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String JTDS_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
    private static final String SQLITE_DRIVER = "org.sqlite.JDBC";

    /* ****************************
     *       MetaData
     * ****************************/

    /**
     * 从 DatabaseMetaData 中符合条件的 Schema
     */
    public static List<DbSchema> getSchemas(final DatabaseMetaData metadata) throws SQLException {
        final List<DbSchema> schemas = new ArrayList<DbSchema>();
        final String connectionSchemaName = getDefaultSchema(metadata);

        ResultSet rs = null;
        try {
            rs = metadata.getSchemas();
            while (rs.next()) {
                final String name = rs.getString("TABLE_SCHEM");
                schemas.add(new DbSchema(name, name.equals(connectionSchemaName)));
            }
        } finally {
            close(rs);
        }

        return schemas;
    }

    /**
     * 从 DatabaseMetaData 中符合条件的Table
     */
    public static List<DbTable> getTables(DatabaseMetaData meta, String schemaPattern, String tablePattern, String[] tabTypes) throws SQLException {
        List<DbTable> tables = new ArrayList<DbTable>();

        ResultSet rs = null;
        try {
            rs = meta.getTables(null, schemaPattern, tablePattern, tabTypes);
            while (rs.next()) {
                final String schema = rs.getString("TABLE_SCHEM");
                final String table = rs.getString("TABLE_NAME");
                final String type = rs.getString("TABLE_TYPE");
                final String remarks = rs.getString("REMARKS");
                tables.add(new DbTable(schema, table, type, remarks));
            }
        } finally {
            close(rs);
        }
        return tables;
    }

    /**
     * 从 DatabaseMetaData 中符合条件的 Column
     */
    public static List<DbColumn> getColumns(DatabaseMetaData dbMeta, String schemaPattern, String tablePattern, String columnPattern) throws SQLException {
        List<DbColumn> columns = new ArrayList<DbColumn>();

        ResultSet rs = null;
        try {
            rs = dbMeta.getColumns(null, schemaPattern, tablePattern, columnPattern);
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEM");
                String table = rs.getString("TABLE_NAME");
                int position = rs.getInt("ORDINAL_POSITION");
                String column = rs.getString("COLUMN_NAME");
                String type = rs.getString("TYPE_NAME");

                int size = rs.getInt("COLUMN_SIZE");
                if (size > 0) {
                    type += "(" + size;
                    int prec = rs.getInt("DECIMAL_DIGITS");
                    if (prec > 0) {
                        type += ", " + prec;
                    }
                    type += ")";
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
                    // LOGGER.warn
                }
                String remarks = rs.getString("REMARKS");

                columns.add(new DbColumn(schema, table, position, column, type, nullable, colDefault, remarks));
            }
        } finally {
            close(rs);
        }
        return columns;
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
    }

    public static class DbTable {
        public final String schema;
        public final String name;
        public final String type;
        public final String remarks;

        protected DbTable(String schema, String name, String type, String remarks) {
            this.schema = schema;
            this.name = name;
            this.type = type;
            this.remarks = remarks;
        }
    }

    public static class DbColumn {
        public final String schema;
        public final String table;
        public final int position;
        public final String name;
        public final String dataType;
        public final boolean nullable;
        public final Object defaultValue;
        public final String remarks;

        protected DbColumn(String schema, String table, int position, String name, String dataType, boolean nullable, Object defaultValue, String remarks) {
            this.schema = schema;
            this.table = table;
            this.position = position;
            this.name = name;
            this.dataType = dataType;
            this.nullable = nullable;
            this.defaultValue = defaultValue;
            this.remarks = remarks;
        }

        @Override
        public String toString() {
            return table + '.' + name + '(' + dataType + ')';
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
        ResultSet rs = executeQueryOnly(conn, sql, parameters);
        Statement st = rs.getStatement();

        try {
            return resultSetToListMap(rs, null);
        } finally {
            close(rs);
            close(st);
        }
    }


    /**
     * SQL Query
     * 警告:
     * 该方法不关闭创建的 Statement (否则结果集无法读取,会提示已经关闭的语句),
     * 因此外部必须通过 rs.getStatement() 来关闭, 不关闭会超出打开游标的最大数
     */
    public static ResultSet executeQueryOnly(Connection conn, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            setParameters(stmt, parameters);
            return stmt.executeQuery();
        } finally {
            // 不能关闭, 否则结果集无法读取, 不关闭会超出打开游标的最大数, 外部必须关闭 rs.getStatement()
            // close(stmt);
        }
    }

    public static PreparedStatement prepareStatement(final Connection conn, final String sql, final Object... params) throws SQLException {
        final PreparedStatement st = conn.prepareStatement(sql);
        for (int i = 0, size = params.length; i < size; ++i) {
            st.setObject(i + 1, params[i]); // jdbc 索引从 1 开始
        }
        return st;
    }


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
     * @throws SQLException
     */
    public static int executeUpdate(DataSource dataSource, String sql, List<Object> parameters) throws SQLException {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return executeUpdate(conn, sql, parameters);
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
     * @throws SQLException
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
     * @throws SQLException
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
     * @throws SQLException
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
     * @throws SQLException
     */
    public static void execute(Connection conn, String sql, List<Object> parameters) throws SQLException {
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);

            setParameters(stmt, parameters);

            stmt.execute();
        } finally {
            close(stmt);
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

    /**
     * 警告: 该方法不关闭 rs
     *
     * @param rs
     * @param rows
     * @return
     * @throws SQLException
     */
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

    public static void printColumnLabel(ResultSetMetaData meta) throws SQLException {
        int count = meta.getColumnCount();
        for (int i = 1; i <= count; i++) {
            System.out.print(meta.getColumnLabel(i) + " | ");
        }
        System.out.println();
    }

    /**
     * 在终端打印 ReusltSet
     *
     * @param rs
     * @throws SQLException
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
                    final String value = rs.getString(columnIndex);
                    out.print(rs.wasNull() ? "(null)" : value);
                } else if (type == Types.DATE) {
                    final Date value = rs.getDate(columnIndex);
                    out.print(rs.wasNull() ? "(null)" : value);
                } else if (type == Types.BIT) {
                    boolean value = rs.getBoolean(columnIndex);
                    if (rs.wasNull()) {
                        out.print("(null)");
                    } else {
                        out.print(Boolean.toString(value));
                    }
                } else if (type == Types.BOOLEAN) {
                    boolean value = rs.getBoolean(columnIndex);
                    if (rs.wasNull()) {
                        out.print("(null)");
                    } else {
                        out.print(Boolean.toString(value));
                    }
                } else if (type == Types.TINYINT) {
                    byte value = rs.getByte(columnIndex);
                    if (rs.wasNull()) {
                        out.print("(null)");
                    } else {
                        out.print(Byte.toString(value));
                    }
                } else if (type == Types.SMALLINT) {
                    short value = rs.getShort(columnIndex);
                    if (rs.wasNull()) {
                        out.print("(null)");
                    } else {
                        out.print(Short.toString(value));
                    }
                } else if (type == Types.INTEGER) {
                    int value = rs.getInt(columnIndex);
                    if (rs.wasNull()) {
                        out.print("(null)");
                    } else {
                        out.print(Integer.toString(value));
                    }
                } else if (type == Types.BIGINT) {
                    long value = rs.getLong(columnIndex);
                    if (rs.wasNull()) {
                        out.print("(null)");
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
                        out.print("(null)");
                    } else {
                        out.print(String.valueOf(objec));
                    }
                } else if (type == Types.LONGVARCHAR) {
                    Object objec = rs.getString(columnIndex);

                    if (rs.wasNull()) {
                        out.print("(null)");
                    } else {
                        out.print(String.valueOf(objec));
                    }
                } else if (type == Types.NULL) {
                    out.print("(null)");
                } else {
                    Object objec = rs.getObject(columnIndex);

                    if (rs.wasNull()) {
                        out.print("(null)");
                    } else {
                        if (objec instanceof byte[]) {
                            /* final byte[] bytes = (byte[]) objec; */
                            out.print("(bytes[])");
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
     * @throws SQLException
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
     * @throws SQLException
     */
    public static String getDriverClassName(String rawUrl) throws SQLException {
        if (rawUrl.startsWith("jdbc:derby:")) {
            return DERBY_DRIVER;
        } else if (rawUrl.startsWith("jdbc:mysql:")) {
            return MYSQL_DRIVER;
        } else if (rawUrl.startsWith("jdbc:oracle:") || rawUrl.startsWith("JDBC:oracle:")) {
            return ORACLE_DRIVER;
        } else if (rawUrl.startsWith("jdbc:postgresql:") || rawUrl.startsWith("jdbc:vertica:")) {
            return POSTGRE_SQL_DRIVER;
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
     * @return True if this is an Apache Derby database.
     */
    public static boolean isDerby(String url) {
        return DERBY.equals(getDbType(url));
    }

    /**
     * @return True if this is a Firebird database.
     */
    public static boolean isFirebird(String url) {
        return FIREBIRD_SQL.equals(getDbType(url));
    }

    /**
     * @return True if this is a H2 database.
     */
    public static boolean isH2(String url) {
        return H2.equals(url);
    }

    /**
     * @return True if this is a MS SQL Server database.
     */
    public static boolean isMSSQLServer(String url) {
        return SQL_SERVER.equals(getDbType(url)) || JTDS.equals(getDbType(url));
    }

    /**
     * @return True if this is a MySQL database.
     */
    public static boolean isMySQL(String url) {
        return MYSQL.equals(getDbType(url));
    }

    /**
     * @return True if this is an Oracle database.
     */
    public static boolean isOracle(String url) {
        return ORACLE.equals(getDbType(url));
    }

    /**
     * @return True if this is a PostgreSQL database.
     */
    public static boolean isPostgreSQL(String url) {
        return POSTGRE_SQL.equals(getDbType(url));
    }

    /**
     * @return True if this is an SQLite database.
     */
    public static boolean isSQLite(String url) {
        return SQLITE.equals(getDbType(url));
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
        }
        if (rawUrl.startsWith("jdbc:firebirdsql:")) {
            return FIREBIRD_SQL;
        }
        if (rawUrl.startsWith("jdbc:mysql:")) {
            return MYSQL;
        }
        if (rawUrl.startsWith("jdbc:oracle:")) {
            return ORACLE;
        } else if (rawUrl.startsWith("jdbc:postgresql:") || rawUrl.startsWith("jdbc:vertica:")) {
            return POSTGRE_SQL;
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
     * 获取 {@link Types} 的名称
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
                LOGGER.error("close connection error", e);
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
                LOGGER.error("close statement error", e);
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
                LOGGER.error("close resultset error", e);
            }
        }
    }

    private Jdbc() {
    }


    public static String getSchema(DatabaseMetaData meta) throws SQLException {
        String username = meta.getUserName();
        String url = meta.getURL();

        System.out.println(String.format("Connection: \t[%s - %s]", username, url));

        String driverName = meta.getDriverName();
        String driverVersion = meta.getDriverVersion();

        System.out.println(String.format("Driver: \t%s %s", driverName, driverVersion));

        int majorVer = meta.getDriverMajorVersion();
        int minorVer = meta.getDriverMinorVersion();
        System.out.println("d major v:" + meta.getDriverMajorVersion());
        System.out.println("d minor v:" + meta.getDriverMinorVersion());
        System.out.println("j major v:" + meta.getJDBCMajorVersion());
        System.out.println("j minor v:" + meta.getJDBCMinorVersion());

        String dbName = meta.getDatabaseProductName();
        String dbVersion = meta.getDatabaseProductVersion();

        System.out.println(String.format("DB: [%s %s]", dbName, dbVersion));
        int major = meta.getDatabaseMajorVersion();
        System.out.println("major:" + major);
        System.out.println("minor:" + meta.getDatabaseMinorVersion());

//        meta.getProcedures(null, null, null, null);


        // getFunctions since 1.6
        // ResultSet rs = Reflections.invokeIfAvaliable(meta, "getFunctions", false, null/*catalog*/, null/*schema*/, null/*namePattern*/);
        // meta.getProcedures() since 1.7
        ResultSet rs = null;//invokeIfAvaliable(meta, "getProcedures", false, null/*catalog*/, null/*schema*/, null/*namePattern*/);
        while (rs.next()) {
            printColumnLabel(rs.getMetaData());
            System.exit(0);
            String cat = rs.getString("FUNCTION_CAT");
            String schema = rs.getString("FUNCTION_SCHEM");
            String name = rs.getString("FUNCTION_NAME");
            String remarks = rs.getString("REMARKS");

            ResultSet cRs = meta.getFunctionColumns(null, schema, name, null);
            printColumnLabel(rs.getMetaData());
            System.exit(0);

            ResultSetMetaData metaData = cRs.getMetaData();
            int cou = metaData.getColumnCount();
            for (int i = 1; i <= cou; i++) {
                System.out.println(metaData.getColumnLabel(i));
            }
            /*
            while (cRs.next()) {
                cRs.getString("FUNCTION_CAT");
                cRs.getString("FUNCTION_SCHEM");
                cRs.getString("FUNCTION_NAME");
                cRs.getString("COLUMN_NAME");
                cRs.getString("COLUMN_TYPE");
                cRs.getString("DATA_TYPE");
                cRs.getString("TYPE_NAME");
                cRs.getString("PRECISION");
                cRs.getString("LENGTH");
                cRs.getString("SCALE");
                cRs.getString("RADIX");
                cRs.getString("NULLABLE");
                cRs.getString("REMARKS");
                cRs.getString("COLUMN_DEF");
                cRs.getString("SQL_DATA_TYPE");
                cRs.getString("SQL_DATETIME_SUB");
                cRs.getString("CHAR_OCTET_LENGTH");
                cRs.getString("ORDINAL_POSITION");
                cRs.getString("IS_NULLABLE");
                cRs.getString("SPECIFIC_NAME");
                cRs.getString("SEQUENCE");
                cRs.getString("OVERLOAD");
                cRs.getString("DEFAULT_VALUE");
            }
            */
//            System.out.print(name);
            System.out.println();
        }
        String sysFns = meta.getSystemFunctions();
        System.out.println("sys fn: " + sysFns);


        return null;
    }

    public static void getFunctions(final DatabaseMetaData metadata, final String catalogPattern, final String schemaPattern, final String procedurePattern) {
        if (null != GET_FUNCTIONS) {
            final ResultSet rs = apply(GET_PROCEDURES, metadata, catalogPattern, schemaPattern, procedurePattern);
            try {
                while (rs.next()) {
                    collectProcedureOrFunction(metadata, rs, "FUNCTION");
                    System.out.println();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                close(rs);
            }
        }
    }

    public static void getProcedures(final DatabaseMetaData metadata, final String catalogPattern, final String schemaPattern, final String procedurePattern) {
        if (null != GET_PROCEDURES) {
            final ResultSet rs = apply(GET_PROCEDURES, metadata, catalogPattern, schemaPattern, procedurePattern);
            try {
                while (rs.next()) {
                    collectProcedureOrFunction(metadata, rs, "PROCEDURE");
                    System.out.println();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                close(rs);
            }
        }
    }

    /**
     * PROCEDURE
     *
     * @param rs
     * @param prefix
     */
    private static void collectProcedureOrFunction(final DatabaseMetaData metadata, final ResultSet rs, final String prefix) throws SQLException {
        String cat = rs.getString("PROCEDURE_CAT");
        String schema = rs.getString("PROCEDURE_SCHEM");
        String func = rs.getString("PROCEDURE_NAME");

        String type = rs.getString("PROCEDURE_TYPE");
        String remarks = rs.getString("REMARKS");

        final Method getColumns = "PROCEDURE".equals(prefix) ? GET_PROCEDURE_COLUMNS : GET_FUNCTIONS_COLUMNS;
        if (null != getColumns) {
            ResultSet columnRs = apply(getColumns, metadata, cat, schema, func, null);
            while (columnRs.next()) {
                // String cat = rs.getString(prefix + "_CAT");
                // String schema = rs.getString(prefix + "_SCHEM");
                // String procedure = rs.getString(prefix + "_NAME");
                String columnName = columnRs.getString("COLUMN_NAME");
                String columnType = columnRs.getString("COLUMN_TYPE");
                String dataType = columnRs.getString("DATA_TYPE");
                String typeName = columnRs.getString("TYPE_NAME");
                String precision = columnRs.getString("PRECISION");
                String length = columnRs.getString("LENGTH");
                String scale = columnRs.getString("SCALE");
                String radix = columnRs.getString("RADIX");
                String nullable = columnRs.getString("NULLABLE");
                String cremarks = columnRs.getString("REMARKS");
                String def = columnRs.getString("COLUMN_DEF");
                String sqlDataType = columnRs.getString("SQL_DATA_TYPE");
                String sqlDatetimeSub = columnRs.getString("SQL_DATETIME_SUB");
                String ordinalPosition = columnRs.getString("ORDINAL_POSITION");
                String isNullable = columnRs.getString("IS_NULLABLE");
                System.out.println();
            }
        }
        System.out.println();
    }

    public static void getProcedureColumns(final DatabaseMetaData metadata,
                                           final String catalogPattern, final String schemaPattern,
                                           final String procedurePattern, final String columnPattern) {
        if (null != GET_PROCEDURE_COLUMNS) {
            final ResultSet rs = apply(GET_PROCEDURE_COLUMNS, metadata, catalogPattern, schemaPattern, procedurePattern, columnPattern);
            try {
                while (rs.next()) {
                    System.out.println();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <R> R apply(final Method method, final Object target, final Object... args) {
        try {
            return (R) method.invoke(target, args);
        } catch (final IllegalAccessException e) {
            throw new UndeclaredThrowableException(e);
        } catch (final InvocationTargetException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /* since 1.6 */
    private static final Method GET_FUNCTIONS = findMethod(DatabaseMetaData.class, "getFunctions", String.class, String.class, String.class);
    private static final Method GET_FUNCTIONS_COLUMNS = findMethod(DatabaseMetaData.class, "getFunctionColumns", String.class, String.class, String.class, String.class);

    /* since 1.7 */
    private static final Method GET_PROCEDURES = findMethod(DatabaseMetaData.class, "getProcedures", String.class, String.class, String.class);
    private static final Method GET_PROCEDURE_COLUMNS = findMethod(DatabaseMetaData.class, "getProcedureColumns", String.class, String.class, String.class, String.class);

    private static Method findMethod(final Class<?> clazz, final String method, final Class<?>... paramTypes) {
        try {
            return clazz.getMethod(method, paramTypes);
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    static String getDefaultSchema(final DatabaseMetaData metadata) {
        /*-
         * since 1.7
         * meta.getConnection().getSchema();
         */
        String schema = "";
        try {
            final String url = metadata.getURL();

            if (isOracle(url)) {
                return metadata.getUserName();
            }
            if (isPostgreSQL(url)) {
                return "public";
            }
            if (isMySQL(url)) {
                return "";
            }
            if (isDerby(url)) {
                return metadata.getUserName().toUpperCase(Locale.ENGLISH);
            }
            if (isFirebird(url)) {
                return null;
            }
            ResultSet rs = metadata.getSchemas();
            int index = rs.findColumn("IS_DEFAULT");
            while (rs.next()) {
                if (rs.getBoolean(index)) {
                    schema = rs.getString("TABLE_SCHEM");
                    break;
                }
            }
        } catch (SQLException ignore) {
            /* IS_DEFAULT not find */
        }
        return schema;
    }

    public static Connection connect(final String url, final String username, final String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public ResultSet executeQuery(final PreparedStatement st, final Object... params) throws SQLException {
        return bind(st, params).executeQuery();
    }

    public int executeUpdate(final Connection conn, final String sql, final Object... params) throws SQLException {
        PreparedStatement st = null;
        try {
            st = newStatement(conn, sql, params);
            return st.executeUpdate();
        } finally {
            close(st);
        }
    }

    public int executeUpdate(final PreparedStatement st, final Object... params) throws SQLException {
        return bind(st, params).executeUpdate();
    }

    public static NamedParameterStatement _bind(final NamedParameterStatement st, final Map<String, Object> params) throws SQLException {
        if (null != params) {
            for (final Map.Entry<String, ?> entry : params.entrySet()) {
                st.setObject(entry.getKey(), entry.getValue());
            }
        }
        return st;
    }


    public static PreparedStatement bind(final PreparedStatement st, final Object... params) throws SQLException {
        final int max = st.getParameterMetaData().getParameterCount();
        if (params.length > max) {
            throw new IllegalArgumentException("too many parameters " + params.length + " > " + max);
        }
        for (int i = 0; i < params.length; i++) {
            st.setObject(i + 1, params[i]);
        }
        return st;
    }

    public static PreparedStatement newStatement(final Connection conn, final String sql,
                                                 final Object... params) throws SQLException {
        return bind(newStatement(conn, sql), params);
    }

    public static NamedParameterStatement newStatement(final Connection conn, final String sql,
                                                       final Map<String, ?> params) throws SQLException {
        return null;// return _bind((NamedParameterStatement) DelegatingNamedParameterStatement.newStatement(conn, sql), params);
    }

    public static PreparedStatement newStatement(final Connection conn, final String sql) throws SQLException {
        if (DelegatingNamedParameterStatement.hasNamedParameters(sql)) {
            return DelegatingNamedParameterStatement.newStatement(conn, sql);
        }
        return conn.prepareStatement(sql);
    }
}
