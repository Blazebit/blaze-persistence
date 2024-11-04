/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.article.view;

import java.util.Locale;
import java.util.Map;

import com.blazebit.persistence.examples.itsm.model.article.entity.LocalizedString;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(LocalizedString.class)
@CreatableEntityView
@UpdatableEntityView
public interface LocalizedStringView {

    @UpdatableMapping
    Map<Locale, String> getLocalizedValues();

}
