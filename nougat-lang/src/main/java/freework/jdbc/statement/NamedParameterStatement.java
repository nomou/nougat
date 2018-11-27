package freework.jdbc.statement;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLTimeoutException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.util.Calendar;

/**
 */
public interface NamedParameterStatement extends PreparedStatement {

    int getNamedParameterCount();

    /**
     * Sets the designated parameter to SQL <code>NULL</code>.
     * <p/>
     * <P><B>Note:</B> You must specify the parameter's SQL type.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param sqlType       the SQL type code defined in <code>java.sql.Types</code>
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if <code>sqlType</code> is
     *                                         a <code>ARRAY</code>, <code>BLOB</code>, <code>CLOB</code>,
     *                                         <code>DATALINK</code>, <code>JAVA_OBJECT</code>, <code>NCHAR</code>,
     *                                         <code>NCLOB</code>, <code>NVARCHAR</code>, <code>LONGNVARCHAR</code>,
     *                                         <code>REF</code>, <code>ROWID</code>, <code>SQLXML</code>
     *                                         or  <code>STRUCT</code> data type and the JDBC driver does not support
     *                                         this data type
     */
    void setNull(final String parameterName, int sqlType) throws SQLException;

    /**
     * Sets the designated parameter to the given Java <code>boolean</code> value.
     * The driver converts this
     * to an SQL <code>BIT</code> or <code>BOOLEAN</code> value when it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement;
     *                      if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setBoolean(final String parameterName, boolean x) throws SQLException;

    /**
     * Sets the designated parameter to the given Java <code>byte</code> value.
     * The driver converts this
     * to an SQL <code>TINYINT</code> value when it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setByte(final String parameterName, byte x) throws SQLException;

    /**
     * Sets the designated parameter to the given Java <code>short</code> value.
     * The driver converts this
     * to an SQL <code>SMALLINT</code> value when it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setShort(final String parameterName, short x) throws SQLException;

    /**
     * Sets the designated parameter to the given Java <code>int</code> value.
     * The driver converts this
     * to an SQL <code>INTEGER</code> value when it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setInt(final String parameterName, int x) throws SQLException;

    /**
     * Sets the designated parameter to the given Java <code>long</code> value.
     * The driver converts this
     * to an SQL <code>BIGINT</code> value when it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setLong(final String parameterName, long x) throws SQLException;

    /**
     * Sets the designated parameter to the given Java <code>float</code> value.
     * The driver converts this
     * to an SQL <code>REAL</code> value when it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setFloat(final String parameterName, float x) throws SQLException;

    /**
     * Sets the designated parameter to the given Java <code>double</code> value.
     * The driver converts this
     * to an SQL <code>DOUBLE</code> value when it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setDouble(final String parameterName, double x) throws SQLException;

    /**
     * Sets the designated parameter to the given <code>java.math.BigDecimal</code> value.
     * The driver converts this to an SQL <code>NUMERIC</code> value when
     * it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setBigDecimal(final String parameterName, BigDecimal x) throws SQLException;

    /**
     * Sets the designated parameter to the given Java <code>String</code> value.
     * The driver converts this
     * to an SQL <code>VARCHAR</code> or <code>LONGVARCHAR</code> value
     * (depending on the argument's
     * size relative to the driver's limits on <code>VARCHAR</code> values)
     * when it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setString(final String parameterName, String x) throws SQLException;

    /**
     * Sets the designated parameter to the given Java array of bytes.  The driver converts
     * this to an SQL <code>VARBINARY</code> or <code>LONGVARBINARY</code>
     * (depending on the argument's size relative to the driver's limits on
     * <code>VARBINARY</code> values) when it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setBytes(final String parameterName, byte x[]) throws SQLException;

    /**
     * Sets the designated parameter to the given <code>java.sql.Date</code> value
     * using the default time zone of the virtual machine that is running
     * the application.
     * The driver converts this
     * to an SQL <code>DATE</code> value when it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setDate(final String parameterName, java.sql.Date x) throws SQLException;

    /**
     * Sets the designated parameter to the given <code>java.sql.Time</code> value.
     * The driver converts this
     * to an SQL <code>TIME</code> value when it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setTime(final String parameterName, java.sql.Time x)
            throws SQLException;

    /**
     * Sets the designated parameter to the given <code>java.sql.Timestamp</code> value.
     * The driver
     * converts this to an SQL <code>TIMESTAMP</code> value when it sends it to the
     * database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setTimestamp(final String parameterName, java.sql.Timestamp x)
            throws SQLException;

    /**
     * Sets the designated parameter to the given input stream, which will have
     * the specified number of bytes.
     * When a very large ASCII value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code>. Data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from ASCII to the database char format.
     * <p/>
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the Java input stream that contains the ASCII parameter value
     * @param length        the number of bytes in the stream
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setAsciiStream(final String parameterName, InputStream x, int length)
            throws SQLException;

    /**
     * Sets the designated parameter to the given input stream, which
     * will have the specified number of bytes.
     * <p/>
     * When a very large Unicode value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from the
     * stream as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from Unicode to the database char format.
     * <p/>
     * The byte format of the Unicode stream must be a Java UTF-8, as defined in the
     * Java Virtual Machine Specification.
     * <p/>
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             a <code>java.io.InputStream</code> object that contains the
     *                      Unicode parameter value
     * @param length        the number of bytes in the stream
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support
     *                                         this method
     * @deprecated
     */
    void setUnicodeStream(final String parameterName, InputStream x,
                          int length) throws SQLException;

