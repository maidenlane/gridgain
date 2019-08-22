/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 * 
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.jdbc2;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import org.apache.ignite.internal.processors.cache.query.IgniteQueryErrorCode;
import org.apache.ignite.internal.processors.query.IgniteSQLException;
import org.apache.ignite.internal.processors.query.QueryUtils;

import static java.sql.Types.BIGINT;
import static java.sql.Types.BINARY;
import static java.sql.Types.BOOLEAN;
import static java.sql.Types.DATE;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.OTHER;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TIME;
import static java.sql.Types.TIMESTAMP;
import static java.sql.Types.TINYINT;
import static java.sql.Types.VARCHAR;
import static java.sql.Types.DECIMAL;
/**
 * Utility methods for JDBC driver.
 */
public class JdbcUtils {
    /**
     * Converts Java class name to type from {@link Types}.
     *
     * @param cls Java class name.
     * @return Type from {@link Types}.
     */
    public static int type(String cls) {
        if (Boolean.class.getName().equals(cls) || boolean.class.getName().equals(cls))
            return BOOLEAN;
        else if (Byte.class.getName().equals(cls) || byte.class.getName().equals(cls))
            return TINYINT;
        else if (Short.class.getName().equals(cls) || short.class.getName().equals(cls))
            return SMALLINT;
        else if (Integer.class.getName().equals(cls) || int.class.getName().equals(cls))
            return INTEGER;
        else if (Long.class.getName().equals(cls) || long.class.getName().equals(cls))
            return BIGINT;
        else if (Float.class.getName().equals(cls) || float.class.getName().equals(cls))
            return FLOAT;
        else if (Double.class.getName().equals(cls) || double.class.getName().equals(cls))
            return DOUBLE;
        else if (String.class.getName().equals(cls))
            return VARCHAR;
        else if (byte[].class.getName().equals(cls))
            return BINARY;
        else if (Time.class.getName().equals(cls))
            return TIME;
        else if (Timestamp.class.getName().equals(cls))
            return TIMESTAMP;
        else if (Date.class.getName().equals(cls) || java.sql.Date.class.getName().equals(cls))
            return DATE;
        else if (BigDecimal.class.getName().equals(cls))
            return DECIMAL;
        else
            return OTHER;
    }

    /**
     * Converts Java class name to SQL type name.
     *
     * @param cls Java class name.
     * @return SQL type name.
     */
    public static String typeName(String cls) {
        if (Boolean.class.getName().equals(cls) || boolean.class.getName().equals(cls))
            return "BOOLEAN";
        else if (Byte.class.getName().equals(cls) || byte.class.getName().equals(cls))
            return "TINYINT";
        else if (Short.class.getName().equals(cls) || short.class.getName().equals(cls))
            return "SMALLINT";
        else if (Integer.class.getName().equals(cls) || int.class.getName().equals(cls))
            return "INTEGER";
        else if (Long.class.getName().equals(cls) || long.class.getName().equals(cls))
            return "BIGINT";
        else if (Float.class.getName().equals(cls) || float.class.getName().equals(cls))
            return "FLOAT";
        else if (Double.class.getName().equals(cls) || double.class.getName().equals(cls))
            return "DOUBLE";
        else if (String.class.getName().equals(cls))
            return "VARCHAR";
        else if (byte[].class.getName().equals(cls))
            return "BINARY";
        else if (Time.class.getName().equals(cls))
            return "TIME";
        else if (Timestamp.class.getName().equals(cls))
            return "TIMESTAMP";
        else if (Date.class.getName().equals(cls) || java.sql.Date.class.getName().equals(cls))
            return "DATE";
        else if (BigDecimal.class.getName().equals(cls))
            return "DECIMAL";
        else
            return "OTHER";
    }

    /**
     * Determines whether type is nullable.
     *
     * @param name Column name.
     * @param cls Java class name.
     * @return {@code True} if nullable.
     */
    public static boolean nullable(String name, String cls) {
        return !"_KEY".equalsIgnoreCase(name) &&
            !"_VAL".equalsIgnoreCase(name) &&
            !(boolean.class.getName().equals(cls) ||
            byte.class.getName().equals(cls) ||
            short.class.getName().equals(cls) ||
            int.class.getName().equals(cls) ||
            long.class.getName().equals(cls) ||
            float.class.getName().equals(cls) ||
            double.class.getName().equals(cls));
    }

    /**
     * Checks whether a class is SQL-compliant.
     *
     * @param cls Class.
     * @return Whether given type is SQL-compliant.
     */
    static boolean isSqlType(Class<?> cls) {
        return QueryUtils.isSqlType(cls) || cls == URL.class;
    }


    /**
     * Convert exception to {@link SQLException}.
     *
     * @param e Converted Exception.
     * @param msgForUnknown Message non-convertable exception.
     * @return JDBC {@link SQLException}.
     * @see IgniteQueryErrorCode
     */
    public static SQLException convertToSqlException(Exception e, String msgForUnknown) {
        return convertToSqlException(e, msgForUnknown, null);
    }

    /**
     * Convert exception to {@link SQLException}.
     *
     * @param e Converted Exception.
     * @param msgForUnknown Message for non-convertable exception.
     * @param sqlStateForUnknown SQLSTATE for non-convertable exception.
     * @return JDBC {@link SQLException}.
     * @see IgniteQueryErrorCode
     */
    public static SQLException convertToSqlException(Exception e, String msgForUnknown, String sqlStateForUnknown) {
        SQLException sqlEx = null;

        Throwable t = e;

        while (sqlEx == null && t != null) {
            if (t instanceof SQLException)
                return (SQLException)t;
            else if (t instanceof IgniteSQLException)
                return ((IgniteSQLException)t).toJdbcException();

            t = t.getCause();
        }

        return new SQLException(msgForUnknown, sqlStateForUnknown, e);
    }
}
