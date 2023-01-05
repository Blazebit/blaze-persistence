
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

import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.spi.JpqlFunctionGroup;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoPostgreSQL;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.LongType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Entity;
import javax.persistence.Id;
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
        public ExtendedDialect() {
            registerFunction("ADDONE", new SQLFunctionTemplate(LongType.INSTANCE, "?1 + 1"));
            registerFunction("CUSTOMSUM", new SQLFunctionTemplate(LongType.INSTANCE, "SUM(?1)"));
        }
    }

}
