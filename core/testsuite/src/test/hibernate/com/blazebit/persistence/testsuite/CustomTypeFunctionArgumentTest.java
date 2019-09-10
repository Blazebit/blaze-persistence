/*
 * Copyright 2014 - 2019 Blazebit.
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

import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.jpa.TypedParameterValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class CustomTypeFunctionArgumentTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] { EntityWithCustomType.class };
    }

    @Before
    public void setUp() throws Exception {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                EntityWithCustomType entityWithCustomType = new EntityWithCustomType();
                entityWithCustomType.sensorNames = new String[] {
                        "Temperature",
                        "Pressure"
                };
                em.persist(entityWithCustomType);
                em.flush();
                em.clear();
            }
        });
    }

    @Override
    protected CriteriaBuilderConfiguration configure(CriteriaBuilderConfiguration config) {
        config.registerFunction(new JpqlFunctionGroup("array_contains", new ArrayContainsFunction()));
        return super.configure(config);
    }

    @Test
    @Category({NoDB2.class, NoOracle.class, NoMySQL.class, NoMSSQL.class, NoMySQLOld.class, NoH2.class})
    public void testHibernateCustomTypedParameterValue() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                cbf.create(em, EntityWithCustomType.class, "a")
                        .where("array_contains(a.sensorNames, :elements)").eqLiteral(true)
                        .setParameter("elements", new TypedParameterValue(StringArrayType.INSTANCE, new String[] { "Temperature"}))
                        .getSingleResult();
            }
        });
    }

    @Entity
    @Table(name = "ewct")
    @TypeDef(
        name = "string-array",
        typeClass = StringArrayType.class
    )
    public static class EntityWithCustomType {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private Long id;

        @Type(type = "string-array")
        @Column(
            name = "sensor_names",
            columnDefinition = "text[]"
        )
        private String[] sensorNames;

    }

    public static class ArrayContainsFunction implements JpqlFunction {

        @Override
        public boolean hasArguments() {
            return true;
        }

        @Override
        public boolean hasParenthesesIfNoArguments() {
            return false;
        }

        @Override
        public Class<?> getReturnType(Class<?> firstArgumentType) {
            return Boolean.class;
        }

        @Override
        public void render(FunctionRenderContext context) {
            context.addArgument(0);
            context.addChunk(" @> ");
            context.addArgument(1);
        }
    }

}
