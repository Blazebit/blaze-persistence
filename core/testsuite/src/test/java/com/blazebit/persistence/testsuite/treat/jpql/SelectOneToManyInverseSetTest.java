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
        List<Integer> bases = list("SELECT TREAT(b.children AS " + strategy + "Sub1).sub1Value FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedOneToManyInverseSet() {
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
        List<Object[]> bases = list("SELECT TREAT(b.children AS " + strategy + "Sub1).sub1Value, TREAT(b.children AS " + strategy + "Sub2).sub2Value FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedParentOneToManyInverseSet() {
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
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(b.children AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2L);
    }
    
    @Test
    public void selectMultipleTreatedParentOneToManyInverseSet() {
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
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(b.children AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(b.children AS " + strategy + "Sub2).sub2Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 2L,   null });
        assertRemoved(bases, new Object[] { null, 4L   });
    }
    
    @Test
    public void selectTreatedEmbeddableOneToManyInverseSet() {
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
        List<Integer> bases = list("SELECT TREAT(b.embeddable.children AS " + strategy + "Sub1).sub1Value FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableOneToManyInverseSet() {
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
        List<Object[]> bases = list("SELECT TREAT(b.embeddable.children AS " + strategy + "Sub1).sub1Value, TREAT(b.embeddable.children AS " + strategy + "Sub2).sub2Value FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedParentEmbeddableOneToManyInverseSet() {
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
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(b.embeddable.children AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2L);
    }
    
    @Test
    public void selectMultipleTreatedParentEmbeddableOneToManyInverseSet() {
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
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(b.embeddable.children AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(b.embeddable.children AS " + strategy + "Sub2).sub2Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 2L,   null });
        assertRemoved(bases, new Object[] { null, 4L   });
    }
    
    @Test
    public void selectTreatedEmbeddableOneToManyInverseSetEmbeddable() {
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
        List<Integer> bases = list("SELECT TREAT(b.embeddable.children AS " + strategy + "Sub1).embeddable1.sub1SomeValue FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedEmbeddableOneToManyInverseSetEmbeddable() {
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
        List<Object[]> bases = list("SELECT TREAT(b.embeddable.children AS " + strategy + "Sub1).embeddable1.sub1SomeValue, TREAT(b.embeddable.children AS " + strategy + "Sub2).embeddable2.sub2SomeValue FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void selectTreatedParentEmbeddableOneToManyInverseSetEmbeddable() {
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
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(b.embeddable.children AS " + strategy + "Sub1).embeddable1.sub1SomeValue) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are two children but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2L);
    }
    
    @Test
    public void selectMultipleTreatedParentEmbeddableOneToManyInverseSetEmbeddable() {
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
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(b.embeddable.children AS " + strategy + "Sub1).embeddable1.sub1SomeValue) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(b.embeddable.children AS " + strategy + "Sub2).embeddable2.sub2SomeValue) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 2L,   null });
        assertRemoved(bases, new Object[] { null, 4L   });
    }
    
    @Test
    public void selectTreatedRootOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, can't resolve collection type
        // - SingleTable   : not working, can't resolve collection type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1).sub1Value FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children1 => 1 instance
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedRootOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, can't resolve collection type
        // - SingleTable   : not working, can't resolve collection type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1).sub1Value, TREAT(TREAT(b AS " + strategy + "Sub2).children2 AS " + strategy + "Sub2).sub2Value FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.children1 and b.children2 => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, can't resolve collection type
        // - SingleTable   : not working, can't resolve collection type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2L);
    }
    
    @Test
    public void selectMultipleTreatedParentRootOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, can't resolve collection type
        // - SingleTable   : not working, can't resolve collection type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub2).children2 AS " + strategy + "Sub2).sub2Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 2L,   null });
        assertRemoved(bases, new Object[] { null, 4L   });
    }
    
    @Test
    public void selectTreatedRootEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, can't resolve collection type
        // - SingleTable   : not working, can't resolve collection type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).sub1Value FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Children => 1 instance
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, can't resolve collection type
        // - SingleTable   : not working, can't resolve collection type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).sub1Value, TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2).sub2Value FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Children and b.embeddable2.sub2Children => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, can't resolve collection type
        // - SingleTable   : not working, can't resolve collection type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2L);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, can't resolve collection type
        // - SingleTable   : not working, can't resolve collection type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2).sub2Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 2L,   null });
        assertRemoved(bases, new Object[] { null, 4L   });
    }
    
    @Test
    public void selectTreatedRootEmbeddableOneToManyInverseSetEmbeddable() {
        // EclipseLink
        // - Joined        : not working, can't resolve collection type
        // - SingleTable   : not working, can't resolve collection type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).embeddable1.sub1SomeValue FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Children => 1 instance
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, 1);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableOneToManyInverseSetEmbeddable() {
        // EclipseLink
        // - Joined        : not working, can't resolve collection type
        // - SingleTable   : not working, can't resolve collection type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).embeddable1.sub1SomeValue, TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2).embeddable2.sub2SomeValue FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // Inner join on b.embeddable1.sub1Children and b.embeddable2.sub2Children => 0 instances
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableOneToManyInverseSetEmbeddable() {
        // EclipseLink
        // - Joined        : not working, can't resolve collection type
        // - SingleTable   : not working, can't resolve collection type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).embeddable1.sub1SomeValue) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are two children but only one is Sub1
        // The sub1Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2L);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableOneToManyInverseSetEmbeddable() {
        // EclipseLink
        // - Joined        : not working, can't resolve collection type
        // - SingleTable   : not working, can't resolve collection type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1).embeddable1.sub1SomeValue) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2).embeddable2.sub2SomeValue) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableOneToManyInverseSetEmbeddable-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        // The sub1Value and sub2Value is doubled
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 2L,   null });
        assertRemoved(bases, new Object[] { null, 4L   });
    }
    
}
