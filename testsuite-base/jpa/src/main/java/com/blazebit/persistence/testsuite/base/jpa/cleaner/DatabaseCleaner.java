/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base.jpa.cleaner;

import java.sql.Connection;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface DatabaseCleaner {

    public static interface Factory {

        public DatabaseCleaner create();

    }

    public void addIgnoredTable(String tableName);

    public boolean isApplicable(Connection connection);

    public boolean supportsClearSchema();

    public void clearAllSchemas(Connection connection);

    public void clearSchema(Connection connection, String schemaName);

    public void clearAllData(Connection connection);

    public void clearData(Connection connection, String schemaName);

    public void createDatabaseIfNotExists(Connection connection, String databaseName);

    public void createSchemaIfNotExists(Connection connection, String schemaName);

    public void applyTargetDatabasePropertyModifications(Map<Object, Object> properties, String databaseName);

    public void applyTargetSchemaPropertyModifications(Map<Object, Object> properties, String schemaName);
}
