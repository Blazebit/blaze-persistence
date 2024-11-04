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

package jakarta.persistence.metamodel;

/**
 * Instances of the type {@code ListAttribute} represent persistent 
 * {@link java.util.List}-valued attributes.
 *
 * @param <X> The type the represented List belongs to
 * @param <E> The element type of the represented List
 *
 * @since 2.0
 *
 */
public interface ListAttribute<X, E> 
		extends PluralAttribute<X, java.util.List<E>, E> {}
