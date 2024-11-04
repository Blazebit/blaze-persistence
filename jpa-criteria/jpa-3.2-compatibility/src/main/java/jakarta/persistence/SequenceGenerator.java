/*
 * Copyright (c) 2008, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Gavin King      - 3.2
//     Lukas Jungmann  - 2.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;

/**
 * Defines a primary key generator that may be referenced by name when
 * a generator element is specified for the {@link GeneratedValue}
 * annotation. A sequence generator may be specified on the entity
 * class or on the primary key field or property. The scope of the
 * generator name is global to the persistence unit (across all
 * generator types).
 *
 * <p> If no name is explicitly specified, and the annotation occurs
 * on an entity class or primary key attribute of an entity class,
 * then the name defaults to the name of the entity.
 *
 * <p>If no name is explicitly specified, and the annotation occurs
 * on a package descriptor, then the annotation defines a recipe for
 * producing a default generator when a {@link GeneratedValue}
 * annotation of any program element in the annotated package has
 * {@link GeneratedValue#strategy strategy=SEQUENCE} and a defaulted
 * {@linkplain GeneratedValue#generator generator name}. The name of
 * this default generator is the defaulted generator name, and its
 * other properties are determined by the members of the package
 * {@code SequenceGenerator} annotation.
 *
 * <p>Example:
 * {@snippet :
 * @SequenceGenerator(name = "EMP_SEQ", allocationSize = 25)
 * }
 *
 * @since 1.0
 */
@Repeatable(SequenceGenerators.class)
@Target({TYPE, METHOD, FIELD, PACKAGE})
@Retention(RUNTIME)
public @interface SequenceGenerator {

    /** 
     * (Optional) A unique generator name that can be referenced
     * by one or more classes to be the generator for primary key 
     * values.
     * <p> Defaults to the name of the entity when the annotation
     * occurs on an entity class or primary key attribute.
     */
    String name() default "";

    /**
     * (Optional) The name of the database sequence object from 
     * which to obtain primary key values.
     * <p> Defaults to a provider-chosen value.
     */
    String sequenceName() default "";

    /** (Optional) The catalog of the sequence generator. 
     *
     * @since 2.0
     */
    String catalog() default "";

    /** (Optional) The schema of the sequence generator. 
     *
     * @since 2.0
     */
    String schema() default "";

    /** 
     * (Optional) The value from which the sequence object 
     * is to start generating.
     */
    int initialValue() default 1;

    /**
     * (Optional) The amount to increment by when allocating 
     * sequence numbers from the sequence.
     */
    int allocationSize() default 50;

    /**
     * (Optional) A SQL fragment appended to the generated DDL
     * statement which creates this sequence.
     *
     * @since 3.2
     */
    String options() default "";
}
