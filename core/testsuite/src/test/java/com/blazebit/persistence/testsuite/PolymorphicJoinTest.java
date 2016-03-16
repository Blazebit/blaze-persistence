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

package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertEquals;

import javax.persistence.Tuple;

import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.PolymorphicBase;
import com.blazebit.persistence.testsuite.entity.PolymorphicSub1;
import com.blazebit.persistence.testsuite.entity.PolymorphicSub2;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class PolymorphicJoinTest extends AbstractCoreTest {
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            IntIdEntity.class,
            PolymorphicBase.class,
            PolymorphicSub1.class,
            PolymorphicSub2.class
        };
    }
    
    @Test
    public void testJoinSubRelations() {
        CriteriaBuilder<PolymorphicBase> cb = cbf.create(em, PolymorphicBase.class, "base");
        cb.leftJoin("relation1", "rel1");
        cb.leftJoin("relation2", "rel2");
        String expectedQuery = "SELECT base FROM PolymorphicBase base LEFT JOIN base.relation1 rel1 LEFT JOIN base.relation2 rel2";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
    
    @Test
    public void testImplicitJoinSubRelations() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.from(PolymorphicBase.class, "base");
        cb.select("relation1.name");
        cb.select("relation2.name");
        String expectedQuery = "SELECT relation1_1.name, relation2_1.name FROM PolymorphicBase base LEFT JOIN base.relation1 relation1_1 LEFT JOIN base.relation2 relation2_1";
        assertEquals(expectedQuery, cb.getQueryString());
        cb.getResultList();
    }
}
