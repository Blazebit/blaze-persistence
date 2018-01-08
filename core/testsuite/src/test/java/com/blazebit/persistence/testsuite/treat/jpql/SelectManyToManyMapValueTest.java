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
        List<Integer> bases = list("SELECT " +
                "TREAT(b.map AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Inner join on b.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedManyToManyMapValue() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(b.map AS " + strategy + "Sub1).sub1Value, TREAT(b.map AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Inner join on b.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentManyToManyMapValue() {
        List<Integer> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.map AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are four map values but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentManyToManyMapValue() {
        List<Object[]> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.map AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.map AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are four map values, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedEmbeddableManyToManyMapValue() {
        List<Integer> bases = list("SELECT " +
                "TREAT(b.embeddable.map AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableManyToManyMapValue() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(b.embeddable.map AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(b.embeddable.map AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentEmbeddableManyToManyMapValue() {
        List<Integer> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.map AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are four map values but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentEmbeddableManyToManyMapValue() {
        List<Object[]> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.map AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.map AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are four map values, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedEmbeddableManyToManyMapValueEmbeddable() {
        List<Integer> bases = list("SELECT " +
                "TREAT(b.embeddable.map AS " + strategy + "Sub1).embeddable1.sub1SomeValue" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedEmbeddableManyToManyMapValueEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableManyToManyMapValueEmbeddable() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(b.embeddable.map AS " + strategy + "Sub1).embeddable1.sub1SomeValue, " +
                "TREAT(b.embeddable.map AS " + strategy + "Sub2).embeddable2.sub2SomeValue" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedEmbeddableManyToManyMapValueEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentEmbeddableManyToManyMapValueEmbeddable() {
        List<Integer> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.map AS " + strategy + "Sub1).embeddable1.sub1SomeValue)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentEmbeddableManyToManyMapValueEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are four map values but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentEmbeddableManyToManyMapValueEmbeddable() {
        List<Object[]> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.map AS " + strategy + "Sub1).embeddable1.sub1SomeValue), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(b.embeddable.map AS " + strategy + "Sub2).embeddable2.sub2SomeValue)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentEmbeddableManyToManyMapValueEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are four map values, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedRootManyToManyMapValue() {
        List<Integer> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Inner join on b.map1 => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootManyToManyMapValue() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(TREAT(b AS " + strategy + "Sub2).map2 AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Inner join on b.map1 and b.map2 => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootManyToManyMapValue() {
        List<Integer> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are two map values but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentRootManyToManyMapValue() {
        List<Object[]> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub2).map2 AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are two map values, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedRootEmbeddableManyToManyMapValue() {
        List<Integer> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Map => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableManyToManyMapValue() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Map and b.embeddable2.sub2Map => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableManyToManyMapValue() {
        List<Integer> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are two map values but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableManyToManyMapValue() {
        List<Object[]> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are two map values, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedRootEmbeddableManyToManyMapValueEmbeddable() {
        List<Integer> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).embeddable1.sub1SomeValue" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableManyToManyMapValueEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Map => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableManyToManyMapValueEmbeddable() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).embeddable1.sub1SomeValue, " +
                "TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2).embeddable2.sub2SomeValue" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableManyToManyMapValueEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Map and b.embeddable2.sub2Map => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableManyToManyMapValueEmbeddable() {
        List<Integer> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).embeddable1.sub1SomeValue)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableManyToManyMapValueEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are two map values but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableManyToManyMapValueEmbeddable() {
        List<Object[]> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1).embeddable1.sub1SomeValue), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2).embeddable2.sub2SomeValue)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableManyToManyMapValueEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are two map values, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
}
