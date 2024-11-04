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
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the inheritance mapping strategy for the entity class
 * hierarchy which descends from the annotated entity class.
 *
 * <p>This annotation must be applied to the entity class that is
 * the root of the entity class hierarchy. If the {@code Inheritance}
 * annotation is not specified, or if no inheritance type is specified
 * for an entity class hierarchy, the {@link InheritanceType#SINGLE_TABLE
 * SINGLE_TABLE} mapping strategy is used.
 *
 * <p>Example:
 * {@snippet :
 * @Entity
 * @Inheritance(strategy = JOINED)
 * public class Customer { ... }
 *
 * @Entity
 * public class ValuedCustomer extends Customer { ... }
 * }
 *
 * @see InheritanceType
 *
 * @since 1.0
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface Inheritance {

    /**
     * The inheritance mapping strategy for the entity inheritance
     * hierarchy.
     */
    InheritanceType strategy() default InheritanceType.SINGLE_TABLE;
}
