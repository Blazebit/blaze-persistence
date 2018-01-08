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

package com.blazebit.persistence.testsuite.treat.builder;

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
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b.list AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }

    @Test
    public void treatInnerJoinOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b.list AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.list => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b.list AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(b.list AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
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
    public void treatInnerJoinMultipleOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b.list AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(b.list AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void treatJoinParentOneToManyList() {
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.list AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
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
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.list AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b.list AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
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
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b.embeddable.list AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable.list => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }

    @Test
    public void treatInnerJoinEmbeddableOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b.embeddable.list AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.embeddable.list => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleEmbeddableOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b.embeddable.list AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(b.embeddable.list AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
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
    public void treatInnerJoinMultipleEmbeddableOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b.embeddable.list AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(b.embeddable.list AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void treatJoinParentEmbeddableOneToManyList() {
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.embeddable.list AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
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
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.embeddable.list AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b.embeddable.list AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
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
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).list1", "s1")
                        .select("s1.value")
        );
                
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
    public void innerJoinTreatedRootOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).list1", "s1")
                        .select("s1.value")
        );

        // From => 4 instances
        // Inner join on b.list1 => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).list1", "s1")
                        .leftJoin("TREAT(b AS " + strategy + "Sub2).list2", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );
                
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
    public void innerJoinMultipleTreatedRootOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).list1", "s1")
                        .innerJoin("TREAT(b AS " + strategy + "Sub2).list2", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).list1", "s1")
                            .select("s1.value")
                        .end()
        );
                
        // From => 4 instances
        // There are two list elements
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedParentRootOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).list1", "s1")
                            .select("s1.value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub2).list2", "s2")
                            .select("s2.value")
                        .end()
        );
                
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
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List", "s1")
                        .select("s1.value")
        );
                
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
    public void innerJoinTreatedRootEmbeddableOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List", "s1")
                        .select("s1.value")
        );

        // From => 4 instances
        // Left join on b.embeddable.sub1List => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootEmbeddableOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List", "s1")
                        .leftJoin("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );
                
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
    public void innerJoinMultipleTreatedRootEmbeddableOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List", "s1")
                        .innerJoin("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootEmbeddableOneToManyList() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List", "s1")
                            .select("s1.value")
                        .end()
        );
                
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
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List", "s1")
                            .select("s1.value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List", "s2")
                            .select("s2.value")
                        .end()
        );
                
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
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
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
    public void treatInnerJoinTreatedRootOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.list1 => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub2).list2 AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
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
    public void treatInnerJoinMultipleTreatedRootOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub2).list2 AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootOneToManyList() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
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
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).list1 AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub2).list2 AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
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
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
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
    public void treatInnerJoinTreatedRootEmbeddableOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.embeddable1.sub1List => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootEmbeddableOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
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
    public void treatInnerJoinMultipleTreatedRootEmbeddableOneToManyList() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootEmbeddableOneToManyList() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
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
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1List AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2List AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two list elements, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
}
