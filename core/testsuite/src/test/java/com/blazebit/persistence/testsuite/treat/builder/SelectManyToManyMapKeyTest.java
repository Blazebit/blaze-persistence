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

import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

/**
 * This test de-references map keys for which the support came in Hibernate 5.2.
 */
@RunWith(Parameterized.class)
public class SelectManyToManyMapKeyTest extends AbstractTreatVariationsTest {

    public SelectManyToManyMapKeyTest(String strategy, String objectPrefix) {
        super(strategy, objectPrefix);
    }
    
    @Parameterized.Parameters
    public static Object[] getParameters() {
        return new Object[] {
            new Object[] { "Joined", "s" }, 
            new Object[] { "SingleTable", "st" }, 
            new Object[] { "TablePerClass", "tpc" }
        };
    }
    
    @Test
    public void selectTreatedManyToManyMapKey() {
        assumeQueryLanguageSupportsKeyDeReference();
        assumeTreatMapAssociationIsSupported();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(KEY(b.map) AS " + strategy + "Sub1).sub1Value")
        );

        // From => 4 instances
        // Left join on KEY(b.map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }

    @Test
    public void selectMultipleTreatedManyToManyMapKey() {
        assumeQueryLanguageSupportsKeyDeReference();
        assumeTreatMapAssociationIsSupported();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(KEY(b.map) AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(KEY(b.map) AS " + strategy + "Sub2).sub2Value")
        );

        // From => 4 instances
        // Left join on KEY(b.map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    public void selectTreatedParentManyToManyMapKey() {
        assumeHibernateSupportsMapKeyTypeExpressionInSubquery();
        assumeAccessTreatedOuterQueryVariableWorks();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(b.map) AS " + strategy + "Sub1).sub1Value")
                        .end()
        );

        // From => 4 instances
        // There are four map keys but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }

    @Test
    public void selectMultipleTreatedParentManyToManyMapKey() {
        assumeHibernateSupportsMapKeyTypeExpressionInSubquery();
        assumeAccessTreatedOuterQueryVariableWorks();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(b.map) AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(b.map) AS " + strategy + "Sub2).sub2Value")
                        .end()
        );

        // From => 4 instances
        // There are four map keys, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    public void selectTreatedEmbeddableManyToManyMapKey() {
        assumeQueryLanguageSupportsKeyDeReference();
        assumeMapInEmbeddableIsSupported();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).sub1Value")
        );

        // From => 4 instances
        // Left join on KEY(b.embeddable.map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }

    @Test
    public void selectMultipleTreatedEmbeddableManyToManyMapKey() {
        assumeQueryLanguageSupportsKeyDeReference();
        assumeMapInEmbeddableIsSupported();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub2).sub2Value")
        );

        // From => 4 instances
        // Left join on KEY(b.embeddable.map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    public void selectTreatedParentEmbeddableManyToManyMapKey() {
        assumeHibernateSupportsMapKeyTypeExpressionInSubquery();
        assumeMapInEmbeddableIsSupported();
        assumeAccessTreatedOuterQueryVariableWorks();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).sub1Value")
                        .end()
        );

        // From => 4 instances
        // There are four map keys but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }

    @Test
    public void selectMultipleTreatedParentEmbeddableManyToManyMapKey() {
        assumeHibernateSupportsMapKeyTypeExpressionInSubquery();
        assumeMapInEmbeddableIsSupported();
        assumeAccessTreatedOuterQueryVariableWorks();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub2).sub2Value")
                        .end()
        );

        // From => 4 instances
        // There are four map keys, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    public void selectTreatedEmbeddableManyToManyMapKeyEmbeddable() {
        assumeQueryLanguageSupportsKeyDeReference();
        assumeMapInEmbeddableIsSupported();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
        );

        // From => 4 instances
        // Left join on KEY(b.embeddable.map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }

    @Test
    public void selectMultipleTreatedEmbeddableManyToManyMapKeyEmbeddable() {
        assumeQueryLanguageSupportsKeyDeReference();
        assumeMapInEmbeddableIsSupported();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .select("TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
        );

        // From => 4 instances
        // Left join on KEY(b.embeddable.map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    public void selectTreatedParentEmbeddableManyToManyMapKeyEmbeddable() {
        assumeHibernateSupportsMapKeyTypeExpressionInSubquery();
        assumeMapInEmbeddableIsSupported();
        assumeAccessTreatedOuterQueryVariableWorks();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
        );

        // From => 4 instances
        // There are four map keys but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }

    @Test
    public void selectMultipleTreatedParentEmbeddableManyToManyMapKeyEmbeddable() {
        assumeHibernateSupportsMapKeyTypeExpressionInSubquery();
        assumeMapInEmbeddableIsSupported();
        assumeAccessTreatedOuterQueryVariableWorks();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
                        .end()
        );

        // From => 4 instances
        // There are four map keys, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    public void selectTreatedRootManyToManyMapKey() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(KEY(TREAT(b AS " + strategy + "Sub1).map1) AS " + strategy + "Sub1).sub1Value")
        );

        // From => 4 instances
        // Left join on KEY(b.map1) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }

    @Test
    public void selectMultipleTreatedRootManyToManyMapKey() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(KEY(TREAT(b AS " + strategy + "Sub1).map1) AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(KEY(TREAT(b AS " + strategy + "Sub2).map2) AS " + strategy + "Sub2).sub2Value")
        );

        // From => 4 instances
        // Left join on KEY(b.map1) and KEY(b.map2) => 4 instances
        // There are four map keys, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101 , null });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    public void selectTreatedParentRootManyToManyMapKey() {
        assumeHibernateSupportsMapKeyTypeExpressionInSubquery();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(TREAT(b AS " + strategy + "Sub1).map1) AS " + strategy + "Sub1).sub1Value")
                        .end()
        );

        // From => 4 instances
        // There are four map keys but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }

    @Test
    public void selectMultipleTreatedParentRootManyToManyMapKey() {
        assumeHibernateSupportsMapKeyTypeExpressionInSubquery();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(TREAT(b AS " + strategy + "Sub1).map1) AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(TREAT(b AS " + strategy + "Sub2).map2) AS " + strategy + "Sub2).sub2Value")
                        .end()
        );

        // From => 4 instances
        // There are four map keys, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    public void selectTreatedRootEmbeddableManyToManyMapKey() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).sub1Value")
        );

        // From => 4 instances
        // Left join on KEY(b.embeddable1.sub1Map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }

    @Test
    public void selectMultipleTreatedRootEmbeddableManyToManyMapKey() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(KEY(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map) AS " + strategy + "Sub2).sub2Value")
        );

        // From => 4 instances
        // Left join on KEY(b.embeddable1.sub1Map) and KEY(b.embeddable2.sub2Map) => 4 instances
        // There are four map keys, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101 , null });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    public void selectTreatedParentRootEmbeddableManyToManyMapKey() {
        assumeHibernateSupportsMapKeyTypeExpressionInSubquery();
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).sub1Value")
                        .end()
        );

        // From => 4 instances
        // There are four map keys but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }

    @Test
    public void selectMultipleTreatedParentRootEmbeddableManyToManyMapKey() {
        assumeHibernateSupportsMapKeyTypeExpressionInSubquery();
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map) AS " + strategy + "Sub2).sub2Value")
                        .end()
        );

        // From => 4 instances
        // There are four map keys, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    public void selectTreatedRootEmbeddableManyToManyMapKeyEmbeddable() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
        );

        // From => 4 instances
        // Left join on KEY(b.embeddable1.sub1Map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }

    @Test
    public void selectMultipleTreatedRootEmbeddableManyToManyMapKeyEmbeddable() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .select("TREAT(KEY(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map) AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
        );

        // From => 4 instances
        // Left join on KEY(b.embeddable1.sub1Map) and KEY(b.embeddable2.sub2Map) => 4 instances
        // There are four map keys, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101 , null });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    public void selectTreatedParentRootEmbeddableManyToManyMapKeyEmbeddable() {
        assumeHibernateSupportsMapKeyTypeExpressionInSubquery();
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
        );
                
        // From => 4 instances
        // There are four map keys but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableManyToManyMapKeyEmbeddable() {
        assumeHibernateSupportsMapKeyTypeExpressionInSubquery();
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeQueryLanguageSupportsKeyDeReference();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(KEY(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map) AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
                        .end()
        );
                
        // From => 4 instances
        // There are four map keys, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
}
