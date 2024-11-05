/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.testsuite;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.parser.JsonParser;
import com.blazebit.persistence.parser.XmlParser;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.testsuite.entity.Version;
import com.blazebit.persistence.testsuite.tx.TxVoidWork;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class JsonAndXmlTest extends AbstractCoreTest {

    @Override
    public void setUpOnce() {
        cleanDatabase();
        transactional(new TxVoidWork() {
            @Override
            public void work(EntityManager em) {
                Person p = new Person("Pers1");
                p.setAge(20L);
                em.persist(p);

                Version v1 = new Version();
                v1.setUrl("a");
                Document doc1 = new Document("Doc1", p, v1);
                em.persist(doc1);
                em.persist(v1);

                Version v2 = new Version();
                v2.setUrl("b");
                Document doc2 = new Document("Doc1", p, v2);
                em.persist(doc2);
                em.persist(v2);

                Version v3 = new Version();
                v3.setUrl("c");
                Document doc3 = new Document("Doc2", p, v3);
                em.persist(doc3);
                em.persist(v3);
            }
        });
    }

    // NOTE: DB2 crashes when executing this test with the GROUP_CONCAT based implementation
    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus can't handle multiple subquery select items... Number of result expressions in subquery should be 1
    @Test
    @Category({ NoEclipselink.class, NoDB2.class })
    public void testToStringJson() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "p")
                .selectSubquery("subquery", "TO_STRING_JSON(subquery, 'name', 'age')")
                    .from(Document.class, "doc")
                    .select("doc.name")
                    .select("doc.age")
                .end()
                ;

        Tuple actual = criteria.getResultList().get(0);
        List<Object[]> objects = JsonParser.parseStringOnly(actual.get(0, String.class), "name", "age");
        assertEquals(3, objects.size());
        assertRemove(objects, new Object[]{ "Doc1", "0"});
        assertRemove(objects, new Object[]{ "Doc1", "0"});
        assertRemove(objects, new Object[]{ "Doc2", "0"});
        assertEquals(0, objects.size());
    }

    // NOTE: DB2 crashes when executing this test with the GROUP_CONCAT based implementation
    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus can't handle multiple subquery select items... Number of result expressions in subquery should be 1
    @Test
    @Category({ NoEclipselink.class, NoDB2.class })
    public void testToStringXml() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "p")
                .selectSubquery("subquery", "TO_STRING_XML(subquery, 'name', 'age')")
                    .from(Document.class, "doc")
                    .select("doc.name")
                    .select("doc.age")
                .end()
                ;

        Tuple actual = criteria.getResultList().get(0);
        List<Object[]> objects = XmlParser.parse(actual.get(0, String.class), "name", "age");
        assertEquals(3, objects.size());
        assertRemove(objects, new Object[]{ "Doc1", "0"});
        assertRemove(objects, new Object[]{ "Doc1", "0"});
        assertRemove(objects, new Object[]{ "Doc2", "0"});
        assertEquals(0, objects.size());
    }

    // NOTE: DB2 crashes when executing this test with the GROUP_CONCAT based implementation
    @Test
    @Category({ NoDB2.class })
    public void testStringJsonAgg() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
            .from(Document.class, "doc")
            .select("STRING_JSON_AGG('name', doc.name, 'age', doc.age)")
            .groupBy("owner")
            ;

        Tuple actual = criteria.getResultList().get(0);
        List<Object[]> objects = JsonParser.parseStringOnly(actual.get(0, String.class), "name", "age");
        assertEquals(3, objects.size());
        assertRemove(objects, new Object[]{ "Doc1", "0"});
        assertRemove(objects, new Object[]{ "Doc1", "0"});
        assertRemove(objects, new Object[]{ "Doc2", "0"});
        assertEquals(0, objects.size());
    }

    // NOTE: DB2 crashes when executing this test with the GROUP_CONCAT based implementation
    @Test
    @Category({ NoDB2.class })
    public void testStringXmlAgg() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Document.class, "doc")
                .select("STRING_XML_AGG('name', doc.name, 'age', doc.age)")
                .groupBy("owner")
                ;

        Tuple actual = criteria.getResultList().get(0);
        List<Object[]> objects = XmlParser.parse(actual.get(0, String.class), "name", "age");
        assertEquals(3, objects.size());
        assertRemove(objects, new Object[]{ "Doc1", "0"});
        assertRemove(objects, new Object[]{ "Doc1", "0"});
        assertRemove(objects, new Object[]{ "Doc2", "0"});
        assertEquals(0, objects.size());
    }

    // NOTE: DB2 crashes when executing this test with the GROUP_CONCAT based implementation
    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus can't handle multiple subquery select items... Number of result expressions in subquery should be 1
    @Test
    @Category({ NoEclipselink.class, NoDB2.class })
    public void testToMultiset() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "p")
                .selectSubquery("subquery", "TO_MULTISET(subquery)")
                    .from(Document.class, "doc")
                    .select("doc.name")
                    .select("doc.age")
                .end()
                ;

        Tuple actual = criteria.getResultList().get(0);
        List<Object[]> objects = actual.get(0, List.class);
        assertEquals(3, objects.size());
        assertRemove(objects, new Object[]{ "Doc1", "0"});
        assertRemove(objects, new Object[]{ "Doc1", "0"});
        assertRemove(objects, new Object[]{ "Doc2", "0"});
        assertEquals(0, objects.size());
    }

    // NOTE: DB2 crashes when executing this test with the GROUP_CONCAT based implementation
    // NOTE: EclipseLink can't handle multiple subquery select items... Only one expression can be declared in a SELECT clause of a subquery
    // NOTE: DataNucleus can't handle multiple subquery select items... Number of result expressions in subquery should be 1
    @Test
    @Category({ NoEclipselink.class, NoDB2.class })
    public void testNestedToMultiset() {
        CriteriaBuilder<Tuple> criteria = cbf.create(em, Tuple.class)
                .from(Person.class, "p")
                .selectSubquery("subquery", "TO_MULTISET(subquery)")
                    .from(Document.class, "doc")
                    .select("doc.name")
                    .select("doc.age")
                    .selectSubquery("subquery", "TO_MULTISET(subquery)")
                        .from("doc.versions", "v")
                        .select("v.url")
                    .end()
                .end()
                ;

        Tuple actual = criteria.getResultList().get(0);
        List<Object[]> objects = actual.get(0, List.class);
        assertEquals(3, objects.size());
        assertRemove(objects, new Object[]{ "Doc1", "0", Arrays.asList((Object) new Object[]{ "a" })});
        assertRemove(objects, new Object[]{ "Doc1", "0", Arrays.asList((Object) new Object[]{ "b" })});
        assertRemove(objects, new Object[]{ "Doc2", "0", Arrays.asList((Object) new Object[]{ "c" })});
        assertEquals(0, objects.size());
    }

    private static void assertRemove(List<Object[]> list, Object[] array2) {
        Iterator<Object[]> iterator = list.iterator();
        OUTER: while (iterator.hasNext()) {
            Object[] array1 = iterator.next();
            for (int i = 0; i < array1.length; i++) {
                if (!Objects.equals(toString(array1[i]), toString(array2[i]))) {
                    continue OUTER;
                }
            }
            iterator.remove();
            return;
        }
        Assert.fail("Element not found: " + Arrays.toString(array2));
    }

    private static String toString(Object o) {
        if (o instanceof List<?>) {
            List<Object[]> list = (List<Object[]>) o;
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            if (!list.isEmpty()) {
                sb.append(toString(list.get(0)));
                for (int i = 1; i < list.size(); i++) {
                    sb.append(", ");
                    sb.append(toString(list.get(i)));
                }
            }
            sb.append(']');
            return sb.toString();
        } else if (o instanceof Object[]) {
            return Arrays.toString((Object[]) o);
        } else {
            return String.valueOf(o);
        }
    }
}
