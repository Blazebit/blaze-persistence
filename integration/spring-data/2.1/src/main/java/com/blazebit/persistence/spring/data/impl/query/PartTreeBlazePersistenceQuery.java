/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.impl.query;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.spring.data.base.query.AbstractPartTreeBlazePersistenceQuery;
import com.blazebit.persistence.spring.data.base.query.EntityViewAwareJpaQueryMethod;
import com.blazebit.persistence.spring.data.base.query.JpaParameters;
import com.blazebit.persistence.spring.data.base.query.KeysetAwarePageImpl;
import com.blazebit.persistence.spring.data.base.query.KeysetAwareSliceImpl;
import com.blazebit.persistence.spring.data.base.query.ParameterBinder;
import com.blazebit.persistence.spring.data.base.query.ParameterMetadataProvider;
import com.blazebit.persistence.spring.data.repository.KeysetPageable;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.AbstractJpaQuery;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.Jpa21Utils;
import org.springframework.data.jpa.repository.query.JpaEntityGraph;
import org.springframework.data.jpa.repository.query.JpaQueryExecution;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.parser.PartTree;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PartTreeBlazePersistenceQuery extends AbstractPartTreeBlazePersistenceQuery {

    public PartTreeBlazePersistenceQuery(EntityViewAwareJpaQueryMethod method, EntityManager em, PersistenceProvider persistenceProvider, EscapeCharacter escape, CriteriaBuilderFactory cbf, EntityViewManager evm) {
        super(method, em, persistenceProvider, escape, cbf, evm);
    }

    @Override
    protected ParameterMetadataProvider createParameterMetadataProvider(CriteriaBuilder builder, ParametersParameterAccessor accessor, PersistenceProvider provider, Object escape) {
        return new ParameterMetadataProviderImpl(builder, accessor, provider, (EscapeCharacter) escape);
    }

    @Override
    protected ParameterMetadataProvider createParameterMetadataProvider(CriteriaBuilder builder, JpaParameters parameters, PersistenceProvider provider, Object escape) {
        return new ParameterMetadataProviderImpl(builder, parameters, provider, (EscapeCharacter) escape);
    }

    @Override
    protected JpaQueryExecution getExecution() {
        if (getQueryMethod().isSliceQuery()) {
            return new PartTreeBlazePersistenceQuery.SlicedExecution();
        } else if (getQueryMethod().isPageQuery()) {
            return new PartTreeBlazePersistenceQuery.PagedExecution();
        } else if (isDelete()) {
            return new PartTreeBlazePersistenceQuery.DeleteExecution(getEntityManager());
        } else if (isExists()) {
            return new PartTreeBlazePersistenceQuery.ExistsExecution();
        } else {
            return super.getExecution();
        }
    }

    /**
     * {@link JpaQueryExecution} performing an exists check on the query.
     *
     * @author Christian Beikov
     * @since 1.3.0
     */
    private static class ExistsExecution extends JpaQueryExecution {

        @Override
        protected Object doExecute(AbstractJpaQuery repositoryQuery, Object[] values) {
            return !((PartTreeBlazePersistenceQuery) repositoryQuery).createQuery(values).getResultList().isEmpty();
        }
    }

    /**
     * Uses the {@link com.blazebit.persistence.PaginatedCriteriaBuilder} API for executing the query.
     *
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class SlicedExecution extends JpaQueryExecution {

        @Override
        @SuppressWarnings("unchecked")
        protected Object doExecute(AbstractJpaQuery repositoryQuery, Object[] values) {
            ParameterAccessor accessor = new ParametersParameterAccessor(repositoryQuery.getQueryMethod().getParameters(), values);
            Pageable pageable = accessor.getPageable();
            if (pageable.isUnpaged()) {
                List<Object> unpagedResult = ((PartTreeBlazePersistenceQuery) repositoryQuery).createQuery(values).getResultList();
                return new KeysetAwareSliceImpl<>(unpagedResult);
            }

            Query paginatedCriteriaBuilder = ((PartTreeBlazePersistenceQuery) repositoryQuery).createPaginatedQuery(values, false);
            PagedList<Object> resultList = (PagedList<Object>) paginatedCriteriaBuilder.getResultList();

            return new KeysetAwareSliceImpl<>(resultList, pageable);
        }
    }

    /**
     * Uses the {@link com.blazebit.persistence.PaginatedCriteriaBuilder} API for executing the query.
     *
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static class PagedExecution extends JpaQueryExecution {

        @Override
        @SuppressWarnings("unchecked")
        protected Object doExecute(AbstractJpaQuery repositoryQuery, Object[] values) {
            ParameterAccessor accessor = new ParametersParameterAccessor(repositoryQuery.getQueryMethod().getParameters(), values);
            Pageable pageable = accessor.getPageable();
            if (pageable.isUnpaged()) {
                List<Object> unpagedResult = ((PartTreeBlazePersistenceQuery) repositoryQuery).createQuery(values).getResultList();
                return new KeysetAwarePageImpl<>(unpagedResult);
            }

            Query paginatedCriteriaBuilder = ((PartTreeBlazePersistenceQuery) repositoryQuery).createPaginatedQuery(values, true);
            PagedList<Object> resultList = (PagedList<Object>) paginatedCriteriaBuilder.getResultList();
            Long total = resultList.getTotalSize();

            if (total.equals(0L)) {
                return new KeysetAwarePageImpl<>(Collections.emptyList(), total, null, pageable);
            }

            return new KeysetAwarePageImpl<>(resultList, pageable);
        }
    }

    /**
     * {@link JpaQueryExecution} removing entities matching the query.
     *
     * @author Thomas Darimont
     * @author Oliver Gierke
     * @since 1.6
     */
    static class DeleteExecution extends JpaQueryExecution {

        private final EntityManager em;

        public DeleteExecution(EntityManager em) {
            this.em = em;
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.jpa.repository.query.JpaQueryExecution#doExecute(org.springframework.data.jpa.repository.query.AbstractJpaQuery, java.lang.Object[])
         */
        @Override
        protected Object doExecute(AbstractJpaQuery jpaQuery, Object[] values) {
            Query query = ((PartTreeBlazePersistenceQuery) jpaQuery).createQuery(values);
            List<?> resultList = query.getResultList();

            for (Object o : resultList) {
                em.remove(o);
            }

            return jpaQuery.getQueryMethod().isCollectionQuery() ? resultList : resultList.size();
        }
    }

    @Override
    protected boolean isCountProjection(PartTree tree) {
        return tree.isCountProjection();
    }

    @Override
    protected boolean isDelete(PartTree tree) {
        return tree.isDelete();
    }

    @Override
    protected boolean isExists(PartTree tree) {
        return tree.isExistsProjection();
    }

    @Override
    protected int getOffset(Pageable pageable) {
        if (pageable.isPaged()) {
            if (pageable instanceof KeysetPageable) {
                return ((KeysetPageable) pageable).getIntOffset();
            } else {
                return (int) pageable.getOffset();
            }
        }
        return 0;
    }

    @Override
    protected int getLimit(Pageable pageable) {
        if (pageable.isPaged()) {
            return pageable.getPageSize();
        }
        return Integer.MAX_VALUE;
    }

    @Override
    protected ParameterBinder createCriteriaQueryParameterBinder(JpaParameters parameters, Object[] values, List<ParameterMetadataProvider.ParameterMetadata<?>> expressions) {
        return new CriteriaQueryParameterBinder(getEntityManager(), evm, parameters, values, expressions);
    }

    @Override
    protected Map<String, Object> tryGetFetchGraphHints(JpaEntityGraph entityGraph, Class<?> entityType) {
        return Jpa21Utils.tryGetFetchGraphHints(this.getEntityManager(), entityGraph, this.getQueryMethod().getEntityInformation().getJavaType());
    }
}
