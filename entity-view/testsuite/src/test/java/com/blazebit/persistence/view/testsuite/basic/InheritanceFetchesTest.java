/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Kacper Urbaniec
 * @since 1.6.14
 */
@Category({NoEclipselink.class, NoDatanucleus.class})
public class InheritanceFetchesTest extends AbstractEntityViewTest {

    private EntityViewManager evm;

    @Entity(name = "Container")
    @Table(name = "test_container_entity")
    public static class Container {

        @Id
        @GeneratedValue
        private Long id;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "base_1_id", referencedColumnName = "id")
        private Base base1;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "base_2_id", referencedColumnName = "id")
        private Base base2;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Base getBase1() {
            return base1;
        }

        public void setBase1(Base base1) {
            this.base1 = base1;
        }

        public Base getBase2() {
            return base2;
        }

        public void setBase2(Base base2) {
            this.base2 = base2;
        }

    }

    @Entity(name = "Base")
    @Table(name = "test_base_entity")
    @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
    public abstract static class Base {

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

    @Entity(name = "Foo")
    public static class Foo extends Base {

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "foo_id", referencedColumnName = "id")
        private Qux foo;

        public Qux getFoo() {
            return foo;
        }

        public void setFoo(Qux foo) {
            this.foo = foo;
        }
    }

    @Entity(name = "Bar")
    public static class Bar extends Base {

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "bar_id", referencedColumnName = "id")
        private Qux bar;

        public Qux getBar() {
            return bar;
        }

        public void setBar(Qux bar) {
            this.bar = bar;
        }
    }

    @Entity(name = "Qux")
    @Table(name = "test_qux_entity")
    public static class Qux {

        @Id
        @GeneratedValue
        private Long id;

        @Column
        private String someValue;

        public String getSomeValue() {
            return someValue;
        }

        public void setSomeValue(String value) {
            this.someValue = value;
        }

    }

    @EntityView(Container.class)
    public interface ContainerView {

        @IdMapping
        Long getId();

        @Mapping("base1")
        BaseView getBase1();

        @Mapping("base2")
        BaseView getBase2();

    }

    @EntityView(Container.class)
    public interface ContainerWithSelectView {

        @IdMapping
        Long getId();

        @Mapping(value = "treat(base1 as Foo)", fetch = FetchStrategy.SELECT)
        FooView getBase1();

        @Mapping(value = "treat(base2 as Bar)", fetch = FetchStrategy.SUBSELECT)
        BarView getBase2();

    }

    @EntityView(Base.class)
    @EntityViewInheritance
    public interface BaseView {

        @IdMapping
        Long getId();

        @Mapping("name")
        String getName();

    }

    @EntityView(Foo.class)
    public interface FooView extends BaseView {

        @IdMapping
        Long getId();

        @Mapping("name")
        String getName();

        @Mapping("foo")
        QuxView getFoo();

    }

    @EntityView(Bar.class)
    public interface BarView extends BaseView {

        @IdMapping
        Long getId();

        @Mapping("name")
        String getName();

        @Mapping("bar")
        QuxView getBar();

    }

    @EntityView(Qux.class)
    public interface QuxView {

        @IdMapping
        Long getId();

        @Mapping("someValue")
        String getSomeValue();

    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[]{
            Container.class,
            Base.class,
            Foo.class,
            Bar.class,
            Qux.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Qux fooQux = new Qux();
                fooQux.setSomeValue("foo");
                em.persist(fooQux);
                Foo foo = new Foo();
                foo.setName("Foo");
                foo.setFoo(fooQux);
                em.persist(foo);

                Qux barQux = new Qux();
                barQux.setSomeValue("bar");
                em.persist(barQux);
                Bar bar = new Bar();
                bar.setName("Bar");
                bar.setBar(barQux);
                em.persist(bar);

                Container container = new Container();
                container.setBase1(foo);
                container.setBase2(bar);
                em.persist(container);
            }
        });
    }

    @Before
    public void setUp() {
        this.evm = build(ContainerView.class, ContainerWithSelectView.class, BaseView.class, FooView.class, BarView.class, QuxView.class);
    }

    @Test
    public void testEmptySelection() {
        EntityViewSetting<BaseView, CriteriaBuilder<BaseView>> setting = EntityViewSetting.create(BaseView.class);
        setting.fetch("id");
        setting.fetch("name");
        CriteriaBuilder<Base> builder = cbf.create(em, Base.class);

        Collection<BaseView> bases = evm.applySetting(setting, builder).getResultList();

        assertEquals(2, bases.size());
        for (BaseView baseView : bases) {
            assertNotNull(baseView);
        }
        FooView foo = firstOrNull(bases, FooView.class);
        assertNotNull(foo);
        assertEquals("Foo", foo.getName());
        assertNull(foo.getFoo());
        BarView bar = firstOrNull(bases, BarView.class);
        assertNotNull(bar);
        assertEquals("Bar", bar.getName());
        assertNull(bar.getBar());
    }

    @Test
    public void testFullSelection() {
        EntityViewSetting<BaseView, CriteriaBuilder<BaseView>> setting = EntityViewSetting.create(BaseView.class);
        setting.fetch("id");
        setting.fetch("name");
        setting.fetch("foo.someValue");
        setting.fetch("bar.someValue");
        CriteriaBuilder<Base> builder = cbf.create(em, Base.class);

        Collection<BaseView> bases = evm.applySetting(setting, builder).getResultList();

        assertEquals(2, bases.size());
        for (BaseView baseView : bases) {
            assertNotNull(baseView);
        }
        FooView foo = firstOrNull(bases, FooView.class);
        assertNotNull(foo);
        assertEquals("Foo", foo.getName());
        assertEquals("foo", foo.getFoo().getSomeValue());
        BarView bar = firstOrNull(bases, BarView.class);
        assertNotNull(bar);
        assertEquals("Bar", bar.getName());
        assertEquals("bar", bar.getBar().getSomeValue());
    }

    @Test
    public void testContainerEmptySelection() {
        EntityViewSetting<ContainerView, CriteriaBuilder<ContainerView>> setting = EntityViewSetting.create(ContainerView.class);
        setting.fetch("id");
        CriteriaBuilder<Container> builder = cbf.create(em, Container.class);

        ContainerView container = evm.applySetting(setting, builder).getSingleResult();

        assertNotNull(container);
        assertNull(container.getBase1());
        assertNull(container.getBase1());
    }

    @Test
    public void testContainerFullSelection() {
        EntityViewSetting<ContainerView, CriteriaBuilder<ContainerView>> setting = EntityViewSetting.create(ContainerView.class);
        setting.fetch("id");
        setting.fetch("base1.id");
        setting.fetch("base1.name");
        setting.fetch("base1.foo.someValue");
        setting.fetch("base1.bar.someValue");
        setting.fetch("base2.id");
        setting.fetch("base2.name");
        setting.fetch("base2.foo.someValue");
        setting.fetch("base2.bar.someValue");
        CriteriaBuilder<Container> builder = cbf.create(em, Container.class);

        ContainerView container = evm.applySetting(setting, builder).getSingleResult();

        assertNotNull(container);
        assertNotNull(container.getBase1());
        FooView foo = (FooView) container.getBase1();
        assertEquals("Foo", foo.getName());
        assertEquals("foo", foo.getFoo().getSomeValue());
        assertNotNull(container.getBase2());
        BarView bar = (BarView) container.getBase2();
        assertEquals("Bar", bar.getName());
        assertEquals("bar", bar.getBar().getSomeValue());
    }

    @Test
    public void testContainerPartialSelection1() {
        EntityViewSetting<ContainerView, CriteriaBuilder<ContainerView>> setting = EntityViewSetting.create(ContainerView.class);
        setting.fetch("id");
        setting.fetch("base1.id");
        setting.fetch("base1.name");
        CriteriaBuilder<Container> builder = cbf.create(em, Container.class);

        ContainerView container = evm.applySetting(setting, builder).getSingleResult();

        assertNotNull(container);
        assertNotNull(container.getBase1());
        FooView foo = (FooView) container.getBase1();
        assertEquals("Foo", foo.getName());
        assertNull(foo.getFoo());
        assertNull(container.getBase2());
    }

    @Test
    public void testContainerPartialSelection2() {
        EntityViewSetting<ContainerView, CriteriaBuilder<ContainerView>> setting = EntityViewSetting.create(ContainerView.class);
        setting.fetch("id");
        setting.fetch("base2.id");
        setting.fetch("base2.name");
        CriteriaBuilder<Container> builder = cbf.create(em, Container.class);

        ContainerView container = evm.applySetting(setting, builder).getSingleResult();

        assertNotNull(container);
        assertNull(container.getBase1());
        assertNotNull(container.getBase2());
        BarView bar = (BarView) container.getBase2();
        assertEquals("Bar", bar.getName());
        assertNull(bar.getBar());
    }

    // For #1978
    @Test
    public void testContainerWithSelectEmpty() {
        EntityViewSetting<ContainerWithSelectView, CriteriaBuilder<ContainerWithSelectView>> setting = EntityViewSetting.create(ContainerWithSelectView.class);
        setting.fetch("id");
        CriteriaBuilder<Container> builder = cbf.create(em, Container.class);

        ContainerWithSelectView container = evm.applySetting(setting, builder).getSingleResult();

        assertNotNull(container);
        assertNull(container.getBase1());
        assertNull(container.getBase2());
    }

    // For #1978
    @Test
    public void testContainerWithSelectFullSelection() {
        EntityViewSetting<ContainerWithSelectView, CriteriaBuilder<ContainerWithSelectView>> setting = EntityViewSetting.create(ContainerWithSelectView.class);
        setting.fetch("id");
        setting.fetch("base1.id");
        setting.fetch("base1.name");
        setting.fetch("base1.foo.someValue");
        setting.fetch("base2.id");
        setting.fetch("base2.name");
        setting.fetch("base2.bar.someValue");
        CriteriaBuilder<Container> builder = cbf.create(em, Container.class);

        ContainerWithSelectView container = evm.applySetting(setting, builder).getSingleResult();

        assertNotNull(container);
        assertNotNull(container.getBase1());
        FooView foo = container.getBase1();
        assertEquals("Foo", foo.getName());
        assertEquals("foo", foo.getFoo().getSomeValue());
        assertNotNull(container.getBase2());
        BarView bar = container.getBase2();
        assertEquals("Bar", bar.getName());
        assertEquals("bar", bar.getBar().getSomeValue());
    }

    // For #1978
    @Test
    public void testContainerWithSelectPartial1() {
        EntityViewSetting<ContainerWithSelectView, CriteriaBuilder<ContainerWithSelectView>> setting = EntityViewSetting.create(ContainerWithSelectView.class);
        setting.fetch("id");
        setting.fetch("base1.id");
        setting.fetch("base1.name");
        CriteriaBuilder<Container> builder = cbf.create(em, Container.class);

        ContainerWithSelectView container = evm.applySetting(setting, builder).getSingleResult();

        assertNotNull(container);
        assertNull(container.getBase2());
        assertNotNull(container.getBase1());
        FooView foo = container.getBase1();
        assertEquals("Foo", foo.getName());
        assertNull(foo.getFoo());
    }

    // For #1978
    @Test
    public void testContainerWithSelectPartial2() {
        EntityViewSetting<ContainerWithSelectView, CriteriaBuilder<ContainerWithSelectView>> setting = EntityViewSetting.create(ContainerWithSelectView.class);
        setting.fetch("id");
        setting.fetch("base2.id");
        setting.fetch("base2.name");
        CriteriaBuilder<Container> builder = cbf.create(em, Container.class);

        ContainerWithSelectView container = evm.applySetting(setting, builder).getSingleResult();

        assertNotNull(container);
        assertNull(container.getBase1());
        assertNotNull(container.getBase2());
        BarView bar = container.getBase2();
        assertEquals("Bar", bar.getName());
        assertNull(bar.getBar());
    }

    static <C, T> T firstOrNull(Collection<C> collection, Class<T> clazz) {
        Object any = collection.stream().filter(clazz::isInstance).findFirst().orElse(null);
        if (any == null) {
            return null;
        }
        return clazz.cast(any);
    }

}
