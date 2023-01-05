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

package com.blazebit.persistence.querydsl;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.querydsl.core.Tuple;
import com.querydsl.core.dml.DeleteClause;
import com.querydsl.core.dml.InsertClause;
import com.querydsl.core.dml.UpdateClause;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAInsertClause;
import com.querydsl.jpa.impl.JPAUpdateClause;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;

/**
 * Query factory to simplify {@link BlazeJPAQuery} instantiation.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.6.7
 */
public class BlazeJPAQueryFactory implements JPQLNextQueryFactory {

    private final EntityManager entityManager;
    private final CriteriaBuilderFactory criteriaBuilderFactory;
    private final JPQLNextTemplates templates;

    public BlazeJPAQueryFactory(EntityManager entityManager, CriteriaBuilderFactory criteriaBuilderFactory) {
        this(JPQLNextTemplates.DEFAULT, entityManager, criteriaBuilderFactory);
    }

    public BlazeJPAQueryFactory(JPQLNextTemplates templates, EntityManager entityManager, CriteriaBuilderFactory criteriaBuilderFactory) {
        this.templates = templates;
        this.entityManager = entityManager;
        this.criteriaBuilderFactory = criteriaBuilderFactory;
    }

    @Override
    public DeleteClause<?> delete(EntityPath<?> path) {
        return new JPADeleteClause(entityManager, path, templates);
    }

    @Override
    public <T> BlazeJPAQuery<T> select(Expression<T> expr) {
        return query().select(expr);
    }

    @Override
    public BlazeJPAQuery<Tuple> select(Expression<?>... exprs) {
        return query().select(exprs);
    }

    @Override
    public <T> BlazeJPAQuery<T> selectDistinct(Expression<T> expr) {
        return select(expr).distinct();
    }

    @Override
    public BlazeJPAQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return select(exprs).distinct();
    }

    @Override
    public BlazeJPAQuery<Integer> selectOne() {
        return select(Expressions.ONE);
    }

    @Override
    public BlazeJPAQuery<Integer> selectZero() {
        return select(Expressions.ZERO);
    }

    @Override
    public <T> BlazeJPAQuery<T> selectFrom(EntityPath<T> from) {
        return select(from).from(from);
    }

    @Override
    public BlazeJPAQuery<?> from(EntityPath<?> from) {
        return select(from).from(from);
    }

    @Override
    public BlazeJPAQuery<?> from(EntityPath<?>... from) {
        return select(from).from(from);
    }

    @Override
    public <X> BlazeJPAQuery<?> from(SubQueryExpression<X> subQueryExpression, Path<X> alias) {
        return query().from(subQueryExpression, alias);
    }

    @Override
    public <X> BlazeJPAQuery<X> selectFrom(SubQueryExpression<X> subQueryExpression, Path<X> alias) {
        return query().select(alias).from(subQueryExpression, alias);
    }

    @Override
    public <X> BlazeJPAQuery<?> fromValues(EntityPath<X> path, Collection<X> elements) {
        return query().fromValues(path, elements);
    }

    @Override
    public <X> BlazeJPAQuery<?> fromIdentifiableValues(EntityPath<X> path, Collection<X> elements) {
        return query().fromIdentifiableValues(path, elements);
    }

    @Override
    public <X> BlazeJPAQuery<?> fromValues(Path<X> path, Path<X> alias, Collection<X> elements) {
        return query().fromValues(path, alias, elements);
    }

    @Override
    public <X> BlazeJPAQuery<?> fromIdentifiableValues(Path<X> path, Path<X> alias, Collection<X> elements) {
        return query().fromIdentifiableValues(path, alias, elements);
    }

    @Override
    public <X> BlazeJPAQuery<?> with(Path<X> alias, SubQueryExpression<?> o) {
        return query().with(alias, o);
    }

    @Override
    public <X> BlazeJPAQuery<?> withRecursive(Path<X> alias, SubQueryExpression<?> o) {
        return query().withRecursive(alias, o);
    }

    @Override
    public WithBuilder<? extends BlazeJPAQuery<?>> with(EntityPath<?> alias, Path<?>... columns) {
        return query().with(alias, columns);
    }

    @Override
    public WithBuilder<? extends BlazeJPAQuery<?>> withRecursive(EntityPath<?> alias, Path<?>... columns) {
        return query().withRecursive(alias, columns);
    }

    @Override
    public UpdateClause<?> update(EntityPath<?> path) {
        return new JPAUpdateClause(entityManager, path, templates);
    }

    @Override
    public InsertClause<?> insert(EntityPath<?> path) {
        return new JPAInsertClause(entityManager, path, templates);
    }

    @Override
    public BlazeJPAQuery<?> query() {
        return new BlazeJPAQuery<>(entityManager, templates, criteriaBuilderFactory);
    }

    @Override
    public <RT> SetExpression<RT> union(List<SubQueryExpression<RT>> sq) {
        return query().union(sq);
    }

    @Override
    public <RT> SetExpression<RT> unionAll(List<SubQueryExpression<RT>> sq) {
        return query().unionAll(sq);
    }

    @Override
    public <RT> SetExpression<RT> intersect(List<SubQueryExpression<RT>> sq) {
        return query().intersect(sq);
    }

    @Override
    public <RT> SetExpression<RT> intersectAll(List<SubQueryExpression<RT>> sq) {
        return query().intersectAll(sq);
    }

    @Override
    public <RT> SetExpression<RT> except(List<SubQueryExpression<RT>> sq) {
        return query().except(sq);
    }

    @Override
    public <RT> SetExpression<RT> exceptAll(List<SubQueryExpression<RT>> sq) {
        return query().exceptAll(sq);
    }

    @Override
    @SafeVarargs
    public final <RT> SetExpression<RT> union(SubQueryExpression<RT>... sq) {
        return query().union(sq);
    }

    @Override
    @SafeVarargs
    public final <RT> SetExpression<RT> unionAll(SubQueryExpression<RT>... sq) {
        return query().unionAll(sq);
    }

    @Override
    @SafeVarargs
    public final <RT> SetExpression<RT> intersect(SubQueryExpression<RT>... sq) {
        return query().intersect(sq);
    }

    @Override
    @SafeVarargs
    public final <RT> SetExpression<RT> intersectAll(SubQueryExpression<RT>... sq) {
        return query().intersectAll(sq);
    }

    @Override
    @SafeVarargs
    public final <RT> SetExpression<RT> except(SubQueryExpression<RT>... sq) {
        return query().except(sq);
    }

    @Override
    @SafeVarargs
    public final <RT> SetExpression<RT> exceptAll(SubQueryExpression<RT>... sq) {
        return query().exceptAll(sq);
    }

}
