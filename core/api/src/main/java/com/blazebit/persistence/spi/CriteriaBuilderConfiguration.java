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
package com.blazebit.persistence.spi;

import com.blazebit.persistence.CriteriaBuilderFactory;
import java.util.List;

/**
 * A configuration for a {@link CriteriaBuilderFactory} which is mostly used in non Java EE environments.
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface CriteriaBuilderConfiguration {

    /**
     * Registers the given query transformer in the configuration.
     *
     * @param queryTransformer The transformer that should be addded
     */
    public void registerQueryTransformer(QueryTransformer queryTransformer);

    /**
     * Returns a list of registered query transformers.
     *
     * @return A list of registered query transformers
     */
    public List<QueryTransformer> getQueryTransformers();

    /**
     * Creates a new {@linkplain CriteriaBuilderFactory} based on this configuration.
     *
     * @return A new {@linkplain CriteriaBuilderFactory}
     */
    public CriteriaBuilderFactory createCriteriaBuilderFactory();
}
