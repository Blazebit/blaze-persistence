/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.DocumentType;
import org.junit.Assert;
import org.junit.Test;

import jakarta.persistence.Parameter;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Tuple;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class ParameterAPITest extends AbstractCoreTest {

    @Test
    public void testSetParameter_noSuchParamter() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class);
        try {
            crit.setParameter("index", 0);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSetDateParameter_noSuchParamter() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class);
        try {
            crit.setParameter("index", new Date(), TemporalType.DATE);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testSetCalendarParameter_noSuchParamter() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class);
        try {
            crit.setParameter("index", Calendar.getInstance(), TemporalType.DATE);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void test() {
        CriteriaBuilder<Document> crit = cbf.create(em, Document.class);
        crit.select("contacts[:index]")
            .where("contacts[:where_index]").isNotNull()
            .where("name").eq("MyDoc")
            .where("lastModified").lt().expression(":lastModifiedFilter")
            .groupBy("age")
            .having("age").gt().expression(":minAge");

        assertFalse(crit.isParameterSet("index"));
        assertFalse(crit.isParameterSet("where_index"));
        assertFalse(crit.isParameterSet("minAge"));
        assertFalse(crit.isParameterSet("lastModifiedFilter"));
        assertTrue(crit.isParameterSet("param_0"));

        Set<? extends Parameter<?>> params = crit.getParameters();
        assertTrue(params.size() == 5);
        for (Parameter<?> p : params) {
            if ("param_0".equals(p.getName())) {
                assertTrue(p.getParameterType().equals(String.class));
            } else {
                assertTrue(p.getParameterType() == null);
            }
            assertTrue(p.getPosition() == null);
        }
        crit.setParameter("index", 1);
        crit.setParameter("where_index", 2);
        crit.setParameter("minAge", 3);
        crit.setParameter("lastModifiedFilter", new Date(), TemporalType.TIMESTAMP);

        assertTrue(crit.isParameterSet("index"));
        assertTrue(crit.isParameterSet("where_index"));
        assertTrue(crit.isParameterSet("minAge"));
        assertTrue(crit.isParameterSet("lastModifiedFilter"));
    }

    @Test
    public void testReservedParameterName1() {
        CriteriaBuilder<Document> criteria = cbf.create(em, Document.class);
        verifyException(criteria, IllegalArgumentException.class, r -> r.select("contacts[:ids]"));
    }

    @Test
    public void testUseParameterTwoTimes() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class).from(Document.class).select(":test")
            .where("contacts[:test]").isNull();
        assertFalse(cb.isParameterSet("test"));
    }

    @Test
    public void testIsParameterSetWithNonExistingParameter() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        assertFalse(cb.isParameterSet("test"));
    }
    
    @Test
    public void testParametersInFunction() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        cb.from(Document.class, "d")
                .where("FUNCTION('zero', FUNCTION('zero', :f2param))").eqExpression("1");
        cb.setParameter("f2param", 3);
        
        assertTrue(cb.isParameterSet("f2param"));
        assertEquals("SELECT d FROM Document d WHERE " + function("zero", function("zero", ":f2param")) + " = 1", cb.getQueryString());
    }
    
    @Test
    public void testParametersInSubqueryInFunction() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        cb.from(Document.class, "d")
                .where("d.id").in()
                    .from(Document.class, "d2")
                    .where("FUNCTION('zero', FUNCTION('zero', :f2param))").eqExpression("1")
                .end();
        cb.setParameter("f2param", 3);
        
        assertTrue(cb.isParameterSet("f2param"));
        assertEquals("SELECT d FROM Document d WHERE d.id IN (SELECT d2 FROM Document d2 WHERE " + function("zero", function("zero", ":f2param")) + " = 1)", cb.getQueryString());
    }

    @Test
    public void testRenderParameterAsLiteral() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        cb.from(Document.class, "d")
                .select(":param");
        cb.setParameter("param", "test");

        assertEquals("SELECT 'test' FROM Document d", cb.getQueryString());
    }

    // Test for #566
    @Test
    public void testRenderEnumParameterAsLiteral() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        cb.from(Document.class, "d")
                .select(":param");
        cb.setParameter("param", DocumentType.NOVEL);

        assertEquals("SELECT :param FROM Document d", cb.getQueryString());
    }

    @Test
    public void testRenderEnumAsLiteralInPredicate() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class);
        cb.from(Document.class, "d")
                .where("d.documentType").eqLiteral(DocumentType.NOVEL)
                .where("d.documentType").inLiterals(DocumentType.NOVEL, DocumentType.CONTRACT);

        assertEquals("SELECT d FROM Document d " +
                "WHERE d.documentType = " + DocumentType.class.getName() + "." + DocumentType.NOVEL.name() + " " +
                "AND d.documentType IN (" + DocumentType.class.getName() + "." + DocumentType.NOVEL.name() + ", " + DocumentType.class.getName() + "." + DocumentType.CONTRACT.name() + ")",
                cb.getQueryString());
    }
}
