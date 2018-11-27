package freework.jdbc.statement;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class wraps around a {@link PreparedStatement} and allows the programmer to set parameters by name instead
 * of by index.  This eliminates any confusion as to which parameter index represents what.  This also means that
 * rearranging the SQL statement or adding a parameter doesn't involve renumbering your indices.
 * Code such as this:
 * <p/>
 * Connection con=getConnection();
 * String query="select * from my_table where name=? or address=?";
 * PreparedStatement p=con.prepareStatement(query);
 * p.setString(1, "bob");
 * p.setString(2, "123 terrace ct");
 * ResultSet rs=p.executeQuery();
 * <p/>
 * can be replaced with:
 * <p/>
 * Connection con=getConnection();
 * String query="select * from my_table where name=:name or address=:address";
 * DelegatingNamedParameterStatement p=new DelegatingNamedParameterStatement(con, query);
 * p.setString("name", "bob");
 * p.setString("address", "123 terrace ct");
 * ResultSet rs=p.executeQuery();
 *
 * @author adam_crume
 */
public class DelegatingNamedParameterStatement extends DelegatingPreparedStatement implements NamedParameterStatement {
    private static final Pattern NAMED_PATTERN = Pattern.compile("(?<!\\\\):([-_0-9a-zA-Z]+)(?=\\s+|$)");

    /**
     * Maps parameter names to arrays of ints which are the parameter indices.
     */
    private final Map<String, Set<Integer>> indexMap;

    private final String rawSql;

    public static boolean hasNamedParameters(final String sql) {
        return NAMED_PATTERN.matcher(sql).find();
    }

    public static DelegatingNamedParameterStatement newStatement(final Connection conn, final String sql) throws SQLException {
        final Matcher matcher = NAMED_PATTERN.matcher(sql);
        final Map<String/*name*/, Set<Integer>/*index*/> nameIndexMap = new HashMap<>();
        for (int i = 1; matcher.find(); i++) {
            final String name = matcher.group(1);

            Set<Integer> index = nameIndexMap.get(name);
            if (null == index) {
                nameIndexMap.put(name, new LinkedHashSet<Integer>());
                index = nameIndexMap.get(name);
            }
            index.add(i);
        }
        final String prepareSql = matcher.replaceAll("?").replace("\\:", ":");
        return new DelegatingNamedParameterStatement(conn.prepareStatement(prepareSql), nameIndexMap, sql);
    }

    /**
     * Creates a DelegatingNamedParameterStatement.  Wraps a call to
     * c.{@link Connection#prepareStatement(String) prepareStatement}.
     *
     * @param connection the database connection
     * @param query      the parameterized query
     * @throws SQLException if the statement could not be created
     */
    public DelegatingNamedParameterStatement(final PreparedStatement statement,
                                             final Map<String, Set<Integer>> nameIndexMap,
                                             final String rawSql) throws SQLException {
        super(statement);
        this.indexMap = nameIndexMap;
        this.rawSql = rawSql;
    }

    @Override
    public int getNamedParameterCount() {
        return indexMap.size();
    }

    /**
     * Returns the indexes for a parameter.
     *
     * @param name parameter name
     * @return parameter indexes
     * @throws IllegalArgumentException if the parameter does not exist
     */
    private Set<Integer> getIndex(String name) throws SQLException {
        final Set<Integer> index = indexMap.get(name);
        if (null == index || 1 > index.size()) {
            throw new SQLException("no named parameter '" + name + "' in the statement");
        }
        return index;
    }


