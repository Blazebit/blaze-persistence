/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies whether an empty flat view i.e. a view without an id where all members are null should be created or if <code>null</code> should be assigned.
 *
 * By default, empty flat views are created for singular attributes and can be disabled with this annotation.
 * When annotating a singular attribute with <code>&#064;EmptyFlatViewCreation(false)</code> i.e. disable the empty flat view creation,
 * <code>null</code> will be assigned to that attribute instead of an empty flat view.
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface EmptyFlatViewCreation {
    /**
     * Returns whether to create an empty flat view for the annotated singular attribute.
     *
     * @return whether to create an empty flat view for the annotated singular attribute
     */
    boolean value();
}
