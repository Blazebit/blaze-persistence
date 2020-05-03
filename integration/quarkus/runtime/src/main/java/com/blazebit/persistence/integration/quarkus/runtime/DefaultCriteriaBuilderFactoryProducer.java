/*
 * Copyright 2014 - 2020 Blazebit.
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

package com.blazebit.persistence.integration.quarkus.runtime;

import com.blazebit.persistence.Criteria;
import com.blazebit.persistence.CriteriaBuilderFactory;
import io.quarkus.arc.DefaultBean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
@Dependent
public class DefaultCriteriaBuilderFactoryProducer {

    @DefaultBean
    @Produces
    @ApplicationScoped
    public CriteriaBuilderFactory produceCriteriaBuilderFactory(Instance<EntityManagerFactory> entityManagerFactory) {
        return Criteria.getDefault().createCriteriaBuilderFactory(entityManagerFactory.get());
    }
}