    /**
     * Sets the designated parameter to the given input stream, which will have
     * the specified number of bytes.
     * When a very large binary value is input to a <code>LONGVARBINARY</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from the
     * stream as needed until end-of-file is reached.
     * <p/>
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the java input stream which contains the binary parameter value
     * @param length        the number of bytes in the stream
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     */
    void setBinaryStream(final String parameterName, InputStream x,
                         int length) throws SQLException;

    //----------------------------------------------------------------------
    // Advanced features:

    /**
     * Sets the value of the designated parameter with the given object.
     * This method is like the method <code>setObject</code>
     * above, except that it assumes a scale of zero.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
     *                      sent to the database
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if <code>targetSqlType</code> is
     *                                         a <code>ARRAY</code>, <code>BLOB</code>, <code>CLOB</code>,
     *                                         <code>DATALINK</code>, <code>JAVA_OBJECT</code>, <code>NCHAR</code>,
     *                                         <code>NCLOB</code>, <code>NVARCHAR</code>, <code>LONGNVARCHAR</code>,
     *                                         <code>REF</code>, <code>ROWID</code>, <code>SQLXML</code>
     *                                         or  <code>STRUCT</code> data type and the JDBC driver does not support
     *                                         this data type
     * @see java.sql.Types
     */
    void setObject(final String parameterName, Object x, int targetSqlType) throws SQLException;

    /**
     * <p>Sets the value of the designated parameter using the given object.
     * The second parameter must be of type <code>Object</code>; therefore, the
     * <code>java.lang</code> equivalent objects should be used for built-in types.
     * <p/>
     * <p>The JDBC specification specifies a standard mapping from
     * Java <code>Object</code> types to SQL types.  The given argument
     * will be converted to the corresponding SQL type before being
     * sent to the database.
     * <p/>
     * <p>Note that this method may be used to pass datatabase-
     * specific abstract data types, by using a driver-specific Java
     * type.
     * <p/>
     * If the object is of a class implementing the interface <code>SQLData</code>,
     * the JDBC driver should call the method <code>SQLData.writeSQL</code>
     * to write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * <code>Ref</code>, <code>Blob</code>, <code>Clob</code>,  <code>NClob</code>,
     * <code>Struct</code>, <code>java.net.URL</code>, <code>RowId</code>, <code>SQLXML</code>
     * or <code>Array</code>, the driver should pass it to the database as a
     * value of the corresponding SQL type.
     * <p/>
     * <b>Note:</b> Not all databases allow for a non-typed Null to be sent to
     * the backend. For maximum portability, the <code>setNull</code> or the
     * <code>setObject(final String parameterName, Object x, int sqlType)</code>
     * method should be used
     * instead of <code>setObject(final String parameterName, Object x)</code>.
     * <p/>
     * <b>Note:</b> This method throws an exception if there is an ambiguity, for example, if the
     * object is of a class implementing more than one of the interfaces named above.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the object containing the input parameter value
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs;
     *                      this method is called on a closed <code>PreparedStatement</code>
     *                      or the type of the given object is ambiguous
     */
    void setObject(final String parameterName, Object x) throws SQLException;

