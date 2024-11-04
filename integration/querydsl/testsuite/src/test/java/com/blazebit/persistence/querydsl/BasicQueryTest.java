/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.FinalSetOperationCriteriaBuilder;
import com.blazebit.persistence.JoinType;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.Queryable;
import com.blazebit.persistence.spi.LateralStyle;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.base.jpa.category.*;
import com.blazebit.persistence.testsuite.entity.BookEntity;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.IdHolderCTE;
import com.blazebit.persistence.testsuite.entity.IntIdEntity;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.QDocument;
import com.blazebit.persistence.testsuite.entity.QIdHolderCTE;
import com.blazebit.persistence.testsuite.entity.QPerson;
import com.blazebit.persistence.testsuite.entity.QRecursiveEntity;
import com.blazebit.persistence.testsuite.entity.RecursiveEntity;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE1;
import com.blazebit.persistence.testsuite.entity.TestAdvancedCTE2;
import com.blazebit.persistence.testsuite.entity.TestCTE;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.Param;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.blazebit.persistence.querydsl.JPQLNextExpressions.*;
import static com.blazebit.persistence.querydsl.SetUtils.intersect;
import static com.blazebit.persistence.querydsl.SetUtils.union;
import static com.blazebit.persistence.querydsl.SetUtils.unionAll;
import static com.blazebit.persistence.testsuite.entity.QDocument.document;
import static com.blazebit.persistence.testsuite.entity.QIdHolderCTE.idHolderCTE;
import static com.blazebit.persistence.testsuite.entity.QPerson.person;
import static com.blazebit.persistence.testsuite.entity.QRecursiveEntity.recursiveEntity;
import static com.querydsl.core.types.dsl.Expressions.asNumber;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class BasicQueryTest extends AbstractCoreTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Override
    protected Class<?>[] getEntityClasses() {
        return new Class<?>[] {
                Document.class,
                Person.class,
                IntIdEntity.class,
                Version.class,
                BookEntity.class,
                RecursiveEntity.class,
                TestCTE.class,
                IdHolderCTE.class,
                TestAdvancedCTE1.class,
                TestAdvancedCTE2.class
        };
    }

    public void doInJPA(Consumer<EntityManager> function) {
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager entityManager) {
                function.accept(entityManager);
            }
        });
    }
    
    @Before
    public void setUp() {
        cleanDatabase();
        doInJPA(entityManager -> {
            for (int i = 0; i < 10; i++) {
                Person person = new Person();
                person.setName("Person " + i);
                Document testEntity = new Document();
                testEntity.setName("bogus " + i);
                testEntity.setOwner(person);
                entityManager.persist(testEntity);
            }
        });
    }

    @Test
    public void testSubqueryInCase() {
        doInJPA(em -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            JPQLNextQuery<Boolean> select = queryFactory
                    .from(document)
                    .select(Expressions.cases().when(
                            selectFrom(person).where(person.id.eq(1L)).exists()
                    ).then(true).otherwise(false));

            assertNotNull(select.getQueryString());
        });
    }

    @Test
    public void testFetchResults() {
        doInJPA(em -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QueryResults<Person> personQueryResults = queryFactory
                    .from(person)
                    .select(person)
                    .limit(1)
                    .offset(1)
                    .orderBy(person.id.asc())
                    .fetchResults();

            assertEquals(1L, personQueryResults.getResults().size());
            assertEquals(1L, personQueryResults.getOffset());
            assertEquals(1L, personQueryResults.getLimit());
            assertEquals(10L, personQueryResults.getTotal());
        });
    }

    @Test
    public void testExplicitJoinFollowedByImplicitJoin() {
        doInJPA(em -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            JPQLNextQuery<Tuple> query = queryFactory
                    .from(person)
                    .select(person.name, person.friend.name, person.friend.partnerDocument.name)
                    .leftJoin(person.friend.partnerDocument);

            String queryString = query.getQueryString();
            // Note the implicit join and the generated join alias
            assertEquals("SELECT person.name, friend_1.name, partnerDocument_1.name FROM Person person LEFT JOIN person.friend friend_1 LEFT JOIN friend_1.partnerDocument partnerDocument_1", queryString);
        });
    }

    @Test
    public void testExplicitJoinFollowedByImplicitJoin2() {
        doInJPA(em -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            JPQLNextQuery<Tuple> query = queryFactory
                    .from(person)
                    .select(person.name, person.friend.name, person.friend.partnerDocument.name, person.friend.friend.name)
                    .leftJoin(person.friend)
                    .leftJoin(person.friend.partnerDocument)
                    .leftJoin(person.friend.friend);

            String queryString = query.getQueryString();
            // Note the implicit join and the generated join alias
            assertEquals("SELECT person.name, friend_1.name, partnerDocument_1.name, friend_4.name FROM Person person LEFT JOIN person.friend friend_1 LEFT JOIN friend_1.partnerDocument partnerDocument_1 LEFT JOIN friend_1.friend friend_4", queryString);
        });
    }

    @Test
    public void testExplicitJoinFollowedByImplicitJoin3() {
        doInJPA(em -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            JPQLNextQuery<Tuple> query = queryFactory
                    .from(person)
                    .select(person.name, person.friend.name, person.friend.partnerDocument.name, person.friend.friend.name)
                    .leftJoin(person.friend)
                    .leftJoin(person.friend.partnerDocument).on(Expressions.TRUE.eq(Expressions.TRUE))
                    .leftJoin(person.friend.friend);

            expectedException.expectMessage("This association join requires an alias, like so: .join(person.friend.partnerDocument, QDocument.partnerDocument)");

            query.getQueryString();
        });
    }


    // NOTE: This requires advanced SQL support
    @Test
    @Category({ NoEclipselink.class, NoDatanucleus.class, NoOpenJPA.class })
    public void testFetchCount() {
        doInJPA(em -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            long totalCount = queryFactory
                    .from(person)
                    .select(person)
                    .limit(1)
                    .offset(1)
                    .orderBy(person.id.asc())
                    .fetchCount();

            assertEquals(1L, totalCount);
        });
    }

    @Test
    public void testModuloLongOperation() {
        doInJPA(em -> {
            BlazeJPAQuery<Tuple> query = new BlazeJPAQuery<>(em, cbf)
                    .from(person)
                    .select(person, person)
                    .where(bitwiseAndLong(person.id, 1L));

            assertNotNull(query.fetchResults());
        });
    }

    public static BooleanExpression bitwiseAndLong(NumberExpression<Long> expression, long bit) {
        NumberExpression<Long> mod = castToNum(Long.class, expression.divide(1L << bit).floor()).mod(2L);
        return mod.eq(1L);
    }

    @Test
    public void testPagination() {
        doInJPA(em -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            PagedList<Person> personQueryResults = queryFactory
                    .from(person)
                    .select(person)
                    .orderBy(person.id.asc())
                    .fetchPage(1, 1);

            assertEquals(1L, personQueryResults.size());
            assertEquals(1L, personQueryResults.getMaxResults());
            assertEquals(1L, personQueryResults.getFirstResult());
            assertEquals(10L, personQueryResults.getTotalSize());
        });
    }

    @Test
    public void testThroughBPVisitor() {
        doInJPA(entityManager -> {
            JPAQuery<Tuple> query = new JPAQuery<Document>(entityManager).from(document)
                    .select(document.name.as("blep"), document.name.substring(0, 2))
                    .where(document.name.length().gt(1));

            BlazeCriteriaBuilderRenderer<Tuple> blazeCriteriaBuilderRenderer = new BlazeCriteriaBuilderRenderer<>(cbf, entityManager, JPQLTemplates.DEFAULT);
            Queryable<Tuple, ?> queryable = blazeCriteriaBuilderRenderer.render(query);
            List<Tuple> fetch = queryable.getResultList();
            assertFalse(fetch.isEmpty());
        });
    }

    @Test
    public void testThroughBlazeJPAQuery() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            JPQLNextQuery<Tuple> query = queryFactory.from(document)
                    .select(document.name.as("blep"), document.name.substring(0, 2))
                    .where(document.name.length().gt(1));

            List<Tuple> fetch = query.fetch();
            assertFalse(fetch.isEmpty());
        });
    }

    @Test
    // NOTE: Querydsl integration needs JPA 2.2 for streaming to work
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoHibernate52.class})
    public void testResultStream() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            JPQLNextQuery<Tuple> query = queryFactory.from(document)
                    .select(document.name.as("blep"), document.name.substring(0, 2))
                    .where(document.name.length().gt(1));

            try (Stream<Tuple> stream = query.stream()) {
                Tuple next = stream.iterator().next();
                assertNotNull(next);
            }
        });
    }

    @Test
    public void testParameterExpression() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            Param<Integer> param = new Param<>(Integer.class, "theSuperName");

            JPQLNextQuery<Tuple> query = queryFactory.from(document)
                    .select(document.name.as("blep"), document.name.substring(0, 2))
                    .where(document.name.length().gt(param))
                    .set(param, 1);

            List<Tuple> fetch = query.fetch();
            assertFalse(fetch.isEmpty());
        });
    }

    // Note: Datanucleus has issues with  parameters in the select clause
    @Test
    @Category({NoDatanucleus.class})
    public void testParameterExpressionInSelect() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            Param<Integer> param = new Param<>(Integer.class, "theSuperName");

            JPQLNextQuery<Tuple> query = queryFactory.from(document)
                    .select(document.name.as("blep"), param)
                    .where(document.name.length().gt(param))
                    .set(param, 1);

            List<Tuple> fetch = query.fetch();
            assertFalse(fetch.isEmpty());
        });
    }

    @Test
    public void testSubQuery() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QDocument sub = new QDocument("sub");
            JPQLNextQuery<Tuple> query = queryFactory.from(document)
                    .select(document.name.as("blep"), document.name.substring(0, 2))
                    .where(document.id.in(select(sub.id).from(sub)));

            List<Tuple> fetch = query.fetch();
            assertFalse(fetch.isEmpty());
        });
    }

    // NOTE: Window functions were only introduced in MySQL8
    @Test
    @Category({ NoMySQLOld.class })
    public void testWindowFunction() {
        Assume.assumeTrue(dbmsDialect.supportsWindowFunctions());

        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QDocument sub = new QDocument("sub");

            JPQLNextQuery<Tuple> query = queryFactory.from(document)
                    .select(document.name.as("documentName"), rowNumber().over().orderBy(document.id), lastValue(document.name).over().partitionBy(document.id).orderBy(document.id))
                    .where(document.id.in(select(sub.id).from(sub)));

            List<Tuple> fetch = query.fetch();
            assertFalse(fetch.isEmpty());
        });
    }

    // NOTE: Window functions were only introduced in MySQL8
    @Test
    @Category({ NoMySQLOld.class })
    public void testNamedWindowFunction() {
        Assume.assumeTrue(dbmsDialect.supportsWindowFunctions());

        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QDocument sub = new QDocument("sub");

            NamedWindow namedWindow = new NamedWindow("namedWindow").partitionBy(document.id).orderBy(document.id);

            JPQLNextQuery<Tuple> query = queryFactory.from(document)
                    .window(namedWindow)
                    .select(document.name.as("documentName"), rowNumber().over(namedWindow), lastValue(document.name).over(namedWindow))
                    .where(document.id.in(select(sub.id).from(sub)));

            List<Tuple> fetch = query.fetch();
            assertFalse(fetch.isEmpty());
        });
    }


    @Test
    @Ignore("Filter support is work in progress")
    @Category({ NoMySQLOld.class })
    public void testFilteredWindowfunction() {
        JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

        Assume.assumeTrue(dbmsDialect.supportsWindowFunctions());

        doInJPA(entityManager -> {
            NamedWindow namedWindow = new NamedWindow("namedWindow").partitionBy(document.id).orderBy(document.id);

            JPQLNextQuery<?> query = queryFactory.from(document)
                    .window(namedWindow)
                    .select(
                            sum(document.age).filter(document.age.lt(literal(5))),
                            sum(document.age).filter(document.age.lt(literal(5))).over(namedWindow)
                    );

            String queryString = query.getQueryString();
            List<?> fetch = query.fetch();
            assertFalse(fetch.isEmpty());
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testNestedSubQuery() {
        JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

        doInJPA(entityManager -> {
            QDocument sub = new QDocument("sub");
            QDocument sub2 = new QDocument("sub2");
            JPQLNextQuery<Tuple> query = queryFactory.from(document)
                    .select(document.name.as("documentName"), document.name.substring(0, 2))
                    .where(document.id.in(select(sub.id).from(sub).where(sub.id.in(select(sub2.id).from(sub2).where(sub2.id.eq(sub.id)))).orderBy(sub.id.asc()).limit(5)));

            List<Tuple> fetch = query.fetch();
            assertFalse(fetch.isEmpty());
        });
    }

    @Test
    public void testTransformBlazeJPAQuery() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            Map<Long, String> blep = queryFactory.from(document)
                    .where(document.name.length().gt(1))
                    .groupBy(document.id, document.name)
                    .transform(GroupBy.groupBy(document.id).as(document.name));

            document.getRoot();
            assertFalse(blep.isEmpty());
        });
    }

    @Test
    public void testAssociationJoin() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            Map<Person, List<Document>> booksByAuthor = queryFactory
                    .from(person)
                    .innerJoin(person.ownedDocuments, document)
                    .transform(GroupBy.groupBy(person).as(GroupBy.list(document)));

            assertNotNull(booksByAuthor);
        });
    }

    // NOTE: Entity joins are only supported on Hibernate 5.1+
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class })
    public void testEntityJoin() {
        Assume.assumeTrue(jpaProvider.supportsEntityJoin());

        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QPerson otherAuthor = new QPerson("otherAuthor");
            QDocument otherBook = new QDocument("otherBook");
            Map<Person, List<Document>> booksByAuthor = queryFactory
                    .from(otherAuthor)
                    .innerJoin(otherBook).on(otherBook.owner.eq(otherAuthor))
                    .transform(GroupBy.groupBy(otherAuthor).as(GroupBy.list(otherBook)));

            assertNotNull(booksByAuthor);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testFromValues() {
        JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

        doInJPA(entityManager -> {
            Document theBook = new Document();
            theBook.setId(1337L);
            theBook.setName("test");

            List<Document> fetch = queryFactory
                    .fromValues(document, Collections.singleton(theBook))
                    .select(document)
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: Querydsl integration needs JPA 2.2 for streaming to work
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoHibernate52.class})
    public void testFromValuesStream() {
        JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

        doInJPA(entityManager -> {
            Document theBook = new Document();
            theBook.setId(1337L);
            theBook.setName("test");

            try (Stream<Document> fetch = queryFactory
                    .fromValues(document, Collections.singleton(theBook))
                    .select(document)
                    .stream()) {

                Document result = fetch.iterator().next();
                assertNotNull(result);
            }

        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testFromValuesAttributes() {
        JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

        doInJPA(entityManager -> {
            StringPath bookName = Expressions.stringPath("bookName");

            List<String> fetch = queryFactory
                    .fromValues(document.name, bookName, Collections.singleton("book"))
                    .select(bookName)
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoMySQL.class, NoFirebird.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void  testComplexUnion() {
        JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

        doInJPA(entityManager -> {
            Person person = new Person();
            person.setName("Person");
            entityManager.persist(person);

            Document theBook = new Document();
            theBook.setId(1337L);
            theBook.setName("test");
            theBook.setOwner(person);
            entityManager.merge(theBook);

            Document theSequel = new Document();
            theSequel.setId(42L);
            theSequel.setName("test2");
            theSequel.setOwner(person);
            entityManager.merge(theSequel);
        });

        doInJPA(entityManager -> {
            List<Document> fetch = queryFactory
                    .union(
                        queryFactory.intersect(
                                select(document).from(document).where(document.id.eq(41L)),
                                queryFactory.except(
                                        select(document).from(document).where(document.id.eq(42L)),
                                        select(document).from(document).where(document.id.eq(43L)))),
                        select(document).from(document).where(document.id.eq(46L)))
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    // NOTE: MySQL only supports the UNION set operation
    @Test
    @Category({ NoMySQL.class, NoFirebird.class, NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testComplexSubqueryUnion() {
        doInJPA(entityManager -> {
            Person person = new Person();
            person.setName("Person");
            entityManager.persist(person);

            Document theBook = new Document();
            theBook.setId(1337L);
            theBook.setName("test");
            theBook.setOwner(person);
            entityManager.merge(theBook);

            Document theSequel = new Document();
            theSequel.setId(42L);
            theSequel.setName("test2");
            theSequel.setOwner(person);
            entityManager.merge(theSequel);
        });

        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            SetExpression<Long> union = queryFactory
                    .union(select(document.id).from(document).where(document.id.eq(1337L)),
                            queryFactory.intersect(
                                    select(document.id).from(document).where(document.id.eq(41L)),
                                    queryFactory.except(
                                            select(document.id).from(document).where(document.id.eq(42L)),
                                            select(document.id).from(document).where(document.id.eq(43L)))),
                            select(document.id).from(document).where(document.id.eq(46L))
                    );

            QDocument book2 = new QDocument("secondBook");

            List<Document> fetch = queryFactory
                    .select(book2)
                    .from(book2).where(book2.id.in(union))
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testCTE() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            List<Long> fetch = queryFactory
                    .with(idHolderCTE, idHolderCTE.id).as(select(document.id).from(document))
                    .select(idHolderCTE.id).from(idHolderCTE)
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testCTEClone() {
        doInJPA(entityManager -> {
            BlazeJPAQuery<Long> from = new BlazeJPAQuery<Document>(entityManager, cbf)
                    .with(idHolderCTE, new BlazeJPAQuery<>().bind(idHolderCTE.id, document.id).from(document))
                    .select(idHolderCTE.id).from(idHolderCTE);
            BlazeJPAQuery<Long> clone = from
                    .clone();

            assertNotNull(clone);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    // NOTE: Querydsl integration needs JPA 2.2 for streaming to work
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoHibernate52.class})
    public void testCTEStream() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            try (Stream<Long> fetch = queryFactory
                    .with(idHolderCTE, idHolderCTE.id).as(select(document.id).from(document))
                    .select(idHolderCTE.id).from(idHolderCTE)
                    .stream()) {
                Long next = fetch.iterator().next();
                assertNotNull(next);
            }
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testCTEWithBinds() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            List<Long> fetch = queryFactory
                    .with(idHolderCTE, select(new Binds<IdHolderCTE>().bind(idHolderCTE.id, document.id)).from(document))
                    .select(idHolderCTE.id).from(idHolderCTE)
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testCTEWithBindsWithAlias() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            List<Long> fetch = queryFactory
                    .with(idHolderCTE, select(new Binds<IdHolderCTE>().bind(idHolderCTE.id, document.id.as("alias"))).from(document))
                    .select(idHolderCTE.id).from(idHolderCTE)
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testCTEWithBinds2() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            List<Long> fetch = queryFactory
                .with(idHolderCTE, select(
                        bind(idHolderCTE.id, document.id)).from(document))
                .select(idHolderCTE.id).from(idHolderCTE)
                .fetch();

                assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    // NOTE: MySQL only supports the UNION set operation
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQL.class })
    public void testCTEUnion() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            List<Long> fetch = queryFactory
                    .with(idHolderCTE, idHolderCTE.id).as(union(select(document.id).from(document), intersect(select(document.id).from(document), select(document.id).from(document))))
                    .select(idHolderCTE.id).from(idHolderCTE)
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class })
    public void testCTEFromValues() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            Document theBook = new Document();
            theBook.setId(1337L);
            theBook.setName("test");

            List<Long> fetch = queryFactory
                    .with(idHolderCTE, idHolderCTE.id).as(queryFactory
                            .fromValues(document, Collections.singleton(theBook))
                            .select(document.id))
                    .select(idHolderCTE.id).from(idHolderCTE)
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    // NOTE: Recursive CTE support was only added in MySQL8
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class })
    public void testRecursiveCTEUnion() {
        Assume.assumeTrue(dbmsDialect.supportsWithClause());
        JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);


        doInJPA(entityManager -> {
            List<Long> fetch = queryFactory
                    .withRecursive(idHolderCTE, idHolderCTE.id).as(unionAll(select(document.id).from(document).where(document.id.eq(1L)), select(document.id).from(document)
                            .from(idHolderCTE).where(idHolderCTE.id.add(1L).eq(document.id))))
                    .select(idHolderCTE.id).from(idHolderCTE)
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    // NOTE: Recursive CTE support was only added in MySQL8
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class })
    public void testRecursiveBindBuilder() {
        Assume.assumeTrue(dbmsDialect.supportsWithClause());

        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            List<Long> fetch = queryFactory
                    .withRecursive(idHolderCTE, idHolderCTE.id)
                        .as(queryFactory.unionAll(
                                select(document.id).from(document).where(document.id.eq(1L)),
                                select(document.id).from(document).join(idHolderCTE).on(idHolderCTE.id.add(1L).eq(document.id))))
                    .select(idHolderCTE.id).from(idHolderCTE)
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testInlineEntityWithLimit() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);
            
            QRecursiveEntity recursiveEntity = new QRecursiveEntity("t");

            List<RecursiveEntity> fetch = queryFactory
                    .select(recursiveEntity)
                    .from(select(recursiveEntity)
                            .from(recursiveEntity)
                            .where(recursiveEntity.parent.name.eq("root1"))
                            .orderBy(recursiveEntity.name.asc())
                            .limit(1L), recursiveEntity)
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class })
    public void testCteInSubquery() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QIdHolderCTE idHolderCTE = QIdHolderCTE.idHolderCTE;

            List<Document> fetch = queryFactory
                    .select(document)
                    .from(document)
                    .innerJoin(selectFrom(idHolderCTE)
                                    .with(idHolderCTE, idHolderCTE.id).as(select(document.id).from(document)),
                            idHolderCTE).on(idHolderCTE.id.eq(document.id))
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: Entity joins are only supported on Hibernate 5.1+
    // NOTE: Hibernate 5.1 renders t.id = tSub.id rather than t = tSub
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class})
    public void testMultipleInlineEntityWithLimitJoin() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QRecursiveEntity t = new QRecursiveEntity("t");
            QRecursiveEntity subT = new QRecursiveEntity("subT");
            QRecursiveEntity subT2 = new QRecursiveEntity("subT2");

            List<RecursiveEntity> fetch = queryFactory
                    .select(t)
                    .from(queryFactory.select(t).from(t)
                            .leftJoin(selectFrom(subT).where(subT.parent.name.eq("root1")).orderBy(subT.name.asc()).limit(1), subT)
                            .on(t.eq(subT))
                            .where(t.parent.name.eq("root1"))
                            .orderBy(t.name.asc())
                            .limit(1L), t)
                    .leftJoin(selectFrom(subT2)
                            .where(subT2.parent.name.eq("root1"))
                            .orderBy(subT2.name.asc())
                            .limit(1), subT2)
                    .on(t.eq(subT2))
                    .fetch();

            assertNotNull(fetch);

        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    // NOTE: Entity joins are only supported on Hibernate 5.1+
    // Oracle, H2 and MySQL <8 do not support lateral joins
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOracle.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoH2.class, NoMySQLOld.class })
    public void testMultipleInlineEntityWithLimitJoinLateral() {
        Assume.assumeFalse(dbmsDialect.getLateralStyle() == LateralStyle.NONE);

        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QRecursiveEntity t = new QRecursiveEntity("t");
            QRecursiveEntity subT = new QRecursiveEntity("subT");
            QRecursiveEntity subT2 = new QRecursiveEntity("subT2");
            QRecursiveEntity subT3 = new QRecursiveEntity("subT3");

            queryFactory
                    .select(t)
                    .from(queryFactory.selectFrom(t)
                            .leftJoin(selectFrom(subT).where(subT.parent.name.eq("root1")).orderBy(subT.name.asc()).limit(1), subT)
                            .on(t.eq(subT))
                            .where(t.parent.name.eq("root1"))
                            .orderBy(t.name.asc())
                            .limit(1L), t)
                    .leftJoin(selectFrom(subT2)
                        .where(subT2.parent.name.eq("root1"))
                        .orderBy(subT2.name.asc())
                        .limit(1), subT3)
                        .on(t.eq(subT3))
                        .lateral()
                    .fetch();

        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    // NOTE: Entity joins are only supported on Hibernate 5.1+
    // Oracle, H2 and MySQL <8 do not support lateral joins
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoH2.class, NoMySQLOld.class, NoOracle.class })
    public void testJoinInlineEntityWithLimit() {
        Assume.assumeFalse(dbmsDialect.getLateralStyle() == LateralStyle.NONE);

        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QRecursiveEntity t = new QRecursiveEntity("t");
            QRecursiveEntity subT = new QRecursiveEntity("subT");
            QRecursiveEntity subT2 = new QRecursiveEntity("subT2");

            List<Tuple> fetch = queryFactory
                    .select(t, subT2)
                    .from(t)
                    .leftJoin(select(subT).from(t.children, subT).orderBy(subT.id.asc()).limit(1), subT2)
                    .lateral()
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    // NOTE: Entity joins are only supported on Hibernate 5.1+
    // Oracle, H2 and MySQL <8 do not support lateral joins
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoH2.class, NoMySQLOld.class, NoOracle.class })
    public void testJoinInlineEntityWithLimitWithCTE() {
        Assume.assumeFalse(dbmsDialect.getLateralStyle() == LateralStyle.NONE);

        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QRecursiveEntity t = new QRecursiveEntity("t");
            QRecursiveEntity subT = new QRecursiveEntity("subT");
            QRecursiveEntity subT2 = new QRecursiveEntity("subT2");

            List<Tuple> fetch = queryFactory
                    .with(idHolderCTE, idHolderCTE.id)
                    .as(select(document.id).from(document).where(document.id.eq(1L)))
                    .select(t, subT2)
                    .from(t)
                    .leftJoin(select(subT).from(t.children, subT).orderBy(subT.id.asc()).limit(1), subT2)
                    .where(t.id.in(select(idHolderCTE.id).from(idHolderCTE)))
                    .lateral()
                    .fetch();

            assertNotNull(fetch);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    // NOTE: Entity joins are only supported on Hibernate 5.1+
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoMySQL.class })
    public void testJoinInlineWithLimitUnion() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QRecursiveEntity t = new QRecursiveEntity("t");
            QRecursiveEntity subT = new QRecursiveEntity("subT");
            QRecursiveEntity subT2 = new QRecursiveEntity("subT2");
            QRecursiveEntity subT3 = new QRecursiveEntity("subT3");

            JPQLQuery<RecursiveEntity> subA = select(
                    new Binds<RecursiveEntity>()
                        .bind(recursiveEntity.id, subT.id)
                        .bind(recursiveEntity.name, subT.name)
                        .bind(recursiveEntity.parent, subT.parent)
            ).from(subT);

            JPQLQuery<RecursiveEntity> subB = select(
                    new Binds<RecursiveEntity>()
                        .bind(recursiveEntity.id, subT3.id)
                        .bind(recursiveEntity.name, subT3.name)
                        .bind(recursiveEntity.parent, subT3.parent)).from(subT3);

            SubQueryExpression<RecursiveEntity> union = queryFactory.unionAll(
                    subA,
                    subB
            );

            queryFactory
                    .select(t, subT2)
                    .from(t)
                    .leftJoin(union, subT2).on(subT2.eq(subT2))
                    .fetch();
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    // NOTE: Entity joins are only supported on Hibernate 5.1+
    // NOTE: MySQL (also MySQL 8) doesn't yet support 'nesting of unions at the right-hand side'
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoMySQL.class })
    public void testJoinInlineEntityWithLimitUnion() {
        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QRecursiveEntity t = new QRecursiveEntity("t");
            QRecursiveEntity subT = new QRecursiveEntity("subT");
            QRecursiveEntity subT2 = new QRecursiveEntity("subT2");
            QRecursiveEntity subT3 = new QRecursiveEntity("subT3");

            JPQLQuery<RecursiveEntity> subA = selectFrom(subT).where(subT.id.lt(5L));
            JPQLQuery<RecursiveEntity> subB = selectFrom(subT3).where(subT3.id.gt(10L));

            SubQueryExpression<RecursiveEntity> union = queryFactory.unionAll(
                    subA,
                    subB
            );

            queryFactory
                    .select(t, subT2)
                    .from(t)
                    .leftJoin(union, subT2).on(subT2.eq(subT2))
                    .fetch();
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    // NOTE: Entity joins are only supported on Hibernate 5.1+
    // Oracle, H2 and MySQL <8 do not support lateral joins
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoHibernate42.class, NoHibernate43.class, NoHibernate50.class, NoHibernate51.class, NoH2.class, NoMySQLOld.class })
    public void testJoinInlineLateralEntityWithLimitUnion() {
        Assume.assumeFalse(dbmsDialect.getLateralStyle() == LateralStyle.NONE);

        doInJPA(entityManager -> {
            JPQLNextQueryFactory queryFactory = new BlazeJPAQueryFactory(em, cbf);

            QRecursiveEntity t = new QRecursiveEntity("t");
            QRecursiveEntity subT = new QRecursiveEntity("subT");
            QRecursiveEntity subT2 = new QRecursiveEntity("subT2");
            QRecursiveEntity subT3 = new QRecursiveEntity("subT3");

            JPQLQuery<RecursiveEntity> subA = selectFrom(subT).where(subT.id.lt(5L));
            JPQLQuery<RecursiveEntity> subB = selectFrom(subT3).where(subT3.id.gt(10L));

            SubQueryExpression<RecursiveEntity> union = queryFactory.unionAll(
                    subA,
                    subB
            );

            expectedException.expect(IllegalStateException.class);
            expectedException.expectMessage("Lateral join with set operations is not yet supported!");

            queryFactory
                    .select(t, subT2)
                    .from(t)
                    .leftJoin(union, subT2).on(subT2.eq(subT2))
                    .lateral()
                    .fetch();
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class })
    public void testMultipleFromEntitySubqueries() {
        doInJPA(em -> {
            String expected = cbf.create(em, Person.class)
                    .fromEntitySubquery(Person.class, "a", "person")
                    .where("person.id").eqLiteral(1L)
                    .end()
                    .fromEntitySubquery(Person.class, "b", "person")
                    .where("person.id").eqLiteral(2L)
                    .end()
                    .joinOnEntitySubquery("b", Person.class, "c", "person", JoinType.INNER)
                    .where("person.id").eqLiteral(3L)
                    .end().on("1").eqExpression("1").end()
                    .select("a").select("c")
                    .getQueryString();


            BlazeJPAQuery<Tuple> select = new BlazeJPAQuery<>(em, cbf)
                    .from(selectFrom(person).where(person.id.eq(literal(1L))), new QPerson("a"))
                    .from(selectFrom(person).where(person.id.eq(literal(2L))), new QPerson("b"))
                    .innerJoin(selectFrom(person).where(person.id.eq(literal(3L))), new QPerson("c"))
                    .on(Expressions.ONE.eq(Expressions.ONE))
                    .select(new QPerson("a"), new QPerson("c"));


            String result = select.getQueryString();
            assertEquals(expected, result);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class })
    public void testCteInLeftNestedSet() {
        doInJPA(em -> {
            FinalSetOperationCriteriaBuilder<Tuple> cb = cbf.startSet(em, Tuple.class)
                    .with(IdHolderCTE.class).from(Document.class).bind("id").select("id").end()
                    .fromEntitySubquery(Document.class, "a", "document").where("responsiblePerson").in()
                    .from(Person.class, "person").select("person").where("id").in()
                    .from(IdHolderCTE.class).select("id").end().end().end()
                    .select("a").select("a.age")
                    .union()
                    .fromEntitySubquery(Document.class, "b", "document").where("responsiblePerson").in()
                    .from(Person.class, "person").select("person").where("id").eqLiteral(2L).end().end()
                    .select("b").select("b.age")
                    .endSet()
                    .union()
                    .fromEntitySubquery(Document.class, "c", "document").whereExists()
                    .from(Person.class, "person").select("1").where("id").eqLiteral(3L).end().end()
                    .select("c").select("c.age")
                    .endSet();

            String expected = cb.getQueryString();

            QDocument a = new QDocument("a");
            QDocument b = new QDocument("b");
            QDocument c = new QDocument("c");

            BlazeJPAQuery<Tuple> queryA = new BlazeJPAQuery<>(em, cbf)
                    .from(select(document).from(document).where(document.responsiblePerson.in(new BlazeJPAQuery<>()
                            .with(idHolderCTE, idHolderCTE.id).as(select(document.id).from(document))
                            .from(person).select(person)
                            .where(person.id.in(select(idHolderCTE.id).from(idHolderCTE))))), a).select(a, a.age);

            BlazeJPAQuery<Tuple> queryB = new BlazeJPAQuery<>()
                    .from(selectFrom(document).where(document.responsiblePerson.in(selectFrom(person).where(person.id.eq(literal(2L))))), b).select(b, b.age);

            BlazeJPAQuery<Tuple> queryC = new BlazeJPAQuery<>()
                    .from(selectFrom(document).where(selectOne().from(person).where(person.id.eq(literal(3L))).exists()), c).select(c, c.age);

            SetExpression<Tuple> union = new BlazeJPAQuery<>(em, cbf).union(new BlazeJPAQuery<>().union(queryA, queryB), queryC);
            String queryString = union.getQueryString();
            assertEquals(expected, queryString);
        });
    }

    // NOTE: No advanced sql support for Datanucleus, Eclipselink and OpenJPA yet
    @Test
    @Category({ NoDatanucleus.class, NoEclipselink.class, NoOpenJPA.class, NoMySQLOld.class })
    public void testExpressionEqualToSubquery() {
        doInJPA(em -> {
            CriteriaBuilder<Person> select = cbf.create(em, Person.class)
                    .from(Person.class, "person")
                    .where("person.id")
                    .eqSubqueries("a")
                    .with("a").from(Document.class, "document").select("document.id").setMaxResults(1).end()
                    .end()
                    .select("person");

            String expected = select
                    .getQueryString();

            BlazeJPAQuery<Person> query = new BlazeJPAQuery<>(em, cbf)
                    .from(person)
                    .where(person.id.eq(select(document.id).from(document).limit(1)))
                    .select(person);


            String actual = query.getQueryString();
            assertEquals(expected, actual);
        });
    }

}
