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
package com.blazebit.persistence.testsuite.base.jpa;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class SchemaClearer extends AbstractJpaPersistenceTest {

    private static final Logger LOG = Logger.getLogger(SchemaClearer.class.getName());

    public static void main(String[] args) throws IOException {
        new SchemaClearer().clearSchema();
    }

    private void clearSchema() throws IOException {
        String configDirectory = System.getProperty("configDirectory");
        Map<Object, Object> properties = new HashMap<>();
        Path configFile;
        String schema;
        if (configDirectory == null || configDirectory.isEmpty() || !Files.exists(configFile = Paths.get(configDirectory, "application.properties"))) {
            String jdbcUrl = System.getProperty("jdbc.url");
            String jdbcUser = System.getProperty("jdbc.user");
            String jdbcPassword = System.getProperty("jdbc.password");
            String jdbcDriver = System.getProperty("jdbc.driver");
            schema = System.getProperty("jdbc.schema");

            if (jdbcUrl == null || jdbcUrl.isEmpty() || jdbcDriver == null || jdbcDriver.isEmpty()) {
                LOG.warning("Skipping schema clearing because no configuration could be found!");
                return;
            }
            properties.put("javax.persistence.jdbc.url", jdbcUrl);
            properties.put("javax.persistence.jdbc.user", jdbcUser);
            properties.put("javax.persistence.jdbc.password", jdbcPassword);
            properties.put("javax.persistence.jdbc.driver", jdbcDriver);
        } else {
            Properties p = new Properties();
            try (InputStream stream = Files.newInputStream(configFile)) {
                p.load(stream);
            }

            schema = p.getProperty("quarkus.hibernate-orm.database.default-schema");

            properties.put("javax.persistence.jdbc.url", p.getProperty("quarkus.datasource.url"));
            properties.put("javax.persistence.jdbc.user", p.getProperty("quarkus.datasource.username"));
            properties.put("javax.persistence.jdbc.password", p.getProperty("quarkus.datasource.password"));
            properties.put("javax.persistence.jdbc.driver", p.getProperty("quarkus.datasource.driver"));
        }
        DataSource dataSource = createDataSource(properties, null);
        try (Connection connection = dataSource.getConnection()) {
            if (schema == null) {
                getDatabaseCleaner(connection).clearAllSchemas(connection);
            } else {
                getDatabaseCleaner(connection).clearSchema(connection, schema);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean supportsMapKeyDeReference() {
        return false;
    }

    @Override
    protected boolean supportsInverseSetCorrelationJoinsSubtypesWhenJoined() {
        return false;
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[0];
    }

    @Override
    protected JpaProviderFamily getJpaProviderFamily() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int getJpaProviderMajorVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int getJpaProviderMinorVersion() {
        throw new UnsupportedOperationException();
    }
}
