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
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatMapAssociationIsSupported();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b.map AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }

    @Test
    public void treatInnerJoinManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatMapAssociationIsSupported();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b.map AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.map => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatMapAssociationIsSupported();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b.map AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(b.map AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
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
    public void treatInnerJoinMultipleManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatMapAssociationIsSupported();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b.map AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(b.map AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void treatJoinParentManyToManyMapValue() {
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.map AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
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
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.map AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b.map AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
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
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMapInEmbeddableIsSupported();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b.embeddable.map AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable.map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }

    @Test
    public void treatInnerJoinEmbeddableManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMapInEmbeddableIsSupported();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b.embeddable.map AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.embeddable.map => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, 1);
        assertRemoved(bases, 101);
    }
    
    @Test
    public void treatJoinMultipleEmbeddableManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMapInEmbeddableIsSupported();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b.embeddable.map AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(b.embeddable.map AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
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
    public void treatInnerJoinMultipleEmbeddableManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMapInEmbeddableIsSupported();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b.embeddable.map AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(b.embeddable.map AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    public void treatJoinParentEmbeddableManyToManyMapValue() {
        assumeTreatInSubqueryCorrelationWorks();
        assumeMapInEmbeddableIsSupported();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.embeddable.map AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
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
        assumeTreatInSubqueryCorrelationWorks();
        assumeMapInEmbeddableIsSupported();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b.embeddable.map AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b.embeddable.map AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
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
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).map1", "s1")
                        .select("s1.value")
        );

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
    public void innerJoinTreatedRootManyToManyMapValue() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).map1", "s1")
                        .select("s1.value")
        );

        // From => 4 instances
        // Inner join on b.map1 => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootManyToManyMapValue() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).map1", "s1")
                        .leftJoin("TREAT(b AS " + strategy + "Sub2).map2", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );
                
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
    public void innerJoinMultipleTreatedRootManyToManyMapValue() {
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).map1", "s1")
                        .innerJoin("TREAT(b AS " + strategy + "Sub2).map2", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootManyToManyMapValue() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).map1", "s1")
                            .select("s1.value")
                        .end()
        );
                
        // From => 4 instances
        // There are two map values
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedParentRootManyToManyMapValue() {
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).map1", "s1")
                            .select("s1.value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub2).map2", "s2")
                            .select("s2.value")
                        .end()
        );
                
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
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map", "s1")
                        .select("s1.value")
        );
                
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
    public void innerJoinTreatedRootEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map", "s1")
                        .select("s1.value")
        );

        // From => 4 instances
        // Inner join on b.embeddable.sub1Parent => 2 instances
        Assert.assertEquals(2, bases.size());
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }

    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedRootEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map", "s1")
                        .leftJoin("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );

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
    public void innerJoinMultipleTreatedRootEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map", "s1")
                        .innerJoin("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map", "s2")
                        .select("s1.value")
                        .select("s2.value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinTreatedParentRootEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map", "s1")
                            .select("s1.value")
                        .end()
        );
                
        // From => 4 instances
        // There are two map values
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 2);
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void joinMultipleTreatedParentRootEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        assumeRootTreatJoinSupportedOrEmulated();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map", "s1")
                            .select("s1.value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map", "s2")
                            .select("s2.value")
                        .end()
        );
                
        // From => 4 instances
        // There are four map values, two Sub1 and two Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { 2,    null });
        assertRemoved(bases, new Object[] { null, 1    });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedRootManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
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
    public void treatInnerJoinTreatedRootManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.map1 => 1 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub2).map2 AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );
                
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
    public void treatInnerJoinMultipleTreatedRootManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub2).map2 AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootManyToManyMapValue() {
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
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
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).map1 AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub2).map2 AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
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
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMapInEmbeddableIsSupported();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );
                
        // From => 4 instances
        // Left join on b.embeddable1.sub1Map => 4 instances
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, null);
        assertRemoved(bases, 101);
    }

    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatInnerJoinTreatedRootEmbeddableManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMapInEmbeddableIsSupported();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1)", "s1")
                        .select("s1.sub1Value")
        );

        // From => 4 instances
        // Inner join on b.embeddable1.sub1Map => 4 instances
        Assert.assertEquals(1, bases.size());
        assertRemoved(bases, 101);
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinMultipleTreatedRootEmbeddableManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMapInEmbeddableIsSupported();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1)", "s1")
                        .leftJoin("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2)", "s2")
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
    public void treatInnerJoinMultipleTreatedRootEmbeddableManyToManyMapValue() {
        assumeHibernateSupportsMultiTpcWithTypeExpression();
        assumeMapInEmbeddableIsSupported();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeCollectionTreatJoinWithRootTreatWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1)", "s1")
                        .innerJoin("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2)", "s2")
                        .select("s1.sub1Value")
                        .select("s2.sub2Value")
        );

        // Can't be Sub1 and Sub2 at the same time
        Assert.assertEquals(0, bases.size());
    }
    
    @Test
    // NOTE: This is a special case that the JPA spec does not cover but is required to make TREAT complete
    public void treatJoinTreatedParentRootEmbeddableManyToManyMapValue() {
        assumeMapInEmbeddableIsSupported();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Integer> bases = list(
                from(Integer.class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
        );
                
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
        assumeMapInEmbeddableIsSupported();
        assumeTreatJoinWithRootTreatSupportedOrEmulated();
        assumeTreatInSubqueryCorrelationWorks();
        List<Object[]> bases = list(
                from(Object[].class, "Base", "b")
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub1).embeddable1.sub1Map AS " + strategy + "Sub1)", "s1")
                            .select("s1.sub1Value")
                        .end()
                        .selectSubquery()
                            .from("TREAT(TREAT(b AS " + strategy + "Sub2).embeddable2.sub2Map AS " + strategy + "Sub2)", "s2")
                            .select("s2.sub2Value")
                        .end()
        );
                
        // From => 4 instances
        // There are two map values, one is Sub1 and the other Sub2
        Assert.assertEquals(4, bases.size());
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { null, null });
        assertRemoved(bases, new Object[] { 101,  null });
        assertRemoved(bases, new Object[] { null, 102  });
    }
    
}
