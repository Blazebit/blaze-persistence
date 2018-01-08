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

package com.blazebit.persistence.testsuite.treat.jpql;

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
        List<Integer> bases = list("SELECT " +
                "TREAT(b.children AS " + strategy + "Sub1).sub1Value" + 
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedOneToManyInverseSet() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(b.children AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(b.children AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedParentOneToManyInverseSet() {
        List<Integer> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.children AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentOneToManyInverseSet-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.children AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.children AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentOneToManyInverseSet-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(b.embeddable.children AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableOneToManyInverseSet() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(b.embeddable.children AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(b.embeddable.children AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedParentEmbeddableOneToManyInverseSet() {
        List<Integer> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.children AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentEmbeddableOneToManyInverseSet-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.children AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.children AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentEmbeddableOneToManyInverseSet-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(b.embeddable.children AS " + strategy + "Sub1).embeddable1.sub1SomeValue" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableOneToManyInverseSetEmbeddable() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(b.embeddable.children AS " + strategy + "Sub1).embeddable1.sub1SomeValue, " +
                "TREAT(b.embeddable.children AS " + strategy + "Sub2).embeddable2.sub2SomeValue" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedParentEmbeddableOneToManyInverseSetEmbeddable() {
        List<Integer> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.children AS " + strategy + "Sub1).embeddable1.sub1SomeValue)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.children AS " + strategy + "Sub1).embeddable1.sub1SomeValue), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.children AS " + strategy + "Sub2).embeddable2.sub2SomeValue)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children1 => 1 instance
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedRootOneToManyInverseSet() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(TREAT(b AS " + strategy + "Sub2).children2 AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children1 and b.children2 => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootOneToManyInverseSet() {
        List<Integer> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootOneToManyInverseSet-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub2).children2 AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootOneToManyInverseSet-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Children => 1 instance
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableOneToManyInverseSet() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Children and b.embeddable2.sub2Children => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableOneToManyInverseSet() {
        List<Integer> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableOneToManyInverseSet-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableOneToManyInverseSet-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).embeddable1.sub1SomeValue" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Children => 1 instance
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableOneToManyInverseSetEmbeddable() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).embeddable1.sub1SomeValue, " +
                "TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2).embeddable2.sub2SomeValue" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Children and b.embeddable2.sub2Children => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableOneToManyInverseSetEmbeddable() {
        List<Integer> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).embeddable1.sub1SomeValue)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).embeddable1.sub1SomeValue), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2).embeddable2.sub2SomeValue)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
}
