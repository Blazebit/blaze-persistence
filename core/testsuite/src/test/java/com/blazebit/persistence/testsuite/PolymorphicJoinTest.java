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

import static org.junit.Assert.assertTrue;

import javax.persistence.Tuple;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate60;
import com.blazebit.persistence.testsuite.entity.PolymorphicBaseContainer;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.PolymorphicBase;
import com.blazebit.persistence.testsuite.entity.PolymorphicSub1;
import com.blazebit.persistence.testsuite.entity.PolymorphicSub2;
import org.junit.experimental.categories.Category;

/**
 * Negative test that asserts that the dangerous implicit downcast is not supported.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
// NOTE: Hibernate 6.0 supports this when only a single subtype contains the attribute
@Category(NoHibernate60.class)
public class PolymorphicJoinTest extends AbstractCoreTest {
    
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
            IntIdEntity.class,
            PolymorphicBase.class,
            PolymorphicSub1.class,
            PolymorphicSub2.class,
            PolymorphicBaseContainer.class
        };
    }
    
    @Test
    @Category(NoEclipselink.class)
    // Eclipselink does not support polymorphic queries
    public void testJoinSubRelations() {
        CriteriaBuilder<PolymorphicBase> cb = cbf.create(em, PolymorphicBase.class, "base");
        IllegalArgumentException e = verifyException(cb, IllegalArgumentException.class, r -> r.leftJoin("relation1", "rel1"));

        String message = e.getMessage();
        assertTrue(message.contains("relation1"));
        assertTrue(message.contains(PolymorphicBase.class.getSimpleName()));
    }
    
    @Test
    @Category(NoEclipselink.class)
    // Eclipselink does not support polymorphic queries
    public void testImplicitJoinSubRelations() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.from(PolymorphicBase.class, "base");
        cb.select("relation1.name");
        cb.select("relation2.name");

        IllegalArgumentException e = verifyException(cb, IllegalArgumentException.class, r -> r.getQueryString());
        String message = e.getMessage();
        assertTrue(message.contains("relation1"));
        assertTrue(message.contains(PolymorphicBase.class.getSimpleName()));
    }
}
