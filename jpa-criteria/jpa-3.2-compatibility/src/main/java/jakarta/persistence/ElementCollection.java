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
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0

package jakarta.persistence;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Declares a collection of instances of a {@linkplain Basic basic type}
 * or {@linkplain Embeddable embeddable class}. Must be specified if the
 * collection is to be mapped by means of a collection table.
 *
 * <p>The {@link CollectionTable} annotation specifies a mapping to a
 * database table.
 *
 * <p>Example:
 * {@snippet :
 * @Entity
 * public class Person {
 *     @Id
 *     protected String ssn;
 *     protected String name;
 *     ...
 *     @ElementCollection
 *     protected Set<String> nickNames = new HashSet<>();
 *     ...
 * }
 * }
 *
 * @see CollectionTable
 *
 * @since 2.0
 */
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
public @interface ElementCollection {

    /**
     * (Optional) The basic or embeddable class that is the element
     * type of the collection. This element is optional only if the
     * collection field or property is defined using Java generics,
     * and must be specified otherwise. It defaults to the
     * parameterized type of the collection when defined using
     * generics.
     */
    Class<?> targetClass() default void.class;
    
    /**
     * (Optional) Whether the collection should be lazily loaded
     * or must be eagerly fetched.
     * <ul>
     * <li>The {@link FetchType#EAGER EAGER} strategy is a
     *     requirement on the persistence provider runtime
     *     that the associated entity must be eagerly fetched.
     * <li>The {@link FetchType#LAZY LAZY} strategy is a hint
     *     to the persistence provider runtime.
     * </ul>
     *
     * <p>If not specified, defaults to {@code LAZY}.
     */
    FetchType fetch() default FetchType.LAZY;
}
