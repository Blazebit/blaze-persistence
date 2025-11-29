/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subview.treat;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.*;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.subview.treat.model.BaseContainerItem;
import com.blazebit.persistence.view.testsuite.subview.treat.model.Container;
import com.blazebit.persistence.view.testsuite.subview.treat.model.ContainerItem1;
import org.junit.Test;

import jakarta.persistence.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
public class SubviewTreatTest extends AbstractEntityViewTest {

    private Container container;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[] {
                Container.class,
                BaseContainerItem.class,
                ContainerItem1.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Container container = new Container(1L);
                ContainerItem1 item = new ContainerItem1(2L);
                container.setItem(item);

                em.persist(item);
                em.persist(container);
                SubviewTreatTest.this.container = container;
            }
        });
    }

    @Test
    public void testSubviewTreat() {
        EntityViewManager evm = build(
                ContainerView.class,
                BaseContainerItemView.class,
                Container1ItemView.class
        );

        ContainerView containerView = evm.applySetting(EntityViewSetting.create(ContainerView.class), cbf.create(em, Container.class)).getSingleResult();

        assertEquals(container.getId(), containerView.getId());
        assertEquals(container.getItem().getId(), containerView.getItem().getId());
    }


    @EntityView(Container.class)
    public interface ContainerView {

        @IdMapping
        Long getId();

        @Mapping("TREAT(item AS ContainerItem1)")
        Container1ItemView getItem();
    }

    @EntityView(BaseContainerItem.class)
    public interface BaseContainerItemView {
        @IdMapping
        Long getId();
    }

    @EntityView(ContainerItem1.class)
    public interface Container1ItemView extends BaseContainerItemView {
    }
}
