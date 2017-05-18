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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.Test;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
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
    
    @Test
    @Category(NoEclipselink.class)
    // TODO: report eclipselink does not support subqueries in functions
    public void testLimit() {
        CriteriaBuilder<Document> cb = cbf.create(em, Document.class, "d");
        cb.where("d.id").nonPortable()
            .in("subqueryAlias", "(FUNCTION('LIMIT', subqueryAlias, 1))")
                .from(Document.class, "subDoc")
                .select("subDoc.id")
                .orderByAsc("subDoc.name")
                .end();
        String expected = "SELECT d FROM Document d WHERE d.id IN (" + function("LIMIT",
                "(SELECT subDoc.id FROM Document subDoc ORDER BY subDoc.name ASC)"
                ,"1") + ")";
        
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
            .in("subqueryAlias", "(FUNCTION('LIMIT', subqueryAlias, 1, 1))")
                .from(Document.class, "subDoc")
                .select("subDoc.id")
                .orderByAsc("subDoc.name")
                .end();
        String expected = "SELECT d FROM Document d WHERE d.id IN (" + function("LIMIT",
                "(SELECT subDoc.id FROM Document subDoc ORDER BY subDoc.name ASC)"
                ,"1", "1") + ")";
        
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
                + "GROUP BY " + groupBy("d.age", groupByPathExpressions(function("YEAR", "d.creationDate"), "d.creationDate"), renderNullPrecedenceGroupBy(groupByPathExpressions(function("YEAR", "d.creationDate"), "d.creationDate")))
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
        Map<String, JpqlFunction> functions = cbf.getRegisteredFunctions();
        assertEquals(String.class, functions.get(coalesceName).getReturnType(String.class));
        assertEquals(Integer.class, functions.get(coalesceName).getReturnType(Integer.class));
        assertEquals(Long.class, functions.get(coalesceName).getReturnType(Long.class));
        assertEquals(Long.class, functions.get(countName).getReturnType(String.class));
        assertEquals(Long.class, functions.get(countName).getReturnType(Integer.class));
        assertEquals(Long.class, functions.get(countName).getReturnType(Long.class));
        assertEquals(Integer.class, functions.get(lengthName).getReturnType(String.class));
    }
}
