/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.view;

import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedEntity;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(LocalizedEntity.class)
public interface LocalizedEntityListItem<T extends LocalizedEntity>
        extends LocalizedEntityWithTitle<T> {

    @Mapping("coalesce(description.localizedValues[:locale], description.localizedValues[:defaultLocale])")
    String getDescription();

}
