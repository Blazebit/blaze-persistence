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

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.MultipleSubqueryInitiator;
import com.blazebit.persistence.WhereBuilder;
import com.blazebit.persistence.criteria.BlazeCommonAbstractCriteria;
import com.blazebit.persistence.criteria.BlazeRoot;
import com.blazebit.persistence.criteria.BlazeSubquery;
import com.blazebit.persistence.criteria.impl.expression.AbstractSelection;
import com.blazebit.persistence.criteria.impl.expression.SubqueryExpression;
import com.blazebit.persistence.criteria.impl.path.RootImpl;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class AbstractModificationCriteriaQuery<T> implements BlazeCommonAbstractCriteria {

    protected final BlazeCriteriaBuilderImpl criteriaBuilder;

    private RootImpl<T> root;
    private Predicate restriction;
    private List<BlazeSubquery<?>> subqueries;

    protected AbstractModificationCriteriaQuery(BlazeCriteriaBuilderImpl criteriaBuilder) {
        this.criteriaBuilder = criteriaBuilder;
    }

    public BlazeRoot<T> from(Class<T> entityClass) {
        return from(entityClass, null);
    }

    public BlazeRoot<T> from(EntityType<T> entityType) {
        return from(entityType, null);
    }

    public BlazeRoot<T> from(Class<T> entityClass, String alias) {
        EntityType<T> entityType = criteriaBuilder.getEntityMetamodel()
                .entity(entityClass);
        if (entityType == null) {
            throw new IllegalArgumentException(entityClass + " is not an entity");
        }
        return from(entityType, alias);
    }

    public BlazeRoot<T> from(EntityType<T> entityType, String alias) {
        root = new RootImpl<T>(criteriaBuilder, entityType, alias, false);
        return root;
    }

    public BlazeRoot<T> getRoot() {
        return root;
    }

    protected void setRestriction(Expression<Boolean> restriction) {
        if (restriction == null) {
            this.restriction = null;
        } else {
            this.restriction = criteriaBuilder.wrap(restriction);
        }
    }

    public void setRestriction(Predicate... restrictions) {
        if (restrictions == null || restrictions.length == 0) {
            this.restriction = null;
        } else {
            this.restriction = criteriaBuilder.and(restrictions);
        }
    }

    @Override
    public Predicate getRestriction() {
        return restriction;
    }

    @Override
    public <U> BlazeSubquery<U> subquery(Class<U> subqueryType) {
        SubqueryExpression<U> subquery = new SubqueryExpression<U>(criteriaBuilder, subqueryType, this);
        internalGetSubqueries().add(subquery);
        return subquery;
    }

    public List<BlazeSubquery<?>> internalGetSubqueries() {
        if (subqueries == null) {
            subqueries = new ArrayList<BlazeSubquery<?>>();
        }
        return subqueries;
    }

    protected void renderWhere(WhereBuilder<?> wb, RenderContextImpl context) {
        if (restriction == null) {
            return;
        }

        context.setClauseType(RenderContext.ClauseType.WHERE);
        context.getBuffer().setLength(0);
        ((AbstractSelection<?>) restriction).render(context);
        String expression = context.takeBuffer();
        Map<String, InternalQuery<?>> aliasToSubqueries = context.takeAliasToSubqueryMap();

        if (aliasToSubqueries.isEmpty()) {
            wb.setWhereExpression(expression);
        } else {
            MultipleSubqueryInitiator<?> initiator = wb.setWhereExpressionSubqueries(expression);

            for (Map.Entry<String, InternalQuery<?>> subqueryEntry : aliasToSubqueries.entrySet()) {
                context.pushSubqueryInitiator(initiator.with(subqueryEntry.getKey()));
                subqueryEntry.getValue().renderSubquery(context);
                context.popSubqueryInitiator();
            }

            initiator.end();
        }
    }

}
