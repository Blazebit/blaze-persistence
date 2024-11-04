/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.view;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedEntity;
import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedEntity_;
import org.springframework.data.jpa.domain.Specification;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(LocalizedEntity.class)
public interface LocalizedEntityId<T extends LocalizedEntity>
        extends Specification<T> {

    @IdMapping
    Long getId();

    @Override
    default Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,
            CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.equal(root.get(LocalizedEntity_.id),
                this.getId());
    }

}
