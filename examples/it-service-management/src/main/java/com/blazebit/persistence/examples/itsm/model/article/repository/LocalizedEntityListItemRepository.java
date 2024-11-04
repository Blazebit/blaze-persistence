/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.repository;

import java.io.Serializable;

import com.blazebit.persistence.examples.itsm.model.OffsetBasedEntityViewSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedEntity;
import com.blazebit.persistence.examples.itsm.model.article.view.LocalizedEntityListItem;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@NoRepositoryBean
public interface LocalizedEntityListItemRepository<V extends LocalizedEntityListItem<E>, E extends LocalizedEntity, I extends Serializable>
        extends JpaRepository<V, I>,
        OffsetBasedEntityViewSpecificationExecutor<V, E> {

}
