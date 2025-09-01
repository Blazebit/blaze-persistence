/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.repository;

import com.blazebit.persistence.view.EntityViewSetting;

/**
 * @author Giovanni Lovato
 * @author Eugen Mayer
 * @since 1.6.9
 */
public interface EntityViewSettingProcessor<T> {

    /**
     * Processes the {@link EntityViewSetting} to allow additional Entity View customization during query creation.
     *
     * @param setting the {@link EntityViewSetting} to be processed
     * @return the final {@link EntityViewSetting} to allow further processing
     */
    EntityViewSetting<? extends T, ?> acceptEntityViewSetting(EntityViewSetting<T, ?> setting);
}
