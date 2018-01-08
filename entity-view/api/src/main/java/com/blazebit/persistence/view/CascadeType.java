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

package com.blazebit.persistence.view;

/**
 * The cascade types for updatable entity views.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public enum CascadeType {

    /**
     * Defines that whether {@link #PERSIST} and/or {@link #UPDATE} cascading should be applied
     * is determined based on the element type and the availability of a setter.
     */
    AUTO,
    /**
     * Defines that <em>new</em> elements should be persisted.
     */
    PERSIST,
    /**
     * Defines that <em>existing</em> elements should be updated.
     */
    UPDATE;
}
