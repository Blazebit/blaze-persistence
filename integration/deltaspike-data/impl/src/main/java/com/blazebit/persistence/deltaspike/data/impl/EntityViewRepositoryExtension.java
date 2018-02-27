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

package com.blazebit.persistence.deltaspike.data.impl;

import com.blazebit.apt.service.ServiceProvider;
import com.blazebit.persistence.deltaspike.data.EntityViewManagerConfig;
import com.blazebit.persistence.deltaspike.data.EntityViewManagerResolver;
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewRepositoryComponents;
import com.blazebit.persistence.view.EntityViewManager;
import org.apache.deltaspike.core.spi.activation.Deactivatable;
import org.apache.deltaspike.core.util.ClassDeactivationUtils;
import org.apache.deltaspike.data.api.AbstractEntityRepository;
import org.apache.deltaspike.data.api.AbstractFullEntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.apache.deltaspike.data.impl.RepositoryDefinitionException;
import org.apache.deltaspike.data.impl.meta.RepositoryComponents;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.lang.reflect.InvocationHandler;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.RepositoryExtension} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
@ServiceProvider(Extension.class)
public class EntityViewRepositoryExtension implements Extension, Deactivatable {

    private static final Logger LOG = Logger.getLogger(EntityViewRepositoryExtension.class.getName());

    private static RepositoryComponents staticComponents = new RepositoryComponents();
    private static EntityViewRepositoryComponents staticEntityViewRepositoryComponents = new EntityViewRepositoryComponents();

    private final List<RepositoryDefinitionException> definitionExceptions = new LinkedList<>();

    private Boolean isActivated = true;

    private final Set<Class<?>> repoClasses = new HashSet<>();
    private RepositoryComponents components = new RepositoryComponents();
    private EntityViewRepositoryComponents entityViewRepositoryComponents = new EntityViewRepositoryComponents();

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery before) {
        isActivated = ClassDeactivationUtils.isActivated(getClass());
    }

    @SuppressWarnings("unchecked")
    <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event) {
        if (!isActivated) {
            return;
        }

        if (isVetoed(event.getAnnotatedType())) {
            event.veto();
        } else if (isRepository(event.getAnnotatedType())) {
            repoClasses.add(event.getAnnotatedType().getJavaClass());
            LOG.log(Level.FINER, "getHandlerClass: Repository annotation detected on {0}",
                    event.getAnnotatedType());
        }
    }

    <X> void addDefinitionErrors(@Observes AfterDeploymentValidation afterDeploymentValidation, BeanManager beanManager) {
        if (!isActivated) {
            return;
        }

        Bean<?> entityViewManagerBean = beanManager.resolve(beanManager.getBeans(EntityViewManager.class));
        EntityViewManager defaultEvm = (EntityViewManager) beanManager.getReference(entityViewManagerBean, EntityViewManager.class, beanManager.createCreationalContext(entityViewManagerBean));
        for (Class<?> repoClass : repoClasses) {
            try {
                if (Deactivatable.class.isAssignableFrom(repoClass)
                        && !ClassDeactivationUtils.isActivated((Class<? extends Deactivatable>) repoClass)) {
                    LOG.log(Level.FINER, "Class {0} is Deactivated", repoClass);
                    return;
                }

                EntityViewManagerConfig config = extractEntityViewManagerConfig(repoClass);
                EntityViewManager configuredEvm;
                if (config != null && !EntityViewManagerResolver.class.equals(config.entityViewManagerResolver())) {
                    configuredEvm = resolveEntityViewManager(beanManager, config);
                } else {
                    configuredEvm = defaultEvm;
                }

                entityViewRepositoryComponents.add(repoClass, configuredEvm);
                staticEntityViewRepositoryComponents.add(repoClass, configuredEvm);
            } catch (RepositoryDefinitionException e) {
                definitionExceptions.add(e);
            } catch (Exception e) {
                definitionExceptions.add(new RepositoryDefinitionException(repoClass, e));
            }
        }

        for (RepositoryDefinitionException ex : definitionExceptions) {
            afterDeploymentValidation.addDeploymentProblem(ex);
        }
    }

    private EntityViewManagerConfig extractEntityViewManagerConfig(Class<?> clazz) {
        if (clazz.isAnnotationPresent(EntityViewManagerConfig.class)) {
            return clazz.getAnnotation(EntityViewManagerConfig.class);
        }
        return null;
    }

    private EntityViewManager resolveEntityViewManager(BeanManager beanManager, EntityViewManagerConfig config) {
        Bean<?> entityViewManagerResolverBean = beanManager.resolve(beanManager.getBeans(config.entityViewManagerResolver()));
        EntityViewManagerResolver resolver = (EntityViewManagerResolver) beanManager.getReference(entityViewManagerResolverBean, EntityViewManagerResolver.class, beanManager.createCreationalContext(entityViewManagerResolverBean));
        return resolver.resolveEntityViewManager();
    }

    private <X> boolean isRepository(AnnotatedType<X> annotatedType) {
        return (annotatedType.isAnnotationPresent(Repository.class) ||
                annotatedType.getJavaClass().isAnnotationPresent(Repository.class)) &&
                !InvocationHandler.class.isAssignableFrom(annotatedType.getJavaClass());
    }

    private <X> boolean isVetoed(AnnotatedType<X> annotated) {
        Class<X> javaClass = annotated.getJavaClass();
        return javaClass.equals(AbstractEntityRepository.class) ||
                javaClass.equals(AbstractFullEntityRepository.class);
    }

    public RepositoryComponents getComponents() {
        RepositoryComponents result = new RepositoryComponents();
        if (components.getRepositories().isEmpty() && !staticComponents.getRepositories().isEmpty()) {
            result.addAll(staticComponents.getRepositories());
        }

        if (!components.getRepositories().isEmpty()) {
            result.addAll(components.getRepositories());
        }

        return result;
    }

    public EntityViewRepositoryComponents getEntityViewRepositoryComponents() {
        EntityViewRepositoryComponents result = new EntityViewRepositoryComponents();
        if (entityViewRepositoryComponents.getRepositories().isEmpty() && !staticEntityViewRepositoryComponents.getRepositories().isEmpty()) {
            result.addAll(staticEntityViewRepositoryComponents.getRepositories());
        }

        if (!entityViewRepositoryComponents.getRepositories().isEmpty()) {
            result.addAll(entityViewRepositoryComponents.getRepositories());
        }

        return result;
    }

    protected void cleanup(@Observes BeforeShutdown beforeShutdown) {
        //we can reset it in any case,
        //because every application produced a copy as application-scoped bean (see RepositoryComponentsFactory)
        staticComponents.getRepositories().clear();
    }
}