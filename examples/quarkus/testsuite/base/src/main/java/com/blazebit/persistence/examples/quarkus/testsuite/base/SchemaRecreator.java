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
package com.blazebit.persistence.examples.quarkus.testsuite.base;

import com.blazebit.persistence.examples.quarkus.base.entity.Document;
import com.blazebit.persistence.examples.quarkus.base.entity.Person;
import com.blazebit.persistence.testsuite.base.jpa.AbstractJpaPersistenceTest;
import com.blazebit.persistence.testsuite.base.jpa.cleaner.DatabaseCleaner;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.persistence.EntityManager;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class SchemaRecreator extends AbstractJpaPersistenceTest {

    public static void main(String[] args) {
        new SchemaRecreator().recreateSchema();
    }

    private void recreateSchema() {
        Map<Object, Object> properties = new HashMap<>(createProperties(""));
        DataSource dataSource = getDataSource(properties);
        try (Connection connection = dataSource.getConnection()) {
            getDatabaseCleaner(connection).clearSchema(connection);
            createSchema();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearSchema(EntityManager em, DatabaseCleaner databaseCleaner) {
        DataSource dataSource = getDataSource(createProperties(""));
        try (Connection connection = dataSource.getConnection()) {
            getDatabaseCleaner(connection).clearSchema(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Properties createProperties(String dbAction) {
        String jdbcUrl = ConfigProvider.getConfig().getValue("quarkus.datasource.url", String.class);
        String jdbcUser = ConfigProvider.getConfig().getValue("quarkus.datasource.username", String.class);
        Optional<String> jdbcPassword = ConfigProvider.getConfig().getOptionalValue("quarkus.datasource.password", String.class);
        String jdbcDriver = ConfigProvider.getConfig().getValue("quarkus.datasource.driver", String.class);

        Properties properties = new Properties();
        properties.put("javax.persistence.jdbc.url", jdbcUrl);
        properties.put("javax.persistence.jdbc.user", jdbcUser);
        if (jdbcPassword.isPresent()) {
            properties.put("javax.persistence.jdbc.password", jdbcPassword.get());
        }
        properties.put("javax.persistence.jdbc.driver", jdbcDriver);
        properties.put("javax.persistence.sharedCache.mode", "NONE");
        properties.put("javax.persistence.schema-generation.database.action", dbAction);
        properties = applyProperties(properties);
        return properties;
    }

    @Override
    protected void createSchema() {
//        Properties props = createProperties("");
//        MutablePersistenceUnitInfo persistenceUnitInfo = new MutablePersistenceUnitInfo();
//        persistenceUnitInfo.setPersistenceUnitName("default");
//        persistenceUnitInfo.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
//        persistenceUnitInfo.setNonJtaDataSource(getDataSource(props));
//        persistenceUnitInfo.setExcludeUnlistedClasses(true);
//
//        try {
//            URL url = AbstractJpaPersistenceTest.class.getClassLoader()
//                .getResource("");
//            persistenceUnitInfo.setPersistenceUnitRootUrl(url);
//        } catch (Exception ex) {
//            throw new RuntimeException(ex);
//        }
//
//        for (Class<?> clazz : getEntityClasses()) {
//            persistenceUnitInfo.addManagedClassName(clazz.getName());
//        }

        PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();
        List<PersistenceProvider> providers = resolver.getPersistenceProviders();
        providers.get(0).generateSchema("default", null);
    }

    @Override
    protected boolean supportsMapKeyDeReference() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean supportsInverseSetCorrelationJoinsSubtypesWhenJoined() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[] { Document.class, Person.class };
    }
}
