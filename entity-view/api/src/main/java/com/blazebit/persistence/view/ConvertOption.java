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
 * The available options that can be enabled when converting entity view types via {@link EntityViewManager#convert(Object, Class, ConvertOption...)}.
 *
 * @author Christian Beikov
 * @since 1.2.0
 * @see EntityViewManager#convert(Object, Class, ConvertOption...)
 */
public enum ConvertOption {

    /**
     * Option to ignore rather than throw an exception when the target type has an attribute that is missing a matching attribute in the source type.
     */
    IGNORE_MISSING_ATTRIBUTES,
    /**
     * Option to specify that the newly created object should be considered "new" i.e. is persisted when flushed.
     * Note that this will not cause <code>@PostCreate</code> listeners to be invoked.
     */
    CREATE_NEW;
}
