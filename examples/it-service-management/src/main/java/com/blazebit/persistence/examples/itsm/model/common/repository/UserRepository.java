/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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

import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;

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
