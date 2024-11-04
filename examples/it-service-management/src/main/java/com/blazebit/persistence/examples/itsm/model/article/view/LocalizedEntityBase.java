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
public interface LocalizedEntityBase<T extends LocalizedEntity>
        extends LocalizedEntityId<T> {

    String getName();

}
