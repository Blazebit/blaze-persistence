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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.impl.ConfigurationProperties;
import com.blazebit.persistence.spi.ValuesStrategy;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoH2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.*;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.googlecode.catchexception.CatchException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ValuesClauseTest extends AbstractCoreTest {

    private Document d1;
    private Person p1;

    @Override
    protected Class<?>[] getEntityClasses() {
        return concat(super.getEntityClasses(), new Class<?>[]{
            PersonCTE.class,
            DocumentNodeCTE.class
        });
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                p1 = new Person("p1");
                d1 = new Document("doc1", 1);

                d1.setOwner(p1);

                em.persist(p1);
                em.persist(d1);
            }
        });
    }

    @Before
    public void setUp() {
        p1 = cbf.create(em, Person.class).getSingleResult();
        d1 = cbf.create(em, Document.class).getSingleResult();
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunction() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Long.class, "allowedAge", Collections.singleton(1L));
        cb.from(Document.class, "doc");
        cb.where("doc.age").eqExpression("allowedAge.value");
        cb.select("doc.name");
        cb.select("allowedAge.value");

        String expected = ""
                + "SELECT doc.name, TREAT_LONG(allowedAge.value) FROM Document doc, Long(1 VALUES) allowedAge WHERE TREAT_LONG(allowedAge.value) = :allowedAge_value_0 AND doc.age = TREAT_LONG(allowedAge.value)";
        
        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("doc1", resultList.get(0).get(0));
        assertEquals(1L, resultList.get(0).get(1));
    }

    // Test for #305
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionWithParameterInSelect() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Long.class, "allowedAge", Arrays.asList(1L, 2L));
        cb.from(Document.class, "doc");
        cb.where("doc.age").eqExpression("allowedAge.value");
        cb.select("CASE WHEN doc.name = :param THEN doc.name ELSE '' END");
        cb.select("allowedAge.value");

        String expected = ""
                + "SELECT CASE WHEN doc.name = :param THEN doc.name ELSE '' END, TREAT_LONG(allowedAge.value) FROM Document doc, Long(2 VALUES) allowedAge " +
                "WHERE TREAT_LONG(allowedAge.value) = :allowedAge_value_0 OR TREAT_LONG(allowedAge.value) = :allowedAge_value_1 AND doc.age = TREAT_LONG(allowedAge.value)";

        assertEquals(expected, cb.getQueryString());
        cb.setParameter("param", "doc1");
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("doc1", resultList.get(0).get(0));
        assertEquals(1L, resultList.get(0).get(1));
    }

    // Test for #305
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionWithParameterInSelectSubquery() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.from(Document.class, "doc");
        cb.select("CASE WHEN doc.name = :param THEN doc.name ELSE '' END");
        cb.selectSubquery()
                .fromValues(Long.class, "allowedAge", Arrays.asList(1L, 2L))
                .select("CASE WHEN doc.name = :param THEN allowedAge.value ELSE 2L END")
                .where("doc.age").eqExpression("allowedAge.value")
                .end();

        // We can't check the JPQL here because it contains SQL as literal text :|
        cb.setParameter("param", "doc1");
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("doc1", resultList.get(0).get(0));
        assertEquals(1L, resultList.get(0).get(1));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionParameters() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Long.class, "allowedAge", Arrays.asList(1L, 2L));
        cb.from(Document.class, "doc");
        cb.where("doc.age").eqExpression("allowedAge.value");
        cb.select("doc.name");
        cb.select("allowedAge.value");

        TypedQuery<Tuple> query = cb.getQuery();
        assertEquals(1, query.getParameters().size());
        assertEquals(Collection.class, query.getParameter("allowedAge").getParameterType());
        assertEquals(Arrays.asList(1L, 2L), query.getParameterValue("allowedAge"));

        List<Tuple> resultList = query.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("doc1", resultList.get(0).get(0));
        assertEquals(1L, resultList.get(0).get(1));

        query.setParameter("allowedAge", Arrays.asList(3L));
        resultList = query.getResultList();
        assertEquals(0, resultList.size());
    }

    @Test
    // NOTE: Entity joins are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionLeftJoin() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Long.class, "allowedAge", Arrays.asList(1L, 2L, 3L));
        cb.leftJoinOn(Document.class, "doc")
            .on("doc.age").eqExpression("allowedAge.value")
        .end();
        cb.select("allowedAge.value");
        cb.select("doc.name");
        cb.orderByAsc("allowedAge.value");

        String expected = ""
                + "SELECT TREAT_LONG(allowedAge.value), doc.name FROM Long(3 VALUES) allowedAge LEFT JOIN Document doc" +
                onClause("TREAT_LONG(allowedAge.value) = :allowedAge_value_0 OR TREAT_LONG(allowedAge.value) = :allowedAge_value_1 OR TREAT_LONG(allowedAge.value) = :allowedAge_value_2 AND doc.age = TREAT_LONG(allowedAge.value)") +
                " ORDER BY TREAT_LONG(allowedAge.value) ASC";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(3, resultList.size());

        assertEquals(1L, resultList.get(0).get(0));
        assertEquals("doc1", resultList.get(0).get(1));

        assertEquals(2L, resultList.get(1).get(0));
        assertNull(resultList.get(1).get(1));

        assertEquals(3L, resultList.get(2).get(0));
        assertNull(resultList.get(2).get(1));
    }

    @Test
    // NOTE: Entity joins are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionWithEntity() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(IntIdEntity.class, "intEntity", Arrays.asList(
                new IntIdEntity("doc1"),
                new IntIdEntity("docX")
        ));
        cb.leftJoinOn(Document.class, "doc")
                .on("doc.name").eqExpression("intEntity.name")
        .end();
        cb.select("intEntity.name");
        cb.select("doc.name");
        cb.orderByAsc("intEntity.name");

        String expected = ""
                + "SELECT intEntity.name, doc.name FROM IntIdEntity(2 VALUES) intEntity LEFT JOIN Document doc" +
                onClause("intEntity.id = :intEntity_id_0 OR intEntity.name = :intEntity_name_0 OR intEntity.value = :intEntity_value_0 OR intEntity.id = :intEntity_id_1 OR intEntity.name = :intEntity_name_1 OR intEntity.value = :intEntity_value_1 AND doc.name = intEntity.name") +
                " ORDER BY " + renderNullPrecedence("intEntity.name", "ASC", "LAST");

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(2, resultList.size());

        assertEquals("doc1", resultList.get(0).get(0));
        assertEquals("doc1", resultList.get(0).get(1));

        assertEquals("docX", resultList.get(1).get(0));
        assertNull(resultList.get(1).get(1));
    }

    @Test
    // NOTE: Entity joins are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionParameter() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(IntIdEntity.class, "intEntity", 1);
        cb.leftJoinOn(Document.class, "doc")
                .on("doc.name").eqExpression("intEntity.name")
        .end();
        cb.select("intEntity.name");
        cb.select("doc.name");
        cb.orderByAsc("intEntity.name");

        String expected = ""
                + "SELECT intEntity.name, doc.name FROM IntIdEntity(1 VALUES) intEntity LEFT JOIN Document doc" +
                onClause("intEntity.id = :intEntity_id_0 OR intEntity.name = :intEntity_name_0 OR intEntity.value = :intEntity_value_0 AND doc.name = intEntity.name") +
                " ORDER BY " + renderNullPrecedence("intEntity.name", "ASC", "LAST");

        assertEquals(expected, cb.getQueryString());

        List<Tuple> resultList;

        // Didn't bind parameters
        CatchException.verifyException(cb, IllegalArgumentException.class).getResultList();

        // Bind wrong values parameter type
        try {
            // Unfortunately the setParameter method does not seem to get proxied..
//            CatchException.verifyException(cb, IllegalArgumentException.class).setParameter("intEntity", Collections.emptyList());
            cb.setParameter("intEntity", 1L);
            Assert.fail("Expected IllegalArgumentException!");
        } catch (IllegalArgumentException ex) {}

        // Bind wrong parameter types
        cb.setParameter("intEntity", Arrays.asList(1L));
        CatchException.verifyException(cb, IllegalArgumentException.class).getResultList();

        // Values with matching entry
        cb.setParameter("intEntity", Arrays.asList(new IntIdEntity("doc1")));
        resultList = cb.getResultList();
        assertEquals(1, resultList.size());

        assertEquals("doc1", resultList.get(0).get(0));
        assertEquals("doc1", resultList.get(0).get(1));

        // Values with no matching entry
        cb.setParameter("intEntity", Arrays.asList(new IntIdEntity("docX")));
        resultList = cb.getResultList();
        assertEquals(1, resultList.size());

        assertEquals("docX", resultList.get(0).get(0));
        assertNull(resultList.get(0).get(1));

        // Empty values
        cb.setParameter("intEntity", Collections.emptyList());
        resultList = cb.getResultList();
        assertEquals(0, resultList.size());
    }

    @Test
    // NOTE: Entity joins are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionParameterWithoutNullsFilter() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.setProperty(ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS, "false");

        cb.fromValues(IntIdEntity.class, "intEntity", 1);
        cb.leftJoinOn(Document.class, "doc")
                .on("doc.name").eqExpression("intEntity.name")
                .end();
        cb.select("intEntity.name");
        cb.select("doc.name");
        cb.orderByAsc("intEntity.name");

        // Empty values
        cb.setParameter("intEntity", Collections.emptyList());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());

        assertNull(resultList.get(0).get(0));
        assertNull(resultList.get(0).get(1));
    }

    @Test
    // NOTE: Entity joins are supported since Hibernate 5.1, Datanucleus 5 and latest Eclipselink
