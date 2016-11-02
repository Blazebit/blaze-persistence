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

package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertEquals;

import com.blazebit.persistence.testsuite.base.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.category.NoHibernate43;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.PolymorphicBase;
import com.blazebit.persistence.testsuite.entity.PolymorphicPropertyBase;
import com.blazebit.persistence.testsuite.entity.PolymorphicPropertySub1;
import com.blazebit.persistence.testsuite.entity.PolymorphicPropertySub2;
import com.blazebit.persistence.testsuite.entity.PolymorphicSub1;
import com.blazebit.persistence.testsuite.entity.PolymorphicSub2;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class PolymorphicPropertyTest extends AbstractCoreTest {
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            IntIdEntity.class,
            PolymorphicPropertyBase.class,
            PolymorphicPropertySub1.class,
            PolymorphicPropertySub2.class,
            PolymorphicBase.class,
            PolymorphicSub1.class,
            PolymorphicSub2.class
        };
    }
    
    @Test
    // NOTE: Datanucleus5 reports: org.datanucleus.store.rdbms.sql.expression.TypeConverterLiteral cannot be cast to org.datanucleus.store.rdbms.sql.expression.StringLiteral
    // TODO: Actually this kind of query is dangerous because hibernate chooses one subtype of PolymorphicPropertyBase and goes on with that assumption instead of searching for the subtype that fits
    @Category({ NoDatanucleus.class, NoHibernate43.class})
    public void testSelectSubProperty() {
        // TODO: Maybe this test should be a negative test, as a usage like this should not be supported but only by using treat
        CriteriaBuilder<PolymorphicPropertyBase> cb = cbf.create(em, PolymorphicPropertyBase.class, "propBase");
        cb.select("propBase.base.relation1");
        cb.where("TYPE(base)").eq(PolymorphicSub1.class);
        String expectedQuery = "SELECT relation1_1 FROM PolymorphicPropertyBase propBase LEFT JOIN propBase.base base_1 LEFT JOIN base_1.relation1 relation1_1 WHERE TYPE(base_1) = :param_0";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }

    @Test
    // NOTE: Datanucleus4 reports: We do not currently support JOIN to TREAT
    // NOTE: Datanucleus5 reports: org.datanucleus.store.rdbms.sql.expression.TypeConverterLiteral cannot be cast to org.datanucleus.store.rdbms.sql.expression.StringLiteral
    // TODO: Actually this kind of query is dangerous because hibernate chooses one subtype of PolymorphicPropertyBase and goes on with that assumption instead of searching for the subtype that fits
    @Category({ NoDatanucleus4.class, NoDatanucleus.class, NoHibernate43.class })
    public void testSelectSubPropertyWithTreat() {
        CriteriaBuilder<PolymorphicPropertyBase> cb = cbf.create(em, PolymorphicPropertyBase.class, "propBase");
        cb.select("relation1");
        cb.leftJoin("TREAT(propBase.base AS PolymorphicSub1)", "base1");
        cb.leftJoin("base1.relation1", "relation1");
        cb.where("TYPE(base1)").eq(PolymorphicSub1.class);
        String expectedQuery = "SELECT relation1 FROM PolymorphicPropertyBase propBase LEFT JOIN " + treatJoin("propBase.base", PolymorphicSub1.class) + " base1 LEFT JOIN base1.relation1 relation1 WHERE TYPE(base1) = :param_0";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
}
