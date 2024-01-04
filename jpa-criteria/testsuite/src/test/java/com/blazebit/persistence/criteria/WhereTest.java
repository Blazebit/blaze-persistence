/*
 * Copyright 2014 - 2024 Blazebit.
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

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.ConfigurationProperties;
import com.blazebit.persistence.spi.CriteriaBuilderConfiguration;
import com.blazebit.persistence.testsuite.AbstractCoreTest;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Document_;
import com.blazebit.persistence.testsuite.entity.NameObject_;
import com.blazebit.persistence.testsuite.entity.Person;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class WhereTest extends AbstractCoreTest {

    private CriteriaBuilderFactory cbfUnoptimized;

    @Before
    public void initNonOptimized() {
        CriteriaBuilderConfiguration config = Criteria.getDefault();
        config.getProperties().setProperty(ConfigurationProperties.EXPRESSION_OPTIMIZATION, "false");
        cbfUnoptimized = config.createCriteriaBuilderFactory(emf);
    }

    @Test
    public void singularAttributeWithLiterals() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        Long longValue = 999999999L;
        Path<Double> doublePath = root.get(Document_.someValue);
        Path<Integer> integerPath = root.get(Document_.idx);

        cq.select(root.get(Document_.id));
        cq.where(cb.and(
                cb.equal(root.get(Document_.id), 1L),
                cb.greaterThan(root.get(Document_.creationDate), Calendar.getInstance()),
                cb.notEqual(root.get(Document_.lastModified), new Date()),
                cb.equal(cb.lower(cb.literal("ABC")), "abc"),
                cb.ge(
                        cb.quot( integerPath, doublePath ),
                        longValue
                )
        ));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document WHERE document.id = :generated_param_0 AND document.creationDate > :generated_param_1 " +
                "AND document.lastModified <> :generated_param_2 AND LOWER('ABC') = :generated_param_3 AND document.idx / document.someValue >= :generated_param_4", criteriaBuilder.getQueryString());
        assertEquals(Long.class, criteriaBuilder.getParameter("generated_param_0").getParameterType());
        assertEquals(GregorianCalendar.class, criteriaBuilder.getParameter("generated_param_1").getParameterType());
        assertEquals(Date.class, criteriaBuilder.getParameter("generated_param_2").getParameterType());
        assertEquals(String.class, criteriaBuilder.getParameter("generated_param_3").getParameterType());
    }

    @Test
    public void embeddablePaths() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(root.get(Document_.id));
        cq.where(cb.and(
                cb.equal(root.get(Document_.nameObject).get(NameObject_.primaryName), "abc"),
                cb.equal(root.get("nameObject").get("secondaryName"), "asd")
        ));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document WHERE document.nameObject.primaryName = :generated_param_0 AND document.nameObject.secondaryName = :generated_param_1" +
                "", criteriaBuilder.getQueryString());
    }

    @Test
    public void simpleCaseWhen() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(root.get(Document_.id));
        cq.where(
                cb.equal(
                    cb.selectCase(root.get(Document_.age))
                        .when(0L, 1)
                        .when(10L, 2)
                        .otherwise(0),
                    1
                )
        );

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document WHERE CASE document.age WHEN :generated_param_0 THEN :generated_param_1 WHEN :generated_param_2 THEN :generated_param_3 ELSE :generated_param_4 END = :generated_param_5" +
                "", criteriaBuilder.getQueryString());
    }

    @Test
    @SuppressWarnings({ "unchecked" })
    public void inVariations() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");
        Expression<Long> zero = cb.literal(0L);
        BlazeSubquery<Long> subquery = cq.subquery(Long.class);
        Root<Document> subRoot = subquery.from(Document.class, "sub");
        subquery.select(zero);

        verifyException(root.get(Document_.id), NullPointerException.class, r -> r.in((Collection<?>) null));
        verifyException(root.get(Document_.id), IllegalArgumentException.class, r -> r.in((Expression<?>) null));
        verifyException(root.get(Document_.id), IllegalArgumentException.class, r -> r.in((Expression<Collection<?>>) null));

        cq.select(root.get(Document_.id));
        cq.where(cb.and(
                cb.in(root.get(Document_.id)),
                root.get(Document_.id).in(),
                root.get(Document_.id).in((Object) null),
                root.get(Document_.id).in(0L),
                root.get(Document_.id).in(cb.literal(0L)),
                root.get(Document_.id).in((Expression<Collection<?>>) (Expression<?>) cb.literal(Arrays.asList(0L))),
                root.get(Document_.id).in(Arrays.asList(0L)),
                root.get(Document_.id).in(subquery),
                root.get(Document_.id).in(cb.parameter(Collection.class, "collectionParam")),
                subquery.in(Arrays.asList(0L))
        ));

        verifyException(cb.parameter(Integer.class, "p"), IllegalArgumentException.class, r -> r.in());

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document " +
                "WHERE 1 = 0 " +
                "AND 1 = 0 " +
                "AND document.id IN " + listParameter("generated_param_0") + " " +
                "AND document.id IN " + listParameter("generated_param_1") + " " +
                "AND document.id IN (0L) " +
                "AND document.id IN (0L) " +
                "AND document.id IN " + listParameter("generated_param_2") + " " +
                "AND document.id IN (SELECT 0L FROM Document sub) " +
                "AND document.id IN " + listParameter("collectionParam") + " " +
                "AND (SELECT 0L FROM Document sub) IN " + listParameter("generated_param_3") +
                "", criteriaBuilder.getQueryString());
    }

    @Test
    public void junctionsTrueFalse() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");
        Expression<Long> zero = cb.literal(0L);

        cq.select(zero);
        cq.where(cb.and(
                cb.isTrue(cb.conjunction()),
                cb.isFalse(cb.conjunction()),
                cb.isTrue(cb.disjunction()),
                cb.isFalse(cb.disjunction())
        ));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT 0L FROM Document document " +
                "WHERE 1 = 1 " +
                "AND 1 = 0 " +
                "AND 1 = 0 " +
                "AND 1 = 1", criteriaBuilder.getQueryString());
    }

    @Test
    public void parameterUsage() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");
        Expression<Long> param = cb.parameter(Long.class, "param");

        cq.select(param);
        cq.where(cb.and(
                cb.equal(root.get(Document_.id), param),
                param.isNotNull()
        ));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT :param FROM Document document " +
                "WHERE document.id = :param " +
                "AND :param IS NOT NULL", criteriaBuilder.getQueryString());
        assertEquals(1, criteriaBuilder.getParameters().size());
        assertEquals(Long.class, criteriaBuilder.getParameter("param").getParameterType());
    }

    @Test
    public void multipleNegations() {
        BlazeCriteriaQuery<Integer> cq = BlazeCriteria.get(cbfUnoptimized, Integer.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");
        Expression<Integer> one = cb.literal(1);
        Expression<Long> id = root.get(Document_.id);
        Expression<String> name = root.get(Document_.name);
        Expression<Boolean> archived = root.get(Document_.archived);
        Expression<Person> owner = root.get(Document_.owner);
        Expression<List<Person>> people = root.get(Document_.people);
        BlazeSubquery<Integer> subquery = cq.subquery(Integer.class);
        Root<Document> subRoot = subquery.from(Document.class, "sub");
        subquery.select(one);

        cq.select(one);
        cq.where(
            createVariations(
                    cb,
                    cb.equal(one, one),
                    cb.notEqual(one, one),
                    cb.greaterThan(one, one),
                    cb.greaterThanOrEqualTo(one, one),
                    cb.lessThan(one, one),
                    cb.lessThanOrEqualTo(one, one),
                    cb.isNull(id),
                    cb.isNotNull(id),
                    cb.like(name, "%"),
                    cb.notLike(name, "%"),
                    cb.between(id, 0L, 0L),
                    cb.conjunction(),
                    cb.disjunction(),
                    cb.isTrue(archived),
                    cb.isFalse(archived),
                    cb.exists(subquery),
                    cb.isEmpty(people),
                    cb.isNotEmpty(people),
                    cb.isMember(owner, people),
                    cb.isNotMember(owner, people),
                    cb.in(id).value(0L)
        ));

        String whereClause = createVariations(
                true,
                // NOTE: we use a third parameter null to mark cases as "problematic"
                // They are problematic because our parser simplifies negations into some predicates instead of simply wrapping the predicates
                new String[]{ "1 = 1", "1 <> 1", null},
                new String[]{ "1 <> 1", "1 = 1"},
                new String[]{ "1 > 1", "1 <= 1"},
                new String[]{ "1 >= 1", "1 < 1"},
                new String[]{ "1 < 1", "1 >= 1"},
                new String[]{ "1 <= 1", "1 > 1"},
                new String[]{ "document IS NULL", "document IS NOT NULL", null},
                new String[]{ "document IS NOT NULL", "document IS NULL"},
                new String[]{ "document.name LIKE :generated_param", "document.name NOT LIKE :generated_param", null},
                new String[]{ "document.name NOT LIKE :generated_param", "document.name LIKE :generated_param"},
                new String[]{ "document.id BETWEEN :generated_param AND :generated_param", "document.id NOT BETWEEN :generated_param AND :generated_param", null},
                new String[]{ "1 = 1", "1 = 0", "1 <> 1"},
                new String[]{ "1 = 0", "1 = 1", "1 <> 0"},
                new String[]{ "document.archived = true", "document.archived = false", "document.archived <> true"},
                new String[]{ "document.archived = false", "document.archived = true", "document.archived <> false"},
                new String[]{ "EXISTS (SELECT 1 FROM Document sub)", "NOT EXISTS (SELECT 1 FROM Document sub)", null},
                new String[]{ "document.people IS EMPTY", "document.people IS NOT EMPTY", null},
                new String[]{ "document.people IS NOT EMPTY", "document.people IS EMPTY"},
                new String[]{ "document.owner MEMBER OF document.people", "document.owner NOT MEMBER OF document.people", null},
                new String[]{ "document.owner NOT MEMBER OF document.people", "document.owner MEMBER OF document.people"},
                new String[]{ "document.id IN " + listParameter("generated_param"), "document.id NOT IN " + listParameter("generated_param"), null}
        );

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT 1 FROM Document document WHERE " + whereClause, criteriaBuilder.getQueryString());
    }

    private Expression<Boolean> createVariations(BlazeCriteriaBuilder cb, Predicate... elements) {
        Predicate[] variations = new Predicate[8];

        for (int i = 0; i < variations.length; i++) {
            Predicate[] predicates = new Predicate[elements.length];

            for (int j = 0; j < predicates.length; j++) {
                if (i % 2 == 0) {
                    predicates[j] = elements[j];
                } else {
                    predicates[j] = elements[j].not();
                }
            }

            if (i < 2) {
                variations[i] = cb.and(predicates);
            } else if (i < 4) {
                variations[i] = cb.and(predicates).not();
            } else if (i < 6) {
                variations[i] = cb.or(predicates);
            } else {
                variations[i] = cb.or(predicates).not();
            }
        }

        return cb.and(variations);
    }

    private String createVariations(boolean wrapping, String[]... elements) {
        final StringBuilder variations = new StringBuilder();
        final String paramPrefix = ":generated_param";
        int paramCount = 0;

        for (int i = 0; i < 8; i++) {
            if (i > 0) {
                variations.append(" AND");
            }

            String operator;
            boolean parens = false;
            if (i < 2) {
                operator = " AND ";
            } else if (i < 4) {
                if (wrapping) {
                    variations.append(" NOT (");
                    operator = " AND ";
                    parens = true;
                } else {
                    operator = " OR NOT ";
                }
            } else if (i < 6) {
                operator = " OR ";
                variations.append(" (");
                parens = true;
            } else {
                if (wrapping) {
                    variations.append(" NOT (");
                    operator = " OR ";
                    parens = true;
                } else {
                    operator = " AND NOT ";
                }
            }

            for (int j = 0; j < elements.length; j++) {
                if (j > 0) {
                    variations.append(operator);
                } else if (i > 0 && !parens) {
                    variations.append(' ');
                }

                String element;
                if (i % 2 == 0) {
                    element = elements[j][0];
                } else {
                    // Workaround
                    if (elements[j].length == 3) {
                        if (elements[j][2] != null) {
                            element = elements[j][2];
                        } else {
                            element = elements[j][1];
                        }
                    } else if (wrapping) {
                        element = elements[j][0];
                        variations.append("NOT ");
                    } else {
                        element = elements[j][1];
                    }
                }

                int start = 0;
                int index;
                while ((index = element.indexOf(paramPrefix, start)) != -1) {
                    variations.append(element, start, index);
                    variations.append(paramPrefix);
                    variations.append('_');
                    variations.append(paramCount++);
                    start = index + paramPrefix.length();
                }
                variations.append(element, start, element.length());
            }

            if (parens) {
                variations.append(")");
            }
        }

        return variations.toString();
    }

    @Test
    public void disjunctionConjunctionBetweenGeLeLikeNotNull() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");
        
        cq.select(root.get(Document_.id));
        cq.where(
            cb.or(
                cb.and(
                        cb.between(root.get(Document_.id), 1L, 10L), 
                        cb.ge(root.get(Document_.id), 1L),
                        cb.le(root.get(Document_.id), 10L)
                ),
                cb.and(
                       cb.like(root.get(Document_.name), "abc%"),
                       cb.isNotNull(root.get(Document_.name))
                )
            )
        );

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document WHERE (document.id BETWEEN :generated_param_0 AND :generated_param_1 AND document.id >= :generated_param_2 AND document.id <= :generated_param_3) OR (document.name LIKE :generated_param_4 AND document.name IS NOT NULL)", criteriaBuilder.getQueryString());
        assertEquals("abc%", criteriaBuilder.getParameterValue("generated_param_4"));
    }

    @Test
    public void inParameterNotEqualGtLtCaseWhenAllSubquery() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");
        
        BlazeSubquery<Timestamp> subquery = cq.subquery(Timestamp.class);
        Root<Document> subRoot = subquery.from(Document.class, "subDoc");
        subquery.select(subRoot.get(Document_.lastModified).as(Timestamp.class));
        
        cq.select(root.get(Document_.id));
        cq.where(
            cb.or(
                  root.get(Document_.id).in(1L, 2L),
                  cb.notEqual(root.get(Document_.id), cb.parameter(Long.class, "idParam")),
                  cb.gt(cb.function("YEAR", Integer.class, root.get(Document_.creationDate)), 2015),
                  cb.lessThan(cb.selectCase()
                        .when(cb.gt(root.get(Document_.age), 12L), root.get(Document_.creationDate))
                        .otherwise(cb.currentTimestamp())
                        .as(Timestamp.class), 
                        cb.all(subquery))
            )
        );
        
        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        String castedPath = "subDoc.lastModified";
        if (!optimizesUnnecessaryCasts()) {
            castedPath = function("CAST_TIMESTAMP", castedPath);
        }
        String queryString = "SELECT document.id FROM Document document WHERE document.id IN " + listParameter("generated_param_0") + " OR document.id <> :idParam OR " + function("YEAR", "document.creationDate") + " > :generated_param_1 OR " + function("CAST_TIMESTAMP", "CASE WHEN document.age > :generated_param_2 THEN document.creationDate ELSE CURRENT_TIMESTAMP END")
            + " < ALL(SELECT " + castedPath + " FROM Document subDoc)";
        assertEquals(queryString, criteriaBuilder.getQueryString());
        assertNotNull(criteriaBuilder.getParameter("idParam"));
        assertEquals(Long.class, criteriaBuilder.getParameter("idParam").getParameterType());
    }

    @Test
    public void existsIsEmptyIsMember() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");
        
        BlazeSubquery<Integer> subquery = cq.subquery(Integer.class);
        Root<Document> subRoot = subquery.from(Document.class, "subDoc");
        subquery.select(cb.literal(1));
        subquery.where(cb.equal(subRoot.get(Document_.id), root.get(Document_.id)));
        
        cq.select(root.get(Document_.id));
        cq.where(
            cb.and(
                  cb.exists(subquery),
                  cb.isEmpty(root.get(Document_.versions)),
                  cb.isMember(root.get(Document_.owner), root.get(Document_.partners))
            )
        );
        
        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document WHERE EXISTS (SELECT 1 FROM Document subDoc WHERE subDoc.id = document.id) AND document.versions IS EMPTY AND document.owner MEMBER OF document.partners", criteriaBuilder.getQueryString());
    }

    @Test
    public void parametersAndArrays() {
        BlazeCriteriaQuery<Document> cq = BlazeCriteria.get(cbf, Document.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        ParameterExpression<byte[]> primitiveBytes = cb.parameter(byte[].class, "primitiveBytes");
        ParameterExpression<Byte[]> wrapperBytes = cb.parameter(Byte[].class, "wrapperBytes");

        cq.where(cb.and(
                cb.equal(root.get(Document_.byteArray), primitiveBytes),
                cb.equal(root.get(Document_.wrappedByteArray), wrapperBytes)
        ));

        CriteriaBuilder<Document> criteriaBuilder = cq.createCriteriaBuilder(em);

        criteriaBuilder.setParameter("primitiveBytes", new byte[] { (byte) 0, (byte) 1});
        assertEquals(byte[].class, criteriaBuilder.getParameterValue("primitiveBytes").getClass());
        assertEquals(byte[].class, criteriaBuilder.getParameter("primitiveBytes").getParameterType());
        assertTrue(criteriaBuilder.getParameters().contains(primitiveBytes));

        criteriaBuilder.setParameter("wrapperBytes", new Byte[] { Byte.valueOf((byte) 0), Byte.valueOf((byte) 1)});
        assertEquals(Byte[].class, criteriaBuilder.getParameterValue("wrapperBytes").getClass());
        assertEquals(Byte[].class, criteriaBuilder.getParameter("wrapperBytes").getParameterType());
        assertTrue(criteriaBuilder.getParameters().contains(wrapperBytes));

        assertEquals("SELECT document FROM Document document WHERE document.byteArray = :primitiveBytes AND document.wrappedByteArray = :wrapperBytes" +
                "", criteriaBuilder.getQueryString());

        TypedQuery<Document> q = criteriaBuilder.getQuery();

        assertEquals(byte[].class, q.getParameterValue("primitiveBytes").getClass());
        assertEquals(byte[].class, q.getParameter("primitiveBytes").getParameterType());
        assertEquals(Byte[].class, q.getParameterValue("wrapperBytes").getClass());
        assertEquals(Byte[].class, q.getParameter("wrapperBytes").getParameterType());

        // TODO: To support the following we have to wrap the query and override all parameter related methods
        // NOTE: retrieving the value by parameter object would require to have the actual parameter object the jpa provider uses
//        assertTrue(Arrays.equals((byte[]) q.getParameterValue("primitiveBytes"), q.getParameterValue(primitiveBytes)));
//        assertTrue(q.getParameters().contains(primitiveBytes));
//
//        assertTrue(Arrays.equals((Byte[]) q.getParameterValue("wrapperBytes"), q.getParameterValue(wrapperBytes)));
//        assertTrue(q.getParameters().contains(wrapperBytes));
    }

    @Test
    public void likeWithEscapeCharacter() {
        BlazeCriteriaQuery<Long> cq = BlazeCriteria.get(cbf, Long.class);
        BlazeCriteriaBuilder cb = cq.getCriteriaBuilder();
        Root<Document> root = cq.from(Document.class, "document");

        cq.select(root.get(Document_.id));
        cq.where(cb.like(root.get(Document_.name), "abc\\_%", '\\'));

        CriteriaBuilder<?> criteriaBuilder = cq.createCriteriaBuilder(em);
        assertEquals("SELECT document.id FROM Document document WHERE document.name LIKE :generated_param_0 ESCAPE :generated_param_1", criteriaBuilder.getQueryString());
        assertEquals("abc\\_%", criteriaBuilder.getParameterValue("generated_param_0"));
        assertEquals('\\', criteriaBuilder.getParameterValue("generated_param_1"));
    }
}
