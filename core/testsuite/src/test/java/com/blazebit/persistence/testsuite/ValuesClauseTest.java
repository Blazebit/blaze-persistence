/*
 * Copyright 2014 - 2021 Blazebit.
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

import com.blazebit.persistence.ConfigurationProperties;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate50;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate51;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQLOld;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOpenJPA;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.DocumentNodeCTE;
import com.blazebit.persistence.testsuite.entity.DocumentType;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.NameObject;
import com.blazebit.persistence.testsuite.entity.NameObjectContainer;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.PersonCTE;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ValuesClauseTest extends AbstractCoreTest {

    private static TimeZone timeZone;

    private Document d1;
    private Person p1;

    @Override
    protected Class<?>[] getEntityClasses() {
        return concat(super.getEntityClasses(), new Class<?>[]{
                PersonCTE.class,
                DocumentNodeCTE.class
        });
    }

    // Thanks to the MySQL driver not being able to handle timezones correctly, we must run this in the UTC timezone...

    @Override
    protected boolean recreateDataSource() {
        // Some drivers have timezone information bound to the connection
        // So we have to recreate the data source to get the newly configured time zones for connections
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }
        return true;
    }

    @Override
    protected DataSource createDataSource(Map<Object, Object> properties, Consumer<Connection> connectionCustomizer) {
        // Set the producer timezone
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        resetTimeZoneCaches();

        return super.createDataSource(properties, connectionCustomizer);
    }

    @AfterClass
    public static void after() {
        TimeZone.setDefault(timeZone);
        resetTimeZoneCaches();
    }

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                p1 = new Person("p1");
                d1 = new Document("doc1", 1);
                d1.setNameObject(new NameObject("123", "abc"));
                d1.getNameContainers().add(new NameObjectContainer("test", new NameObject("123", "abc")));
                d1.setOwner(p1);

                em.persist(p1);
                em.persist(d1);
            }
        });
        setUp();
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
        cb.where("doc.age").eqExpression("allowedAge");
        cb.select("doc.name");
        cb.select("allowedAge");

        String expected = ""
                + "SELECT doc.name, allowedAge FROM Long(1 VALUES) allowedAge, Document doc WHERE doc.age = allowedAge";
        
        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("doc1", resultList.get(0).get(0));
        assertEquals(1L, resultList.get(0).get(1));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionWithEmbeddable() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(NameObject.class, "embeddable", Collections.singleton(new NameObject("abc", "123")));
        cb.from(Document.class, "doc");
        cb.where("doc.nameObject.primaryName").eqExpression("embeddable.secondaryName");
        cb.where("doc.nameObject.secondaryName").eqExpression("embeddable.primaryName");
        cb.select("doc.name");
        cb.select("embeddable");

        String expected = ""
                + "SELECT doc.name, embeddable FROM NameObject(1 VALUES) embeddable, Document doc" +
                " WHERE doc.nameObject.primaryName = embeddable.secondaryName AND doc.nameObject.secondaryName = embeddable.primaryName";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("doc1", resultList.get(0).get(0));
        assertEquals(new NameObject("abc", "123"), resultList.get(0).get(1));
    }

    @Test
    // NOTE: Only the latest Hibernate 5.2 properly implements support for selecting element collections
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionWithPluralOnlyEmbeddable() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(NameObjectContainer.class, "embeddable", Collections.singleton(new NameObjectContainer("test", new NameObject("abc", "123"))));
        cb.from(Document.class, "doc");
        cb.where("doc.nameContainers.name").eqExpression("embeddable.name");
        cb.where("doc.nameContainers.nameObject.primaryName").eqExpression("embeddable.nameObject.secondaryName");
        cb.where("doc.nameContainers.nameObject.secondaryName").eqExpression("embeddable.nameObject.primaryName");
        cb.select("doc.name");
        cb.select("embeddable");

        String expected = ""
                + "SELECT doc.name, embeddable FROM NameObjectContainer(1 VALUES) embeddable, Document doc LEFT JOIN doc.nameContainers nameContainers_1" +
                " WHERE nameContainers_1.name = embeddable.name AND nameContainers_1.nameObject.primaryName = embeddable.nameObject.secondaryName AND nameContainers_1.nameObject.secondaryName = embeddable.nameObject.primaryName";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("doc1", resultList.get(0).get(0));
        assertEquals(new NameObjectContainer("test", new NameObject("abc", "123")), resultList.get(0).get(1));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionLikeBasic() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Document.class, "name", "n", Collections.singleton("someName"));
        cb.select("n");

        String expected = "SELECT n FROM String(1 VALUES LIKE Document.name) n";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("someName", resultList.get(0).get(0));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionLikeEnum() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Document.class, "documentType", "t", Collections.singleton(DocumentType.NOVEL));
        cb.select("t");

        String expected = "SELECT t FROM DocumentType(1 VALUES LIKE Document.documentType) t";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals(DocumentType.NOVEL, resultList.get(0).get(0));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionLikeCalendar() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        Calendar cal = Calendar.getInstance();
        cb.fromValues(Document.class, "creationDate", "t", Collections.singleton(cal));
        cb.select("t");

        String expected = "SELECT t FROM Calendar(1 VALUES LIKE Document.creationDate) t";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        // creationDate is just a DATE
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        assertEquals(cal, resultList.get(0).get(0, Calendar.class));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionLikeEmbeddable() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Document.class, "nameObject", "t", Collections.singleton(new NameObject("123", "abc")));
        cb.select("t");

        String expected = "SELECT t FROM NameObject(1 VALUES LIKE Document.nameObject) t";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals(new NameObject("123", "abc"), resultList.get(0).get(0));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionLikeEntity() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Document.class, "owner", "t", Collections.singleton(new Person(1L)));
        cb.select("t");

        String expected = "SELECT t FROM Person(1 VALUES LIKE Document.owner) t";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals(new Person(1L), resultList.get(0).get(0));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionLikePluralBasic() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Document.class, "strings", "t", Collections.singleton("test"));
        cb.select("t");

        String expected = "SELECT t FROM String(1 VALUES LIKE Document.strings) t";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("test", resultList.get(0).get(0));
    }

    @Test
    // NOTE: Only the latest Hibernate 5.2 properly implements support for selecting element collections
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionLikePluralEmbeddable() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Document.class, "names", "t", Collections.singleton(new NameObject("123", "abc")));
        cb.select("t");

        String expected = "SELECT t FROM NameObject(1 VALUES LIKE Document.names) t";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals(new NameObject("123", "abc"), resultList.get(0).get(0));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionLikePluralEntity() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Document.class, "partners", "t", Collections.singleton(new Person(1L)));
        cb.select("t.id");

        String expected = "SELECT t.id FROM Person(1 VALUES LIKE Document.partners) t";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals(1L, resultList.get(0).get(0));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionLikePluralIndex() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Document.class, "INDEX(people)", "t", Collections.singleton(1));
        cb.select("t");

        String expected = "SELECT t FROM Integer(1 VALUES LIKE INDEX(Document.people)) t";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals(1, resultList.get(0).get(0));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionLikePluralKey() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Document.class, "KEY(stringMap)", "t", Collections.singleton("key"));
        cb.select("t");

        String expected = "SELECT t FROM String(1 VALUES LIKE KEY(Document.stringMap)) t";

        assertEquals(expected, cb.getQueryString());
        List<Tuple> resultList = cb.getResultList();
        assertEquals(1, resultList.size());
        assertEquals("key", resultList.get(0).get(0));
    }

    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testIdentifiableValuesEntityFunctionGroupBy() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);

        cb.fromIdentifiableValues(Person.class, "p", Arrays.asList(p1))
                .fromValues(Integer.class, "someVal", Arrays.asList(1, 2))
                .select("p")
                .select("MAX(someVal)");

        String expected = "SELECT p, MAX(someVal) FROM Person(1 ID VALUES) p, Integer(2 VALUES) someVal GROUP BY p.id";

        // Only test that the EntitySelectResolveVisitor works properly here. In fact, this is only relevant for INSERT-SELECT
        assertEquals(expected, cb.getQueryString());
    }

    // Test for #305
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testValuesEntityFunctionWithParameterInSelect() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.fromValues(Long.class, "allowedAge", Arrays.asList(1L, 2L));
        cb.from(Document.class, "doc");
        cb.where("doc.age").eqExpression("allowedAge");
        cb.select("CASE WHEN doc.name = :param THEN doc.name ELSE '' END");
        cb.select("allowedAge");

        String expected = ""
                + "SELECT CASE WHEN doc.name = :param THEN doc.name ELSE '' END, allowedAge FROM Long(2 VALUES) allowedAge, Document doc " +
                "WHERE doc.age = allowedAge";

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
                .select("CASE WHEN doc.name = :param THEN allowedAge ELSE 2L END")
                .where("doc.age").eqExpression("allowedAge")
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
        cb.where("doc.age").eqExpression("allowedAge");
        cb.select("doc.name");
        cb.select("allowedAge");

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
            .on("doc.age").eqExpression("allowedAge")
        .end();
        cb.select("allowedAge");
        cb.select("doc.name");
        cb.orderByAsc("allowedAge");

        String expected = ""
                + "SELECT allowedAge, doc.name FROM Long(3 VALUES) allowedAge LEFT JOIN Document doc" +
                onClause("doc.age = allowedAge") +
                " ORDER BY allowedAge ASC";

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
                onClause("doc.name = intEntity.name") +
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
                onClause("doc.name = intEntity.name") +
                " ORDER BY " + renderNullPrecedence("intEntity.name", "ASC", "LAST");

        assertEquals(expected, cb.getQueryString());

        List<Tuple> resultList;

        // Didn't bind parameters
        verifyException(cb, IllegalArgumentException.class, r -> r.getResultList());

        // Bind wrong values parameter type
        verifyException(cb, IllegalArgumentException.class, r -> r.setParameter("intEntity", 1L));

        // Bind wrong parameter types
        cb.setParameter("intEntity", Arrays.asList(1L));
        verifyException(cb, IllegalArgumentException.class, r -> r.getResultList());

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
    // H2 before 1.4.199 does not support parameters in the CTE http://dba.stackexchange.com/a/78449
    @Category({ NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class })
    public void testValuesEntityFunctionInCte() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.setProperty(ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS, "false");

        cb.with(PersonCTE.class, false)
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
    // H2 before 1.4.199 does not support parameters in the CTE http://dba.stackexchange.com/a/78449
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class })
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
    // H2 before 1.4.199 does not support parameters in the CTE http://dba.stackexchange.com/a/78449
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class })
    public void testIdentifiableValuesEntityFunction() {
        CriteriaBuilder<Tuple> cb = cbf.create(em, Tuple.class);
        cb.setProperty(ConfigurationProperties.VALUES_CLAUSE_FILTER_NULLS, "false");

        cb.with(PersonCTE.class, false)
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
