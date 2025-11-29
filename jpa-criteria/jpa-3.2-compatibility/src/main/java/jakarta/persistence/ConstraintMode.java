/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.persistence;

/**
 * Used to control the application of a constraint.
 * 
 * @since 2.1
 */
public enum ConstraintMode {

    /** Apply the constraint. */
	CONSTRAINT,

    /** Do not apply the constraint. */
	NO_CONSTRAINT,

    /** Use the provider-defined default behavior. */
    PROVIDER_DEFAULT
}
