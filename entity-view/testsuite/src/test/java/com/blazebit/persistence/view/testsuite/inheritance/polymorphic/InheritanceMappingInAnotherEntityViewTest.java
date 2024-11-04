/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.polymorphic;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.*;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import jakarta.persistence.*;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.0
 */
@Category({ NoEclipselink.class, NoDatanucleus.class })
public class InheritanceMappingInAnotherEntityViewTest extends AbstractEntityViewTest {

    private EntityViewManager evm;

    @Entity
    @Table(name = "test_a_entity")
    @Inheritance(strategy = InheritanceType.JOINED)
    public static abstract class A {

        @Id
        @GeneratedValue
        private Long id;

        @Column
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Entity
    @Table(name = "test_b_entity")
    public static class B extends A {

        @Column
        private Short someValue;

        public Short getSomeValue() {
            return someValue;
        }

        public void setSomeValue(Short someValue) {
            this.someValue = someValue;
        }

    }

    @Entity
    @Table(name = "test_c_entity")
    public static class C {

        @Id
        @GeneratedValue
        private Long id;

        @Column
        private String name;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn
        private A a;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public A getA() {
            return a;
        }

        public void setA(A a) {
            this.a = a;
        }
    }

    @EntityView(A.class)
    @EntityViewInheritance
    public interface AView {

        @IdMapping
        Long getId();

        @Mapping("name")
        String getName();

    }

    @EntityView(B.class)
    public interface BView extends AView {

        @Mapping("someValue")
        Short getSomeValue();

    }

    @EntityView(C.class)
    public interface CView {

        @IdMapping
        Long getId();

        @Mapping("name")
        String getName();

        @Mapping("a")
        AView getAView();
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
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
                B b = new B();
                b.setSomeValue((short) 5);
                b.setName("test1");
                em.persist(b);

                C c = new C();
                c.setA(b);
                c.setName("test");
                em.persist(c);
            }
        });
    }

    @Before
    public void setUp() {
        this.evm = build(AView.class, BView.class, CView.class);
    }

    @Test
    public void testUsingInheritanceMappingInAnotherEntityView() throws Exception {
        CView singleResult = evm.applySetting(
                EntityViewSetting.create(CView.class),
                cbf.create(em, C.class).where("name").eq("test")
        ).getSingleResult();

        assertTrue(singleResult instanceof CView);
        assertTrue(((CView) singleResult).getAView() instanceof BView);
    }

}