    /**
     * Executes the SQL statement in this <code>PreparedStatement</code> object,
     * which may be any kind of SQL statement.
     * Some prepared statements return multiple results; the <code>execute</code>
     * method handles these complex statements as well as the simpler
     * form of statements handled by the methods <code>executeQuery</code>
     * and <code>executeUpdate</code>.
     * <p/>
     * The <code>execute</code> method returns a <code>boolean</code> to
     * indicate the form of the first result.  You must call either the method
     * <code>getResultSet</code> or <code>getUpdateCount</code>
     * to retrieve the result; you must call <code>getMoreResults</code> to
     * move to any subsequent result(s).
     *
     * @return <code>true</code> if the first result is a <code>ResultSet</code>
     * object; <code>false</code> if the first result is an update
     * count or there is no result
     * @throws SQLException        if a database access error occurs;
     *                             this method is called on a closed <code>PreparedStatement</code>
     *                             or an argument is supplied to this method
     * @throws SQLTimeoutException when the driver has determined that the
     *                             timeout value that was specified by the {@code setQueryTimeout}
     *                             method has been exceeded and has at least attempted to cancel
     *                             the currently running {@code Statement}
     * @see Statement#execute
     * @see Statement#getResultSet
     * @see Statement#getUpdateCount
     * @see Statement#getMoreResults
     */
    boolean execute() throws SQLException;

    //--------------------------JDBC 2.0-----------------------------

    /**
     * Sets the designated parameter to the given <code>Reader</code>
     * object, which is the given number of characters long.
     * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.Reader</code> object. The data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from UNICODE to the database char format.
     * <p/>
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param reader        the <code>java.io.Reader</code> object that contains the
     *                      Unicode data
     * @param length        the number of characters in the stream
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     * @since 1.2
     */
    void setCharacterStream(final String parameterName,
                            Reader reader,
                            int length) throws SQLException;

    /**
     * Sets the designated parameter to the given
     * <code>REF(&lt;structured-type&gt;)</code> value.
     * The driver converts this to an SQL <code>REF</code> value when it
     * sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             an SQL <code>REF</code> value
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.2
     */
    void setRef(final String parameterName, Ref x) throws SQLException;

    /**
     * Sets the designated parameter to the given <code>java.sql.Blob</code> object.
     * The driver converts this to an SQL <code>BLOB</code> value when it
     * sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             a <code>Blob</code> object that maps an SQL <code>BLOB</code> value
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.2
     */
    void setBlob(final String parameterName, Blob x) throws SQLException;

    /**
     * Sets the designated parameter to the given <code>java.sql.Clob</code> object.
     * The driver converts this to an SQL <code>CLOB</code> value when it
     * sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             a <code>Clob</code> object that maps an SQL <code>CLOB</code> value
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.2
     */
    void setClob(final String parameterName, Clob x) throws SQLException;

    /**
     * Sets the designated parameter to the given <code>java.sql.Array</code> object.
     * The driver converts this to an SQL <code>ARRAY</code> value when it
     * sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             an <code>Array</code> object that maps an SQL <code>ARRAY</code> value
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.2
     */
    void setArray(final String parameterName, Array x) throws SQLException;

