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
//     Petros Splinakis - 2.2
//     Linda DeMichiel - 2.1
//     Linda DeMichiel - 2.0


package jakarta.persistence;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Expresses a dependency on a container-managed {@link EntityManager} and
 * its associated persistence context.
 *
 * @since 1.0
 */

@Repeatable(PersistenceContexts.class)
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface PersistenceContext {

    /**
     * (Optional) The name by which the entity manager is to be accessed
     * in the environment referencing context; not needed when dependency
     * injection is used.
     */
    String name() default "";

    /**
     * (Optional) The name of the persistence unit as defined in the
     * {@code persistence.xml} file. If the {@code unitName} element
     * is specified, the persistence unit for the entity manager that is
     * accessible in JNDI must have the same name.
     */
    String unitName() default "";

    /**
     * (Optional) Specifies whether a transaction-scoped persistence
     * context or an extended persistence context is to be used.
     */
    PersistenceContextType type() default PersistenceContextType.TRANSACTION;

    /**
     * (Optional) Specifies whether the persistence context is always
     * automatically synchronized with the current transaction or whether
     * the persistence context must be explicitly joined to the current
     * transaction by means of the {@link EntityManager#joinTransaction}
     * method.
     * @since 2.1
     */
    SynchronizationType synchronization() default SynchronizationType.SYNCHRONIZED;

    /**
     * (Optional) Properties for the container or persistence provider.
     * Vendor specific properties may be included in this set of
     * properties. Properties that are not recognized by a vendor are
     * ignored.
     */ 
    PersistenceProperty[] properties() default {};
}
