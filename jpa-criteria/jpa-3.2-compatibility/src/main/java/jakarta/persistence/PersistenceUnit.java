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
 * Expresses a dependency on an {@link EntityManagerFactory} and
 * its associated persistence unit.
 *
 * @since 1.0
 */
@Repeatable(PersistenceUnits.class)
@Target({TYPE, METHOD, FIELD})
@Retention(RUNTIME)
public @interface PersistenceUnit {

    /**
     * (Optional) The name by which the entity manager factory
     * is to be accessed in the environment referencing context;
     * not needed when dependency injection is used.
     */
    String name() default "";

    /**
     * (Optional) The name of the persistence unit as defined
     * in the {@code persistence.xml} file. If specified, the
     * persistence unit for the entity manager factory that is
     * accessible in JNDI must have the same name.
     */
    String unitName() default "";

}
