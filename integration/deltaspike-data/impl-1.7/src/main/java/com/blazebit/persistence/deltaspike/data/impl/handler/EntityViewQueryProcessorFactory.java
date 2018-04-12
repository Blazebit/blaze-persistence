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

package com.blazebit.persistence.deltaspike.data.impl.handler;

import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedTypedQuery;
import com.blazebit.persistence.deltaspike.data.KeysetAwarePage;
import com.blazebit.persistence.deltaspike.data.KeysetAwareSlice;
import com.blazebit.persistence.deltaspike.data.Page;
import com.blazebit.persistence.deltaspike.data.Slice;
import com.blazebit.persistence.deltaspike.data.base.handler.KeysetAwarePageImpl;
import com.blazebit.persistence.deltaspike.data.base.handler.KeysetAwareSliceImpl;
import com.blazebit.persistence.deltaspike.data.impl.builder.result.EntityViewQueryProcessor;
import org.apache.deltaspike.core.util.OptionalUtil;
import org.apache.deltaspike.core.util.StreamUtil;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.QueryResult;
import org.apache.deltaspike.data.api.SingleResultType;
import org.apache.deltaspike.data.impl.meta.MethodPrefix;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.result.QueryProcessorFactory} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public class EntityViewQueryProcessorFactory {
    private final Method method;
    private final MethodPrefix methodPrefix;

    private EntityViewQueryProcessorFactory(Method method) {
        this.method = method;
        this.methodPrefix = new MethodPrefix("", method.getName());
    }

    private EntityViewQueryProcessorFactory(Method method, MethodPrefix methodPrefix) {
        this.method = method;
        this.methodPrefix = methodPrefix;
    }

    public static EntityViewQueryProcessorFactory newInstance(Method method) {
        return new EntityViewQueryProcessorFactory(method);
    }

    public static EntityViewQueryProcessorFactory newInstance(Method method, MethodPrefix methodPrefix) {
        return new EntityViewQueryProcessorFactory(method, methodPrefix);
    }

    public EntityViewQueryProcessor build() {
        if (returns(QueryResult.class)) {
            return new EntityViewQueryProcessorFactory.NoOpQueryProcessor();
        }
        if (returns(List.class) || returns(PagedList.class)) {
            return new EntityViewQueryProcessorFactory.ListQueryProcessor();
        }
        if (returns(Slice.class) || returns(KeysetAwareSlice.class)) {
            return new EntityViewQueryProcessorFactory.SliceQueryProcessor();
        }
        if (returns(Page.class) || returns(KeysetAwarePage.class)) {
            return new EntityViewQueryProcessorFactory.PageQueryProcessor();
        }
        if (streams()) {
            return new EntityViewQueryProcessorFactory.StreamQueryProcessor();
        }
        if (isModifying()) {
            return new EntityViewQueryProcessorFactory.ExecuteUpdateQueryProcessor(returns(Void.TYPE));
        }
        return new EntityViewQueryProcessorFactory.SingleResultQueryProcessor();
    }

    private boolean isModifying() {
        boolean matchesType = Void.TYPE.equals(method.getReturnType()) ||
                int.class.equals(method.getReturnType()) ||
                Integer.class.equals(method.getReturnType());
        return (method.isAnnotationPresent(Modifying.class) && matchesType) || methodPrefix.isDelete();
    }

    private boolean returns(Class<?> clazz) {
        return method.getReturnType().isAssignableFrom(clazz);
    }

    private boolean streams() {
        return StreamUtil.isStreamReturned(method);
    }

    /**
     * @author Moritz Becker
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class ListQueryProcessor implements EntityViewQueryProcessor {
        @Override
        public Object executeQuery(Query query, EntityViewCdiQueryInvocationContext context) {
            return query.getResultList();
        }
    }

    /**
     * @author Moritz Becker
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class SliceQueryProcessor implements EntityViewQueryProcessor {
        @Override
        public Object executeQuery(Query query, EntityViewCdiQueryInvocationContext context) {
            List list;
            if (query instanceof PaginatedTypedQuery<?>) {
                list = ((PaginatedTypedQuery) query).getPageResultList();
            } else {
                list = query.getResultList();
            }
            if (list instanceof PagedList) {
                return new KeysetAwareSliceImpl<>((PagedList) list, context.getParams().getPageable());
            } else {
                return new KeysetAwareSliceImpl<>(list, context.getParams().getPageable());
            }
        }
    }

    /**
     * @author Moritz Becker
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class PageQueryProcessor implements EntityViewQueryProcessor {
        @Override
        public Object executeQuery(Query query, EntityViewCdiQueryInvocationContext context) {
            List list = query.getResultList();
            if (list instanceof PagedList) {
                return new KeysetAwarePageImpl<>((PagedList) list, context.getParams().getPageable());
            } else {
                return new KeysetAwarePageImpl<>(list, context.getParams().getPageable());
            }
        }
    }

    /**
     * @author Moritz Becker
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class NoOpQueryProcessor implements EntityViewQueryProcessor {
        @Override
        public Object executeQuery(Query query, EntityViewCdiQueryInvocationContext context) {
            return query;
        }
    }

    /**
     * @author Moritz Becker
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class StreamQueryProcessor implements EntityViewQueryProcessor {
        @Override
        public Object executeQuery(Query query, EntityViewCdiQueryInvocationContext context) {
            return StreamUtil.wrap(query.getResultList());
        }
    }

    /**
     * @author Moritz Becker
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class SingleResultQueryProcessor implements EntityViewQueryProcessor {
        @Override
        public Object executeQuery(Query query, EntityViewCdiQueryInvocationContext context) {
            SingleResultType style = context.getSingleResultStyle();
            Object result = null;
            switch (style) {
                case JPA:
                    return query.getSingleResult();
                case OPTIONAL:
                    try {
                        result = query.getSingleResult();
                    } catch (NoResultException e) {
                    }
                    break;
                default:
                    @SuppressWarnings("unchecked")
                    List<Object> queryResult = query.getResultList();
                    result = !queryResult.isEmpty() ? queryResult.get(0) : null;
            }
            if (context.isOptional()) {
                return OptionalUtil.wrap(result);
            } else {
                return result;
            }
        }
    }

    /**
     * @author Moritz Becker
     * @author Christian Beikov
     * @since 1.2.0
     */
    private static final class ExecuteUpdateQueryProcessor implements EntityViewQueryProcessor {

        private final boolean returnsVoid;

        private ExecuteUpdateQueryProcessor(boolean returnsVoid) {
            this.returnsVoid = returnsVoid;
        }

        @Override
        public Object executeQuery(Query query, EntityViewCdiQueryInvocationContext context) {
            int result = query.executeUpdate();
            if (!returnsVoid) {
                return result;
            }
            return null;
        }
    }
}