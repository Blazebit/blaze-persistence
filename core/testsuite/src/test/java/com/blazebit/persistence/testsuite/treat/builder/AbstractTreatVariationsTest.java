
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

package com.blazebit.persistence.testsuite.treat.builder;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.impl.CachingJpaProvider;
import com.blazebit.persistence.spi.JpaProvider;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.treat.entity.Base;
import com.blazebit.persistence.testsuite.treat.entity.BaseEmbeddable;
import com.blazebit.persistence.testsuite.treat.entity.JoinedBase;
import com.blazebit.persistence.testsuite.treat.entity.JoinedSub1;
import com.blazebit.persistence.testsuite.treat.entity.JoinedSub2;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableBase;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableSub1;
import com.blazebit.persistence.testsuite.treat.entity.SingleTableSub2;
import com.blazebit.persistence.testsuite.treat.entity.Sub1;
import com.blazebit.persistence.testsuite.treat.entity.Sub1Embeddable;
import com.blazebit.persistence.testsuite.treat.entity.Sub2;
import com.blazebit.persistence.testsuite.treat.entity.Sub2Embeddable;
import com.blazebit.persistence.testsuite.treat.entity.TablePerClassBase;
import com.blazebit.persistence.testsuite.treat.entity.TablePerClassSub1;
import com.blazebit.persistence.testsuite.treat.entity.TablePerClassSub2;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractTreatVariationsTest extends AbstractCoreTest {
    
    protected final String strategy;
    protected final String objectPrefix;
    
    public AbstractTreatVariationsTest(String strategy, String objectPrefix) {
        this.strategy = strategy;
        this.objectPrefix = objectPrefix;
    }

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                BaseEmbeddable.class,
                IntIdEntity.class,
                JoinedBase.class,
                JoinedSub1.class,
                JoinedSub2.class,
                SingleTableBase.class,
                SingleTableSub1.class,
                SingleTableSub2.class,
                Sub1Embeddable.class,
                Sub2Embeddable.class,
                TablePerClassBase.class,
                TablePerClassSub1.class,
                TablePerClassSub2.class
        };
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                IntIdEntity i1 = new IntIdEntity("i1", 1);
                em.persist(i1);
                persist(em, new IntIdEntity("s1", 1));
                persist(em, new IntIdEntity("s2", 2));
                persist(em, new IntIdEntity("s1.parent", 101));
                persist(em, new IntIdEntity("s2.parent", 102));
                persist(em, new IntIdEntity("st1", 1));
                persist(em, new IntIdEntity("st2", 2));
                persist(em, new IntIdEntity("st1.parent", 101));
                persist(em, new IntIdEntity("st2.parent", 102));
                persist(em, new IntIdEntity("tpc1", 1));
                persist(em, new IntIdEntity("tpc2", 2));
                persist(em, new IntIdEntity("tpc1.parent", 101));
                persist(em, new IntIdEntity("tpc2.parent", 102));

                /****************
                 * Joined
                 ***************/

                JoinedSub1 s1 = new JoinedSub1("s1");
                JoinedSub2 s2 = new JoinedSub2("s2");
                JoinedSub1 s1Parent = new JoinedSub1("s1.parent");
                JoinedSub2 s2Parent = new JoinedSub2("s2.parent");

                if (supportsJoinedInheritance()) {
                    persist(em, i1, s1, s2, s1Parent, s2Parent);
                }

                /****************
                 * Single Table
                 ***************/

                SingleTableSub1 st1 = new SingleTableSub1("st1");
                SingleTableSub2 st2 = new SingleTableSub2("st2");
                SingleTableSub1 st1Parent = new SingleTableSub1("st1.parent");
                SingleTableSub2 st2Parent = new SingleTableSub2("st2.parent");

                persist(em, i1, st1, st2, st1Parent, st2Parent);

                /****************
                 * Table per Class
                 ***************/

                TablePerClassSub1 tpc1 = new TablePerClassSub1(1L, "tpc1");
                TablePerClassSub2 tpc2 = new TablePerClassSub2(2L, "tpc2");
                TablePerClassSub1 tpc1Parent = new TablePerClassSub1(3L, "tpc1.parent");
                TablePerClassSub2 tpc2Parent = new TablePerClassSub2(4L, "tpc2.parent");

                // The Java compiler can't up-cast automatically, maybe a bug?
                //persist(em, i1, tpc1, tpc2, tpc1Parent, tpc2Parent);
                if (supportsTablePerClassInheritance()) {
                    persist(em, i1, (Sub1) tpc1, (Sub2) tpc2, (Sub1) tpc1Parent, (Sub2) tpc2Parent);
                }
            }
        });
    }

    @Override
    public void init() {
        // Well Datanucleus essentially has no support for treat...
        // I tried very hard but it unfortunately does not produce any useful results
        Assume.assumeTrue(!STATIC_JPA_PROVIDER.getClass().getName().contains("DataNucleus"));
        super.init();
    }

    @Before
    public void generalAssumptions() {
        assumeTablePerClassSupportedWithTreat();
        assumeJoinedSupportedWithTreat();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void persist(
            EntityManager em,
            IntIdEntity i1,
            Sub1<? extends Base<?, ?>, ? extends BaseEmbeddable<?>, ? extends Sub1Embeddable<?>> s1,
            Sub2<? extends Base<?, ?>, ? extends BaseEmbeddable<?>, ? extends Sub2Embeddable<?>> s2,
            Sub1<? extends Base<?, ?>, ? extends BaseEmbeddable<?>, ? extends Sub1Embeddable<?>> s1Parent,
            Sub2<? extends Base<?, ?>, ? extends BaseEmbeddable<?>, ? extends Sub2Embeddable<?>> s2Parent) {
        
        
        em.persist(s1Parent);
        em.persist(s2Parent);
        em.persist(s1);
        em.persist(s2);
        
        s1Parent.setValue(101);
        s1Parent.setSub1Value(101);
        s1Parent.getSub1Embeddable().setSomeValue(101);
        s1Parent.getEmbeddable1().setSub1SomeValue(101);
        s1.setValue(1);
        s1.setSub1Value(1);
        s1.getEmbeddable1().setSub1SomeValue(1);
        s1.setRelation1(i1);
        ((Sub1) s1).setParent(s1Parent);
        ((Sub1) s1).setParent1(s1Parent);
        ((BaseEmbeddable) s1.getEmbeddable()).setParent(s1Parent);
        ((Sub1Embeddable) s1.getEmbeddable1()).setSub1Parent(s1Parent);
        ((List<Base<?, ?>>) s1.getList()).add(s1Parent);
        ((List<Base<?, ?>>) s1.getList1()).add(s1Parent);
        ((List<Base<?, ?>>) s1.getEmbeddable().getList()).add(s1Parent);
        ((List<Base<?, ?>>) s1.getEmbeddable1().getSub1List()).add(s1Parent);
        ((List<Base<?, ?>>) s1Parent.getList()).add(s2);
        ((List<Base<?, ?>>) s1Parent.getList1()).add(s2);
        ((List<Base<?, ?>>) s1Parent.getEmbeddable().getList()).add(s2);
        ((List<Base<?, ?>>) s1Parent.getEmbeddable1().getSub1List()).add(s2);
        ((Map<Base<?, ?>, Base<?, ?>>) s1.getMap()).put(s1Parent, s1Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s1.getMap1()).put(s1Parent, s1Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s1.getEmbeddable().getMap()).put(s1Parent, s1Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s1.getEmbeddable1().getSub1Map()).put(s1Parent, s1Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s1Parent.getMap()).put(s2, s2);
        ((Map<Base<?, ?>, Base<?, ?>>) s1Parent.getMap1()).put(s2, s2);
        ((Map<Base<?, ?>, Base<?, ?>>) s1Parent.getEmbeddable().getMap()).put(s2, s2);
        ((Map<Base<?, ?>, Base<?, ?>>) s1Parent.getEmbeddable1().getSub1Map()).put(s2, s2);
        
        s2Parent.setValue(102);
        s2Parent.setSub2Value(102);
        s2Parent.getSub2Embeddable().setSomeValue(102);
        s2Parent.getEmbeddable2().setSub2SomeValue(102);
        s2.setValue(2);
        s2.setSub2Value(2);
        s2.getEmbeddable2().setSub2SomeValue(2);
        s2.setRelation2(i1);
        ((Sub2) s2).setParent(s2Parent);
        ((Sub2) s2).setParent2(s2Parent);
        ((BaseEmbeddable) s2.getEmbeddable()).setParent(s2Parent);
        ((Sub2Embeddable) s2.getEmbeddable2()).setSub2Parent(s2Parent);
        ((List<Base<?, ?>>) s2.getList()).add(s2Parent);
        ((List<Base<?, ?>>) s2.getList2()).add(s2Parent);
        ((List<Base<?, ?>>) s2.getEmbeddable().getList()).add(s2Parent);
        ((List<Base<?, ?>>) s2.getEmbeddable2().getSub2List()).add(s2Parent);
        ((List<Base<?, ?>>) s2Parent.getList()).add(s1);
        ((List<Base<?, ?>>) s2Parent.getList2()).add(s1);
        ((List<Base<?, ?>>) s2Parent.getEmbeddable().getList()).add(s1);
        ((List<Base<?, ?>>) s2Parent.getEmbeddable2().getSub2List()).add(s1);
        ((Map<Base<?, ?>, Base<?, ?>>) s2.getMap()).put(s2Parent, s2Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s2.getMap2()).put(s2Parent, s2Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s2.getEmbeddable().getMap()).put(s2Parent, s2Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s2.getEmbeddable2().getSub2Map()).put(s2Parent, s2Parent);
        ((Map<Base<?, ?>, Base<?, ?>>) s2Parent.getMap()).put(s1, s1);
        ((Map<Base<?, ?>, Base<?, ?>>) s2Parent.getMap2()).put(s1, s1);
        ((Map<Base<?, ?>, Base<?, ?>>) s2Parent.getEmbeddable().getMap()).put(s1, s1);
        ((Map<Base<?, ?>, Base<?, ?>>) s2Parent.getEmbeddable2().getSub2Map()).put(s1, s1);
    }
    
    private void persist(
            EntityManager em,
            IntIdEntity i1) {
        // Persist 2 name matching IntIdEntity one with the child value and one with the parent value
        em.persist(i1);
        if (i1.getValue() > 100) {
            em.persist(new IntIdEntity(i1.getName(), i1.getValue() - 100));
        } else {
            em.persist(new IntIdEntity(i1.getName(), i1.getValue() + 100));
        }
    }
    
    /************************************************************
     * Just some helper methods
     ************************************************************/
    protected Class<?> entity(String suffix) {
        try {
            return Class.forName(Base.class.getPackage().getName() + "." + strategy + suffix);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> CriteriaBuilder<T> from(Class<T> result, String suffix, String alias) {
        return cbf.create(em, result).from(entity(suffix), alias);
    }

    protected <T> List<T> list(CriteriaBuilder<T> cb) {
        List<T> bases = cb.getResultList();
        // Close the em and emf to make sure this was fetched properly
        em.getTransaction().rollback();
        em.close();
        emf.close();
        // Return new list since datanucleus makes it impossible to modify the direct result
        return new ArrayList<>(bases);
    }
    
    protected void assertRemoved(List<Object[]> list, Object[] expected) {
        Iterator<Object[]> iter = list.iterator();
        while (iter.hasNext()) {
            if (Arrays.deepEquals(expected, iter.next())) {
                iter.remove();
                return;
            }
        }
        
        Assert.fail(Arrays.deepToString(list.toArray()) + " does not contain expected entry: " + Arrays.deepToString(expected));
    }
    
    protected void assertRemoved(List<? extends Object> list, Object expected) {
        if (list.remove(expected)) {
            return;
        }
        
        Assert.fail(list + " does not contain expected entry: " + expected);
    }

    private boolean isHibernate() {
        return getJpaProvider().getClass().getName().contains("Hibernate");
    }

    private boolean isEclipseLink() {
        return getJpaProvider().getClass().getName().contains("Eclipse");
    }

    private boolean isDataNucleus() {
        return getJpaProvider().getClass().getName().contains("DataNucleus");
    }

    private JpaProvider getJpaProvider() {
        if (jpaProvider instanceof CachingJpaProvider) {
            return ((CachingJpaProvider) jpaProvider).getJpaProvider();
        }

        return jpaProvider;
    }

    protected void assumeQueryLanguageSupportsKeyDeReference() {
        Assume.assumeTrue("The JPA provider does not support de-referencing map keys", supportsMapKeyDeReference());
    }

    protected void assumeHibernateSupportsMultiTpcWithTypeExpression() {
        // TODO: create issue for this
        Assume.assumeTrue("Hibernate does not prefix the table per class discriminator column properly when using a type expression!", !strategy.equals("TablePerClass") || !isHibernate());
    }

    protected void assumeHibernateSupportsMapKeyTypeExpressionInSubquery() {
        // TODO: create issue for this
        Assume.assumeTrue("Hibernate currently does not support a type expression with a key in a subquery!", !isHibernate());
    }

    protected void assumeInverseSetCorrelationJoinsSubtypesWhenJoined() {
        Assume.assumeTrue("Hibernate before 5 did not support joining an inverse collection that has a joined inheritance type!", !strategy.equals("Joined") || supportsInverseSetCorrelationJoinsSubtypesWhenJoined());
    }

    protected void assumeRootTreatJoinSupportedOrEmulated() {
        Assume.assumeTrue("The JPA provider does not support root treat joins!", jpaProvider.supportsRootTreatJoin() || jpaProvider.supportsSubtypeRelationResolving());
    }

    protected void assumeTreatJoinWithRootTreatSupportedOrEmulated() {
        Assume.assumeTrue("The JPA provider does not support a treat join that contains a root treat!", jpaProvider.supportsRootTreatTreatJoin() || jpaProvider.supportsSubtypeRelationResolving());
    }

    protected void assumeCollectionTreatJoinWithRootTreatWorks() {
        Assume.assumeTrue("Eclipselink does not support a treat join of a collection when containing a root treat!", !isEclipseLink());
    }

    protected void assumeAccessTreatedOuterQueryVariableWorks() {
        Assume.assumeTrue("Eclipselink does not support using a treat in a subquery referring to the outer query!", !isEclipseLink());
    }

    protected void assumeTreatInSubqueryCorrelationWorks() {
        Assume.assumeTrue("The JPA provider does not support treat as correlation path in a subquery!", jpaProvider.supportsTreatCorrelation() || jpaProvider.supportsSubtypeRelationResolving());
    }

    protected void assumeMapInEmbeddableIsSupported() {
        Assume.assumeTrue("Only Hibernate supports mapping a java.util.Map in an embeddable!", isHibernate());
    }

    protected void assumeTreatMapAssociationIsSupported() {
        // Seems the code assumes it's the "key" of the map when it's actually a "treat"
        Assume.assumeTrue("Eclipselink does not support treating an association of type java.util.Map!", !isEclipseLink());
    }

    protected void assumeMultipleTreatJoinWithSingleTableIsNotBroken() {
        // So with Eclipselink having two different treat joins that use the same join path will result in a single sql join. Type restrictions are in the WHERE clause
        Assume.assumeTrue("Eclipselink does not support multiple treat joins on the same relation with single table inheritance!", !strategy.equals("SingleTable") || !isEclipseLink());
    }

    protected void assumeMultipleInnerTreatJoinWithSingleTableIsNotBroken() {
        // So with Hibernate having two different inner treat joins that use the same join path will share the type restrictions when the association type uses single table inheritance
        Assume.assumeTrue("Hibernate does not support multiple inner treat joins on the same relation with single table inheritance!", !strategy.equals("SingleTable") || !isHibernate());
    }

    protected void assumeLeftTreatJoinWithSingleTableIsNotBroken() {
        // Eclipselink puts the type restriction of a left treat join in the WHERE clause which is wrong. The type restriction should be part of the ON clause
        Assume.assumeTrue("Eclipselink does not support left treat joins with single table inheritance properly as the type filter is not part of the join condition!", !strategy.equals("SingleTable") || !isEclipseLink());
    }

    protected void assumeLeftTreatJoinWithRootTreatIsNotBroken() {
        // Eclipselink puts the type restriction of a left treat join in the WHERE clause which is wrong. The type restriction should be part of the ON clause
        Assume.assumeTrue("Eclipselink does not support left treat joins with root treats properly as the type filter is not part of the join condition!", !isEclipseLink());
    }

    protected void assumeTreatInNonPredicateDoesNotFilter() {
        // Eclipselink creates type restrictions for every treat expression it encounters, regardless of the location
        Assume.assumeTrue("Eclipelink does not support treat in non-predicates without filtering the result!", !isEclipseLink());
    }

    private boolean supportsTablePerClassInheritance() {
        return !isEclipseLink();
    }

    private void assumeTablePerClassSupportedWithTreat() {
        Assume.assumeTrue("Eclipselink does not support treat when using the table per class strategy!", !strategy.equals("TablePerClass") || supportsTablePerClassInheritance());
    }

    private boolean supportsJoinedInheritance() {
        return !isDataNucleus();
    }

    private void assumeJoinedSupportedWithTreat() {
        Assume.assumeTrue("Datanucleus does not support treat when using the joined strategy!", !strategy.equals("Joined") || supportsJoinedInheritance());
    }
    
}
