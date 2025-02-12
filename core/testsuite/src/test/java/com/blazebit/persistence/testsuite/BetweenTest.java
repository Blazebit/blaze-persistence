/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blazebit.persistence.BetweenBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class BetweenTest extends AbstractCoreTest {

    @Test
    public void testBetween() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age").between(1L).and(10L);

        assertEquals("SELECT d FROM Document d WHERE d.age BETWEEN :param_0 AND :param_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testBetweenValueAndNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age").between(1), NullPointerException.class, r -> r.and(null));
    }

    @Test
    public void testBetweenNullAndValue() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class, r -> r.between(null));
    }

    @Test
    public void testNotBetween() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.age").notBetween(1L).and(10L);

        assertEquals("SELECT d FROM Document d WHERE d.age NOT BETWEEN :param_0 AND :param_1", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNotBetweenValueAndNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age").notBetween(1), NullPointerException.class, r -> r.and(null));
    }

    @Test
    public void testNotBetweenNullAndValue() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class, r -> r.notBetween(null));
    }

    /* builder ended tests */
    @Test
    public void testBetweenObjectBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.between(1);
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testNotBetweenObjectBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.notBetween(1);
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testBetweenExpressionBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.betweenExpression("1");
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testNotBetweenExpressionBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.notBetweenExpression("1");
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testBetweenSubqueryBuilderNotEnded1() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.betweenSubquery();
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testNotBetweenSubqueryBuilderNotEnded1() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.notBetweenSubquery();
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }
    
    @Test
    public void testBetweenSubqueryBuilderNotEnded2() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.betweenSubquery("s", "s+1");
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testNotBetweenSubqueryBuilderNotEnded2() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.notBetweenSubquery("s", "s+1");
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testBetweenAndSubqueryBuilderNotEnded1() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        BetweenBuilder<CriteriaBuilder<Document>> betweenBuilder = criteria.where("d.age").between(1);
        betweenBuilder.andSubqery();
        verifyBuilderChainingException(betweenBuilder);
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testBetweenAndSubqueryBuilderNotEnded2() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        BetweenBuilder<CriteriaBuilder<Document>> betweenBuilder = criteria.where("d.age").between(1);
        betweenBuilder.andSubqery("s", "s+1");
        verifyBuilderChainingException(betweenBuilder);
        verifyException(criteria, BuilderChainingException.class, r -> r.getQueryString());
    }

    @Test
    public void testBetweenAndBuilderAlreadyEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        BetweenBuilder<CriteriaBuilder<Document>> betweenBuilder = criteria.where("d.age").between(1);
        betweenBuilder.and(1);
        verifyBuilderChainingExceptionWithEndedBuilder(betweenBuilder);
    }

    @Test
    public void testBetweenAndExpressionBuilderAlreadyEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        BetweenBuilder<CriteriaBuilder<Document>> betweenBuilder = criteria.where("d.age").between(1);
        betweenBuilder.andExpression("1");
        verifyBuilderChainingExceptionWithEndedBuilder(betweenBuilder);
    }
    
    private void verifyBuilderChainingException(RestrictionBuilder<?> restrictionBuilder) {
        verifyException(restrictionBuilder, BuilderChainingException.class, r -> r.betweenExpression("1"));
        verifyException(restrictionBuilder, BuilderChainingException.class, r -> r.notBetweenExpression("1"));
        verifyException(restrictionBuilder, BuilderChainingException.class, r -> r.between(1));
        verifyException(restrictionBuilder, BuilderChainingException.class, r -> r.notBetween(1));
        verifyException(restrictionBuilder, BuilderChainingException.class, r -> r.betweenSubquery());
        verifyException(restrictionBuilder, BuilderChainingException.class, r -> r.notBetweenSubquery());
        verifyException(restrictionBuilder, BuilderChainingException.class, r -> r.betweenSubquery("s", "s+1"));
        verifyException(restrictionBuilder, BuilderChainingException.class, r -> r.notBetweenSubquery("s", "s+1"));
    }
    
    private void verifyBuilderChainingException(BetweenBuilder<CriteriaBuilder<Document>> betweenBuilder) {
        verifyException(betweenBuilder, BuilderChainingException.class, r -> r.and(1));
        verifyException(betweenBuilder, BuilderChainingException.class, r -> r.andExpression("1"));
        verifyException(betweenBuilder, BuilderChainingException.class, r -> r.andSubqery());
        verifyException(betweenBuilder, BuilderChainingException.class, r -> r.andSubqery("s", "s+1"));
    }
    
    private void verifyBuilderChainingExceptionWithEndedBuilder(BetweenBuilder<CriteriaBuilder<Document>> betweenBuilder) {
        verifyException(betweenBuilder, BuilderChainingException.class, r -> r.and(1));
        verifyException(betweenBuilder, BuilderChainingException.class, r -> r.andExpression("1"));
        verifyException(betweenBuilder.andSubqery().from(Person.class), BuilderChainingException.class, r -> r.end());
        verifyException(betweenBuilder.andSubqery("s", "s+1").from(Person.class), BuilderChainingException.class, r -> r.end());
    }

}
