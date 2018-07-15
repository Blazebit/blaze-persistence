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

import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;


/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ViewTypeCascadeDeleter implements UnmappedAttributeCascadeDeleter {

    private final ViewToEntityMapper viewToEntityMapper;

    public ViewTypeCascadeDeleter(ViewToEntityMapper viewToEntityMapper) {
        this.viewToEntityMapper = viewToEntityMapper;
    }

    @Override
    public void removeById(UpdateContext context, Object id) {
        viewToEntityMapper.removeById(context, id);
    }

    @Override
    public void removeByOwnerId(UpdateContext context, Object ownerId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAttributeValuePath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean requiresDeleteCascadeAfterRemove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UnmappedAttributeCascadeDeleter createFlusherWiseDeleter() {
        return this;
    }
}
