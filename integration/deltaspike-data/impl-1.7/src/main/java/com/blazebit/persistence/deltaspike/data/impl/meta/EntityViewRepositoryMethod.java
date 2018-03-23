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

package com.blazebit.persistence.deltaspike.data.impl.meta;

import com.blazebit.persistence.deltaspike.data.impl.builder.part.EntityViewQueryRoot;
import com.blazebit.persistence.deltaspike.data.impl.builder.result.EntityViewQueryProcessor;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewQueryProcessorFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.reflection.ReflectionUtils;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.api.provider.DependentProvider;
import org.apache.deltaspike.core.util.OptionalUtil;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.SingleResultType;
import org.apache.deltaspike.data.api.mapping.MappingConfig;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.impl.builder.MethodExpressionException;
import org.apache.deltaspike.data.impl.meta.MethodPrefix;
import org.apache.deltaspike.data.impl.meta.MethodType;
import org.apache.deltaspike.data.impl.meta.RepositoryMethod;
import org.apache.deltaspike.data.impl.util.bean.DependentProviderDestroyable;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.persistence.LockModeType;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import static org.apache.deltaspike.core.util.StringUtils.isNotEmpty;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.meta.RepositoryMethod} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewRepositoryMethod extends RepositoryMethod {

    private final Method method;
    private final MethodType methodType;
    private final MethodPrefix methodPrefix;
    private final EntityViewRepositoryComponent repo;
    private final EntityViewQueryRoot queryRoot;
    private final EntityViewQueryProcessor queryProcessor;
    private final Class<? extends QueryInOutMapper<?>> mapper;
    private final Class<?> entityViewClass;
    private final boolean isOptional;

    private volatile Boolean queryInOutMapperIsNormalScope;

    public EntityViewRepositoryMethod(Method method, EntityViewRepositoryComponent repo, Class<?> entityViewClass) {
        super(method, repo);
        this.method = method;
        this.repo = repo;
        this.methodPrefix = new MethodPrefix(repo.getCustomMethodPrefix(), method.getName());
        this.methodType = extractMethodType();
        this.entityViewClass = entityViewClass;
        this.queryRoot = initQueryRoot();
        this.queryProcessor = EntityViewQueryProcessorFactory.newInstance(method, methodPrefix).build();
        this.mapper = extractMapper(method, repo);
        this.isOptional = OptionalUtil.isOptionalReturned(this.method);
    }

    public boolean returns(Class<?> returnType) {
        return returnType.equals(method.getReturnType());
    }

    public QueryInOutMapper<?> getQueryInOutMapperInstance(EntityViewCdiQueryInvocationContext context) {
        if (!hasQueryInOutMapper()) {
            return null;
        }
        QueryInOutMapper<?> result = null;
        lazyInit();
        if (!queryInOutMapperIsNormalScope) {
            final DependentProvider<? extends QueryInOutMapper<?>> mappedProvider = BeanProvider.getDependent(mapper);
            result = mappedProvider.get();
            context.addDestroyable(new DependentProviderDestroyable(mappedProvider));
        } else {
            result = BeanProvider.getContextualReference(mapper);
        }
        return result;
    }

    public boolean isQuery() {
        return methodType == MethodType.ANNOTATED;
    }

    private MethodType extractMethodType() {
        if (isAnnotated()) {
            return MethodType.ANNOTATED;
        }
        if (isMethodExpression()) {
            return MethodType.PARSE;
        }
        return MethodType.DELEGATE;
    }

    public static RepositoryMethod create(EntityViewRepositoryComponent repo, Method method, EntityViewManager evm) {
        Class<?> entityViewClass = extractEntityViewClass(repo, method, evm);
        return new EntityViewRepositoryMethod(method, repo, entityViewClass);
    }

    public static Class<?> extractEntityViewClass(EntityViewRepositoryComponent repo, Method method, EntityViewManager evm) {
        Class<?> returnType = ReflectionUtils.getResolvedMethodReturnType(repo.getRepositoryClass(), method);
        ManagedViewType<?> managedViewType = evm.getMetamodel().managedView(returnType);

        if (managedViewType != null) {
            return managedViewType.getJavaType();
        } else if (returnType.getTypeParameters().length > 0) {
            Class<?> firstReturnTypeArgument = ReflectionUtils.getResolvedMethodReturnTypeArguments(repo.getRepositoryClass(), method)[0];
            managedViewType = evm.getMetamodel().managedView(firstReturnTypeArgument);
            if (managedViewType != null) {
                return managedViewType.getJavaType();
            }
        }
        return null;
    }

    private EntityViewQueryRoot initQueryRoot() {
        if (methodType == MethodType.PARSE) {
            return EntityViewQueryRoot.create(method.getName(), repo, methodPrefix);
        }
        return null;
    }

    private boolean isMethodExpression() {
        if (!Modifier.isAbstract(method.getModifiers())) {
            return false;
        }
        try {
            EntityViewQueryRoot.create(method.getName(), repo, methodPrefix);
            return true;
        } catch (MethodExpressionException e) {
            return false;
        }
    }

    private boolean isAnnotated() {
        if (method.isAnnotationPresent(Query.class)) {
            Query query = method.getAnnotation(Query.class);
            return isValid(query);
        }
        return false;
    }

    private boolean isValid(Query query) {
        return isNotEmpty(query.value()) || isNotEmpty(query.named());
    }

    private Class<? extends QueryInOutMapper<?>> extractMapper(Method queryMethod, EntityViewRepositoryComponent repoComponent) {
        if (queryMethod.isAnnotationPresent(MappingConfig.class)) {
            return queryMethod.getAnnotation(MappingConfig.class).value();
        }
        if (repoComponent.getRepositoryClass().isAnnotationPresent(MappingConfig.class)) {
            return repoComponent.getRepositoryClass().getAnnotation(MappingConfig.class).value();
        }
        return null;
    }

    //don't trigger this lookup during ProcessAnnotatedType
    private void lazyInit() {
        if (queryInOutMapperIsNormalScope == null) {
            init(BeanManagerProvider.getInstance().getBeanManager());
        }
    }

    private synchronized void init(BeanManager beanManager) {
        if (queryInOutMapperIsNormalScope != null) {
            return;
        }

        if (beanManager != null) {
            final Set<Bean<?>> beans = beanManager.getBeans(mapper);
            final Class<? extends Annotation> scope = beanManager.resolve(beans).getScope();
            queryInOutMapperIsNormalScope = beanManager.isNormalScope(scope);
        } else {
            queryInOutMapperIsNormalScope = false;
        }
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public EntityViewRepositoryComponent getRepository() {
        return repo;
    }

    public EntityViewQueryRoot getEntityViewQueryRoot() {
        return queryRoot;
    }

    public EntityViewQueryProcessor getEntityViewQueryProcessor() {
        return queryProcessor;
    }

    public boolean hasQueryInOutMapper() {
        return mapper != null;
    }

    public int getDefinedMaxResults() {
        try {
            Method getDefinedMaxResultsMethod = MethodPrefix.class.getDeclaredMethod("getDefinedMaxResults");
            getDefinedMaxResultsMethod.setAccessible(true);
            return (int) getDefinedMaxResultsMethod.invoke(methodPrefix);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public SingleResultType getSingleResultStyle() {
        return methodPrefix.getSingleResultStyle();
    }

    public boolean requiresTransaction() {
        boolean hasLockMode = false;
        if (method.isAnnotationPresent(Query.class)) {
            hasLockMode = !method.getAnnotation(Query.class).lock().equals(LockModeType.NONE);
        }
        return hasLockMode || method.isAnnotationPresent(Modifying.class);
    }

    public boolean isOptional() {
        return this.isOptional;
    }

    public Class<?> getEntityViewClass() {
        return entityViewClass;
    }
}