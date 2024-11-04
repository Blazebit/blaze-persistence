/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.view;

import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedEntity;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(LocalizedEntity.class)
public interface LocalizedEntityView<T extends LocalizedEntity>
        extends LocalizedEntityBase<T> {

    void setName(String name);

    LocalizedStringView getTitle();

    void setTitle(LocalizedStringView title);

    LocalizedStringView getDescription();

    void setDescription(LocalizedStringView description);

}
