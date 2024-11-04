/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import com.blazebit.persistence.view.impl.entity.ViewToEntityMapper;
import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ListAction<T extends List<?>> extends CollectionAction<T> {

    @Override
    public void doAction(T list, UpdateContext context, ViewToEntityMapper mapper, CollectionRemoveListener removeListener);

    public List<Map.Entry<Object, Integer>> getInsertedObjectEntries();

    public List<Map.Entry<Object, Integer>> getAppendedObjectEntries();

    public List<Map.Entry<Object, Integer>> getRemovedObjectEntries();

    public List<Map.Entry<Object, Integer>> getTrimmedObjectEntries();

}
