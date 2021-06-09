/*
 * Copyright 2014 - 2021 Blazebit.
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
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAInsertClause;
import com.querydsl.jpa.impl.JPAUpdateClause;

import javax.persistence.EntityManager;

/**
 * Query factory to simplify {@link BlazeJPAQuery} instantiation.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.6.2
 */
public class JPQLNextQueryFactory implements JPQLQueryFactory {

    private final EntityManager entityManager;
    private final CriteriaBuilderFactory criteriaBuilderFactory;
    private JPQLNextTemplates templates;

    public JPQLNextQueryFactory(EntityManager entityManager, CriteriaBuilderFactory criteriaBuilderFactory) {
        this.entityManager = entityManager;
        this.criteriaBuilderFactory = criteriaBuilderFactory;
    }

    public JPQLNextQueryFactory(JPQLNextTemplates templates, EntityManager entityManager, CriteriaBuilderFactory criteriaBuilderFactory) {
        this.templates = templates;
        this.entityManager = entityManager;
        this.criteriaBuilderFactory = criteriaBuilderFactory;
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
        return query().from(from).select(from);
    }

    @Override
    public BlazeJPAQuery<?> from(EntityPath<?> from) {
        return query().from(from);
    }

    @Override
    public BlazeJPAQuery<?> from(EntityPath<?>... from) {
        return query().from(from);
    }


    @Override
    public BlazeJPAQuery<?> query() {
        if (templates != null) {
            return new BlazeJPAQuery<Void>(entityManager, templates, criteriaBuilderFactory);
        } else {
            return new BlazeJPAQuery<Void>(entityManager, criteriaBuilderFactory);
        }
    }

    @Override
    public JPAUpdateClause update(EntityPath<?> path) {
        if (templates != null) {
            return new JPAUpdateClause(entityManager, path, templates);
        } else {
            return new JPAUpdateClause(entityManager, path);
        }
    }

    @Override
    public JPAInsertClause insert(EntityPath<?> path) {
        if (templates != null) {
            return new JPAInsertClause(entityManager, path, templates);
        } else {
            return new JPAInsertClause(entityManager, path);
        }
    }

    @Override
    public DeleteClause<?> delete(EntityPath<?> path) {
        if (templates != null) {
            return new JPADeleteClause(entityManager, path, templates);
        } else {
            return new JPADeleteClause(entityManager, path);
        }
    }

}
