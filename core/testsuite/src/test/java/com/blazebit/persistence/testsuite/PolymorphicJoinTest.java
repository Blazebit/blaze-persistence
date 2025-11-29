/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertTrue;

import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import jakarta.persistence.Tuple;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate62;
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
@Category(NoHibernate.class)
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
