/*
 * Copyright 2014 - 2023 Blazebit.
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

import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.IdClassEntity;
import com.blazebit.persistence.spring.data.testsuite.webmvc.entity.IdClassEntityId;
import org.springframework.data.repository.Repository;

/**
 * @author Christian Beikov
 * @since 1.6.8
 */
public interface IdClassEntityRepository extends Repository<IdClassEntity, IdClassEntityId> {
}
