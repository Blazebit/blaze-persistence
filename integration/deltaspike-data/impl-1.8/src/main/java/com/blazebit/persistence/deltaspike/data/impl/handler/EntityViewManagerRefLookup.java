/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.handler;

import com.blazebit.persistence.deltaspike.data.base.handler.EntityViewManagerRef;
import com.blazebit.persistence.deltaspike.data.impl.meta.EntityViewAwareRepositoryMetadata;
import com.blazebit.persistence.view.EntityViewManager;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.Set;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@ApplicationScoped
public class EntityViewManagerRefLookup {
    private volatile Boolean globalEntityViewManagerInitialized;
    private boolean globalEntityViewManagerIsNormalScope;
    private EntityViewManager globalEntityViewManager;

    private void lazyInitGlobalEntityManager() {
        if (this.globalEntityViewManagerInitialized == null) {
            initGlobalEntityViewManager();
        }
    }

    private synchronized void initGlobalEntityViewManager() {
        // switch into paranoia mode
        if (this.globalEntityViewManagerInitialized == null) {
            this.globalEntityViewManagerInitialized = true;

            BeanManager beanManager = BeanManagerProvider.getInstance().getBeanManager();
            Set<Bean<?>> beans = beanManager.getBeans(EntityViewManager.class);
            Bean<?> bean = beanManager.resolve(beans);

            if (bean == null) {
                throw new IllegalStateException("Could not find EntityViewManager with default qualifier.");
            }

            globalEntityViewManagerIsNormalScope = beanManager.isNormalScope(bean.getScope());
            if (globalEntityViewManagerIsNormalScope) {
                globalEntityViewManager = (EntityViewManager) beanManager.getReference(bean,
                        EntityViewManager.class,
                        beanManager.createCreationalContext(bean));
            }
        }
    }

    public com.blazebit.persistence.deltaspike.data.base.handler.EntityViewManagerRef lookupReference(final EntityViewAwareRepositoryMetadata repository) {
        com.blazebit.persistence.deltaspike.data.base.handler.EntityViewManagerRef ref = new EntityViewManagerRef();

        if (repository.hasEntityViewManagerResolver()) {
            ref.setEntityViewManagerResolverClass(
                    repository.getEntityViewManagerResolverClass());

            if (repository.isEntityManagerResolverIsNormalScope()) {
                ref.setEntityViewManagerResolver(
                        BeanProvider.getContextualReference(ref.getEntityViewManagerResolverClass()));
            } else {
                ref.setEntityViewManagerResolverDependentProvider(
                        BeanProvider.getDependent(ref.getEntityViewManagerResolverClass()));

                ref.setEntityViewManagerResolver(
                        ref.getEntityViewManagerResolverDependentProvider().get());
            }

            ref.setEntityViewManager(
                    ref.getEntityViewManagerResolver().resolveEntityViewManager());
        } else {
            lazyInitGlobalEntityManager();
            if (globalEntityViewManagerIsNormalScope) {
                ref.setEntityViewManager(globalEntityViewManager);
            } else {
                ref.setEntityViewManagerDependentProvider(
                        BeanProvider.getDependent(EntityViewManager.class));
                ref.setEntityViewManager(
                        ref.getEntityViewManagerDependentProvider().get());
            }
        }

        return ref;
    }
}
