/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.testsuite.base.jpa.cleaner;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PostgreSQLDatabaseCleaner implements DatabaseCleaner {

    private static final Logger LOG = Logger.getLogger(PostgreSQLDatabaseCleaner.class.getName());
    private static final String SYSTEM_SCHEMAS = "'information_schema'," +
            "'pg_catalog'";

    private List<String> ignoredTables = new ArrayList<>();
    private String truncateSql;

    public static class Factory implements DatabaseCleaner.Factory {

        @Override
        public DatabaseCleaner create() {
            return new PostgreSQLDatabaseCleaner();
        }

    }

    @Override
    public boolean isApplicable(Connection connection) {
        try {
            return connection.getMetaData().getDatabaseProductName().startsWith("PostgreSQL");
        } catch (SQLException e) {
            throw new RuntimeException("Could not resolve the database metadata!", e);
        }
    }

    @Override
    public boolean supportsClearSchema() {
        return true;
    }

    @Override
    public void addIgnoredTable(String tableName) {
        ignoredTables.add(tableName);
    }

    @Override
    public void clearSchema(Connection c) {
        try (Statement s = c.createStatement()) {
            ResultSet rs;
            List<String> sqls = new ArrayList<>();

            // Collect schema objects
            String user = c.getMetaData().getUserName();
            LOG.log(Level.FINEST, "Collect schema objects: START");
            rs = s.executeQuery("SELECT DISTINCT TABLE_SCHEMA FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
            while (rs.next()) {
                String schema = rs.getString(1);
                sqls.add("DROP SCHEMA " + schema + " CASCADE");
                sqls.add("CREATE SCHEMA " + schema);
                sqls.add("GRANT ALL ON SCHEMA " + schema + " TO " + user);
                sqls.add("GRANT ALL ON SCHEMA " + schema + " TO " + schema);
            }
            LOG.log(Level.FINEST, "Collect schema objects: END");

            LOG.log(Level.FINEST, "Dropping schema objects: START");
            for (String sql : sqls) {
                s.execute(sql);
            }
            LOG.log(Level.FINEST, "Dropping schema objects: END");

            LOG.log(Level.FINEST, "Committing: START");
            c.commit();
            LOG.log(Level.FINEST, "Committing: END");
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException e1) {
                e.addSuppressed(e1);
            }

            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearData(Connection connection) {
        try (Statement s = connection.createStatement()) {
            // Delete data
            LOG.log(Level.FINEST, "Deleting data: START");
            if (truncateSql == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("TRUNCATE TABLE ");
                ResultSet rs = s.executeQuery("SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
                while (rs.next()) {
                    String tableSchema = rs.getString(1);
                    String tableName = rs.getString(2);
                    if (!ignoredTables.contains(tableName)) {
                        sb.append(tableSchema);
                        sb.append('.');
                        sb.append('"');
                        sb.append(tableName);
                        sb.append('"');
                        sb.append(',');
                    }
                }
                sb.setCharAt(sb.length() - 1, ' ');
                sb.append("RESTART IDENTITY CASCADE");
                truncateSql = sb.toString();
            }
            s.execute(truncateSql);
            LOG.log(Level.FINEST, "Deleting data: END");

            LOG.log(Level.FINEST, "Committing: START");
            connection.commit();
            LOG.log(Level.FINEST, "Committing: END");
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e.addSuppressed(e1);
            }

            throw new RuntimeException(e);
        }
    }

}
