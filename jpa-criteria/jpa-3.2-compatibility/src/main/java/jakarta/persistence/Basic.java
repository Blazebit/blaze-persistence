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
 * The simplest type of mapping of a persistent field or property to a
 * single database column.
 *
 * <p>The {@code Basic} annotation may be applied to a property or
 * instance variable whose type is any one of the following:
 * <ul>
 * <li>a Java primitive type, or wrapper of a primitive type,
 * <li>{@link String},
 * <li>{@link java.math.BigInteger} or {@link java.math.BigDecimal},
 * <li>{@link java.time.LocalDate}, {@link java.time.LocalTime},
 *     {@link java.time.LocalDateTime}, {@link java.time.OffsetTime},
 *     {@link java.time.OffsetDateTime}, {@link java.time.Instant},
 *     or {@link java.time.Year}
 * <li>{@link java.util.Date} or {@link java.util.Calendar},
 * <li>{@code java.sql.Date}, {@code java.sql.Time},
 *     or {@code java.sql.Timestamp},
 * <li>{@code byte[]} or {@code Byte[]},
 *     {@code char[]} or {@code Character[]},
 * <li>a Java {@code enum} type, or
 * <li>any other {@linkplain java.io.Serializable serializable} type.
 * </ul>
 *
 * <p>The use of the {@code Basic} annotation is optional for persistent
 * fields and properties of these types. If the {@code Basic} annotation
 * is not specified for such a field or property, the default values of
 * the {@code Basic} annotation apply.
 *
 * <p>The database column mapped by the persistent field or property may
 * be specified using the {@link Column} annotation.
 *
 * <p>Example 1:
 * {@snippet :
 * @Basic
 * protected String name;
 * }
 *
 * <p>Example 2:
 * {@snippet :
 * @Basic(fetch = LAZY)
 * protected String getName() { return name; }
 * }
 *
 * <p>The use of {@link java.util.Date}, {@link java.util.Calendar},
 * {@code java.sql.Date}, {@code java.sql.Time}, {@code java.sql.Timestamp},
 * {@code Character[]}, or {@code Byte[]} as the type of a basic attribute
 * is now discouraged. Newly-written code should use the date/time types
 * defined in the package {@code java.time}, or the primitive array types
 * {@code char[]} and {@code byte[]}.
 *
 * @since 1.0
 */
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface Basic {

    /**
     * (Optional) Whether the value of the field or property
     * should be lazily loaded or must be eagerly fetched.
     * <ul>
     * <li>The {@link FetchType#EAGER EAGER} strategy is a
     *     requirement on the persistence provider runtime
     *     that the associated entity must be eagerly fetched.
     * <li>The {@link FetchType#LAZY LAZY} strategy is a hint
     *     to the persistence provider runtime.
     * </ul>
     *
     * <p>If not specified, defaults to {@code EAGER}.
     */
    FetchType fetch() default FetchType.EAGER;

    /**
     * (Optional) Specifies whether the value of the field or
     * property may be null.
     *
     * <p>This is a hint and is disregarded for primitive types;
     * it may be used in schema generation to infer that the
     * mapped column is {@link Column#nullable not null}.
     *
     * <p>If not specified, defaults to {@code true}.
     */
    boolean optional() default true;
}
