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

package com.blazebit.persistence.deltaspike.data;

import com.blazebit.persistence.view.EntityViewManager;

/**
 * Resolve the EntityViewManager used for a specific repository.
 * Only necessary if there are multiple EntityViewManagers with different qualifiers.
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public interface EntityViewManagerResolver {

    /**
     * @return the resolved EntityViewManager
     */
    EntityViewManager resolveEntityViewManager();
}
