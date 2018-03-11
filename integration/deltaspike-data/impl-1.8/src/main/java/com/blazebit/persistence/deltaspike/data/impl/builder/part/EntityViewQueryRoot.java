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

package com.blazebit.persistence.deltaspike.data.impl.builder.part;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.deltaspike.data.Specification;
import com.blazebit.persistence.deltaspike.data.base.builder.EntityViewQueryBuilderContext;
import com.blazebit.persistence.deltaspike.data.base.builder.part.CompoundOperatorSpecification;
import com.blazebit.persistence.deltaspike.data.base.builder.part.EntityViewOrQueryPart;
import com.blazebit.persistence.deltaspike.data.base.builder.part.EntityViewOrderByQueryPart;
import com.blazebit.persistence.deltaspike.data.base.builder.part.EntityViewQueryPart;
import com.blazebit.persistence.deltaspike.data.base.builder.part.OrderByQueryAttribute;
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewAwareRepositoryMetadata;
import org.apache.deltaspike.data.impl.builder.MethodExpressionException;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodPrefix;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static org.apache.deltaspike.data.impl.util.QueryUtils.splitByKeyword;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.part.QueryRoot} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewQueryRoot extends EntityViewQueryPart {
    private final Class<?> entityClass;
    private final RepositoryMethodPrefix methodPrefix;
    private final String whereExpression;
    private final List<OrderByQueryAttribute> orderByAttributes;
    private final Specification<?> specification;

    protected EntityViewQueryRoot(String method, EntityViewAwareRepositoryMetadata repo, RepositoryMethodPrefix methodPrefix) {
        this.entityClass = repo.getEntityMetadata().getEntityClass();
        this.methodPrefix = methodPrefix;
        build(method, method, repo.getRepositoryClass(), entityClass);
        EntityViewQueryBuilderContext ctx = new EntityViewQueryBuilderContext();
        buildQuery(ctx);
        StringBuilder whereExpressionBuilder = ctx.getWhereExpressionBuilder();
        this.whereExpression = whereExpressionBuilder.length() > 0 ? whereExpressionBuilder.toString() : null;
        this.orderByAttributes = ctx.getOrderByAttributes();
        this.specification = buildSpecification(ctx.reset());
    }

    public static EntityViewQueryRoot create(String method, EntityViewAwareRepositoryMetadata repo, RepositoryMethodPrefix prefix) {
        return new EntityViewQueryRoot(method, repo, prefix);
    }

    @Override
    public EntityViewQueryPart build(String queryPart, String method, Class<?> repositoryClass, Class<?> entityClass) {
        String[] orderByParts = splitByKeyword(queryPart, "OrderBy");
        if (hasQueryConditions(orderByParts)) {
            String[] orParts = splitByKeyword(removePrefix(orderByParts[0]), "Or");
            boolean first = true;
            for (String or : orParts) {
                EntityViewOrQueryPart orPart = new EntityViewOrQueryPart(first);
                first = false;
                children.add(orPart.build(or, method, repositoryClass, entityClass));
            }
        }
        if (orderByParts.length > 1) {
            EntityViewOrderByQueryPart orderByPart = new EntityViewOrderByQueryPart();
            children.add(orderByPart.build(orderByParts[1], method, repositoryClass, entityClass));
        }
        if (children.isEmpty()) {
            throw new MethodExpressionException(repositoryClass, method);
        }
        return this;
    }

    @Override
    protected EntityViewQueryPart buildQuery(EntityViewQueryBuilderContext ctx) {
        buildQueryForChildren(ctx);
        return this;
    }

    @Override
    protected Specification<?> buildSpecification(EntityViewQueryBuilderContext ctx) {
        return CompoundOperatorSpecification.and(buildSpecificationsForChildren(ctx));
    }

    public void apply(CriteriaBuilder<?> cb) {
        if (methodPrefix.isDelete()) {
            throw new UnsupportedOperationException("Delete queries not supported in entity view repositories");
        } else {
            cb.from(entityClass);
        }
        if (whereExpression != null) {
            cb.setWhereExpression(whereExpression);
        }
        for (OrderByQueryAttribute orderByAttribute : orderByAttributes) {
            orderByAttribute.buildQuery(cb);
        }
    }

    public void apply(Root<?> root, CriteriaQuery<?> query, javax.persistence.criteria.CriteriaBuilder cb) {
        if (specification != null) {
            query.where(specification.toPredicate((Root) root, query, cb));
        }
        List<Order> orders = new ArrayList<>(orderByAttributes.size());
        for (OrderByQueryAttribute orderByAttribute : orderByAttributes) {
            orders.add(orderByAttribute.buildOrder(root, cb));
        }
        query.orderBy(orders);
    }

    private boolean hasQueryConditions(String[] orderByParts) {
        String orderByPart = orderByParts[0];
        String prefix = methodPrefix.getPrefix();
        return !prefix.equals(orderByPart) && !orderByPart.matches(prefix);
    }

    private String removePrefix(String queryPart) {
        return methodPrefix.removePrefix(queryPart);
    }
}