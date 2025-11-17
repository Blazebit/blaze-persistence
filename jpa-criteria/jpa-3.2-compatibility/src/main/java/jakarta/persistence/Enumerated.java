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
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static jakarta.persistence.EnumType.ORDINAL;

/**
 * Specifies that a persistent property or field should be persisted
 * as an enumerated type. This annotation is optional if the type of
 * a persistent field or property is a Java {@code enum} type.
 *
 * <p>The {@code Enumerated} annotation may be used in conjunction
 * with the {@link Basic} annotation, or in conjunction with the
 * {@link ElementCollection} annotation when the element type of the
 * collection is an enum type.
 *
 * <p>An enum can be mapped as either a {@linkplain EnumType#STRING
 * string} or an {@linkplain EnumType#ORDINAL integer}, where
 * {@link EnumType} enumerates the available options. The mapping
 * may be {@linkplain #value explicitly specified} by this annotation.
 *
 * <p>If a persistent field or property of enum type has no explicit
 * {@code Enumerated} annotation, and if no converter is applied to
 * the field or property:
 * <ul>
 * <li>if the enum type has a final field of type {@link String}
 *     annotated {@link EnumeratedValue}, the enumerated type is
 *     inferred to be {@link EnumType#STRING};
 * <li>otherwise, the enumerated type is taken to be
 *     {@link EnumType#ORDINAL}.
 * </ul>
 *
 * <p>Example:
 * {@snippet :
 * public enum EmployeeStatus {FULL_TIME, PART_TIME, CONTRACT}
 *
 * public enum SalaryRate {JUNIOR, SENIOR, MANAGER, EXECUTIVE}
 *
 * @Entity
 * public class Employee {
 *     public EmployeeStatus getStatus() { ... }
 *     ...
 *     @Enumerated(STRING)
 *     public SalaryRate getPayScale() { ... }
 *     ...
 * }
 * }
 *
 * @see EnumeratedValue
 * @see Basic
 * @see ElementCollection
 *
 * @since 1.0
 */
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface Enumerated {

    /**
     * (Optional) The type used in mapping an enum type.
     */
    EnumType value() default ORDINAL;
}
