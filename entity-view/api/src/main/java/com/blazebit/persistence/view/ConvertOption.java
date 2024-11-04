/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

/**
 * The available options that can be enabled when converting entity view types via {@link EntityViewManager#convert(Object, Class, ConvertOption...)}.
 *
 * @author Christian Beikov
 * @since 1.2.0
 * @see EntityViewManager#convert(Object, Class, ConvertOption...)
 */
public enum ConvertOption {

    /**
     * Option to ignore rather than throw an exception when the target type has an attribute that is missing a matching attribute in the source type.
     */
    IGNORE_MISSING_ATTRIBUTES,
    /**
     * Option to specify that the newly created object should be considered "new" i.e. is persisted when flushed.
     * Note that this will not cause <code>@PostCreate</code> listeners to be invoked.
     */
    CREATE_NEW;
}
