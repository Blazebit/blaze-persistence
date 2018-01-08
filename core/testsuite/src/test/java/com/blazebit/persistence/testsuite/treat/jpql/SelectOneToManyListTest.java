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
        List<Integer> bases = list("SELECT " +
                "TREAT(b.list AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedOneToManyList-" + strategy);
        
        // From => 4 instances
        // Inner join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedOneToManyList() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(b.list AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(b.list AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedOneToManyList-" + strategy);
        
        // From => 4 instances
        // Inner join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentOneToManyList() {
        List<Integer> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.list AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentOneToManyList-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.list AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.list AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentOneToManyList-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(b.embeddable.list AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // Inner join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableOneToManyList() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(b.embeddable.list AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(b.embeddable.list AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // Inner join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentEmbeddableOneToManyList() {
        List<Integer> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.list AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentEmbeddableOneToManyList-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.list AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.list AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentEmbeddableOneToManyList-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(b.embeddable.list AS " + strategy + "Sub1).embeddable1.sub1SomeValue" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedEmbeddableOneToManyListEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableOneToManyListEmbeddable() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(b.embeddable.list AS " + strategy + "Sub1).embeddable1.sub1SomeValue, " +
                "TREAT(b.embeddable.list AS " + strategy + "Sub2).embeddable2.sub2SomeValue" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedEmbeddableOneToManyListEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentEmbeddableOneToManyListEmbeddable() {
        List<Integer> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.list AS " + strategy + "Sub1).embeddable1.sub1SomeValue)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentEmbeddableOneToManyListEmbeddable-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.list AS " + strategy + "Sub1).embeddable1.sub1SomeValue), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.list AS " + strategy + "Sub2).embeddable2.sub2SomeValue)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentEmbeddableOneToManyListEmbeddable-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootOneToManyList-" + strategy);
        
        // From => 4 instances
        // Inner join on b.list1 => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootOneToManyList() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(TREAT(b AS " + strategy + "Sub2).list2 AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootOneToManyList-" + strategy);
        
        // From => 4 instances
        // Inner join on b.list1 and b.list2 => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootOneToManyList() {
        List<Integer> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootOneToManyList-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub2).list2 AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootOneToManyList-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1List => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableOneToManyList() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1List and b.embeddable2.sub2List => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableOneToManyList() {
        List<Integer> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableOneToManyList-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableOneToManyList-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).embeddable1.sub1SomeValue" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableOneToManyListEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1List => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableOneToManyListEmbeddable() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).embeddable1.sub1SomeValue, " +
                "TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2).embeddable2.sub2SomeValue" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableOneToManyListEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1List and b.embeddable2.sub2List => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableOneToManyListEmbeddable() {
        List<Integer> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).embeddable1.sub1SomeValue)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableOneToManyListEmbeddable-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " + 
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1).embeddable1.sub1SomeValue), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" + 
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2).embeddable2.sub2SomeValue)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableOneToManyListEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are two list elements, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
}
