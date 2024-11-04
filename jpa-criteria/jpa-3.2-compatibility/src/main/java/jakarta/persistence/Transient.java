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
 * Specifies that the annotated property or field is not
 * persistent. May annotate a property or field of an
 * entity class, mapped superclass, or embeddable class.
 *
 * <p>Example:
 * {@snippet :
 * @Entity
 * public class Employee {
 *     @Id
 *     int id;
 *     @Transient
 *     User currentUser;
 *     ...
 * }
 * }
 *
 * @since 1.0
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Transient {}
