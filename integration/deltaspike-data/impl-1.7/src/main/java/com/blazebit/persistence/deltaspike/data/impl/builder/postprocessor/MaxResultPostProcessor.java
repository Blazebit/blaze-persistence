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

package com.blazebit.persistence.deltaspike.data.impl.builder.postprocessor;

import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewCdiQueryInvocationContext;
import com.blazebit.persistence.deltaspike.data.impl.handler.EntityViewJpaQueryPostProcessor;

import javax.persistence.Query;

/**
 * Implementation is similar to {@link org.apache.deltaspike.data.impl.builder.postprocessor.MaxResultPostProcessor} but was modified to
 * work with entity views.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class MaxResultPostProcessor implements EntityViewJpaQueryPostProcessor {

    private final int max;

    public MaxResultPostProcessor(int max) {
        this.max = max;
    }

    @Override
    public Query postProcess(EntityViewCdiQueryInvocationContext context, Query query) {
        query.setMaxResults(max);
        return query;
    }
}