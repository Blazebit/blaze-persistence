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

import com.blazebit.persistence.deltaspike.data.EntityViewManagerResolver;
import com.blazebit.persistence.deltaspike.data.impl.builder.part.EntityViewQueryRoot;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.reflection.ReflectionUtils;
import org.apache.deltaspike.core.util.AnnotationUtils;
import org.apache.deltaspike.core.util.ClassUtils;
import org.apache.deltaspike.core.util.OptionalUtil;
import org.apache.deltaspike.core.util.StreamUtil;
import org.apache.deltaspike.data.api.Modifying;
import org.apache.deltaspike.data.api.Query;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.api.SingleResultType;
import org.apache.deltaspike.data.api.mapping.MappingConfig;
import org.apache.deltaspike.data.api.mapping.QueryInOutMapper;
import org.apache.deltaspike.data.impl.builder.MethodExpressionException;
import org.apache.deltaspike.data.impl.builder.part.QueryRoot;
import org.apache.deltaspike.data.impl.builder.result.QueryProcessorFactory;
import org.apache.deltaspike.data.impl.handler.EntityRepositoryHandler;
import org.apache.deltaspike.data.impl.meta.RepositoryMetadata;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadata;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodMetadataInitializer;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodPrefix;
import org.apache.deltaspike.data.impl.meta.RepositoryMethodType;
import org.apache.deltaspike.data.impl.meta.RequiresTransaction;
import org.apache.deltaspike.jpa.api.transaction.Transactional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.persistence.LockModeType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import static org.apache.deltaspike.core.util.StringUtils.isNotEmpty;

