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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class InTest extends AbstractCoreTest {

    @Test
    // NOTE: See https://hibernate.atlassian.net/browse/HHH-9630
    @Category({ NoHibernate42.class, NoHibernate43.class })
    public void testBuggyHqlIn() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        List<Integer> indicies = Arrays.asList(new Integer[]{ 1, 2, 3, 4, 5 });
        criteria
            .where("1").eqExpression("1")
            .whereOr()
                .where("1").eqExpression("1")
                .where("d.idx").in(indicies)
            .endOr();

        assertEquals("SELECT d FROM Document d WHERE 1 = 1 AND (1 = 1 OR d.idx IN " + listParameter("param_0") + ")", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    // NOTE: See https://hibernate.atlassian.net/browse/HHH-9630
    @Category({ NoHibernate42.class, NoHibernate43.class })
    public void testBuggyHqlIn1() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        List<Integer> indicies = Arrays.asList(new Integer[]{ 1, 2, 3, 4, 5 });
        criteria
            .whereOr()
                .where("1").eqExpression("1")
                .whereAnd()
                    .where("1").eqExpression("1")
                    .where("d.idx").in(indicies)
                .endAnd()
            .endOr();

        assertEquals("SELECT d FROM Document d WHERE 1 = 1 OR (1 = 1 AND d.idx IN " + listParameter("param_0") + ")", criteria.getQueryString());
        criteria.getResultList();
    }
    
    @Test
    public void testIn() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        List<Long> ages = new ArrayList<Long>(Arrays.asList(new Long[]{ 1L, 2L, 3L, 4L, 5L }));
        criteria.where("d.age").in(ages);

        assertEquals("SELECT d FROM Document d WHERE d.age IN " + listParameter("param_0"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testInNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).in((List<?>) null);
    }

    @Test
    public void testNotIn() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        List<Long> ages = new ArrayList<Long>(Arrays.asList(new Long[]{ 1L, 2L, 3L, 4L, 5L }));
        criteria.where("d.age").notIn(ages);
        assertEquals("SELECT d FROM Document d WHERE d.age NOT IN " + listParameter("param_0"), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNotInNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.age"), NullPointerException.class).notIn((List<?>) null);
    }
    
    @Test
    public void testInSubqueryAliasExpression1(){
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.id").nonPortable()
                .in("alias", "(FUNCTION('zero', alias, :elementCount))")
                    .from(Document.class, "d2")
                    .select("d2.id")
                    .select("FUNCTION('zero',FUNCTION('zero',d2.id,FUNCTION('zero',FUNCTION('zero',:colors))),1)", "colorMatches")
                .end();
        assertEquals("SELECT d FROM Document d WHERE d.id IN (" + function("zero", "(SELECT d2.id, " + function("zero", function("zero", "d2.id", function("zero", function("zero", ":colors"))), "1") + " AS colorMatches FROM Document d2)", ":elementCount") + ")", criteria.getQueryString());
    }
    
    // this is not redundant with testInSubqueryAliasExpression1o
    @Test
    public void testInSubqueryAliasExpression2(){
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.id").nonPortable()
                .in("alias", "(FUNCTION('zero', alias, :elementCount))")
                    .from(Document.class, "d2")
                .end();
        assertEquals("SELECT d FROM Document d WHERE d.id IN (" + function("zero", "(SELECT d2 FROM Document d2)", ":elementCount") + ")", criteria.getQueryString());
    }
    
    @Test
    public void testInEmptyListConditional(){
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .where("d.id").in(new ArrayList<Object>());
        assertEquals("SELECT d FROM Document d WHERE " + booleanConditionalExpression(false), criteria.getQueryString());
    }

    @Test
    public void testInEmptyArrayConditional(){
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .where("d.id").in(new Integer[0]);
        assertEquals("SELECT d FROM Document d WHERE " + booleanConditionalExpression(false), criteria.getQueryString());
    }

    @Test
    public void testNotInEmptyListConditional(){
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .where("d.id").notIn(new ArrayList<Object>());
        assertEquals("SELECT d FROM Document d WHERE " + booleanConditionalExpression(true), criteria.getQueryString());
    }
    
    @Test
    public void testInSingleLiteral(){
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d")
            .where("d.id").inExpressions("12");
        assertEquals("SELECT d FROM Document d WHERE d.id IN (12)", criteria.getQueryString());
        criteria.getResultList();
    }
}
