/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.collection;

import com.blazebit.persistence.view.impl.update.UpdateContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface CollectionRemoveListener {

    public void onEntityCollectionRemove(UpdateContext context, Object element);

    public void onCollectionRemove(UpdateContext context, Object element);

}