/**
 * Implementation is similar to {@link RepositoryMethodMetadataInitializer} but was modified to
 * work with entity views.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@Specializes
@ApplicationScoped
public class EntityViewAwareRepositoryMethodMetadataInitializer extends RepositoryMethodMetadataInitializer {

    @Inject
    private QueryProcessorFactory queryProcessorFactory;

    @Override
    public EntityViewAwareRepositoryMethodMetadata init(RepositoryMetadata repositoryMetadata, Method method, BeanManager beanManager) {
        EntityViewAwareRepositoryMetadata entityViewAwareRepositoryMetadata = (EntityViewAwareRepositoryMetadata) repositoryMetadata;
        EntityViewManager evm;
        if (entityViewAwareRepositoryMetadata.getEntityViewManagerResolverClass() != null) {
            Bean<?> entityViewManagerResolverBean = beanManager.resolve(beanManager.getBeans(entityViewAwareRepositoryMetadata.getEntityViewManagerResolverClass()));
            EntityViewManagerResolver resolver = (EntityViewManagerResolver) beanManager.getReference(entityViewManagerResolverBean, EntityViewManagerResolver.class, beanManager.createCreationalContext(entityViewManagerResolverBean));
            evm = resolver.resolveEntityViewManager();
        } else {
            Bean<?> entityViewManagerBean = beanManager.resolve(beanManager.getBeans(EntityViewManager.class));
            evm = (EntityViewManager) beanManager.getReference(entityViewManagerBean, EntityViewManager.class, beanManager.createCreationalContext(entityViewManagerBean));
        }

        EntityViewAwareRepositoryMethodMetadata repositoryMethodMetadata = new EntityViewAwareRepositoryMethodMetadata();

        repositoryMethodMetadata.setMethod(method);

        repositoryMethodMetadata.setReturnsOptional(OptionalUtil.isOptionalReturned(method));
        repositoryMethodMetadata.setReturnsStream(StreamUtil.isStreamReturned(method));

        repositoryMethodMetadata.setQuery(method.isAnnotationPresent(Query.class) ? method.getAnnotation(Query.class) : null);
        repositoryMethodMetadata.setModifying(method.isAnnotationPresent(Modifying.class) ? method.getAnnotation(Modifying.class) : null);

        repositoryMethodMetadata.setTransactional(AnnotationUtils.extractAnnotationFromMethodOrClass(beanManager, method, repositoryMetadata.getRepositoryClass(), Transactional.class));

        repositoryMethodMetadata.setMethodPrefix(new RepositoryMethodPrefix(repositoryMetadata.getRepositoryClass().getAnnotation(Repository.class).methodPrefix(), method.getName()));
        repositoryMethodMetadata.setMethodType(extractMethodType(repositoryMetadata, repositoryMethodMetadata));

        repositoryMethodMetadata.setEntityViewClass(extractEntityViewClass((EntityViewAwareRepositoryMetadata) repositoryMetadata, method, evm));
        repositoryMethodMetadata.setEntityViewQueryRoot(initEntityViewQueryRoot(entityViewAwareRepositoryMetadata, repositoryMethodMetadata));

        repositoryMethodMetadata.setQueryProcessor(queryProcessorFactory.build(repositoryMethodMetadata));

        repositoryMethodMetadata.setQueryInOutMapperClass(extractMapper(method, repositoryMetadata));

        initQueryRoot(repositoryMetadata, repositoryMethodMetadata);
        initQueryInOutMapperIsNormalScope(repositoryMetadata, repositoryMethodMetadata, beanManager);

        initSingleResultType(repositoryMethodMetadata);
        initRequiresTransaction(repositoryMethodMetadata);


        return repositoryMethodMetadata;
    }

    private RepositoryMethodType extractMethodType(RepositoryMetadata repositoryMetadata, RepositoryMethodMetadata repositoryMethodMetadata) {
        if (isAnnotated(repositoryMethodMetadata)) {
            return RepositoryMethodType.ANNOTATED;
        }

        if (isMethodExpression(repositoryMetadata, repositoryMethodMetadata)) {
            return RepositoryMethodType.PARSE;
        }

        return RepositoryMethodType.DELEGATE;
    }

    private void initQueryRoot(RepositoryMetadata repositoryMetadata, RepositoryMethodMetadata methodMetadata) {
        if (methodMetadata.getMethodType() == RepositoryMethodType.PARSE) {
            methodMetadata.setQueryRoot(QueryRoot.create(methodMetadata.getMethod().getName(), repositoryMetadata, methodMetadata.getMethodPrefix()));
        } else {
            methodMetadata.setQueryRoot(QueryRoot.UNKNOWN_ROOT);
        }
    }

    public static Class<?> extractEntityViewClass(EntityViewAwareRepositoryMetadata repo, Method method, EntityViewManager evm) {
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

    private EntityViewQueryRoot initEntityViewQueryRoot(EntityViewAwareRepositoryMetadata repositoryMetadata, EntityViewAwareRepositoryMethodMetadata methodMetadata) {
        if (methodMetadata.getMethodType() == RepositoryMethodType.PARSE) {
            return EntityViewQueryRoot.create(methodMetadata.getMethod().getName(), repositoryMetadata, methodMetadata.getMethodPrefix());
        }
        return null;
    }

    private void initQueryInOutMapperIsNormalScope(RepositoryMetadata repositoryMetadata, RepositoryMethodMetadata repositoryMethodMetadata, BeanManager beanManager) {
        if (repositoryMethodMetadata.getQueryInOutMapperClass() != null) {
            Set<Bean<?>> beans = beanManager.getBeans(repositoryMethodMetadata.getQueryInOutMapperClass());
            Class<? extends Annotation> scope = beanManager.resolve(beans).getScope();
            repositoryMethodMetadata.setQueryInOutMapperIsNormalScope(beanManager.isNormalScope(scope));
        }
    }

    private boolean isAnnotated(RepositoryMethodMetadata repositoryMethodMetadata) {
        if (repositoryMethodMetadata.getQuery() != null) {
            return isValid(repositoryMethodMetadata.getQuery());
        }
        return false;
    }

    private boolean isValid(Query query) {
        return isNotEmpty(query.value()) || isNotEmpty(query.named());
    }

    private boolean isMethodExpression(RepositoryMetadata repositoryMetadata,
                                       RepositoryMethodMetadata repositoryMethodMetadata) {
        if (!Modifier.isAbstract(repositoryMethodMetadata.getMethod().getModifiers())) {
            return false;
        }

        try {
            QueryRoot.create(repositoryMethodMetadata.getMethod().getName(), repositoryMetadata, repositoryMethodMetadata.getMethodPrefix());
            return true;
        } catch (MethodExpressionException e) {
            return false;
        }
    }

    private Class<? extends QueryInOutMapper<?>> extractMapper(Method queryMethod, RepositoryMetadata repositoryMetadata) {
        if (queryMethod.isAnnotationPresent(MappingConfig.class)) {
            return queryMethod.getAnnotation(MappingConfig.class).value();
        }

        if (repositoryMetadata.getRepositoryClass().isAnnotationPresent(MappingConfig.class)) {
            return repositoryMetadata.getRepositoryClass().getAnnotation(MappingConfig.class).value();
        }

        return null;
    }

    private void initSingleResultType(RepositoryMethodMetadata repositoryMethodMetadata) {
        SingleResultType singleResultType = repositoryMethodMetadata.getQuery() != null
                ? repositoryMethodMetadata.getQuery().singleResult()
                : repositoryMethodMetadata.getMethodPrefix().getSingleResultStyle();

        if (repositoryMethodMetadata.isReturnsOptional() && singleResultType == SingleResultType.JPA) {
            repositoryMethodMetadata.setSingleResultType(SingleResultType.OPTIONAL);
        } else {
            repositoryMethodMetadata.setSingleResultType(singleResultType);
        }
    }

    private void initRequiresTransaction(RepositoryMethodMetadata repositoryMethodMetadata) {
        boolean requiresTransaction = false;

        if (ClassUtils.containsMethod(EntityRepositoryHandler.class, repositoryMethodMetadata.getMethod())) {
            Method originalMethod = ClassUtils.extractMethod(EntityRepositoryHandler.class, repositoryMethodMetadata.getMethod());
            if (originalMethod.isAnnotationPresent(RequiresTransaction.class)) {
                requiresTransaction = true;
            }
        }

        Query query = repositoryMethodMetadata.getQuery();
        Modifying modifying = repositoryMethodMetadata.getModifying();

        if ((query != null && !query.lock().equals(LockModeType.NONE)) || modifying != null) {
            requiresTransaction = true;
        }

        repositoryMethodMetadata.setRequiresTransaction(requiresTransaction);
    }
}