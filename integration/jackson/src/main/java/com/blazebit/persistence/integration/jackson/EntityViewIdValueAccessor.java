/*
 * Copyright 2014 - 2022 Blazebit.
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

package com.blazebit.persistence.integration.jackson;

import com.fasterxml.jackson.core.JsonParser;

/**
 * This interface is used to supply an entity view id from platform specific sources
 * to the deserializer. The deserializer uses values provided by {@link EntityViewIdValueAccessor}
 * as fallback in case no ID value extraction via the {@link JsonParser} is possible.
 *
 * @author Moritz Becker
 * @since 1.5.0
 */
public interface EntityViewIdValueAccessor {

    /**
     * Retrieve an ID value for the entity view that is being deserialized at invocation time.
     *
     * @param jsonParser The {@link JsonParser} instance used by the deserializer
     * @param idType The ID type
     * @param <T> An ID type parameter
     * @return The ID value or null
     */
    <T> T getValue(JsonParser jsonParser, Class<T> idType);
}
