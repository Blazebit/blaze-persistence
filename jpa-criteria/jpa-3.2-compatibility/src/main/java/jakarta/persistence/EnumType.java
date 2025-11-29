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

/**
 * Enumerates available options for mapping enumerated types.
 * The values of this enumeration specify how a persistent
 * property or field whose type is a Java {@code enum} type
 * should be persisted.
 *
 * @see Enumerated
 * @see EnumeratedValue
 *
 * @since 1.0
 */
public enum EnumType {
    /**
     * Persist enumerated type property or field as an integer.
     * The ordinal value of an enum instance with no
     * {@link EnumeratedValue} field is the value of its
     * {@link Enum#ordinal()} member.
     */
    ORDINAL,

    /**
     * Persist enumerated type property or field as a string.
     * The string value of an enum instance with no
     * {@link EnumeratedValue} field is the value of its
     * {@link Enum#name()} member.
     */
    STRING
}
