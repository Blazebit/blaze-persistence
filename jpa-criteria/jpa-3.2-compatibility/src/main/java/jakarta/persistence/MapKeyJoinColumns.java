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
import static jakarta.persistence.ConstraintMode.PROVIDER_DEFAULT;

/**
 * Supports composite map keys that reference entities.
 *
 * <p> The {@code MapKeyJoinColumns} annotation groups
 * {@link MapKeyJoinColumn} annotations. When the
 * {@code MapKeyJoinColumns} annotation is used, both the
 * {@code name} and the {@code referencedColumnName}
 * elements must be specified in each of the grouped
 * {@code MapKeyJoinColumn} annotations.
 * 
 * @see MapKeyJoinColumn
 * @see ForeignKey
 * 
 * @since 2.0
 */
@Target( { METHOD, FIELD })
@Retention(RUNTIME)
public @interface MapKeyJoinColumns {
	/**
	 * (Required) The map key join columns that are used to map to
	 * the entity that is the map key.
	 */
	MapKeyJoinColumn[] value();

        /**
         * (Optional) Used to specify or control the generation of a
         * foreign key constraint when table generation is in effect.
         * If both this element and the {@code foreignKey} element of
		 * any of the {@link MapKeyJoinColumn} elements are specified,
		 * the behavior is undefined. If no {@code foreignKey}
		 * annotation element is specified in either location, a
		 * default foreign key strategy is selected by the
		 * persistence provider.
         *
         * @since 2.1
         */
        ForeignKey foreignKey() default @ForeignKey(PROVIDER_DEFAULT);
}
