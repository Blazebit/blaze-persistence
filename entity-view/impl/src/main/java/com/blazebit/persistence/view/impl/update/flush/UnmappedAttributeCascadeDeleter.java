/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.update.flush;

import com.blazebit.persistence.view.impl.update.UpdateContext;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface UnmappedAttributeCascadeDeleter {

    public void removeById(UpdateContext context, Object id);

    public void removeByOwnerId(UpdateContext context, Object ownerId);

    public String getAttributeValuePath();

    public boolean requiresDeleteCascadeAfterRemove();

    public UnmappedAttributeCascadeDeleter createFlusherWiseDeleter();
}
