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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DB2DatabaseCleaner implements DatabaseCleaner {

    private static final Logger LOG = Logger.getLogger(DB2DatabaseCleaner.class.getName());
    private static final String SYSTEM_SCHEMAS = "'SYSCAT',"
            + "'SYSIBM',"
            + "'SYSIBMADM',"
            + "'SYSPUBLIC',"
            + "'SYSSTAT',"
            + "'SYSTOOLS'";

    private List<String> ignoredTables = new ArrayList<>();
    private Map<String, List<String>> cachedForeignKeys;

    public static class Factory implements DatabaseCleaner.Factory {

        @Override
        public DatabaseCleaner create() {
            return new DB2DatabaseCleaner();
        }

    }

    @Override
    public boolean isApplicable(Connection connection) {
        try {
            return connection.getMetaData().getDatabaseProductName().startsWith("DB2");
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
            rs = s.executeQuery("SELECT 'DROP INDEX \"' || TRIM(INDSCHEMA) || '\".\"' || TRIM(INDNAME) || '\"' " +
                    "FROM SYSCAT.INDEXES " +
                    "WHERE UNIQUERULE = 'D' " +
                    "AND INDSCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
            while (rs.next()) {
                sqls.add(rs.getString(1));
            }

            rs = s.executeQuery("SELECT 'ALTER TABLE \"' || TRIM(TABSCHEMA) || '\".\"' || TRIM(TABNAME) || '\" DROP FOREIGN KEY \"' || TRIM(CONSTNAME) || '\"' " +
                    "FROM SYSCAT.TABCONST " +
                    "WHERE TYPE = 'F' " +
                    "AND TABSCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
            while (rs.next()) {
                sqls.add(rs.getString(1));
            }

            rs = s.executeQuery("SELECT 'ALTER TABLE \"' || TRIM(TABSCHEMA) || '\".\"' || TRIM(TABNAME) || '\" DROP UNIQUE \"' || TRIM(INDNAME) || '\"' " +
                    "FROM SYSCAT.INDEXES " +
                    "WHERE UNIQUERULE = 'U' " +
                    "AND INDSCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
            while (rs.next()) {
                sqls.add(rs.getString(1));
            }

            rs = s.executeQuery("SELECT 'ALTER TABLE \"' || TRIM(TABSCHEMA) || '\".\"' || TRIM(TABNAME) || '\" DROP PRIMARY KEY' " +
                    "FROM SYSCAT.INDEXES " +
                    "WHERE UNIQUERULE = 'P' " +
                    "AND INDSCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
            while (rs.next()) {
                sqls.add(rs.getString(1));
            }

            rs = s.executeQuery("SELECT 'DROP VIEW \"' || TRIM(TABSCHEMA) || '\".\"' || TRIM(TABNAME) || '\"' " +
                    "FROM SYSCAT.TABLES " +
                    "WHERE TYPE = 'V' " +
                    "AND TABSCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
            while (rs.next()) {
                sqls.add(rs.getString(1));
            }

            rs = s.executeQuery("SELECT 'DROP TABLE \"' || TRIM(TABSCHEMA) || '\".\"' || TRIM(TABNAME) || '\"' " +
                    "FROM SYSCAT.TABLES " +
                    "WHERE TYPE = 'T' " +
                    "AND TABSCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
            while (rs.next()) {
                sqls.add(rs.getString(1));
            }

            rs = s.executeQuery("SELECT 'DROP SEQUENCE \"' || TRIM(SEQSCHEMA) || '\".\"' || TRIM(SEQNAME) || '\"' " +
                    "FROM SYSCAT.SEQUENCES " +
                    "WHERE SEQSCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
            while (rs.next()) {
                sqls.add(rs.getString(1));
            }
            LOG.log(Level.FINEST, "Collect schema objects: END");

            LOG.log(Level.FINEST, "Dropping schema objects: START");
            for (String sql : sqls) {
                try {
                    s.execute(sql);
                } catch (SQLException e) {
                    if (-204 == e.getErrorCode()) {
                        // Apparently we deleted this along with a dependent object since it doesn't exist anymore
                    } else {
                        throw e;
                    }
                }
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
        if (cachedForeignKeys == null) {
            cachedForeignKeys = collectForeignKeys(connection);
        }
        deleteAllData(connection, cachedForeignKeys);
    }

    private Map<String, List<String>> collectForeignKeys(Connection c) {
        try (Statement s = c.createStatement()) {
            // Collect table names for schemas
            LOG.log(Level.FINEST, "Collect table names: START");
            ResultSet rs = s.executeQuery("SELECT TABLE_SCHEMA || '.' || TABLE_NAME FROM SYSIBM.TABLES WHERE TABLE_SCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
            Map<String, List<String>> foreignKeys = new HashMap<>();
            while (rs.next()) {
                foreignKeys.put(rs.getString(1), new ArrayList<String>());
            }
            LOG.log(Level.FINEST, "Collect table names: END");

            // Collect foreign keys for tables
            LOG.log(Level.FINEST, "Collect foreign keys: START");
            ResultSet rs2 = s.executeQuery("SELECT FKTABLE_SCHEM || '.' || FKTABLE_NAME, FK_NAME FROM SYSIBM.SQLFOREIGNKEYS WHERE FKTABLE_SCHEM NOT IN (" + SYSTEM_SCHEMAS + ")");
            while (rs2.next()) {
                foreignKeys.get(rs2.getString(1)).add(rs2.getString(2));
            }
            LOG.log(Level.FINEST, "Collect foreign keys: END");

            return foreignKeys;
        } catch (SQLException e) {
            try {
                c.rollback();
            } catch (SQLException e1) {
                e.addSuppressed(e1);
            }

            throw new RuntimeException(e);
        }
    }

    private void deleteAllData(Connection c, Map<String, List<String>> foreignKeys) {
        try (Statement s = c.createStatement()) {
            // Disable foreign keys
            LOG.log(Level.FINEST, "Disable foreign keys: START");
            for (Map.Entry<String, List<String>> entry : foreignKeys.entrySet()) {
                for (String fk : entry.getValue()) {
                    s.execute("ALTER TABLE " + entry.getKey() + " ALTER FOREIGN KEY " + fk + " NOT ENFORCED");
                }
            }
            c.commit();
            LOG.log(Level.FINEST, "Disable foreign keys: END");

            // Delete data
            LOG.log(Level.FINEST, "Deleting data: START");
            for (String table : foreignKeys.keySet()) {
                if (!ignoredTables.contains(table)) {
                    s.execute("TRUNCATE TABLE " + table + " IMMEDIATE");
                    // DB2 needs a commit after every truncate statement
                    c.commit();
                }
            }
            LOG.log(Level.FINEST, "Deleting data: END");

            // Enable foreign keys
            LOG.log(Level.FINEST, "Enabling foreign keys: START");
            for (Map.Entry<String, List<String>> entry : foreignKeys.entrySet()) {
                for (String fk : entry.getValue()) {
                    s.execute("ALTER TABLE " + entry.getKey() + " ALTER FOREIGN KEY " + fk + " ENFORCED");
                }
            }
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

}
