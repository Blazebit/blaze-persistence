/*
 * Copyright 2014 - 2020 Blazebit.
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
public class OracleDatabaseCleaner implements DatabaseCleaner {

    private static final Logger LOG = Logger.getLogger(OracleDatabaseCleaner.class.getName());
    private static final String SYSTEM_SEQUENCE_OWNERS = "'SYS'," +
            "'CTXSYS'," +
            "'DVSYS'," +
            "'OJVMSYS'," +
            "'ORDDATA'," +
            "'MDSYS'," +
            "'OLAPSYS'," +
            "'LBACSYS'," +
            "'XDB'," +
            "'WMSYS'";

    private final List<String> ignoredTables = new ArrayList<>();
    private final Map<String, List<String>> cachedTruncateTableSqlPerSchema = new HashMap<>();
    private final Map<String, List<String>> cachedConstraintDisableSqlPerSchema = new HashMap<>();
    private final Map<String, List<String>> cachedConstraintEnableSqlPerSchema = new HashMap<>();

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
    public void clearAllSchemas(Connection connection) {
        clearSchema0(connection, s -> {
            try {
                return s.executeQuery("SELECT 'DROP TABLE ' || owner || '.' || table_name || ' CASCADE CONSTRAINTS' " +
                        "FROM all_tables " +
                        // Exclude the tables owner by sys
                        "WHERE owner NOT IN ('SYS')" +
                        // Normally, user tables aren't in sysaux
                        "      AND tablespace_name NOT IN ('SYSAUX')" +
                        // Apparently, user tables have global stats off
                        "      AND global_stats = 'NO'" +
                        // Exclude the tables with names starting like 'DEF$_'
                        "      AND table_name NOT LIKE 'DEF$\\_%' ESCAPE '\\'" +
                        " UNION ALL " +
                        "SELECT 'DROP SEQUENCE ' || sequence_owner || '.' || sequence_name FROM all_sequences WHERE sequence_owner NOT IN (" + SYSTEM_SEQUENCE_OWNERS + ")");
            } catch (SQLException sqlException) {
                throw new UncheckedSqlException(sqlException);
            }
        });
    }

    @Override
    public void clearSchema(Connection c, String schemaName) {
        clearSchema0(c, s -> {
            try {
                return s.executeQuery("SELECT 'DROP TABLE ' || owner || '.' || table_name || ' CASCADE CONSTRAINTS' " +
                        "FROM all_tables " +
                        "WHERE owner = '" + schemaName + "'" +
                        // Normally, user tables aren't in sysaux
                        "      AND tablespace_name NOT IN ('SYSAUX')" +
                        // Apparently, user tables have global stats off
                        "      AND global_stats = 'NO'" +
                        // Exclude the tables with names starting like 'DEF$_'
                        "      AND table_name NOT LIKE 'DEF$\\_%' ESCAPE '\\'" +
                        " UNION ALL " +
                        "SELECT 'DROP SEQUENCE ' || sequence_owner || '.' || sequence_name FROM all_sequences WHERE sequence_owner = '" + schemaName + "'");
            } catch (SQLException sqlException) {
                throw new UncheckedSqlException(sqlException);
            }
        });
    }

    private void clearSchema0(Connection c, Function<Statement, ResultSet> sqlProvider) {
        try (Statement s = c.createStatement()) {
            ResultSet rs;
            List<String> sqls = new ArrayList<>();

            // Collect schema objects
            LOG.log(Level.FINEST, "Collect schema objects: START");
            rs = sqlProvider.apply(s);
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
        } catch (SQLException | UncheckedSqlException e) {
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
                return s.executeQuery(
                        "SELECT tbl.owner || '.' || tbl.table_name, c.constraint_name FROM (" +
                                "SELECT owner, table_name " +
                                "FROM all_tables " +
                                // Exclude the tables owner by sys
                                "WHERE owner NOT IN ('SYS')" +
                                // Normally, user tables aren't in sysaux
                                "      AND tablespace_name NOT IN ('SYSAUX')" +
                                // Apparently, user tables have global stats off
                                "      AND global_stats = 'NO'" +
                                // Exclude the tables with names starting like 'DEF$_'
                                "      AND table_name NOT LIKE 'DEF$\\_%' ESCAPE '\\'" +
                                ") tbl LEFT JOIN all_constraints c ON tbl.owner = c.owner AND tbl.table_name = c.table_name AND constraint_type = 'R'");
            } catch (SQLException sqlException) {
                throw new UncheckedSqlException(sqlException);
            }
        });
    }

    @Override
    public void clearData(Connection connection, String schemaName) {
        clearData0(connection, schemaName, s -> {
            try {
                return s.executeQuery("SELECT tbl.owner || '.' || tbl.table_name, c.constraint_name FROM (" +
                                "SELECT owner, table_name " +
                                "FROM all_tables " +
                                "WHERE owner = '" + schemaName + "'" +
                                // Normally, user tables aren't in sysaux
                                "      AND tablespace_name NOT IN ('SYSAUX')" +
                                // Apparently, user tables have global stats off
                                "      AND global_stats = 'NO'" +
                                // Exclude the tables with names starting like 'DEF$_'
                                "      AND table_name NOT LIKE 'DEF$\\_%' ESCAPE '\\'" +
                                ") tbl LEFT JOIN all_constraints c ON tbl.owner = c.owner AND tbl.table_name = c.table_name AND constraint_type = 'R'");
            } catch (SQLException sqlException) {
                throw new UncheckedSqlException(sqlException);
            }
        });
    }

    private void clearData0(Connection connection, String schemaName, Function<Statement, ResultSet> tablesProvider) {
        try (Statement s = connection.createStatement()) {
            List<String> cachedTruncateTableSql = cachedTruncateTableSqlPerSchema.get(schemaName);
            List<String> cachedConstraintDisableSql = cachedConstraintDisableSqlPerSchema.get(schemaName);
            List<String> cachedConstraintEnableSql = cachedConstraintEnableSqlPerSchema.get(schemaName);
            if (cachedTruncateTableSql == null) {
                cachedTruncateTableSql = new ArrayList<>();
                cachedConstraintDisableSql = new ArrayList<>();
                cachedConstraintEnableSql = new ArrayList<>();
                ResultSet rs = tablesProvider.apply(s);
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
                cachedTruncateTableSqlPerSchema.put(schemaName, cachedTruncateTableSql);
                cachedConstraintDisableSqlPerSchema.put(schemaName, cachedConstraintDisableSql);
                cachedConstraintEnableSqlPerSchema.put(schemaName, cachedConstraintEnableSql);
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
            LOG.log(Level.FINEST, "Check if schema exists: START");
            ResultSet schemas = s.executeQuery("SELECT 1 FROM SYS.dba_users WHERE username = '" + schemaName + "'");
            LOG.log(Level.FINEST, "Check if schema exists: END");
            if (!schemas.next()) {
                LOG.log(Level.FINEST, "Create user: START");
                s.execute("CREATE USER " + schemaName + " IDENTIFIED BY " + schemaName + " QUOTA UNLIMITED ON USERS");
                LOG.log(Level.FINEST, "Create user: END");
                LOG.log(Level.FINEST, "Grant user privileges: START");
                s.execute("GRANT create session TO " + schemaName);
                s.execute("GRANT create table TO " + schemaName);
                s.execute("GRANT create view TO " + schemaName);
                s.execute("GRANT create any trigger TO " + schemaName);
                s.execute("GRANT create any procedure TO " + schemaName);
                s.execute("GRANT create sequence TO " + schemaName);
                s.execute("GRANT create synonym TO " + schemaName);
                s.execute("GRANT select any dictionary TO " + schemaName);
                LOG.log(Level.FINEST, "Grant user privileges: END");
            }
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void applyTargetSchemaPropertyModifications(Map<Object, Object> properties, String schemaName) {
        properties.put("javax.persistence.jdbc.user", schemaName);
        properties.put("javax.persistence.jdbc.password", schemaName);
    }
}
