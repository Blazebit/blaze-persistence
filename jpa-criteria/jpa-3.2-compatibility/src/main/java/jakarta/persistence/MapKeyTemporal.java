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
 * This annotation must be specified for persistent map keys of type 
 * {@link java.util.Date} and {@link java.util.Calendar}. It may only
 * be specified for map keys of these types.
 * 
 * <p> The {@code MapKeyTemporal} annotation can be applied to an
 * element collection or relationship of type {@link java.util.Map}
 * in conjunction with the {@link ElementCollection}, {@link OneToMany},
 * or {@link ManyToMany} annotation.
 *
 * <p>Example:
 * {@snippet :
 * @OneToMany
 * @MapKeyTemporal(DATE)
 * protected Map<java.util.Date, Employee> employees;
 * }
 *
 * @since 2.0
 *
 * @deprecated Newly-written code should use the date/time types
 *             defined in {@link java.time}.
 */
@Deprecated(since = "3.2")
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface MapKeyTemporal {

    /** (Required) The type used in mapping
     * {@code java.util.Date} or
     * {@code java.util.Calendar}. 
     */
    TemporalType value();
}

