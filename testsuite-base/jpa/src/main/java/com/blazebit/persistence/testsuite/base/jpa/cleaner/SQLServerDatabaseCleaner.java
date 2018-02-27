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
public class SQLServerDatabaseCleaner implements DatabaseCleaner {

    private static final Logger LOG = Logger.getLogger(SQLServerDatabaseCleaner.class.getName());

    private List<String> ignoredTables = new ArrayList<>();
    private List<String> cachedTableNames;

    public static class Factory implements DatabaseCleaner.Factory {

        @Override
        public DatabaseCleaner create() {
            return new SQLServerDatabaseCleaner();
        }

    }

    @Override
    public boolean isApplicable(Connection connection) {
        try {
            return connection.getMetaData().getDatabaseProductName().startsWith("Microsoft SQL Server");
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
            rs = s.executeQuery("SELECT 'ALTER TABLE [' + TABLE_SCHEMA + '].[' + TABLE_NAME + '] DROP CONSTRAINT [' + CONSTRAINT_NAME + ']' FROM INFORMATION_SCHEMA.CONSTRAINT_TABLE_USAGE " +
                    "WHERE EXISTS (SELECT 1 FROM sys.Tables t JOIN sys.Schemas s ON t.schema_id = s.schema_id WHERE t.is_ms_shipped = 0 AND s.name = TABLE_SCHEMA AND t.name = TABLE_NAME) " +
                    "AND EXISTS (SELECT 1 FROM sys.Foreign_keys WHERE name = CONSTRAINT_NAME)");
            while (rs.next()) {
                sqls.add(rs.getString(1));
            }

            rs = s.executeQuery("SELECT 'DROP VIEW [' + TABLE_SCHEMA + '].[' + TABLE_NAME + ']' FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'VIEW' " +
                    "AND EXISTS (SELECT 1 FROM sys.Views t JOIN sys.Schemas s ON t.schema_id = s.schema_id WHERE t.is_ms_shipped = 0 AND s.name = TABLE_SCHEMA AND t.name = TABLE_NAME)");
            while (rs.next()) {
                sqls.add(rs.getString(1));
            }

            rs = s.executeQuery("SELECT 'DROP TABLE [' + TABLE_SCHEMA + '].[' + TABLE_NAME + ']' FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' " +
                    "AND EXISTS (SELECT 1 FROM sys.Tables t JOIN sys.Schemas s ON t.schema_id = s.schema_id WHERE t.is_ms_shipped = 0 AND s.name = TABLE_SCHEMA AND t.name = TABLE_NAME)");
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

            if (cachedTableNames == null) {
                cachedTableNames = new ArrayList<>();
                ResultSet rs = s.executeQuery("SELECT s.name, t.name FROM sys.Tables t JOIN sys.Schemas s ON t.schema_id = s.schema_id WHERE t.is_ms_shipped = 0");
                while (rs.next()) {
                    String tableSchema = rs.getString(1);
                    String tableName = rs.getString(2);
                    if (!ignoredTables.contains(tableName)) {
                        cachedTableNames.add(tableSchema + "." + tableName);
                    }
                }
            }
            // Disable foreign keys
            LOG.log(Level.FINEST, "Disable foreign keys: START");
            for (String table : cachedTableNames) {
                s.execute("ALTER TABLE " + table + " NOCHECK CONSTRAINT ALL");
            }
            LOG.log(Level.FINEST, "Disable foreign keys: END");

            // Delete data
            LOG.log(Level.FINEST, "Deleting data: START");
            for (String table : cachedTableNames) {
                s.execute("DELETE FROM " + table);
            }
            LOG.log(Level.FINEST, "Deleting data: END");

            // Enable foreign keys
            LOG.log(Level.FINEST, "Enabling foreign keys: START");
            for (String table : cachedTableNames) {
                s.execute("ALTER TABLE " + table + " WITH CHECK CHECK CONSTRAINT ALL");
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
