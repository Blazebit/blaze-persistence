/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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


package jakarta.persistence;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static jakarta.persistence.EnumType.ORDINAL;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies that an annotated field of a Java {@code enum}
 * type is the source of database column values for an
 * {@linkplain Enumerated enumerated} mapping. The annotated
 * field must be declared {@code final}, and must be of type:
 * <ul>
 * <li>{@code byte}, {@code short}, or {@code int} for
 *     {@link EnumType#ORDINAL}, or
 * <li>{@link String} for {@link EnumType#STRING}.
 * </ul>
 * The annotated field must not be null, and must hold a
 * distinct value for each value of the enum type.
 *
 * <p>Example:
 * {@snippet :
 * enum Status {
 *     OPEN(0), CLOSED(1), CANCELLED(-1);
 *
 *     @EnumeratedValue
 *     final int intValue;
 *
 *     Status(int intValue) {
 *         this.intValue = intValue;
 *     }
 * }
 * }
 *
 * @see Enumerated
 * @see EnumType
 *
 * @since 3.2
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface EnumeratedValue {
}
