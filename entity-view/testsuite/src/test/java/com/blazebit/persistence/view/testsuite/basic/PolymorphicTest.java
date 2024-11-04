/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Before;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.basic.model.NamedView;
import com.blazebit.persistence.view.testsuite.basic.model.TestEntityView;
import com.blazebit.persistence.view.testsuite.entity.TestEntity;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class PolymorphicTest extends AbstractEntityViewTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                TestEntity.class
        };
    }

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                TestEntity test = new TestEntity("test1", "test");
                em.persist(test);
            }
        });
    }

    @Test
    public void testApplyParent() {
        EntityViewManager evm = build(
                NamedView.class,
                TestEntityView.class
        );

        // Base setting
        EntityViewSetting<NamedView, CriteriaBuilder<NamedView>> setting = EntityViewSetting.create(NamedView.class);

        // Query
        CriteriaBuilder<TestEntity> cb = cbf.create(em, TestEntity.class);
        List<NamedView> result = evm.applySetting(setting, cb).getResultList();

        assertEquals(1, result.size());
        assertEquals("test1", result.get(0).getName());
    }
}
