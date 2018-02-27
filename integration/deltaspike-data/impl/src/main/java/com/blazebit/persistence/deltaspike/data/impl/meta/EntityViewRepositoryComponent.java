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

import com.blazebit.persistence.deltaspike.data.EntityViewManagerConfig;
import com.blazebit.persistence.deltaspike.data.EntityViewManagerResolver;
import com.blazebit.persistence.view.EntityViewManager;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.data.api.EntityManagerConfig;
import org.apache.deltaspike.data.api.EntityManagerResolver;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.meta.MethodType;
import org.apache.deltaspike.data.impl.meta.RepositoryComponent;
import org.apache.deltaspike.data.impl.meta.RepositoryEntity;
import org.apache.deltaspike.data.impl.meta.RepositoryMethod;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.persistence.FlushModeType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.meta.RepositoryComponent} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewRepositoryComponent extends RepositoryComponent {
    private static final Logger LOG = Logger.getLogger(EntityViewRepositoryComponent.class.getName());

    private volatile Boolean entityManagerResolverIsNormalScope;

    private final Class<?> repoClass;
    private final RepositoryEntity repositoryEntity;
    private final Class<? extends EntityManagerResolver> entityManagerResolver;
    private final FlushModeType entityManagerFlushMode;
    private final Class<? extends EntityViewManagerResolver> entityViewManagerResolver;

    private final Map<Method, RepositoryMethod> methods = new HashMap<Method, RepositoryMethod>();

    public EntityViewRepositoryComponent(Class<?> repoClass, RepositoryEntity repositoryEntity, EntityViewManager evm) {
        super(repoClass, repositoryEntity);
        this.repoClass = repoClass;
        this.repositoryEntity = repositoryEntity;
        this.entityManagerResolver = extractEntityManagerResolver(repoClass);
        this.entityManagerFlushMode = extractEntityManagerFlushMode(repoClass);
        this.entityViewManagerResolver = extractEntityViewManagerResolver(repoClass);
        init(BeanManagerProvider.getInstance().getBeanManager(), evm);
    }

    private synchronized void init(BeanManager beanManager, EntityViewManager evm) {
        if (entityManagerResolverIsNormalScope != null) {
            return;
        }
        initialize(evm);
        if (entityManagerResolver != null && beanManager != null) {
            final Set<Bean<?>> beans = beanManager.getBeans(entityManagerResolver);
            final Class<? extends Annotation> scope = beanManager.resolve(beans).getScope();
            entityManagerResolverIsNormalScope = beanManager.isNormalScope(scope);
        } else {
            entityManagerResolverIsNormalScope = false;
        }
    }

    public boolean isEntityManagerResolverIsNormalScope() {
        return entityManagerResolverIsNormalScope;
    }

    public String getEntityName() {
        return repositoryEntity.getEntityName();
    }

    /**
     * Looks up method meta data by a Method object.
     *
     * @param method The Repository method.
     * @return Method meta data.
     */
    public RepositoryMethod lookupMethod(Method method) {
        return methods.get(method);
    }

    /**
     * Looks up the method type by a Method object.
     *
     * @param method The Repository method.
     * @return Method meta data.
     */
    public MethodType lookupMethodType(Method method) {
        return lookupMethod(method).getMethodType();
    }

    /**
     * Gets the entity class related the Repository.
     *
     * @return The class of the entity related to the Repository.
     */
    public Class<?> getEntityClass() {
        return repositoryEntity.getEntityClass();
    }

    public RepositoryEntity getRepositoryEntity() {
        return repositoryEntity;
    }

    /**
     * Returns the original Repository class this meta data is related to.
     *
     * @return The class of the Repository.
     */
    public Class<?> getRepositoryClass() {
        return repoClass;
    }

    public boolean hasEntityManagerResolver() {
        return getEntityManagerResolverClass() != null;
    }

    public Class<? extends EntityManagerResolver> getEntityManagerResolverClass() {
        return entityManagerResolver;
    }

    public boolean hasEntityViewManagerResolver() {
        return getEntityViewManagerResolverClass() != null;
    }

    public Class<? extends EntityViewManagerResolver> getEntityViewManagerResolverClass() {
        return entityViewManagerResolver;
    }

    public boolean hasEntityManagerFlushMode() {
        return entityManagerFlushMode != null;
    }

    public FlushModeType getEntityManagerFlushMode() {
        return entityManagerFlushMode;
    }

    private void initialize(EntityViewManager evm) {
        Collection<Class<?>> allImplemented = collectClasses();
        for (Class<?> implemented : allImplemented) {
            Method[] repoClassMethods = implemented.getDeclaredMethods();
            for (Method repoClassMethod : repoClassMethods) {
                RepositoryMethod repoMethod = EntityViewRepositoryMethod.create(this, repoClassMethod, evm);
                methods.put(repoClassMethod, repoMethod);
            }
        }
    }

    private Set<Class<?>> collectClasses() {
        Set<Class<?>> result = new HashSet<Class<?>>();
        collectClasses(repoClass, result);
        LOG.log(Level.FINER, "collectClasses(): Found {0} for {1}", new Object[]{result, repoClass});
        return result;
    }

    private void collectClasses(Class<?> cls, Set<Class<?>> result) {
        if (cls == null || cls == Object.class) {
            return;
        }
        result.add(cls);
        for (Class<?> child : cls.getInterfaces()) {
            collectClasses(child, result);
        }
        collectClasses(cls.getSuperclass(), result);
    }

    private Class<? extends EntityManagerResolver> extractEntityManagerResolver(Class<?> clazz) {
        EntityManagerConfig config = extractEntityManagerConfig(clazz);
        if (config != null && !EntityManagerResolver.class.equals(config.entityManagerResolver())) {
            return config.entityManagerResolver();
        }
        return null;
    }

    private FlushModeType extractEntityManagerFlushMode(Class<?> clazz) {
        EntityManagerConfig config = extractEntityManagerConfig(clazz);
        if (config != null) {
            return config.flushMode();
        }
        return null;
    }

    private EntityManagerConfig extractEntityManagerConfig(Class<?> clazz) {
        if (clazz.isAnnotationPresent(EntityManagerConfig.class)) {
            return clazz.getAnnotation(EntityManagerConfig.class);
        }
        return null;
    }

    private Class<? extends EntityViewManagerResolver> extractEntityViewManagerResolver(Class<?> clazz) {
        EntityViewManagerConfig config = extractEntityViewManagerConfig(clazz);
        if (config != null && !EntityViewManagerResolver.class.equals(config.entityViewManagerResolver())) {
            return config.entityViewManagerResolver();
        }
        return null;
    }

    private EntityViewManagerConfig extractEntityViewManagerConfig(Class<?> clazz) {
        if (clazz.isAnnotationPresent(EntityViewManagerConfig.class)) {
            return clazz.getAnnotation(EntityViewManagerConfig.class);
        }
        return null;
    }

    public String getCustomMethodPrefix() {
        return repoClass.getAnnotation(Repository.class).methodPrefix();
    }
}