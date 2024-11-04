/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.collection;

import java.util.List;
import jakarta.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.inheritance.collection.model.SingleTableBaseView;
import com.blazebit.persistence.view.testsuite.inheritance.collection.model.SingleTableParentView;
import com.blazebit.persistence.view.testsuite.inheritance.collection.model.SingleTableSimpleView;
import com.blazebit.persistence.view.testsuite.inheritance.collection.model.SingleTableSub1View;
import com.blazebit.persistence.view.testsuite.inheritance.collection.model.SingleTableSub2View;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Christian Beikov
 * @since 1.6.15
 */
// NOTE: Eclipselink and Datanucleus have no real support for subtype property access
@Category({ NoEclipselink.class, NoDatanucleus.class })
public class PolymorphicCollectionInheritanceTest extends AbstractEntityViewTest {

    private SingleTableSub1 parent;
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
                parent = new SingleTableSub1("st0");
                base1 = new SingleTableSub1("st1");
                base2 = new SingleTableSub2("st2");
                base1.setSub1Value(123);
                base2.setSub2Value(456);
                em.persist(parent);
                em.persist(base1);
                em.persist(base2);
                base1.setParent(parent);
                base2.setParent(parent);
            }
        });
    }

    @Before
    public void setUp() {
        parent = cbf.create(em, SingleTableSub1.class).where("name").eq("st0").getSingleResult();
        base1 = cbf.create(em, SingleTableSub1.class).where("name").eq("st1").getSingleResult();
        base2 = cbf.create(em, SingleTableSub2.class).where("name").eq("st2").getSingleResult();

        this.evm = build(
                SingleTableSimpleView.class,
                SingleTableParentView.class,
                SingleTableBaseView.class,
                SingleTableSub1View.class,
                SingleTableSub2View.class
        );
    }

    @Test
    public void testFetch() {
        CriteriaBuilder<SingleTableBase> criteria = cbf.create(em, SingleTableBase.class, "d")
            .where("name").eq("st0");
        CriteriaBuilder<SingleTableParentView> cb = evm.applySetting(EntityViewSetting.create(SingleTableParentView.class), criteria);
        List<SingleTableParentView> results = cb.getResultList();

        assertEquals(1, results.size());
        SingleTableParentView parent = results.get(0);

        assertEquals(2, parent.getChildren().size());

        SingleTableBaseView st1 = null;
        SingleTableBaseView st2 = null;

        for (SingleTableBaseView child : parent.getChildren()) {
            if (child.getName().equals("st1")) {
                st1 = child;
            } else if (child.getName().equals("st2")) {
                st2 = child;
            }
        }

        assertNotNull(st1);
        assertNotNull(st2);

        assertTypeMatches( st1, evm, SingleTableBaseView.class, SingleTableSub1View.class);
        assertTypeMatches( st2, evm, SingleTableBaseView.class, SingleTableSub2View.class);

        SingleTableSub1View view1 = (SingleTableSub1View) st1;
        SingleTableSub2View view2 = (SingleTableSub2View) st2;

        assertBaseEquals(base1, view1);
        assertBaseEquals(base2, view2);

        assertEquals(base1.getSub1Value(), view1.getSub1Value());
        assertEquals(base2.getSub2Value(), view2.getSub2Value());
    }

    // test for #1722
    @Test
    public void testPartialFetch() {
        CriteriaBuilder<SingleTableBase> criteria = cbf.create(em, SingleTableBase.class, "d")
                .where("name").eq("st0");
        EntityViewSetting<SingleTableParentView, CriteriaBuilder<SingleTableParentView>> setting = EntityViewSetting.create(SingleTableParentView.class);
        setting.fetch( "id" );
        CriteriaBuilder<SingleTableParentView> cb = evm.applySetting(setting, criteria);
        List<SingleTableParentView> results = cb.getResultList();

        assertEquals(1, results.size());
        SingleTableParentView parent = results.get(0);

        assertNull(parent.getChildren());
    }

    public static void assertBaseEquals(SingleTableBase doc, SingleTableBaseView view) {
        assertEquals(doc.getId(), view.getId());
        assertEquals(doc.getName(), view.getName());
    }

    public static <T> void assertTypeMatches(T o, EntityViewManager evm, Class<T> baseType, Class<? extends T> subtype) {
        String name = o.getClass().getName();
        if (name.endsWith("_")) {
            assertEquals(subtype.getName() + "_$$_javassist_entityview_", name);
        } else {
            assertEquals(subtype.getName() + "Impl", name);
        }
    }
}
