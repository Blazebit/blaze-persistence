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
public interface EntityLoader {

    public Class<?> getEntityClass();

    public Object toEntity(UpdateContext context, Object view, Object id);

    void toEntities(UpdateContext context, List<Object> views, List<Object> ids);

    public Object getEntityId(UpdateContext context, Object entity);
}
