/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.base;

import com.blazebit.persistence.testsuite.base.jpa.AbstractJpaPersistenceTest;

import java.util.Properties;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class AbstractPersistenceTest extends AbstractJpaPersistenceTest {

    @Override
    protected Properties applyProperties(Properties properties) {
        properties.put("openjpa.RuntimeUnenhancedClasses", "supported");
        properties.put("openjpa.jdbc.SchemaFactory", "native(foreignKeys=true)");
        properties.put("openjpa.Sequence", "native");
        properties.put("openjpa.Log", "DefaultLevel=WARN, Tool=INFO, SQL=TRACE");
        properties.put("openjpa.jdbc.MappingDefaults", "ForeignKeyDeleteAction=restrict,JoinForeignKeyDeleteAction=restrict");
        String dbAction = (String) properties.remove("javax.persistence.schema-generation.database.action");
        if ("drop-and-create".equals(dbAction)) {
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(foreignKeys=true,schemaAction='dropDB,add')");
        } else if ("create".equals(dbAction)) {
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(foreignKeys=true,schemaAction='add')");
        } else if ("drop".equals(dbAction)) {
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(foreignKeys=true,schemaAction='dropDB')");
        } else if (!"none".equals(dbAction)) {
            throw new IllegalArgumentException("Unsupported database action: " + dbAction);
        }
        return properties;
    }

    @Override
    protected boolean needsEntityManagerForDbAction() {
        return true;
    }

    @Override
    protected boolean supportsMapKeyDeReference() {
        return true;
    }

    @Override
    protected boolean supportsInverseSetCorrelationJoinsSubtypesWhenJoined() {
        return true;
    }

    @Override
    protected JpaProviderFamily getJpaProviderFamily() {
        return JpaProviderFamily.OPENJPA;
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
