/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package org.hibernate.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.generator.EventType;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.generator.EventType.INSERT;

@Target( {FIELD, METHOD} )
@Retention( RUNTIME )
public @interface Generated {
	EventType[] event() default INSERT;
	@Deprecated(since = "6.2")
	GenerationTime value() default GenerationTime.INSERT;
	String sql() default "";
	boolean writable() default false;
}