    /**
     * Sets the designated parameter to the given <code>java.sql.Date</code> value,
     * using the given <code>Calendar</code> object.  The driver uses
     * the <code>Calendar</code> object to construct an SQL <code>DATE</code> value,
     * which the driver then sends to the database.  With
     * a <code>Calendar</code> object, the driver can calculate the date
     * taking into account a custom timezone.  If no
     * <code>Calendar</code> object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the application.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @param cal           the <code>Calendar</code> object the driver will use
     *                      to construct the date
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     * @since 1.2
     */
    void setDate(final String parameterName, java.sql.Date x, Calendar cal)
            throws SQLException;

    /**
     * Sets the designated parameter to the given <code>java.sql.Time</code> value,
     * using the given <code>Calendar</code> object.  The driver uses
     * the <code>Calendar</code> object to construct an SQL <code>TIME</code> value,
     * which the driver then sends to the database.  With
     * a <code>Calendar</code> object, the driver can calculate the time
     * taking into account a custom timezone.  If no
     * <code>Calendar</code> object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the application.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @param cal           the <code>Calendar</code> object the driver will use
     *                      to construct the time
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     * @since 1.2
     */
    void setTime(final String parameterName, java.sql.Time x, Calendar cal)
            throws SQLException;

    /**
     * Sets the designated parameter to the given <code>java.sql.Timestamp</code> value,
     * using the given <code>Calendar</code> object.  The driver uses
     * the <code>Calendar</code> object to construct an SQL <code>TIMESTAMP</code> value,
     * which the driver then sends to the database.  With a
     * <code>Calendar</code> object, the driver can calculate the timestamp
     * taking into account a custom timezone.  If no
     * <code>Calendar</code> object is specified, the driver uses the default
     * timezone, which is that of the virtual machine running the application.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @param cal           the <code>Calendar</code> object the driver will use
     *                      to construct the timestamp
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     * @since 1.2
     */
    void setTimestamp(final String parameterName, java.sql.Timestamp x, Calendar cal)
            throws SQLException;

    /**
     * Sets the designated parameter to SQL <code>NULL</code>.
     * This version of the method <code>setNull</code> should
     * be used for user-defined types and REF type parameters.  Examples
     * of user-defined types include: STRUCT, DISTINCT, JAVA_OBJECT, and
     * named array types.
     * <p/>
     * <P><B>Note:</B> To be portable, applications must give the
     * SQL type code and the fully-qualified SQL type name when specifying
     * a NULL user-defined or REF parameter.  In the case of a user-defined type
     * the name is the type name of the parameter itself.  For a REF
     * parameter, the name is the type name of the referenced type.  If
     * a JDBC driver does not need the type code or type name information,
     * it may ignore it.
     * <p/>
     * Although it is intended for user-defined and Ref parameters,
     * this method may be used to set a null parameter of any JDBC type.
     * If the parameter does not have a user-defined or REF type, the given
     * typeName is ignored.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param sqlType       a value from <code>java.sql.Types</code>
     * @param typeName      the fully-qualified name of an SQL user-defined type;
     *                      ignored if the parameter is not a user-defined type or REF
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if <code>sqlType</code> is
     *                                         a <code>ARRAY</code>, <code>BLOB</code>, <code>CLOB</code>,
     *                                         <code>DATALINK</code>, <code>JAVA_OBJECT</code>, <code>NCHAR</code>,
     *                                         <code>NCLOB</code>, <code>NVARCHAR</code>, <code>LONGNVARCHAR</code>,
     *                                         <code>REF</code>, <code>ROWID</code>, <code>SQLXML</code>
     *                                         or  <code>STRUCT</code> data type and the JDBC driver does not support
     *                                         this data type or if the JDBC driver does not support this method
     * @since 1.2
     */
    void setNull(final String parameterName, int sqlType, String typeName)
            throws SQLException;

