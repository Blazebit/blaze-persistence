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
public class OracleDatabaseCleaner implements DatabaseCleaner {

    private static final Logger LOG = Logger.getLogger(OracleDatabaseCleaner.class.getName());

    private List<String> ignoredTables = new ArrayList<>();
    private List<String> cachedTruncateTableSql;
    private List<String> cachedConstraintDisableSql;
    private List<String> cachedConstraintEnableSql;

    public static class Factory implements DatabaseCleaner.Factory {

        @Override
        public DatabaseCleaner create() {
            return new OracleDatabaseCleaner();
        }

    }

    @Override
    public boolean isApplicable(Connection connection) {
        try {
            return connection.getMetaData().getDatabaseProductName().startsWith("Oracle");
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
            LOG.log(Level.FINEST, "Collect schema objects: START");
            rs = s.executeQuery("SELECT 'DROP TABLE ' || table_name || ' CASCADE CONSTRAINTS' FROM user_tables WHERE table_name IN (" +
                    "SELECT table_name " +
                    "FROM dba_tables " +
                    // Exclude the tables owner by sys
                    "WHERE owner NOT IN ('SYS')" +
                    // Normally, user tables aren't in sysaux
                    "      AND tablespace_name NOT IN ('SYSAUX')" +
                    // Apparently, user tables have global stats off
                    "      AND global_stats = 'NO'" +
                    // Exclude the tables with names starting like 'DEF$_'
                    "      AND table_name NOT LIKE 'DEF$\\_%' ESCAPE '\\'" +
                    ")");
            while (rs.next()) {
                sqls.add(rs.getString(1));
            }

            rs = s.executeQuery("SELECT 'DROP SEQUENCE ' || sequence_name FROM USER_SEQUENCES");
            while (rs.next()) {
                sqls.add(rs.getString(1));
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

            if (cachedTruncateTableSql == null) {
                cachedTruncateTableSql = new ArrayList<>();
                cachedConstraintDisableSql = new ArrayList<>();
                cachedConstraintEnableSql = new ArrayList<>();
                ResultSet rs = s.executeQuery("SELECT tbl.table_name, c.constraint_name FROM (" +
                        "SELECT table_name " +
                        "FROM dba_tables " +
                        // Exclude the tables owner by sys
                        "WHERE owner NOT IN ('SYS')" +
                        // Normally, user tables aren't in sysaux
                        "      AND tablespace_name NOT IN ('SYSAUX')" +
                        // Apparently, user tables have global stats off
                        "      AND global_stats = 'NO'" +
                        // Exclude the tables with names starting like 'DEF$_'
                        "      AND table_name NOT LIKE 'DEF$\\_%' ESCAPE '\\'" +
                        ") tbl LEFT JOIN user_constraints c ON tbl.table_name = c.table_name AND constraint_type = 'R'");
                while (rs.next()) {
                    String tableName = rs.getString(1);
                    String constraintName = rs.getString(2);
                    if (!ignoredTables.contains(tableName)) {
                        cachedTruncateTableSql.add("TRUNCATE TABLE " + tableName);
                        if (constraintName != null) {
                            cachedConstraintDisableSql.add("ALTER TABLE " + tableName + " DISABLE CONSTRAINT " + constraintName);
                            cachedConstraintEnableSql.add("ALTER TABLE " + tableName + " ENABLE CONSTRAINT " + constraintName);
                        }
                    }
                }
            }
            // Disable foreign keys
            LOG.log(Level.FINEST, "Disable foreign keys: START");
            for (String sql : cachedConstraintDisableSql) {
                s.execute(sql);
            }
            LOG.log(Level.FINEST, "Disable foreign keys: END");

            // Delete data
            LOG.log(Level.FINEST, "Deleting data: START");
            for (String sql : cachedTruncateTableSql) {
                s.execute(sql);
            }
            LOG.log(Level.FINEST, "Deleting data: END");

            // Enable foreign keys
            LOG.log(Level.FINEST, "Enabling foreign keys: START");
            for (String sql : cachedConstraintEnableSql) {
                s.execute(sql);
            }
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
