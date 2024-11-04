/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.entity.ElementToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PostFlushCollectionElementByIdDeleter implements PostFlushDeleter {

    private final ElementToEntityMapper elementToEntityMapper;
    private final List<Object> elementIds;

    public PostFlushCollectionElementByIdDeleter(ElementToEntityMapper elementToEntityMapper, List<Object> elementIds) {
        this.elementToEntityMapper = elementToEntityMapper;
        this.elementIds = elementIds;
    }

    @Override
    public void execute(UpdateContext context) {
        for (Object elementId : elementIds) {
            elementToEntityMapper.removeById(context, elementId);
        }
    }
}
