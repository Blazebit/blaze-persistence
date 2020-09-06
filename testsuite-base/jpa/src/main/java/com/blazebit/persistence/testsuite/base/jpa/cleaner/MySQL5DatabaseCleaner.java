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

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class MySQL5DatabaseCleaner extends AbstractMySQLDatabaseCleaner {

    @Override
    public boolean isApplicable(Connection connection) {
        try {
            return connection.getMetaData().getDatabaseProductName().startsWith("MySQL") && connection.getMetaData().getDatabaseMajorVersion() < 8;
        } catch (SQLException e) {
            throw new RuntimeException("Could not resolve the database metadata!", e);
        }
    }

    @Override
    protected String createClearingStatementForTable(String tableSchema, String tableName) {
        // We do not use TRUNCATE for MySQL 5.7 due to https://bugs.mysql.com/bug.php?id=68184
        return "DELETE FROM " + tableSchema + "." + tableName;
    }

    public static class Factory implements DatabaseCleaner.Factory {

        @Override
        public DatabaseCleaner create() {
            return new MySQL5DatabaseCleaner();
        }

    }
}
