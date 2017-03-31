/*
 * Copyright 2014 - 2017 Blazebit.
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

import com.blazebit.persistence.testsuite.treat.entity.IntIdEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class SelectManyToManyMapValueTest extends AbstractTreatVariationsTest {

    public SelectManyToManyMapValueTest(String strategy, String objectPrefix) {
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
    public void selectTreatedManyToManyMapValue() {
        assumeTreatMapAssociationIsSupported();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(b.map AS " + strategy + "Sub1).sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedManyToManyMapValue() {
        assumeTreatMapAssociationIsSupported();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(b.map AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(b.map AS " + strategy + "Sub2).sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentManyToManyMapValue() {
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(b.map AS " + strategy + "Sub1).sub1Value)")
                        .end()
        );
                
        // From => 4 instances
        // There are four map values but only two are Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2L);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentManyToManyMapValue() {
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(b.map AS " + strategy + "Sub1).sub1Value)")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(b.map AS " + strategy + "Sub2).sub2Value)")
                        .end()
        );
                
        // From => 4 instances
        // There are four map values, two are Sub1 and the other are Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2L,   null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 4L   });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
    @Test
    public void selectTreatedEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(b.embeddable.map AS " + strategy + "Sub1).sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(b.embeddable.map AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(b.embeddable.map AS " + strategy + "Sub2).sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(b.embeddable.map AS " + strategy + "Sub1).sub1Value)")
                        .end()
        );
                
        // From => 4 instances
        // There are four map values but only two are Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2L);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(b.embeddable.map AS " + strategy + "Sub1).sub1Value)")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(b.embeddable.map AS " + strategy + "Sub2).sub2Value)")
                        .end()
        );
                
        // From => 4 instances
        // There are four map values, two are Sub1 and the other are Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2L,   null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 4L   });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
    @Test
    public void selectTreatedEmbeddableManyToManyMapValueEmbeddable() {
        assumeMapInEmbeddableIsSupported();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(b.embeddable.map AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
        );
                
        // From => 4 instances
        // Left join on b.embeddable.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableManyToManyMapValueEmbeddable() {
        assumeMapInEmbeddableIsSupported();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(b.embeddable.map AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .select("TREAT(b.embeddable.map AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
        );
                
        // From => 4 instances
        // Left join on b.embeddable.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentEmbeddableManyToManyMapValueEmbeddable() {
        assumeMapInEmbeddableIsSupported();
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(b.embeddable.map AS " + strategy + "Sub1).embeddable1.sub1SomeValue)")
                        .end()
        );
                
        // From => 4 instances
        // There are four map values but only two are Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2L);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentEmbeddableManyToManyMapValueEmbeddable() {
        assumeMapInEmbeddableIsSupported();
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(b.embeddable.map AS " + strategy + "Sub1).embeddable1.sub1SomeValue)")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(b.embeddable.map AS " + strategy + "Sub2).embeddable2.sub2SomeValue)")
                        .end()
        );
                
        // From => 4 instances
        // There are four map values, two are Sub1 and the other are Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2L,   null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 4L   });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
    @Test
    public void selectTreatedRootManyToManyMapValue() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1).sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.map1 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootManyToManyMapValue() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub2).map2 AS " + strategy + "Sub2).sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.map1 and b.map2 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentRootManyToManyMapValue() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1).sub1Value)")
                        .end()
        );
                
        // From => 4 instances
        // There are two map values but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentRootManyToManyMapValue() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1).sub1Value)")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(TREAT(b AS " + strategy + "Sub2).map2 AS " + strategy + "Sub2).sub2Value)")
                        .end()
        );
                
        // From => 4 instances
        // There are two map values, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
    @Test
    public void selectTreatedRootEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable1.sub1Map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2).sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable1.sub1Map and b.embeddable2.sub2Map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).sub1Value)")
                        .end()
        );
                
        // From => 4 instances
        // There are two map values but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).sub1Value)")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2).sub2Value)")
                        .end()
        );
                
        // From => 4 instances
        // There are two map values, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
    @Test
    public void selectTreatedRootEmbeddableManyToManyMapValueEmbeddable() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
        );
                
        // From => 4 instances
        // Left join on b.embeddable1.sub1Map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableManyToManyMapValueEmbeddable() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
        );
                
        // From => 4 instances
        // Left join on b.embeddable1.sub1Map and b.embeddable2.sub2Map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableManyToManyMapValueEmbeddable() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).embeddable1.sub1SomeValue)")
                        .end()
        );
                
        // From => 4 instances
        // There are two map values but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableManyToManyMapValueEmbeddable() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).embeddable1.sub1SomeValue)")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("SUM(TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2).embeddable2.sub2SomeValue)")
                        .end()
        );
                
        // From => 4 instances
        // There are two map values, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
}
