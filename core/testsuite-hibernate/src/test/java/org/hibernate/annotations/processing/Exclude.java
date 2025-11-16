/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package org.hibernate.annotations.processing;

import org.hibernate.Incubating;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target({PACKAGE, TYPE})
@Retention(CLASS)
@Incubating
public @interface Exclude {
}
