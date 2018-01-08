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
public class SelectOneToManyListTest extends AbstractTreatVariationsTest {

    public SelectOneToManyListTest(String strategy, String objectPrefix) {
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
    public void selectTreatedOneToManyList() {
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(b.list AS " + strategy + "Sub1).sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedOneToManyList() {
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(b.list AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(b.list AS " + strategy + "Sub2).sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.list AS " + strategy + "Sub1).sub1Value")
                        .end()
        );
                
        // From => 4 instances
        // There are four list elements but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.list AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.list AS " + strategy + "Sub2).sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are four list elements, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedEmbeddableOneToManyList() {
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(b.embeddable.list AS " + strategy + "Sub1).sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableOneToManyList() {
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(b.embeddable.list AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(b.embeddable.list AS " + strategy + "Sub2).sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentEmbeddableOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.embeddable.list AS " + strategy + "Sub1).sub1Value")
                        .end()
        );
                
        // From => 4 instances
        // There are four list elements but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentEmbeddableOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.embeddable.list AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.embeddable.list AS " + strategy + "Sub2).sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are four list elements, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedEmbeddableOneToManyListEmbeddable() {
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(b.embeddable.list AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
        );
                
        // From => 4 instances
        // Left join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableOneToManyListEmbeddable() {
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(b.embeddable.list AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .select("TREAT(b.embeddable.list AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
        );
                
        // From => 4 instances
        // Left join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentEmbeddableOneToManyListEmbeddable() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.embeddable.list AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
        );
                
        // From => 4 instances
        // There are four list elements but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentEmbeddableOneToManyListEmbeddable() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.embeddable.list AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b.embeddable.list AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
                        .end()
        );
                
        // From => 4 instances
        // There are four list elements, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedRootOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1).sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.list1 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub2).list2 AS " + strategy + "Sub2).sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.list1 and b.list2 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentRootOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1).sub1Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two list elements but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentRootOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub2).list2 AS " + strategy + "Sub2).sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two list elements, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedRootEmbeddableOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable1.sub1List => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2).sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable1.sub1List and b.embeddable2.sub2List => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).sub1Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two list elements but only is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2).sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two list elements, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedRootEmbeddableOneToManyListEmbeddable() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
        );
                
        // From => 4 instances
        // Left join on b.embeddable1.sub1List => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableOneToManyListEmbeddable() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .select("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
        );
                
        // From => 4 instances
        // Left join on b.embeddable1.sub1List and b.embeddable2.sub2List => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableOneToManyListEmbeddable() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
        );
                
        // From => 4 instances
        // There are two list elements but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableOneToManyListEmbeddable() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
                        .end()
        );
                
        // From => 4 instances
        // There are two list elements, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
}
