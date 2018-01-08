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
public class JoinManyToManyMapValueTest extends AbstractTreatVariationsTest {

    public JoinManyToManyMapValueTest(String strategy, String objectPrefix) {
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
    public void treatJoinManyToManyMapValue() {
        // EclipseLink
        // - Joined        : not working, NPE during SQL rendering
        // - SingleTable   : not working, NPE during SQL rendering
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT s1.sub1Value FROM " + strategy + "Base b LEFT JOIN TREAT(b.map AS " + strategy + "Sub1) s1", Integer.class);
        System.out.println("treatJoinManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Left join on b.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleManyToManyMapValue() {
        // EclipseLink
        // - Joined        : not working, NPE during SQL rendering
        // - SingleTable   : not working, NPE during SQL rendering
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT s1.sub1Value, s2.sub2Value FROM " + strategy + "Base b LEFT JOIN TREAT(b.map AS " + strategy + "Sub1) s1 LEFT JOIN TREAT(b.map AS " + strategy + "Sub2) s2", Object[].class);
        System.out.println("treatJoinMultipleManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Left join on b.map => 4 instances
        // Left join on b.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void treatJoinParentManyToManyMapValue() {
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
        List<Integer> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(b.map AS " + strategy + "Sub1) s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("treatJoinParentManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are four map values but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleParentManyToManyMapValue() {
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
        List<Object[]> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(b.map AS " + strategy + "Sub1) s1), (SELECT s2.sub2Value FROM TREAT(b.map AS " + strategy + "Sub2) s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("treatJoinMultipleParentManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are four map values, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void treatJoinEmbeddableManyToManyMapValue() {
        // EclipseLink
        // - Joined        : not working, cast outside of inheritance hierarchy
        // - SingleTable   : not working, cast outside of inheritance hierarchy
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT s1.sub1Value FROM " + strategy + "Base b LEFT JOIN TREAT(b.embeddable.map AS " + strategy + "Sub1) s1", Integer.class);
        System.out.println("treatJoinEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleEmbeddableManyToManyMapValue() {
        // EclipseLink
        // - Joined        : not working, cast outside of inheritance hierarchy
        // - SingleTable   : not working, cast outside of inheritance hierarchy
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT s1.sub1Value, s2.sub2Value FROM " + strategy + "Base b LEFT JOIN TREAT(b.embeddable.map AS " + strategy + "Sub1) s1 LEFT JOIN TREAT(b.embeddable.map AS " + strategy + "Sub2) s2", Object[].class);
        System.out.println("treatJoinMultipleEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable.map => 4 instances
        // Left join on b.embeddable.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void treatJoinParentEmbeddableManyToManyMapValue() {
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
        List<Integer> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(b.embeddable.map AS " + strategy + "Sub1) s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("treatJoinParentEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are four map values but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleParentEmbeddableManyToManyMapValue() {
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
        List<Object[]> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(b.embeddable.map AS " + strategy + "Sub1) s1), (SELECT s2.sub2Value FROM TREAT(b.embeddable.map AS " + strategy + "Sub2) s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("treatJoinMultipleParentEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are four map values, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedRootManyToManyMapValue() {
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
        List<Integer> bases = list("SELECT s1.value FROM " + strategy + "Base b LEFT JOIN TREAT(b AS " + strategy + "Sub1).map1 s1", Integer.class);
        System.out.println("joinTreatedRootManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Left join on b.map1 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootManyToManyMapValue() {
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
        List<Object[]> bases = list("SELECT s1.value, s2.value FROM " + strategy + "Base b LEFT JOIN TREAT(b AS " + strategy + "Sub1).map1 s1 LEFT JOIN TREAT(b AS " + strategy + "Sub2).map2 s2", Object[].class);
        System.out.println("joinMultipleTreatedRootManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Left join on b.map1 => 4 instances
        // Left join on b.map2 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2,    null });
        assertRemoved(bases, new Object[] { null, 1    });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootManyToManyMapValue() {
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
        List<Integer> bases = list("SELECT (SELECT s1.value FROM TREAT(b AS " + strategy + "Sub1).map1 s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("joinTreatedParentRootManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are two map values but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedParentRootManyToManyMapValue() {
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
        List<Object[]> bases = list("SELECT (SELECT s1.value FROM TREAT(b AS " + strategy + "Sub1).map1 s1), (SELECT s2.value FROM TREAT(b AS " + strategy + "Sub2).map2 s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("joinMultipleTreatedParentRootManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are two map values, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2,    null });
        assertRemoved(bases, new Object[] { null, 1    });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedRootEmbeddableManyToManyMapValue() {
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
        List<Integer> bases = list("SELECT s1.value FROM " + strategy + "Base b LEFT JOIN TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map s1", Integer.class);
        System.out.println("joinTreatedRootEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable.sub1Parent => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootEmbeddableManyToManyMapValue() {
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
        List<Object[]> bases = list("SELECT s1.value, s2.value FROM " + strategy + "Base b LEFT JOIN TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map s1 LEFT JOIN TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map s2", Object[].class);
        System.out.println("joinMultipleTreatedRootEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1Parent => 4 instances
        // Left join on b.embeddable2.sub2Parent => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2,    null });
        assertRemoved(bases, new Object[] { null, 1    });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootEmbeddableManyToManyMapValue() {
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
        List<Integer> bases = list("SELECT (SELECT s1.value FROM TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("joinTreatedParentRootEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are two map values but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedParentRootEmbeddableManyToManyMapValue() {
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
        List<Object[]> bases = list("SELECT (SELECT s1.value FROM TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map s1), (SELECT s2.value FROM TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("joinMultipleTreatedParentRootEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are two map values, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2,    null });
        assertRemoved(bases, new Object[] { null, 1    });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedRootManyToManyMapValue() {
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
        List<Integer> bases = list("SELECT s1.sub1Value FROM " + strategy + "Base b LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1) AS s1", Integer.class);
        System.out.println("treatJoinTreatedRootManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Left join on b.map1 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootManyToManyMapValue() {
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
        List<Object[]> bases = list("SELECT s1.sub1Value, s2.sub2Value FROM " + strategy + "Base b LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1) AS s1 LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub2).map2 AS " + strategy + "Sub2) AS s2", Object[].class);
        System.out.println("treatJoinMultipleTreatedRootManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Left join on b.map1 => 4 instances
        // Left join on b.map2 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootManyToManyMapValue() {
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
        List<Integer> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1) s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("treatJoinTreatedParentRootManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are two map values but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedParentRootManyToManyMapValue() {
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
        List<Object[]> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1) s1), (SELECT s2.sub2Value FROM TREAT(TREAT(b AS " + strategy + "Sub2).map2 AS " + strategy + "Sub2) s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("treatJoinMultipleTreatedParentRootManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are two map values, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedRootEmbeddableManyToManyMapValue() {
        // EclipseLink
        // - Joined        : not working, can't resolve valid type
        // - SingleTable   : not working, can't resolve valid type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT s1.sub1Value FROM " + strategy + "Base b LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1) AS s1", Integer.class);
        System.out.println("treatJoinTreatedRootEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1Parent => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootEmbeddableManyToManyMapValue() {
        // EclipseLink
        // - Joined        : not working, can't resolve valid type
        // - SingleTable   : not working, can't resolve valid type
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT s1.sub1Value, s2.sub2Value FROM " + strategy + "Base b LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1) AS s1 LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2) AS s2", Object[].class);
        System.out.println("treatJoinMultipleTreatedRootEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1Parent => 4 instances
        // Left join on b.embeddable2.sub2Parent => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootEmbeddableManyToManyMapValue() {
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
        List<Integer> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1) s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("treatJoinTreatedParentRootEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are two map values but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedParentRootEmbeddableManyToManyMapValue() {
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
        List<Object[]> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1) s1), (SELECT s2.sub2Value FROM TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2) s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("treatJoinMultipleTreatedParentRootEmbeddableManyToManyMapValue-" + strategy);
        
        // From => 4 instances
        // There are two map values, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
}
