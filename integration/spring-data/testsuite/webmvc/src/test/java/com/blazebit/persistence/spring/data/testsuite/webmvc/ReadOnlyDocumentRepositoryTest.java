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

package com.blazebit.persistence.spring.data.testsuite.webmvc;

import com.blazebit.persistence.integration.view.spring.EnableEntityViews;
import com.blazebit.persistence.spring.data.impl.repository.BlazePersistenceRepositoryFactoryBean;
import com.blazebit.persistence.spring.data.repository.BlazeSpecification;
import com.blazebit.persistence.spring.data.repository.EntityViewSettingProcessor;
import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import com.blazebit.persistence.spring.data.repository.KeysetPageRequest;
import com.blazebit.persistence.spring.data.testsuite.webmvc.accessor.DocumentAccessor;
import com.blazebit.persistence.spring.data.testsuite.webmvc.accessor.DocumentAccessors;
import com.blazebit.persistence.spring.data.testsuite.webmvc.config.SystemPropertyBasedActiveProfilesResolver;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Person;
import com.blazebit.persistence.spring.data.testsuite.webmvc.repository.ReadOnlyDocumentEntityRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.repository.ReadOnlyDocumentRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.repository.ReadOnlyDocumentViewRepository;
import com.blazebit.persistence.spring.data.testsuite.webmvc.tx.TransactionalWorkService;
import com.blazebit.persistence.spring.data.testsuite.webmvc.tx.TxWork;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentView;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDB2;
import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoFirebird;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoMySQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoOracle;
import com.blazebit.persistence.testsuite.base.jpa.category.NoPostgreSQL;
import com.blazebit.persistence.testsuite.base.jpa.category.NoSQLite;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import com.blazebit.persistence.view.Sorters;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@RunWith(Parameterized.class)
@ContextConfiguration(classes = ReadOnlyDocumentRepositoryTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReadOnlyDocumentRepositoryTest extends AbstractSpringTest {

    @Parameterized.Parameters
    public static Iterable<?> createParameters() {
        return Arrays.asList(ReadOnlyDocumentViewRepository.class, ReadOnlyDocumentEntityRepository.class);
    }

    private final Class<? extends ReadOnlyDocumentRepository<?>> repositoryClass;

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Autowired
    private TransactionalWorkService transactionalWorkService;

    private ReadOnlyDocumentRepository<?> readOnlyDocumentRepository;

    public ReadOnlyDocumentRepositoryTest(Class<? extends ReadOnlyDocumentRepository<?>> repositoryClass) {
        this.repositoryClass = repositoryClass;
    }

    @Before
    public void initRepository() {
        readOnlyDocumentRepository = autowireCapableBeanFactory.getBean(repositoryClass);
    }

    // NOTE: EclipseLink and DataNucleus seem to have a problem with the way Spring Data makes use of entity graphs, although it just uses the JPA APIs
    // Hibernate 4.2 doesn't support the JPA 2.1 APIs that introduced entity graphs
    @Test
    @Category({ NoHibernate42.class, NoEclipselink.class, NoDatanucleus.class })
    public void testFindD1EntityGraph() {
        // Given
        final Document d1 = createDocument("D1");
        final Document d2 = createDocument("D2");

        // When
        Document d = readOnlyDocumentRepository.findD1EntityGraph();

        // Then
        assertEquals(d1.getId(), d.getId());
    }

    @Test
    public void testFindD1NamedQuery() {
        // Given
        final Document d1 = createDocument("D1");
        final Document d2 = createDocument("D2");

        // When
        Document d = readOnlyDocumentRepository.findD1NamedQuery();

        // Then
        assertEquals(d1.getId(), d.getId());
    }

    @Test
    // We don't want to deal with the different casing requirements for table names in the various DBs so we only run
    // this test for H2.
    // DataNucleus is not able to create entities from native query result sets.
    @Category({NoDatanucleus.class, NoPostgreSQL.class, NoOracle.class, NoMySQL.class, NoSQLite.class, NoFirebird.class, NoDB2.class})
    public void testFindD1Native() {
        // Given
        final Document d1 = createDocument("D1");
        final Document d2 = createDocument("D2");

        // When
        Document d = readOnlyDocumentRepository.findD1Native();

        // Then
        assertEquals(d1.getId(), d.getId());
    }

    @Test
    public void testFindD1Projection() {
        // Given
        final Document d1 = createDocument("D1");
        final Document d2 = createDocument("D2");

        // When
        Long id = readOnlyDocumentRepository.findD1Projection();

        // Then
        assertEquals(d1.getId(), id);
    }

    @Test
    public void testFindD1NamedQueryProjection() {
        // Given
        final Document d1 = createDocument("D1");
        final Document d2 = createDocument("D2");

        // When
        Long id = readOnlyDocumentRepository.findD1NamedQueryProjection();

        // Then
        assertEquals(d1.getId(), id);
    }

    @Test
    // We don't want to deal with the different casing requirements for table names in the various DBs so we only run
    // this test for H2.
    @Category({NoPostgreSQL.class, NoOracle.class, NoMySQL.class, NoSQLite.class, NoFirebird.class, NoDB2.class})
    public void testFindD1NativeProjection() {
        // Given
        final Document d1 = createDocument("D1");
        final Document d2 = createDocument("D2");

        // When
        Long id = readOnlyDocumentRepository.findD1NativeProjection();

        // Then
        assertEquals(d1.getId(), id);
    }

    @Test
    public void testFindOne() {
        // Given
        final Document d1 = createDocument("D1");
        final Document d2 = createDocument("D2");

        // When
        DocumentAccessor result1 = DocumentAccessors.of(readOnlyDocumentRepository.findOne(d1.getId()));
        DocumentAccessor result2 = DocumentAccessors.of(readOnlyDocumentRepository.findOne(d2.getId()));

        // Then
        assertEquals(d1.getId(), result1.getId());
        assertNotNull(result1);

        assertEquals(d2.getId(), result2.getId());
        assertNotNull(result2);
    }

    @Test
    public void testExists(){
        // Given
        final Document d1 = createDocument("D1");

        // When
        boolean existsP1 = readOnlyDocumentRepository.exists(d1.getId());

        // Then
        assertTrue(existsP1);
    }

    @Test
    public void testFindAll() {
        // Given
        final Person p1 = createPerson("P1");
        final Document d1 = createDocument("D1", p1);
        final Document d2 = createDocument("D2", p1);

        // When
        Iterable<DocumentAccessor> actual = DocumentAccessors.of(readOnlyDocumentRepository.findAll());
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(2, actualIds.size());
        assertTrue(actualIds.contains(d1.getId()));
        assertTrue(actualIds.contains(d2.getId()));
    }

    @Test
    public void testFindAllByIds() {
        // ignored with EclipseLink due to IN collection rendering bug
        Assume.assumeFalse(isEntityRepository() && isEclipseLink());
        // Given
        final Document d1 = createDocument("D1");
        final Document d2 = createDocument("D2");

        // When
        Iterable<DocumentAccessor> actual = DocumentAccessors.of(readOnlyDocumentRepository.findAll(Arrays.asList(d1.getId(), d2.getId())));
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(2, actualIds.size());
        assertTrue(actualIds.contains(d1.getId()));
        assertTrue(actualIds.contains(d2.getId()));
    }

    @Test
    public void testCount() {
        // Given
        final Document d1 = createDocument("D1");
        final Document d2 = createDocument("D2");

        // When
        long count = readOnlyDocumentRepository.count();

        // Then
        assertEquals(2, count);
    }

    @Test
    public void testFindByName() {
        // Given
        final Document d1 = createDocument("D1");

        // When
        List<DocumentAccessor> result = DocumentAccessors.of(readOnlyDocumentRepository.findByName(d1.getName()));

        // Then
        assertEquals(1, result.size());
        assertEquals(d1.getId(), result.get(0).getId());
    }

    @Test
    public void testFindByDescription() {
        // Given
        final Document d1 = createDocument("D1", "test", 0, null);

        // When
        List<?> elements = readOnlyDocumentRepository.findByDescription(d1.getDescription());

        // Then
        assertEquals(1, elements.size());
        if (repositoryClass == ReadOnlyDocumentViewRepository.class) {
            assertTrue(elements.get(0) instanceof Document);
        } else {
            assertTrue(elements.get(0) instanceof DocumentView);
        }
        List<DocumentAccessor> result = DocumentAccessors.of(elements);
        assertEquals(d1.getId(), result.get(0).getId());
    }

    @Test
    public void testFindByNameAndAgeOrDescription() {
        // Given
        final String name = "D1";
        final Document d1 = createDocument(name, "desc1", 12, null);
        final Document d2 = createDocument(name, "desc2", 13, null);
        final Document d3 = createDocument(name, "desc3", 14, null);

        // When
        List<DocumentAccessor> actual = DocumentAccessors.of(readOnlyDocumentRepository.findByNameAndAgeOrDescription(name, 12, "desc2"));
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(2, actual.size());
        assertTrue(actualIds.contains(d1.getId()));
        assertTrue(actualIds.contains(d2.getId()));
    }

    @Test
    public void testFindByNameIn() {
        // ignored with EclipseLink due to IN collection rendering bug
        Assume.assumeFalse(isEntityRepository() && isEclipseLink());
        // Given
        final Document d1 = createDocument("d1");
        final Document d2 = createDocument("d2");
        final Document d3 = createDocument("d3");

        // When
        List<DocumentAccessor> actual = DocumentAccessors.of(readOnlyDocumentRepository.findByNameIn(d2.getName(), d3.getName()));
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(2, actual.size());
        assertTrue(actualIds.contains(d2.getId()));
        assertTrue(actualIds.contains(d3.getId()));
    }

    @Test
    public void testFindByNameInPaginated() {
        // ignored with EclipseLink due to IN collection rendering bug
        Assume.assumeFalse(isEntityRepository() && isEclipseLink());
        // Given
        final Document d1 = createDocument("d1");
        final Document d2 = createDocument("d2");
        final Document d3 = createDocument("d3");

        // When
        Page<DocumentAccessor> actual = DocumentAccessors.of(readOnlyDocumentRepository.findByNameInOrderById(new PageRequest(0, 1), d2.getName(), d3.getName()));
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(2, actual.getTotalPages());
        assertEquals(0, actual.getNumber());
        assertEquals(1, actual.getNumberOfElements());
        assertEquals(1, actual.getSize());
        assertTrue(actualIds.contains(d2.getId()));

        actual = DocumentAccessors.of(readOnlyDocumentRepository.findByNameInOrderById(actual.nextPageable(), d2.getName(), d3.getName()));
        actualIds = getIdsFromViews(actual);
        assertEquals(2, actual.getTotalPages());
        assertEquals(1, actual.getNumber());
        assertEquals(1, actual.getNumberOfElements());
        assertEquals(1, actual.getSize());
        assertTrue(actualIds.contains(d3.getId()));
    }

    @Test
    public void testFindByNameInKeysetPaginated() {
        // ignored with EclipseLink due to IN collection rendering bug
        Assume.assumeFalse(isEntityRepository() && isEclipseLink());
        // Given
        final Document d1 = createDocument("d1");
        final Document d2 = createDocument("d2");
        final Document d3 = createDocument("d3");

        // When
        KeysetAwarePage<DocumentAccessor> actual = DocumentAccessors.of(readOnlyDocumentRepository.findByNameIn(new KeysetPageRequest(null, Sort.asc("id"), 0, 1), d2.getName(), d3.getName()));
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(2, actual.getTotalPages());
        assertEquals(0, actual.getNumber());
        assertEquals(1, actual.getNumberOfElements());
        assertEquals(1, actual.getSize());
        assertTrue(actualIds.contains(d2.getId()));

        actual = DocumentAccessors.of(readOnlyDocumentRepository.findByNameIn(actual.nextPageable(), d2.getName(), d3.getName()));
        actualIds = getIdsFromViews(actual);
        assertEquals(2, actual.getTotalPages());
        assertEquals(1, actual.getNumber());
        assertEquals(1, actual.getNumberOfElements());
        assertEquals(1, actual.getSize());
        assertTrue(actualIds.contains(d3.getId()));
    }

    @Test
    public void testFindByNameLikeOrderByAgeAsc() {
        // Given
        final Document d1 = createDocument("d1", null, 2l, null);
        final Document d2 = createDocument("d2", null, 1l, null);

        // When
        List<DocumentAccessor> actual = DocumentAccessors.of(readOnlyDocumentRepository.findByNameLikeOrderByAgeAsc("d%"));

        // Then
        assertEquals(2, actual.size());
        assertEquals(d2.getId(), actual.get(0).getId());
        assertEquals(d1.getId(), actual.get(1).getId());
    }

    @Test
    public void testFindByOwnerName() {
        // Given
        final Person p1 = createPerson("p1");
        final Person p2 = createPerson("p2");

        final Document d1 = createDocument("d1", p1);
        final Document d2 = createDocument("d2", p2);
        final Document d3 = createDocument("d3", p2);

        // When
        List<DocumentAccessor> actual = DocumentAccessors.of(readOnlyDocumentRepository.findByOwnerName(p2.getName()));
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(2, actual.size());
        assertTrue(actualIds.contains(d2.getId()));
        assertTrue(actualIds.contains(d3.getId()));
    }

    @Test
    public void testFindByAgeGreaterThanEqual() {
        // Given
        final Document d1 = createDocument("d1", null, 3L, null);
        final Document d2 = createDocument("d2", null, 4L, null);
        final Document d3 = createDocument("d3", null, 5L, null);

        // When
        List<DocumentAccessor> actual = DocumentAccessors.of(readOnlyDocumentRepository.findByAgeGreaterThanEqual(4L));
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(2, actual.size());
        assertTrue(actualIds.contains(d2.getId()));
        assertTrue(actualIds.contains(d3.getId()));
    }

    @Test
    public void testFindSliceByAgeGreaterThanEqual() {
        // Given
        final Document d1 = createDocument("d1", null, 3L, null);
        final Document d2 = createDocument("d2", null, 4L, null);
        final Document d3 = createDocument("d3", null, 5L, null);

        // When
        Slice<DocumentAccessor> actual = DocumentAccessors.of(readOnlyDocumentRepository.findSliceByAgeGreaterThanEqual(4L, new PageRequest(1, 1, "age", "id")));
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(1, actual.getSize());
        assertFalse(actual.hasNext());
        assertTrue(actual.hasPrevious());
        assertTrue(actualIds.contains(d3.getId()));
    }

    @Test
    public void testFindFirstByOrderByNameAsc() {
        // Given
        final Document d3 = createDocument("d3");
        final Document d2 = createDocument("d2");
        final Document d1 = createDocument("d1");

        // When
        DocumentAccessor actual = DocumentAccessors.of(readOnlyDocumentRepository.findFirstByOrderByNameAsc());

        // Then
        assertEquals(d1.getId(), actual.getId());
    }

    @Test
    public void testFindByAgeIn() {
        // ignored with EclipseLink due to IN collection rendering bug
        Assume.assumeFalse(isEclipseLink());
        // Given
        final Document d3 = createDocument("d3");
        final Document d2 = createDocument("d2");
        final Document d1 = createDocument("d1");

        // When

        List<DocumentAccessor> actual1 = DocumentAccessors.of(readOnlyDocumentRepository.findByNameIn(new HashSet<String>(0)));
        List<DocumentAccessor> actual2 = DocumentAccessors.of(readOnlyDocumentRepository.findByAgeIn(new Long[0]));

        // Then
        assertEquals(0, actual1.size());
        assertEquals(0, actual2.size());
    }

    @Test
    public void testFindOneBySpec() {
        // Given
        final Document d3 = createDocument("d3", null, 3L, null);
        final Document d2 = createDocument("d2", null, 2L, null);
        final Document d1 = createDocument("d1", null, 1L, null);

        // When
        DocumentAccessor actual = DocumentAccessors.of(((ReadOnlyDocumentRepository) readOnlyDocumentRepository).findOne(new Specification<Document>() {
            @Override
            public Predicate toPredicate(Root<Document> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.equal(root.<String>get("name"), "d2");
            }
        }));

        // Then
        assertTrue(actual.getId().equals(d2.getId()));
    }

    @Test
    public void testFindAllBySpec() {
        // Given
        final Document d3 = createDocument("d3", null, 3L, null);
        final Document d2 = createDocument("d2", null, 2L, null);
        final Document d1 = createDocument("d1", null, 1L, null);

        // When
        List<DocumentAccessor> actual = DocumentAccessors.of(((ReadOnlyDocumentRepository) readOnlyDocumentRepository).findAll(new Specification<Document>() {
            @Override
            public Predicate toPredicate(Root<Document> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.ge(root.<Long>get("age"), 2L);
            }
        }));
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(2, actual.size());
        assertTrue(actualIds.contains(d2.getId()));
        assertTrue(actualIds.contains(d3.getId()));
    }

    @Test
    public void testFindAllBySpecPageable() {
        // Given
        final Document d4 = createDocument("d4", null, 2L, null);
        final Document d3 = createDocument("d3", null, 3L, null);
        final Document d2 = createDocument("d2", null, 2L, null);
        final Document d1 = createDocument("d1", null, 1L, null);

        // When
        Page<DocumentAccessor> actual = DocumentAccessors.of(((ReadOnlyDocumentRepository) readOnlyDocumentRepository).findAll(new Specification<Document>() {
            @Override
            public Predicate toPredicate(Root<Document> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.gt(root.<Long>get("age"), 1L);
            }
        }, new PageRequest(1, 2, "name", "id")));
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(1, actual.getNumberOfElements());
        assertTrue(actualIds.contains(d4.getId()));
    }

    @Test
    public void testFindAllBySpecSorted() {
        // Given
        final Document d3 = createDocument("d3", null, 3L, null);
        final Document d2 = createDocument("d2", null, 2L, null);
        final Document d1 = createDocument("d1", null, 1L, null);

        // When
        List<DocumentAccessor> actual = DocumentAccessors.of(((ReadOnlyDocumentRepository) readOnlyDocumentRepository).findAll(new Specification<Document>() {
            @Override
            public Predicate toPredicate(Root<Document> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.ge(root.<Long>get("age"), 2L);
            }
        }, Sort.asc("name")));

        // Then
        assertEquals(2, actual.size());
        assertTrue(actual.get(0).getId().equals(d2.getId()));
        assertTrue(actual.get(1).getId().equals(d3.getId()));
    }

    @Test
    public void testFindWithOptionalParameter() {
        // Given
        String name = "D1";
        String param = "Foo";
        createDocument(name);

        // When
        List<DocumentView> result = readOnlyDocumentRepository.findByName(name, param);

        // Then
        assertEquals(1, result.size());
        String optionalParameter = result.get(0).getOptionalParameter();
        assertEquals(param, optionalParameter);
    }

    @Test
    public void testFindWithOptionalParameterAndPageable() {
        // Given
        String name = "D1";
        String param = "Foo";
        createDocument("D1");

        // When
        Page<DocumentView> result = readOnlyDocumentRepository.findByNameOrderById(name, new PageRequest(0, 1), param);

        // Then
        assertEquals(1, result.getSize());
        String optionalParameter = result.getContent().get(0).getOptionalParameter();
        assertEquals(param, optionalParameter);
    }

    @Test
    public void testFindAllBySpecWithOptionalParameter() {
        // Given
        final Document d3 = createDocument("d3", null, 3L, null);
        final Document d2 = createDocument("d2", null, 2L, null);
        final Document d1 = createDocument("d1", null, 1L, null);

        final String param = "Foo";

        // When
        List<DocumentView> actual = readOnlyDocumentRepository.findAll(new Specification<Document>() {

            @Override
            public Predicate toPredicate(Root<Document> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.ge(root.<Long>get("age"), 2L);
            }
        }, param);

        // Then
        assertEquals(2, actual.size());
        assertEquals(actual.get(0).getOptionalParameter(), param);
    }

    @Test
    public void testEntityViewSettingProcessorParameter() {
        // Given
        String name = "D1";
        String param = "Foo";
        createDocument(name);

        // When
        List<DocumentView> actual = readOnlyDocumentRepository.findAll(new EntityViewSettingProcessor<DocumentView>() {

            @Override
            public EntityViewSetting<DocumentView, ?> acceptEntityViewSetting(EntityViewSetting<DocumentView, ?> setting) {
                setting.addOptionalParameter("optionalParameter", param);
                return setting;
            }
        });

        // Then
        assertEquals(1, actual.size());
        assertEquals(actual.get(0).getOptionalParameter(), param);
    }

    @Test
    public void testBlazeSpecificationParameter() {
        // Given
        String name = "D1";
        createDocument(name);

        // When
        List<DocumentView> actual = readOnlyDocumentRepository.findAll(new BlazeSpecification() {
            @Override
            public void applySpecification(String rootAlias, com.blazebit.persistence.CriteriaBuilder<?> builder) {
                builder.where("name").eqExpression("'D2'");
            }
        });

        // Then
        assertEquals(0, actual.size());
    }

    @Test
    public void testFindAllBySpecWithKeysetExtraction() {
        // Given
        final Document d3 = createDocument("d3", null, 3L, null);
        final Document d2 = createDocument("d2", null, 2L, null);
        final Document d1 = createDocument("d1", null, 1L, null);

        final String param = "Foo";

        Pageable pageable = new KeysetPageRequest(null, Sort.asc("id"), 0, 3, true, true);

        // When
        Page<?> actual = readOnlyDocumentRepository.findAll(new Specification<Document>() {

            @Override
            public Predicate toPredicate(Root<Document> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return criteriaBuilder.conjunction();
            }
        }, pageable);
        KeysetAwarePage<?> keysetAwarePage = (KeysetAwarePage<?>) actual;

        // Then
        assertEquals(3, keysetAwarePage.getKeysetPage().getKeysets().size());
    }

    @Test
    public void testEntityViewAttributeSorting() {
        // Given
        String doc1 = "D1";
        String doc2 = "D2";
        String doc3 = "D3";
        Person person = createPerson("Foo");
        createDocument(doc1, person);
        createDocument(doc2, person);
        createDocument(doc3, createPerson("Bar"));

        String sortProperty = "ownerDocumentCount";

        List<DocumentView> list = readOnlyDocumentRepository.findAll(Sort.asc(sortProperty));

        assertEquals(doc3, list.get(0).getName());

        list = readOnlyDocumentRepository.findAll(Sort.desc(sortProperty));

        assertEquals(doc3, list.get(2).getName());
    }

    @Test
    public void testMixedEntityViewAndEntityAttributeSortingPartTree() {
        // Given
        String doc1 = "D1";
        String doc2 = "D2";
        String doc3 = "D2";
        Person person = createPerson("Foo");
        createDocument(doc1, "A", 0L, person);
        createDocument(doc2, "B", 0L, person);
        createDocument(doc3, createPerson("Bar"));

        String entityViewSortProperty = "ownerDocumentCount";
        String entitySortProperty = "description";

        List<DocumentView> list = readOnlyDocumentRepository.findAll(
                Sort.of(
                    new org.springframework.data.domain.Sort.Order(org.springframework.data.domain.Sort.Direction.ASC, entityViewSortProperty),
                    new org.springframework.data.domain.Sort.Order(org.springframework.data.domain.Sort.Direction.DESC, entitySortProperty)
                ),
                "foo");

        assertEquals(doc3, list.get(0).getName());
        assertEquals(doc2, list.get(1).getName());
        assertEquals(doc1, list.get(2).getName());

        list = readOnlyDocumentRepository.findAll(
                Sort.of(
                    new org.springframework.data.domain.Sort.Order(org.springframework.data.domain.Sort.Direction.ASC, entitySortProperty),
                    new org.springframework.data.domain.Sort.Order(org.springframework.data.domain.Sort.Direction.ASC, entityViewSortProperty)
                ),
                "foo");

        assertEquals(doc1, list.get(0).getName());
        assertEquals(doc3, list.get(1).getName());
        assertEquals(doc2, list.get(2).getName());
    }

    @Test
    public void testPageableSortOrder() {
        // Given
        String doc1 = "D1";
        String doc2 = "D2";
        String doc3 = "D2";
        String doc4 = "D2";
        Person person = createPerson("Foo");
        Document d1 = createDocument(doc1, "A", 0L, person);
        Document d2 = createDocument(doc2, "B", 0L, person);
        Document d3 = createDocument(doc3, "B", 0L, person);
        Document d4 = createDocument(doc4, createPerson("Bar"));

        String entityViewSortProperty = "ownerDocumentCount";
        String entitySortProperty = "description";

        // When
        Page<DocumentAccessor> actual = DocumentAccessors.of(((ReadOnlyDocumentRepository) readOnlyDocumentRepository).findAllByOrderByNameAsc(
                new PageRequest(0, 3, entityViewSortProperty, entitySortProperty), evs -> {
                    evs.addAttributeSorter("id", Sorters.ascending());
                    return evs;
                }
        ));
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(3, actual.getNumberOfElements());
        assertEquals(actualIds.get(0), d1.getId());
        assertEquals(actualIds.get(1), d4.getId());
        assertEquals(actualIds.get(2), d2.getId() < d3.getId() ? d2.getId() : d3.getId());
    }

     @Test
    public void testNonPageableSortOrder() {
        // Given
        String doc1 = "D1";
        String doc2 = "D2";
        String doc3 = "D2";
        String doc4 = "D2";
        Person person = createPerson("Foo");
        Document d1 = createDocument(doc1, "A", 0L, person);
        Document d2 = createDocument(doc2, "B", 0L, person);
        Document d3 = createDocument(doc3, "B", 0L, person);
        Document d4 = createDocument(doc4, createPerson("Bar"));

        String entityViewSortProperty = "ownerDocumentCount";
        String entitySortProperty = "description";

        // When
        List<DocumentAccessor> actual = DocumentAccessors.of(((ReadOnlyDocumentRepository) readOnlyDocumentRepository).findAllByOrderByNameAsc(
                Sort.asc(entityViewSortProperty, entitySortProperty),evs -> {
                    evs.addAttributeSorter("id", Sorters.ascending());
                    return evs;
                }
        ));
        List<Long> actualIds = getIdsFromViews(actual);

        // Then
        assertEquals(4, actual.size());
        assertEquals(actualIds.get(0), d1.getId());
        assertEquals(actualIds.get(1), d4.getId());
        assertEquals(actualIds.get(2), d2.getId() < d3.getId() ? d2.getId() : d3.getId());
        assertEquals(actualIds.get(3), d2.getId() < d3.getId() ? d3.getId() : d2.getId());
    }

    private List<Long> getIdsFromViews(Iterable<DocumentAccessor> views) {
        List<Long> ids = new ArrayList<>();
        for (DocumentAccessor view : views) {
            ids.add(view.getId());
        }
        return ids;
    }

    private Document createDocument(String name) {
        return createDocument(name, null);
    }

    private Document createDocument(final String name, final Person owner) {
        return createDocument(name, null, 0L, owner);
    }

    private Document createDocument(final String name, final String description, final long age, final Person owner) {
        return transactionalWorkService.doTxWork(new TxWork<Document>() {
            @Override
            public Document work(EntityManager em, EntityViewManager evm) {
                Document d = new Document(name);
                d.setDescription(description);
                d.setAge(age);
                em.persist(d);
                if (owner != null) {
                    d.setOwner(em.getReference(Person.class, owner.getId()));
                }
                return d;
            }
        });
    }

    private Person createPerson(final String name) {
        return transactionalWorkService.doTxWork(new TxWork<Person>() {
            @Override
            public Person work(EntityManager em, EntityViewManager evm) {
                Person p = new Person(name);
                em.persist(p);
                return p;
            }
        });
    }

    private boolean isEntityRepository() {
        return repositoryClass == ReadOnlyDocumentEntityRepository.class;
    }

    private boolean isEclipseLink() {
        return Arrays.asList(new SystemPropertyBasedActiveProfilesResolver().resolve(ReadOnlyDocumentRepositoryTest.class)).contains("eclipselink");
    }

    @Configuration
    @ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*TestConfig"))
    @ImportResource("classpath:/com/blazebit/persistence/spring/data/testsuite/webmvc/application-config.xml")
    @EnableEntityViews(basePackages = "com.blazebit.persistence.spring.data.testsuite.webmvc.view")
    @EnableJpaRepositories(
            basePackages = "com.blazebit.persistence.spring.data.testsuite.webmvc.repository",
            entityManagerFactoryRef = "myEmf",
            repositoryFactoryBeanClass = BlazePersistenceRepositoryFactoryBean.class
    )
    static class TestConfig {
    }
}
