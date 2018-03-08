/*
 * Copyright 2014 - 2018 Blazebit.
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

package com.blazebit.persistence.view.testsuite.inheritance.nested;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.treat.entity.IntValueEmbeddable;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableBase;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableEmbeddable;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableEmbeddableSub1;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableEmbeddableSub2;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableSub1;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableSub2;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.inheritance.nested.model.SingleTableBaseView;
import com.blazebit.persistence.view.testsuite.inheritance.nested.model.SingleTableChildView;
import com.blazebit.persistence.view.testsuite.inheritance.nested.model.SingleTableNormalView;
import com.blazebit.persistence.view.testsuite.inheritance.nested.model.SingleTableSub1View;
import com.blazebit.persistence.view.testsuite.inheritance.nested.model.SingleTableSub2View;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: Eclipselink and Datanucleus have no real support for subtype property access
@Category({ NoEclipselink.class, NoDatanucleus.class })
public class PolymorphicNestedInheritanceTest extends AbstractEntityViewTest {

    private SingleTableSub1 base1;
    private SingleTableSub2 base2;
    private EntityViewManager evm;

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[] {
                IntIdEntity.class,
                IntValueEmbeddable.class,
                SingleTableBase.class,
                SingleTableSub1.class,
                SingleTableSub2.class,
                SingleTableEmbeddable.class,
                SingleTableEmbeddableSub1.class,
                SingleTableEmbeddableSub2.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                base1 = new SingleTableSub1("st1");
                base2 = new SingleTableSub2("st2");
                base1.setSub1Value(123);
                base2.setSub2Value(456);
                em.persist(base1);
                em.persist(base2);
                base1.setParent(base2);
                base2.setParent(base1);
            }
        });
    }

    @Before
    public void setUp() {
        base1 = cbf.create(em, SingleTableSub1.class).where("name").eq("st1").getSingleResult();
        base2 = cbf.create(em, SingleTableSub2.class).where("name").eq("st2").getSingleResult();

        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.addEntityView(SingleTableNormalView.class);
        cfg.addEntityView(SingleTableChildView.class);
        cfg.addEntityView(SingleTableBaseView.class);
        cfg.addEntityView(SingleTableSub1View.class);
        cfg.addEntityView(SingleTableSub2View.class);
        this.evm = cfg.createEntityViewManager(cbf);
    }

    @Test
    public void testIssue456() {
        CriteriaBuilder<SingleTableBase> criteria = cbf.create(em, SingleTableBase.class, "d")
            .orderByAsc("id");
        CriteriaBuilder<SingleTableNormalView> cb = evm.applySetting(EntityViewSetting.create(SingleTableNormalView.class), criteria);
        List<SingleTableNormalView> results = cb.getResultList();

        assertEquals(2, results.size());
        SingleTableNormalView st1Normal = results.get(0);
        SingleTableNormalView st2Normal = results.get(1);

        assertEquals(1, st1Normal.getChildren().size());
        assertEquals(1, st2Normal.getChildren().size());

        SingleTableBaseView st1 = st1Normal.getChildren().iterator().next().getParent();
        SingleTableBaseView st2 = st2Normal.getChildren().iterator().next().getParent();

        assertTypeMatches(st1, evm, SingleTableBaseView.class, SingleTableSub1View.class);
        assertTypeMatches(st2, evm, SingleTableBaseView.class, SingleTableSub2View.class);

        SingleTableSub1View view1 = (SingleTableSub1View) st1;
        SingleTableSub2View view2 = (SingleTableSub2View) st2;

        assertBaseEquals(base1, view1);
        assertBaseEquals(base2, view2);

        assertEquals(base1.getSub1Value(), view1.getSub1Value());
        assertEquals(base2.getSub2Value(), view2.getSub2Value());
    }

    public static void assertBaseEquals(SingleTableBase doc, SingleTableBaseView view) {
        assertEquals(doc.getId(), view.getId());
        assertEquals(doc.getName(), view.getName());
    }

    public static <T> void assertTypeMatches(T o, EntityViewManager evm, Class<T> baseType, Class<? extends T> subtype) {
        assertEquals(baseType.getName() + "_" + subtype.getSimpleName() + "_$$_javassist_entityview_", o.getClass().getName());
    }
}
