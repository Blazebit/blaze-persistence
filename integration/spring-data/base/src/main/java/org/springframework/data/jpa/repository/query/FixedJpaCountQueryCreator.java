/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package org.springframework.data.jpa.repository.query;

import com.blazebit.persistence.spring.data.base.query.ParameterMetadataProvider;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.PartTree;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class FixedJpaCountQueryCreator extends FixedJpaQueryCreator {

    public FixedJpaCountQueryCreator(PartTree tree, Class<?> domainClass, CriteriaBuilder builder,
                                     ParameterMetadataProvider provider) {
        super(tree, domainClass, builder, provider);
    }

    @Override
    protected CriteriaQuery<Object> complete(Predicate predicate, Sort sort, CriteriaQuery<Object> query,
                                             CriteriaBuilder builder, Root<?> root) {
        CriteriaQuery<Object> select = query.select(query.isDistinct() ? builder.countDistinct(root) : builder.count(root));
        return predicate == null ? select : select.where(predicate);
    }
}
