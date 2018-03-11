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

package com.blazebit.persistence.deltaspike.data.impl.builder;

import com.blazebit.persistence.deltaspike.data.base.handler.EntityViewDelegateQueryHandler;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.OptionalUtil;
import org.apache.deltaspike.core.util.StreamUtil;
import org.apache.deltaspike.data.api.QueryInvocationException;
import org.apache.deltaspike.data.impl.util.bean.BeanDestroyable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.DelegateQueryBuilder} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@ApplicationScoped
public class EntityViewDelegateQueryBuilder extends EntityViewQueryBuilder {
    @Inject
    private BeanManager beanManager;

    private final Map<Method, Bean<EntityViewDelegateQueryHandler>> lookupCache = new ConcurrentHashMap<>();

    @Override
    public Object execute(EntityViewCdiQueryInvocationContext context) {
        try {
            EntityViewDelegateQueryHandler delegate = lookup(context);
            if (delegate != null) {
                Object result = invoke(delegate, context);
                if (result instanceof Collection && StreamUtil.isStreamReturned(context.getMethod())) {
                    return StreamUtil.wrap(result);
                } else if (OptionalUtil.isOptionalReturned(context.getMethod())) {
                    return OptionalUtil.wrap(result);
                } else {
                    return result;
                }
            }
        } catch (PersistenceException e) {
            throw e;
        } catch (Exception e) {
            throw new QueryInvocationException(e, context);
        }
        throw new QueryInvocationException("No DelegateQueryHandler found", context);
    }

    private EntityViewDelegateQueryHandler lookup(EntityViewCdiQueryInvocationContext context) {
        Bean<EntityViewDelegateQueryHandler> selectedBean = lookupCache.get(context.getMethod());

        if (selectedBean == null) {
            Set<Bean<EntityViewDelegateQueryHandler>> beans = BeanProvider
                    .getBeanDefinitions(EntityViewDelegateQueryHandler.class, true, true);
            for (Bean<EntityViewDelegateQueryHandler> bean : beans) {
                if (ClassUtils.containsPossiblyGenericMethod(bean.getBeanClass(), context.getMethod())) {
                    selectedBean = bean;
                }
            }

            if (selectedBean != null) {
                lookupCache.put(context.getMethod(), selectedBean);
            }
        }


        if (selectedBean != null) {
            CreationalContext<EntityViewDelegateQueryHandler> cc = beanManager.createCreationalContext(selectedBean);
            EntityViewDelegateQueryHandler instance = (EntityViewDelegateQueryHandler) beanManager.getReference(
                    selectedBean, EntityViewDelegateQueryHandler.class, cc);

            if (selectedBean.getScope().equals(Dependent.class)) {
                context.addDestroyable(new BeanDestroyable<EntityViewDelegateQueryHandler>(selectedBean, instance, cc));
            }

            return instance;
        }
        return null;
    }

    private Object invoke(EntityViewDelegateQueryHandler delegate, EntityViewCdiQueryInvocationContext context) {
        try {
            Method extract = ClassUtils.extractPossiblyGenericMethod(delegate.getClass(), context.getMethod());
            return extract.invoke(delegate, context.getMethodParameters());
        } catch (InvocationTargetException e) {
            if (e.getCause() != null && e.getCause() instanceof PersistenceException) {
                throw (PersistenceException) e.getCause();
            }
            throw new QueryInvocationException(e, context);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}