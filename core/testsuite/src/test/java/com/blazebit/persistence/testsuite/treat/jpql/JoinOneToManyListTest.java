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
public class JoinOneToManyListTest extends AbstractTreatVariationsTest {

    public JoinOneToManyListTest(String strategy, String objectPrefix) {
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
    public void treatJoinOneToManyList() {
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
        List<Integer> bases = list("SELECT s1.sub1Value FROM " + strategy + "Base b LEFT JOIN TREAT(b.list AS " + strategy + "Sub1) s1", Integer.class);
        System.out.println("treatJoinOneToManyList-" + strategy);
        
        // From => 4 instances
        // Left join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleOneToManyList() {
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
        List<Object[]> bases = list("SELECT s1.sub1Value, s2.sub2Value FROM " + strategy + "Base b LEFT JOIN TREAT(b.list AS " + strategy + "Sub1) s1 LEFT JOIN TREAT(b.list AS " + strategy + "Sub2) s2", Object[].class);
        System.out.println("treatJoinMultipleOneToManyList-" + strategy);
        
        // From => 4 instances
        // Left join on b.list => 4 instances
        // Left join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void treatJoinParentOneToManyList() {
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
        List<Integer> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(b.list AS " + strategy + "Sub1) s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("treatJoinParentOneToManyList-" + strategy);
        
        // From => 4 instances
        // There are four list elements but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleParentOneToManyList() {
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
        List<Object[]> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(b.list AS " + strategy + "Sub1) s1), (SELECT s2.sub2Value FROM TREAT(b.list AS " + strategy + "Sub2) s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("treatJoinMultipleParentOneToManyList-" + strategy);
        
        // From => 4 instances
        // There are four list elements, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void treatJoinEmbeddableOneToManyList() {
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
        List<Integer> bases = list("SELECT s1.sub1Value FROM " + strategy + "Base b LEFT JOIN TREAT(b.embeddable.list AS " + strategy + "Sub1) s1", Integer.class);
        System.out.println("treatJoinEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleEmbeddableOneToManyList() {
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
        List<Object[]> bases = list("SELECT s1.sub1Value, s2.sub2Value FROM " + strategy + "Base b LEFT JOIN TREAT(b.embeddable.list AS " + strategy + "Sub1) s1 LEFT JOIN TREAT(b.embeddable.list AS " + strategy + "Sub2) s2", Object[].class);
        System.out.println("treatJoinMultipleEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable.list => 4 instances
        // Left join on b.embeddable.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void treatJoinParentEmbeddableOneToManyList() {
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
        List<Integer> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(b.embeddable.list AS " + strategy + "Sub1) s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("treatJoinParentEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // There are four list elements but only two are Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleParentEmbeddableOneToManyList() {
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
        List<Object[]> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(b.embeddable.list AS " + strategy + "Sub1) s1), (SELECT s2.sub2Value FROM TREAT(b.embeddable.list AS " + strategy + "Sub2) s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("treatJoinMultipleParentEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // There are four list elements, two are Sub1 and the other are Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedRootOneToManyList() {
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
        List<Integer> bases = list("SELECT s1.value FROM " + strategy + "Base b LEFT JOIN TREAT(b AS " + strategy + "Sub1).list1 s1", Integer.class);
        System.out.println("joinTreatedRootOneToManyList-" + strategy);
        
        // From => 4 instances
        // Left join on b.list1 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootOneToManyList() {
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
        List<Object[]> bases = list("SELECT s1.value, s2.value FROM " + strategy + "Base b LEFT JOIN TREAT(b AS " + strategy + "Sub1).list1 s1 LEFT JOIN TREAT(b AS " + strategy + "Sub2).list2 s2", Object[].class);
        System.out.println("joinMultipleTreatedRootOneToManyList-" + strategy);
        
        // From => 4 instances
        // Left join on b.list1 => 4 instances
        // Left join on b.list2 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2,    null });
        assertRemoved(bases, new Object[] { null, 1    });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootOneToManyList() {
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
        List<Integer> bases = list("SELECT (SELECT s1.value FROM TREAT(b AS " + strategy + "Sub1).list1 s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("joinTreatedParentRootOneToManyList-" + strategy);
        
        // From => 4 instances
        // There are two list elements but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedParentRootOneToManyList() {
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
        List<Object[]> bases = list("SELECT (SELECT s1.value FROM TREAT(b AS " + strategy + "Sub1).list1 s1), (SELECT s2.value FROM TREAT(b AS " + strategy + "Sub2).list2 s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("joinMultipleTreatedParentRootOneToManyList-" + strategy);
        
        // From => 4 instances
        // There are two list elements, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2,    null });
        assertRemoved(bases, new Object[] { null, 1    });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedRootEmbeddableOneToManyList() {
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
        List<Integer> bases = list("SELECT s1.value FROM " + strategy + "Base b LEFT JOIN TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List s1", Integer.class);
        System.out.println("joinTreatedRootEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable.sub1List => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootEmbeddableOneToManyList() {
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
        List<Object[]> bases = list("SELECT s1.value, s2.value FROM " + strategy + "Base b LEFT JOIN TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List s1 LEFT JOIN TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List s2", Object[].class);
        System.out.println("joinMultipleTreatedRootEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1List => 4 instances
        // Left join on b.embeddable2.sub2List => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2,    null });
        assertRemoved(bases, new Object[] { null, 1    });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootEmbeddableOneToManyList() {
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
        List<Integer> bases = list("SELECT (SELECT s1.value FROM TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("joinTreatedParentRootEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // There are two list elements but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedParentRootEmbeddableOneToManyList() {
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
        List<Object[]> bases = list("SELECT (SELECT s1.value FROM TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List s1), (SELECT s2.value FROM TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("joinMultipleTreatedParentRootEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // There are two list elements, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2,    null });
        assertRemoved(bases, new Object[] { null, 1    });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedRootOneToManyList() {
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
        List<Integer> bases = list("SELECT s1.sub1Value FROM " + strategy + "Base b LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1) AS s1", Integer.class);
        System.out.println("treatJoinTreatedRootOneToManyList-" + strategy);
        
        // From => 4 instances
        // Left join on b.list1 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootOneToManyList() {
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
        List<Object[]> bases = list("SELECT s1.sub1Value, s2.sub2Value FROM " + strategy + "Base b LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1) AS s1 LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub2).list2 AS " + strategy + "Sub2) AS s2", Object[].class);
        System.out.println("treatJoinMultipleTreatedRootOneToManyList-" + strategy);
        
        // From => 4 instances
        // Left join on b.list1 => 4 instances
        // Left join on b.list2 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootOneToManyList() {
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
        List<Integer> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1) s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("treatJoinTreatedParentRootOneToManyList-" + strategy);
        
        // From => 4 instances
        // There are two list elements but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedParentRootOneToManyList() {
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
        List<Object[]> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1) s1), (SELECT s2.sub2Value FROM TREAT(TREAT(b AS " + strategy + "Sub2).list2 AS " + strategy + "Sub2) s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("treatJoinMultipleTreatedParentRootOneToManyList-" + strategy);
        
        // From => 4 instances
        // There are two list elements, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedRootEmbeddableOneToManyList() {
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
        List<Integer> bases = list("SELECT s1.sub1Value FROM " + strategy + "Base b LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1) AS s1", Integer.class);
        System.out.println("treatJoinTreatedRootEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1List => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootEmbeddableOneToManyList() {
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
        List<Object[]> bases = list("SELECT s1.sub1Value, s2.sub2Value FROM " + strategy + "Base b LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1) AS s1 LEFT JOIN TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2) AS s2", Object[].class);
        System.out.println("treatJoinMultipleTreatedRootEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // Left join on b.embeddable1.sub1List => 4 instances
        // Left join on b.embeddable2.sub2List => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootEmbeddableOneToManyList() {
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
        List<Integer> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1) s1) FROM " + strategy + "Base b", Integer.class);
        System.out.println("treatJoinTreatedParentRootEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // There are two list elements but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedParentRootEmbeddableOneToManyList() {
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
        List<Object[]> bases = list("SELECT (SELECT s1.sub1Value FROM TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1) s1), (SELECT s2.sub2Value FROM TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2) s2) FROM " + strategy + "Base b", Object[].class);
        System.out.println("treatJoinMultipleTreatedParentRootEmbeddableOneToManyList-" + strategy);
        
        // From => 4 instances
        // There are two list elements, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
}
