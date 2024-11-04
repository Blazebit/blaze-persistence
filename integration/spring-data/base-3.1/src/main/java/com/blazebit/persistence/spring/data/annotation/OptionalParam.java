/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.blazebit.persistence.view.EntityViewSetting;

/**
 * Annotation to let method parameters be bound to the {@link EntityViewSetting}.
 *
 * @author Giovanni Lovato
 * @author Eugen Mayer
 * @since 1.6.9
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OptionalParam {

    /**
     * The name of the parameter.
     */
    String value();
}
