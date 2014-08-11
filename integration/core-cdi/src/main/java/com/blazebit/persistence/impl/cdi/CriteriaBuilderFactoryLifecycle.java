/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.impl.cdi;

import com.blazebit.persistence.CriteriaBuilderFactory;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class CriteriaBuilderFactoryLifecycle implements ContextualLifecycle<CriteriaBuilderFactory> {
    
    private final CriteriaBuilderFactory criteriaBuilderFactory;

    public CriteriaBuilderFactoryLifecycle(CriteriaBuilderFactory criteriaBuilderFactory) {
        this.criteriaBuilderFactory = criteriaBuilderFactory;
    }

    @Override
    public CriteriaBuilderFactory create(Bean<CriteriaBuilderFactory> bean, CreationalContext<CriteriaBuilderFactory> creationalContext) {
        return criteriaBuilderFactory;
    }

    @Override
    public void destroy(Bean<CriteriaBuilderFactory> bean, CriteriaBuilderFactory instance, CreationalContext<CriteriaBuilderFactory> creationalContext) {
        // No op
    }
    
}