    //------------------------- JDBC 3.0 -----------------------------------

    /**
     * Sets the designated parameter to the given <code>java.net.URL</code> value.
     * The driver converts this to an SQL <code>DATALINK</code> value
     * when it sends it to the database.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the <code>java.net.URL</code> object to be set
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.4
     */
    void setURL(final String parameterName, java.net.URL x) throws SQLException;

    //------------------------- JDBC 4.0 -----------------------------------

    /**
     * Sets the designated parameter to the given <code>java.sql.RowId</code> object. The
     * driver converts this to a SQL <code>ROWID</code> value when it sends it
     * to the database
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the parameter value
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setRowId(final String parameterName, RowId x) throws SQLException;


    /**
     * Sets the designated paramter to the given <code>String</code> object.
     * The driver converts this to a SQL <code>NCHAR</code> or
     * <code>NVARCHAR</code> or <code>LONGNVARCHAR</code> value
     * (depending on the argument's
     * size relative to the driver's limits on <code>NVARCHAR</code> values)
     * when it sends it to the database.
     *
     * @param parameterName of the first parameter is 1, the second is 2, ...
     * @param value         the parameter value
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if the driver does not support national
     *                                         character sets;  if the driver can detect that a data conversion
     *                                         error could occur; if a database access error occurs; or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setNString(final String parameterName, String value) throws SQLException;

    /**
     * Sets the designated parameter to a <code>Reader</code> object. The
     * <code>Reader</code> reads the data till end-of-file is reached. The
     * driver does the necessary conversion from Java character format to
     * the national character set in the database.
     *
     * @param parameterName of the first parameter is 1, the second is 2, ...
     * @param value         the parameter value
     * @param length        the number of characters in the parameter data.
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if the driver does not support national
     *                                         character sets;  if the driver can detect that a data conversion
     *                                         error could occur; if a database access error occurs; or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setNCharacterStream(final String parameterName, Reader value, long length) throws SQLException;

    /**
     * Sets the designated parameter to a <code>java.sql.NClob</code> object. The driver converts this to a
     * SQL <code>NCLOB</code> value when it sends it to the database.
     *
     * @param parameterName of the first parameter is 1, the second is 2, ...
     * @param value         the parameter value
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if the driver does not support national
     *                                         character sets;  if the driver can detect that a data conversion
     *                                         error could occur; if a database access error occurs; or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setNClob(final String parameterName, NClob value) throws SQLException;

    /**
     * Sets the designated parameter to a <code>Reader</code> object.  The reader must contain  the number
     * of characters specified by length otherwise a <code>SQLException</code> will be
     * generated when the <code>PreparedStatement</code> is executed.
     * This method differs from the <code>setCharacterStream (int, Reader, int)</code> method
     * because it informs the driver that the parameter value should be sent to
     * the server as a <code>CLOB</code>.  When the <code>setCharacterStream</code> method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a <code>LONGVARCHAR</code> or a <code>CLOB</code>
     *
     * @param parameterName index of the first parameter is 1, the second is 2, ...
     * @param reader        An object that contains the data to set the parameter value to.
     * @param length        the number of characters in the parameter data.
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs; this method is called on
     *                                         a closed <code>PreparedStatement</code> or if the length specified is less than zero.
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setClob(final String parameterName, Reader reader, long length)
            throws SQLException;

    /**
     * Sets the designated parameter to a <code>InputStream</code> object.  The inputstream must contain  the number
     * of characters specified by length otherwise a <code>SQLException</code> will be
     * generated when the <code>PreparedStatement</code> is executed.
     * This method differs from the <code>setBinaryStream (int, InputStream, int)</code>
     * method because it informs the driver that the parameter value should be
     * sent to the server as a <code>BLOB</code>.  When the <code>setBinaryStream</code> method is used,
     * the driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a <code>LONGVARBINARY</code> or a <code>BLOB</code>
     *
     * @param parameterName index of the first parameter is 1,
     *                      the second is 2, ...
     * @param inputStream   An object that contains the data to set the parameter
     *                      value to.
     * @param length        the number of bytes in the parameter data.
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs;
     *                                         this method is called on a closed <code>PreparedStatement</code>;
     *                                         if the length specified
     *                                         is less than zero or if the number of bytes in the inputstream does not match
     *                                         the specfied length.
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setBlob(final String parameterName, InputStream inputStream, long length)
            throws SQLException;

    /**
     * Sets the designated parameter to a <code>Reader</code> object.  The reader must contain  the number
     * of characters specified by length otherwise a <code>SQLException</code> will be
     * generated when the <code>PreparedStatement</code> is executed.
     * This method differs from the <code>setCharacterStream (int, Reader, int)</code> method
     * because it informs the driver that the parameter value should be sent to
     * the server as a <code>NCLOB</code>.  When the <code>setCharacterStream</code> method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a <code>LONGNVARCHAR</code> or a <code>NCLOB</code>
     *
     * @param parameterName index of the first parameter is 1, the second is 2, ...
     * @param reader        An object that contains the data to set the parameter value to.
     * @param length        the number of characters in the parameter data.
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if the length specified is less than zero;
     *                                         if the driver does not support national character sets;
     *                                         if the driver can detect that a data conversion
     *                                         error could occur;  if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setNClob(final String parameterName, Reader reader, long length)
            throws SQLException;

    /**
     * Sets the designated parameter to the given <code>java.sql.SQLXML</code> object.
     * The driver converts this to an
     * SQL <code>XML</code> value when it sends it to the database.
     * <p/>
     *
     * @param parameterName index of the first parameter is 1, the second is 2, ...
     * @param xmlObject     a <code>SQLXML</code> object that maps an SQL <code>XML</code> value
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs;
     *                                         this method is called on a closed <code>PreparedStatement</code>
     *                                         or the <code>java.xml.transform.Result</code>,
     *                                         <code>Writer</code> or <code>OutputStream</code> has not been closed for
     *                                         the <code>SQLXML</code> object
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setSQLXML(final String parameterName, SQLXML xmlObject) throws SQLException;

    /**
     * <p>Sets the value of the designated parameter with the given object. The second
     * argument must be an object type; for integral values, the
     * <code>java.lang</code> equivalent objects should be used.
     * <p/>
     * If the second argument is an <code>InputStream</code> then the stream must contain
     * the number of bytes specified by scaleOrLength.  If the second argument is a
     * <code>Reader</code> then the reader must contain the number of characters specified
     * by scaleOrLength. If these conditions are not true the driver will generate a
     * <code>SQLException</code> when the prepared statement is executed.
     * <p/>
     * <p>The given Java object will be converted to the given targetSqlType
     * before being sent to the database.
     * <p/>
     * If the object has a custom mapping (is of a class implementing the
     * interface <code>SQLData</code>),
     * the JDBC driver should call the method <code>SQLData.writeSQL</code> to
     * write it to the SQL data stream.
     * If, on the other hand, the object is of a class implementing
     * <code>Ref</code>, <code>Blob</code>, <code>Clob</code>,  <code>NClob</code>,
     * <code>Struct</code>, <code>java.net.URL</code>,
     * or <code>Array</code>, the driver should pass it to the database as a
     * value of the corresponding SQL type.
     * <p/>
     * <p>Note that this method may be used to pass database-specific
     * abstract data types.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the object containing the input parameter value
     * @param targetSqlType the SQL type (as defined in java.sql.Types) to be
     *                      sent to the database. The scale argument may further qualify this type.
     * @param scaleOrLength for <code>java.sql.Types.DECIMAL</code>
     *                      or <code>java.sql.Types.NUMERIC types</code>,
     *                      this is the number of digits after the decimal point. For
     *                      Java Object types <code>InputStream</code> and <code>Reader</code>,
     *                      this is the length
     *                      of the data in the stream or reader.  For all other types,
     *                      this value will be ignored.
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs;
     *                                         this method is called on a closed <code>PreparedStatement</code> or
     *                                         if the Java Object specified by x is an InputStream
     *                                         or Reader object and the value of the scale parameter is less
     *                                         than zero
     * @throws SQLFeatureNotSupportedException if <code>targetSqlType</code> is
     *                                         a <code>ARRAY</code>, <code>BLOB</code>, <code>CLOB</code>,
     *                                         <code>DATALINK</code>, <code>JAVA_OBJECT</code>, <code>NCHAR</code>,
     *                                         <code>NCLOB</code>, <code>NVARCHAR</code>, <code>LONGNVARCHAR</code>,
     *                                         <code>REF</code>, <code>ROWID</code>, <code>SQLXML</code>
     *                                         or  <code>STRUCT</code> data type and the JDBC driver does not support
     *                                         this data type
     * @see java.sql.Types
     * @since 1.6
     */
    void setObject(final String parameterName, Object x, int targetSqlType, int scaleOrLength)
            throws SQLException;

