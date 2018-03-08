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
public class MySQLDatabaseCleaner implements DatabaseCleaner {

    private static final Logger LOG = Logger.getLogger(MySQLDatabaseCleaner.class.getName());
    private static final String SYSTEM_SCHEMAS = "'information_schema'," +
            "'mysql'," +
            "'performance_schema'";

    private List<String> ignoredTables = new ArrayList<>();
    private List<String> truncateSqls;

    public static class Factory implements DatabaseCleaner.Factory {

        @Override
        public DatabaseCleaner create() {
            return new MySQLDatabaseCleaner();
        }

    }

    @Override
    public boolean isApplicable(Connection connection) {
        try {
            return connection.getMetaData().getDatabaseProductName().startsWith("MySQL");
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
            // Collect schema names
            LOG.log(Level.FINEST, "Collect table names: START");
            ResultSet rs = s.executeQuery("SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
            StringBuilder sb = new StringBuilder();
            sb.append("DROP TABLE ");

            if (rs.next()) {
                do {
                    String tableSchema = rs.getString(1);
                    String tableName = rs.getString(2);
                    sb.append(tableSchema);
                    sb.append('.');
                    sb.append(tableName);
                    sb.append(',');
                } while (rs.next());
            } else {
                // Nothing to clear since there are no tables
                return;
            }
            sb.setCharAt(sb.length() - 1, ' ');
            LOG.log(Level.FINEST, "Collect table names: END");

            // Disable foreign keys
            LOG.log(Level.FINEST, "Disable foreign keys: START");
            s.execute("SET FOREIGN_KEY_CHECKS = 0");
            LOG.log(Level.FINEST, "Disable foreign keys: END");

            LOG.log(Level.FINEST, "Dropping tables: START");
            String sql = sb.toString();
            s.execute(sql);
            LOG.log(Level.FINEST, "Dropping tables: END");

            // Enable foreign keys
            LOG.log(Level.FINEST, "Enabling foreign keys: START");
            s.execute("SET FOREIGN_KEY_CHECKS = 1");
            LOG.log(Level.FINEST, "Enabling foreign keys: END");

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
            // Disable foreign keys
            LOG.log(Level.FINEST, "Disable foreign keys: START");
            s.execute("SET FOREIGN_KEY_CHECKS = 0");
            LOG.log(Level.FINEST, "Disable foreign keys: END");

            // Delete data
            LOG.log(Level.FINEST, "Deleting data: START");
            if (truncateSqls == null) {
                truncateSqls = new ArrayList<>();
                ResultSet rs = s.executeQuery("SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
                while (rs.next()) {
                    String tableSchema = rs.getString(1);
                    String tableName = rs.getString(2);
                    if (!ignoredTables.contains(tableName)) {
                        truncateSqls.add("TRUNCATE " + tableSchema + "." + tableName);
                    }
                }
            }
            for (String truncateSql : truncateSqls) {
                s.execute(truncateSql);
            }
            LOG.log(Level.FINEST, "Deleting data: END");

            // Enable foreign keys
            LOG.log(Level.FINEST, "Enabling foreign keys: START");
            s.execute("SET FOREIGN_KEY_CHECKS = 1");
            LOG.log(Level.FINEST, "Enabling foreign keys: END");

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
