/*
 * Copyright 2014 - 2022 Blazebit.
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

import com.blazebit.persistence.testsuite.base.jpa.UncheckedSqlException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class H2DatabaseCleaner implements DatabaseCleaner {

    private static final Logger LOG = Logger.getLogger(H2DatabaseCleaner.class.getName());
    private static final String SYSTEM_SCHEMAS = "'INFORMATION_SCHEMA'";

    private final List<String> ignoredTables = new ArrayList<>();
    private final Map<String, List<String>> cachedTableNamesPerSchema = new HashMap<>();

    public static class Factory implements DatabaseCleaner.Factory {

        @Override
        public DatabaseCleaner create() {
            return new H2DatabaseCleaner();
        }

    }

    @Override
    public boolean isApplicable(Connection connection) {
        try {
            return connection.getMetaData().getDatabaseProductName().startsWith("H2");
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
    public void clearAllSchemas(Connection c) {
        try (Statement s = c.createStatement()) {
            LOG.log(Level.FINEST, "Dropping schema objects: START");
            s.execute("DROP ALL OBJECTS");
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
    public void clearSchema(Connection c, String schemaName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearAllData(Connection connection) {
        clearData0(connection, null, s -> {
            try {
                return s.executeQuery("SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
            } catch (SQLException sqlException) {
                throw new UncheckedSqlException(sqlException);
            }
        });
    }

    @Override
    public void clearData(Connection connection, String schemaName) {
        clearData0(connection, schemaName, s -> {
            try {
                return s.executeQuery("SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + schemaName + "'");
            } catch (SQLException sqlException) {
                throw new UncheckedSqlException(sqlException);
            }
        });
    }

    private void clearData0(Connection connection, String schemaName, Function<Statement, ResultSet> tablesProvider) {
        try (Statement s = connection.createStatement()) {
            // Disable foreign keys
            LOG.log(Level.FINEST, "Disable foreign keys: START");
            s.execute("SET REFERENTIAL_INTEGRITY FALSE");
            LOG.log(Level.FINEST, "Disable foreign keys: END");

            // Delete data
            LOG.log(Level.FINEST, "Deleting data: START");
            List<String> cachedTableNames = cachedTableNamesPerSchema.get(schemaName);
            if (cachedTableNames == null) {
                cachedTableNames = new ArrayList<>();
                ResultSet rs = tablesProvider.apply(s);
                while (rs.next()) {
                    String tableSchema = rs.getString(1);
                    String tableName = rs.getString(2);
                    if (!ignoredTables.contains(tableName)) {
                        cachedTableNames.add(tableSchema + "." + tableName);
                    }
                }
                cachedTableNamesPerSchema.put(schemaName, cachedTableNames);
            }
            for (String table : cachedTableNames) {
                s.execute("TRUNCATE TABLE " + table);
            }
            LOG.log(Level.FINEST, "Deleting data: END");

            // Enable foreign keys
            LOG.log(Level.FINEST, "Enabling foreign keys: START");
            s.execute("SET REFERENTIAL_INTEGRITY TRUE");
            LOG.log(Level.FINEST, "Enabling foreign keys: END");

            LOG.log(Level.FINEST, "Committing: START");
            connection.commit();
            LOG.log(Level.FINEST, "Committing: END");
        } catch (SQLException | UncheckedSqlException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e.addSuppressed(e1);
            }

            throw new RuntimeException(e);
        }
    }

    @Override
    public void createDatabaseIfNotExists(Connection connection, String databaseName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void createSchemaIfNotExists(Connection connection, String schemaName) {
        try (Statement s = connection.createStatement()) {
            LOG.log(Level.FINEST, "Create schema: START");
            s.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            LOG.log(Level.FINEST, "Create schema: END");
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e.addSuppressed(e1);
            }

            throw new RuntimeException(e);
        }
    }

    @Override
    public void applyTargetDatabasePropertyModifications(Map<Object, Object> properties, String databaseName) {
        // do nothing
    }

    @Override
    public void applyTargetSchemaPropertyModifications(Map<Object, Object> properties, String schemaName) {
        // do nothing
    }
}
