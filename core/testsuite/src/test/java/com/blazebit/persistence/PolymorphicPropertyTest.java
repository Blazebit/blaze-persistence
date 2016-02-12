/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.blazebit.persistence.entity.IntIdEntity;
import com.blazebit.persistence.entity.PolymorphicBase;
import com.blazebit.persistence.entity.PolymorphicPropertyBase;
import com.blazebit.persistence.entity.PolymorphicPropertySub1;
import com.blazebit.persistence.entity.PolymorphicPropertySub2;
import com.blazebit.persistence.entity.PolymorphicSub1;
import com.blazebit.persistence.entity.PolymorphicSub2;

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
    @Ignore("Actually this kind of query is dangerous because hibernate chooses one subtype of PolymorphicPropertyBase and goes on with that assumption instead of searching for the subtype that fits")
    public void testSelectSubProperty() {
        CriteriaBuilder<PolymorphicPropertyBase> cb = cbf.create(em, PolymorphicPropertyBase.class, "propBase");
        cb.select("propBase.base.relation1");
        cb.where("TYPE(base)").eq(PolymorphicSub1.class);
        String expectedQuery = "SELECT relation1_1 FROM PolymorphicPropertyBase propBase LEFT JOIN propBase.base base_1 LEFT JOIN base_1.relation1 relation1_1 WHERE TYPE(base_1) = :param_0";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    // NOTE: This is JPA 2.1 specific
    // TODO: implement treat support
    @Test
    @Ignore("Treat support is not yet implemented. Also note that hibernate does not fully support TREAT: https://hibernate.atlassian.net/browse/HHH-9345")
    public void testSelectSubPropertyWithTreat() {
//        CriteriaBuilder<PolymorphicPropertyBase> cb = cbf.create(em, PolymorphicPropertyBase.class, "propBase");
//        cb.select("TREAT(propBase.base AS PolymorphicSub1).relation1");
//        cb.where("TYPE(base)").eq(PolymorphicSub1.class);
        String expectedQuery = "SELECT relation1_1 FROM PolymorphicPropertyBase propBase LEFT JOIN TREAT(propBase.base AS PolymorphicSub1) base_1 LEFT JOIN base_1.relation1 relation1_1 WHERE TYPE(base_1) = :param_0";
//        assertEquals(expectedQuery, cb.getQueryString());
//        cb.getResultList();
        em.createQuery(expectedQuery).getResultList();
    }
}
