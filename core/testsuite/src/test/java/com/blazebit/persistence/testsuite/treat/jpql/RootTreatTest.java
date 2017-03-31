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
        List<Integer> bases = list("SELECT TREAT(b AS " + strategy + "Sub1).sub1Value FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootBasic-" + strategy);
        
        // From => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootBasic() {
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
        List<Object[]> bases = list("SELECT TREAT(b AS " + strategy + "Sub1).sub1Value, TREAT(b AS " + strategy + "Sub2).sub2Value FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootBasic-" + strategy);
        
        // From => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 2    });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentRootBasic() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, successful, but would fail because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(b AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootBasic-" + strategy);
        
        // From => 4 instances
        // There are 2 IntIdEntity instances per Base instance
        // For the 2 Sub1 instances, their sub1Value is doubled
        // For the 2 Sub2 instances, null is emmitted
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2L);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentRootBasic() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, successful, but would fail because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(b AS " + strategy + "Sub1).sub1Value) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(b AS " + strategy + "Sub2).sub2Value) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootBasic-" + strategy);
        
        // From => 4 instances
        // There are 2 IntIdEntity instances per Base instance
        // For the 2 Sub1 instances, their sub1Value is doubled
        // For the 2 Sub2 instances, null is emmitted
        // The second subquery is like the first but for Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2L,   null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 4L   });
        assertRemoved(bases, new Object[] { null, 204L });
    }
    
    @Test
    public void selectTreatedRootEmbeddableBasic() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because filters subtype
        // - SingleTable   : issues 1 query, FAILS because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treat embeddable path parsing fails
        // - SingleTable   : not working, treat embeddable path parsing fails
        // - TablePerClass : not working, treat embeddable path parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT TREAT(b AS " + strategy + "Sub1).sub1Embeddable.someValue FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedRootEmbeddableBasic-" + strategy);
        
        // From => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void selectMultipleTreatedRootEmbeddableBasic() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because filters subtype
        // - SingleTable   : issues 1 query, FAILS because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treat embeddable path parsing fails
        // - SingleTable   : not working, treat embeddable path parsing fails
        // - TablePerClass : not working, treat embeddable path parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT TREAT(b AS " + strategy + "Sub1).sub1Embeddable.someValue, TREAT(b AS " + strategy + "Sub2).sub2Embeddable.someValue FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedRootEmbeddableBasic-" + strategy);
        
        // From => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void selectTreatedParentRootEmbeddableBasic() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, successful, but would fail because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Integer> bases = list("SELECT (SELECT SUM(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Integer.class);
        System.out.println("selectTreatedParentRootEmbeddableBasic-" + strategy);
        
        // From => 4 instances
        // There are 2 IntIdEntity instances per Base instance
        // For the 2 Sub1 instances, their sub1SomeValue is doubled
        // For the 2 Sub2 instances, null is emmitted
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2L);
        assertRemoved(bases, 202L);
    }
    
    @Test
    public void selectMultipleTreatedParentRootEmbeddableBasic() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because query is completely wrong
        // - SingleTable   : issues 1 query, successful, but would fail because filters subtype
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, subquery parsing fails
        // - SingleTable   : not working, subquery parsing fails
        // - TablePerClass : not working, subquery parsing fails
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<Object[]> bases = list("SELECT (SELECT SUM(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue) FROM IntIdEntity i WHERE i.name = b.name), (SELECT SUM(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2SomeValue) FROM IntIdEntity i WHERE i.name = b.name) FROM " + strategy + "Base b", Object[].class);
        System.out.println("selectMultipleTreatedParentRootEmbeddableBasic-" + strategy);
        
        // From => 4 instances
        // There are 2 IntIdEntity instances per Base instance
        // For the 2 Sub1 instances, their sub1SomeValue is doubled
        // For the 2 Sub2 instances, null is emmitted
        // The second subquery is like the first but for Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2L,   null });
        assertRemoved(bases, new Object[] { 202L, null });
        assertRemoved(bases, new Object[] { null, 4L   });
        assertRemoved(bases, new Object[] { null, 204L });
    }

    @Test
    public void whereTreatedRootBasic() {
        // EclipseLink
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass :
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE COALESCE(TREAT(b AS " + strategy + "Sub1).sub1Value, 0) < 100", String.class);
        System.out.println("whereTreatedRootBasic-" + strategy);

        // From => 4 instances
        // Where => 1 instance because 1 Sub1 has sub1Value 101, the others are Sub2s
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, objectPrefix + "1");
    }
    
    @Test
    public void whereTreatedRootBasicOr() {
        // EclipseLink
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass :
        // Hibernate
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass :
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE TYPE(b) <> " + strategy + "Sub1 OR COALESCE(TREAT(b AS " + strategy + "Sub1).sub1Value, 0) < 100", String.class);
        System.out.println("whereTreatedRootBasicOr-" + strategy);
        
        // From => 4 instances
        // Where => 3 instance because 1 Sub1 has sub1Value 101, the others are Sub2s
        Assert.assertEquals(3, bases.size());
        assertRemoved(bases, objectPrefix + "2.parent");
        assertRemoved(bases, objectPrefix + "1");
        assertRemoved(bases, objectPrefix + "2");
    }
    
    @Test
    public void whereMultipleTreatedRootBasic() {
        // EclipseLink
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE COALESCE(TREAT(b AS " + strategy + "Sub1).sub1Value, 0) < 100 AND COALESCE(TREAT(b AS " + strategy + "Sub2).sub2Value, 0) < 100", String.class);
        System.out.println("whereMultipleTreatedRootBasic-" + strategy);
        
        // From => 4 instances
        // Where => 0 instances because one variable can't be treated with 2 different subtypes in a single AND predicate
        Assert.assertEquals(0, bases.size());
    }

    @Test
    public void whereMultipleTreatedRootBasicOr() {
        // EclipseLink
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass :
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE TYPE(b) <> " + strategy + "Sub1 OR COALESCE(TREAT(b AS " + strategy + "Sub1).sub1Value, 0) < 100 AND TYPE(b) <> " + strategy + "Sub2 OR COALESCE(TREAT(b AS " + strategy + "Sub2).sub2Value, 0) < 100", String.class);
        System.out.println("whereMultipleTreatedRootBasicOr-" + strategy);

        // From => 4 instances
        // Where => 2 instances because 1 Sub1 has sub1Value 101, the other has 1
        // 1 Sub2 has sub2Value 102, the other has 2
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, objectPrefix + "1");
        assertRemoved(bases, objectPrefix + "2");
    }
    
    @Test
    public void whereTreatedRootEmbeddableBasic() {
        // EclipseLink
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE COALESCE(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue, 0) < 100", String.class);
        System.out.println("whereTreatedRootEmbeddableBasic-" + strategy);
        
        // From => 4 instances
        // Where => 3 instances because 1 Sub1 has sub1Value 101, the other has 1 and Sub2s use 0 because of coalesce
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, objectPrefix + "1");
    }

    @Test
    public void whereTreatedRootEmbeddableBasicOr() {
        // EclipseLink
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass :
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE TYPE(b) <> " + strategy + "Sub1 OR COALESCE(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue, 0) < 100", String.class);
        System.out.println("whereTreatedRootEmbeddableBasicOr-" + strategy);

        // From => 4 instances
        // Where => 3 instances because 1 Sub1 has sub1Value 101, the other has 1 and Sub2s use 0 because of coalesce
        Assert.assertEquals(3, bases.size());
        assertRemoved(bases, objectPrefix + "2.parent");
        assertRemoved(bases, objectPrefix + "1");
        assertRemoved(bases, objectPrefix + "2");
    }
    
    @Test
    public void whereMultipleTreatedRootEmbeddableBasic() {
        // EclipseLink
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE COALESCE(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue, 0) < 100 AND COALESCE(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2SomeValue, 0) < 100", String.class);
        System.out.println("whereMultipleTreatedRootEmbeddableBasic-" + strategy);
        
        // From => 4 instances
        // Where => 0 instances because one variable can't be treated with 2 different subtypes in a single AND predicate
        Assert.assertEquals(0, bases.size());
    }

    @Test
    public void whereMultipleTreatedRootEmbeddableBasicOr() {
        // EclipseLink
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        :
        // - SingleTable   :
        // - TablePerClass :
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE TYPE(b) <> " + strategy + "Sub1 OR COALESCE(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue, 0) < 100 AND TYPE(b) <> " + strategy + "Sub2 OR COALESCE(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2SomeValue, 0) < 100", String.class);
        System.out.println("whereMultipleTreatedRootEmbeddableBasicOr-" + strategy);

        // From => 4 instances
        // 1 Sub2 has sub2SomeValue 102, the other has 2
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, objectPrefix + "1");
        assertRemoved(bases, objectPrefix + "2");
    }
    
    @Test
    public void whereTreatedRootConditionBasic() {
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
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE TREAT(b AS " + strategy + "Sub1).sub1Value = 101", String.class);
        System.out.println("whereTreatedRootConditionBasic-" + strategy);
        
        // From => 4 instances
        // Where => 1 instance because 1 Sub1 has sub1Value 101
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, objectPrefix + "1.parent");
    }
    
    @Test
    public void whereMultipleTreatedRootConditionBasic() {
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
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE TREAT(b AS " + strategy + "Sub1).sub1Value = 101 OR TREAT(b AS " + strategy + "Sub2).sub2Value = 102", String.class);
        System.out.println("whereMultipleTreatedRootConditionBasic-" + strategy);
        
        // From => 4 instances
        // Where => 2 instances because 1 Sub1 has sub1Value 101 and 1 Sub2 has sub2Value 102
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, objectPrefix + "1.parent");
        assertRemoved(bases, objectPrefix + "2.parent");
    }
    
    @Test
    public void whereTreatedRootConditionEmbeddableBasic() {
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
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue = 101", String.class);
        System.out.println("whereTreatedRootConditionEmbeddableBasic-" + strategy);
        
        // From => 4 instances
        // Where => 1 instance because 1 Sub1 has sub1SomeValue 101
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, objectPrefix + "1.parent");
    }
    
    @Test
    public void whereMultipleTreatedRootConditionEmbeddableBasic() {
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
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE TREAT(b AS " + strategy + "Sub1).embeddable1.sub1SomeValue = 101 OR TREAT(b AS " + strategy + "Sub2).embeddable2.sub2SomeValue = 102", String.class);
        System.out.println("whereMultipleTreatedRootConditionEmbeddableBasic-" + strategy);
        
        // From => 4 instances
        // Where => 2 instances because 1 Sub1 has sub1SomeValue 101 and 1 Sub2 has sub2SomeValue 102
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, objectPrefix + "1.parent");
        assertRemoved(bases, objectPrefix + "2.parent");
    }
    
    @Test
    public void whereTreatedRootConditionNegated() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because filters subtype, NOT not applied to type constraint
        // - SingleTable   : issues 1 query, FAILS because filters subtype, NOT not applied to type constraint
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE NOT(TREAT(b AS " + strategy + "Sub1).sub1Value = 101)", String.class);
        System.out.println("whereTreatedRootConditionNegated-" + strategy);
        
        // From => 4 instances
        // Where => 3 instances because 1 Sub1 has sub1Value 101, the other has 1 and Sub2s are included because type constraint is inverted
        Assert.assertEquals(3, bases.size());
        assertRemoved(bases, objectPrefix + "1");
        assertRemoved(bases, objectPrefix + "2");
        assertRemoved(bases, objectPrefix + "2.parent");
    }
    
    @Test
    public void whereMultipleTreatedRootConditionNegated() {
        // EclipseLink
        // - Joined        : issues 1 query, FAILS because filters subtype, NOT not applied to type constraint
        // - SingleTable   : issues 1 query, FAILS because filters subtype, NOT not applied to type constraint
        // - TablePerClass : not working, strategy unsupported
        // Hibernate
        // - Joined        : not working, treated paths unsupported
        // - SingleTable   : not working, treated paths unsupported
        // - TablePerClass : not working, treated paths unsupported
        // DataNucleus
        // - Joined        : 
        // - SingleTable   : 
        // - TablePerClass : 
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE NOT(TREAT(b AS " + strategy + "Sub1).sub1Value = 101) AND NOT(TREAT(b AS " + strategy + "Sub2).sub2Value = 102)", String.class);
        System.out.println("whereMultipleTreatedRootConditionNegated-" + strategy);
        
        // From => 4 instances
        // Where => 2 instances because 1 Sub1 has sub1Value 101 and 1 Sub2 has sub2Value 102
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, objectPrefix + "1");
        assertRemoved(bases, objectPrefix + "2");
    }
    
    @Test
    public void whereTreatedRootConditionSuperTypeAccess() {
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
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE TREAT(b AS " + strategy + "Sub1).value > 100", String.class);
        System.out.println("whereTreatedRootConditionSuperTypeAccess-" + strategy);
        
        // From => 4 instances
        // Where => 1 instance because 1 Sub1 has value 101, the other has 1 and Sub2s are excluded because of type constraint
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, objectPrefix + "1.parent");
    }
    
    @Test
    public void whereMultipleTreatedRootConditionSuperTypeAccess() {
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
        List<String> bases = list("SELECT b.name FROM " + strategy + "Base b WHERE TREAT(b AS " + strategy + "Sub1).value > 100 OR TREAT(b AS " + strategy + "Sub2).value < 100", String.class);
        System.out.println("whereMultipleTreatedRootConditionSuperTypeAccess-" + strategy);
        
        // From => 4 instances
        // Where => 2 instances because 1 Sub1 has value 101 and 1 Sub2 has value 102 the has value 1
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, objectPrefix + "1.parent");
        assertRemoved(bases, objectPrefix + "2");
    }
    
}
