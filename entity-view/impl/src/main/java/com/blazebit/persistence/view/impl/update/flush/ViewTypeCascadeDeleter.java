/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
