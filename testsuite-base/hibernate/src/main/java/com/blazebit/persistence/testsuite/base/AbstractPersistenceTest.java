/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.testsuite.base;

import org.hibernate.dialect.SQLServer2012Dialect;

import java.util.Properties;


/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public abstract class AbstractPersistenceTest extends AbstractJpaPersistenceTest {

    @Override
    protected Properties applyProperties(Properties properties) {
        if (System.getProperty("hibernate.dialect") != null) {
            properties.put("hibernate.dialect", System.getProperty("hibernate.dialect"));
        } else if (properties.get("javax.persistence.jdbc.url").toString().contains("mysql")) {
            // MySQL is drunk, it does stuff case insensitive by default...
            properties.put("hibernate.dialect", SaneMySQLDialect.class.getName());
            
            // Since MySQL has no sequences, the native strategy is needed for batch inserts
            if (isHibernate5()) {
                properties.put("hibernate.id.new_generator_mappings", "false");
            }
        } else if (properties.get("javax.persistence.jdbc.url").toString().contains("db2")) {
            // The original DB2 dialect misses support for sequence retrieve in select statements
            properties.put("hibernate.dialect", SaneDB2Dialect.class.getName());
        } else if (properties.get("javax.persistence.jdbc.url").toString().contains("h2")) {
            // Hibernate 5 uses sequences by default but h2 seems to have a bug with sequences in a limited query
            if (isHibernate5()) {
                properties.put("hibernate.id.new_generator_mappings", "false");
            }
        } else if (properties.get("javax.persistence.jdbc.url").toString().contains("sqlserver")) {
            // Apparently the dialect resolver doesn't choose the latest dialect
            properties.put("hibernate.dialect", SQLServer2012Dialect.class.getName());
            // Not sure what is happening, but when the sequence is tried to be fetched, it doesn't exist in SQL Server
            if (isHibernate5()) {
                properties.put("hibernate.id.new_generator_mappings", "false");
            }
        }
        
        if (isHibernate42()) {
            properties.put("hibernate.connection.url", properties.get("javax.persistence.jdbc.url"));
            properties.put("hibernate.connection.password", properties.get("javax.persistence.jdbc.password"));
            properties.put("hibernate.connection.username", properties.get("javax.persistence.jdbc.user"));
            properties.put("hibernate.connection.driver_class", properties.get("javax.persistence.jdbc.driver"));
            properties.put("hibernate.hbm2ddl.auto", "create-drop");
        }
        
        // We use the following only for debugging purposes
        // Normally these settings should be disabled since the output would be too big TravisCI
//        properties.put("hibernate.show_sql", "true");
//        properties.put("hibernate.format_sql", "true");
        return properties;
    }

    private boolean isHibernate42() {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        int minor = Integer.parseInt(versionParts[1]);
        return major == 4 && minor == 2;
    }

    private boolean isHibernate5() {
        String version = org.hibernate.Version.getVersionString();
        String[] versionParts = version.split("\\.");
        int major = Integer.parseInt(versionParts[0]);
        return major >= 5;
    }
}