    /**
     * Sets the designated parameter to the given input stream, which will have
     * the specified number of bytes.
     * When a very large ASCII value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code>. Data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from ASCII to the database char format.
     * <p/>
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the Java input stream that contains the ASCII parameter value
     * @param length        the number of bytes in the stream
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     * @since 1.6
     */
    void setAsciiStream(final String parameterName, InputStream x, long length)
            throws SQLException;

    /**
     * Sets the designated parameter to the given input stream, which will have
     * the specified number of bytes.
     * When a very large binary value is input to a <code>LONGVARBINARY</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from the
     * stream as needed until end-of-file is reached.
     * <p/>
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the java input stream which contains the binary parameter value
     * @param length        the number of bytes in the stream
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     * @since 1.6
     */
    void setBinaryStream(final String parameterName, InputStream x,
                         long length) throws SQLException;

    /**
     * Sets the designated parameter to the given <code>Reader</code>
     * object, which is the given number of characters long.
     * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.Reader</code> object. The data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from UNICODE to the database char format.
     * <p/>
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param reader        the <code>java.io.Reader</code> object that contains the
     *                      Unicode data
     * @param length        the number of characters in the stream
     * @throws SQLException if parameterName does not correspond to a parameter
     *                      marker in the SQL statement; if a database access error occurs or
     *                      this method is called on a closed <code>PreparedStatement</code>
     * @since 1.6
     */
    void setCharacterStream(final String parameterName,
                            Reader reader,
                            long length) throws SQLException;
    //-----

