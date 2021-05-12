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

package com.blazebit.persistence.spring.data.testsuite.webmvc.repository;

import com.blazebit.persistence.spring.data.annotation.OptionalParam;
import com.blazebit.persistence.spring.data.repository.BlazeSpecification;
import com.blazebit.persistence.spring.data.repository.EntityViewRepository;
import com.blazebit.persistence.spring.data.repository.EntityViewSettingProcessor;
import com.blazebit.persistence.spring.data.repository.EntityViewSpecificationExecutor;
import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.webmvc.view.DocumentView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Transactional(readOnly = true)
@NoRepositoryBean
public interface ReadOnlyDocumentRepository<T> extends EntityViewRepository<T, Long>, EntityViewSpecificationExecutor<T, Document> {

    @EntityGraph(attributePaths = "owner")
    @Query("select d from Document d where d.name = 'D1'")
    Document findD1EntityGraph();

    @Query(name = "findD1")
    Document findD1NamedQuery();

    @Query(value = "select * from Document d where d.name = 'D1'", nativeQuery = true)
    Document findD1Native();

    @Query("select d.id from Document d where d.name = 'D1'")
    Long findD1Projection();

    @Query(name = "findIdOfD1")
    Long findD1NamedQueryProjection();

    @Query(value = "select d.id from Document d where d.name = 'D1'", nativeQuery = true)
    Long findD1NativeProjection();

    List<?> findByDescription(String description);

    List<T> findByName(String name);

    List<T> findByNameAndAgeOrDescription(String name, long age, String description);

    List<T> findByNameIn(String... name);

    List<T> findByNameIn(Set<String> names);

    Page<T> findByNameInOrderById(Pageable pageable, String... name);

    KeysetAwarePage<T> findByNameIn(Pageable pageable, String... name);

    List<T> findByNameLikeOrderByAgeAsc(String name);

    List<T> findByOwnerName(String ownerName);

    List<T> findByAgeGreaterThanEqual(long age);

    List<T> findByAgeIn(Long[] ages);

    Slice<T> findSliceByAgeGreaterThanEqual(long age, Pageable pageable);

    T findFirstByOrderByNameAsc();

    List<DocumentView> findByName(String name, @OptionalParam("optionalParameter") String optionalParameter);

    Page<DocumentView> findByNameOrderById(String name, Pageable pageable, @OptionalParam("optionalParameter") String optionalParameter);

    List<DocumentView> findAll(Specification<Document> specification, @OptionalParam("optionalParameter") String optionalParameter);

    List<DocumentView> findAll(EntityViewSettingProcessor<DocumentView> processor);

    List<DocumentView> findAll(BlazeSpecification processor);

    List<DocumentView> findAll(Sort sort);

    List<DocumentView> findAll(Sort sort, @OptionalParam("optionalParameter") String optionalParameter);

    Page<DocumentView> findAllByOrderByNameAsc(Pageable pageable, EntityViewSettingProcessor<DocumentView> processor);

    List<DocumentView> findAllByOrderByNameAsc(Sort sort, EntityViewSettingProcessor<DocumentView> processor);
}
