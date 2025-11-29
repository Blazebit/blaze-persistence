/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.SchemaEntity;
import org.junit.Test;

import jakarta.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.util.Properties;

/**
 * This test is for issue #344
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntitySchemaInTableAnnotationTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[]{
                SchemaEntity.class
        };
    }


    @Override
    protected EntityManagerFactory repopulateSchema() {
        // Skip schema creation since we only want to check if we can build our metamodel properly
        return null;
    }

    @Override
    protected void createSchemaIfNotExists(Connection connection, String schemaName) {
        // Skip schema creation since we only want to check if we can build our metamodel properly
    }

    @Override
    protected String getTargetSchema() {
        return super.getTargetSchema() == null ? null : "test_schema";
    }

    @Test
    public void buildingEntityMetamodelWorksWithSchemaInTableAnnotation() {
        CriteriaBuilder<SchemaEntity> criteria = cbf.create(em, SchemaEntity.class, "d");
        criteria.getQuery();
    }
}
