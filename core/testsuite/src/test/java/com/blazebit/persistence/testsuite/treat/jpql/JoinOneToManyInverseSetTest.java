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
public class JoinOneToManyInverseSetTest extends AbstractTreatVariationsTest {

    public JoinOneToManyInverseSetTest(String strategy, String objectPrefix) {
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
    public void treatJoinOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, FAILS because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT s1.sub1Value FROM " + strategy + "Base b LEFT JOIN TREAT(b.children AS " + strategy + "Sub1) s1", Integer.class);
        System.out.println("treatJoinOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Left join on b.children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void treatJoinMultipleOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, FAILS because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT s1.sub1Value, s2.sub2Value FROM " + strategy + "Base b LEFT JOIN TREAT(b.children AS " + strategy + "Sub1) s1 LEFT JOIN TREAT(b.children AS " + strategy + "Sub2) s2", Object[].class);
        System.out.println("treatJoinMultipleOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Left join on b.children => 4 instances
        // Left join on b.children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void treatJoinParentOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(b.children AS " + strategy + "Sub1) s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("treatJoinParentOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void treatJoinMultipleParentOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(b.children AS " + strategy + "Sub1) s1), (SELECT s2.sub2Value FROM TREAT(b.children AS " + strategy + "Sub2) s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("treatJoinMultipleParentOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void treatJoinEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, FAILS because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT s1.sub1Value FROM " + strategy + "Base b LEFT JOIN TREAT(b.embeddable.children AS " + strategy + "Sub1) s1", Integer.class);
        System.out.println("treatJoinEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable.children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void treatJoinMultipleEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : issues 1 query, all successful
        // - SingleTable   : issues 1 query, FAILS because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT s1.sub1Value, s2.sub2Value FROM " + strategy + "Base b LEFT JOIN TREAT(b.embeddable.children AS " + strategy + "Sub1) s1 LEFT JOIN TREAT(b.embeddable.children AS " + strategy + "Sub2) s2", Object[].class);
        System.out.println("treatJoinMultipleEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable.children => 4 instances
        // Left join on b.embeddable.children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    public void treatJoinParentEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(b.embeddable.children AS " + strategy + "Sub1) s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("treatJoinParentEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    public void treatJoinMultipleParentEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(b.embeddable.children AS " + strategy + "Sub1) s1), (SELECT s2.sub2Value FROM TREAT(b.embeddable.children AS " + strategy + "Sub2) s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("treatJoinMultipleParentEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedRootOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, join path parsing fails
        // - SingleTable   : not working, join path parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT s1.value FROM " + strategy + "Base b LEFT JOIN TREAT(b AS " + strategy + "Sub1).children1 s1", Integer.class);
        System.out.println("joinTreatedRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Left join on b.children1 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, join path parsing fails
        // - SingleTable   : not working, join path parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT s1.value, s2.value FROM " + strategy + "Base b LEFT JOIN TREAT(b AS " + strategy + "Sub1).children1 s1 LEFT JOIN TREAT(b AS " + strategy + "Sub2).children2 s2", Object[].class);
        System.out.println("joinMultipleTreatedRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Left join on b.children1 => 4 instances
        // Left join on b.children2 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT s1.value FROM TREAT(b AS " + strategy + "Sub1).children1 s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("joinTreatedParentRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedParentRootOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT s1.value FROM TREAT(b AS " + strategy + "Sub1).children1 s1), (SELECT s2.value FROM TREAT(b AS " + strategy + "Sub2).children2 s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("joinMultipleTreatedParentRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedRootEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, join path parsing fails
        // - SingleTable   : not working, join path parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT s1.value FROM " + strategy + "Base b LEFT JOIN TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children s1", Integer.class);
        System.out.println("joinTreatedRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1Children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, join path parsing fails
        // - SingleTable   : not working, join path parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT s1.value, s2.value FROM " + strategy + "Base b LEFT JOIN TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children s1 LEFT JOIN TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children s2", Object[].class);
        System.out.println("joinMultipleTreatedRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1Children => 4 instances
        // Left join on b.embeddable2.sub2Children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT s1.value FROM TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("joinTreatedParentRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedParentRootEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT s1.value FROM TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children s1), (SELECT s2.value FROM TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("joinMultipleTreatedParentRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedRootOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, join path parsing fails
        // - SingleTable   : not working, join path parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT s1.sub1Value FROM " + strategy + "Base b LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1) AS s1", Integer.class);
        System.out.println("treatJoinTreatedRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Left join on b.children1 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, join path parsing fails
        // - SingleTable   : not working, join path parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT s1.sub1Value, s2.sub2Value FROM " + strategy + "Base b LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1) AS s1 LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub2).children2 AS " + strategy + "Sub2) AS s2", Object[].class);
        System.out.println("treatJoinMultipleTreatedRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Left join on b.children1 => 4 instances
        // Left join on b.children2 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1) s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("treatJoinTreatedParentRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedParentRootOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1) s1), (SELECT s2.sub2Value FROM TREAT(TREAT(b AS " + strategy + "Sub2).children2 AS " + strategy + "Sub2) s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("treatJoinMultipleTreatedParentRootOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedRootEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, join path parsing fails
        // - SingleTable   : not working, join path parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT s1.sub1Value FROM " + strategy + "Base b LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1) AS s1", Integer.class);
        System.out.println("treatJoinTreatedRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1Children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, join path parsing fails
        // - SingleTable   : not working, join path parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT s1.sub1Value, s2.sub2Value FROM " + strategy + "Base b LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1) AS s1 LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2) AS s2", Object[].class);
        System.out.println("treatJoinMultipleTreatedRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1Children => 4 instances
        // Left join on b.embeddable2.sub2Children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1) s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("treatJoinTreatedParentRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedParentRootEmbeddableOneToManyInverseSet() {
        // EclipseLink
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1) s1), (SELECT s2.sub2Value FROM TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2) s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("treatJoinMultipleTreatedParentRootEmbeddableOneToManyInverseSet-" + strategy);
        
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
}
