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
import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;
import com.blazebit.persistence.view.spi.type.EntityViewProxy;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ViewCollectionRemoveListener implements CollectionRemoveListener {

    private final ViewToEntityMapper viewToEntityMapper;

    public ViewCollectionRemoveListener(ViewToEntityMapper viewToEntityMapper) {
        this.viewToEntityMapper = viewToEntityMapper;
    }

    @Override
    public void onEntityCollectionRemove(UpdateContext context, Object element) {
        viewToEntityMapper.removeById(context, viewToEntityMapper.getEntityIdAccessor().getValue(element));
    }

    @Override
    public void onCollectionRemove(UpdateContext context, Object element) {
        // TODO: Remove this when implementing https://github.com/Blazebit/blaze-persistence/issues/509
        // This is only necessary because of the fallback to entity flushing when updatable collections are present
        context.getEntityManager().flush();
        context.getInitialStateResetter().addRemovedView((EntityViewProxy) element);
        viewToEntityMapper.remove(context, element);
    }

}
