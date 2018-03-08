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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class LikeTest extends AbstractCoreTest {

    @Test
    public void testLikeCaseInsensitive() {
        final String pattern = "te%t";
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").like(false).value(pattern).noEscape();

        assertEquals("SELECT d FROM Document d WHERE " + getCaseInsensitiveLike("d.name", ":param_0", null), criteria
                     .getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLikeCaseSensitive() {
        final String pattern = "te%t";
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").like().value(pattern).noEscape();

        assertEquals("SELECT d FROM Document d WHERE d.name LIKE :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLikeEscaped() {
        final String pattern = "t\\_e%t";
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").like().value(pattern).escape('\\');

        assertEquals("SELECT d FROM Document d WHERE d.name LIKE :param_0 ESCAPE '" + escapeCharacter('\\') + "'", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLikeNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.name").like(), NullPointerException.class).value(null);
    }

    @Test
    public void testLikeExpressionCaseInsensitive() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").like(false).expression("d.owner.name").noEscape();

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE " + getCaseInsensitiveLike("d.name", "owner_1.name", null),
                     criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLikeExpressionCaseSensitive() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").like().expression("d.owner.name").noEscape();

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE d.name LIKE owner_1.name", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLikeExpressionEscaped() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").like(true).expression("d.owner.name").escape('\\');

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE d.name LIKE owner_1.name ESCAPE '" + escapeCharacter('\\') + "'", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLikeExpressionNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.name").like(), NullPointerException.class).expression(null);
    }

    /** *** NOT LIKE **** */
    @Test
    public void testNotLikeCaseInsensitive() {
        final String pattern = "te%t";
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").notLike(false).value(pattern).noEscape();

        assertEquals("SELECT d FROM Document d WHERE " + getCaseInsensitiveNotLike("d.name", ":param_0", null), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNotLikeCaseSensitive() {
        final String pattern = "te%t";
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").notLike().value(pattern).noEscape();

        assertEquals("SELECT d FROM Document d WHERE d.name NOT LIKE :param_0", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNotLikeEscaped() {
        final String pattern = "t\\_e%t";
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").notLike().value(pattern).escape('\\');

        assertEquals("SELECT d FROM Document d WHERE d.name NOT LIKE :param_0 ESCAPE '" + escapeCharacter('\\') + "'", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNotLikeNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.name").notLike(), NullPointerException.class).value(null);
    }

    @Test
    public void testNotLikeExpressionCaseInsensitive() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").notLike(false).expression("d.owner.name").noEscape();

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE "
            + getCaseInsensitiveNotLike("d.name", "owner_1.name", null), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testLikeExpressionCaseInsensitiveEscaped() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").like(false).expression("d.owner.name").escape('\\');

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE " + getCaseInsensitiveLike("d.name", "owner_1.name", '\\'),
                     criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNotLikeExpressionCaseInsensitiveEscaped() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").notLike(false).expression("d.owner.name").escape('\\');

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE "
            + getCaseInsensitiveNotLike("d.name", "owner_1.name", '\\'), criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNotLikeExpressionCaseSensitive() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").notLike().expression("d.owner.name").noEscape();

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE d.name NOT LIKE owner_1.name", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNotLikeExpressionEscaped() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        criteria.where("d.name").notLike().expression("d.owner.name").escape('\\');

        assertEquals("SELECT d FROM Document d JOIN d.owner owner_1 WHERE d.name NOT LIKE owner_1.name ESCAPE '" + escapeCharacter('\\') + "'", criteria.getQueryString());
        criteria.getResultList();
    }

    @Test
    public void testNotLikeExpressionNull() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class, "d");
        verifyException(criteria.where("d.name").notLike(), NullPointerException.class).expression(null);
    }

    private String getCaseInsensitiveLike(String property, String likeParam, Character escape) {
        return getCaseInsensitiveLike(property, likeParam, escape, false);
    }
    
    private String getCaseInsensitiveLike(String property, String likeParam, Character escape, boolean negated) {
        String res = "UPPER(" + property + ")" + (negated ? " NOT": "") + " LIKE UPPER(" + likeParam + ")";
        if (escape != null) {
            res += " ESCAPE UPPER('" + escapeCharacter(escape) + "')";
        }
        return res;
    }

    private String getCaseInsensitiveNotLike(String property, String likeParam, Character escape) {
        return getCaseInsensitiveLike(property, likeParam, escape, true);
    }
}