    @Override
    public void setNull(final String parameterName, final int sqlType) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setNull(i, sqlType);
        }
    }

    @Override
    public void setBoolean(final String parameterName, final boolean x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setBoolean(i, x);
        }
    }

    @Override
    public void setByte(final String parameterName, final byte x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setByte(i, x);
        }
    }

    @Override
    public void setShort(final String parameterName, final short x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setShort(i, x);
        }
    }

    @Override
    public void setInt(final String parameterName, final int x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setInt(i, x);
        }
    }

    @Override
    public void setLong(final String parameterName, final long x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setLong(i, x);
        }
    }

    @Override
    public void setFloat(final String parameterName, final float x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setFloat(i, x);
        }
    }

    @Override
    public void setDouble(final String parameterName, final double x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setDouble(i, x);
        }
    }

    @Override
    public void setBigDecimal(final String parameterName, final BigDecimal x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setBigDecimal(i, x);
        }
    }

    @Override
    public void setString(final String parameterName, final String x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setString(i, x);
        }
    }

    @Override
    public void setBytes(final String parameterName, final byte[] x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setBytes(i, x);
        }
    }

    @Override
    public void setDate(final String parameterName, final Date x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setDate(i, x);
        }
    }

    @Override
    public void setTime(final String parameterName, final Time x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setTime(i, x);
        }
    }

    @Override
    public void setTimestamp(final String parameterName, final Timestamp x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setTimestamp(i, x);
        }
    }

    @Override
    public void setAsciiStream(final String parameterName, final InputStream x, final int length) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setAsciiStream(i, x);
        }
    }

    @Override
    public void setUnicodeStream(final String parameterName, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException("setUnicodeStream()");
    }

    @Override
    public void setBinaryStream(final String parameterName, final InputStream x, final int length) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setBinaryStream(i, x);
        }
    }

    @Override
    public void setObject(final String parameterName, final Object x, final int targetSqlType) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setObject(i, x);
        }
    }

    @Override
    public void setObject(final String parameterName, final Object x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setObject(i, x);
        }
    }

    @Override
    public void setCharacterStream(final String parameterName, final Reader reader, final int length) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setCharacterStream(i, reader, length);
        }
    }

    @Override
    public void setRef(final String parameterName, final Ref x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setRef(i, x);
        }
    }

    @Override
    public void setBlob(final String parameterName, final Blob x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setBlob(i, x);
        }
    }

    @Override
    public void setClob(final String parameterName, final Clob x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setClob(i, x);
        }
    }

    @Override
    public void setArray(final String parameterName, final Array x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setArray(i, x);
        }
    }

    @Override
    public void setDate(final String parameterName, final Date x, final Calendar cal) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setDate(i, x);
        }
    }

    @Override
    public void setTime(final String parameterName, final Time x, final Calendar cal) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setTime(i, x);
        }
    }

    @Override
    public void setTimestamp(final String parameterName, final Timestamp x, final Calendar cal) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setTimestamp(i, x);
        }
    }

    @Override
    public void setNull(final String parameterName, final int sqlType, final String typeName) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setNull(i, sqlType, typeName);
        }
    }

    @Override
    public void setURL(final String parameterName, final URL x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setURL(i, x);
        }
    }

    @Override
    public void setRowId(final String parameterName, final RowId x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setRowId(i, x);
        }
    }

    @Override
    public void setNString(final String parameterName, final String value) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setNString(i, value);
        }
    }

    @Override
    public void setNCharacterStream(final String parameterName, final Reader value, final long length) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setNCharacterStream(i, value, length);
        }
    }

    @Override
    public void setNClob(final String parameterName, final NClob value) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setNClob(i, value);
        }
    }

    @Override
    public void setClob(final String parameterName, final Reader reader, final long length) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setClob(i, reader, length);
        }
    }

    @Override
    public void setBlob(final String parameterName, final InputStream inputStream, final long length) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setBlob(i, inputStream, length);
        }
    }

    @Override
    public void setNClob(final String parameterName, final Reader reader, final long length) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setNClob(i, reader, length);
        }
    }

    @Override
    public void setSQLXML(final String parameterName, final SQLXML xmlObject) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setSQLXML(i, xmlObject);
        }
    }

    @Override
    public void setObject(final String parameterName, final Object x, final int targetSqlType, final int scaleOrLength) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setObject(i, x);
        }
    }

    @Override
    public void setAsciiStream(final String parameterName, final InputStream x, final long length) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setAsciiStream(i, x);
        }
    }

    @Override
    public void setBinaryStream(final String parameterName, final InputStream x, final long length) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setBinaryStream(i, x);
        }
    }

    @Override
    public void setCharacterStream(final String parameterName, final Reader reader, final long length) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setCharacterStream(i, reader, length);
        }
    }

    @Override
    public void setAsciiStream(final String parameterName, final InputStream x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setAsciiStream(i, x);
        }
    }

    @Override
    public void setBinaryStream(final String parameterName, final InputStream x) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setBinaryStream(i, x);
        }
    }

    @Override
    public void setCharacterStream(final String parameterName, final Reader reader) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setCharacterStream(i, reader);
        }
    }

    @Override
    public void setNCharacterStream(final String parameterName, final Reader value) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setNCharacterStream(i, value);
        }
    }

    @Override
    public void setClob(final String parameterName, final Reader reader) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setClob(i, reader);
        }
    }

    @Override
    public void setBlob(final String parameterName, final InputStream inputStream) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setBlob(i, inputStream);
        }
    }

    @Override
    public void setNClob(final String parameterName, final Reader reader) throws SQLException {
        for (final Integer i : getIndex(parameterName)) {
            setNClob(i, reader);
        }
    }
}

