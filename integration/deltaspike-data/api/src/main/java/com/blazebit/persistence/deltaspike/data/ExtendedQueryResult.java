/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data;

import com.blazebit.persistence.PagedList;
import org.apache.deltaspike.data.api.QueryResult;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.List;

/**
 * An extended version of {@link QueryResult} that allows to configure Blaze-Persistence specific features.
 *
 * @param <E> Entity type.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ExtendedQueryResult<E> extends QueryResult<E> {

    /**
     * Returns the result list of the page for which this query was constructed for without executing a count query.
     *
     * @return The result list of the requested page
     */
    List<E> getPageResultList();

    /**
     * If paginated, returns a {@link PagedList} containing the result list of the requested page and optionally the total count depending on {@link #withCountQuery(boolean)}. Otherwise, returns a {@link List}.
     *
     * @return The result as {@link PagedList} if paginated or {@link List} otherwise
     */
    @Override
    List<E> getResultList();

    /**
     * Enables or disables the execution of the count query which determines whether {@link PagedList#getTotalSize()} is available.
     *
     * @param withCountQuery true to enable, false to disable the count query execution
     * @return The query result for chaining calls
     */
    ExtendedQueryResult<E> withCountQuery(boolean withCountQuery);

    /* Covariant overrides */

    @Override
    <X> ExtendedQueryResult<E> orderAsc(SingularAttribute<E, X> attribute);

    @Override
    <X> ExtendedQueryResult<E> orderAsc(SingularAttribute<E, X> attribute, boolean appendEntityName);

    @Override
    ExtendedQueryResult<E> orderAsc(String attribute);

    @Override
    ExtendedQueryResult<E> orderAsc(String attribute, boolean appendEntityName);

    @Override
    <X> ExtendedQueryResult<E> orderDesc(SingularAttribute<E, X> attribute);

    @Override
    <X> ExtendedQueryResult<E> orderDesc(SingularAttribute<E, X> attribute, boolean appendEntityName);

    @Override
    ExtendedQueryResult<E> orderDesc(String attribute);

    @Override
    ExtendedQueryResult<E> orderDesc(String attribute, boolean appendEntityName);

    @Override
    <X> ExtendedQueryResult<E> changeOrder(SingularAttribute<E, X> attribute);

    @Override
    ExtendedQueryResult<E> clearOrder();

    @Override
    ExtendedQueryResult<E> changeOrder(String attribute);

    @Override
    ExtendedQueryResult<E> maxResults(int max);

    @Override
    ExtendedQueryResult<E> firstResult(int first);

    @Override
    ExtendedQueryResult<E> lockMode(LockModeType lockMode);

    @Override
    ExtendedQueryResult<E> flushMode(FlushModeType flushMode);

    @Override
    ExtendedQueryResult<E> hint(String hint, Object value);

    @Override
    ExtendedQueryResult<E> withPageSize(int pageSize);

    @Override
    ExtendedQueryResult<E> toPage(int page);

    @Override
    ExtendedQueryResult<E> nextPage();

    @Override
    ExtendedQueryResult<E> previousPage();
}