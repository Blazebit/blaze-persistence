/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
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
import java.lang.reflect.InvocationTargetException;

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
    protected void configure(CriteriaBuilderConfiguration config) {
        config.registerFunction(new JpqlFunctionGroup("array_contains", new ArrayContainsFunction()));
        super.configure(config);
    }

    // NOTE: this API was only introduced in Hibernate 5
    @Test
    @Category({ NoHibernate42.class, NoHibernate43.class, NoDB2.class, NoOracle.class, NoMySQL.class, NoMSSQL.class, NoMySQLOld.class, NoH2.class })
    public void testHibernateCustomTypedParameterValue() {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                cbf.create(em, EntityWithCustomType.class, "a")
                        .where("array_contains(a.sensorNames, :elements)").eqLiteral(true)
                        .setParameter("elements", getTypedParameterValue(StringArrayType.INSTANCE, new String[] { "Temperature"}))
                        .getSingleResult();
            }
        });
    }

    Object getTypedParameterValue(org.hibernate.type.Type type, Object value) {
        try {
            return Class.forName("org.hibernate.jpa.TypedParameterValue").getConstructor(org.hibernate.type.Type.class, Object.class).newInstance(type, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
