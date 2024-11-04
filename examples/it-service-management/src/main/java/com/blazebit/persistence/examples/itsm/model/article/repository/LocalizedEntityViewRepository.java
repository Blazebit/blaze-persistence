/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.repository;

import java.util.Optional;

import com.blazebit.persistence.examples.itsm.model.OffsetBasedEntityViewSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedEntity;
import com.blazebit.persistence.examples.itsm.model.article.view.LocalizedEntityView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@NoRepositoryBean
public interface LocalizedEntityViewRepository<T extends LocalizedEntityView<E>, E extends LocalizedEntity>
        extends JpaRepository<T, Long>,
        OffsetBasedEntityViewSpecificationExecutor<T, E> {

    Optional<T> findByName(String name);

}
