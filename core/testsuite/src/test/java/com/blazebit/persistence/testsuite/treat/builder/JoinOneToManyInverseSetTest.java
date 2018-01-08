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
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b.children AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }

    @Test
    public void treatInnerJoinOneToManyInverseSet() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b.children AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.children => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 1);
    }
    
    @Test
    public void treatJoinMultipleOneToManyInverseSet() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b.children AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(b.children AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
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
    public void treatInnerJoinMultipleOneToManyInverseSet() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b.children AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(b.children AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void treatJoinParentOneToManyInverseSet() {
        assumeTreatInSubqueryCorrelationWorks();
        assumeInverseSetCorrelationJoinsSubtypesWhenJoined();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.children AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
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
        assumeTreatInSubqueryCorrelationWorks();
        assumeInverseSetCorrelationJoinsSubtypesWhenJoined();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.children AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b.children AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
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
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b.embeddable.children AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable.children => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
    }

    @Test
    public void treatInnerJoinEmbeddableOneToManyInverseSet() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b.embeddable.children AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.embeddable.children => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 1);
    }
    
    @Test
    public void treatJoinMultipleEmbeddableOneToManyInverseSet() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b.embeddable.children AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(b.embeddable.children AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
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
    public void treatInnerJoinMultipleEmbeddableOneToManyInverseSet() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b.embeddable.children AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(b.embeddable.children AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void treatJoinParentEmbeddableOneToManyInverseSet() {
        assumeTreatInSubqueryCorrelationWorks();
        assumeInverseSetCorrelationJoinsSubtypesWhenJoined();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.embeddable.children AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
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
        assumeTreatInSubqueryCorrelationWorks();
        assumeInverseSetCorrelationJoinsSubtypesWhenJoined();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.embeddable.children AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b.embeddable.children AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
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
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).children1", "s1")
                        .select("s1.value")
        );
                
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
    public void innerJoinTreatedRootOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).children1", "s1")
                        .select("s1.value")
        );

        // From => 4 instances
        // Inner join on b.children1 => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 1);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).children1", "s1")
                        .leftJoin("TREAT(b AS " + strategy + "Sub2).children2", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );
                
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
    public void innerJoinMultipleTreatedRootOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).children1", "s1")
                        .innerJoin("TREAT(b AS " + strategy + "Sub2).children2", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeInverseSetCorrelationJoinsSubtypesWhenJoined();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).children1", "s1")
                            .select("s1.value")
                        .end()
        );
                
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
        assumeRootTreatJoinSupportedOrEmulated();
        assumeInverseSetCorrelationJoinsSubtypesWhenJoined();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).children1", "s1")
                            .select("s1.value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub2).children2", "s2")
                            .select("s2.value")
                        .end()
        );
                
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
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children", "s1")
                        .select("s1.value")
        );
                
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
    public void innerJoinTreatedRootEmbeddableOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children", "s1")
                        .select("s1.value")
        );

        // From => 4 instances
        // Inner join on b.embeddable1.sub1Children => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 1);
    }

    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootEmbeddableOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children", "s1")
                        .leftJoin("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );

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
    public void innerJoinMultipleTreatedRootEmbeddableOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children", "s1")
                        .innerJoin("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootEmbeddableOneToManyInverseSet() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeInverseSetCorrelationJoinsSubtypesWhenJoined();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children", "s1")
                            .select("s1.value")
                        .end()
        );
                
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
        assumeRootTreatJoinSupportedOrEmulated();
        assumeInverseSetCorrelationJoinsSubtypesWhenJoined();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children", "s1")
                            .select("s1.value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children", "s2")
                            .select("s2.value")
                        .end()
        );
                
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
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
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
    public void treatInnerJoinTreatedRootOneToManyInverseSet() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.children1 => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 1);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootOneToManyInverseSet() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub2).children2 AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
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
    public void treatInnerJoinMultipleTreatedRootOneToManyInverseSet() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub2).children2 AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootOneToManyInverseSet() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
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
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).children1 AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub2).children2 AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
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
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
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
    public void treatInnerJoinTreatedRootEmbeddableOneToManyInverseSet() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.embeddable1.sub1Children => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 1);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootEmbeddableOneToManyInverseSet() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
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
    public void treatInnerJoinMultipleTreatedRootEmbeddableOneToManyInverseSet() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootEmbeddableOneToManyInverseSet() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
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
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Children AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Children AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two children, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 1,    null });
        assertRemoved(bases, new Object[] { null, 2    });
    }
    
}
