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
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Describes a single container or persistence provider property.
 * Used in {@link PersistenceContext}.
 * 
 * <p> Vendor specific properties may be included in the set of 
 * properties, and are passed to the persistence provider by the 
 * container when the entity manager is created. Properties that 
 * are not recognized by a vendor are ignored.
 *
 * @since 1.0
 */
@Target({})
@Retention(RUNTIME)
public @interface PersistenceProperty {

    /** The name of the property */
    String name();

    /** The value of the property */
    String value();

}
