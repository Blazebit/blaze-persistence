/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.subview.multiplecollections;

import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.subview.multiplecollections.model.A;
import com.blazebit.persistence.view.testsuite.subview.multiplecollections.model.B;
import com.blazebit.persistence.view.testsuite.subview.multiplecollections.model.C;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author Moritz Becker
 * @since 1.4.0
 */
public class MissingItemsTest extends AbstractEntityViewTest {

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class[] {
                A.class,
                B.class,
                C.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                A a1 = new A();
                A a2 = new A();
                C c = new C(1L, 2L);
                c.setValue("test");
                B b1 = new B();
                B b2 = new B();
                b1.getcSet().add(c);
                b2.getcSet().add(c);

                a1.getcSet().add(c);
                a2.getcSet().add(c);

                a1.getbSet().add(b1);
                a1.getbSet().add(b2);
                a2.getbSet().add(b1);
                a2.getbSet().add(b2);

                em.persist(c);
                em.persist(b1);
                em.persist(b2);
                em.persist(a1);
                em.persist(a2);
            }
        });
    }

    @Test
    public void test() {
        EntityViewManager evm = build(
                AView.class,
                BView.class,
                CView.class,
                CView.Id.class
        );

        List<AView> aView = evm.applySetting(EntityViewSetting.create(AView.class), cbf.create(em, AView.class).from(A.class)).getResultList();
        assertEquals(2, aView.size());
        assertEquals(2, aView.get(0).getbSet().size());
        assertEquals(1, aView.get(0).getcSet().size());
    }

    @EntityView(A.class)
    public interface AView {
        @IdMapping
        Long getId();
        Set<BView> getbSet();
        Set<CView> getcSet();
    }

    @EntityView(B.class)
    public interface BView {
        @IdMapping
        Long getId();
        Set<CView> getcSet();
    }

    @EntityView(C.class)
    public interface CView {
        @IdMapping("this")
        Id getId();
        String getValue();

        @EntityView(C.class)
        interface Id {
            Long getId1();
            Long getId2();
        }
    }
}
