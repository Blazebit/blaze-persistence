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

package com.blazebit.persistence.view.testsuite.correlation.batchsubselect;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus4;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.correlation.batchsubselect.model.LegacyOrderPositionElementView;
import com.blazebit.persistence.view.testsuite.correlation.batchsubselect.model.LegacyOrderPositionView;
import com.blazebit.persistence.view.testsuite.correlation.batchsubselect.model.LegacyOrderView;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrder;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPosition;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionDefault;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionDefaultId;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionElement;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionEmbeddable;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.6.9
 */
// NOTE: Requires values clause which currently is only available for Hibernate
@Category({ NoDatanucleus4.class, NoDatanucleus.class, NoOpenJPA.class, NoEclipselink.class})
public class BatchSubselectSubsetCorrelationTest extends AbstractEntityViewTest {
    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            LegacyOrder.class,
            LegacyOrderPosition.class,
            LegacyOrderPositionId.class,
            LegacyOrderPositionDefault.class,
            LegacyOrderPositionDefaultId.class,
            LegacyOrderPositionElement.class,
            LegacyOrderPositionEmbeddable.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                LegacyOrder legacyOrder = new LegacyOrder();
                em.persist(legacyOrder);

                LegacyOrderPosition pos1 = new LegacyOrderPosition(new LegacyOrderPositionId(legacyOrder.getId(), 1));
                pos1.setArticleNumber("123");
                em.persist(pos1);
                pos1.getElems().add(new LegacyOrderPositionElement(legacyOrder.getId(), 1, "1abc"));
                pos1.getElems().add(new LegacyOrderPositionElement(legacyOrder.getId(), 1, "1def"));
                for (LegacyOrderPositionElement elem : pos1.getElems()) {
                    em.persist(elem);
                }

                LegacyOrderPosition pos2 = new LegacyOrderPosition(new LegacyOrderPositionId(legacyOrder.getId(), 2));
                pos2.setArticleNumber("234");
                em.persist(pos2);
                pos2.getElems().add(new LegacyOrderPositionElement(legacyOrder.getId(), 2, "2abc"));
                pos2.getElems().add(new LegacyOrderPositionElement(legacyOrder.getId(), 2, "2def"));
                for (LegacyOrderPositionElement elem : pos2.getElems()) {
                    em.persist(elem);
                }
            }
        });
    }

    // Test for issue #1689
    @Test
    public void testUseCorrelatedEmbeddingViewMacroForSubquery() {
        EntityViewManager evm = build(
                LegacyOrderView.class,
                LegacyOrderPositionView.class,
                LegacyOrderPositionView.Id.class,
                LegacyOrderPositionElementView.class
        );

        CriteriaBuilder<LegacyOrder> criteria = cbf.create(em, LegacyOrder.class, "o")
                .orderByDesc("id");
        EntityViewSetting<LegacyOrderView, CriteriaBuilder<LegacyOrderView>> setting =
                EntityViewSetting.create(LegacyOrderView.class);
        CriteriaBuilder<LegacyOrderView> cb = evm.applySetting(setting, criteria);
        List<LegacyOrderView> resultList = cb.getResultList();
        Assert.assertEquals(1, resultList.size());
        LegacyOrderView orderView = resultList.get(0);
        Set<LegacyOrderPositionView> positions = orderView.getPositions();
        Assert.assertEquals(2, positions.size());
        for (LegacyOrderPositionView position : positions) {
            Assert.assertEquals(2, position.getElems().size());
            for (LegacyOrderPositionElementView elem : position.getElems()) {
                Assert.assertEquals(orderView.getId(), elem.getSubquery());
            }
        }
    }

}
