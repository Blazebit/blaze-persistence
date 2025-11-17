/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate62;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.PolymorphicBase;
import com.blazebit.persistence.testsuite.entity.PolymorphicBaseContainer;
import com.blazebit.persistence.testsuite.entity.PolymorphicPropertyBase;
import com.blazebit.persistence.testsuite.entity.PolymorphicPropertySub1;
import com.blazebit.persistence.testsuite.entity.PolymorphicPropertySub2;
import com.blazebit.persistence.testsuite.entity.PolymorphicSub1;
import com.blazebit.persistence.testsuite.entity.PolymorphicSub2;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

/**
 * Negative tests to assert implicit downcast is not supported!
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
// NOTE: Hibernate 6.0 supports this when only a single subtype contains the attribute
@Category(NoHibernate.class)
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
            PolymorphicSub2.class,
            PolymorphicBaseContainer.class
        };
    }
    
    @Test
    public void testSelectSubProperty() {
        CriteriaBuilder<PolymorphicPropertyBase> cb = cbf.create(em, PolymorphicPropertyBase.class, "propBase");
        cb.select("propBase.base.relation1");
        cb.where("TYPE(base)").eq(PolymorphicSub1.class);

        IllegalArgumentException e = verifyException(cb, IllegalArgumentException.class, r -> r.getQueryString());
        String message = e.getMessage();
        assertTrue(message.contains("base"));
        assertTrue(message.contains(PolymorphicPropertyBase.class.getSimpleName()));
    }

    @Test
    public void testSelectSubPropertyWithTreat() {
        CriteriaBuilder<PolymorphicPropertyBase> cb = cbf.create(em, PolymorphicPropertyBase.class, "propBase");
        cb.select("relation1");
        IllegalArgumentException e = verifyException(cb, IllegalArgumentException.class, r -> r.leftJoin("TREAT(propBase.base AS PolymorphicSub1)", "base1"));
        String message = e.getMessage();
        assertTrue(message.contains("base"));
        assertTrue(message.contains(PolymorphicPropertyBase.class.getSimpleName()));
    }
}
