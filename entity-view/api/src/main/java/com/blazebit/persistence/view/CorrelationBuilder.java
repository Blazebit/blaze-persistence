/*
 * Copyright 2014 - 2016 Blazebit.
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
package com.blazebit.persistence.view;

import com.blazebit.persistence.BaseQueryBuilder;
import com.blazebit.persistence.JoinOnBuilder;

/**
 * A builder for correlating a basis with an entity class.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CorrelationBuilder {

    /**
     * Correlates a basis with the given entity class.
     *
     * @param entityClass The entity class which should be correlated
     * @param alias The alias of the entity class
     * @return The restriction builder for the correlation predicate
     */
    public JoinOnBuilder<BaseQueryBuilder<?, ?>> correlate(Class<?> entityClass, String alias);
}
