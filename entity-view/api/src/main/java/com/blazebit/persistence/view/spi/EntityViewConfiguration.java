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
package com.blazebit.persistence.view.spi;

import com.blazebit.persistence.view.EntityViewManager;
import java.util.Set;

/**
 * This class is used to configure the entity view manager that it creates.
 *
 * @author Christian Beikov
 * @since 1.0
 */
public interface EntityViewConfiguration {

    /**
     * Adds the given class to the set of known entity views.
     *
     * @param clazz The class to be added
     */
    public void addEntityView(Class<?> clazz);

    /**
     * Creates a new entity view manager from this configuration.
     *
     * @return A new entity view manager
     */
    public EntityViewManager createEntityViewManager();

    /**
     * Returns the currently known entity views.
     *
     * @return The currently known entity views
     */
    public Set<Class<?>> getEntityViews();

}
