/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.handler;

import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedTypedQuery;
import com.blazebit.persistence.deltaspike.data.KeysetAwarePage;
import com.blazebit.persistence.deltaspike.data.KeysetAwareSlice;
import com.blazebit.persistence.deltaspike.data.Page;
import com.blazebit.persistence.deltaspike.data.Slice;
import com.blazebit.persistence.deltaspike.data.base.handler.KeysetAwarePageImpl;
import com.blazebit.persistence.deltaspike.data.base.handler.KeysetAwareSliceImpl;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.data.impl.builder.result.QueryProcessor;
import org.apache.deltaspike.data.impl.builder.result.QueryProcessorFactory;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;
import jakarta.persistence.Query;
import java.util.List;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.result.QueryProcessorFactory} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
@Specializes
@ApplicationScoped
public class EntityViewQueryProcessorFactory extends QueryProcessorFactory {

    public QueryProcessor build(RepositoryMethodMetadata methodMetadata) {
        if (ClassUtils.returns(methodMetadata.getMethod(), List.class) || ClassUtils.returns(methodMetadata.getMethod(), PagedList.class)) {
            return new ListQueryProcessor();
        }
        if (ClassUtils.returns(methodMetadata.getMethod(), Slice.class) || ClassUtils.returns(methodMetadata.getMethod(), KeysetAwareSlice.class)) {
            return new SliceQueryProcessor();
        }
        if (ClassUtils.returns(methodMetadata.getMethod(), Page.class) || ClassUtils.returns(methodMetadata.getMethod(), KeysetAwarePage.class)) {
            return new PageQueryProcessor();
        }
        return super.build(methodMetadata);
    }

    /**
     * @author Moritz Becker
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class ListQueryProcessor implements QueryProcessor {
        @Override
        public Object executeQuery(Query query, CdiQueryInvocationContext context) {
            return query.getResultList();
        }
    }

    /**
     * @author Moritz Becker
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class SliceQueryProcessor implements QueryProcessor {
        @Override
        public Object executeQuery(Query query, CdiQueryInvocationContext context) {
            EntityViewCdiQueryInvocationContext c = (EntityViewCdiQueryInvocationContext) context;
            List list;
            if (query instanceof PaginatedTypedQuery<?>) {
                list = ((PaginatedTypedQuery) query).getPageResultList();
            } else {
                list = query.getResultList();
            }
            if (list instanceof PagedList) {
                return new KeysetAwareSliceImpl<>((PagedList) list, c.getExtendedParams().getPageable());
            } else {
                return new KeysetAwareSliceImpl<>(list, c.getExtendedParams().getPageable());
            }
        }
    }

    /**
     * @author Moritz Becker
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class PageQueryProcessor implements QueryProcessor {
        @Override
        public Object executeQuery(Query query, CdiQueryInvocationContext context) {
            EntityViewCdiQueryInvocationContext c = (EntityViewCdiQueryInvocationContext) context;
            List list = query.getResultList();
            if (list instanceof PagedList) {
                return new KeysetAwarePageImpl<>((PagedList) list, c.getExtendedParams().getPageable());
            } else {
                return new KeysetAwarePageImpl<>(list, c.getExtendedParams().getPageable());
            }
        }
    }

}