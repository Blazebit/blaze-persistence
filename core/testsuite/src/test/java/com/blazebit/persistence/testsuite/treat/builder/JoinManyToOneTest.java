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
public class JoinManyToOneTest extends AbstractTreatVariationsTest {

    public JoinManyToOneTest(String strategy, String objectPrefix) {
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
    public void treatJoinManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b.parent AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.parent => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }

    @Test
    public void treatInnerJoinManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b.parent AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.parent => 1 instance
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b.parent AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(b.parent AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.parent => 4 instances
        // Left join on b.parent => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }


    @Test
    public void treatInnerJoinMultipleManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeMultipleInnerTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b.parent AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(b.parent AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }

    @Test
    public void treatJoinParentManyToOne() {
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.parent AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two parents but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleParentManyToOne() {
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.parent AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b.parent AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two parents, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    public void treatJoinEmbeddableManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b.embeddable.parent AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable.parent => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }

    @Test
    public void treatInnerJoinEmbeddableManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b.embeddable.parent AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.embeddable.parent => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleEmbeddableManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeLeftTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b.embeddable.parent AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(b.embeddable.parent AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable.parent => 4 instances
        // Left join on b.embeddable.parent => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    public void treatInnerJoinMultipleEmbeddableManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMultipleTreatJoinWithSingleTableIsNotBroken();
        assumeMultipleInnerTreatJoinWithSingleTableIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b.embeddable.parent AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(b.embeddable.parent AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void treatJoinParentEmbeddableManyToOne() {
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.embeddable.parent AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two parents but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleParentEmbeddableManyToOne() {
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.embeddable.parent AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b.embeddable.parent AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two parents, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedRootManyToOne() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).parent1", "s1")
                        .select("s1.value")
        );
                
        // From => 4 instances
        // Left join on b.parent1 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }

    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void innerJoinTreatedRootManyToOne() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).parent1", "s1")
                        .select("s1.value")
        );

        // From => 4 instances
        // Inner join on b.parent1 => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootManyToOne() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).parent1", "s1")
                        .leftJoin("TREAT(b AS " + strategy + "Sub2).parent2", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );
                
        // From => 4 instances
        // Left join on b.parent1 => 4 instances
        // Left join on b.parent2 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void innerJoinMultipleTreatedRootManyToOne() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).parent1", "s1")
                        .innerJoin("TREAT(b AS " + strategy + "Sub2).parent2", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootManyToOne() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).parent1", "s1")
                            .select("s1.value")
                        .end()
        );
                
        // From => 4 instances
        // There are two parents but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedParentRootManyToOne() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).parent1", "s1")
                            .select("s1.value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub2).parent2", "s2")
                            .select("s2.value")
                        .end()
        );
                
        // From => 4 instances
        // There are two parents, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedRootEmbeddableManyToOne() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent", "s1")
                        .select("s1.value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable.sub1Parent => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }

    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void innerJoinTreatedRootEmbeddableManyToOne() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent", "s1")
                        .select("s1.value")
        );

        // From => 4 instances
        // Inner join on b.embeddable.sub1Parent => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 101);
    }

    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootEmbeddableManyToOne() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent", "s1")
                        .leftJoin("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Parent", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );
                
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
    public void innerJoinMultipleTreatedRootEmbeddableManyToOne() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent", "s1")
                        .innerJoin("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Parent", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }

    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootEmbeddableManyToOne() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent", "s1")
                            .select("s1.value")
                        .end()
        );
                
        // From => 4 instances
        // There are two parents but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedParentRootEmbeddableManyToOne() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent", "s1")
                            .select("s1.value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Parent", "s2")
                            .select("s2.value")
                        .end()
        );
                
        // From => 4 instances
        // There are two parents, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedRootManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeLeftTreatJoinWithRootTreatIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).parent1 AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.parent1 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }

    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatInnerJoinTreatedRootManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).parent1 AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.parent1 => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeLeftTreatJoinWithRootTreatIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).parent1 AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub2).parent2 AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
        // From => 4 instances
        // Left join on b.parent1 => 4 instances
        // Left join on b.parent2 => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }

    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatInnerJoinMultipleTreatedRootManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).parent1 AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub2).parent2 AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootManyToOne() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).parent1 AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two parents but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedParentRootManyToOne() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).parent1 AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub2).parent2 AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two parents, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedRootEmbeddableManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeLeftTreatJoinWithRootTreatIsNotBroken();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
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
    public void treatInnerJoinTreatedRootEmbeddableManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.embeddable1.sub1Parent => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootEmbeddableManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeLeftTreatJoinWithRootTreatIsNotBroken();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Parent AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
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
    public void treatInnerJoinMultipleTreatedRootEmbeddableManyToOne() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Parent AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootEmbeddableManyToOne() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two parents but only one is Sub1
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedParentRootEmbeddableManyToOne() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Parent AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Parent AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two parents, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
}
