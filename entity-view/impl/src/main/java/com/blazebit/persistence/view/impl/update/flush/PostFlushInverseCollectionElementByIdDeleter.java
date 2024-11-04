/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PostFlushInverseCollectionElementByIdDeleter implements PostFlushDeleter {

    private final UnmappedAttributeCascadeDeleter deleter;
    private final List<Object> elementIds;

    public PostFlushInverseCollectionElementByIdDeleter(UnmappedAttributeCascadeDeleter deleter, List<Object> elementIds) {
        this.deleter = deleter;
        this.elementIds = elementIds;
    }

    @Override
    public void execute(UpdateContext context) {
        for (Object elementId : elementIds) {
            deleter.removeById(context, elementId);
        }
    }
}
