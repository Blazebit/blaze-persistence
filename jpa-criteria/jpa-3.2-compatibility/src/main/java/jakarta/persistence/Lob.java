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

/**
 * Specifies that the annotated persistent property or field should
 * be persisted as a large object to a database-native large object
 * (LOB) type.
 *
 * <p>Portable applications should use the {@code Lob} annotation
 * when mapping to a database Lob type. The {@code Lob} annotation
 * may be used in conjunction with the {@link Basic} annotation or
 * with the {@link ElementCollection} annotation when the element
 * collection value is of basic type. A {@code Lob} may be either
 * a binary or character type.
 *
 * <p>The LOB type ({@code BLOB} or {@code CLOB}) is inferred from
 * the type of the persistent field or property. For string and
 * character-based types it defaults to {@code CLOB}; for all other
 * types it defaults to {@code BLOB}.
 *
 * <p>Example 1:
 * {@snippet :
 * @Lob @Basic(fetch = LAZY)
 * @Column(name = "REPORT")
 * protected String report;
 * }
 *
 * <p>Example 2:
 * {@snippet :
 * @Lob @Basic(fetch = LAZY)
 * @Column(name = "EMP_PIC", columnDefinition = "BLOB NOT NULL")
 * protected byte[] pic;
 * }
 *
 * @see Basic
 * @see ElementCollection
 *
 * @since 1.0
 */
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface Lob {
}
