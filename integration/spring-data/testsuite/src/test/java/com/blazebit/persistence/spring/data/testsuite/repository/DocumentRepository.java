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

package com.blazebit.persistence.spring.data.testsuite.repository;

import com.blazebit.persistence.spring.data.annotation.OptionalParam;
import com.blazebit.persistence.spring.data.repository.EntityViewRepository;
import com.blazebit.persistence.spring.data.repository.EntityViewSettingProcessor;
import com.blazebit.persistence.spring.data.repository.EntityViewSpecificationExecutor;
import com.blazebit.persistence.spring.data.repository.KeysetAwarePage;
import com.blazebit.persistence.spring.data.testsuite.entity.Document;
import com.blazebit.persistence.spring.data.testsuite.view.DocumentView;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
public interface DocumentRepository<T> extends EntityViewRepository<T, Long>, EntityViewSpecificationExecutor<T, Document> {

    @EntityGraph(attributePaths = "owner")
    @Query("select d from Document d where d.name = 'D1'")
    Document findD1();

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
}
