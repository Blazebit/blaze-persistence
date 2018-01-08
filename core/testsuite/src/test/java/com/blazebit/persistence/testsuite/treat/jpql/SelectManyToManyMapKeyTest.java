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
        List<Integer> bases = list("SELECT " +
                "TREAT(KEY(b.map) AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedManyToManyMapKey-" + strategy);
        
        // From => 4 instances
        // Inner join on KEY(b.map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedManyToManyMapKey() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(KEY(b.map) AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(KEY(b.map) AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedManyToManyMapKey-" + strategy);
        
        // From => 4 instances
        // Inner join on KEY(b.map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentManyToManyMapKey() {
        List<Integer> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(b.map) AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentManyToManyMapKey-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(b.map) AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(b.map) AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentManyToManyMapKey-" + strategy);
        
        // From => 4 instances
        // There are four map keys, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedEmbeddableManyToManyMapKey() {
        List<Integer> bases = list("SELECT TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedEmbeddableManyToManyMapKey-" + strategy);
        
        // From => 4 instances
        // Inner join on KEY(b.embeddable.map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableManyToManyMapKey() {
        List<Object[]> bases = list("SELECT TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedEmbeddableManyToManyMapKey-" + strategy);
        
        // From => 4 instances
        // Inner join on KEY(b.embeddable.map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentEmbeddableManyToManyMapKey() {
        List<Integer> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentEmbeddableManyToManyMapKey-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentEmbeddableManyToManyMapKey-" + strategy);
        
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
        List<Integer> bases = list("SELECT TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedEmbeddableManyToManyMapKeyEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on KEY(b.embeddable.map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableManyToManyMapKeyEmbeddable() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue, " +
                "TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub2).embeddable2.sub2SomeValue" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedEmbeddableManyToManyMapKeyEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on KEY(b.embeddable.map) => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentEmbeddableManyToManyMapKeyEmbeddable() {
        List<Integer> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentEmbeddableManyToManyMapKeyEmbeddable-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(b.embeddable.map) AS " + strategy + "Sub2).embeddable2.sub2SomeValue)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentEmbeddableManyToManyMapKeyEmbeddable-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(KEY(TREAT(b AS " + strategy + "Sub1).map1) AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootManyToManyMapKey-" + strategy);
        
        // From => 4 instances
        // Inner join on KEY(b.map1) => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootManyToManyMapKey() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(KEY(TREAT(b AS " + strategy + "Sub1).map1) AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(KEY(TREAT(b AS " + strategy + "Sub2).map2) AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootManyToManyMapKey-" + strategy);
        
        // From => 4 instances
        // Inner join on KEY(b.map1) and KEY(b.map2) => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootManyToManyMapKey() {
        List<Integer> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(TREAT(b AS " + strategy + "Sub1).map1) AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootManyToManyMapKey-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(TREAT(b AS " + strategy + "Sub1).map1) AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(TREAT(b AS " + strategy + "Sub2).map2) AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootManyToManyMapKey-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).sub1Value" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableManyToManyMapKey-" + strategy);
        
        // From => 4 instances
        // Inner join on KEY(b.embeddable1.sub1Map) => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableManyToManyMapKey() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).sub1Value, " +
                "TREAT(KEY(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map) AS " + strategy + "Sub2).sub2Value" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableManyToManyMapKey-" + strategy);
        
        // From => 4 instances
        // Inner join on KEY(b.embeddable1.sub1Map) and KEY(b.embeddable2.sub2Map) => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableManyToManyMapKey() {
        List<Integer> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).sub1Value)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableManyToManyMapKey-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).sub1Value), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map) AS " + strategy + "Sub2).sub2Value)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableManyToManyMapKey-" + strategy);
        
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
        List<Integer> bases = list("SELECT " +
                "TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableManyToManyMapKeyEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on KEY(b.embeddable1.sub1Map) => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableManyToManyMapKeyEmbeddable() {
        List<Object[]> bases = list("SELECT " +
                "TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue, " +
                "TREAT(KEY(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map) AS " + strategy + "Sub2).embeddable2.sub2SomeValue" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableManyToManyMapKeyEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on KEY(b.embeddable1.sub1Map) and KEY(b.embeddable2.sub2Map) => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableManyToManyMapKeyEmbeddable() {
        List<Integer> bases = list("SELECT " +
                "(SELECT SUM(TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue)" +
                " FROM IntIdEntity i WHERE i.name = b.name)" +
                " FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableManyToManyMapKeyEmbeddable-" + strategy);
        
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
        List<Object[]> bases = list("SELECT " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map) AS " + strategy + "Sub1).embeddable1.sub1SomeValue), " +
                "(SELECT i.value" +
                " FROM IntIdEntity i" +
                " WHERE i.name = b.name" +
                " AND i.value = TREAT(KEY(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map) AS " + strategy + "Sub2).embeddable2.sub2SomeValue)" +
                " FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableManyToManyMapKeyEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are four map keys, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
}
