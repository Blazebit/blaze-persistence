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

@RunWith(Parameterized.class)
public class SelectOneToManyInverseSetTest extends AbstractTreatVariationsTest {

    public SelectOneToManyInverseSetTest(String strategy, String objectPrefix) {
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
    public void selectTreatedOneToManyInverseSet() {
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(b.children AS " + strategy + "Sub1).sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedOneToManyInverseSet() {
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(b.children AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(b.children AS " + strategy + "Sub2).sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedParentOneToManyInverseSet() {
        assumeAccessTreatedOuterQueryVariableWorks();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.children AS " + strategy + "Sub1).sub1Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two children but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedParentOneToManyInverseSet() {
        assumeAccessTreatedOuterQueryVariableWorks();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.children AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.children AS " + strategy + "Sub2).sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedEmbeddableOneToManyInverseSet() {
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(b.embeddable.children AS " + strategy + "Sub1).sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableOneToManyInverseSet() {
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(b.embeddable.children AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(b.embeddable.children AS " + strategy + "Sub2).sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedParentEmbeddableOneToManyInverseSet() {
        assumeAccessTreatedOuterQueryVariableWorks();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.embeddable.children AS " + strategy + "Sub1).sub1Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two children but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedParentEmbeddableOneToManyInverseSet() {
        assumeAccessTreatedOuterQueryVariableWorks();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.embeddable.children AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.embeddable.children AS " + strategy + "Sub2).sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedEmbeddableOneToManyInverseSetEmbeddable() {
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(b.embeddable.children AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
        );
                
        // From => 4 instances
        // Left join on b.children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableOneToManyInverseSetEmbeddable() {
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(b.embeddable.children AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .select("TREAT(b.embeddable.children AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
        );
                
        // From => 4 instances
        // Left join on b.children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedParentEmbeddableOneToManyInverseSetEmbeddable() {
        assumeAccessTreatedOuterQueryVariableWorks();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.embeddable.children AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
        );
                
        // From => 4 instances
        // There are two children but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedParentEmbeddableOneToManyInverseSetEmbeddable() {
        assumeAccessTreatedOuterQueryVariableWorks();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.embeddable.children AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.embeddable.children AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
                        .end()
        );
                
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedRootOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1).sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.children1 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedRootOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub2).children2 AS " + strategy + "Sub2).sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.children1 and b.children2 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }

    @Test
    public void selectTreatedParentRootOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                        .select("i.value")
                        .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1).sub1Value")
                        .end()
        );
        
        // From => 4 instances
        // There are two children but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }

    @Test
    public void selectMultipleTreatedParentRootOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub2).children2 AS " + strategy + "Sub2).sub2Value")
                        .end()
        );
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }

    @Test
    public void selectTreatedRootEmbeddableOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).sub1Value")
        );
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1Children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }

    @Test
    public void selectMultipleTreatedRootEmbeddableOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2).sub2Value")
        );
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1Children and b.embeddable2.sub2Children => 4 instances
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }

    @Test
    public void selectTreatedParentRootEmbeddableOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).sub1Value")
                        .end()
        );
        
        // From => 4 instances
        // There are two children but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }

    @Test
    public void selectMultipleTreatedParentRootEmbeddableOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2).sub2Value")
                        .end()
        );
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }

    @Test
    public void selectTreatedRootEmbeddableOneToManyInverseSetEmbeddable() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
        );
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1Children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }

    @Test
    public void selectMultipleTreatedRootEmbeddableOneToManyInverseSetEmbeddable() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
        );
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1Children and b.embeddable2.sub2Children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableOneToManyInverseSetEmbeddable() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
        );
                
        // From => 4 instances
        // There are two children but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableOneToManyInverseSetEmbeddable() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
                        .end()
        );
                
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
}
