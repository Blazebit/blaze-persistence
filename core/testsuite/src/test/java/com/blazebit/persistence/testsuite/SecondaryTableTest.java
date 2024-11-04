/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.UpdateCriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.entity.SecondaryTableEntityBase;
import com.blazebit.persistence.testsuite.entity.SecondaryTableEntitySub;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SecondaryTableTest extends AbstractCoreTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] { SecondaryTableEntityBase.class, SecondaryTableEntitySub.class };
    }

    @Test
    // #521
    public void secondaryTableWithJoinedInheritanceTest() {
        SecondaryTableEntitySub b = new SecondaryTableEntitySub();
        b.setB(4L);
        b.setC(5L);
        em.persist(b);

        CriteriaBuilder<SecondaryTableEntitySub> criteria = cbf.create(em, SecondaryTableEntitySub.class, "d");
        criteria.where("d.c").eq(5L);

        assertFalse(criteria.getResultList().isEmpty());
    }

    @Test
    // DataNucleus doesn't handle that properly
    @Category({ NoDatanucleus.class })
    public void updateSecondaryTableAttribute() {
        SecondaryTableEntitySub b = new SecondaryTableEntitySub();
        b.setB(4L);
        em.persist(b);
        em.flush();
        em.clear();

        UpdateCriteriaBuilder<SecondaryTableEntitySub> criteria = cbf.update(em, SecondaryTableEntitySub.class, "d");
        criteria.set("b", 4L);
        criteria.set("c", 5L);
        criteria.executeUpdate();
        assertEquals(Long.valueOf(5L), em.find(SecondaryTableEntitySub.class, b.getId()).getC());
    }

}
