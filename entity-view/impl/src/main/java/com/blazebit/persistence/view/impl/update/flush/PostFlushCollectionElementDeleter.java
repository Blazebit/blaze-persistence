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

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.collection.CollectionRemoveListener;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PostFlushCollectionElementDeleter implements PostFlushDeleter {

    private final CollectionRemoveListener collectionRemoveListener;
    private final List<Object> elements;

    public PostFlushCollectionElementDeleter(CollectionRemoveListener collectionRemoveListener, List<Object> elements) {
        this.collectionRemoveListener = collectionRemoveListener;
        this.elements = elements;
    }

    @Override
    public void execute(UpdateContext context) {
        for (Object element : elements) {
            collectionRemoveListener.onCollectionRemove(context, element);
        }
    }
}