//    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    // No hibernate for now, see https://hibernate.atlassian.net/browse/HHH-11340
    // H2 does not support parameters in the CTE http://dba.stackexchange.com/a/78449
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoH2.class })
    public void testValuesEntityFunctionInCte() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.setProperty(ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS, "false");

        cb.with(PersonCTE.class)
            .fromValues(IntIdEntity.class, "intEntity", 1)
            .leftJoinOn(Document.class, "doc")
                .on("doc.name").eqExpression("intEntity.name")
            .end()
            .innerJoin("doc.owner", "owner")
            .bind("id").select("owner.id")
            .bind("name").select("owner.name")
            .bind("age").select("owner.age")
            .bind("idx").select("1")
            .bind("owner").select("owner")
        .end()
        .from(PersonCTE.class)
        .select("id")
        .select("name");

        // Empty values
        cb.setParameter("intEntity", Arrays.asList(new IntIdEntity("doc1")));
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());

        assertNotNull(resultList.get(0).get(0));
        assertEquals("p1", resultList.get(0).get(1));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionWithCteEntity() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.setProperty(ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS, "false");

        cb.fromValues(PersonCTE.class, "cteValues", 2)
            .select("cteValues.id")
            .where("cteValues.id").isNotNull();

        final PersonCTE personCTE = new PersonCTE();
        personCTE.setId(1L);
        cb.setParameter("cteValues", Arrays.asList(personCTE));
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());

        assertEquals(personCTE.getId(), resultList.get(0).get(0));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testFromValuesWithEmbeddables() {
        final Document doc1 = new Document("doc1");
        doc1.setNameObject(new NameObject("doc1Primary", "doc1Secondary"));
        final Document doc2 = new Document("doc2");
        doc2.setNameObject(new NameObject("doc2Primary", "doc2Secondary"));
        CriteriaBuilder<String> cb = cbf.create(em, String.class)
                .fromValues(Document.class, "docs", Arrays.asList(doc1, doc2))
                .select("docs.nameObject.primaryName", "name")
                .orderByAsc("name");

        final List<String> primaryNames = cb.getResultList();
        assertEquals(2, primaryNames.size());
        assertEquals(doc1.getNameObject().getPrimaryName(), primaryNames.get(0));
        assertEquals(doc2.getNameObject().getPrimaryName(), primaryNames.get(1));
    }

    @Test
    // H2 does not support parameters in the CTE http://dba.stackexchange.com/a/78449
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoH2.class })
    public void testValuesEntityFunctionWithCteInCteWithSetOperation() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.setProperty(ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS, "false");

        cb.withStartSet(PersonCTE.class)
            .endSet()
            .unionAll()
            .fromValues(DocumentNodeCTE.class, "docNode", 1)
            .from(Document.class, "doc")
            .where("doc.id").eqExpression("docNode.id")
            .innerJoin("doc.owner", "owner")
            .bind("id").select("owner.id")
            .bind("name").select("owner.name")
            .bind("age").select("owner.age")
            .bind("idx").select("1")
            .bind("owner").select("owner")
            .endSet()
        .end()
        .from(PersonCTE.class)
        .select("id")
        .select("name");

        final DocumentNodeCTE d1Node = new DocumentNodeCTE();
        d1Node.setId(d1.getId());
        cb.setParameter("docNode", Arrays.asList(d1Node));
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());

        assertEquals(p1.getId(), resultList.get(0).get(0));
        assertEquals(p1.getName(), resultList.get(0).get(1));
    }

    @Test
    // H2 does not support parameters in the CTE http://dba.stackexchange.com/a/78449
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class, NoH2.class })
    public void testIdentifiableValuesEntityFunction() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.setProperty(ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS, "false");

        cb.with(PersonCTE.class)
            .fromIdentifiableValues(Person.class, "persons", Arrays.asList(p1))
            .from(Person.class, "p")
            .where("p.id").eqExpression("persons")
            .bind("id").select("p.id")
            .bind("name").select("p.name")
            .bind("age").select("p.age")
            .bind("idx").select("1")
            .bind("owner").select("persons")
        .end()
        .from(PersonCTE.class)
        .select("id")
        .select("owner.id");

        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());

        assertEquals(p1.getId(), resultList.get(0).get(0));
        assertEquals(p1.getId(), resultList.get(0).get(1));
    }
}
