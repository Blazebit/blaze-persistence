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

package com.blazebit.persistence.criteria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.criteria.Root;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.testsuite.tx.TxWork;
import com.googlecode.catchexception.CatchException;
import org.junit.Test;

import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Document_;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Person_;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SelectTest extends AbstractCoreTest {

    @Override
    protected void setUpOnce() {
        // TODO: Remove me when DataNucleus fixes map value access: https://github.com/datanucleus/datanucleus-rdbms/issues/230
        cleanDatabase();
    }

    @Test
    public void implicitRootEntitySelect() {
        BlazeCriteriaQuery<Document> cq = BlazeCriteria.get(cbf, Document.class);
        Root<Document> root = cq.from(Document.class, "document");

        CriteriaBuilder<Document> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void mapEntrySelect() {
        BlazeCriteriaQuery<Map.Entry> cq = BlazeCriteria.get(cbf, Map.Entry.class);
        BlazeRoot<Document> root = cq.from(Document.class, "document");

        cq.select(root.join(Document_.contacts, "contact").entry());

        CriteriaBuilder<Map.Entry> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT ENTRY(contact) FROM Document document JOIN document.contacts contact", criteriaBuilder.getQueryString());
    }

    @Test
    public void mapKeySelect() {
        BlazeCriteriaQuery<Integer> cq = BlazeCriteria.get(cbf, Integer.class);
        BlazeRoot<Document> root = cq.from(Document.class, "document");

        cq.select(root.join(Document_.contacts, "contact").key());

        CriteriaBuilder<Integer> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT KEY(contact) FROM Document document JOIN document.contacts contact", criteriaBuilder.getQueryString());
    }

    @Test
    public void mapIndexSelect() {
        BlazeCriteriaQuery<Integer> cq = BlazeCriteria.get(cbf, Integer.class);
        BlazeRoot<Document> root = cq.from(Document.class, "document");

        cq.select(root.join(Document_.people, "myPeople").index());

        CriteriaBuilder<Integer> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT INDEX(myPeople) FROM Document document JOIN document.people myPeople", criteriaBuilder.getQueryString());
    }

    @Test
    public void typeSelects() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
        BlazeRoot<Document> root = cq.from(Document.class, "document");
        BlazeMapJoin<Document, Integer, Person> contacts;

        cq.multiselect(
                root.type(),
                root.get(Document_.id).type(),
                root.join(Document_.people, "myPerson").type(),
                (contacts = root.join(Document_.contacts, "contact")).type(),
                root.join(Document_.partners, "partner").type(),
                contacts.key().type()
        );

        CriteriaBuilder<Tuple> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT TYPE(document), TYPE(document.id), TYPE(myPerson), TYPE(" + joinAliasValue("contact") + "), TYPE(partner), TYPE(KEY(contact)) FROM Document document JOIN document.contacts contact JOIN document.partners partner JOIN document.people myPerson", criteriaBuilder.getQueryString());
    }

    @Test
    public void rootEntitySelect() {
        BlazeCriteriaQuery<Document> cq = BlazeCriteria.get(cbf, Document.class);
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(root);

        CriteriaBuilder<Document> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void tupleSelectAccess() {
        Long docId = transactional(new TxWork<Long>() {
            @Override
            public Long work(EntityManager em) {
                Person p = new Person("abc");
                Document d = new Document("abc", p);
                em.persist(p);
                em.persist(d);
                em.flush(); // required for datanucleus
                return d.getId();
            }
        });

        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
        BlazeRoot<Document> root = cq.from(Document.class, "document");

        cq.multiselect(
                root.get(Document_.id),
                root.get(Document_.id).alias("docId")
        );

        CriteriaBuilder<Tuple> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id, document.id AS docId FROM Document document", criteriaBuilder.getQueryString());
        List<Tuple> list = criteriaBuilder.getResultList();

        assertEquals(1, list.size());
        Tuple t = list.get(0);

        assertEquals(2, t.getElements().size());
        CatchException.verifyException(t).get((String) null);
        assertEquals(docId, t.get("docId"));
        CatchException.verifyException(t).get(-1);
        assertEquals(docId, t.get(0));
        assertEquals(docId, t.get(1));
        CatchException.verifyException(t).get(2);
        CatchException.verifyException(t).get((TupleElement<?>) null);
        assertEquals(docId, t.get(t.getElements().get(0)));
        assertEquals(docId, t.get(t.getElements().get(1)));
        assertNull(t.getElements().get(0).getAlias());
        assertEquals("docId", t.getElements().get(1).getAlias());
        assertEquals(docId, t.get(t.getElements().get(1).getAlias()));
        assertEquals(Long.class, t.getElements().get(0).getJavaType());
        assertEquals(Long.class, t.getElements().get(1).getJavaType());
    }

    @Test
    public void tupleSelectDuplicateAlias() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
        BlazeRoot<Document> root = cq.from(Document.class, "document");

        cq.multiselect(
                root.get(Document_.id).alias("docId"),
                root.get(Document_.id).alias("docId")
        );

        CatchException.verifyException(cq).createCriteriaBuilder(em);
    }

    @Test
    public void searchedCaseWhen() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        BlazeRoot<Document> root = cq.from(Document.class, "document");

        cq.multiselect(
                cb.selectCase(root.get(Document_.idx)).when(1, 1).otherwise(2),
                cb.selectCase(root.get(Document_.idx)).when(1, "a").otherwise("b")
        );

        CriteriaBuilder<Tuple> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT CASE document.idx WHEN 1 THEN 1 ELSE 2 END, CASE document.idx WHEN 1 THEN 'a' ELSE 'b' END FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void caseWhenLiterals() throws Exception {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.multiselect(
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal(1))
                        .otherwise(cb.literal(2)),
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal(true))
                        .otherwise(cb.literal(false)),
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal(1L))
                        .otherwise(cb.literal(2L)),
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal(1F))
                        .otherwise(cb.literal(2F)),
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal(1D))
                        .otherwise(cb.literal(2D)),
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal(new BigDecimal("1.1")))
                        .otherwise(cb.literal(new BigDecimal("2.1"))),
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal(BigInteger.valueOf(1)))
                        .otherwise(cb.literal(BigInteger.valueOf(2))),
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal(new SimpleDateFormat("dd.MM.yyyy").parse("01.01.2016")))
                        .otherwise(cb.literal(new SimpleDateFormat("dd.MM.yyyy").parse("10.10.2016"))),
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal(calendarOf(2016, 0, 1)))
                        .otherwise(cb.literal(calendarOf(2016, 9, 10))),
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal(new java.sql.Date(116, 0, 1)))
                        .otherwise(cb.literal(new java.sql.Date(116, 9, 10))),
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal(new java.sql.Time(1, 1, 1)))
                        .otherwise(cb.literal(new java.sql.Time(10, 10, 10))),
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal(new java.sql.Timestamp(116, 0, 1, 1, 1, 1, 1_000_000)))
                        .otherwise(cb.literal(new java.sql.Timestamp(116, 9, 10, 10, 10, 10, 10_000_000))),
                cb.selectCase()
                        .when(cb.lt(root.get(Document_.age), 12), cb.literal("1"))
                        .otherwise(cb.literal("2"))
        );

        CriteriaBuilder<Tuple> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT "
                + caseWhenAge("1", "2") + ", "
                + caseWhenAge("true", "false") + ", "
                + caseWhenAge("1L", "2L") + ", "
                + caseWhenAge("1.0F", "2.0F") + ", "
                + caseWhenAge("1.0D", "2.0D") + ", "
                + caseWhenAge("1.1BD", "2.1BD") + ", "
                + caseWhenAge("1BI", "2BI") + ", "
                + caseWhenAge("{ts '2016-01-01 00:00:00'}", "{ts '2016-10-10 00:00:00'}") + ", "
                + caseWhenAge("{ts '2016-01-01 00:00:00'}", "{ts '2016-10-10 00:00:00'}") + ", "
                + caseWhenAge("{d '2016-01-01'}", "{d '2016-10-10'}") + ", "
                + caseWhenAge("{t '01:01:01'}", "{t '10:10:10'}") + ", "
                + caseWhenAge("{ts '2016-01-01 01:01:01.001000000'}", "{ts '2016-10-10 10:10:10.010000000'}") + ", "
                + caseWhenAge("'1'", "'2'")
                + " FROM Document document", criteriaBuilder.getQueryString());
    }

    private static String caseWhenAge(String result, String otherwise) {
        return "CASE WHEN document.age < 12L THEN " + result + " ELSE " + otherwise + " END";
    }

    private static Calendar calendarOf(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(0);
        c.set(year, month, day, 0, 0, 0);
        return c;
    }

    @Test
    public void nullLiteral() {
        BlazeCriteriaQuery<Integer> cq = BlazeCriteria.get(cbf, Integer.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(cb.nullLiteral(Integer.class));

        CriteriaBuilder<Integer> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT " + STATIC_JPA_PROVIDER.getNullExpression()+ " FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void singularAttributeSelect() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(root.get(Document_.id));

        CriteriaBuilder<Long> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void singularAttributePlusLiteral() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(cb.sum(root.get(Document_.id), 1L));

        CriteriaBuilder<Long> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id + 1L FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void singularAttributeMinusModuloCasts() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(cb.diff(root.get(Document_.id), cb.toLong(cb.mod(cb.toInteger(root.get(Document_.age)), 1))));

        CriteriaBuilder<Long> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id - MOD(document.age,1) FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void singularAttributeProductAbsAttribute() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(cb.prod(root.get(Document_.id), cb.abs(cb.neg(root.get(Document_.age)))));

        CriteriaBuilder<Long> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id * ABS(-document.age) FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void singularAttributeQuotientAttributeSqrt() {
        BlazeCriteriaQuery<Number> cq = BlazeCriteria.get(cbf, Number.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(cb.quot(root.get(Document_.id), cb.sqrt(root.get(Document_.age))));

        CriteriaBuilder<Number> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id / SQRT(document.age) FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void singularRelationSelect() {
        BlazeCriteriaQuery<Person> cq = BlazeCriteria.get(cbf, Person.class);
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(root.get(Document_.owner));

        CriteriaBuilder<Person> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT owner_1 FROM Document document JOIN document.owner owner_1", criteriaBuilder.getQueryString());
    }

    @Test
    public void singularAssociationIdAttributeSelect() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(root.get(Document_.owner).get(Person_.id));

        CriteriaBuilder<Long> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT " + singleValuedAssociationIdPath("document.owner.id", "owner_1") + " FROM Document document" + singleValuedAssociationIdJoin("document.owner", "owner_1", false), criteriaBuilder.getQueryString());
    }

    @Test
    public void setAssociationSelect() {
        @SuppressWarnings("rawtypes")
        BlazeCriteriaQuery<Set> cq = BlazeCriteria.get(cbf, Set.class);
        BlazeRoot<Document> root = cq.from(Document.class, "document");

        cq.select(root.get(Document_.partners));

        CriteriaBuilder<Set> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT partners_1 FROM Document document LEFT JOIN document.partners partners_1", criteriaBuilder.getQueryString());
    }

    @Test
    public void mapAssociationSelect() {
        @SuppressWarnings("rawtypes")
        BlazeCriteriaQuery<Map> cq = BlazeCriteria.get(cbf, Map.class);
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(root.get(Document_.contacts));

        CriteriaBuilder<Map> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT " + joinAliasValue("contacts_1") + " FROM Document document LEFT JOIN document.contacts contacts_1", criteriaBuilder.getQueryString());
    }

    @Test
    public void constructSelect() {
        BlazeCriteriaQuery<DocumentResult> cq = BlazeCriteria.get(cbf, DocumentResult.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(cb.construct(DocumentResult.class, root.get(Document_.id)));

        CriteriaBuilder<DocumentResult> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void constructTuple() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(cb.tuple(root.get(Document_.id)));

        CriteriaBuilder<Tuple> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void constructArray() {
        BlazeCriteriaQuery<Object[]> cq = BlazeCriteria.get(cbf, Object[].class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(cb.array(root.get(Document_.id)));

        CriteriaBuilder<Object[]> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void aggregateFunctions() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(cb.tuple(cb.avg(root.get(Document_.id)), cb.min(root.get(Document_.id)), cb.max(root.get(Document_.id)), cb.sum(root.get(Document_.id)), cb.count(root.get(Document_.id)), cb.countDistinct(root.get(Document_.id))));

        CriteriaBuilder<Tuple> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT AVG(document.id), MIN(document.id), MAX(document.id), SUM(document.id), COUNT(document.id), COUNT(DISTINCT document.id) FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void collectionSizes() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(cb.tuple(cb.size(root.get(Document_.partners))));

        CriteriaBuilder<Tuple> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT " + function("COUNT_TUPLE", "partners_1.id") + " FROM Document document LEFT JOIN document.partners partners_1 GROUP BY document.id", criteriaBuilder.getQueryString());
    }

    @Test
    public void concatSubstringTrimLowerUpperLength() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(cb.tuple(
                cb.concat(root.get(Document_.name), "-Test"),
                cb.lower(root.get(Document_.name)),
                cb.upper(root.get(Document_.name)),
                cb.substring(root.get(Document_.name), 1),
                cb.length(root.get(Document_.name)),
                cb.trim(root.get(Document_.name)),
                cb.locate(root.get(Document_.name), "abc")
        ));

        CriteriaBuilder<Tuple> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT CONCAT(document.name,'-Test'), LOWER(document.name), UPPER(document.name), SUBSTRING(document.name,1), LENGTH(document.name), TRIM(BOTH FROM document.name), LOCATE('abc',document.name) FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void coalesceNullif() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(cb.tuple(
                cb.coalesce(root.get(Document_.name), "Doc"),
                cb.nullif(cb.literal(1), 1)
        ));

        CriteriaBuilder<Tuple> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT COALESCE(document.name,'Doc'), NULLIF(1,1) FROM Document document", criteriaBuilder.getQueryString());
    }

    @Test
    public void selectPredicate() {
        BlazeCriteriaQuery<Tuple> cq = BlazeCriteria.get(cbf, Tuple.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");
        cq.multiselect(
                cb.greaterThan(root.get(Document_.age), 0L)
        );
        CriteriaBuilder<Tuple> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT CASE WHEN document.age > 0L THEN true ELSE false END FROM Document document", criteriaBuilder.getQueryString());
    }

    public static class DocumentResult {

        private Long id;

        public DocumentResult(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

}
