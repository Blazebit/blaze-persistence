/*
 * Copyright 2014 - 2016 Blazebit.
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

package com.blazebit.persistence.springdata.repository;

import com.blazebit.persistence.springdata.view.DocumentView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 * @since 1.2
 */
@Repository
@Transactional(readOnly = true)
public interface DocumentRepository extends EntityViewRepository<DocumentView, Long> {

    List<DocumentView> findByName(String name);

    List<DocumentView> findByNameAndAgeOrDescription(String name, long age, String description);

    List<DocumentView> findByNameIn(String... name);

    Page<DocumentView> findByNameInOrderById(Pageable pageable, String... name);

    List<DocumentView> findByNameLikeOrderByAgeAsc(String name);

    List<DocumentView> findByOwnerName(String ownerName);

    List<DocumentView> findByAgeGreaterThanEqual(long age);

    Slice<DocumentView> findSliceByAgeGreaterThanEqual(long age, Pageable pageable);

    DocumentView findFirstByOrderByNameAsc();
}
