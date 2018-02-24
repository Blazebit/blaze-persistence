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

package com.blazebit.persistence.testsuite;

import static com.googlecode.catchexception.CatchException.verifyException;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blazebit.persistence.BetweenBuilder;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.impl.BuilderChainingException;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
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
        verifyException(criteria.where("d.age").between(1), NullPointerException.class).and(null);
    }

    @Test
    public void testBetweenNullAndValue() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).between(null);
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
        verifyException(criteria.where("d.age").notBetween(1), NullPointerException.class).and(null);
    }

    @Test
    public void testNotBetweenNullAndValue() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).notBetween(null);
    }

    /* builder ended tests */
    @Test
    public void testBetweenObjectBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.between(1);
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testNotBetweenObjectBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.notBetween(1);
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testBetweenExpressionBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.betweenExpression("1");
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testNotBetweenExpressionBuilderNotEnded() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.notBetweenExpression("1");
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testBetweenSubqueryBuilderNotEnded1() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.betweenSubquery();
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testNotBetweenSubqueryBuilderNotEnded1() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.notBetweenSubquery();
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }
    
    @Test
    public void testBetweenSubqueryBuilderNotEnded2() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.betweenSubquery("s", "s+1");
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testNotBetweenSubqueryBuilderNotEnded2() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        RestrictionBuilder<CriteriaBuilder<Document>> restrictionBuilder = criteria.where("d.age");
        restrictionBuilder.notBetweenSubquery("s", "s+1");
        verifyBuilderChainingException(restrictionBuilder);
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testBetweenAndSubqueryBuilderNotEnded1() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        BetweenBuilder<CriteriaBuilder<Document>> betweenBuilder = criteria.where("d.age").between(1);
        betweenBuilder.andSubqery();
        verifyBuilderChainingException(betweenBuilder);
        verifyException(criteria, BuilderChainingException.class).getQueryString();
    }

    @Test
    public void testBetweenAndSubqueryBuilderNotEnded2() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        BetweenBuilder<CriteriaBuilder<Document>> betweenBuilder = criteria.where("d.age").between(1);
        betweenBuilder.andSubqery("s", "s+1");
        verifyBuilderChainingException(betweenBuilder);
        verifyException(criteria, BuilderChainingException.class).getQueryString();
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
        verifyException(restrictionBuilder, BuilderChainingException.class).betweenExpression("1");
        verifyException(restrictionBuilder, BuilderChainingException.class).notBetweenExpression("1");
        verifyException(restrictionBuilder, BuilderChainingException.class).between(1);
        verifyException(restrictionBuilder, BuilderChainingException.class).notBetween(1);
        verifyException(restrictionBuilder, BuilderChainingException.class).betweenSubquery();
        verifyException(restrictionBuilder, BuilderChainingException.class).notBetweenSubquery();
        verifyException(restrictionBuilder, BuilderChainingException.class).betweenSubquery("s", "s+1");
        verifyException(restrictionBuilder, BuilderChainingException.class).notBetweenSubquery("s", "s+1");
    }
    
    private void verifyBuilderChainingException(BetweenBuilder<CriteriaBuilder<Document>> betweenBuilder) {
        verifyException(betweenBuilder, BuilderChainingException.class).and(1);
        verifyException(betweenBuilder, BuilderChainingException.class).andExpression("1");
        verifyException(betweenBuilder, BuilderChainingException.class).andSubqery();
        verifyException(betweenBuilder, BuilderChainingException.class).andSubqery("s", "s+1");
    }
    
    private void verifyBuilderChainingExceptionWithEndedBuilder(BetweenBuilder<CriteriaBuilder<Document>> betweenBuilder) {
        verifyException(betweenBuilder, BuilderChainingException.class).and(1);
        verifyException(betweenBuilder, BuilderChainingException.class).andExpression("1");
        verifyException(betweenBuilder.andSubqery().from(Person.class), BuilderChainingException.class).end();
        verifyException(betweenBuilder.andSubqery("s", "s+1").from(Person.class), BuilderChainingException.class).end();
    }

}
