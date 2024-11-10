/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Parent;
import com.blazebit.persistence.testsuite.entity.Sub1;
import com.blazebit.persistence.testsuite.entity.Sub1Sub1;
import com.blazebit.persistence.testsuite.entity.Sub1Sub2;
import com.blazebit.persistence.testsuite.entity.Sub2;
import com.blazebit.persistence.testsuite.entity.Sub2Sub1;
import com.blazebit.persistence.testsuite.entity.Sub2Sub2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class TreatMultiLevelTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                Parent.class,
                Sub1.class,
                Sub2.class,
                Sub1Sub1.class,
                Sub1Sub2.class,
                Sub2Sub1.class,
                Sub2Sub2.class
        };
    }

    @Test
    public void implicitJoinTreatedRoot() {
        CriteriaBuilder<Integer> criteria = cbf.create(em, Integer.class);
        criteria.from(Parent.class, "p");
        criteria.select("TREAT(p AS Sub1).number");
        assertEquals("SELECT " + treatRoot("p", Sub1.class, "number") + " FROM Parent p", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: EclipseLink is just fundamentally broken in this regard...
    @Category({ NoEclipselink.class })
    public void multiTreatWithDiscriminatorColumn() {
        // Given
        Sub1 sub1 = new Sub1();
        sub1.setSub1Value(11);
        Sub2 sub2 = new Sub2();
        sub2.setSub2Value(9);
        em.persist(sub1);
        em.persist(sub2);

        // When
        CriteriaBuilder<Parent> criteria = cbf.create(em, Parent.class);
        List<Parent> result = criteria.from(Parent.class, "p")
                .where("COALESCE(TREAT(p AS Sub1).sub1Value, TREAT(p AS Sub2).sub2Value)").gtExpression("10")
                .getResultList();

        // Then
        assertEquals(1, result.size());
        assertTrue(result.contains(sub1));
    }

    @Test
    // NOTE: EclipseLink is just fundamentally broken in this regard...
    @Category({ NoEclipselink.class })
    public void treatWithSuperTypeColumnAccess() {
        // Given
        Sub1 sub1 = new Sub1();
        sub1.setSub1Value(11);
        Sub2 sub2 = new Sub2();
        sub2.setSub2Value(9);
        em.persist(sub1);
        em.persist(sub2);

        // When
        CriteriaBuilder<Parent> criteria = cbf.create(em, Parent.class);
        List<Parent> result = criteria.from(Parent.class, "p")
                .where("TREAT(p AS Sub2).id").eq(sub1.getId())
                .where("TREAT(p AS Sub1).id").eq(sub2.getId())
                .getResultList();

        // Then
        assertEquals(0, result.size());
    }
}
