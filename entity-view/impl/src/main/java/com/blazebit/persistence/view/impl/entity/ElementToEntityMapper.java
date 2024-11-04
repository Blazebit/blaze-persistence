/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.entity;

import com.blazebit.persistence.view.impl.update.UpdateContext;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface ElementToEntityMapper {

    public void remove(UpdateContext context, Object element);

    public void removeById(UpdateContext context, Object elementId);

    public Object applyToEntity(UpdateContext context, Object entity, Object element);

    public void applyAll(UpdateContext context, List<Object> elements);
}
