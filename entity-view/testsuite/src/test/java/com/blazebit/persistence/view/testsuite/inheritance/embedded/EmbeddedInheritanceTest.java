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

package com.blazebit.persistence.view.testsuite.inheritance.embedded;

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
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.SingularAttribute;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.inheritance.embedded.model.IntIdEntityView;
import com.blazebit.persistence.view.testsuite.inheritance.embedded.model.SingleTableDetailsView;
import com.blazebit.persistence.view.testsuite.inheritance.embedded.model.SingleTableSub1DetailsView;
import com.blazebit.persistence.view.testsuite.inheritance.embedded.model.SingleTableSub2DetailsView;
import com.blazebit.persistence.view.testsuite.inheritance.embedded.model.SingleTableView;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: Eclipselink and Datanucleus have no real support for subtype property access
@Category({ NoEclipselink.class, NoDatanucleus.class })
public class EmbeddedInheritanceTest extends AbstractEntityViewTest {

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
                IntIdEntity i1 = new IntIdEntity("i1", 1);
                base1 = new SingleTableSub1("st1");
                base2 = new SingleTableSub2("st2");
                base1.setSub1Value(123);
                base2.setSub2Value(456);
                base2.setRelation2(i1);
                em.persist(i1);
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
        cfg.addEntityView(IntIdEntityView.class);
        cfg.addEntityView(SingleTableView.class);
        cfg.addEntityView(SingleTableDetailsView.class);
        cfg.addEntityView(SingleTableSub1DetailsView.class);
        cfg.addEntityView(SingleTableSub2DetailsView.class);
        this.evm = cfg.createEntityViewManager(cbf);
    }

    @Test
    public void inheritanceMetamodel() {
        ManagedViewType<?> baseViewType = evm.getMetamodel().managedView(SingleTableView.class);
        ManagedViewType<?> detailsViewType = evm.getMetamodel().managedView(SingleTableDetailsView.class);
        ManagedViewType<?> detailsViewType1 = evm.getMetamodel().managedView(SingleTableSub1DetailsView.class);
        ManagedViewType<?> detailsViewType2 = evm.getMetamodel().managedView(SingleTableSub2DetailsView.class);
        SingularAttribute<?, ?> detailsAttribute = (SingularAttribute<?, ?>) baseViewType.getAttribute("details");

        assertEquals(3, detailsViewType.getInheritanceSubtypes().size());
        assertTrue(detailsViewType.getInheritanceSubtypes().contains(detailsViewType));
        assertTrue(detailsViewType.getInheritanceSubtypes().contains(detailsViewType1));
        assertTrue(detailsViewType.getInheritanceSubtypes().contains(detailsViewType2));

        assertEquals("", detailsAttribute.getInheritanceSubtypeMappings().get(detailsViewType));
        assertEquals("TYPE(this) = " + SingleTableSub1.class.getSimpleName(), detailsAttribute.getInheritanceSubtypeMappings().get(detailsViewType1));
        assertEquals("TYPE(this) = " + SingleTableSub2.class.getSimpleName(), detailsAttribute.getInheritanceSubtypeMappings().get(detailsViewType2));
    }

    @Test
    public void inheritanceQuery() {
        CriteriaBuilder<SingleTableBase> criteria = cbf.create(em, SingleTableBase.class, "d")
                .orderByAsc("id");
        CriteriaBuilder<SingleTableView> cb = evm.applySetting(EntityViewSetting.create(SingleTableView.class), criteria);
        List<SingleTableView> results = cb.getResultList();

        assertEquals(2, results.size());
        assertTypeMatches(results.get(0).getDetails(), evm, SingleTableDetailsView.class, SingleTableSub1DetailsView.class);
        assertTypeMatches(results.get(1).getDetails(), evm, SingleTableDetailsView.class, SingleTableSub2DetailsView.class);

        assertViewEquals(base1, results.get(0));
        assertViewEquals(base2, results.get(1));

        SingleTableSub1DetailsView view1 = (SingleTableSub1DetailsView) results.get(0).getDetails();
        SingleTableSub2DetailsView view2 = (SingleTableSub2DetailsView) results.get(1).getDetails();

        assertViewEquals(base1, view1);
        assertViewEquals(base2, view2);

        assertEquals(base1.getSub1Value(), view1.getSub1Value());
        assertViewEquals(base2.getRelation2(), view2.getRelation2());
    }

    public static void assertViewEquals(SingleTableBase doc, SingleTableView view) {
        assertEquals(doc.getId(), view.getId());
    }

    public static void assertViewEquals(SingleTableBase doc, SingleTableDetailsView view) {
        assertEquals(doc.getName(), view.getName());
    }

    public static void assertViewEquals(IntIdEntity intIdEntity, IntIdEntityView view) {
        assertEquals(intIdEntity.getId(), view.getId());
        assertEquals(intIdEntity.getName(), view.getName());
    }

    public static <T> void assertTypeMatches(T o, EntityViewManager evm, Class<T> baseType, Class<? extends T> subtype) {
        assertEquals(baseType.getName() + "_" + subtype.getSimpleName() + "_$$_javassist_entityview_", o.getClass().getName());
    }
}
