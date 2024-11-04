/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.testsuite.base.jpa.cleaner;

import com.blazebit.persistence.testsuite.base.jpa.UncheckedSqlException;
import org.apache.hc.core5.net.URIBuilder;

import java.net.URISyntaxException;
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
 * @author Moritz Becker
 * @since 1.5.0
 */
public abstract class AbstractMySQLDatabaseCleaner implements DatabaseCleaner {
    private static final Logger LOG = Logger.getLogger(AbstractMySQLDatabaseCleaner.class.getName());
    private static final String SYSTEM_SCHEMAS = "'information_schema'," +
            "'mysql'," +
            "'sys'," +
            "'performance_schema'";
    private final List<String> ignoredTables = new ArrayList<>();
    private final Map<String, List<String>> clearingSqlsPerSchema = new HashMap<>();

    @Override
    public boolean supportsClearSchema() {
        return true;
    }

    @Override
    public void addIgnoredTable(String tableName) {
        ignoredTables.add(tableName);
    }

    @Override
    public void clearAllSchemas(Connection connection) {
        try {
            clearSchema(connection, connection.getSchema());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearSchema(Connection connection, String schemaName) {
        clearSchema0(connection, schemaName);
    }

    private void clearSchema0(Connection c, String schemaName) {
        try (Statement s = c.createStatement()) {
            // Collect schema names
            LOG.log(Level.FINEST, "Collect table names: START");
            ResultSet rs = s.executeQuery("SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA " + (schemaName == null ? "NOT IN ('information_schema', 'performance_schema', 'mysql', 'sys')" : "= '" + schemaName + "'"));

            StringBuilder sb = new StringBuilder("DROP TABLE ");
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
    public void clearAllData(Connection connection) {
        clearData0(connection, null, s -> {
            try {
                return s.executeQuery("SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA NOT IN (" + SYSTEM_SCHEMAS + ")");
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        });
    }

    @Override
    public void clearData(Connection connection, String schemaName) {
        clearData0(connection, schemaName, s -> {
            try {
                return s.executeQuery("SELECT TABLE_SCHEMA, TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + schemaName + "'");
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        });
    }

    private void clearData0(Connection connection, String schemaName, Function<Statement, ResultSet> tablesProvider) {
        try (Statement s = connection.createStatement()) {
            // Disable foreign keys
            LOG.log(Level.FINEST, "Disable foreign keys: START");
            s.execute("SET FOREIGN_KEY_CHECKS = 0");
            LOG.log(Level.FINEST, "Disable foreign keys: END");

            // Delete data
            LOG.log(Level.FINEST, "Deleting data: START");
            List<String> clearingSqls = clearingSqlsPerSchema.get(schemaName);
            if (clearingSqls == null) {
                clearingSqls = new ArrayList<>();
                ResultSet rs = tablesProvider.apply(s);
                while (rs.next()) {
                    String tableSchema = rs.getString(1);
                    String tableName = rs.getString(2);
                    if (!ignoredTables.contains(tableName)) {
                        clearingSqls.add(createClearingStatementForTable(tableSchema, tableName));
                    }
                }
                clearingSqlsPerSchema.put(schemaName, clearingSqls);
            }
            for (String clearingSql : clearingSqls) {
                s.execute(clearingSql);
            }
            LOG.log(Level.FINEST, "Deleting data: END");

            // Enable foreign keys
            LOG.log(Level.FINEST, "Enabling foreign keys: START");
            s.execute("SET FOREIGN_KEY_CHECKS = 1");
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

    protected abstract String createClearingStatementForTable(String tableSchema, String tableName);

    @Override
    public void createDatabaseIfNotExists(Connection connection, String databaseName) {
        try (Statement s = connection.createStatement()) {
            LOG.log(Level.FINEST, "Create schema: START");
            s.execute("CREATE DATABASE IF NOT EXISTS " + databaseName);
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
    public void createSchemaIfNotExists(Connection connection, String schemaName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyTargetDatabasePropertyModifications(Map<Object, Object> properties, String databaseName) {
        String jdbcUrl = (String) properties.get("jakarta.persistence.jdbc.url");
        try {
            jdbcUrl = "jdbc:" + new URIBuilder(jdbcUrl.substring(jdbcUrl.indexOf(':') + 1))
                    .setPath(databaseName)
                    .toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException();
        }
        properties.put("jakarta.persistence.jdbc.url", jdbcUrl);
    }

    @Override
    public void applyTargetSchemaPropertyModifications(Map<Object, Object> properties, String schemaName) {
        throw new UnsupportedOperationException();
    }
}
