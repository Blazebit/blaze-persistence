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

package com.blazebit.persistence.testsuite.treat.jpql;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class SelectManyToOneTest extends AbstractTreatVariationsTest {

    public SelectManyToOneTest(String strategy, String objectPrefix) {
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
    public void selectTreatedManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because filters subtype
        // - SingleTable   : issues 1 query, FAILS because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT TREAT(b.parent AS " + strategy + "Sub1).sub1Value FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedManyToOne-" + strategy);
        
        // From => 4 instances
        // Inner join on b.parent => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because filters subtype
        // - SingleTable   : issues 1 query, FAILS because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT TREAT(b.parent AS " + strategy + "Sub1).sub1Value, TREAT(b.parent AS " + strategy + "Sub2).sub2Value FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedManyToOne-" + strategy);
        
        // From => 4 instances
        // Inner join on b.parent => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, all successful
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(b.parent AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentManyToOne-" + strategy);
        
        // From => 4 instances
        // There are two parents but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, FAILS because query is completely wrong
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(b.parent AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(b.parent AS " + strategy + "Sub2).sub2Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentManyToOne-" + strategy);
        
        // From => 4 instances
        // There are two parents, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
    @Test
    public void selectTreatedEmbeddableManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because filters subtype
        // - SingleTable   : issues 1 query, FAILS because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT TREAT(b.embeddable.parent AS " + strategy + "Sub1).sub1Value FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedEmbeddableManyToOne-" + strategy);
        
        // From => 4 instances
        // Inner join on b.parent => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because filters subtype
        // - SingleTable   : issues 1 query, FAILS because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT TREAT(b.embeddable.parent AS " + strategy + "Sub1).sub1Value, TREAT(b.embeddable.parent AS " + strategy + "Sub2).sub2Value FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedEmbeddableManyToOne-" + strategy);
        
        // From => 4 instances
        // Inner join on b.parent => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentEmbeddableManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, all successful
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(b.embeddable.parent AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentEmbeddableManyToOne-" + strategy);
        
        // From => 4 instances
        // There are two parents but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentEmbeddableManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, FAILS because query is completely wrong
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(b.embeddable.parent AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(b.embeddable.parent AS " + strategy + "Sub2).sub2Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentEmbeddableManyToOne-" + strategy);
        
        // From => 4 instances
        // There are two parents, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
    @Test
    public void selectTreatedEmbeddableManyToOneEmbeddable() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because filters subtype
        // - SingleTable   : issues 1 query, FAILS because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT TREAT(b.embeddable.parent AS " + strategy + "Sub1).embeddable1.sub1SomeValue FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedEmbeddableManyToOneEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.parent => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableManyToOneEmbeddable() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because filters subtype
        // - SingleTable   : issues 1 query, FAILS because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT TREAT(b.embeddable.parent AS " + strategy + "Sub1).embeddable1.sub1SomeValue, TREAT(b.embeddable.parent AS " + strategy + "Sub2).embeddable2.sub2SomeValue FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedEmbeddableManyToOneEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.parent => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentEmbeddableManyToOneEmbeddable() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, all successful
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(b.embeddable.parent AS " + strategy + "Sub1).embeddable1.sub1SomeValue) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentEmbeddableManyToOneEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are two parents but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentEmbeddableManyToOneEmbeddable() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, FAILS because query is completely wrong
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(b.embeddable.parent AS " + strategy + "Sub1).embeddable1.sub1SomeValue) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(b.embeddable.parent AS " + strategy + "Sub2).embeddable2.sub2SomeValue) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentEmbeddableManyToOneEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are two parents, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
    @Test
    public void selectTreatedRootManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, all successful
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT TREAT(TREAT(b AS " + strategy + "Sub1).parent1 AS " + strategy + "Sub1).sub1Value FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootManyToOne-" + strategy);
        
        // From => 4 instances
        // Inner join on b.parent1 => 1 instance
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, all successful
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT TREAT(TREAT(b AS " + strategy + "Sub1).parent1 AS " + strategy + "Sub1).sub1Value, TREAT(TREAT(b AS " + strategy + "Sub2).parent2 AS " + strategy + "Sub2).sub2Value FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootManyToOne-" + strategy);
        
        // From => 4 instances
        // Inner join on b.parent1 and b.parent2 => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, successful BUT query is completely wrong
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub1).parent1 AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootManyToOne-" + strategy);
        
        // From => 4 instances
        // There are two parents but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentRootManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, successful BUT query is completely wrong
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub1).parent1 AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub2).parent2 AS " + strategy + "Sub2).sub2Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootManyToOne-" + strategy);
        
        // From => 4 instances
        // There are two parents, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
    @Test
    public void selectTreatedRootEmbeddableManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, all successful
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1).sub1Value FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableManyToOne-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Parent => 1 instance
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, all successful
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1).sub1Value, TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Parent AS " + strategy + "Sub2).sub2Value FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableManyToOne-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Parent and b.embeddable2.sub2Parent => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, successful BUT query is completely wrong
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableManyToOne-" + strategy);
        
        // From => 4 instances
        // There are two parents but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableManyToOne() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, successful BUT query is completely wrong
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Parent AS " + strategy + "Sub2).sub2Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableManyToOne-" + strategy);
        
        // From => 4 instances
        // There are two parents, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
    @Test
    public void selectTreatedRootEmbeddableManyToOneEmbeddable() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, all successful
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1).embeddable1.sub1SomeValue FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableManyToOneEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Parent => 1 instance
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableManyToOneEmbeddable() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, all successful
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1).embeddable1.sub1SomeValue, TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Parent AS " + strategy + "Sub2).embeddable2.sub2SomeValue FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableManyToOneEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Parent and b.embeddable2.sub1Parent => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableManyToOneEmbeddable() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, successful BUT query is completely wrong
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1).embeddable1.sub1SomeValue) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableManyToOneEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are two parents but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableManyToOneEmbeddable() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, successful BUT query is completely wrong
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1).embeddable1.sub1SomeValue) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Parent AS " + strategy + "Sub2).embeddable2.sub2SomeValue) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableManyToOneEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are two parents, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
}
