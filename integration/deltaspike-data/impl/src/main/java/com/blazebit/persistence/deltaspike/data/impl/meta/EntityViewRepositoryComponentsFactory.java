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