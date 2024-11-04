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
public class PostFlushViewToEntityMapperDeleter implements PostFlushDeleter {

    private final ViewToEntityMapper viewToEntityMapper;
    private final Object object;

    public PostFlushViewToEntityMapperDeleter(ViewToEntityMapper viewToEntityMapper, Object object) {
        this.viewToEntityMapper = viewToEntityMapper;
        this.object = object;
    }

    @Override
    public void execute(UpdateContext context) {
        viewToEntityMapper.remove(context, object);
    }
}
