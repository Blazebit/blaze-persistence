/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.persistence.EntityManager;

import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.testsuite.base.jpa.assertion.AssertStatement;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMSSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoPostgreSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoSQLite;
import com.blazebit.persistence.testsuite.entity.DocumentType;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Category;

/**
 *
 * @author Christian Beikov
 */
public class JpqlFunctionTest extends AbstractCoreTest {

    @Override
    public void setUpOnce(){
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Document doc1 = new Document("D1");
                doc1.setAge(5L);
                Document doc2 = new Document("D2");
                Document doc3 = new Document("D3");

                Person o1 = new Person("P1");

                doc1.setOwner(o1);
                doc2.setOwner(o1);
                doc3.setOwner(o1);

                em.persist(o1);

                em.persist(doc1);
                em.persist(doc2);
                em.persist(doc3);
            }
        });
    }

    @Before
    public final void setUp() {
        enableQueryCollecting();
    }

    @After
    public final void tearDown() {
        disableQueryCollecting();
    }
    
    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipselink does not support subqueries in functions
    public void testLimit() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d");
        cb.where("d.id").nonPortable()
            .in()
                .from(Document.class, "subDoc")
                .select("subDoc.id")
                .orderByAsc("subDoc.name")
                .setMaxResults(1)
            .end();
        String expected = "SELECT d FROM Document d WHERE d.id IN (SELECT subDoc.id FROM Document subDoc ORDER BY subDoc.name ASC LIMIT 1)";
        
        assertEquals(expected, cb.getQueryString());
        List<Document> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("D1", resultList.get(0).getName());
    }
    
    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipselink does not support subqueries in functions
    public void testLimitOffset() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d");
        cb.where("d.id").nonPortable()
            .in()
                .from(Document.class, "subDoc")
                .select("subDoc.id")
                .orderByAsc("subDoc.name")
                .setMaxResults(1)
                .setFirstResult(1)
            .end();
        String expected = "SELECT d FROM Document d WHERE d.id IN (SELECT subDoc.id FROM Document subDoc ORDER BY subDoc.name ASC LIMIT 1 OFFSET 1)";
        
        assertEquals(expected, cb.getQueryString());
        List<Document> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("D2", resultList.get(0).getName());
    }
    
    @Test
    public void testGroupByFunction() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d");
        cb.select("SUM(d.id)")
            .select("FUNCTION('YEAR', d.creationDate)", "years")
            .where("FUNCTION('YEAR', d.creationDate)").in(2013,2014,2015)
            .groupBy("d.age")
            .orderByAsc("years");
        String expected = "SELECT SUM(d.id), " + function("YEAR", "d.creationDate") + " AS years "
                + "FROM Document d "
                + "WHERE " + function("YEAR", "d.creationDate") + " IN " + listParameter("param_0") + " "
                + "GROUP BY " + groupBy("d.age", function("YEAR", "d.creationDate"), renderNullPrecedenceGroupBy(function("YEAR", "d.creationDate"), "ASC", "LAST"))
                + " ORDER BY " + renderNullPrecedence("years", function("YEAR", "d.creationDate"), "ASC", "LAST");
        
        assertEquals(expected, cb.getQueryString());
        cb.getResultList();
//        List<Document> resultList = cb.getResultList();
//        assertEquals(1, resultList.size());
//        assertEquals("D2", resultList.get(0).getName());
    }

    @Test
    public void builtinFunctionsReturnCorrectTypes() {
        String coalesceName = resolveRegisteredFunctionName("coalesce");
        String countName = resolveRegisteredFunctionName("count");
        String lengthName = resolveRegisteredFunctionName("length");
        String maxName = resolveRegisteredFunctionName("max");
        Map<String, JpqlFunction> functions = cbf.getRegisteredFunctions();
        assertEquals(String.class, functions.get(coalesceName).getReturnType(String.class));
        assertEquals(Integer.class, functions.get(coalesceName).getReturnType(Integer.class));
        assertEquals(Long.class, functions.get(coalesceName).getReturnType(Long.class));
        assertEquals(Long.class, functions.get(countName).getReturnType(String.class));
        assertEquals(Long.class, functions.get(countName).getReturnType(Integer.class));
        assertEquals(Long.class, functions.get(countName).getReturnType(Long.class));
        assertEquals(Integer.class, functions.get(lengthName).getReturnType(String.class));
        assertEquals(DocumentType.class, functions.get(maxName).getReturnType(DocumentType.class));
    }

    @Test
    @Category({ NoPostgreSQL.class, NoDB2.class, NoOracle.class, NoSQLite.class, NoFirebird.class, NoH2.class, NoMSSQL.class, NoEclipselink.class})
    public void testCastFunctionWithTargetTypeOverrideMysql() {
        testCastFunctionWithTargetTypeOverride("unsigned");
    }

    @Test
    @Category({ NoMySQL.class, NoEclipselink.class })
    public void testCastFunctionWithTargetTypeOverrideDefault() {
        testCastFunctionWithTargetTypeOverride("int");
    }

    private void testCastFunctionWithTargetTypeOverride(final String castTypeOverride) {
        CriteriaBuilder<Long> cb = cbf.create(em, Long.class).from(Document.class, "d")
                .select("FUNCTION('cast_long', d.age, '" + castTypeOverride + "')").where("d.name").eq("D1");

        clearQueries();
        Long age = cb.getSingleResult();
        assertUnorderedQuerySequence()
                .addStatement(new AssertStatement() {
                    @Override
                    public void validate(String query) {
                        Assert.assertTrue(Pattern.matches(".*cast\\(.*as " + castTypeOverride + "\\).*", query));
                    }
                })
                .validate();

        assertEquals(5L, age.longValue());
    }

    @Test
    @Category({ NoHibernate.class, NoDatanucleus.class, NoOpenJPA.class })
    public void testCastFunctionWithTargetTypeOverrideEclipselink() {
        final String castTypeOverride = "smallint";
        CriteriaBuilder<Integer> cb = cbf.create(em, Integer.class).from(Document.class, "d")
                .select("FUNCTION('cast_integer', d.age, '" + castTypeOverride + "')").where("d.name").eq("D1");

        clearQueries();
        Integer age = cb.getSingleResult();
        assertUnorderedQuerySequence()
                .addStatement(new AssertStatement() {
                    @Override
                    public void validate(String query) {
                        Assert.assertTrue(Pattern.matches(".*cast\\(.*as " + castTypeOverride + "\\).*", query));
                    }
                })
                .validate();

        assertEquals(5, age.longValue());
    }
}
