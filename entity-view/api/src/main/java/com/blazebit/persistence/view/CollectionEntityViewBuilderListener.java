/*
 * Copyright 2014 - 2021 Blazebit.
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

import java.util.Collection;

/**
 * A listener that adds the built entity view to a collection.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class CollectionEntityViewBuilderListener implements EntityViewBuilderListener {

    private final Collection<Object> collection;

    /**
     * Creates a listener.
     *
     * @param collection The collection to add the built entity view to
     */
    public CollectionEntityViewBuilderListener(Collection<Object> collection) {
        this.collection = collection;
    }

    @Override
    public void onBuildComplete(Object object) {
        collection.add(object);
    }
}
