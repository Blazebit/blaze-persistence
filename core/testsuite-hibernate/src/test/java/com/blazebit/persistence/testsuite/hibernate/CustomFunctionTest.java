
/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite.hibernate;

import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoPostgreSQL;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.query.sqm.produce.function.StandardFunctionReturnTypeResolvers;
import org.hibernate.type.BasicType;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.Properties;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.6.2
 */
public class CustomFunctionTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{ BasicEntity.class };
    }

    @Entity(name = "BasicEntity")
    public static class BasicEntity {
        @Id
        Long id;
    }

    @Test
    @Category({NoMSSQL.class, NoMySQL.class, NoMySQLOld.class, NoPostgreSQL.class, NoOracle.class, NoDB2.class})
    public void implicitJoinCustomAggregateFunction() {
        String queryString = cbf.create(em, BasicEntity.class, "b")
                .select("addone(b.id)")
                .select("customsum(b.id)")
                .select("b.id")
                .getQueryString();

        Assert.assertEquals("SELECT addone(b.id), customsum(b.id), b.id FROM BasicEntity b GROUP BY addone(b.id), b.id", queryString);
    }

    @Override
    protected void configure(CriteriaBuilderConfiguration config) {
        super.configure(config);
        // Mark CUSTOMSUM as aggregate function, even though an implementation is provided through
        config.registerFunction(new JpqlFunctionGroup("CUSTOMSUM", true));
    }

    @Override
    protected Properties applyProperties(Properties properties) {
        super.applyProperties(properties);
        properties.put("hibernate.dialect", ExtendedDialect.class.getName());
        return properties;
    }

    public static class ExtendedDialect extends H2Dialect {
        @Override
        public void initializeFunctionRegistry(FunctionContributions functionContributions) {
            super.initializeFunctionRegistry( functionContributions );
            SqmFunctionRegistry functionRegistry = functionContributions.getFunctionRegistry();
            BasicType<Long> longType = functionContributions.getTypeConfiguration().getBasicTypeForJavaType(Long.class);
            functionRegistry.patternDescriptorBuilder("ADDONE", "?1 + 1")
                    .setReturnTypeResolver(StandardFunctionReturnTypeResolvers.invariant(longType))
                    .register();
            functionRegistry.patternDescriptorBuilder("CUSTOMSUM", "SUM(?1)")
                    .setReturnTypeResolver(StandardFunctionReturnTypeResolvers.invariant(longType))
                    .register();
        }
    }

}
