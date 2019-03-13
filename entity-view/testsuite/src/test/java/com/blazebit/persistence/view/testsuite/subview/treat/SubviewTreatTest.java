/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.view.testsuite.subview.treat;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.*;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.subview.treat.model.BaseContainerItem;
import com.blazebit.persistence.view.testsuite.subview.treat.model.Container;
import com.blazebit.persistence.view.testsuite.subview.treat.model.ContainerItem1;
import org.junit.Test;

import javax.persistence.*;

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
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(ContainerView.class);
        cfg.addEntityView(BaseContainerItemView.class);
        cfg.addEntityView(Container1ItemView.class);
        EntityViewManager evm = cfg.createEntityViewManager(cbf);

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
