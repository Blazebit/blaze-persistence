/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.impl.meta;

import com.blazebit.persistence.deltaspike.data.impl.EntityViewRepositoryExtension;
import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.apache.deltaspike.data.impl.meta.RepositoryComponents;
import org.apache.deltaspike.data.impl.meta.RepositoryComponentsFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class EntityViewRepositoryComponentsFactory extends RepositoryComponentsFactory {

    @Inject
    private EntityViewRepositoryExtension extension;

    @Override
    @Produces
    @ApplicationScoped
    @Initialized
    @Specializes
    public RepositoryComponents producer() {
        return extension.getComponents();
    }

    @Produces
    @ApplicationScoped
    @Initialized
    public EntityViewRepositoryComponents createEntityViewRepositoryComponents() {
        return extension.getEntityViewRepositoryComponents();
    }
}