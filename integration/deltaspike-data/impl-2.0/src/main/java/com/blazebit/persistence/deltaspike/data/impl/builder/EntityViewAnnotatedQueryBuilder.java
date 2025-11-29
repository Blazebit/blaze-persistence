/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.builder;

import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.impl.handler.CdiQueryInvocationContext;
import org.apache.deltaspike.data.impl.param.Parameters;
import org.apache.deltaspike.data.impl.util.jpa.QueryStringExtractorFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import static org.apache.deltaspike.core.util.StringUtils.isNotEmpty;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.AnnotatedQueryBuilder} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@ApplicationScoped
public class EntityViewAnnotatedQueryBuilder extends EntityViewQueryBuilder {

    private final QueryStringExtractorFactory factory = new QueryStringExtractorFactory();

    @Override
    public Object execute(EntityViewCdiQueryInvocationContext context) {
        Method method = context.getMethod();
        Query query = method.getAnnotation(Query.class);
        jakarta.persistence.Query jpaQuery = createJpaQuery(query, context);
        return context.executeQuery(jpaQuery);
    }

    private jakarta.persistence.Query createJpaQuery(Query query, CdiQueryInvocationContext context) {
        EntityManager entityManager = context.getEntityManager();
        Parameters params = context.getParams();
        jakarta.persistence.Query result = null;
        if (isNotEmpty(query.named())) {
            if (context.hasQueryStringPostProcessors()) {
                jakarta.persistence.Query namedQuery = entityManager.createNamedQuery(query.named());
                String named = factory.extract(namedQuery);
                String jpqlQuery = context.applyQueryStringPostProcessors(named);
                result = params.applyTo(entityManager.createQuery(jpqlQuery));
            } else {
                result = params.applyTo(entityManager.createNamedQuery(query.named()));
            }
        } else if (query.isNative()) {
            String jpqlQuery = context.applyQueryStringPostProcessors(query.value());
            Class<?> resultType = getQueryResultType(context.getMethod());
            if (isEntityType(resultType)) {
                result = params.applyTo(entityManager.createNativeQuery(jpqlQuery, resultType));
            } else {
                result = params.applyTo(entityManager.createNativeQuery(jpqlQuery));
            }
        } else {
            String jpqlQuery = context.applyQueryStringPostProcessors(query.value());
            context.setQueryString(jpqlQuery);
            result = params.applyTo(entityManager.createQuery(jpqlQuery));
        }
        return context.applyRestrictions(result);
    }

    private boolean isEntityType(Class<?> cls) {
        return cls.getAnnotation(Entity.class) != null;
    }

    private Class<?> getQueryResultType(Method method) {
        if (ClassUtils.returns(method, List.class) && !ClassUtils.returns(method, Object.class)) {
            ParameterizedType pt = (ParameterizedType) method.getGenericReturnType();
            return (Class<?>) pt.getActualTypeArguments()[0];
        }

        return method.getReturnType();
    }
}