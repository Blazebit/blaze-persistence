/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.multiple;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.IdClassEntity;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.collections.multiple.model.IdClassEntityIdView;
import com.blazebit.persistence.view.testsuite.collections.multiple.model.IdClassEntityWithMultipleSetsAsListView;
import org.junit.Before;
import org.junit.Test;

import jakarta.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class SetAsListEntityViewTestTest extends AbstractEntityViewTest {

    private IdClassEntity entity;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            IdClassEntity.class
        };
    }

    @Before
    public void setUp() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                entity = new IdClassEntity(1, "1", 1);
                IdClassEntity e2 = new IdClassEntity(2, "2", 2);
                IdClassEntity e3 = new IdClassEntity(3, "3", 3);

                entity.getChildren().add(e2);
                entity.getChildren().add(e3);
                entity.getChildren2().add(e2);
                entity.getChildren2().add(e3);

                em.persist(e2);
                em.persist(e3);
                em.persist(entity);
            }
        });
    }

    @Test
    public void testCollections() {
        EntityViewManager evm = build(
                IdClassEntityIdView.class,
                IdClassEntityIdView.Id.class,
                IdClassEntityWithMultipleSetsAsListView.class
        );

        CriteriaBuilder<IdClassEntity> criteria = cbf.create(em, IdClassEntity.class, "e")
            .where("key1").eq(entity.getKey1())
            .where("key2").eq(entity.getKey2());
        CriteriaBuilder<IdClassEntityWithMultipleSetsAsListView> cb = evm.applySetting(EntityViewSetting.create(IdClassEntityWithMultipleSetsAsListView.class), criteria);
        List<IdClassEntityWithMultipleSetsAsListView> results = cb.getResultList();

        assertEquals(1, results.size());
        assertEquals(2, results.get(0).getChildren().size());
        assertEquals(2, results.get(0).getChildren2().size());
        assertEquals(results.get(0).getChildren().get(0), results.get(0).getChildren2().get(1));
        assertEquals(results.get(0).getChildren().get(1), results.get(0).getChildren2().get(0));
    }
}
