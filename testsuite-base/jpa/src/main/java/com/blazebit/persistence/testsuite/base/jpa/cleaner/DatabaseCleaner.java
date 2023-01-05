/*
 * Copyright 2014 - 2023 Blazebit.
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
