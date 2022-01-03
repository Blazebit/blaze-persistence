/*
 * Copyright 2014 - 2022 Blazebit.
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
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPQLTemplates;

import javax.persistence.EntityManager;

/**
 * BlazeJPAQuery is the default implementation of the JPQLQuery interface for Blaze-Persistence JPQL.Next
 * @param <T> Query result type
 * @author Jan-Willem Gmelig Meyling
 * @since 1.5.0
 */
@SuppressWarnings("unused")
public class BlazeJPAQuery<T> extends AbstractBlazeJPAQuery<T, BlazeJPAQuery<T>> implements JPQLNextQuery<T, BlazeJPAQuery<T>>, ExtendedFetchable<T> {

    private static final long serialVersionUID = -7384005953945213671L;

    /**
     * Creates a new detached query The query can be attached via the clone method
     */
    public BlazeJPAQuery() {
        this(null);
    }

    /**
     * Creates a new CriteriaBuilderFactory bound query
     *
     * @param criteriaBuilderFactory the CriteriaBuilderFactory
     */
    public BlazeJPAQuery(CriteriaBuilderFactory criteriaBuilderFactory) {
        super(criteriaBuilderFactory);
    }

    /**
     * Creates a new query
     *
     * @param em The {@code EntityManager}
     * @param criteriaBuilderFactory The {@code CriteriaBuilderFactory}
     */
    public BlazeJPAQuery(EntityManager em, CriteriaBuilderFactory criteriaBuilderFactory) {
        super(em, criteriaBuilderFactory);
    }

    /**
     * Creates a new query
     *
     * @param em The {@code EntityManager}
     * @param metadata The {@code QueryMetadata}
     * @param criteriaBuilderFactory The {@code CriteriaBuilderFactory}
     */
    public BlazeJPAQuery(EntityManager em, QueryMetadata metadata, CriteriaBuilderFactory criteriaBuilderFactory) {
        super(em, metadata, criteriaBuilderFactory);
    }

    /**
     *
     * Creates a new query
     *
     * @param em The {@code EntityManager}
     * @param templates The templates
     * @param criteriaBuilderFactory The {@code CriteriaBuilderFactory}
     */
    public BlazeJPAQuery(EntityManager em, JPQLTemplates templates, CriteriaBuilderFactory criteriaBuilderFactory) {
        super(em, templates, criteriaBuilderFactory);
    }

    /**
     * Creates a new query
     *
     * @param em The {@code EntityManager}
     * @param templates The templates
     * @param metadata The metadata implementation
     * @param criteriaBuilderFactory The {@code CriteriaBuilderFactory}
     */
    public BlazeJPAQuery(EntityManager em, JPQLTemplates templates, QueryMetadata metadata, CriteriaBuilderFactory criteriaBuilderFactory) {
        super(em, templates, metadata, criteriaBuilderFactory);
    }

    @Override
    public BlazeJPAQuery<T> clone(EntityManager entityManager, JPQLTemplates templates) {
        BlazeJPAQuery<T> q = new BlazeJPAQuery<T>(entityManager, templates, getMetadata().clone(), criteriaBuilderFactory);
        q.clone(this);
        return q;
    }

    @Override
    public BlazeJPAQuery<T> clone(EntityManager entityManager) {
        return clone(entityManager, getTemplates());
    }

    @Override
    public <U> BlazeJPAQuery<U> select(Expression<U> expr) {
        queryMixin.setProjection(expr);
        @SuppressWarnings("unchecked") // This is the new type
                BlazeJPAQuery<U> newType = (BlazeJPAQuery<U>) this;
        return newType;
    }

    @Override
    public BlazeJPAQuery<Tuple> select(Expression<?>... exprs) {
        queryMixin.setProjection(exprs);
        @SuppressWarnings("unchecked") // This is the new type
                BlazeJPAQuery<Tuple> newType = (BlazeJPAQuery<Tuple>) this;
        return newType;
    }

    // TODO: Package protected?
    public <T> BlazeJPAQuery<T> createSubQuery() {
        return new BlazeJPAQuery<T>(entityManager, getTemplates(), criteriaBuilderFactory);
    }

}
