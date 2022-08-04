/*
 * Copyright 2021-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.spring.data.impl.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.Subgraph;
import javax.persistence.TypedQuery;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.Assert;

/**
 * Immutable implementation of {@link FetchableFluentQuery} based on Query by {@link Example}. All methods that return a
 * {@link FetchableFluentQuery} will return a new instance, not the original.
 *
 * Christian Beikov: Copied to be able to share code between Spring Data integrations for 2.6 and 2.7.
 *
 * @param <S> Domain type
 * @param <R> Result type
 * @author Greg Turnquist
 * @author Mark Paluch
 * @author Jens Schauder
 * @author J.R. Onyschak
 * @since 2.6
 */
public class FetchableFluentQueryByExample<S, R> extends FluentQuerySupport<S, R> implements FetchableFluentQuery<R> {

    private final Example<S> example;
    private final Function<Sort, TypedQuery<S>> finder;
    private final Function<Example<S>, Long> countOperation;
    private final Function<Example<S>, Boolean> existsOperation;
    private final EntityManager entityManager;
    private final EscapeCharacter escapeCharacter;

    public FetchableFluentQueryByExample(Example<S> example, Function<Sort, TypedQuery<S>> finder,
                                         Function<Example<S>, Long> countOperation, Function<Example<S>, Boolean> existsOperation,
                                         EntityManager entityManager, EscapeCharacter escapeCharacter) {
        this(example, example.getProbeType(), (Class<R>) example.getProbeType(), Sort.unsorted(), Collections.emptySet(),
             finder, countOperation, existsOperation, entityManager, escapeCharacter);
    }

    private FetchableFluentQueryByExample(Example<S> example, Class<S> entityType, Class<R> returnType, Sort sort,
                                          Collection<String> properties, Function<Sort, TypedQuery<S>> finder, Function<Example<S>, Long> countOperation,
                                          Function<Example<S>, Boolean> existsOperation,
                                          EntityManager entityManager, EscapeCharacter escapeCharacter) {

        super(returnType, sort, properties, entityType);
        this.example = example;
        this.finder = finder;
        this.countOperation = countOperation;
        this.existsOperation = existsOperation;
        this.entityManager = entityManager;
        this.escapeCharacter = escapeCharacter;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#sortBy(org.springframework.data.domain.Sort)
     */
    @Override
    public FetchableFluentQuery<R> sortBy(Sort sort) {

        Assert.notNull(sort, "Sort must not be null!");

        return new FetchableFluentQueryByExample<>(example, entityType, resultType, this.sort.and(sort), properties,
                                                   finder, countOperation, existsOperation, entityManager, escapeCharacter);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#as(java.lang.Class)
     */
    @Override
    public <NR> FetchableFluentQuery<NR> as(Class<NR> resultType) {

        Assert.notNull(resultType, "Projection target type must not be null!");
        if (!resultType.isInterface()) {
            throw new UnsupportedOperationException("Class-based DTOs are not yet supported.");
        }

        return new FetchableFluentQueryByExample<>(example, entityType, resultType, sort, properties, finder,
                                                   countOperation, existsOperation, entityManager, escapeCharacter);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#project(java.util.Collection)
     */
    @Override
    public FetchableFluentQuery<R> project(Collection<String> properties) {

        return new FetchableFluentQueryByExample<>(example, entityType, resultType, sort, mergeProperties(properties),
                                                   finder, countOperation, existsOperation, entityManager, escapeCharacter);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#oneValue()
     */
    @Override
    public R oneValue() {

        TypedQuery<S> limitedQuery = createSortedAndProjectedQuery();
        limitedQuery.setMaxResults(2); // Never need more than 2 values

        List<S> results = limitedQuery.getResultList();

        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1);
        }

        return results.isEmpty() ? null : getConversionFunction().apply(results.get(0));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#firstValue()
     */
    @Override
    public R firstValue() {

        TypedQuery<S> limitedQuery = createSortedAndProjectedQuery();
        limitedQuery.setMaxResults(1); // Never need more than 1 value

        List<S> results = limitedQuery.getResultList();

        return results.isEmpty() ? null : getConversionFunction().apply(results.get(0));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#all()
     */
    @Override
    public List<R> all() {

        List<S> resultList = createSortedAndProjectedQuery().getResultList();

        return convert(resultList);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#page(org.springframework.data.domain.Pageable)
     */
    @Override
    public Page<R> page(Pageable pageable) {
        return pageable.isUnpaged() ? new PageImpl<>(all()) : readPage(pageable);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#stream()
     */
    @Override
    public Stream<R> stream() {

        return createSortedAndProjectedQuery() //
            .getResultStream() //
            .map(getConversionFunction());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#count()
     */
    @Override
    public long count() {
        return countOperation.apply(example);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery#exists()
     */
    @Override
    public boolean exists() {
        return existsOperation.apply(example);
    }

    private Page<R> readPage(Pageable pageable) {

        TypedQuery<S> pagedQuery = createSortedAndProjectedQuery();

        if (pageable.isPaged()) {
            pagedQuery.setFirstResult((int) pageable.getOffset());
            pagedQuery.setMaxResults(pageable.getPageSize());
        }

        List<R> paginatedResults = convert(pagedQuery.getResultList());

        return PageableExecutionUtils.getPage(paginatedResults, pageable, () -> countOperation.apply(example));
    }

    private TypedQuery<S> createSortedAndProjectedQuery() {

        TypedQuery<S> query = finder.apply(sort);

        if (!properties.isEmpty()) {
            query.setHint("javax.persistence.fetchgraph", create(entityManager, entityType, properties));
        }

        return query;
    }

    private static <T> EntityGraph<T> create(EntityManager entityManager, Class<T> domainType, Set<String> properties) {

        EntityGraph<T> entityGraph = entityManager.createEntityGraph(domainType);

        for (String property : properties) {

            Subgraph<Object> current = null;

            for (PropertyPath path : PropertyPath.from(property, domainType)) {

                if (path.hasNext()) {
                    current = current == null ? entityGraph.addSubgraph(path.getSegment())
                        : current.addSubgraph(path.getSegment());
                    continue;
                }

                if (current == null) {
                    entityGraph.addAttributeNodes(path.getSegment());
                } else {
                    current.addAttributeNodes(path.getSegment());

                }
            }
        }

        return entityGraph;
    }

    private List<R> convert(List<S> resultList) {

        Function<Object, R> conversionFunction = getConversionFunction();
        List<R> mapped = new ArrayList<>(resultList.size());

        for (S s : resultList) {
            mapped.add(conversionFunction.apply(s));
        }
        return mapped;
    }

    private Function<Object, R> getConversionFunction() {
        return getConversionFunction(example.getProbeType(), resultType);
    }

}
