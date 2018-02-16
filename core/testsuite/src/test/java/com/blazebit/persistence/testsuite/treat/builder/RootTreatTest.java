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
public class RootTreatTest extends AbstractTreatVariationsTest {

    public RootTreatTest(String strategy, String objectPrefix) {
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
    public void selectTreatedRootBasic() {
        assumeTreatInNonPredicateDoesNotFilter();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(b AS " + strategy + "Sub1).sub1Value")
        );
                
        // From => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootBasic() {
        assumeTreatInNonPredicateDoesNotFilter();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(b AS " + strategy + "Sub1).sub1Value")
                        .select("TREAT(b AS " + strategy + "Sub2).sub2Value")
        );
                
        // From => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentRootBasic() {
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b AS " + strategy + "Sub1).sub1Value")
                        .end()
        );
                
        // From => 4 instances
        // There are 2 IntIdEntity instances per Base instance
        // For the 2 Sub1 instances, their sub1Value is returned
        // For the 2 Sub2 instances, null is emmitted
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentRootBasic() {
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b AS " + strategy + "Sub1).sub1Value")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b AS " + strategy + "Sub2).sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are 2 IntIdEntity instances per Base instance
        // For the 2 Sub1 instances, their sub1Value is returned
        // For the 2 Sub2 instances, null is emmitted
        // The second subquery is like the first but for Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedRootEmbeddableBasic() {
        assumeTreatInNonPredicateDoesNotFilter();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .select("TREAT(b AS " + strategy + "Sub1).sub1Embeddable.someValue")
        );
                
        // From => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableBasic() {
        assumeTreatInNonPredicateDoesNotFilter();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .select("TREAT(b AS " + strategy + "Sub1).sub1Embeddable.someValue")
                        .select("TREAT(b AS " + strategy + "Sub2).sub2Embeddable.someValue")
        );
                
        // From => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableBasic() {
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
        );
                
        // From => 4 instances
        // There are 2 IntIdEntity instances per Base instance
        // For the 2 Sub1 instances, their sub1SomeValue is returned
        // For the 2 Sub2 instances, null is emmitted
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableBasic() {
        assumeAccessTreatedOuterQueryVariableWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue")
                        .end()
                        .selectSubquery()
                            .from(IntIdEntity.class, "i")
                            .where("i.name").eqExpression("b.name")
                            .select("i.value")
                            .where("i.value").eqExpression("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2SomeValue")
                        .end()
        );
                
        // From => 4 instances
        // There are 2 IntIdEntity instances per Base instance
        // For the 2 Sub1 instances, their sub1SomeValue is returned
        // For the 2 Sub2 instances, null is emmitted
        // The second subquery is like the first but for Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void whereTreatedRootBasic() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .where("COALESCE(TREAT(b AS " + strategy + "Sub1).sub1Value, 0)").ltExpression("100")
        );
                
        // From => 4 instances
        // Where => 1 instance because 1 Sub1 has sub1Value 101, the others are Sub2s
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, objectPrefix + "1");
    }

    @Test
    public void whereTreatedRootBasicOr() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .whereOr()
                            .where("TYPE(b)").notEqExpression(strategy + "Sub1")
                            .where("COALESCE(TREAT(b AS " + strategy + "Sub1).sub1Value, 0)").ltExpression("100")
                        .endOr()
        );
        
        // From => 4 instances
        // Where => 3 instance because 1 Sub1 has sub1Value 101, the others are Sub2s
        Assert.assertEquals(3, bases.size());
        assertRemoved(bases, objectPrefix + "2.parent");
        assertRemoved(bases, objectPrefix + "1");
        assertRemoved(bases, objectPrefix + "2");
    }
    
    @Test
    public void whereMultipleTreatedRootBasic() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .where("COALESCE(TREAT(b AS " + strategy + "Sub1).sub1Value, 0)").ltExpression("100")
                        .where("COALESCE(TREAT(b AS " + strategy + "Sub2).sub2Value, 0)").ltExpression("100")
        );
                
        // From => 4 instances
        // Where => 0 instances because one variable can't be treated with 2 different subtypes in a single AND predicate
        Assert.assertEquals(0, bases.size());
    }

    @Test
    public void whereMultipleTreatedRootBasicOr() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .whereOr()
                            .where("TYPE(b)").notEqExpression(strategy + "Sub1")
                            .where("COALESCE(TREAT(b AS " + strategy + "Sub1).sub1Value, 0)").ltExpression("100")
                        .endOr()
                        .whereOr()
                            .where("TYPE(b)").notEqExpression(strategy + "Sub2")
                            .where("COALESCE(TREAT(b AS " + strategy + "Sub2).sub2Value, 0)").ltExpression("100")
                        .endOr()
        );
        
        // From => 4 instances
        // Where => 2 instances because 1 Sub1 has sub1Value 101, the other has 1
        // 1 Sub2 has sub2Value 102, the other has 2
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, objectPrefix + "1");
        assertRemoved(bases, objectPrefix + "2");
    }

    @Test
    public void whereTreatedRootEmbeddableBasic() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .where("COALESCE(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue, 0)").ltExpression("100")
        );
        
        // From => 4 instances
        // Where => 1 instance because 1 Sub1 has sub1Value 101, the others are Sub2s
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, objectPrefix + "1");
    }
    
    @Test
    public void whereTreatedRootEmbeddableBasicOr() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .whereOr()
                            .where("TYPE(b)").notEqExpression(strategy + "Sub1")
                            .where("COALESCE(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue, 0)").ltExpression("100")
                        .endOr()
        );
                
        // From => 4 instances
        // Where => 3 instances because 1 Sub1 has sub1Value 101, the other has 1 and Sub2s use 0 because of coalesce
        Assert.assertEquals(3, bases.size());
        assertRemoved(bases, objectPrefix + "2.parent");
        assertRemoved(bases, objectPrefix + "1");
        assertRemoved(bases, objectPrefix + "2");
    }
    
    @Test
    public void whereMultipleTreatedRootEmbeddableBasic() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .where("COALESCE(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue, 0)").ltExpression("100")
                        .where("COALESCE(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2SomeValue, 0)").ltExpression("100")
        );
                
        // From => 4 instances
        // Where => 0 instances because one variable can't be treated with 2 different subtypes in a single AND predicate
        Assert.assertEquals(0, bases.size());
    }

    @Test
    public void whereMultipleTreatedRootEmbeddableBasicOr() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .whereOr()
                            .where("TYPE(b)").notEqExpression(strategy + "Sub1")
                            .where("COALESCE(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue, 0)").ltExpression("100")
                        .endOr()
                        .whereOr()
                            .where("TYPE(b)").notEqExpression(strategy + "Sub2")
                            .where("COALESCE(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2SomeValue, 0)").ltExpression("100")
                        .endOr()
        );
        
        // From => 4 instances
        // Where => 2 instances because 1 Sub1 has sub1SomeValue 101, the other has 1
        // 1 Sub2 has sub2SomeValue 102, the other has 2
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, objectPrefix + "1");
        assertRemoved(bases, objectPrefix + "2");
    }
    
    @Test
    public void whereTreatedRootConditionBasic() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .where("TREAT(b AS " + strategy + "Sub1).sub1Value").eqExpression("101")
        );
                
        // From => 4 instances
        // Where => 1 instance because 1 Sub1 has sub1Value 101
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, objectPrefix + "1.parent");
    }
    
    @Test
    public void whereMultipleTreatedRootConditionBasic() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .whereOr()
                            .where("TREAT(b AS " + strategy + "Sub1).sub1Value").eqExpression("101")
                            .where("TREAT(b AS " + strategy + "Sub2).sub2Value").eqExpression("102")
                        .endOr()

        );
                
        // From => 4 instances
        // Where => 2 instances because 1 Sub1 has sub1Value 101 and 1 Sub2 has sub2Value 102
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, objectPrefix + "1.parent");
        assertRemoved(bases, objectPrefix + "2.parent");
    }
    
    @Test
    public void whereTreatedRootConditionEmbeddableBasic() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .where("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue").eqExpression("101")
        );
                
        // From => 4 instances
        // Where => 1 instance because 1 Sub1 has sub1SomeValue 101
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, objectPrefix + "1.parent");
    }
    
    @Test
    public void whereMultipleTreatedRootConditionEmbeddableBasic() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .whereOr()
                            .where("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue").eqExpression("101")
                            .where("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2SomeValue").eqExpression("102")
                        .endOr()

        );
                
        // From => 4 instances
        // Where => 2 instances because 1 Sub1 has sub1SomeValue 101 and 1 Sub2 has sub2SomeValue 102
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, objectPrefix + "1.parent");
        assertRemoved(bases, objectPrefix + "2.parent");
    }
    
    @Test
    public void whereTreatedRootConditionNegated() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .setWhereExpression("NOT(TREAT(b AS " + strategy + "Sub1).sub1Value = 101)")
        );

        // From => 4 instances
        // Where => 3 instances because 1 Sub1 has sub1Value 101, the other has 1 and Sub2s are included because type constraint is inverted
        Assert.assertEquals(3, bases.size());
        assertRemoved(bases, objectPrefix + "1");
        assertRemoved(bases, objectPrefix + "2");
        assertRemoved(bases, objectPrefix + "2.parent");
    }
    
    @Test
    public void whereMultipleTreatedRootConditionNegated() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .setWhereExpression("NOT(TREAT(b AS " + strategy + "Sub1).sub1Value = 101) AND NOT(TREAT(b AS " + strategy + "Sub2).sub2Value = 102)")
        );
                
        // From => 4 instances
        // Where => 2 instances because 1 Sub1 has sub1Value 101 and 1 Sub2 has sub2Value 102
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, objectPrefix + "1");
        assertRemoved(bases, objectPrefix + "2");
    }
    
    @Test
    public void whereTreatedRootConditionSuperTypeAccess() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .where("TREAT(b AS " + strategy + "Sub1).value").gtExpression("100")
        );
                
        // From => 4 instances
        // Where => 1 instance because 1 Sub1 has value 101, the other has 1 and Sub2s are excluded because of type constraint
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, objectPrefix + "1.parent");
    }
    
    @Test
    public void whereMultipleTreatedRootConditionSuperTypeAccess() {
        List<String> bases = list(
                from(String.class, "Base", "b")
                        .select("b.name")
                        .whereOr()
                            .where("TREAT(b AS " + strategy + "Sub1).value").gtExpression("100")
                            .where("TREAT(b AS " + strategy + "Sub2).value").ltExpression("100")
                        .endOr()
        );
                
        // From => 4 instances
        // Where => 2 instances because 1 Sub1 has value 101 and 1 Sub2 has value 102 the has value 1
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, objectPrefix + "1.parent");
        assertRemoved(bases, objectPrefix + "2");
    }
    
}
