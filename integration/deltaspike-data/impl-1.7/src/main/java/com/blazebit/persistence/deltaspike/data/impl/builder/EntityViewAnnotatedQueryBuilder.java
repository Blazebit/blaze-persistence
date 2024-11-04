/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.builder;

import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import com.blazebit.persistence.deltaspike.data.impl.param.ExtendedParameters;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.impl.meta.MethodType;
import org.apache.deltaspike.data.impl.meta.QueryInvocation;
import org.apache.deltaspike.data.impl.util.jpa.QueryStringExtractorFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
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
@QueryInvocation(MethodType.ANNOTATED)
@ApplicationScoped
public class EntityViewAnnotatedQueryBuilder extends EntityViewQueryBuilder {

    private final QueryStringExtractorFactory factory = new QueryStringExtractorFactory();

    @Override
    public Object execute(EntityViewCdiQueryInvocationContext context) {
        Method method = context.getMethod();
        Query query = method.getAnnotation(Query.class);
        javax.persistence.Query jpaQuery = createJpaQuery(query, context);
        return context.executeQuery(jpaQuery);
    }

    private javax.persistence.Query createJpaQuery(Query query, EntityViewCdiQueryInvocationContext context) {
        EntityManager entityManager = context.getEntityManager();
        ExtendedParameters params = context.getParams();
        javax.persistence.Query result = null;
        if (isNotEmpty(query.named())) {
            if (context.hasQueryStringPostProcessors()) {
                javax.persistence.Query namedQuery = entityManager.createNamedQuery(query.named());
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

    private Class<?> getQueryResultType(Method m) {
        Class<?> rt = m.getReturnType();
        if (rt.isAssignableFrom(List.class) && rt != Object.class) {
            ParameterizedType pt = (ParameterizedType) m.getGenericReturnType();
            return (Class<?>) pt.getActualTypeArguments()[0];
        }
        return rt;
    }
}