    /**
     * Sets the designated parameter to the given input stream.
     * When a very large ASCII value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code>. Data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from ASCII to the database char format.
     * <p/>
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * <code>setAsciiStream</code> which takes a length parameter.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the Java input stream that contains the ASCII parameter value
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setAsciiStream(final String parameterName, InputStream x)
            throws SQLException;

    /**
     * Sets the designated parameter to the given input stream.
     * When a very large binary value is input to a <code>LONGVARBINARY</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.InputStream</code> object. The data will be read from the
     * stream as needed until end-of-file is reached.
     * <p/>
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * <code>setBinaryStream</code> which takes a length parameter.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param x             the java input stream which contains the binary parameter value
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setBinaryStream(final String parameterName, InputStream x)
            throws SQLException;

    /**
     * Sets the designated parameter to the given <code>Reader</code>
     * object.
     * When a very large UNICODE value is input to a <code>LONGVARCHAR</code>
     * parameter, it may be more practical to send it via a
     * <code>java.io.Reader</code> object. The data will be read from the stream
     * as needed until end-of-file is reached.  The JDBC driver will
     * do any necessary conversion from UNICODE to the database char format.
     * <p/>
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * <code>setCharacterStream</code> which takes a length parameter.
     *
     * @param parameterName the first parameter is 1, the second is 2, ...
     * @param reader        the <code>java.io.Reader</code> object that contains the
     *                      Unicode data
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setCharacterStream(final String parameterName,
                            Reader reader) throws SQLException;

    /**
     * Sets the designated parameter to a <code>Reader</code> object. The
     * <code>Reader</code> reads the data till end-of-file is reached. The
     * driver does the necessary conversion from Java character format to
     * the national character set in the database.
     * <p/>
     * <P><B>Note:</B> This stream object can either be a standard
     * Java stream object or your own subclass that implements the
     * standard interface.
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * <code>setNCharacterStream</code> which takes a length parameter.
     *
     * @param parameterName of the first parameter is 1, the second is 2, ...
     * @param value         the parameter value
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if the driver does not support national
     *                                         character sets;  if the driver can detect that a data conversion
     *                                         error could occur; if a database access error occurs; or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setNCharacterStream(final String parameterName, Reader value) throws SQLException;

    /**
     * Sets the designated parameter to a <code>Reader</code> object.
     * This method differs from the <code>setCharacterStream (int, Reader)</code> method
     * because it informs the driver that the parameter value should be sent to
     * the server as a <code>CLOB</code>.  When the <code>setCharacterStream</code> method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a <code>LONGVARCHAR</code> or a <code>CLOB</code>
     * <p/>
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * <code>setClob</code> which takes a length parameter.
     *
     * @param parameterName index of the first parameter is 1, the second is 2, ...
     * @param reader        An object that contains the data to set the parameter value to.
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs; this method is called on
     *                                         a closed <code>PreparedStatement</code>or if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setClob(final String parameterName, Reader reader)
            throws SQLException;

    /**
     * Sets the designated parameter to a <code>InputStream</code> object.
     * This method differs from the <code>setBinaryStream (int, InputStream)</code>
     * method because it informs the driver that the parameter value should be
     * sent to the server as a <code>BLOB</code>.  When the <code>setBinaryStream</code> method is used,
     * the driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a <code>LONGVARBINARY</code> or a <code>BLOB</code>
     * <p/>
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * <code>setBlob</code> which takes a length parameter.
     *
     * @param parameterName index of the first parameter is 1,
     *                      the second is 2, ...
     * @param inputStream   An object that contains the data to set the parameter
     *                      value to.
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement; if a database access error occurs;
     *                                         this method is called on a closed <code>PreparedStatement</code> or
     *                                         if parameterName does not correspond
     *                                         to a parameter marker in the SQL statement,
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setBlob(final String parameterName, InputStream inputStream) throws SQLException;

    /**
     * Sets the designated parameter to a <code>Reader</code> object.
     * This method differs from the <code>setCharacterStream (int, Reader)</code> method
     * because it informs the driver that the parameter value should be sent to
     * the server as a <code>NCLOB</code>.  When the <code>setCharacterStream</code> method is used, the
     * driver may have to do extra work to determine whether the parameter
     * data should be sent to the server as a <code>LONGNVARCHAR</code> or a <code>NCLOB</code>
     * <P><B>Note:</B> Consult your JDBC driver documentation to determine if
     * it might be more efficient to use a version of
     * <code>setNClob</code> which takes a length parameter.
     *
     * @param parameterName index of the first parameter is 1, the second is 2, ...
     * @param reader        An object that contains the data to set the parameter value to.
     * @throws SQLException                    if parameterName does not correspond to a parameter
     *                                         marker in the SQL statement;
     *                                         if the driver does not support national character sets;
     *                                         if the driver can detect that a data conversion
     *                                         error could occur;  if a database access error occurs or
     *                                         this method is called on a closed <code>PreparedStatement</code>
     * @throws SQLFeatureNotSupportedException if the JDBC driver does not support this method
     * @since 1.6
     */
    void setNClob(final String parameterName, Reader reader) throws SQLException;

}
