/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.testsuite.base.cleaner;

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
    public void clearData(Connection connection) {
        try (Statement s = connection.createStatement()) {

            if (cachedTableNames == null) {
                cachedTableNames = new ArrayList<>();
                ResultSet rs = s.executeQuery("SELECT s.name, t.name FROM sys.Tables t JOIN sys.Schemas s ON t.schema_id = s.schema_id WHERE t.is_ms_shipped = 0");
                while (rs.next()) {
                    String tableSchema = rs.getString(1);
                    String tableName = rs.getString(2);
                    cachedTableNames.add(tableSchema + "." + tableName);
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
