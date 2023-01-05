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

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.SchemaEntity;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
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
