/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.examples.itsm.model.common.repository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.blazebit.persistence.examples.itsm.model.common.entity.User;
import com.blazebit.persistence.examples.itsm.model.common.entity.User_;
import com.blazebit.persistence.examples.itsm.model.common.view.UserDetail;

import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface UserRepository
        extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    default Optional<User> findByEmailAddress(String... emailAddresses) {
        return this.findOne((root, query, builder) -> {
            Subquery<Boolean> subquery = query.subquery(Boolean.class);
            MapJoin<User, String, Boolean> join = subquery.correlate(root).join(User_.emailAddresses);
            Set<String> addresses = Stream.of(emailAddresses).map(String::toLowerCase)
                .collect(Collectors.toSet());
            Path<Boolean> confirmed = join.value();
            Path<String> address = join.key();
            Predicate p1 = builder.isTrue(confirmed);
            Predicate p2 = builder.lower(address).in(addresses);
            return builder.exists(subquery.where(p1, p2));
        });
    }

    UserDetail findByName(String name);

}
