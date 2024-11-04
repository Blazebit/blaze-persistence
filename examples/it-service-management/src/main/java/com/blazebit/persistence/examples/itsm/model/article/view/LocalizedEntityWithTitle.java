/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.view;

import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedEntity;
import com.blazebit.persistence.view.Mapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface LocalizedEntityWithTitle<T extends LocalizedEntity>
        extends LocalizedEntityBase<T> {

    @Mapping("coalesce(title.localizedValues[:locale], title.localizedValues[:defaultLocale])")
    String getTitle();

}
