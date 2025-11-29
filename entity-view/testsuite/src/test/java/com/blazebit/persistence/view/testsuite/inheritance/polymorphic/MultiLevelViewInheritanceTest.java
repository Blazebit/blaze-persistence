/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.polymorphic;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.2.0
 */
@Category({ NoEclipselink.class })
public class MultiLevelViewInheritanceTest extends AbstractEntityViewTest {

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
    public static abstract class B extends A {

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
    public static class C extends B {

        @Column
        private Integer someOtherValue;

        public Integer getSomeOtherValue() {
            return someOtherValue;
        }

        public void setSomeOtherValue(Integer someOtherValue) {
            this.someOtherValue = someOtherValue;
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
    @EntityViewInheritance
    public interface BView extends AView {

        @Mapping("someValue")
        Short getSomeValue();

    }

    @EntityView(C.class)
    public interface CView extends BView {

        @Mapping("someOtherValue")
        Integer getSomeOtherValue();
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
                C c = new C();
                c.setSomeOtherValue(1);
                c.setSomeValue((short) 7);
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
    public void testThatEntityViewSubTypeCanBeResolvedFromSuperSuperType() throws Exception {
        AView singleResult = evm.applySetting(
                EntityViewSetting.create(AView.class),
                cbf.create(em, A.class).where("name").eq("test")
        ).getSingleResult();
        assertTrue(singleResult instanceof CView);
    }

    @Test
    public void testThatEntityViewSubTypeCanBeResolvedFromSuperType() throws Exception {
        BView singleResult = evm.applySetting(
                EntityViewSetting.create(BView.class),
                cbf.create(em, B.class).where("name").eq("test")
        ).getSingleResult();
        assertTrue(singleResult instanceof CView);
    }

    @Test
    public void testThatEntityViewSubTypeWorks() throws Exception {
        CView singleResult = evm.applySetting(
                EntityViewSetting.create(CView.class),
                cbf.create(em, C.class).where("name").eq("test")
        ).getSingleResult();
        assertTrue(singleResult instanceof CView);
    }

}
